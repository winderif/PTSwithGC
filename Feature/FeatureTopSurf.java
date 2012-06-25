package Feature;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import Utils.RGBToHSVHistogram;

public class FeatureTopSurf extends Feature {
	private File imgFile = null;
	private File topsurfFile = null;
	
	private String extractCommand = "";
	private static final String topsurfExec = "topsurf\\topsurf.exe";
	private static final String topsurfDict = "topsurf\\dict";	

	private String featureDir = "";
	private boolean featureFileExisted = false; 
	
	private static final int TOPSURF_BIN = 10000;
	private double[] histogram = null;	
	
	public FeatureTopSurf(File arg0) {
		imgFile = arg0;
	}
	
	public void run() {
		super.run();
	}
	
	protected void init() {
		super.init();
	}
	
	protected void initialize() {
		String[] tmp = imgFile.getAbsolutePath().split(".jpg");
		
		extractCommand = 
			topsurfExec + " extract " + 
			topsurfDict + " 256 100 " + "\"" + 
			imgFile.getAbsolutePath() + "\" \"" +
			tmp[0] + ".top" + "\"";
		//System.out.println(extractCommand);
	}	
	
	protected void extractFeature() {
		checkTopSurf();
	
		if(featureFileExisted == false) {			
			execTopSurf();
		}
		
		loadTopSurf();
	}		
	
	private void checkTopSurf() {		
		String[] tmp = imgFile.getName().split(".jpg");
		String setDir = featureDir + tmp[0] + ".top";
		tmp = imgFile.getAbsolutePath().split(".jpg");
		String sameDir = tmp[0] + ".top"; 
		
		File setDirFile = new File(setDir);
		File sameDirFile = new File(sameDir);
		//System.out.println(setDirFile.isFile());
		//System.out.println(sameDirFile.isFile());
		if(setDirFile.isFile() | sameDirFile.isFile()) {
			if(setDirFile.isFile())
				topsurfFile = setDirFile;
			else if(sameDirFile.isFile())
				topsurfFile = sameDirFile;
			else
				topsurfFile = null;
			featureFileExisted = true;
		}			
	}
	
	private void execTopSurf() {
		try {
			Runtime rt = Runtime.getRuntime(); 
			Process proc = rt.exec(extractCommand);
			InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
			while(isr.read() != -1) ;
			/**
			BufferedReader br = new BufferedReader(isr); 
			String line = null;			 					
			while((line = br.readLine()) != null) ;
			br.close();
			*/
		    isr.close();			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		String[] tmp = imgFile.getAbsolutePath().split(".jpg");
		String sameDir = tmp[0] + ".top"; 
		//System.out.println(sameDir);
		topsurfFile = new File(sameDir);
		//System.out.println(topsurfFile.isFile());
	}
	
	private void loadTopSurf() {
		//System.out.println(topsurfFile.isFile() + " " + topsurfFile);
		if(topsurfFile.isFile() != false && topsurfFile != null) {		
			try {
				FileReader inFile = new FileReader(topsurfFile);				
				int in = 0;
				String tmpFile = "";
				
				while((in = inFile.read()) != -1) {
					tmpFile = tmpFile + (char)in;
				}
				// split each line
				String[] tmpLine = tmpFile.split("\r\n");											
								
				histogram = new double[TOPSURF_BIN];
				
				int index = 0;
				for(int i=1; i<tmpLine.length; i++) {
					//System.out.println(tmpLine[i]);
					String[] tmpValue = tmpLine[i].split("\t");
							
					index = Integer.parseInt(tmpValue[0]);
					histogram[index] = Double.parseDouble(tmpValue[1]);					
				}
				/** debugging
				for(int i=1; i<histogram.length; i++) {
					System.out.println(i + "\t" + histogram[i]);							
				}
				*/
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Cannot laod topsurf file.");
			System.exit(0);
		}
	}
	
	public double[] getFeature() {
		return this.histogram;
	}
	
	public void setFaetureDir(String dir) {
		this.featureDir = dir;
	}
	
	public static void main(String[] args) {
		File f = new File("000.jpg");
		FeatureTopSurf ft = new FeatureTopSurf(f);
		ft.setFaetureDir("topsurf/");
		ft.run();		
	}
}
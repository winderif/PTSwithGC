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
	
	private double[][] histogram = null;	
	
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
		System.out.println(extractCommand);
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
		System.out.println(setDirFile.isFile());
		System.out.println(sameDirFile.isFile());
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
		    isr.close();			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadTopSurf() {
		if(topsurfFile != null) {					
			try {
				FileReader inFile = new FileReader(topsurfFile);				
				int in = 0;
				String tmpFile = "";	
				
				while((in = inFile.read()) != -1) {
					tmpFile = tmpFile + (char)in;
				}
				// split each line
				String[] tmpLine = tmpFile.split("\r\n");
				
				histogram = new double[4][tmpLine.length];				
								
				for(int i=1; i<tmpLine.length; i++) {
					System.out.println(tmpLine[i]);
					String[] tmpValue = tmpLine[i].split("\t");
										
					histogram[0][i] = Integer.parseInt(tmpValue[0]);					
					histogram[1][i] = Integer.parseInt(tmpValue[1]);
					histogram[2][i] = Double.parseDouble(tmpValue[2]);
					histogram[3][i] = Double.parseDouble(tmpValue[3]);
				}
				
				for(int i=1; i<histogram[0].length; i++) {
					System.out.println(
							histogram[0][i] + "\t" +
							histogram[1][i] + "\t" + 
							histogram[2][i] + "\t" +
							histogram[3][i] + "\t");
				}
				
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("Cannot laod topsurf file.");
	}
	
	public double[] getFeature() {
		return this.histogram[1];
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
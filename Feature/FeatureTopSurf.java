package Feature;

import java.io.*;
import java.util.Map;

import Utils.Create;

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
	private Map<Integer, Double> descriptor = null; 
	
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
		String[] fileToken = imgFile.getName().split(".jpg");
		String setDir = featureDir + fileToken[0] + ".top";
		
		String[] abFileToken = imgFile.getAbsolutePath().split(".jpg");
		String sameDir = abFileToken[0] + ".top"; 
		
		File setDirFile = new File(setDir);
		File sameDirFile = new File(sameDir);
		//System.out.println(setDirFile.isFile());
		//System.out.println(sameDirFile.isFile());
		if(setDirFile.isFile() | sameDirFile.isFile()) {
			if(setDirFile.isFile()) {
				topsurfFile = setDirFile;
			}
			else if(sameDirFile.isFile()) {							
				topsurfFile = sameDirFile;
			}
			else {
				topsurfFile = null;
			}
			featureFileExisted = true;
		}			
	}
	
	private void execTopSurf() {
		try {
			Runtime rt = Runtime.getRuntime(); 
			Process proc = rt.exec(extractCommand);
			InputStreamReader isr = new InputStreamReader(proc.getErrorStream());
			while(isr.read() != -1) {;}
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
				StringBuilder tmpFile = new StringBuilder();				
								
				while((in = inFile.read()) != -1) {					
					tmpFile.append((char)in);
				}
			
				// split each line
				String[] tmpLine = tmpFile.toString().split("\r\n");				
				if(tmpLine[0].equals("") == false) {
					histogram = new double[TOPSURF_BIN];
					descriptor = Create.linkedHashMap();					
					
					int index = 0;				
					double tf = 0.0;
					double idf = 0.0;
					for(int i=0; i<tmpLine.length; i++) {
						//System.out.println(tmpLine[i]);
						String[] tmpValue = tmpLine[i].split("\t");
								
						//System.out.println(tmpValue[0]);
						index = Integer.parseInt(tmpValue[0]);				
						// count of histogram
						tf = Double.parseDouble(tmpValue[2]);
						idf = Double.parseDouble(tmpValue[3]);
						histogram[index] = tf * idf;
						descriptor.put(index, tf * idf);
					}
					/** debugging 								
					for(int i=0; i<histogram.length; i++) {
						System.out.println(i + "\t" + histogram[i]);						
					}				
					*/	
				}
				else {
					System.out.println("\nNo TopSurf feature." + imgFile.getAbsolutePath());
					// All bin value is 0.
					histogram = new double[TOPSURF_BIN];
					descriptor = Create.linkedHashMap();							
				}															
				inFile.close();
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
	
	public Map<Integer, Double> getDescriptor() {
		return this.descriptor;
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
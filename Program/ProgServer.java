// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.net.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.input.CountingInputStream;

import Utils.VideoFrame;

public abstract class ProgServer extends Program {

    final private  int         serverPort   = 23456;             // server port number
    private ServerSocket       sock         = null;              // original server socket
    private Socket             clientSocket = null;              // socket created by accept
    
    protected String databaseDirName = null;
    protected File[] databaseTagDirFile = null;	
    protected Vector<File[]> databaseDataFile = new Vector<File[]>();
    protected Vector<Vector<VideoFrame>> databaseData = new Vector<Vector<VideoFrame>>();
	
	private HashMap<String, Vector<VideoFrame>> imageClustersMap = new HashMap<String, Vector<VideoFrame>>();
	private String[] allTags = null;
	
	private final static int FILTER_NUM = 2;	
	// Bin of HSV color histogram = 8 + 4 + 4 = 16 
	private final static int BIN_HISTO = 16;
	
	private double[][] mTagAverageHistogram = null;
    
    public void run() throws Exception {
    	create_socket_and_listen();

    	super.run();

    	cleanup();
    }

    protected void init() throws Exception {
	//Program.iterCount = ProgCommon.ois.readInt();
	//System.out.println(Program.iterCount);

    	super.init();
    }

    private void create_socket_and_listen() throws Exception {
    	sock = new ServerSocket(serverPort);            // create socket and bind to port
		System.out.println("waiting for client to connect");
		clientSocket = sock.accept();                   // wait for client to connect
		System.out.println("client has connected");

		CountingOutputStream cos = new CountingOutputStream(clientSocket.getOutputStream());
		CountingInputStream  cis = new CountingInputStream(clientSocket.getInputStream());
	
		ProgCommon.oos = new ObjectOutputStream(cos);
		ProgCommon.ois = new ObjectInputStream(cis);
    }
    
    protected void initialize() throws Exception {
    	readData();
    	generateTagClusters();
    	getAverageColorHistogram();
    }
    
    private void readData() {
		File dirFile = new File(databaseDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("\t[S][ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {			
			System.out.println("\t[S][START]\tRead database datas.");
			
			File[] tmpFileArray = null; 
			Vector<VideoFrame> tmpVideoFrame = null;
			databaseTagDirFile = new File[dirFile.listFiles().length];
			
			for(int i=0; i<dirFile.listFiles().length; i++) {				
				databaseTagDirFile[i] = dirFile.listFiles()[i];
	
				if(databaseTagDirFile[i].listFiles().length != 0) {					
					int sizeOfTagDir = databaseTagDirFile[i].listFiles().length;
					tmpFileArray = new File[sizeOfTagDir];		
					tmpVideoFrame = new Vector<VideoFrame>();
					
					for(int j=0; j<sizeOfTagDir; j++) {
						if(databaseTagDirFile[i].listFiles()[j].getName().endsWith(".jpg")) {
							tmpFileArray[j] = databaseTagDirFile[i].listFiles()[j];
							tmpVideoFrame.add(new VideoFrame(tmpFileArray[j], j));
							//System.out.println("[DIR] " + tmpFileArray[j].getName());
						}						
					}
					databaseDataFile.add(tmpFileArray.clone());
					databaseData.add(tmpVideoFrame);
				}											
				//System.out.println(this.databaseTagDirFile[i].getName());
			}
		}
    }
    
	private void generateTagClusters() {
		System.out.println("\t[S][START]\tGenerate Photo Tag Clusters");
		Vector<VideoFrame> tmpPhotos = null;
		String[] tmpTags = null;
		Vector<String> stopwordVector = null;
				
        try {
    		File stopwords = new File("Stopwords.txt");
            Scanner scanner = new Scanner(stopwords);
        	
        	for(int i=0; i<this.databaseData.size(); i++) {
        		for(int j=0; j<this.databaseData.elementAt(i).size(); j++) {
        			//System.out.print(this.databaseDatasFile.elementAt(i)[j] + " ");
				
        			tmpTags = this.databaseData.elementAt(i).elementAt(j).getTags();
        			for(int k=0; k<tmpTags.length; k++) {
        				if(imageClustersMap.get(tmpTags[k]) == null) {
        					tmpPhotos = new Vector<VideoFrame>();
        					tmpPhotos.add(this.databaseData.elementAt(i).elementAt(j));
        				} else {
        					tmpPhotos = imageClustersMap.get(tmpTags[k]);
        					tmpPhotos.add(this.databaseData.elementAt(i).elementAt(j));
        				}
        				imageClustersMap.put(tmpTags[k], tmpPhotos);	
        			}
        		}
        	}	        	        
        	
        	String remove = "";
			stopwordVector = new Vector<String>();

	        while(scanner.hasNext()){
	        	remove = scanner.nextLine().trim();
	        	stopwordVector.add(remove);
	        	if(imageClustersMap.containsKey(remove)){
	        		imageClustersMap.remove(remove);
	        	}
	        }
	        	        
			String[] allTags = new String[imageClustersMap.keySet().size()];
			imageClustersMap.keySet().toArray(allTags);
			//System.out.println("[INFO]\t allTags.length: " + allTags.length);
			//System.out.println("[INFO]\t map.keySet().size(): "+imageClustersMap.keySet().size());
			
			/*** 移除小於4張圖的Tag cluster ***/
			for(int i = 0; i<allTags.length; i++) {
				if(imageClustersMap.get(allTags[i]).size() < FILTER_NUM) {
					imageClustersMap.remove(allTags[i]);
				}						
			}
			allTags = new String[imageClustersMap.keySet().size()];
			imageClustersMap.keySet().toArray(allTags);
			//System.out.println("[INFO]\t After remove : allTags.length: "+allTags.length);
			//System.out.println("[INFO]\t After remove : map.keySet().size(): "+imageClustersMap.keySet().size());
			/*for(int i=0; i<allTags.length; i++) {
				System.out.println(allTags[i]);
			}*/
			/*** 移除小於4張圖的Tag cluster *** END */
        	
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println(e);
			System.exit(0);
		}						
	}

	/**	 
	 * Get mTagAverageHistogram[] and mEncTagAverageHistogram[]
	 * 12.03.13 winderif
	 */
	private void getAverageColorHistogram() {
		System.out.println("\t[S][START]\tGet Average Color Histogram");
		double[] tmpHistogram = null;
		this.mTagAverageHistogram = new double[this.imageClustersMap.keySet().size()][BIN_HISTO];
		this.allTags = new String[this.imageClustersMap.keySet().size()];
		this.imageClustersMap.keySet().toArray(allTags);
		for(int i=0; i<this.imageClustersMap.keySet().size(); i++) {
			//System.out.println("[TAG]\t" + allTags[i] + "\t" + this.imageClustersMap.get(allTags[i]).size());			
			for(VideoFrame mPhoto : this.imageClustersMap.get(allTags[i])) {
				tmpHistogram = mPhoto.getHistogram();
				for(int j=0; j<BIN_HISTO; j++) {
					this.mTagAverageHistogram[i][j] += tmpHistogram[j];
				}
			}
			
			for(int j=0; j<BIN_HISTO; j++) {
				this.mTagAverageHistogram[i][j] /= this.imageClustersMap.get(allTags[i]).size();
				//System.out.print(this.mTagAverageHistogram[i][j] + " ");
			}
			//System.out.println();
		}
		System.out.println("\t[S][SUCCESS]\tGet Average Color Histogram");
	}

    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                          // close everything
    	ProgCommon.ois.close();
    	clientSocket.close();
    	sock.close();
    }
}
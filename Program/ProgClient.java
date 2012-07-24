// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.Map;

import Utils.Create;
import Utils.VideoFrame;


public abstract class ProgClient extends Program {

    public static String serverIPname = "localhost";             // server IP name
    private final int    serverPort   = 23456;                   // server port number
    private Socket       sock         = null;                    // Socket object for communicating
    
    //protected static final int BIN_HISTO = 16;
    protected static final int BIN_HISTO = 10000;
    
    public static String queryDirName = null;
    protected File[] queryDataFile = null;	
    protected Vector<VideoFrame> videoFrames = new Vector<VideoFrame>();
    protected Vector<Vector<VideoFrame>> videoShots = new Vector<Vector<VideoFrame>>();
    
    protected double[] queryAverageHistogram = null;  
    protected Map<Integer, Double> queryAverageDescriptor = null;

    public void run() throws Exception {
    	create_socket_and_connect();

    	super.run();

    	cleanup();
    }

    protected void init() throws Exception {
    	super.init();
    	
    	//Program.iterCount = 1;
    	System.out.println(Program.iterCount);
    	ProgCommon.oos.writeInt(Program.iterCount);
    	ProgCommon.oos.flush();
    }        

    private void create_socket_and_connect() throws Exception {
    	sock = new java.net.Socket(serverIPname, serverPort);          // create socket and connect
    	ProgCommon.oos  = new java.io.ObjectOutputStream(sock.getOutputStream());  
    	ProgCommon.ois  = new java.io.ObjectInputStream(sock.getInputStream());
    }
    
    protected void initialize() throws Exception {
    	//loadQuery();
    	loadQueryVideos();
    	    	
    	//getQueryAverageHistorgram();    	
    	//getQueryAverageDescriptor();
    }
    
    protected void execute() throws Exception {
    	//System.out.println("iter: " + iter);
    	videoFrames = videoShots.elementAt(iter);    	
    	
    	getQueryAverageHistorgram();
    	
    	getQueryAverageDescriptor();
    	
    	super.execute();
    }        
    
    private void loadQuery() {
		File dirFile = new File(queryDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("[ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {
			System.out.println("[C][START]\tRead query datas.");
									
			queryDataFile = dirFile.listFiles(
					new FilenameFilter() {  
						public boolean accept(File file, String name) {  
							boolean ret = name.endsWith(".jpg");   
							return ret;  
						}
					});
			
			for(File queryFile : queryDataFile) {				
				this.videoFrames.add(new VideoFrame(queryFile));
			}			
		}			
    }
    
    private void loadQueryVideos() {
		File dirFile = new File(queryDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("[ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {
			System.out.println("[C][START]\tRead query datas.");
									
			queryDataFile = dirFile.listFiles(
					new FilenameFilter() {  
						public boolean accept(File file, String name) {  
							boolean ret = name.endsWith(".jpg");   
							return ret;  
						}
					});
			
			Vector<VideoFrame> tmpFrames = new Vector<VideoFrame>();
			int shotCount = 1;
			int shotSize = 0;
			
			for(File queryFile : queryDataFile) {
				//System.out.println(queryFile.getName());				
				String[] tmpFileName = queryFile.getName().split(" ");
				
				if(Integer.valueOf(tmpFileName[1]) > shotSize)
					shotSize = Integer.valueOf(tmpFileName[1]);
				
				// when shot change add VectorFrame to VectorShot
				if(Integer.valueOf(tmpFileName[1]) != shotCount) {
					shotCount = Integer.valueOf(tmpFileName[1]);
					videoShots.add(tmpFrames);
					tmpFrames = new Vector<VideoFrame>();	
				}
				
				tmpFrames.add(new VideoFrame(queryFile));
				System.out.print(".");
			}
			System.out.println("x");
			videoShots.add(tmpFrames);
			tmpFrames = null;
			/** debugging
			for(int i=0; i<videoShots.size(); i++) {
				System.out.print(i+1 + "\t");
				System.out.println(videoShots.elementAt(i).size());
			}
			*/
			Program.iterCount = shotSize;
		}			
    }    

    private void getQueryAverageHistorgram() throws Exception {
    	System.out.println("[C][START]\tGet Query Average Color Histogram");
    	double[] tmpHistogram = new double[BIN_HISTO];
    	queryAverageHistogram = new double[BIN_HISTO];
    	
    	for(int i=0; i<videoFrames.size(); i++) {
    		tmpHistogram = videoFrames.elementAt(i).getFeatureVector();
    		for(int j=0; j<BIN_HISTO; j++) {
    			queryAverageHistogram[j] += tmpHistogram[j];
    		}
    	}
    	for(int j=0; j<BIN_HISTO; j++) {
    		//System.out.print(queryAverageHistogram[j] + " ");
			queryAverageHistogram[j] /= videoFrames.size();
			//System.out.println(queryAverageHistogram[j]);
		}
    	System.out.println("[C][SUCCESS]\tGet Query Average Color Histogram");
    }
    
    private void getQueryAverageDescriptor() {
    	System.out.println("[C][START]\tGet Query Average Descriptor Map");
    	    	
    	queryAverageDescriptor = Create.linkedHashMap();
    	for(int i=0; i<BIN_HISTO; i++) {
    		if(queryAverageHistogram[i] > 0.0) {
    			queryAverageDescriptor.put(i, queryAverageHistogram[i]);
    		}
    	}
    	
    	System.out.println("[C][SUCCESS]\tGet Query Average Descriptor Map");    	
    }
    
    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                                                   // close everything
		ProgCommon.ois.close();
		sock.close();
    }
}
// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.io.File;
import java.net.*;
import java.util.Vector;

import Utils.VideoFrame;

public abstract class ProgClient extends Program {

    public static String serverIPname = "localhost";             // server IP name
    private final int    serverPort   = 23456;                   // server port number
    private Socket       sock         = null;                    // Socket object for communicating
    
    protected static final int BIN_HISTO = 16; 
    
    public static String queryDirName = null;
    protected File[] queryDataFile = null;	
    protected Vector<VideoFrame> videoFrames = new Vector<VideoFrame>();
    protected double[] queryAverageHistogram;

    public void run() throws Exception {
    	create_socket_and_connect();

    	super.run();

    	cleanup();
    }

    protected void init() throws Exception {
	//System.out.println(Program.iterCount);
	//ProgCommon.oos.writeInt(Program.iterCount);
	//ProgCommon.oos.flush();

    	super.init();
    }

    private void create_socket_and_connect() throws Exception {
    	sock = new java.net.Socket(serverIPname, serverPort);          // create socket and connect
    	ProgCommon.oos  = new java.io.ObjectOutputStream(sock.getOutputStream());  
    	ProgCommon.ois  = new java.io.ObjectInputStream(sock.getInputStream());
    }
    
    protected void initialize() throws Exception {
    	readData();
    	
    	getQueryAverageHistorgram();
    }
    
    private void readData() {
		File dirFile = new File(queryDirName);
		if(!dirFile.isDirectory()) {
			System.out.println("[ERROR]\tNot a dictionary.");
			System.exit(0);
		}
		else {
			System.out.println("[C][START]\tRead query datas.");
			
			queryDataFile = new File[dirFile.listFiles().length];
			for(int i=0; i<dirFile.listFiles().length; i++) {
				queryDataFile[i] = dirFile.listFiles()[i];							
				this.videoFrames.add(new VideoFrame(this.queryDataFile[i], i));
				//System.out.println(this.queryDatasFile[i].getName());
			}
		}
    }

    private void getQueryAverageHistorgram() throws Exception {
    	System.out.println("[C][START]\tGet Query Average Color Histogram");
    	double[] tmpHistogram = new double[BIN_HISTO];
    	queryAverageHistogram = new double[BIN_HISTO];
    	
    	for(int i=0; i<videoFrames.size(); i++) {
    		tmpHistogram = videoFrames.elementAt(i).getHistogram();
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
    
    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                                                   // close everything
		ProgCommon.ois.close();
		sock.close();
    }
}
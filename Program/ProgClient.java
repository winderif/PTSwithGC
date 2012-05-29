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
    
    private String queryDirName = null;
	private File[] queryDataFile = null;	
	private Vector<VideoFrame> videoFrames = new Vector<VideoFrame>();

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

    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                                                   // close everything
		ProgCommon.ois.close();
		sock.close();
    }
}
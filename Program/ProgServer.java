// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.net.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.io.*;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.input.CountingInputStream;

import com.articulate.sigma.WordNet;

import Utils.ImageClusteringByTags;
import Utils.TagClusteringByDomain;
import Utils.VideoFrame;
import Utils.DomainsData;

public abstract class ProgServer extends Program {

    final private  int         serverPort   = 23456;             // server port number
    private ServerSocket       sock         = null;              // original server socket
    private Socket             clientSocket = null;              // socket created by accept

	// Bin of HSV color histogram = 8 + 4 + 4 = 16 
    protected static final int BIN_HISTO = 16;     
    private boolean domains_file_existed = false;
    
    public static String databaseDirName;
    protected File[] databaseTagDirFile = null;	
    protected Vector<File[]> databaseDataFile = new Vector<File[]>();
    protected Vector<Vector<VideoFrame>> databaseData = new Vector<Vector<VideoFrame>>();
	
	private HashMap<String, Vector<VideoFrame>> imageClustersMap = null;	
	protected HashMap<String, Vector<String>> tagClustersMap = null;
		
	protected String[] allTags = null;
	protected String[] allDomains = null;
	
	protected HashMap<String, double[]> tagsHistogramMap = null;
	protected double[][] mTagAverageHistogram = null;
	protected double[][] mDomainAverageHistogram = null;
    
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
    	
    	generateImageClusters();    	
    	
    	generateTagClusters();    	
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
	
				if(!databaseTagDirFile[i].isDirectory()) {
					if(TagClusteringByDomain.existedDomainFile(databaseTagDirFile[i]))
						domains_file_existed = true;										
				}
				else if(databaseTagDirFile[i].listFiles().length != 0) {					
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
    
	private void generateImageClusters() {
		System.out.println("\t[S][START]\tGenerate Image Clusters");
		
		imageClustersMap
			= ImageClusteringByTags.getImageClusters(databaseData);    	        
		allTags 		 
			= ImageClusteringByTags.getAllTags(imageClustersMap);
		System.out.println("\t[S][SUCCESS]\tGenerate Image Clusters");
		
		System.out.println("\t[S][START]\tGet Average Color Histogram");
		mTagAverageHistogram 
			= ImageClusteringByTags.getTagAverageHistogram(imageClustersMap, allTags);
		System.out.println("\t[S][SUCCESS]\tGet Average Color Histogram");
		
		tagsHistogramMap
			= ImageClusteringByTags.getTagsHistogramMap(allTags, mTagAverageHistogram);
	}	
	
	private void generateTagClusters() {		
		if(!domains_file_existed) {
			
			tagClustersMap 
				= TagClusteringByDomain.getTagClusters(allTags);
			
			allDomains 
				= TagClusteringByDomain.getAllDomains(tagClustersMap, databaseDirName);
		}
		else {
			DomainsData tmpDomainsData 
				= TagClusteringByDomain.getDomainsData(databaseDirName);
			
			tagClustersMap = tmpDomainsData.tagClustersMap;
			allDomains = tmpDomainsData.allDomains;
		}						
		
		System.out.println("\t[S][START]\tGet Domain Average Color Histogram");
		mDomainAverageHistogram
			= TagClusteringByDomain.getDomainAverageColorHistogram(tagClustersMap, allDomains, tagsHistogramMap);
		System.out.println("\t[S][SUCCESS]\tGet Domain Average Color Histogram");		
	}

    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                          // close everything
    	ProgCommon.ois.close();
    	clientSocket.close();
    	sock.close();
    }        
}
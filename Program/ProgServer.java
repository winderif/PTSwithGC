// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.*;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.input.CountingInputStream;

import Utils.ImageClusteringByTags;
import Utils.TagClusteringByDomain;
import Utils.VideoFrame;
import Utils.DomainsData;
import Utils.Experiment;

public abstract class ProgServer extends Program {

    final private  int         serverPort   = 23456;             // server port number
    private ServerSocket       sock         = null;              // original server socket
    private Socket             clientSocket = null;              // socket created by accept
	
    //protected static final int BIN_HISTO = 16;
    protected static final int BIN_HISTO = 10000;         
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
	protected HashMap<String, Map<Integer, Double>> tagsDescriptorMap = null;
	protected double[][] mTagAverageHistogram = null;
	protected Vector<Map<Integer, Double>> mTagAverageDescriptor = null;
	protected double[][] mDomainAverageHistogram = null;
	protected Vector<Map<Integer, Double>> mDomainAverageDescriptor = null;
	
	protected Experiment mExp = null;
    
	public ProgServer() {
		this.mExp = new Experiment(this.databaseDirName);			
	}
	
    public void run() throws Exception {
    	create_socket_and_listen();

    	super.run();

    	cleanup();
    	this.mExp.close();
    }

    protected void init() throws Exception {
    	this.mExp.createSheet("Exec_Time", 0);
    	
    	super.init();
    	
    	Program.iterCount = ProgCommon.ois.readInt();
    	System.out.println(Program.iterCount);        
    	
    	/** Create Excel sheet */    	
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
    	loadQuery();
    	
    	generateImageClusters();    	
    	
    	generateTagClusters();    	    	
    }       
    
    private void loadQuery() {
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
					File[] imagesFile = databaseTagDirFile[i].listFiles(
							new FilenameFilter() {  
								public boolean accept(File file, String name) {  
									boolean ret = name.endsWith(".jpg");   
									return ret;  
								}
							});		
					int sizeOfTagDir = imagesFile.length;
					//int sizeOfTagDir = databaseTagDirFile[i].listFiles().length;
					tmpFileArray = new File[sizeOfTagDir];		
					tmpVideoFrame = new Vector<VideoFrame>();
					
					for(int j=0; j<sizeOfTagDir; j++) {
						if(imagesFile[j].getName().endsWith(".jpg")) {
							tmpFileArray[j] = imagesFile[j];
							tmpVideoFrame.add(new VideoFrame(tmpFileArray[j]));
							//System.out.println("[DIR] " + tmpFileArray[j].getName());
						}						
					}
					databaseDataFile.add(tmpFileArray.clone());
					databaseData.add(tmpVideoFrame);
					System.out.print(".");
				}															
				//System.out.println(this.databaseTagDirFile[i].getName());
			}
			System.out.println("x");
		}
    }
    
	private void generateImageClusters() throws Exception {
		System.out.println("\t[S][START]\tGenerate Image Clusters");
		
		imageClustersMap =
			ImageClusteringByTags.getImageClusters(databaseData);    	        
		allTags =  		 
			ImageClusteringByTags.getAllTags(imageClustersMap);
		System.out.println("\t[S][SUCCESS]\tGenerate Image Clusters");	
		
		System.out.println("\t[S][START]\tGet Average Histogram");
		mTagAverageHistogram = 
			ImageClusteringByTags.getTagAverageHistogram(imageClustersMap, allTags);
		System.out.println("\t[S][SUCCESS]\tGet Average Histogram");
		
		tagsHistogramMap =
			ImageClusteringByTags.getTagsHistogramMap(allTags, mTagAverageHistogram);
		
		System.out.println("\t[S][START]\tGet Average Descriptor");
		mTagAverageDescriptor =
			ImageClusteringByTags.getTagsDescriptor(mTagAverageHistogram);
		System.out.println("\t[S][SUCCESS]\tGet Average Descriptor");
		/** Write # of tags*/
		this.mExp.writeSheet(0, iter, 7, "# of Original Tags", mTagAverageDescriptor.size());
		
		tagsDescriptorMap = 
			ImageClusteringByTags.getTagsDescriptorMap(allTags, mTagAverageDescriptor);				
	}
	
	private void generateTagClusters() throws Exception {		
		if(domains_file_existed == false) {
			
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
		
		System.out.println("\t[S][START]\tGet Domain Average Histogram");
		mDomainAverageHistogram =
			TagClusteringByDomain.getDomainAverageColorHistogram(tagClustersMap, allDomains, tagsHistogramMap);
		System.out.println("\t[S][SUCCESS]\tGet Domain Average Histogram");
		
		System.out.println("\t[S][START]\tGet Domain Average Descriptor");
		mDomainAverageDescriptor =
			TagClusteringByDomain.getDomainAverageColorDescriptor(mDomainAverageHistogram);
		System.out.println("\t[S][SUCCESS]\tGet Domain Average Descriptor");
		/** Write # of domains */
		this.mExp.writeSheet(0, iter, 8, "# of Domains", mDomainAverageDescriptor.size());
	}	
	
    private void cleanup() throws Exception {
    	ProgCommon.oos.close();                          // close everything
    	ProgCommon.ois.close();
    	clientSocket.close();
    	sock.close();
    }        
}
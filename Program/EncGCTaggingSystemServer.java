// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Random;

import Protocol.GCComparisonServer;
import Utils.ClientState;
import Utils.Create;
import Utils.Print;
import Matching.GCbasedHungarianAlgorithm;
import Utils.FindExtremeValue;
import Crypto.CryptosystemPaillierServer;
import Score.*;

public class EncGCTaggingSystemServer extends ProgServer {

	private CryptosystemPaillierServer mPaillier = null;
	private Distance mDistance = null;
		
	private int mQueryNum = 0;
	private BigInteger[][] mEncQueryHistogram = null;
	private BigInteger[] mEncQueryAverageHistogram = null;
	private Vector<Map<Integer, BigInteger>> mEncQueryDescriptor = null;
	private Map<Integer, BigInteger> mEncQueryAverageDescriptor = null;
	private BigInteger[][] mEncTagAverageHistogram = null;
	private BigInteger[][] mEncDomainAverageHistogram = null;
	private BigInteger[][] mEncHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
	private BigInteger[] mEncDomainDistance = null;
	
    public EncGCTaggingSystemServer() {
    }

    protected void init() throws Exception {
    	super.init();
    	
    	// recv key from client and setup
    	BigInteger[] pkey = new BigInteger[2]; 		
    	pkey[0] = new BigInteger(EncProgCommon.ois.readObject().toString());
    	pkey[1] = new BigInteger(EncProgCommon.ois.readObject().toString());
    	this.mPaillier = new CryptosystemPaillierServer(pkey);
    	System.out.println("\t[S][SUCCESS]\treceive public key pair (n, g).");
    	
    	this.mDistance = new DistanceL2square();
    	//this.mDistance = new DistanceWeightedL2square();    	
    	/**
    	System.out.println("\t[S][START]\tEncrypt Database.");
    	EncrptTagAverageHistogram();
    	EncrptDomainAverageHistogram();
    	System.out.println("\t[S][SUCCESS]\tEncrypt Database.");
    	*/
    }
    
	private void EncrptTagAverageHistogram() {				
		this.mEncTagAverageHistogram = 
			EncProgCommon.encryption(mPaillier, mTagAverageHistogram);					
	}
	
	private void EncrptDomainAverageHistogram() {				
		this.mEncDomainAverageHistogram = 
			EncProgCommon.encryption(mPaillier, mDomainAverageHistogram);					
	}
    
	protected void execQueryTransfer() throws Exception {
    	System.out.println("\t[S][START]\treceive Query datas.");
    	
    	mQueryNum = TaggingSystemCommon.ois.readInt();    
    	//System.out.println("mQueryNum: " + mQueryNum);
    	//transferHistogram();
    	transferDescriptor();
    	
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    private void transferHistogram() throws Exception {
    	mEncQueryHistogram = new BigInteger[mQueryNum][BIN_HISTO];
    	mEncQueryAverageHistogram = new BigInteger[BIN_HISTO];    	
 		for(int i=0; i<mEncQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mEncQueryHistogram[i][j] 
 				    = new BigInteger(EncProgCommon.ois.readObject().toString());
 				//System.out.print(mEncQueryHistogram[i][j] + " ");
 			}
 			//System.out.println();
 		} 		
 		
 		for(int i=0; i<BIN_HISTO; i++) {
 			mEncQueryAverageHistogram[i] 
 			    = new BigInteger(EncProgCommon.ois.readObject().toString());
			//System.out.print(mQueryAverageHistogram[i] + " ");
		}
 		//System.out.println();
    }
    
    private void transferDescriptor() throws Exception {
    	mEncQueryDescriptor = new Vector<Map<Integer, BigInteger>>();
    	mEncQueryAverageDescriptor = Create.linkedHashMap();
    	for(int i=0; i<mQueryNum; i++) {
    		mEncQueryDescriptor.add(
    				(Map<Integer, BigInteger>)TaggingSystemCommon.ois.readObject());
    		/** Printing 
    		Print.printEncMap(mEncQueryDescriptor.elementAt(i), "\t[S]\tPrint mQueryDescriptor Map", mPaillier);
    		*/    		
    	}
    	
    	mEncQueryAverageDescriptor =
    		(Map<Integer, BigInteger>)TaggingSystemCommon.ois.readObject();
    	/** Printing 
		Print.printEncMap(mEncQueryAverageDescriptor, "\t[S]\tPrint mEncQueryAverageDescriptor Map", mPaillier);
		*/
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
    	evaluateDomainDistance();		
		
		BigInteger threshold = evaluateThreshold();
		
    	// 80% of minimum distance		
		findCandidateTags(threshold); 
    	
    	/** If # tag < # query */ 
    	if(mTagAverageDescriptor.size() < mEncQueryDescriptor.size()) {
    		System.out.println("\t[S][INFO]\t# query < # tag.");
    		mEncQueryDescriptor = new Vector<Map<Integer, BigInteger>>();
    		mEncQueryDescriptor.add(mEncQueryAverageDescriptor);
    	}    	    	
    }
    
    private void evaluateDomainDistance() throws Exception {
    	System.out.println("\t[S][START]\tEvaluate Encrypted Domain Distance.");
    	double startTime = System.nanoTime();
    	/***/
    	mEncDomainDistance = new BigInteger[allDomains.length];    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		System.out.println("\t[S]\tComput distance " + i);
    		
    		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
    		mEncDomainDistance[i] = mDistance.evaluate(
    				mPaillier, mEncQueryAverageDescriptor, mDomainAverageDescriptor.elementAt(i));
    		
    		System.out.println(mPaillier.Decryption(mEncDomainDistance[i]));
    	}
    	EncProgCommon.oos.writeObject(ClientState.CLIENT_EXIT);
    	EncProgCommon.oos.flush();
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tEvaluate Encrypted Domain Distance." + time);
		
		/** Debugging 
		mEncDomainDistance = new BigInteger[allDomains.length];
		for(int i=0; i<mEncDomainDistance.length; i++) {
			mEncDomainDistance[i] = 
			    mPaillier.Encryption(new BigInteger(15, new Random()));
			    //mPaillier.Encryption(new BigInteger(Long.valueOf(1000000+i).toString()));
			System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");			
		}
		System.out.println();		
		*/
    }
    
    private BigInteger evaluateThreshold() throws Exception {
    	GCComparisonServer gcc_s = new GCComparisonServer(mPaillier);
    	
    	System.out.println("\t[START]\tfindEncMaximumGC()");
    	double startTime = System.nanoTime();
    	
    	BigInteger maxDistance =  
			FindExtremeValue.findEncMaximumGC(mEncDomainDistance, gcc_s);
    	//System.out.println(mPaillier.Decryption(maxDistance));
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;    	
    	System.out.println("\t[SUCCESS]\tfindEncMaximumGC()" + time);
    	this.mExp.writeSheet(0, iter, 11, "Find Maximum", time);
    	
    	System.out.println("\t[START]\tfindEncMiniimumGC()");
    	startTime = System.nanoTime();
    	
    	BigInteger minDistance =  
			FindExtremeValue.findEncMinimumGC(mEncDomainDistance, gcc_s);
    	//System.out.println(mPaillier.Decryption(minDistance));
    	
    	endTime = System.nanoTime();
		time = (endTime - startTime)/1000000000.0;    	
    	System.out.println("\t[SUCCESS]\tfindEncMiniimumGC()" + time);
    	this.mExp.writeSheet(0, iter, 12, "Find Minimum", time);
	
    	BigInteger sMin = minDistance.pow(5).mod(mPaillier.nsquare);
    	//System.out.println(mPaillier.Decryption(sMin));
	
    	BigInteger diff =  
			maxDistance.multiply(minDistance.modInverse(mPaillier.nsquare))
						 .mod(mPaillier.nsquare);
	
    	BigInteger threshold =
			sMin.multiply(diff).mod(mPaillier.nsquare);
    	//System.out.println("T : \t" + mPaillier.Decryption(threshold).divide(new BigInteger("5")));
    	
    	return threshold;
    }
    
    private void findCandidateTags(BigInteger threshold) throws Exception {
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	double startTime = System.nanoTime();
    	
    	HashMap<String, Map<Integer, Double>> tmpCandidateTags = Create.hashMap();
    	BigInteger tmpDistance = null;
    	GCComparisonServer gcc_s = new GCComparisonServer(mPaillier);
    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);  
    		//System.out.println(mPaillier.Decryption(tmpDistance) + " " + mPaillier.Decryption(threshold));
    		
    		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
    	    if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{tmpDistance, threshold}, 0).equals(tmpDistance)) {
    	    	System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
    	    	System.out.print(allDomains[i] + "\t");
    	    	for(String tag : tagClustersMap.get(allDomains[i])) {
    	    		System.out.print(tag + " ");
    	    		if(!tmpCandidateTags.containsKey(tag)) {
    	    			System.out.print(tag + "-");
    	    			tmpCandidateTags.put(tag, tagsDescriptorMap.get(tag));
    	    		}    	    		
    	    	}
    	    	System.out.println();  	
    	    }
    	}    	    
    	EncProgCommon.oos.writeObject(ClientState.CLIENT_EXIT);
    	EncProgCommon.oos.flush();
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tFind Candidate Tag.");
    	System.out.println("time: " + time);

    	allTags = new String[tmpCandidateTags.keySet().size()];
    	tmpCandidateTags.keySet().toArray(allTags);
    	mTagAverageDescriptor = new Vector<Map<Integer, Double>>();
    	for(int i=0; i<allTags.length; i++) {
    		System.out.print(allTags[i] + ", ");
    		mTagAverageDescriptor.add(tmpCandidateTags.get(allTags[i]));
    	}    	
    	System.out.println();
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    	    	
    	System.out.println("\t[S][START]\tBuild Encrypted Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryDescriptor.size()][this.mTagAverageDescriptor.size()];
		for(int i=0; i<this.mEncQueryDescriptor.size(); i++) {
			for(int j=0; j<this.mTagAverageDescriptor.size(); j++) {
				System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				double start = System.nanoTime();
				
				EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
				this.mEncHungarianMatrix[i][j] = mDistance.evaluate(
						mPaillier, 
						mEncQueryDescriptor.elementAt(i), 
						mTagAverageDescriptor.elementAt(j));

				double end = System.nanoTime();
				double time = (end - start)/1000000000.0;
				System.out.print(mPaillier.Decryption(mEncHungarianMatrix[i][j]) + " " + time);
				//System.out.print(mPaillier.Decryption(this.mEncHungarianMatrix[i][j]) + " ");
			}
			//System.out.println();
		}
		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXIT);
    	EncProgCommon.oos.flush();
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);					
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "min";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];	
		
		GCbasedHungarianAlgorithm GCbasedFHA =
			new GCbasedHungarianAlgorithm(mPaillier);
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = GCbasedFHA.hgAlgorithm(this.mEncHungarianMatrix, sumType);
		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXIT);
    	EncProgCommon.oos.flush();
		/*** ***/
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		
		for(int k=0; k<assignment.length; k++) {
			System.out.printf("array(%d,%d) = %s %s\n", 
					(assignment[k][0]+1), 
					(assignment[k][1]+1),
					this.mEncHungarianMatrix[assignment[k][0]][assignment[k][1]].toString(), 
					this.allTags[assignment[k][1]]);			
		}
		
		System.out.println("\t[S][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph." + time);
		
		if(mEncQueryDescriptor.size() == 1) {
			mMatchingTags = new String[mQueryNum];
			mMatchingTags[0] = allTags[assignment[0][1]];
			for(int i=1; i<mQueryNum; i++) {
				mMatchingTags[i] = " ";					
			}
		}
		else {
			mMatchingTags = new String[this.mEncQueryDescriptor.size()];		
			for(int i=0; i<this.mMatchingTags.length; i++) {
				this.mMatchingTags[i] = this.allTags[assignment[i][1]];
				//System.out.println("[MATCH]\t" + (i+1) + "\t" + this.mMatchingTags[i]);
			}	
		}				
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		EncProgCommon.oos.writeObject(mMatchingTags[i]);
    	}
    	EncProgCommon.oos.flush();
    }        
}
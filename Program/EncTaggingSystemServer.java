// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Map;
import java.util.Iterator;
import java.util.Random;

import Protocol.ComparisonProtocolOnServer;
import Utils.Create;
import Utils.EncFastHungarianAlgorithm;
import Utils.FindExtremeValue;
import Utils.Print;
import Crypto.CryptosystemPaillierServer;
import Score.*;

public class EncTaggingSystemServer extends ProgServer {

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
	
    public EncTaggingSystemServer() {
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
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
    	System.out.println("\t[S][START]\tEvaluate Encrypted Domain Distance.");
    	double startTime = System.nanoTime();
    	
    	mEncDomainDistance = new BigInteger[allDomains.length];    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		System.out.println("\t[S]\tComput distance " + i);
    		EncProgCommon.oos.writeInt(0);
    		mEncDomainDistance[i] = mDistance.evaluate(
    				mPaillier, mEncQueryAverageDescriptor, mDomainAverageDescriptor.elementAt(i));    		
    		System.out.println(mPaillier.Decryption(mEncDomainDistance[i]) + " ");    		
    	}
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tEvaluate Encrypted Domain Distance." + time);    	    	    
				
    	/** Debugging
		mEncDomainDistance = new BigInteger[allDomains.length];
		for(int i=0; i<mEncDomainDistance.length; i++) {
			mEncDomainDistance[i] = 
			    mPaillier.Encryption(BigInteger.probablePrime(10, new Random()));
			    //mPaillier.Encryption(new BigInteger(Long.valueOf(1000000+i).toString()));
			System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");			
		}
		System.out.println();
		*/
		EncProgCommon.oos.writeInt(1);
		EncProgCommon.oos.flush();
		
    	ComparisonProtocolOnServer cp_s
    		= new ComparisonProtocolOnServer(mPaillier);
    	
    	BigInteger maxDistance 
			= FindExtremeValue.findEncMaximum(mEncDomainDistance, cp_s, mPaillier);
    	System.out.println(mPaillier.Decryption(maxDistance));
    	
    	BigInteger minDistance 
			= FindExtremeValue.findEncMinimum(mEncDomainDistance, cp_s, mPaillier);
    	System.out.println(mPaillier.Decryption(minDistance));
    	
    	BigInteger sMin = minDistance.pow(5).mod(mPaillier.nsquare);
    	System.out.println(mPaillier.Decryption(sMin));
    	
    	BigInteger diff
    		= maxDistance.multiply(minDistance.modInverse(mPaillier.nsquare))
    					 .mod(mPaillier.nsquare);
    	
    	BigInteger threshold
    		= sMin.multiply(diff).mod(mPaillier.nsquare);
    	System.out.println("T : \t" + mPaillier.Decryption(threshold).divide(new BigInteger("5")));
    	
    	/** Debugging
    	BigInteger tmpDistance = null;    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);    
    		if(cp_s.findMinimumOfTwoEncValues(tmpDistance, threshold).equals(tmpDistance)) {
    	    	System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
    		}
    	}
    	*/
    	
    	/***/
    	// 80% of minimum distance
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	startTime = System.nanoTime();
    	    	
    	HashMap<String, Map<Integer, Double>> tmpCandidateTags = Create.hashMap();
    	BigInteger tmpDistance = null;
    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);    
    		System.out.println(mPaillier.Decryption(tmpDistance) + " " + mPaillier.Decryption(threshold));
    		
    		EncProgCommon.oos.writeInt(0);
    		BigInteger min = cp_s.findMinimumOfTwoEncValues(tmpDistance, threshold);
    		System.out.println("Min: " + mPaillier.Decryption(min));
    	    if(min.equals(tmpDistance)) {
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
    	
    	endTime = System.nanoTime();
		time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tFind Candidate Tag." + time);    	
    	
    	allTags = new String[tmpCandidateTags.keySet().size()];
    	tmpCandidateTags.keySet().toArray(allTags);    	
    	mTagAverageDescriptor = new Vector<Map<Integer, Double>>();
    	for(int i=0; i<allTags.length; i++) {
    		System.out.println(allTags[i]);    		
    		mTagAverageDescriptor.add(tmpCandidateTags.get(allTags[i]));
    	}    	
    	//System.out.println();
    	/** If # tag < # query */ 
    	if(mTagAverageDescriptor.size() < mEncQueryDescriptor.size()) {
    		System.out.println("\t[S][INFO]\t# query < # tag.");
    		mEncQueryDescriptor = new Vector<Map<Integer, BigInteger>>();
    		mEncQueryDescriptor.add(mEncQueryAverageDescriptor);
    	}
    	EncProgCommon.oos.writeInt(1);
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    	    	
    	System.out.println("\t[S][START]\tBuild Encrypted Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryDescriptor.size()][this.mTagAverageDescriptor.size()];
		for(int i=0; i<this.mEncQueryDescriptor.size(); i++) {
			for(int j=0; j<this.mTagAverageDescriptor.size(); j++) {
				System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				double start = System.nanoTime();
				
				this.mEncHungarianMatrix[i][j] = mDistance.evaluate(
							mPaillier, 
							mEncQueryDescriptor.elementAt(i), 
							mTagAverageDescriptor.elementAt(j));
				
				double end = System.nanoTime();
				double time = (end - start)/1000000000.0;
				System.out.print(mPaillier.Decryption(mEncHungarianMatrix[i][j]) + " " + time);
			}
			System.out.println();
		}				
		
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);		
		EncProgCommon.oos.writeObject(1);		
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "min";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];
		EncFastHungarianAlgorithm EncFHA = 
			new EncFastHungarianAlgorithm(
					new ComparisonProtocolOnServer(mPaillier), mPaillier, null);
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = EncFHA.hgAlgorithm(this.mEncHungarianMatrix, sumType);
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
		
		mMatchingTags = new String[this.mEncQueryHistogram.length];		
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			this.mMatchingTags[i] = this.allTags[assignment[i][1]];
			//System.out.println("[MATCH]\t" + (i+1) + "\t" + this.mMatchingTags[i]);
		}
		EncProgCommon.oos.writeObject(1);	
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		EncProgCommon.oos.writeObject(mMatchingTags[i]);    		
    	}    	
    	EncProgCommon.oos.flush();
    }        	
}
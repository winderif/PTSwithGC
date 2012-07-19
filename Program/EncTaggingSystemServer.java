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
import Matching.HEbasedHungarianAlgorithm;
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
    	double startTime = System.nanoTime();
    	
    	mQueryNum = TaggingSystemCommon.ois.readInt();    	
    	//transferHistogram();
    	transferDescriptor();
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
 		this.mExp.writeSheet(0, iter, 0, "Query Transfer", time); 		
 		this.mExp.writeSheet(0, iter, 6, "# of query", mEncQueryDescriptor.size());
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
    	evaluateDomainDistance();		
		
		BigInteger threshold = evaluateThreshold();
		
    	// 80% of minimum distance		
		findCandidateTags(threshold);    	
		/** Write # of Candidate Tags*/
    	this.mExp.writeSheet(0, iter, 9, "# of Candidate Tags", mTagAverageDescriptor.size());
		
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
    	
    	mEncDomainDistance = new BigInteger[allDomains.length];    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		System.out.println("\t[S]\tComput distance " + i);
    		
    		EncProgCommon.oos.writeInt(CLIENT_EXEC);
    		mEncDomainDistance[i] = mDistance.evaluate(
    				mPaillier, mEncQueryAverageDescriptor, mDomainAverageDescriptor.elementAt(i));
    		
    		System.out.println(mPaillier.Decryption(mEncDomainDistance[i]) + " ");    		
    	}    	
    	EncProgCommon.oos.writeInt(CLIENT_EXIT);
		EncProgCommon.oos.flush();
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tEvaluate Encrypted Domain Distance." + time);    
    	this.mExp.writeSheet(0, iter, 1, "Evaluate Domain Distance", time);
				
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
    }
    
    private BigInteger evaluateThreshold() throws Exception {
    	ComparisonProtocolOnServer cp_s =
    		new ComparisonProtocolOnServer(mPaillier);
	
    	System.out.println("\t[START]\tfindEncMaximum()");
		double startTime = System.nanoTime();
		
    	BigInteger maxDistance = 
    		FindExtremeValue.findEncMaximum(mEncDomainDistance, cp_s, mPaillier);
    	//System.out.println(mPaillier.Decryption(maxDistance));
    	
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[SUCCESS]\tfindEncMaximum()" + time);
		this.mExp.writeSheet(0, iter, 11, "Find Maximum", time);
		
		System.out.println("\t[START]\tfindEncMinimum()");
		startTime = System.nanoTime();
		
    	BigInteger minDistance =
    		FindExtremeValue.findEncMinimum(mEncDomainDistance, cp_s, mPaillier);
    	//System.out.println(mPaillier.Decryption(minDistance));
    	
    	endTime = System.nanoTime();
		time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[SUCCESS]\tfindEncMinimum()" + time);
		this.mExp.writeSheet(0, iter, 12, "Find Minimum", time);
    	
    	BigInteger sMin = minDistance.pow(5).mod(mPaillier.nsquare);
    	//System.out.println(mPaillier.Decryption(sMin));
    	
    	BigInteger diff =
    		maxDistance.multiply(minDistance.modInverse(mPaillier.nsquare))
    					 .mod(mPaillier.nsquare);
    	
    	BigInteger threshold =
    		sMin.multiply(diff).mod(mPaillier.nsquare);
    	//System.out.println("T : \t" + mPaillier.Decryption(threshold).divide(new BigInteger("5")));
    	
    	/** Debugging
    	BigInteger tmpDistance = null;    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);    
    		if(cp_s.findMinimumOfTwoEncValues(tmpDistance, threshold).equals(tmpDistance)) {
    	    	System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
    		}
    	}
    	*/    	
    	return threshold;
    }
    
    private void findCandidateTags(BigInteger threshold) throws Exception {
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	double startTime = System.nanoTime();
    	// Initialize
    	HashMap<String, Map<Integer, Double>> tmpCandidateTags = Create.hashMap();
    	BigInteger tmpDistance = null;
    	ComparisonProtocolOnServer cp_s =
    		new ComparisonProtocolOnServer(mPaillier);    	
    	/** 
    	 *  For each domain distance D, 
    	 *  if D < threshold T, then get tags in this domain.    	 
    	 */
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);    
    		//System.out.println(mPaillier.Decryption(tmpDistance) + " " + mPaillier.Decryption(threshold));
    		
    		EncProgCommon.oos.writeInt(CLIENT_EXEC);
    		BigInteger min = cp_s.findMinimumOfTwoEncValues(tmpDistance, threshold);
    		//System.out.println("Min: " + mPaillier.Decryption(min));
    	    if(min.equals(tmpDistance)) {
    	    	//System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
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
    	EncProgCommon.oos.writeInt(CLIENT_EXIT);
		EncProgCommon.oos.flush();
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tFind Candidate Tag." + time);
    	this.mExp.writeSheet(0, iter, 2, "Find Candidate Tag", time);    	
    	/**
    	 *  Get Descriptor from candidate tags. 
    	 */    	
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
    	System.out.println("\t[S] " + this.mEncQueryDescriptor.size() + ", " + this.mTagAverageDescriptor.size());
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryDescriptor.size()][this.mTagAverageDescriptor.size()];
		for(int i=0; i<this.mEncQueryDescriptor.size(); i++) {
			System.out.printf("\t[S][START]\tComputing D(%d)\n", i);
			
			for(int j=0; j<this.mTagAverageDescriptor.size(); j++) {
				//System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				double start = System.nanoTime();
				EncProgCommon.oos.writeInt(CLIENT_EXEC);
				this.mEncHungarianMatrix[i][j] = mDistance.evaluate(
							mPaillier, 
							mEncQueryDescriptor.elementAt(i), 
							mTagAverageDescriptor.elementAt(j));
				
				double end = System.nanoTime();
				double time = (end - start)/1000000000.0;
				//System.out.print(mPaillier.Decryption(mEncHungarianMatrix[i][j]) + " " + time);
			}
			//System.out.println();
		}				
		
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);
		this.mExp.writeSheet(0, iter, 3, "Build Bipartile Graph", time);
		EncProgCommon.oos.writeInt(CLIENT_EXIT);
		EncProgCommon.oos.flush();		
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "min";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];
		
		HEbasedHungarianAlgorithm HEbasedFHA = 
			new HEbasedHungarianAlgorithm(mPaillier);		
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = HEbasedFHA.hgAlgorithm(this.mEncHungarianMatrix, sumType);
		EncProgCommon.oos.writeInt(CLIENT_EXIT);
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
		this.mExp.writeSheet(0, iter, 4, "Find Bset Matching", time);
		
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
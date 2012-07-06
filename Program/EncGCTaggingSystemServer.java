// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import Protocol.GCComparisonServer;
import Utils.EncFastHungarianAlgorithm;
import Utils.FindExtremeValue;
import Crypto.CryptosystemPaillierServer;
import Score.ComputingScoreServer;
import Score.Distance;
import Score.DistanceL2square;

public class EncGCTaggingSystemServer extends ProgServer {

	private CryptosystemPaillierServer mPaillier = null;
	private Distance mDistance = null;
		
	private int mQueryNum = 0;
	private BigInteger[][] mEncQueryHistogram = null;
	private BigInteger[] mEncQueryAverageHistogram = null;
	private Vector<Map<Integer, BigInteger>> mEncQueryDescriptor = null;
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
    	mQueryNum = TaggingSystemCommon.ois.readInt();
    	mEncQueryHistogram = new BigInteger[mQueryNum][BIN_HISTO];
    	mEncQueryDescriptor = new Vector<Map<Integer, BigInteger>>();
    	
    	for(int i=0; i<mQueryNum; i++) {
    		mEncQueryDescriptor.add(
    				(LinkedHashMap<Integer, BigInteger>)TaggingSystemCommon.ois.readObject());
    		/** Printing 
    		Iterator iter = mEncQueryDescriptor.elementAt(i).entrySet().iterator();
    		while(iter.hasNext()) {
    			Map.Entry<Integer, BigInteger> p = 
    				(Map.Entry<Integer, BigInteger>)iter.next();
    			System.out.println(p.getKey() + "\t" + mPaillier.Decryption(p.getValue()));
    		}    		
    		*/
    	}
    	/**
 		for(int i=0; i<mEncQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mEncQueryHistogram[i][j] 
 				    = new BigInteger(EncProgCommon.ois.readObject().toString());
 				//System.out.print(mPaillier.Decryption(mEncQueryHistogram[i][j]) + " ");
 			}
 			//System.out.println();
 		}
 		
 		for(int i=0; i<BIN_HISTO; i++) {
 			mEncQueryAverageHistogram[i] 
 			    = new BigInteger(EncProgCommon.ois.readObject().toString());
			//System.out.print(mQueryAverageHistogram[i] + " ");
		}
 		//System.out.println(); 		 
 		*/
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
    	System.out.println("\t[S][START]\tEvaluate Encrypted Domain Distance.");
    	double startTime = System.nanoTime();
    	
    	mEncDomainDistance = new BigInteger[allDomains.length];    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		System.out.println("\t[S]\tComput distance " + i);
    		mEncDomainDistance[i] 
    		    = EncScore(mEncQueryAverageHistogram, mDomainAverageHistogram[i]);
    		//System.out.println(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
    	} 
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tEvaluate Encrypted Domain Distance.");
    	System.out.println("time: " + time);
    	
    	EncProgCommon.oos.writeObject(null);		
    	
    	GCComparisonServer gcc_s = new GCComparisonServer(mPaillier);
    	
    	BigInteger maxDistance 
			= FindExtremeValue.findEncMaximumGC(mEncDomainDistance, gcc_s);
    	System.out.println(mPaillier.Decryption(maxDistance));
	
    	BigInteger minDistance 
			= FindExtremeValue.findEncMinimumGC(mEncDomainDistance, gcc_s);
    	System.out.println(mPaillier.Decryption(minDistance));
	
    	BigInteger sMin = minDistance.pow(5).mod(mPaillier.nsquare);
    	System.out.println(mPaillier.Decryption(sMin));
	
    	BigInteger diff 
			= maxDistance.multiply(minDistance.modInverse(mPaillier.nsquare))
						 .mod(mPaillier.nsquare);
	
    	BigInteger threshold
			= sMin.multiply(diff).mod(mPaillier.nsquare);
    	System.out.println("T : \t" + mPaillier.Decryption(threshold).divide(new BigInteger("5")));
    	
    	// 80% of minimum distance
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	startTime = System.nanoTime();
    	
    	HashMap<String, double[]> tmpCandidateTags = new HashMap<String, double[]>();
    	BigInteger tmpDistance = null;
    	
    	for(int i=0; i<mEncDomainDistance.length; i++) {
    		tmpDistance = mEncDomainDistance[i].pow(5).mod(mPaillier.nsquare);    
    		
    	    if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{tmpDistance, threshold}, 0).equals(tmpDistance)) {
    	    	System.out.print(mPaillier.Decryption(mEncDomainDistance[i]) + " ");
    	    	System.out.print(allDomains[i] + "\t");
    	    	for(String tag : tagClustersMap.get(allDomains[i])) {
    	    		System.out.print(tag + " ");
    	    		if(!tmpCandidateTags.containsKey(tag)) {
    	    			System.out.print(tag + "-");
    	    			tmpCandidateTags.put(tag, tagsHistogramMap.get(tag));
    	    		}    	    		
    	    	}
    	    	System.out.println();  	
    	    }
    	}    	    
    	
    	endTime = System.nanoTime();
		time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tFind Candidate Tag.");
    	System.out.println("time: " + time);

    	allTags = new String[tmpCandidateTags.keySet().size()];
    	tmpCandidateTags.keySet().toArray(allTags);
    	mTagAverageHistogram = new double[allTags.length][BIN_HISTO];
    	for(int i=0; i<allTags.length; i++) {
    		System.out.println(allTags[i]);
    		mTagAverageHistogram[i] = tmpCandidateTags.get(allTags[i]);
    	}    	
    	System.out.println();
    	
    	EncProgCommon.oos.writeObject(null);	
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    	    	
    	System.out.println("\t[S][START]\tBuild Encrypted Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryHistogram.length][this.mTagAverageHistogram.length];
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			for(int j=0; j<this.mTagAverageHistogram.length; j++) {
				System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				double start = System.nanoTime();
				
				this.mEncHungarianMatrix[i][j] = mDistance.evaluate(
						mPaillier, 
						mEncQueryDescriptor.elementAt(i), 
						mTagAverageDescriptor.elementAt(j));
				/**
				this.mEncHungarianMatrix[i][j] = 
					EncScore(this.mEncQueryHistogram[i], mTagAverageHistogram[j]);
				*/
				double end = System.nanoTime();
				double time = (end - start)/1000000000.0;
				System.out.print(mPaillier.Decryption(mEncHungarianMatrix[i][j]) + " " + time);
				//System.out.print(mPaillier.Decryption(this.mEncHungarianMatrix[i][j]) + " ");
			}
			//System.out.println();
		}
		/** Printing results
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			for(int j=0; j<this.mEncTagAverageHistogram.length; j++) {
				System.out.print(mPaillier.Decryption(this.mEncHungarianMatrix[i][j]) + " ");
			}
			System.out.println();
		}
		*/
		
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);		
		EncProgCommon.oos.writeObject(null);		
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "min";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];		
		EncFastHungarianAlgorithm EncFHA = 
			new EncFastHungarianAlgorithm(new GCComparisonServer(mPaillier), mPaillier, null);
		
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
		EncProgCommon.oos.writeObject(null);		
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		EncProgCommon.oos.writeObject(mMatchingTags[i]);
    	}
    	EncProgCommon.oos.flush();
    }    
    
	// Calculating score with square Euclidain distance
	private BigInteger EncScore(BigInteger[] EncQueryHistogram,  double[] mDatabaseHistogram) throws Exception {
		ComputingScoreServer computeServer = 
			new ComputingScoreServer(this.mPaillier, EncQueryHistogram, mDatabaseHistogram);
		computeServer.run();
		return computeServer.getScore();
	}
}
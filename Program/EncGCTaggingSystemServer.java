// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;

import Protocol.GCComparisonServer;
import Utils.EncFastHungarianAlgorithm;
import Crypto.CryptosystemPaillierServer;
import Score.ComputingScoreServer;

public class EncGCTaggingSystemServer extends ProgServer {

	private CryptosystemPaillierServer mPaillier = null;
		
	private BigInteger[][] mEncQueryHistogram = null;
	private BigInteger[][] mEncTagAverageHistogram = null;	
	private BigInteger[][] mEncHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
    public EncGCTaggingSystemServer() {
    }

    protected void init() throws Exception {
    	super.init();
    	
    	// recv key from client and setup
    	BigInteger[] pkey = new BigInteger[2]; 		
    	pkey[0] = new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
    	pkey[1] = new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
    	this.mPaillier = new CryptosystemPaillierServer(pkey);
    	System.out.println("\t[S][SUCCESS]\treceive public key pair (n, g).");
    	
    	EncrptTagAverageHistogram();
    }
    
	private void EncrptTagAverageHistogram() {		
		System.out.println("\t[S][START]\tEncrypt Database.");
		this.mEncTagAverageHistogram = 
			EncGCTaggingSystemCommon.encryption(mPaillier, mTagAverageHistogram);	
		System.out.println("\t[S][SUCCESS]\tEncrypt Database.");
	}
    
    protected void execQueryTransfer() throws Exception {    	    	
    	mEncQueryHistogram = new BigInteger[EncGCTaggingSystemCommon.ois.readInt()][BIN_HISTO]; 		
 		for(int i=0; i<mEncQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mEncQueryHistogram[i][j] = new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
 				//System.out.print(mPaillier.Decryption(mEncQueryHistogram[i][j]) + " ");
 			}
 			//System.out.println();
 		}
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    	    	
    	System.out.println("\t[S][START]\tBuild Encrypted Bipartile Graph.");
		double startTime = System.nanoTime();
		
		this.mEncHungarianMatrix = new BigInteger[this.mEncQueryHistogram.length][this.mEncTagAverageHistogram.length];
		for(int i=0; i<this.mEncQueryHistogram.length; i++) {
			for(int j=0; j<this.mEncTagAverageHistogram.length; j++) {
				System.out.printf("\t[S][START]\tComputing D(%d, %d)\n", i, j);
				this.mEncHungarianMatrix[i][j] = 
					EncScore(this.mEncQueryHistogram[i], mTagAverageHistogram[j]);
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
		EncGCTaggingSystemCommon.oos.writeObject(null);
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "max";
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
		EncGCTaggingSystemCommon.oos.writeObject(null);
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		EncGCTaggingSystemCommon.oos.writeObject(mMatchingTags[i]);
    	}
    	EncGCTaggingSystemCommon.oos.flush();
    }    
    
	// Calculating score with square Euclidain distance
	private BigInteger EncScore(BigInteger[] EncQueryHistogram,  double[] mDatabaseHistogram) throws Exception {
		ComputingScoreServer computeServer = 
			new ComputingScoreServer(this.mPaillier, EncQueryHistogram, mDatabaseHistogram);
		computeServer.run();
		return computeServer.getScore();
	}
}
// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;

import Protocol.ComparisonProtocolOnServer;
import Utils.EncFastHungarianAlgorithm;
import Crypto.CryptosystemPaillierServer;
import Score.ComputingScoreServer;

public class EncTaggingSystemServer extends ProgServer {

	private CryptosystemPaillierServer mPaillier = null;
		
	private BigInteger[][] mEncQueryHistogram = null;
	private BigInteger[][] mEncTagAverageHistogram = null;	
	private BigInteger[][] mEncHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
    public EncTaggingSystemServer() {
    }

    protected void init() throws Exception {
    	super.init();
    	
    	// recv key from client and setup
    	BigInteger[] pkey = new BigInteger[2]; 		
    	pkey[0] = new BigInteger(EncTaggingSystemCommon.ois.readObject().toString());
    	pkey[1] = new BigInteger(EncTaggingSystemCommon.ois.readObject().toString());
    	this.mPaillier = new CryptosystemPaillierServer(pkey);
    	System.out.println("\t[S][SUCCESS]\treceive public key pair (n, g).");
    	
    	EncrptTagAverageHistogram();
    }
    
	private void EncrptTagAverageHistogram() {		
		System.out.println("\t[S][START]\tEncrypt Database.");
		this.mEncTagAverageHistogram = 
			EncTaggingSystemCommon.encryption(mPaillier, mTagAverageHistogram);			
		System.out.println("\t[S][SUCCESS]\tEncrypt Database.");
	}
    
    protected void execQueryTransfer() throws Exception {    	    	
    	mEncQueryHistogram = new BigInteger[EncTaggingSystemCommon.ois.readInt()][BIN_HISTO]; 		
 		for(int i=0; i<mEncQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mEncQueryHistogram[i][j] = new BigInteger(EncTaggingSystemCommon.ois.readObject().toString());
 				//System.out.print(mEncQueryHistogram[i][j] + " ");
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
			}
		}				
		
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Encrypted Bipartile Graph." + time);		
		EncTaggingSystemCommon.oos.writeObject(null);
		EncGCTaggingSystemCommon.oos.flush();
    }    
    
    protected void execFindBestMatching() throws Exception {	
		System.out.println("\t[S][START]\tFind Bset Matching for Encrypted Bipartile Graph.");		
		
		String sumType = "max";
		int[][] assignment = new int[this.mEncHungarianMatrix.length][2];
		EncFastHungarianAlgorithm EncFHA = 
			new EncFastHungarianAlgorithm(new ComparisonProtocolOnServer(mPaillier), mPaillier, null);
		
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
		EncTaggingSystemCommon.oos.writeObject(null);
		//EncGCTaggingSystemCommon.oos.flush();
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		EncTaggingSystemCommon.oos.writeObject(mMatchingTags[i]);    		
    	}    	
    	EncTaggingSystemCommon.oos.flush();
    }    
    
	// Calculating score with square Euclidain distance
	private BigInteger EncScore(BigInteger[] EncQueryHistogram,  double[] mDatabaseHistogram) throws Exception {
		ComputingScoreServer computeServer = 
			new ComputingScoreServer(this.mPaillier, EncQueryHistogram, mDatabaseHistogram);
		computeServer.run();
		return computeServer.getScore();
	}
}
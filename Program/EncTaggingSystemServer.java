// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.BigInteger;

import Utils.FastHungarianAlgorithm;
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
    }    
    
    protected void execFindBestMatching() throws Exception {	
    }
    
    protected void execResultTransfer() throws Exception {
    }    
    
	// Calculating score with square Euclidain distance
	private BigInteger EncScore(BigInteger[] EncQueryHistogram,  double[] mDatabaseHistogram) throws Exception {
		ComputingScoreServer computeServer = 
			new ComputingScoreServer(this.mPaillier, EncQueryHistogram, mDatabaseHistogram);
		computeServer.run();
		return computeServer.getScore();
	}
}
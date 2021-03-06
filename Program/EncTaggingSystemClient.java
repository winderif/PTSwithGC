// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import Score.*;
import Utils.WriteOutput;
import Crypto.CryptosystemPaillierClient;
import Crypto.CryptosystemDGKClient;
import Protocol.ComparisonProtocolOnClient;

public class EncTaggingSystemClient extends ProgClient {
	private String[] mMatchingTags = null;
	private CryptosystemPaillierClient mPaillier = null;
	private CryptosystemDGKClient mDGK = null;
	
	public EncTaggingSystemClient() {
		this.mPaillier = new CryptosystemPaillierClient();
		this.mDGK = new CryptosystemDGKClient();
    }

    protected void init() throws Exception {
    	super.init();
    	
    	// key generation and send to server
//    	System.out.println("[C][STRAT]\tsend Paillier public key pair (n, g).");
    	EncProgCommon.oos.writeObject(mPaillier.getPublicKey());
    	
//    	System.out.println("[C][STRAT]\tsend DGK public key pair (n, g, h, u).");
    	EncProgCommon.oos.writeObject(mDGK.getPublicKey());

    	EncProgCommon.oos.flush();
    }
    
    protected void execQueryTransfer() throws Exception {
//		System.out.println("[C][STRAT]\tsend Query datas.");
		// Number of Query		
		EncProgCommon.oos.writeInt(videoFrames.size());
		//System.out.println(videoFrames.size());			
		
		//transferHistogram();
		transferDescriptor();
				 	    
//    	System.out.println("[C][SUCCESS]\tsend Query datas.");
    }
    
    private void transferHistogram() throws Exception {    	
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");					
				EncProgCommon.oos.writeObject(
						EncProgCommon.encryption(mPaillier, videoFrames.elementAt(i).getFeatureVector()[j]));																
			}
			//System.out.println();
		}
		EncProgCommon.oos.flush();		
				
		for(int i=0; i<BIN_HISTO; i++) {
			//System.out.print(queryAverageHistogram[i] + " ");
			EncProgCommon.oos.writeObject(
					EncProgCommon.encryption(mPaillier, queryAverageHistogram[i]));			
		}
		//System.out.println();
		EncProgCommon.oos.flush();
    }
    
    private void transferDescriptor() throws Exception {    	
		for(int i=0; i<videoFrames.size(); i++) {
			EncProgCommon.oos.writeObject(
					EncProgCommon.encryption(mPaillier, videoFrames.elementAt(i).getFeatureDescriptor()));
		}
		EncProgCommon.oos.flush();
		
		EncProgCommon.oos.writeObject(
				EncProgCommon.encryption(mPaillier, this.queryAverageDescriptor));
		EncProgCommon.oos.flush();
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
//    	System.out.println("[C][START]\tEvaluate Encrypted Domain Distance.");
    	ComputingScore computeClient = 
    		//new DistanceL2squareClient(mPaillier);
    		new DistanceL2squarePackingClient(mPaillier);	
    		//new DistanceWeightedL2squareClient(mPaillier);    		
    	computeClient.run();
//    	System.out.println("[C][SUCCESS]\tEvaluate Encrypted Domain Distance.");    	
    	
    	ComparisonProtocolOnClient protocolClient = 
    		new ComparisonProtocolOnClient(mPaillier, mDGK); 
    	protocolClient.run();    	
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    
//    	System.out.println("[C][START]\tBuild Encrypted Bipartile Graph.");
    	ComputingScore computeClient = 
    		//new DistanceL2squareClient(mPaillier);
    		new DistanceL2squarePackingClient(mPaillier);	
    		//new DistanceWeightedL2squareClient(mPaillier);
    	computeClient.run();    	    
//    	System.out.println("[C][SUCCESS]\tBuild Encrypted Bipartile Graph.");    	
    }    
    protected void execFindBestMatching() throws Exception {
//    	System.out.println("[C][START]\tFind Bset Matching for Encrypted Bipartile Graph.");	
    	ComparisonProtocolOnClient protocolClient = 
    		new ComparisonProtocolOnClient(mPaillier, mDGK); 
    	protocolClient.run();    	
    }
    
    protected void execResultTransfer() throws Exception {
    	mMatchingTags = new String[videoFrames.size()];	
    	for(int i=0; i<videoFrames.size(); i++) {
    		mMatchingTags[i] = (String)EncProgCommon.ois.readObject();
//    		System.out.println("[MATCH]\t" + (i+1) + "\t" + mMatchingTags[i]);
    	}
    	
    	WriteOutput.writeTagResult(mMatchingTags, iter, videoFrames.size(), 
    			ProgClient.queryDirName, "Exp_YouTube_orig_EncHE_TopSurf");
//    	System.out.println("[C][SUCCESS]\tRecv result from server.");
    }
}
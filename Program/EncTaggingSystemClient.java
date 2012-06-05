// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import Score.ComputingScoreClient;
import Crypto.CryptosystemPaillierClient;
import Protocol.ComparisonProtocolOnClient;

public class EncTaggingSystemClient extends ProgClient {
	private String[] mMatchingTags = null;
	private CryptosystemPaillierClient mPaillier = null;
	
	public EncTaggingSystemClient() {
		this.mPaillier = new CryptosystemPaillierClient();
    }

    protected void init() throws Exception {
    	super.init();
    	
    	// key generation and send to server
    	System.out.println("[C][STRAT]\tsend public key pair (n, g).");
    	EncTaggingSystemCommon.oos.writeObject(mPaillier.getPublicKey()[0]);
    	EncTaggingSystemCommon.oos.writeObject(mPaillier.getPublicKey()[1]);
    	EncTaggingSystemCommon.oos.flush();
    }
    
    protected void execQueryTransfer() throws Exception {
		System.out.println("[C][STRAT]\tsend Query datas.");
		// Number of Query		
		EncTaggingSystemCommon.oos.writeInt(videoFrames.size());
		//System.out.println(videoFrames.size());
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");				
				EncTaggingSystemCommon.oos.writeObject(
						EncTaggingSystemCommon.encryption(mPaillier, videoFrames.elementAt(i).getHistogram()[j]));
										
			}
			//System.out.println();
		}
		EncTaggingSystemCommon.oos.flush();
    }
    
    protected void execBuildBipartiteGraph() throws Exception {    
    	System.out.println("[C][START]\tBuild Encrypted Bipartile Graph.");
    	ComputingScoreClient computeClient = 
    		new ComputingScoreClient(mPaillier);
    	computeClient.run();    	    
    }    
    protected void execFindBestMatching() throws Exception {
    	System.out.println("[C][START]\tFind Bset Matching for Encrypted Bipartile Graph.");	
    	ComparisonProtocolOnClient protocolClient = 
    		new ComparisonProtocolOnClient(mPaillier); 
    	protocolClient.run();
    }
    
    protected void execResultTransfer() throws Exception {
    	mMatchingTags = new String[videoFrames.size()];	
    	for(int i=0; i<videoFrames.size(); i++) {
    		mMatchingTags[i] = EncTaggingSystemCommon.ois.readObject().toString();
    		System.out.println("[MATCH]\t" + (i+1) + "\t" + mMatchingTags[i]);
    	}    	
    	System.out.println("[C][SUCCESS]\tRecv result from server.");
    }
}
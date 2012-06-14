// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

public class TaggingSystemClient extends ProgClient {
	private String[] mMatchingTags = null;
	
	public TaggingSystemClient() {
	
    }

    protected void init() throws Exception {
    	super.init();
    }
    
    protected void execQueryTransfer() throws Exception {
		System.out.println("[C][STRAT]\tsend Query datas.");
		// Number of Query		
		TaggingSystemCommon.oos.writeInt(videoFrames.size());
		//System.out.println(videoFrames.size());
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");
				TaggingSystemCommon.oos.writeDouble(videoFrames.elementAt(i).getHistogram()[j]);				
			}
			//System.out.println();
		}
		
		for(int i=0; i<BIN_HISTO; i++) {
			System.out.print(queryAverageHistogram[i] + " ");
			TaggingSystemCommon.oos.writeDouble(queryAverageHistogram[i]);				
		}
		System.out.println();
		TaggingSystemCommon.oos.flush();
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
    	
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
    	
    }
    
    protected void execFindBestMatching() throws Exception {
    	
    }
    
    protected void execResultTransfer() throws Exception {    	    	
    	mMatchingTags = new String[videoFrames.size()];	
    	for(int i=0; i<videoFrames.size(); i++) {
    		mMatchingTags[i] = TaggingSystemCommon.ois.readObject().toString();
    		System.out.println("[MATCH]\t" + (i+1) + "\t" + mMatchingTags[i]);
    	}    	
    	System.out.println("[C][SUCCESS]\tRecv result from server.");
    }
}
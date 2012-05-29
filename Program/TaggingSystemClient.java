// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;

public class TaggingSystemClient extends ProgClient {
	public TaggingSystemClient() {
	
    }

    protected void init() throws Exception {
    	super.init();
    }
    
    protected void execQueryTransfer() throws Exception {
		System.out.println("[C][STRAT]\tsend Query datas.");
		// Number of Query		
		TaggingSystemCommon.oos.writeDouble(videoFrames.size());		
		for(int i=0; i<videoFrames.size(); i++) {
			for(int j=0; j<BIN_HISTO; j++) {
				//System.out.print(videoFrames.elementAt(i).getHistogram()[j] + " ");
				TaggingSystemCommon.oos.writeDouble(videoFrames.elementAt(i).getHistogram()[j]);				
			}
			//System.out.println();
		}
		//System.out.println();
		//mMode.setMode(Mode.SEND_QUERY);
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
    	
    }
    
    protected void execFindBestMatching() throws Exception {
    	
    }
}
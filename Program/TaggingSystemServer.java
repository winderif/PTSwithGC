// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

public class TaggingSystemServer extends ProgServer {

	private double[][] mQueryHistogram = null;	
	
    public TaggingSystemServer() {
    }

    protected void init() throws Exception {
    	super.init();
    }
    
    protected void execQueryTransfer() throws Exception {    	    	
    	mQueryHistogram = new double[TaggingSystemCommon.ois.readInt()][BIN_HISTO]; 		
 		for(int i=0; i<mQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mQueryHistogram[i][j] = TaggingSystemCommon.ois.readDouble();
 				//System.out.print(mQueryHistogram[i][j] + " ");
 			}
 			//System.out.println();
 		}
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
    	
    }
    
    protected void execFindBestMatching() throws Exception {
    	
    }
}
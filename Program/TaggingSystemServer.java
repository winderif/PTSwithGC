// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.util.Arrays;
import java.util.HashMap;

import Utils.FastHungarianAlgorithm;

public class TaggingSystemServer extends ProgServer {

	private double[][] mQueryHistogram = null;
	private double[] mQueryAverageHistogram = null;
	private double[][] mHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
	private double[] mDomainDistance = null;
	
    public TaggingSystemServer() {
    }

    protected void init() throws Exception {
    	super.init();
    }
    
    protected void execQueryTransfer() throws Exception {    	    	
    	mQueryHistogram = new double[TaggingSystemCommon.ois.readInt()][BIN_HISTO]; 
    	mQueryAverageHistogram = new double[BIN_HISTO];
 		for(int i=0; i<mQueryHistogram.length; i++) {
 			for(int j=0; j<BIN_HISTO; j++) {
 				mQueryHistogram[i][j] = TaggingSystemCommon.ois.readDouble();
 				//System.out.print(mQueryHistogram[i][j] + " ");
 			}
 			//System.out.println();
 		}
 		
 		for(int i=0; i<BIN_HISTO; i++) {
 			mQueryAverageHistogram[i] = TaggingSystemCommon.ois.readDouble();
			//System.out.print(mQueryAverageHistogram[i] + " ");
		}
 		//System.out.println(); 		 
 		
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    protected void execFindCandidateTagClusters() throws Exception {    	
    	mDomainDistance = new double[allDomains.length];    	
    	for(int i=0; i<allDomains.length; i++) {
    		mDomainDistance[i] = Score(mQueryAverageHistogram, mDomainAverageHistogram[i]);
    		System.out.print(mDomainDistance[i] + " ");
    	}
    	System.out.println();
    	double[] sortingDistance = mDomainDistance.clone();
    	Arrays.sort(sortingDistance);
    	System.out.println("MIN : \t" + sortingDistance[0]);
    	// 80% of minimum distance
    	double threshold 
    		= (sortingDistance[sortingDistance.length-1] - sortingDistance[0])*0.2 + sortingDistance[0];
    	System.out.println("T : \t" + threshold);  	
    	HashMap<String, double[]> tmpCandidateTags = new HashMap<String, double[]>();    	
    	for(int i=0; i<mDomainDistance.length; i++) {
    	    if(mDomainDistance[i] <= threshold) {
    	    	System.out.print(mDomainDistance[i] + " ");
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
    	/**
    	HashMap<String, double[]> tmpCandidateTags = new HashMap<String, double[]>();    	
    	for(int i=0; i<mQueryHistogram.length; i++) {
    		for(String tag : domainMap.get(allDomains[i])) {
	    		System.out.print(tag + " ");
	    		if(!tmpCandidateTags.containsKey(tag)) {
	    			System.out.print(tag + "-");
	    			tmpCandidateTags.put(tag, tagsHistogramMap.get(tag));
	    		}    	    		
	    	}
    	}
    	*/
    	allTags = new String[tmpCandidateTags.keySet().size()];
    	tmpCandidateTags.keySet().toArray(allTags);
    	mTagAverageHistogram = new double[allTags.length][BIN_HISTO];
    	for(int i=0; i<allTags.length; i++) {
    		System.out.println(allTags[i]);
    		mTagAverageHistogram[i] = tmpCandidateTags.get(allTags[i]);
    	}    	
    	System.out.println();
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
		System.out.println("\t[S][START]\tBuild Bipartile Graph.");
		double startTime = System.nanoTime();		
		mHungarianMatrix = new double[mQueryHistogram.length][mTagAverageHistogram.length];
		
		for(int i=0; i<mQueryHistogram.length; i++) {
			for(int j=0; j<mTagAverageHistogram.length; j++) {				
				mHungarianMatrix[i][j] = Score(mQueryHistogram[i], mTagAverageHistogram[j]);
				//this.mHungarianMatrix[i][j] = Score(this.mQueryHistogram[i], this.mTagAverageHistogram[j], j);
				//System.out.print(this.mHungarianMatrix[i][j] + " ");
			}
			//System.out.println();
		}
		//System.out.println();
		double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
		System.out.println("\t[S][SUCCESS]\tBuild Bipartile Graph." + time);
    }    
    
    protected void execFindBestMatching() throws Exception {
		System.out.println("\t[S][START]\tFind Bset Matching for Bipartile Graph.");
		
		String sumType = "min";
		int[][] assignment = new int[mHungarianMatrix.length][2];
		
		double startTime = System.nanoTime();
		/*** ***/
		assignment = FastHungarianAlgorithm.hgAlgorithm(mHungarianMatrix, sumType);
		/*** ***/
		double endTime = System.nanoTime();		
		double time = (endTime - startTime)/1000000000.0;
		
		for(int k=0; k<assignment.length; k++) {
			System.out.printf("array(%d,%d) = %.2f %s\n", 
					(assignment[k][0]+1), 
					(assignment[k][1]+1),
					mHungarianMatrix[assignment[k][0]][assignment[k][1]], 
					allTags[assignment[k][1]]);			
		}
		
		System.out.println("\t[S][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph." + time);
		
		mMatchingTags = new String[mQueryHistogram.length];		
		for(int i=0; i<mQueryHistogram.length; i++) {
			mMatchingTags[i] = allTags[assignment[i][1]];
			//System.out.println("[MATCH]\t" + (i+1) + "\t" + mMatchingTags[i]);
		}
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		TaggingSystemCommon.oos.writeObject(mMatchingTags[i]);
    	}
    	TaggingSystemCommon.oos.flush();
    }
    
	// Calculating score with square Euclidain distance
	private double Score(double[] keyframeHistogram, double[] tagPhotosHistogram) {
		double tmpScore = 0.0;
		double diff = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			diff = keyframeHistogram[i] - tagPhotosHistogram[i];
			tmpScore += diff*diff;
		}
		return tmpScore;
	}
}
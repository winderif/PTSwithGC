// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;

import Utils.FastHungarianAlgorithm;
import Utils.ValueComparator;

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
    	System.out.println("\t[S][START]\tEvaluate Encrypted Domain Distance.");
    	double startTime = System.nanoTime();
    	
    	/**
    	HashMap<String, Double> domainMap = new HashMap<String, Double>();
    	ValueComparator bvc = new ValueComparator(domainMap);
    	TreeMap<String, Double> sortingMap = new TreeMap<String, Double>(bvc);    	    
    	double maxDistance = 0.0;
    	*/
    	mDomainDistance = new double[allDomains.length];
    	for(int i=0; i<allDomains.length; i++) {
    		mDomainDistance[i] = Score(mQueryAverageHistogram, mDomainAverageHistogram[i]);
    		//System.out.print(mDomainDistance[i] + " ");
    		/**    		
    		System.out.println(allDomains[i]);
    		domainMap.put(allDomains[i], mDomainDistance[i]);
    		if(mDomainDistance[i] > maxDistance)
    			maxDistance = mDomainDistance[i];
    		*/
    	}
    	//System.out.println();
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tEvaluate Encrypted Domain Distance.");
    	System.out.println("time: " + time);
    	/**
    	for(int i=0; i<allDomains.length; i++) {
    		mDomainDistance[i] = maxDistance - mDomainDistance[i];
    		domainMap.put(allDomains[i], mDomainDistance[i]);
    	}
    	
    	sortingMap.putAll(domainMap);
    	
    	for(String domain : sortingMap.keySet()) {
    		System.out.println(domain + "\t" + sortingMap.get(domain));    		
    	}
    	String[] sortingDomain = new String[sortingMap.keySet().size()];
    	sortingMap.keySet().toArray(sortingDomain);
    	
    	for(int i=0; i<sortingDomain.length; i++) {    		
    		System.out.println(sortingDomain[i] + "\t" + sortingMap.get(sortingDomain[i]));
    		if(sortingMap.get(sortingDomain[i]) == null)
    			sortingMap.put(sortingDomain[i], sortingMap.get(sortingDomain[i-1]));
    	}
    	
    	HashMap<String, double[]> tmpCandidateTags = new HashMap<String, double[]>();
    	System.out.print(sortingMap.get(sortingDomain[0]) + " ");
    	System.out.print(sortingDomain[0] + "\t");
    	for(String tag : tagClustersMap.get(sortingDomain[0])) {
    		System.out.print(tag + " ");
    		if(!tmpCandidateTags.containsKey(tag)) {
    			System.out.print(tag + "-");
    			tmpCandidateTags.put(tag, tagsHistogramMap.get(tag));
    		}
    	}
    	double maximum = sortingMap.get(sortingDomain[sortingDomain.length - 1]);
    	double threshold = 0.0;
    	for(int i=1; i<mQueryHistogram.length; i++) {
    	//for(int i=1; i<sortingDomain.length; i++) {
    		//threshold = (maximum - sortingMap.get(sortingDomain[i-1]))*0.2 + sortingMap.get(sortingDomain[i-1]);
    		threshold = sortingMap.get(sortingDomain[i-1]) * 0.8;
    		if(sortingMap.get(sortingDomain[i]) >= threshold) {
    	    	System.out.print(sortingMap.get(sortingDomain[i]) + " ");
    	    	System.out.print(sortingDomain[i] + "\t");
    	    	for(String tag : tagClustersMap.get(sortingDomain[i])) {
    	    		System.out.print(tag + " ");
    	    		if(!tmpCandidateTags.containsKey(tag)) {
    	    			System.out.print(tag + "-");
    	    			tmpCandidateTags.put(tag, tagsHistogramMap.get(tag));
    	    		}    	    		
    	    	}
    	    	System.out.println();  	
    	    }
    		else
    			break;
    	}
    	*/
    	    	
    	double[] sortingDistance = mDomainDistance.clone();
    	Arrays.sort(sortingDistance);
    	System.out.println("MIN : \t" + sortingDistance[0]);
    	
    	// 80% of minimum distance
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	startTime = System.nanoTime();
    	
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
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
		System.out.println("\t[S][START]\tBuild Bipartile Graph.");
		double startTime = System.nanoTime();		
		mHungarianMatrix = new double[mQueryHistogram.length][mTagAverageHistogram.length];
		
		for(int i=0; i<mQueryHistogram.length; i++) {
			for(int j=0; j<mTagAverageHistogram.length; j++) {				
				mHungarianMatrix[i][j] = Score(mQueryHistogram[i], mTagAverageHistogram[j]);
				//this.mHungarianMatrix[i][j] = Score(this.mQueryHistogram[i], this.mTagAverageHistogram[j], j);
				//System.out.print(this.mHungarianMatrix[i][j]*1000000 + " ");
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
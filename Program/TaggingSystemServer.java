// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.util.Arrays;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Iterator;

import Utils.Create;
import Utils.FastHungarianAlgorithm;
import Utils.Print;
import Utils.ValueComparator;
import Score.*;

public class TaggingSystemServer extends ProgServer {

	private Distance mDistance = null;
	
	private int mQueryNum = 0;
	private double[][] mQueryHistogram = null;	
	private double[] mQueryAverageHistogram = null;
	private Vector<Map<Integer, Double>> mQueryDescriptor = null;
	private Map<Integer, Double> mQueryAverageDescriptor = null;
	private double[][] mHungarianMatrix = null;
	private String[] mMatchingTags = null;
	
	private double[] mDomainDistance = null;		
	
    public TaggingSystemServer() {
    }

    protected void init() throws Exception {    	
    	super.init();
    	
    	this.mDistance = new DistanceL2square();
    	//this.mDistance = new DistanceWeightedL2square();
    	//this.mDistance = new DistanceL1();
    	//this.mDistance = new DistanceWeightedL1();
    }
    
    protected void execQueryTransfer() throws Exception {
    	System.out.println("\t[S][START]\treceive Query datas.");
    	
    	mQueryNum = TaggingSystemCommon.ois.readInt();
    	//transferHistogram();
    	transferDescriptor();
    	
 		System.out.println("\t[S][SUCCESS]\treceive Query datas.");
    }
    
    private void transferHistogram() throws Exception {
    	mQueryHistogram = new double[mQueryNum][BIN_HISTO];    	    
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
    }
    
    private void transferDescriptor() throws Exception {
    	mQueryDescriptor = new Vector<Map<Integer, Double>>();
    	for(int i=0; i<mQueryNum; i++) {    		    	
    		mQueryDescriptor.add(
    				(LinkedHashMap<Integer, Double>)TaggingSystemCommon.ois.readObject());
    		/** Printing 
    		Print.printMap(mQueryDescriptor.elementAt(i), "\t[S]\tPrint mQueryDescriptor Map");
    		*/    		    		
    	}
    	
 		mQueryAverageDescriptor = 
 			(LinkedHashMap<Integer, Double>)TaggingSystemCommon.ois.readObject();
 		/** Printing 
 		Print.printMap(mQueryAverageDescriptor, "\t[S]\tPrint mQueryAverageDescriptor Map");
 		*/
    }
    
    protected void execFindCandidateTagClusters() throws Exception {
    	System.out.println("\t[S][START]\tEvaluate Domain Distance.");
    	double startTime = System.nanoTime();
    	    	
    	mDomainDistance = new double[allDomains.length];
    	for(int i=0; i<allDomains.length; i++) {
    		mDomainDistance[i] = mDistance.evaluate(
    				mQueryAverageDescriptor, mDomainAverageDescriptor.elementAt(i));    		
    		//System.out.print(mDomainDistance[i] + " ");
    	}
    	//System.out.println();
    	
    	double endTime = System.nanoTime();
		double time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tEvaluate Domain Distance." + time);    	
    	    	
    	double[] sortingDistance = mDomainDistance.clone();
    	Arrays.sort(sortingDistance);    	    	
    	//System.out.println("MIN : \t" + sortingDistance[0]);
    	
    	// 80% of maximum score
    	System.out.println("\t[S][START]\tFind Candidate Tag.");
    	startTime = System.nanoTime();
    	
    	double threshold 
    		= (sortingDistance[sortingDistance.length-1] - sortingDistance[0])*0.2 + sortingDistance[0];
    	//System.out.println("T : \t" + threshold);  	    	
    	HashMap<String, Map<Integer, Double>> tmpCandidateTags = Create.hashMap();
    	for(int i=0; i<mDomainDistance.length; i++) {
    	    if(mDomainDistance[i] <= threshold) {
    	    	/**
    	    	System.out.print(mDomainDistance[i] + " ");
    	    	System.out.print(allDomains[i] + "\t");
    	    	*/
    	    	for(String tag : tagClustersMap.get(allDomains[i])) {
    	    		//System.out.print(tag + " ");
    	    		if(!tmpCandidateTags.containsKey(tag)) {
    	    			//System.out.print(tag + "-");    	    			
    	    			tmpCandidateTags.put(tag, tagsDescriptorMap.get(tag));
    	    		}    	    		
    	    	}
    	    	//System.out.println();  	
    	    }
    	}
    	
    	endTime = System.nanoTime();
		time = (endTime - startTime)/1000000000.0;
    	System.out.println("\t[S][SUCCESS]\tFind Candidate Tag." + time);    	
    	
    	allTags = new String[tmpCandidateTags.keySet().size()];
    	tmpCandidateTags.keySet().toArray(allTags);    	
    	mTagAverageDescriptor = new Vector<Map<Integer, Double>>();
    	for(int i=0; i<allTags.length; i++) {
    		//System.out.println(allTags[i]);    		    	
    		mTagAverageDescriptor.add(tmpCandidateTags.get(allTags[i]));
    	}    	
    	//System.out.println();
    	
    	/** If # tag < # query */ 
    	if(mTagAverageDescriptor.size() < mQueryDescriptor.size()) {
    		System.out.println("\t[S][SUCCESS]\t# query < # tag.");
    		mQueryDescriptor = new Vector<Map<Integer, Double>>();
    		mQueryDescriptor.add(mQueryAverageDescriptor);
    	}
    }
    
    protected void execBuildBipartiteGraph() throws Exception {
		System.out.println("\t[S][START]\tBuild Bipartile Graph.");
		double startTime = System.nanoTime();
		
		mHungarianMatrix = new double[mQueryDescriptor.size()][mTagAverageDescriptor.size()];
		for(int i=0; i<mQueryDescriptor.size(); i++) {
			for(int j=0; j<mTagAverageDescriptor.size(); j++) {
				mHungarianMatrix[i][j] = mDistance.evaluate(
						mQueryDescriptor.elementAt(i), 
						mTagAverageDescriptor.elementAt(j));							
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
			System.out.printf("array(%d,%d) = %f %s\n", 
					(assignment[k][0]+1), 
					(assignment[k][1]+1),
					mHungarianMatrix[assignment[k][0]][assignment[k][1]], 
					allTags[assignment[k][1]]);			
		}
		
		System.out.println("\t[S][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph." + time);
		
		if(mQueryDescriptor.size() == 1) {
			mMatchingTags = new String[mQueryNum];
			mMatchingTags[0] = allTags[assignment[0][1]];
			for(int i=1; i<mQueryNum; i++) {
				mMatchingTags[i] = " ";					
			}
		}		
		else {			
			mMatchingTags = new String[mQueryDescriptor.size()];
			for(int i=0; i<mMatchingTags.length; i++) {
				mMatchingTags[i] = allTags[assignment[i][1]];
				//System.out.println("[MATCH]\t" + (i+1) + "\t" + mMatchingTags[i]);
			}	
		}	
    }
    
    protected void execResultTransfer() throws Exception {
    	System.out.println("\t[S][START]\tSend result to client.");
    	for(int i=0; i<mMatchingTags.length; i++) {
    		TaggingSystemCommon.oos.writeObject(mMatchingTags[i]);
    	}
    	TaggingSystemCommon.oos.flush();
    }    
}
package Utils;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import Utils.VideoFrame;

public class ImageClusteringByTags {
	private static final int FILTER_NUM = 2;
	//private static final int BIN_HISTO = 16;
	private static final int BIN_HISTO = 10000;   
	
	public static HashMap<String, Vector<VideoFrame>> 
		getImageClusters(Vector<Vector<VideoFrame>> databaseData) {
			
		HashMap<String, Vector<VideoFrame>> imageClustersMap 
			= new HashMap<String, Vector<VideoFrame>>();
		
		Vector<VideoFrame> tmpPhotos = null;
		String[] tmpTags = null;
		Vector<String> stopwordVector = null;
				
        try {
    		File stopwords = new File("Stopwords.txt");
            Scanner scanner = new Scanner(stopwords);
        	
        	for(int i=0; i<databaseData.size(); i++) {
        		for(int j=0; j<databaseData.elementAt(i).size(); j++) {
        			//System.out.print(this.databaseDatasFile.elementAt(i)[j] + " ");
				
        			tmpTags = databaseData.elementAt(i).elementAt(j).getTags();
        			for(int k=0; k<tmpTags.length; k++) {
        				if(imageClustersMap.get(tmpTags[k]) == null) {
        					tmpPhotos = new Vector<VideoFrame>();
        					tmpPhotos.add(databaseData.elementAt(i).elementAt(j));
        				} else {
        					tmpPhotos = imageClustersMap.get(tmpTags[k]);
        					tmpPhotos.add(databaseData.elementAt(i).elementAt(j));
        				}
        				imageClustersMap.put(tmpTags[k], tmpPhotos);	
        			}
        		}
        	}	        	        
        	
        	String remove = "";
			stopwordVector = new Vector<String>();

	        while(scanner.hasNext()){
	        	remove = scanner.nextLine().trim();
	        	stopwordVector.add(remove);
	        	if(imageClustersMap.containsKey(remove)){
	        		imageClustersMap.remove(remove);
	        	}
	        }	        	       
        } catch(IOException e) {
			e.printStackTrace();			
			System.exit(0);
		}	
        return imageClustersMap;
	}
	
	public static String[] getAllTags(HashMap<String, Vector<VideoFrame>> imageClustersMap) {
		String[] allTags = new String[imageClustersMap.keySet().size()];		
		imageClustersMap.keySet().toArray(allTags);
		//System.out.println("[INFO]\t allTags.length: " + allTags.length);
		//System.out.println("[INFO]\t map.keySet().size(): "+imageClustersMap.keySet().size());
		
		/*** 移除小於n張圖的Tag cluster ***/
		for(int i = 0; i<allTags.length; i++) {
			if(imageClustersMap.get(allTags[i]).size() < FILTER_NUM) {
				imageClustersMap.remove(allTags[i]);
			}						
		}
		
		allTags = new String[imageClustersMap.keySet().size()];
		imageClustersMap.keySet().toArray(allTags);
		//System.out.println("[INFO]\t After remove : allTags.length: "+allTags.length);
		//System.out.println("[INFO]\t After remove : map.keySet().size(): "+imageClustersMap.keySet().size());
		/*for(int i=0; i<allTags.length; i++) {
			System.out.println(allTags[i]);
		}*/
		/*** 移除小於n張圖的Tag cluster *** END */
		return allTags;
	}
	
	public static double[][] getTagAverageHistogram(
			HashMap<String, Vector<VideoFrame>> imageClustersMap, 
			String[] allTags) {
				
		double[][] mTagAverageHistogram 
			= new double[imageClustersMap.keySet().size()][BIN_HISTO];
		
		double[] tmpHistogram = null;
				
		for(int i=0; i<imageClustersMap.keySet().size(); i++) {
			//System.out.println("[TAG]\t" + allTags[i] + "\t" + this.imageClustersMap.get(allTags[i]).size());			
			for(VideoFrame mPhoto : imageClustersMap.get(allTags[i])) {
				tmpHistogram = mPhoto.getFeatureVector();
				for(int j=0; j<BIN_HISTO; j++) {
					mTagAverageHistogram[i][j] += tmpHistogram[j];
				}
			}
			
			for(int j=0; j<BIN_HISTO; j++) {
				mTagAverageHistogram[i][j] /= imageClustersMap.get(allTags[i]).size();
				//System.out.print(this.mTagAverageHistogram[i][j] + " ");
			}
			//System.out.println();			
		}
		
		return mTagAverageHistogram;
	}
	
	public static HashMap<String, double[]> getTagsHistogramMap(
			String[] allTags, double[][] mTagAverageHistogram) {
		
		HashMap<String, double[]> tagsHistogramMap = new HashMap<String, double[]>();
		
		for(int i=0; i<allTags.length; i++) {
			/*** HashMap for <tag, tag histogram>***/
			tagsHistogramMap.put(allTags[i], mTagAverageHistogram[i]);
		}
		
		return tagsHistogramMap;
	}
			
}

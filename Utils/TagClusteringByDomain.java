package Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

import com.articulate.sigma.WordNet;
import Utils.DomainsData;

public class TagClusteringByDomain {		
	private static final String domainFileName = "domains_SUMO.dat";
	//private static final int BIN_HISTO = 16;     
	private static final int BIN_HISTO = 10000;     
	
	public static boolean existedDomainFile(File databaseTagDirFile) {
		return databaseTagDirFile.getName().equals(domainFileName);
	}
	
	public static HashMap<String, Vector<String>> getTagClusters(String[] allTags) {
		HashMap<String, Vector<String>> tagClustersMap = new HashMap<String, Vector<String>>();
				
		WordNet.initOnce();
		
		Vector<String> tmpTags = null;
		String tmpDomain;		
		//int[] POS = new int[]{wn.NOUN, wn.VERB};	
				
		for(String tmpTag : allTags) {
			tmpDomain = WordNet.wn.getBestDefaultSense(tmpTag);
			
			if(tmpDomain != null) {
				if(!tagClustersMap.containsKey(tmpDomain)) {
					//System.out.print(tmpDomain + "\t\t");
					tmpTags = new Vector<String>();	    				
    				tmpTags.add(tmpTag);
    			}
    			else {
    				tmpTags = tagClustersMap.get(tmpDomain);
    				tmpTags.add(tmpTag);	    				
    			}	
				tagClustersMap.put(tmpDomain, tmpTags);
			}
			else {
				continue;
			}					
			//System.out.println();
		}
		
		return tagClustersMap;
	}
	
	public static String[] getAllDomains(
			HashMap<String, Vector<String>> tagClustersMap,
			String databaseDirName) {
		
		String[] allDomains = null;
		
		allDomains = new String[tagClustersMap.keySet().size()];
		tagClustersMap.keySet().toArray(allDomains);
		
		try {
			FileWriter outFile = new FileWriter(databaseDirName + "\\" + domainFileName);
			PrintWriter out = new PrintWriter(outFile);
			
			out.println(allDomains.length);
			for(int i=0; i < allDomains.length; i++) {
				//System.out.print(allDomains[i] + "\t: ");
				out.print(allDomains[i] + ":\t");
				for(String tmp : tagClustersMap.get(allDomains[i])) {
					out.print(tmp + " ");
					//System.out.print(tmp + " ");
				}
				out.println();
				//System.out.println();
			}
			
			out.close();
			outFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return allDomains;
	}
	
	public static DomainsData getDomainsData(String databaseDirName) {	
		DomainsData tmpDomainsData = new DomainsData();
		tmpDomainsData.tagClustersMap = new HashMap<String, Vector<String>>();
		
		try {
			FileReader inFile = new FileReader(databaseDirName + "\\" + domainFileName);				
			int in = 0;
			StringBuilder tmpFile = new StringBuilder();
			Vector<String> vectorTags = null;
							
			while((in = inFile.read()) != -1) {
				tmpFile.append((char)in);
			}
			// split each line
			String[] tmpLine = tmpFile.toString().split("\r\n");
			
			int length = Integer.parseInt(tmpLine[0]);
			tmpDomainsData.allDomains = new String[length];
			
			for(int i=1; i<tmpLine.length; i++) {
				String[] tmpDomainTags = tmpLine[i].split(":\t");
				String[] tmpTags = tmpDomainTags[1].split(" ");
				vectorTags = new Vector<String>();
				for(int j=0; j<tmpTags.length; j++) {
					vectorTags.add(tmpTags[j]);
				}
				tmpDomainsData.allDomains[i-1] = tmpDomainTags[0];
				tmpDomainsData.tagClustersMap.put(tmpDomainTags[0], vectorTags);
				//System.out.println(tmpLine[i]);
			}
			/** Printing
			for(int i=0; i < allDomains.length; i++) {
				System.out.print(allDomains[i] + "\t: ");					
				for(String tmp : domainMap.get(allDomains[i])) {						
					System.out.print(tmp + " ");
				}					
				System.out.println();
			}
			*/

			inFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}	
		
		return tmpDomainsData;
	}
	
	public static double[][] getDomainAverageColorHistogram(
			HashMap<String, Vector<String>> tagClustersMap,
			String[] allDomains,
			HashMap<String, double[]> tagsHistogramMap) {
		
		double[][] mDomainAverageHistogram 
			= new double[tagClustersMap.keySet().size()][BIN_HISTO];
		double[] tmpHistogram = null;
								
		for(int i=0; i<allDomains.length; i++) {
			//System.out.println("[DOMAIN]\t" + allDomains[i] + "\t" + domainMap.get(allDomains[i]).size());			
			for(String mTag : tagClustersMap.get(allDomains[i])) {
				tmpHistogram = tagsHistogramMap.get(mTag);		
				for(int j=0; j<BIN_HISTO; j++) {
					mDomainAverageHistogram[i][j] += tmpHistogram[j];
				}
			}
			
			for(int j=0; j<BIN_HISTO; j++) {
				mDomainAverageHistogram[i][j] /= tagClustersMap.get(allDomains[i]).size();
				//System.out.print(mDomainAverageHistogram[i][j] + " ");
			}
			//System.out.println();			
		}
		
		return mDomainAverageHistogram;
	}
}

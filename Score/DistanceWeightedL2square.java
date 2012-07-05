package Score;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

public class DistanceWeightedL2square extends Distance {
	private double[] idf = null;
	private static final int TOPSURF_BIN = 10000;
	
	public DistanceWeightedL2square() {
		loadTopSurfIDF();
	}
	
	public double evaluate(double[] q, double[] d) {
		double BIN_HISTO = q.length;
		double tmpScore = 0.0;
		double diff = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			diff = q[i] - d[i];
			tmpScore += (diff*diff) * idf[i];
		}
		return tmpScore;
	}
	
	public double evaluate(
			LinkedHashMap<Integer, Double> q, 
			LinkedHashMap<Integer, Double> d) {
		
		double tmpScore = 0.0;
		double diff = 0.0;
		double weight = 0.0;
		Iterator qIter = q.entrySet().iterator();
		Iterator dIter = d.entrySet().iterator();
		Map.Entry<Integer, Double> qPair = null;
		Map.Entry<Integer, Double> dPair = null;
		boolean isQueryNext = false;
		boolean isDatabaseNext = false;
		while(qIter.hasNext() || (isQueryNext == true)) {			
			if(isQueryNext == false) {
				qPair = (Map.Entry<Integer, Double>)qIter.next();
			}
			
			if(dIter.hasNext() || (isDatabaseNext == true)) {
				if(isDatabaseNext == false) { 
					dPair = (Map.Entry<Integer, Double>)dIter.next();
				}
				
				if(qPair.getKey() < dPair.getKey()) {
					diff = qPair.getValue();		
					weight = idf[qPair.getKey()];
					isQueryNext = false;		
					isDatabaseNext = true;
				}
				else if(qPair.getKey() > dPair.getKey()) {
					diff = dPair.getValue();
					weight = idf[dPair.getKey()];
					isQueryNext = true;		
					isDatabaseNext = false;
				}
				else {
					diff = qPair.getValue() - dPair.getValue();
					weight = idf[qPair.getKey()];
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {
				diff = qPair.getValue();
				weight = idf[qPair.getKey()];
				isQueryNext = false;
			}		
			tmpScore += (diff*diff) * weight;
		}
		
		if(isDatabaseNext == true) { 		
			diff = dPair.getValue();
			weight = idf[dPair.getKey()];
			tmpScore += (diff*diff) * weight;
		}
		while(dIter.hasNext()) {
			dPair = (Map.Entry<Integer, Double>)dIter.next();
			diff = dPair.getValue();
			weight = idf[dPair.getKey()];
			tmpScore += (diff*diff) * weight;			
		}
		return tmpScore;
	}
	
	protected void loadTopSurfIDF() {
		System.out.println("\t[S][START]\tLoad Topsurf idf weight.");
		
		File idfFile = new File("topsurf/idf/idf_10000.txt");
		idf = new double[TOPSURF_BIN];
		
		if(idfFile.isFile() == false || idfFile == null) {
			System.out.println("\t[S][START]\tCannot load Topsurf idf weight.");
		}
		else {
			try {
				FileReader inFile = new FileReader(idfFile);
				StringBuilder tmpText = new StringBuilder();
				int in = 0;
				while((in = inFile.read()) != -1) {
					tmpText.append((char)in);
				}
				
				//System.out.println(tmpText);
				
				String[] tmpNum = tmpText.toString().split(" ");
				
				for(int i=0; i<TOPSURF_BIN; i++) {
					idf[i] = Double.parseDouble(tmpNum[i]);
					//System.out.println(idf[i]);
				}
				System.out.println("\t[S][SUCCESS]\tLoad Topsurf idf weight.");
				
				inFile.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}			
	}
}

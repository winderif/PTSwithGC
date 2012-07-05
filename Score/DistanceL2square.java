package Score;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

public class DistanceL2square extends Distance {	
	public double evaluate(double[] q, double[] d) {
		double BIN_HISTO = q.length;
		double tmpScore = 0.0;
		double diff = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			diff = q[i] - d[i];
			tmpScore += diff*diff;
		}
		return tmpScore;
	}
	
	public double evaluate(
			LinkedHashMap<Integer, Double> q, 
			LinkedHashMap<Integer, Double> d) {
		
		double tmpScore = 0.0;
		double diff = 0.0;
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
					isQueryNext = false;		
					isDatabaseNext = true;
				}
				else if(qPair.getKey() > dPair.getKey()) {
					diff = dPair.getValue();										
					isQueryNext = true;		
					isDatabaseNext = false;
				}
				else {
					diff = qPair.getValue() - dPair.getValue();					
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {
				diff = qPair.getValue();				
				isQueryNext = false;
			}		
			tmpScore += diff*diff;
		}
		
		if(isDatabaseNext == true) { 		
			diff = dPair.getValue();
			tmpScore += diff*diff;
		}
		while(dIter.hasNext()) {
			dPair = (Map.Entry<Integer, Double>)dIter.next();
			diff = dPair.getValue();
			tmpScore += diff*diff;			
		}
		return tmpScore;
	}
}

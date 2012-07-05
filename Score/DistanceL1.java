package Score;

import java.math.BigInteger;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

import Crypto.CryptosystemPaillierServer;

public class DistanceL1 extends Distance {	
	public double evaluate(double[] q, double[] d) {
		double BIN_HISTO = q.length;
		double tmpScore = 0.0;
		double diff = 0.0;
		for(int i=0; i<BIN_HISTO; i++) {
			diff = Math.abs(q[i] - d[i]);
			tmpScore += diff;
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
					diff = Math.abs(qPair.getValue() - dPair.getValue());					
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {
				diff = qPair.getValue();				
				isQueryNext = false;
			}		
			tmpScore += diff;
		}
		
		if(isDatabaseNext == true) { 		
			diff = dPair.getValue();
			tmpScore += diff;
		}
		while(dIter.hasNext()) {
			dPair = (Map.Entry<Integer, Double>)dIter.next();
			diff = dPair.getValue();
			tmpScore += diff;			
		}
		return tmpScore;
	}
	
	public BigInteger evaluate(
			CryptosystemPaillierServer mPaillier,
			LinkedHashMap<Integer, BigInteger> q_Enc, 
			LinkedHashMap<Integer, Double> d) throws Exception {
		return null;
	}
}

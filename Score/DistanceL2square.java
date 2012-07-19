package Score;

import java.math.BigInteger;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

import Program.EncProgCommon;
import Utils.AdditivelyBlindProtocol;
import Crypto.CryptosystemPaillierServer;

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
	
	public double evaluate(Map<Integer, Double> q, Map<Integer, Double> d) {
		//BigInteger sum = BigInteger.ZERO;
		//BigInteger dif = BigInteger.ZERO;
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
					//dif = new BigInteger(Long.toString(Math.round(diff * 1000.0)));
					isQueryNext = false;		
					isDatabaseNext = true;
				}
				else if(qPair.getKey() > dPair.getKey()) {
					diff = dPair.getValue();				
					//dif = new BigInteger(Long.toString(Math.round(diff * 1000.0)));
					isQueryNext = true;		
					isDatabaseNext = false;
				}
				else {
					diff = qPair.getValue() - dPair.getValue();					
					//BigInteger qbig = new BigInteger(Long.toString(Math.round(qPair.getValue() * 1000.0)));
					//BigInteger dbig = new BigInteger(Long.toString(Math.round(dPair.getValue() * 1000.0)));
					//sum = sum.add(dbig.pow(2)).add(qbig.pow(2))
							//.subtract(dbig.multiply(qbig)).subtract(dbig.multiply(qbig));
					//dif = BigInteger.ZERO;
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {
				diff = qPair.getValue();				
				//dif = new BigInteger(Long.toString(Math.round(diff * 1000.0)));
				isQueryNext = false;
			}		
			tmpScore += diff*diff;
			//sum = sum.add(dif.multiply(dif));
		}
		
		if(isDatabaseNext == true) { 		
			diff = dPair.getValue();
			tmpScore += diff*diff;
			//dif = new BigInteger(Long.toString(Math.round(diff * 1000.0)));
			//sum = sum.add(dif.multiply(dif));
		}
		while(dIter.hasNext()) {
			dPair = (Map.Entry<Integer, Double>)dIter.next();
			diff = dPair.getValue();
			tmpScore += diff*diff;
			//dif = new BigInteger(Long.toString(Math.round(diff * 1000.0)));
			//sum = sum.add(dif.multiply(dif));
		}
		//System.out.print(sum + " ");
		return tmpScore;
	}
	
	public BigInteger evaluate(
			CryptosystemPaillierServer mPaillier,
			Map<Integer, BigInteger> q_Enc, 
			Map<Integer, Double> d) throws Exception {
		
		Map<Integer, BigInteger> d_big = EncProgCommon.DoubleMapToBigIntegerMap(d);		
		BigInteger s1 = evaluateFirstTerm(mPaillier, d_big);		
		BigInteger s2 = evaluateSecondTerm(mPaillier, q_Enc, d_big);		 	
		BigInteger s3 = evaluateThirdTerm(mPaillier, q_Enc);		
		BigInteger s = BigInteger.ONE.multiply(s1).multiply(s2).multiply(s3).mod(mPaillier.nsquare);
		return s;
	}
	
	private BigInteger evaluateFirstTerm(
			CryptosystemPaillierServer mPaillier, 
			Map<Integer, BigInteger> d_big) {
		
		BigInteger tmpS1 = BigInteger.ZERO;
		for(BigInteger w_i : d_big.values()) {
			tmpS1 = tmpS1.add(w_i.multiply(w_i));
		}
		BigInteger s1 = mPaillier.Encryption(tmpS1);
		return s1;
	}
	
	private BigInteger evaluateSecondTerm(
			CryptosystemPaillierServer mPaillier,
			Map<Integer, BigInteger> q_Enc, 
			Map<Integer, BigInteger> d_big) {
		
		BigInteger tmpS2 = BigInteger.ONE;								
		
		Iterator qIter = q_Enc.entrySet().iterator();
		Iterator dIter = d_big.entrySet().iterator();
		Map.Entry<Integer, BigInteger> qPair = null;
		Map.Entry<Integer, BigInteger> dPair = null;
		boolean isQueryNext = false;
		boolean isDatabaseNext = false;
		while(qIter.hasNext() || (isQueryNext == true)) {			
			if(isQueryNext == false) {
				qPair = (Map.Entry<Integer, BigInteger>)qIter.next();
			}
			
			if(dIter.hasNext() || (isDatabaseNext == true)) {
				if(isDatabaseNext == false) { 
					dPair = (Map.Entry<Integer, BigInteger>)dIter.next();
				}
				
				if(qPair.getKey() < dPair.getKey()) {									
					isQueryNext = false;		
					isDatabaseNext = true;
				}
				else if(qPair.getKey() > dPair.getKey()) {												
					isQueryNext = true;		
					isDatabaseNext = false;
				}
				else {
					// (-2)*w
					BigInteger tmpPow = dPair.getValue().multiply(new BigInteger("2"));
					//System.out.println("tmpPow:\t" + tmpPow + " ");
					// [w_bar]^((-2)*w) and w1*w2
					BigInteger tmpMul = qPair.getValue().modPow(tmpPow, mPaillier.nsquare);
					//System.out.println("tmpMul:\t" + mPaillier.Decryption(tmpMul));
					tmpS2 = tmpS2.multiply(tmpMul);
					//System.out.println("tmpS2:\t" + mPaillier.Decryption(tmpS2));
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {							
				isQueryNext = false;
			}					
		}		
		BigInteger s2 = tmpS2.modInverse(mPaillier.nsquare);		
		return s2;
	}
	
	private BigInteger evaluateThirdTerm(
			CryptosystemPaillierServer mPaillier,
			Map<Integer, BigInteger> q_Enc) throws Exception {
				
		BigInteger[] q_Enc_Array = new BigInteger[q_Enc.values().size()]; 
		q_Enc.values().toArray(q_Enc_Array);
		
		AdditivelyBlindProtocol r = new AdditivelyBlindProtocol(mPaillier, q_Enc_Array);		
		sendDistanceAdditivelyBlind(r.getAdditivelyBlindNumbers());
		
		BigInteger s3_p = new BigInteger(EncProgCommon.ois.readObject().toString());
		BigInteger s3 = r.getSumOfThirdPart(s3_p);
		return s3;
	}
	
	private void sendDistanceAdditivelyBlind(BigInteger[] x) throws Exception {
 		//System.out.println("\t[S][STRAT]\tsend AB datas of distance.");
 		
 		EncProgCommon.oos.writeInt(x.length);
 		for(int i=0; i<x.length; i++) { 		
 			//System.out.println(x[i]); 			
 			EncProgCommon.oos.writeObject(x[i]); 			
 		}
 		EncProgCommon.oos.flush(); 		
 	}
}

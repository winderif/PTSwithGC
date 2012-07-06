package Score;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Iterator;

import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;
import Utils.AdditivelyBlindProtocol;

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
	
	public double evaluate(Map<Integer, Double> q, Map<Integer, Double> d) {		
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
		
		Iterator iter = d_big.keySet().iterator();
		for(BigInteger w_i : d_big.values()) {
			int index = (Integer)iter.next();			
			long w = Math.round(idf[index] * 100.0);
			BigInteger weight = new BigInteger(Long.toString(w));			
			tmpS1 = tmpS1.add(w_i.multiply(w_i).multiply(weight));
			//System.out.println(tmpS1);			
		}		
		//System.out.println(tmpS1);
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
					int index = qPair.getKey();
					long w = Math.round(idf[index] * 100.0);
					BigInteger weight = new BigInteger(Long.toString(w));
										
					tmpS2 = tmpS2.multiply(tmpMul.modPow(weight, mPaillier.nsquare));
					//System.out.println("tmpS2:\t" + mPaillier.Decryption(tmpS2));					
					
					isQueryNext = false;
					isDatabaseNext = false;
				}				
			}
			else {							
				isQueryNext = false;
			}					
		}		
		BigInteger s2 = tmpS2.modPow(BigInteger.ONE.negate(), mPaillier.nsquare); 
		return s2;
	}
	
	private BigInteger evaluateThirdTerm(
			CryptosystemPaillierServer mPaillier,
			Map<Integer, BigInteger> q_Enc) throws Exception {
				
		BigInteger[] q_Enc_Array = new BigInteger[q_Enc.values().size()]; 
		q_Enc.values().toArray(q_Enc_Array);		
		
		AdditivelyBlindProtocol r = new AdditivelyBlindProtocol(mPaillier, q_Enc_Array);		
		BigInteger[] y_Enc_square_Array = 
			sendDistanceAdditivelyBlind(r.getAdditivelyBlindNumbers());
		BigInteger[] x_Enc_square_Array =
			r.getOringinalNumbers(y_Enc_square_Array);
		
		Iterator iter = q_Enc.keySet().iterator();
		BigInteger s3 = BigInteger.ONE;
		for(int i=0; i<x_Enc_square_Array.length; i++) {
			int index = (Integer)iter.next();
			long w = Math.round(idf[index] * 100.0);
			BigInteger weight = new BigInteger(Long.toString(w)); 
			s3 = s3.multiply(x_Enc_square_Array[i].modPow(weight, mPaillier.nsquare));
		}			
		return s3;
	}
	
	private BigInteger[] sendDistanceAdditivelyBlind(BigInteger[] x) throws Exception {
 		System.out.println("\t[S][STRAT]\tsend AB datas of distance.");
 		
 		BigInteger[] tmpEncArray = new BigInteger[x.length]; 		 		
 		for(int i=0; i<x.length; i++) { 		
 			//System.out.println(x[i]);
 			// Send to client
 			EncProgCommon.oos.writeObject(x[i]);
 			EncProgCommon.oos.flush(); 		
 			
 			tmpEncArray[i] = 
 				new BigInteger(EncProgCommon.ois.readObject().toString());
 			System.out.print(".");
 		}
 		System.out.println("x");
 		return tmpEncArray;
 	}
}

// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

import Utils.Create;

import Crypto.CryptosystemAbstract;

public class EncProgCommon extends ProgCommon {	
	private static final double SCALAR = 10000.0;
	
	public static BigInteger encryption(CryptosystemAbstract cryptosystem, double plaintext) {
		return encryption(cryptosystem, DoubleToBigInteger(plaintext));
	}
	
	public static BigInteger encryption(CryptosystemAbstract cryptosystem, BigInteger plaintext) {
		return cryptosystem.Encryption(plaintext);
	}
	
	public static Map<Integer, BigInteger> encryption(
			CryptosystemAbstract cryptosystem, Map<Integer, Double> plaintexts) {
		
		Map<Integer, BigInteger> tmpMap = Create.linkedHashMap(); 
		Iterator iter = plaintexts.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>) iter.next();
			tmpMap.put(pair.getKey(), encryption(cryptosystem, pair.getValue()));
		}		
		return tmpMap;
	}
	
	public static BigInteger[][] encryption(CryptosystemAbstract cryptosystem, double[][] plaintexts) {
		return encryption(cryptosystem, DoubleToBigInteger2DArray(plaintexts));
	}
	
	public static BigInteger[][] encryption(CryptosystemAbstract cryptosystem, BigInteger[][] plaintexts) {
		BigInteger[][] ciphertexts = new BigInteger[plaintexts.length][plaintexts[0].length];
		for(int i=0; i<ciphertexts.length; i++) {
			for(int j=0; j<ciphertexts[0].length; j++) {
				ciphertexts[i][j] = encryption(cryptosystem, plaintexts[i][j]);
			}
		}
		return ciphertexts;
	}
	
    /**
     * Transform Double into BigInteger.
     * @param d double value as a double
     * @return bigInteger value as a BigInteger
     * 12.03.13 winderif
     */    
    public static BigInteger DoubleToBigInteger(double d) {    
    	long tmp = Math.round(d * SCALAR);
    	return new BigInteger(Long.toString(tmp));
    }

    /**
     * Transform Double[] into BigInteger[].
     * @param d array of double value as a double[]
     * @return array of bigInteger value as a BigInteger[]
     * 12.03.13 winderif
     */
    public static BigInteger[] DoubleToBigInteger1DArray(double d[]) {
    	BigInteger[] tmpBigInteger = new BigInteger[d.length];
    	for(int i=0; i<d.length; i++) {
    		tmpBigInteger[i] = DoubleToBigInteger(d[i]);
    	}
    	return tmpBigInteger;
    }
    
    /**    
     * Transform Double[][] into BigInteger[][].
     * @param d array of double value as a double[]
     * @return array of bigInteger value as a BigInteger[]
     * 12.03.13 winderif
     */
    public static BigInteger[][] DoubleToBigInteger2DArray(double d[][]) {
    	BigInteger[][] tmpBigInteger = new BigInteger[d.length][d[0].length];
    	for(int i=0; i<d.length; i++) {    		
    		tmpBigInteger[i] = DoubleToBigInteger1DArray(d[i]);
    	}
    	return tmpBigInteger;
    }   
    
    public static Map<Integer, BigInteger> DoubleMapToBigIntegerMap(
			Map<Integer, Double> plaintexts) {
    	Map<Integer, BigInteger> ciphertexts = Create.linkedHashMap();
		Iterator iter = plaintexts.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<Integer, Double> pair = (Map.Entry<Integer, Double>) iter.next();
			ciphertexts.put(pair.getKey(), DoubleToBigInteger(pair.getValue()));
		}		
		return ciphertexts;   	
    }
}
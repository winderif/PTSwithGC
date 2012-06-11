// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Program;

import java.math.*;

import Crypto.CryptosystemAbstract;

public class EncGCTaggingSystemCommon extends ProgCommon {	
	private static final double SCALAR = 1000.0;
	
	public static BigInteger encryption(CryptosystemAbstract cryptosystem, double plaintext) {
		return encryption(cryptosystem, DoubleToBigInteger(plaintext));
	}
	
	public static BigInteger encryption(CryptosystemAbstract cryptosystem, BigInteger plaintext) {
		return cryptosystem.Encryption(plaintext);
	}
	
	public static BigInteger[][] encryption(CryptosystemAbstract cryptosystem, double[][] plaintexts) {
		return encryption(cryptosystem, DoubleToBigInteger2DArray(plaintexts));
	}
	
	public static BigInteger[][] encryption(CryptosystemAbstract cryptosystem, BigInteger[][] plaintexts) {
		BigInteger[][] ciphertexts = new BigInteger[plaintexts.length][plaintexts[0].length];
		for(int i=0; i<ciphertexts.length; i++) {
			for(int j=0; j<ciphertexts[0].length; j++) {
				//System.out.print(plaintexts[i][j] + " ");
				ciphertexts[i][j] = encryption(cryptosystem, plaintexts[i][j]);
				//System.out.print(cryptosystem.Decryption(ciphertexts[i][j]) + " ");
			}
			//System.out.println();
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
}
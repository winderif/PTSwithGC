package Protocol;

import java.math.BigInteger;
import java.util.Random;
import FastGC.Program.*;

import Crypto.*;
import Program.EncGCTaggingSystemCommon;

public class GCComparisonServer {
	private CryptosystemPaillierServer mPaillier;	
	private static int RANDOM_BIT = 40;
	private static final int L = 15;
	
	private BigInteger cInput;
	private static final int l = 20;
	private static final int k = 2;
	
	public GCComparisonServer() {
		this.mPaillier = null;	
	}
	
	public GCComparisonServer(CryptosystemPaillierServer p) {
		this.mPaillier = p;
	}
	
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		/**
		BigInteger r1 = BigInteger.probablePrime(RANDOM_BIT, new Random());
		BigInteger r2 = BigInteger.probablePrime(RANDOM_BIT, new Random());
		BigInteger r_min = BigInteger.probablePrime(RANDOM_BIT, new Random());
		*/
		System.out.println("EncA, EncB:\t" + mPaillier.Decryption(EncA) + " " + mPaillier.Decryption(EncB));
		
		BigInteger r2;
		BigInteger r_min;
		BigInteger r1 = r2 = r_min = BigInteger.ONE;
		BigInteger r_min_Enc = mPaillier.Encryption(r_min);
		
		// [y1] = [x1 + r1] = [x1] * [r1]
		BigInteger y_A_Enc = EncA.multiply(mPaillier.Encryption(r1)).mod(mPaillier.nsquare);
		// [y2] = [x2 + r2] = [x2] * [r2]
		BigInteger y_B_Enc = EncB.multiply(mPaillier.Encryption(r2)).mod(mPaillier.nsquare);
		System.out.println("y_A_Enc, y_B_Enc:\t" + mPaillier.Decryption(y_A_Enc) + " " + mPaillier.Decryption(y_B_Enc));
		
		try {
			System.out.println("\t[S]\tsend input.");
			EncGCTaggingSystemCommon.oos.writeObject(y_A_Enc);
			EncGCTaggingSystemCommon.oos.writeObject(y_B_Enc);
			EncGCTaggingSystemCommon.oos.flush();			
		} catch(Exception e) {						
			System.out.println("\t[S]\tsend input error.");
		}
		/**
		BigInteger r1_mod = r1.mod((new BigInteger("2")).pow(l));
		BigInteger r2_mod = r2.mod((new BigInteger("2")).pow(l));
		BigInteger r_min_mod = r_min.mod((new BigInteger("2")).pow(l));
		BigInteger r_min_mod_Enc = mPaillier.Encryption(r_min_mod);		
		cInput = mergeInput(new BigInteger[]{r_min_mod, r1_mod, r2_mod});
		*/
		cInput = mergeInput(new BigInteger[]{r_min, r1, r2});
		System.out.println("cInput:" + Long.toBinaryString(cInput.longValue()));
		
		try {
			System.out.println("\t[S]\trun gc.");
			FastGC.Program.ProgClient.serverIPname = new String("localhost");
			FastGC.Program.Program.iterCount = 1;
			FindMinimumClient minimumClient = new FindMinimumClient(cInput, l, k);
			minimumClient.run();
			
			BigInteger y_min_Enc
				= new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
			System.out.println("y_min_Enc:\t" + mPaillier.Decryption(y_min_Enc));
			BigInteger x_min_Enc 
				= y_min_Enc.multiply(r_min_Enc.modPow(BigInteger.ONE.negate(), mPaillier.nsquare))
				  			.mod(mPaillier.nsquare);
			System.out.println("x_min_Enc:\t" + mPaillier.Decryption(x_min_Enc));
			
			// min is A?
			BigInteger diffEncA = EncA.multiply(x_min_Enc.modInverse(mPaillier.nsquare)).mod(mPaillier.nsquare);
			EncGCTaggingSystemCommon.oos.writeObject(diffEncA);
			EncGCTaggingSystemCommon.oos.flush();
			
			BigInteger lambda_A
				= new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
			if(lambda_A.equals(BigInteger.ONE)) {
				System.out.println("A is Min");
				return EncA;
			}
			else {
				System.out.println("B is Min");
				return EncB;
			}			
		} catch(Exception e) {		
			System.out.println("\t[S][SUCCESS]\tCompare.");			
		}	
		return null;
	}
	
	public BigInteger findMinimumOfTwoEncValues(BigInteger[] EncArray, int type) {
		BigInteger[] r_Array = new BigInteger[EncArray.length + 1];
		BigInteger[] y_Array_Enc = new BigInteger[EncArray.length];
		BigInteger r_min = BigInteger.ONE; 
		BigInteger r_min_Enc = mPaillier.Encryption(r_min);
					
		/*** Printing */
		for(int i=0; i<EncArray.length; i++)
			System.out.print(mPaillier.Decryption(EncArray[i]) + " ");
		System.out.println();
				
		// initial r
		for(int i=0; i<EncArray.length; i++) {
			r_Array[i+1] = BigInteger.ONE;
			System.out.print(r_Array[i+1] + " ");
		}
		System.out.println();
						
		r_Array[0] = r_min;

		// initial y
		// [y] = [x + r] = [x] * [r]
		for(int i=0; i<EncArray.length; i++) {
			y_Array_Enc[i] = EncArray[i].multiply(mPaillier.Encryption(r_Array[i])).mod(mPaillier.nsquare);
			System.out.print(mPaillier.Decryption(y_Array_Enc[i]) + " ");
		}
		System.out.println();		
		
		try {
			System.out.println("\t[S]\tsend input.");
			EncGCTaggingSystemCommon.oos.writeInt(EncArray.length);
			for(int i=0; i<EncArray.length; i++) {
				EncGCTaggingSystemCommon.oos.writeObject(y_Array_Enc[i]);
			}			
			EncGCTaggingSystemCommon.oos.flush();			
		} catch(Exception e) {						
			System.out.println("\t[S]\tsend input error.");
		}
		
		cInput = mergeInput(r_Array);		
		System.out.println("cInput:" + cInput.toString());
		
		try {
			System.out.println("\t[S]\trun gc.");
			FastGC.Program.ProgClient.serverIPname = new String("localhost");
			FastGC.Program.Program.iterCount = 1;
			FindMinimumClient minimumClient = new FindMinimumClient(cInput, l, EncArray.length);
			minimumClient.run();
			
			BigInteger y_min_Enc
				= new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
			System.out.println("y_min_Enc:\t" + mPaillier.Decryption(y_min_Enc));
			BigInteger x_min_Enc 
				= y_min_Enc.multiply(r_min_Enc.modPow(BigInteger.ONE.negate(), mPaillier.nsquare))
				  			.mod(mPaillier.nsquare);
			System.out.println("x_min_Enc:\t" + mPaillier.Decryption(x_min_Enc));
					
			for(int i=0; i<EncArray.length; i++) {
				BigInteger diffEnc 
					= EncArray[i].multiply(x_min_Enc.modInverse(mPaillier.nsquare))
								.mod(mPaillier.nsquare);
				EncGCTaggingSystemCommon.oos.writeObject(diffEnc);
				EncGCTaggingSystemCommon.oos.flush();	
			
				BigInteger lambda
					= new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
				if(lambda.equals(BigInteger.ONE)) {
					return EncArray[i];
				}
				else
					continue;
			}						
			return null;
			
		} catch(Exception e) {		
			System.out.println("\t[S][SUCCESS]\tCompare.");			
		}	
		return null;
	}
	
	private BigInteger mergeInput(BigInteger[] input) {
		BigInteger tmp = BigInteger.ZERO;
		for(int i=0; i < input.length; i++) {
			tmp = tmp.shiftLeft(l).add(input[i]);
		}
		return tmp;
	}	
}

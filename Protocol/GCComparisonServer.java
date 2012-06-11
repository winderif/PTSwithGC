package Protocol;

import java.math.BigInteger;
import java.util.Random;
import FastGC.Program.*;

import Crypto.*;
import Program.EncGCTaggingSystemCommon;

public class GCComparisonServer {
	private CryptosystemPaillierServer mPaillier;	
	private static int RANDOM_BIT = 100;
	private static final int L = 15;
	
	private BigInteger cInput;
	private static final int l = 15;
	private static final int k = 2;
	
	public GCComparisonServer() {
		this.mPaillier = null;	
	}
	
	public GCComparisonServer(CryptosystemPaillierServer p) {
		this.mPaillier = p;
	}
	
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		//BigInteger r1 = BigInteger.probablePrime(L/2, new Random());
		//BigInteger r2 = BigInteger.probablePrime(L/2, new Random());
		//BigInteger r_min = BigInteger.probablePrime(L/2, new Random());
		System.out.println("EncA, EncB:\t" + mPaillier.Decryption(EncA) + " " + mPaillier.Decryption(EncB));
		
		BigInteger r2;
		BigInteger r_min;
		BigInteger r1 = r2 = r_min = BigInteger.ONE;
		BigInteger r_min_Enc = mPaillier.Encryption(r_min);
		
		// [y1] = [x1 + r1] = [x1] * [r1]
		BigInteger y_A_Enc = EncA.multiply(mPaillier.Encryption(r1)).mod(mPaillier.nsquare);
		// [y2] = [x2 + r2] = [x2] * [r2]
		BigInteger y_B_Enc = EncB.multiply(mPaillier.Encryption(r2)).mod(mPaillier.nsquare);
		
		try {
			System.out.println("\t[S]\tsend input.");
			EncGCTaggingSystemCommon.oos.writeObject(y_A_Enc);
			EncGCTaggingSystemCommon.oos.writeObject(y_B_Enc);
			EncGCTaggingSystemCommon.oos.flush();			
		} catch(Exception e) {						
			System.out.println("\t[S]\tsend input error.");
		}
		
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
			BigInteger x_min_Enc 
				= y_min_Enc.multiply(r_min_Enc.modPow(BigInteger.ONE.negate(), mPaillier.nsquare))
				  			.mod(mPaillier.nsquare);
			
			if(x_min_Enc.equals(EncA))
				System.out.println("A is Min");
			else if(x_min_Enc.equals(EncB))
				System.out.println("B is Min");
			else
				System.out.println("error");
						
			return x_min_Enc;
			
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

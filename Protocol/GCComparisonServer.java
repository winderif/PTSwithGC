package Protocol;

import java.math.BigInteger;
import FastGC.Program.*;

import Crypto.*;
import Program.EncProgCommon;

public class GCComparisonServer extends ComparisonProtocol  {
	private CryptosystemPaillierServer mPaillier;
	private BigInteger cInput;
	private static int RANDOM_BIT = 40;		
	private int K;
	
	public GCComparisonServer() {
		this.mPaillier = null;	
	}
	
	public GCComparisonServer(CryptosystemPaillierServer p) {
		this.mPaillier = p;
	}
		
	public BigInteger findMinimumOfTwoEncValues(BigInteger[] EncArray, int type) throws Exception {
		K = EncArray.length;
		BigInteger[] r_Array = new BigInteger[K + 1];
		BigInteger[] y_Array_Enc = new BigInteger[K];
		BigInteger r_min = BigInteger.ONE; 
		BigInteger r_min_Enc = mPaillier.Encryption(r_min);
					
		/*** Printing */
		for(int i=0; i < K; i++)
			System.out.print(mPaillier.Decryption(EncArray[i]) + " ");
		System.out.println();
				
		// initial r
		for(int i=0; i < K; i++) {
			r_Array[i+1] = BigInteger.ONE;
			//System.out.print(r_Array[i+1] + " ");
		}
		//System.out.println();
						
		r_Array[0] = r_min;

		// initial y, [y] = [x + r] = [x] * [r]		
		for(int i=0; i < K; i++) {
			y_Array_Enc[i] = EncArray[i].multiply(mPaillier.Encryption(r_Array[i])).mod(mPaillier.nsquare);
			//System.out.print(mPaillier.Decryption(y_Array_Enc[i]) + " ");
		}
		//System.out.println();
				
		
		try {
			System.out.println("\t[S]\tsend input.");
			EncProgCommon.oos.writeInt(type);			
			EncProgCommon.oos.writeInt(EncArray.length);			

			for(int i=0; i < K; i++) {
				EncProgCommon.oos.writeObject(y_Array_Enc[i]);				
			}								
			EncProgCommon.oos.flush();				
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("\t[S]\tsend input error.");
		}
		
		cInput = mergeInput(r_Array);		
		//System.out.println("cInput:" + cInput.toString());		
				
		try {
			System.out.println("\t[S]\trun gc.");
			FastGC.Program.ProgClient.serverIPname = new String("localhost");
			FastGC.Program.Program.iterCount = 1;
			FindMinimumClient minimumClient = new FindMinimumClient(cInput, L, EncArray.length);
			minimumClient.run();
			
			// Recv. [y_min]
			BigInteger y_min_Enc
				= new BigInteger(EncProgCommon.ois.readObject().toString());
			//System.out.println("y_min_Enc:\t" + mPaillier.Decryption(y_min_Enc));
			
			// [x_min] = [y_min - x_min] = [y_min] * [r_min]^(-1) 
			BigInteger x_min_Enc 
				= y_min_Enc.multiply(r_min_Enc.modInverse(mPaillier.nsquare))
				  			.mod(mPaillier.nsquare);
			//System.out.println("x_min_Enc:\t" + mPaillier.Decryption(x_min_Enc));
					
			for(int i=0; i < K; i++) {
				/**
				 *  [diff] = [x(i) - x_min]
				 *  if @diff is 0, x(i) is x_min
				 */				
				BigInteger diffEnc 
					= EncArray[i].multiply(x_min_Enc.modInverse(mPaillier.nsquare))
								.mod(mPaillier.nsquare);
				//System.out.println(mPaillier.Decryption(diffEnc));
				
				EncProgCommon.oos.writeObject(diffEnc);
				EncProgCommon.oos.flush();	
			
				BigInteger lambda
					= new BigInteger(EncProgCommon.ois.readObject().toString());
				if(lambda.equals(Enc_ZERO)) {
					return EncArray[i];
				}				
				else
					continue;
			}			
			System.err.println("\t[S]\tNo found value");
			return null;
			
		} catch(Exception e) {		
			e.printStackTrace();			
			return null;
		}			
	}
	
	private BigInteger mergeInput(BigInteger[] input) {
		BigInteger tmp = BigInteger.ZERO;
		for(int i=0; i < input.length; i++) {
			tmp = tmp.shiftLeft(L).add(input[i]);
		}
		return tmp;
	}	
}

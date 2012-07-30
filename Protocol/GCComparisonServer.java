package Protocol;

import java.math.BigInteger;
import java.util.Random;
import FastGC.Program.*;

import Crypto.*;
import Program.EncProgCommon;
import Utils.Print;

public class GCComparisonServer extends ComparisonProtocol  {
	private CryptosystemPaillierServer mPaillier;
	private BigInteger cInput;
	
	private static int K;
	
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
		
		/** Printing 
		Print.printEncArray(EncArray, "EncArray", mPaillier);
		*/
		
		// Initial r_min		
		BigInteger r_min = new BigInteger(L + SECURITY_BIT, new Random()); 
		BigInteger r_min_Enc = mPaillier.Encryption(r_min);		
		r_Array[0] = r_min;						
				
		// Initial r_i_mod = r_i mod MAX
		for(int i=1; i < K+1; i++) {			
			BigInteger r = new BigInteger(L + CORRECTNESS_BIT, new Random());
			// r_mod = r mod MAX
			r_Array[i] = r.mod(MAX);			
		}							
		
		// Initial y, [y] = [x + r] = [x] * [r]		
		for(int i=0; i < K; i++) {
			y_Array_Enc[i] = 
				EncArray[i].multiply(mPaillier.Encryption(r_Array[i + 1]))
							.mod(mPaillier.nsquare);			
		}
						
		try {
			//System.out.println("\t[S]\tsend input.");
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
				
		//System.out.println("\t[S]\trun gc.");
		FastGC.Program.ProgClient.serverIPname = new String("localhost");
		FastGC.Program.Program.iterCount = 1;
		FindMinimumClient minimumClient = 
			new FindMinimumClient(cInput, L, EncArray.length, L + SECURITY_BIT);
		minimumClient.run();				
		
		
		// Recv. [y_min]
		BigInteger y_min_Enc =
			new BigInteger(EncProgCommon.ois.readObject().toString());
		//System.out.println("y_min_Enc:\t" + mPaillier.Decryption(y_min_Enc));
			
		// [x_min] = [y_min - x_min] = [y_min] * [r_min]^(-1) 
		BigInteger x_min_Enc =
			y_min_Enc.multiply(r_min_Enc.modInverse(mPaillier.nsquare))
				  	.mod(mPaillier.nsquare);
		//System.out.println("x_min_Enc:\t" + mPaillier.Decryption(x_min_Enc));
			
		try {			
			for(int i=0; i < K; i++) {
				/**
				 *  [diff] = [x(i) - x_min]
				 *  if @diff is 0, x(i) is x_min
				 */				
				BigInteger diffEnc =
					EncArray[i].multiply(x_min_Enc.modInverse(mPaillier.nsquare))
								.mod(mPaillier.nsquare);
				//System.out.println(mPaillier.Decryption(diffEnc));
				
				EncProgCommon.oos.writeObject(diffEnc);
				EncProgCommon.oos.flush();	
			
				BigInteger lambda =
					new BigInteger(EncProgCommon.ois.readObject().toString());
				if(lambda.equals(Enc_ZERO)) {
					return EncArray[i];
				}				
				else {
					continue;
				}
			}			
			System.err.println("\t[S]\tNo found value");
			return null;
			
		} catch(Exception e) {		
			e.printStackTrace();			
			return null;
		}			
	}
	
	private BigInteger mergeInput(BigInteger[] input) {
		BigInteger tmp = input[0];
		
		for(int i=1; i < input.length; i++) {
			tmp = tmp.shiftLeft(L).add(input[i]);
		}
		return tmp;
	}	
}

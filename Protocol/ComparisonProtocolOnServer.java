package Protocol;

import java.math.BigInteger;
import java.util.Random;
import Utils.Print;
import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;

public class ComparisonProtocolOnServer{
	private CryptosystemPaillierServer mPaillier;
	private static final int KAPA = 100;
	private static final int L = 15;
	private static final int U = 15;
	private static final BigInteger L_big = new BigInteger(Integer.toString(L));
	private static final BigInteger THREE = new BigInteger("3");
	private static final BigInteger Enc_ZERO = BigInteger.ONE;	
	private BigInteger TwoPowL = (new BigInteger("2")).pow(L);
	private BigInteger TwoPowNeL;
	private BigInteger[] d_bin_Array;
	private BigInteger[] r_bin_Array;
	private BigInteger[] c_Array;
	
	
	public ComparisonProtocolOnServer() {
		this.mPaillier = null;
		this.d_bin_Array = null;
		this.r_bin_Array = null;
		this.c_Array = null;
	}
	public ComparisonProtocolOnServer(CryptosystemPaillierServer p) {
		this.mPaillier = p;		
		TwoPowNeL = (new BigInteger("2")).modPow(L_big.negate(), mPaillier.nsquare);		
		this.d_bin_Array = new BigInteger[L+1];
		this.r_bin_Array = new BigInteger[L+1];
		this.c_Array = new BigInteger[L+1];
	}
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		try {
			// [z] = [ 2^L + a - b ] = [2^L]*[a]*[b]^(-1)			
			BigInteger z_Enc = mPaillier.Encryption(TwoPowL)
								.multiply(EncA)
								.multiply(EncB.modInverse(mPaillier.nsquare))
								.mod(mPaillier.nsquare);
						
			//System.out.println("z\t" + mPaillier.Decryption(z_Enc));
			
			// r is uniform random (KAPA + L + 1)-bit
			BigInteger r = BigInteger.probablePrime(KAPA + L + 1, new Random());
			//System.out.println("r\t" + r);
					
			// [d] = [z + r] = [z]*[r]
			BigInteger d_Enc = z_Enc.multiply(mPaillier.Encryption(r)).mod(mPaillier.nsquare);			
			//System.out.println("d_Enc\t" + mPaillier.Decryption(d_Enc));
						
			// send [d] to Alice						
			EncProgCommon.oos.writeObject(d_Enc);
			EncProgCommon.oos.flush();
			
			//System.out.println("\t[S][START]\tMasking");		
			// [d^] = [d mod 2^L]
			BigInteger d_head_Enc = new BigInteger(EncProgCommon.ois.readObject().toString());
			//System.out.println("d_head_Enc\t" + mPaillier.Decryption(d_head_Enc));
			
			// [d^_0] ... [d^_L-1]
			for(int i=0; i<L+1; i++) {
				d_bin_Array[i] = new BigInteger(EncProgCommon.ois.readObject().toString());				
			}
			/** Printing 
			Print.printEncArray(d_bin_Array, "d_bin_Array\t", mPaillier);
			*/
			// r^ = r mod 2^L
			BigInteger r_head = r.mod(TwoPowL);
			//System.out.println("r_head\t" + r_head);
			String r_bin = Long.toBinaryString(r_head.longValue());
			// r^_0 ... r^_L-1
			for(int i=0; i<(L+1) - r_bin.length(); i++) {
				r_bin_Array[i] = BigInteger.ZERO;
				
			}
			for(int i=0; i<r_bin.length(); i++) {
				r_bin_Array[i+(L+1) - r_bin.length()] =
					new BigInteger(Character.toString(r_bin.charAt(i))); 					
			}
			/** Printing 
			Print.printArray(r_bin_Array, "r_bin_Array\t");
			*/
			
			BigInteger s = (Long.lowestOneBit(System.currentTimeMillis()) == 1)?
					(BigInteger.ONE):
					(BigInteger.ONE.negate());					
			BigInteger s_Enc = mPaillier.Encryption(s);
			
			EncMask(s_Enc);
			
			for(int i=0; i<L+1; i++) {
				BigInteger r_i = BigInteger.probablePrime(U, new Random());
				BigInteger e_i = c_Array[i].modPow(r_i, mPaillier.nsquare);
				EncProgCommon.oos.writeObject(e_i);				
			}			
			EncProgCommon.oos.flush();
			
			//System.out.println("\t[S][START]\tGet lambda_b value");
						
			BigInteger lambda_Enc = 
				transformLambda(new BigInteger(EncProgCommon.ois.readObject().toString()), s);
			//System.out.println("lambda = " + lambda);
			
			// [r^] = Enc.(r^) 
			BigInteger r_head_Enc = mPaillier.Encryption(r_head);
							
			// [z~] = [d^ - r^] = [d^] * [r^]^(-1)
			BigInteger z_tilde_Enc = d_head_Enc.multiply(r_head_Enc.modInverse(mPaillier.nsquare))
											  .mod(mPaillier.nsquare);
			// [z mod 2^L] = [z~ + lambda*2^L] = [z~] * [lambda]^(2^L)
			BigInteger z_mod_Enc = z_tilde_Enc.multiply(lambda_Enc.modPow(TwoPowL, mPaillier.nsquare))
											 .mod(mPaillier.nsquare);
			// [z_LBS] = [2^-L * (z - (z mod 2^L))]
			BigInteger z_LBS_Enc = (z_Enc.multiply(z_mod_Enc.modInverse(mPaillier.nsquare)).mod(mPaillier.nsquare))
									.modPow(TwoPowNeL, mPaillier.nsquare);			
			//System.out.println("z_LBS_Enc = " + mPaillier.Decryption(z_LBS_Enc));			
			
			BigInteger r_bit = BigInteger.probablePrime(U, new Random());
			EncProgCommon.oos.writeObject(z_LBS_Enc.modPow(r_bit, mPaillier.nsquare));						
			EncProgCommon.oos.flush();
			
			BigInteger EncM_head = new BigInteger(EncProgCommon.ois.readObject().toString());			
			//System.out.println("EncM\t" + mPaillier.Decryption(EncM_head));
			BigInteger Enc_Min = (EncM_head.equals(Enc_ZERO))?(EncB):(EncA);
			//System.out.println("Enc_Min\t" + mPaillier.Decryption(Enc_Min));
			reset();
			return Enc_Min;
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("\t[S]Exception");
			return null;
		}
	}
	private void reset() {
		this.d_bin_Array = new BigInteger[L+1];
		this.r_bin_Array = new BigInteger[L+1];
		this.c_Array = new BigInteger[L+1];
	}
		
	private void EncMask(BigInteger s) {
		// [c_i] = [d^_i] * [r^_i] * [s] * {Multiply[w_j]}^3
		for(int i=0; i<L+1; i++) {
			c_Array[i] = (d_bin_Array[i].multiply(mPaillier.Encryption(r_bin_Array[i].negate()))
										.multiply(s))
										.multiply(MultiplyOfEncXOR(i).modPow(THREE, mPaillier.nsquare))
										.mod(mPaillier.nsquare);								
		}
	}
	
	private BigInteger MultiplyOfEncXOR(int last) {
		BigInteger tmp = BigInteger.ONE;
		for(int j=0; j<last; j++) {
			tmp = tmp.multiply(mPaillier.EncXOR(d_bin_Array[j], mPaillier.Encryption(r_bin_Array[j]), r_bin_Array[j]))
					 .mod(mPaillier.nsquare);
		}		
		return tmp;
	}	
	
	private BigInteger transformLambda(BigInteger lambda_b, BigInteger s) {
		if(s.equals(BigInteger.ONE)) {
			//System.out.println("lambda_b\t" + mPaillier.Decryption(lambda_b));			
			return lambda_b;
		}
		else {			
			return mPaillier.EncXOR(lambda_b, mPaillier.Encryption(BigInteger.ONE), BigInteger.ONE);
		}
	}
}

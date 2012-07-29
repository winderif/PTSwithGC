package Protocol;

import java.math.BigInteger;
import java.util.Random;
import Utils.Print;
import Crypto.CryptosystemPaillierServer;
import Crypto.CryptosystemDGKServer;
import Program.EncProgCommon;

public class ComparisonProtocolOnServer extends ComparisonProtocol {
	private CryptosystemPaillierServer mPaillier;
	private CryptosystemDGKServer mDGK;
		
	// DGK cryptosystem plaintext space with 8-bit values
	private static final int U = 8;
	private static final BigInteger THREE = new BigInteger("3");		
	
	// Paillier [1]
	private static BigInteger EncP_ONE;
	// DGK [[1]]
	private static BigInteger EncDGK_ONE;
	// DGK [[-1]]
	private static BigInteger EncDGK_NegONE;
	
	// [2^L] = Enc.( 2^L )
	private static BigInteger EncMAX;
	private static BigInteger MAXInv;
	private BigInteger[] d_bin_Array;
	private BigInteger[] r_bin_Array;
	private BigInteger[] c_Array;
	
	
	public ComparisonProtocolOnServer() {
		this.mPaillier = null;
		this.mDGK = null;
		this.d_bin_Array = null;
		this.r_bin_Array = null;
		this.c_Array = null;
	}
	public ComparisonProtocolOnServer(
			CryptosystemPaillierServer p, CryptosystemDGKServer dgk) {
		this.mPaillier = p;		
		this.mDGK = dgk;				
		this.d_bin_Array = new BigInteger[L+1];
		this.r_bin_Array = new BigInteger[L+1];
		this.c_Array = new BigInteger[L+1];
		
		EncP_ONE = mPaillier.Encryption(BigInteger.ONE);
		EncDGK_ONE = mDGK.Encryption(BigInteger.ONE);
		EncDGK_NegONE = mDGK.Encryption(BigInteger.ONE.negate());
		
		EncMAX = mPaillier.Encryption(MAX);
		MAXInv = (BigInteger.ONE.shiftLeft(L)).modInverse(mPaillier.nsquare);
	}
	
	private void reset() {
		this.d_bin_Array = new BigInteger[L+1];
		this.r_bin_Array = new BigInteger[L+1];
		this.c_Array = new BigInteger[L+1];
	}
	
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		try {
			// [z] = [ 2^L + a - b ] = [2^L]*[a]*[b]^(-1)			
			BigInteger z_Enc = EncMAX.multiply(EncA)
								.multiply(EncB.modInverse(mPaillier.nsquare))
								.mod(mPaillier.nsquare);
						
			//System.out.println("z\t" + mPaillier.Decryption(z_Enc));
			
			// r is uniform random (RANDOM_BIT + L + 1)-bit
			BigInteger r = new BigInteger(RANDOM_BIT + L + 1, new Random());
			//System.out.println("r\t" + r);
					
			// [d] = [z + r] = [z]*[r]
			BigInteger d_Enc = z_Enc.multiply(mPaillier.Encryption(r)).mod(mPaillier.nsquare);			
			//System.out.println("d_Enc\t" + mPaillier.Decryption(d_Enc));
						
			// send [d] to Alice						
			EncProgCommon.oos.writeObject(d_Enc);
			EncProgCommon.oos.flush();
						
			// [d^] = [d mod 2^L]
			BigInteger d_head_Enc = new BigInteger(EncProgCommon.ois.readObject().toString());
			//System.out.println("d_head_Enc\t" + mPaillier.Decryption(d_head_Enc));
			
			// [[d^_0]] ... [[d^_L-1]]
			for(int i=0; i<L+1; i++) {
				d_bin_Array[i] = new BigInteger(EncProgCommon.ois.readObject().toString());				
			}
			/** Printing 
			Print.printEncArray(d_bin_Array, "d_bin_Array\t", mPaillier);
			*/
			// r^ = r mod 2^L
			BigInteger r_head = r.mod(MAX);
			//System.out.println("r_head\t" + r_head);
			String r_bin = r_head.toString(2);
			// r^_0 ... r^_L-1
			for(int i=0; i<(L+1) - r_bin.length(); i++) {
				r_bin_Array[i] = BigInteger.ZERO;
				
			}
			for(int i=0; i<r_bin.length(); i++) {
				r_bin_Array[i+ (L+1) - r_bin.length()] =
					new BigInteger(Character.toString(r_bin.charAt(i))); 					
			}
			/** Printing 
			Print.printArray(r_bin_Array, "r_bin_Array\t");
			*/
			
			// Random s is {1, -1} by current time
			BigInteger s = (Long.lowestOneBit(System.currentTimeMillis()) == 1)?
					(BigInteger.ONE):(BigInteger.ONE.negate());		
					
			// [[s]] = Enc.DGK( s )
			BigInteger s_Enc = (s.equals(BigInteger.ONE))?
					(EncDGK_ONE):(EncDGK_NegONE);			
			
			// Comparing Private Inputs with DGK cryptosystem
			EncMask(s_Enc);
			
			// send [[ e_i * r_i ]] = [[ e_i ]]^r_i
			for(int i=0; i<L+1; i++) {
				BigInteger r_i = new BigInteger(U, new Random());
				BigInteger e_i = c_Array[i].modPow(r_i, mDGK.n);
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
			BigInteger z_mod_Enc = z_tilde_Enc.multiply(lambda_Enc.modPow(MAX, mPaillier.nsquare))
											 .mod(mPaillier.nsquare);
			// [z_LBS] = [2^-L * (z - (z mod 2^L))]
			BigInteger z_LBS_Enc = (z_Enc.multiply(z_mod_Enc.modInverse(mPaillier.nsquare)).mod(mPaillier.nsquare))
									.modPow(MAXInv, mPaillier.nsquare);			
			//System.out.println("z_LBS_Enc = " + mPaillier.Decryption(z_LBS_Enc));			
			
			// send [z_LBS * r_bit] = [z_LBS]^r_bit
			BigInteger r_bit = new BigInteger(U, new Random());
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
		
	private void EncMask(BigInteger s) {		
		// [[c_i]] = [[d^_i]] * [[r^_i]] * [[s]] * {Multiply[w_j]}^3
		for(int i=0; i<L+1; i++) {
			c_Array[i] = 
				(d_bin_Array[i].multiply(
						(r_bin_Array[i].negate().equals(BigInteger.ZERO))?
						(Enc_ZERO):(EncDGK_NegONE))
								.multiply(s))
								.multiply(MultiplyOfEncXOR(i).modPow(THREE, mDGK.n))
								.mod(mDGK.n);								
		}		
	}
	
	private BigInteger MultiplyOfEncXOR(int last) {
		BigInteger tmp = BigInteger.ONE;
		for(int j=0; j<last; j++) {
			tmp = tmp.multiply(mDGK.EncXOR(
					d_bin_Array[j], 
					(r_bin_Array[j].equals(BigInteger.ZERO))?(Enc_ZERO):(EncDGK_ONE), 
					r_bin_Array[j]))
					 .mod(mDGK.n);
		}		
		return tmp;
	}	
	
	private BigInteger transformLambda(BigInteger lambda_b, BigInteger s) {
		if(s.equals(BigInteger.ONE)) {
			//System.out.println("lambda_b\t" + mPaillier.Decryption(lambda_b));			
			return lambda_b;
		}
		else {			
			return mPaillier.EncXOR(lambda_b, EncP_ONE, BigInteger.ONE);
		}
	}		
}

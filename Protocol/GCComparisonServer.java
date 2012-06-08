package Protocol;

import java.math.BigInteger;
import java.util.Random;

import Crypto.CryptosystemPaillierServer;
import Program.EncTaggingSystemCommon;

public class GCComparisonServer {
	private CryptosystemPaillierServer mPaillier;
	private static int RANDOM_BIT = 100;
	private static final int L = 15;
	
	public GCComparisonServer() {
		this.mPaillier = null;	
	}
	
	public GCComparisonServer(CryptosystemPaillierServer p) {
		this.mPaillier = p;
	}
	
	public BigInteger findMinimumOfTwoEncValues(BigInteger EncA, BigInteger EncB) {
		BigInteger r1 = BigInteger.probablePrime(L/2, new Random());
		BigInteger r2 = BigInteger.probablePrime(L/2, new Random());
		
		// [y1] = [x1 + r1] = [x1] * [r1]
		BigInteger y_A_Enc = EncA.multiply(mPaillier.Encryption(r1)).mod(mPaillier.nsquare);
		// [y2] = [x2 + r2] = [x2] * [r2]
		BigInteger y_B_Enc = EncB.multiply(mPaillier.Encryption(r2)).mod(mPaillier.nsquare);
		
		try {
			EncTaggingSystemCommon.oos.writeObject(y_A_Enc);
			EncTaggingSystemCommon.oos.writeObject(y_B_Enc);
			EncTaggingSystemCommon.oos.flush();
		} catch(Exception e) {			
		}
		
		
		
		return null;
	}
}

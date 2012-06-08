package Protocol;

import java.math.BigInteger;

import Crypto.CryptosystemPaillierClient;
import Crypto.CryptosystemPaillierServer;
import Program.EncTaggingSystemCommon;

public class GCComparisonClient {
	private CryptosystemPaillierClient mPaillier;
	private static final int L = 15;
	
	public GCComparisonClient() {
		this.mPaillier = null;	
	}
	
	public GCComparisonClient(CryptosystemPaillierClient p) {
		this.mPaillier = p;
	}
	
	public void run(){
		while(true) {
			try {
				// y1, y2 = dec.([y1], [y2])
				BigInteger y_A
					= mPaillier.Decryption(new BigInteger(EncTaggingSystemCommon.ois.readObject().toString()));
				BigInteger y_B 
					= mPaillier.Decryption(new BigInteger(EncTaggingSystemCommon.ois.readObject().toString()));
				
				
				
			} catch(Exception e) {
				System.out.println("[C][SUCCESS]\tCompare.");
				break;
			}
		}
	}
}

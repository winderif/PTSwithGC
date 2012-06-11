package Protocol;

import java.math.BigInteger;

import Crypto.*;
import Program.EncGCTaggingSystemCommon;
import FastGC.Program.FindMinimumServer;

public class GCComparisonClient {
	private CryptosystemPaillierClient mPaillier;
	private static final int L = 15;
	
	private BigInteger sInput;
	private static final int l = 15;
	private static final int k = 2;
	
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
					= mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));
				BigInteger y_B 
					= mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));
				
				System.out.println();
				sInput = mergeInput(new BigInteger[]{y_A, y_B});
				
				FindMinimumServer minimumServer = new FindMinimumServer(sInput, l, k, 1);
				minimumServer.run();
				
				BigInteger y_min = minimumServer.getOutput();
				BigInteger y_min_Enc = mPaillier.Encryption(y_min);
				EncGCTaggingSystemCommon.oos.writeObject(y_min_Enc);
				EncGCTaggingSystemCommon.oos.flush();
				
			} catch(Exception e) {
				System.out.println("[C][SUCCESS]\tCompare.");
				break;
			}
		}
	}
	
	private BigInteger mergeInput(BigInteger[] input) {
		BigInteger tmp = BigInteger.ONE;
		for(int i=0; i < input.length; i++) {
			tmp = tmp.shiftLeft(l).add(input[i]);
		}
		return tmp;
	}
}

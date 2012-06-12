package Protocol;

import java.math.BigInteger;

import Crypto.*;
import Program.EncGCTaggingSystemCommon;
import FastGC.Program.FindMinimumServer;

public class GCComparisonClient {
	private CryptosystemPaillierClient mPaillier;
	private BigInteger sInput;
		
	private static final int L = 40;
	private int K;
	
	public GCComparisonClient() {
		this.mPaillier = null;	
	}
	
	public GCComparisonClient(CryptosystemPaillierClient p) {
		this.mPaillier = p;
	}
	
	public void run() throws Exception {
		while(true) {
			try {
				int type = EncGCTaggingSystemCommon.ois.readInt();
				K = EncGCTaggingSystemCommon.ois.readInt();				
				BigInteger[] y = new BigInteger[K];
				// y = dec.([y])				
				for(int i=0; i < K; i++) {
					y[i] = mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));					
					System.out.print(y[i] + " ");
				}								
				System.out.println();							
								
				sInput = mergeInput(y);
				
				FindMinimumServer minimumServer 
					= new FindMinimumServer(sInput, L, K, 1, type);
				minimumServer.run();								
				
				BigInteger y_min = minimumServer.getOutput();
				BigInteger y_min_Enc = mPaillier.Encryption(y_min);				
				EncGCTaggingSystemCommon.oos.writeObject(y_min_Enc);
				EncGCTaggingSystemCommon.oos.flush();							
				
				for(int i=0; i < K; i++) {
					BigInteger diffEnc
						= mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));
					//System.out.println(diffEnc);
					if(diffEnc.equals(BigInteger.ZERO)) {
						EncGCTaggingSystemCommon.oos.writeObject(BigInteger.ONE);
						EncGCTaggingSystemCommon.oos.flush();		
						break;
					}
					else {
						EncGCTaggingSystemCommon.oos.writeObject(mPaillier.Encryption(BigInteger.ONE));
						EncGCTaggingSystemCommon.oos.flush();
					}
				}				
			} catch(Exception e) {				
				System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");				
				break;
			}
		}
	}
	
	private BigInteger mergeInput(BigInteger[] input) {
		BigInteger tmp = BigInteger.ONE;
		for(int i=0; i < input.length; i++) {
			tmp = tmp.shiftLeft(L).add(input[i]);
		}
		return tmp;
	}
}

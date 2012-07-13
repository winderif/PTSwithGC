package Protocol;

import java.math.BigInteger;

import Crypto.*;
import Program.EncProgCommon;
import FastGC.Program.FindMinimumServer;

public class GCComparisonClient extends ComparisonProtocol {
	private CryptosystemPaillierClient mPaillier;
	private int exit = 0;
	private BigInteger sInput;
		
	private int K;
	
	public GCComparisonClient() {
		this.mPaillier = null;	
		this.exit = 0;
	}
	
	public GCComparisonClient(CryptosystemPaillierClient p) {
		this.mPaillier = p;
		this.exit = 0;
	}
	
	public void run() throws Exception {
		while(true) {
			try {
				this.exit = EncProgCommon.ois.readInt();
				if(this.exit == 1) {
					System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");
					break;
				}
				
				int type = EncProgCommon.ois.readInt();
				K = EncProgCommon.ois.readInt();				
				BigInteger[] y = new BigInteger[K];
				// y = dec.([y])				
				for(int i=0; i < K; i++) {
					y[i] = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));					
					System.out.print(y[i] + " ");
				}								
				System.out.println();							
								
				sInput = mergeInput(y);
				
				FindMinimumServer minimumServer 
					= new FindMinimumServer(sInput, L, K, 1, type);
				minimumServer.run();								
				
				BigInteger y_min = minimumServer.getOutput();
				BigInteger y_min_Enc = mPaillier.Encryption(y_min);				
				EncProgCommon.oos.writeObject(y_min_Enc);
				EncProgCommon.oos.flush();							
				
				for(int i=0; i < K; i++) {
					// diff = dec.( [diff] )
					BigInteger diffEnc
						= mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
					//System.out.println(diffEnc);
					
					// If diff == 0, then return [0] = 1
					if(diffEnc.equals(BigInteger.ZERO)) {
						EncProgCommon.oos.writeObject(Enc_ZERO);
						EncProgCommon.oos.flush();		
						break;
					}
					// If diff != 0, then return [1]
					else {
						EncProgCommon.oos.writeObject(mPaillier.Encryption(BigInteger.ONE));
						EncProgCommon.oos.flush();
					}
				}				
			} catch(Exception e) {				
				e.printStackTrace();				
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

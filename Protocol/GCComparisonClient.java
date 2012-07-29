package Protocol;

import java.math.BigInteger;

import Crypto.*;
import Program.EncProgCommon;
import Utils.ClientState;
import Utils.Print;
import FastGC.Program.FindMinimumServer;

public class GCComparisonClient extends ComparisonProtocol {
	private CryptosystemPaillierClient mPaillier;
	private ClientState exit = ClientState.CLIENT_EXEC;
	private BigInteger sInput;
		
	private int K;
	
	public GCComparisonClient() {
		this.mPaillier = null;	
		this.exit = ClientState.CLIENT_EXEC;
	}
	
	public GCComparisonClient(CryptosystemPaillierClient p) {
		this.mPaillier = p;
		this.exit = ClientState.CLIENT_EXEC;
	}
	
	public void run() throws Exception {
		while(true) {
			try {
				this.exit = (ClientState)EncProgCommon.ois.readObject();
				if(this.exit.equals(ClientState.CLIENT_EXIT)) {
					System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");
					break;
				}
				// Garbled circuit type, type = 1 (Max), type = 0 (Min)
				int type = EncProgCommon.ois.readInt();
				K = EncProgCommon.ois.readInt();	
				
				BigInteger[] y = new BigInteger[K];
				// y = dec.([y])				
				for(int i=0; i < K; i++) {
					BigInteger y_tmp = mPaillier.Decryption(
							new BigInteger(EncProgCommon.ois.readObject().toString()));
					// y_mod = y mod MAX
					y[i] = y_tmp.mod(MAX);
				}								
				/***/
				Print.printArray(y, "y");
								
				sInput = mergeInput(y);
				
				FindMinimumServer minimumServer 
					= new FindMinimumServer(sInput, L, K, type, L+RANDOM_BIT);
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

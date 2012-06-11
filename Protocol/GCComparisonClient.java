package Protocol;

import java.math.BigInteger;

import Crypto.*;
import Program.EncGCTaggingSystemCommon;
import FastGC.Program.FindMinimumServer;

public class GCComparisonClient {
	private CryptosystemPaillierClient mPaillier;
	private static final int L = 15;
	
	private BigInteger sInput;
	private static final int l = 20;
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
				int type = EncGCTaggingSystemCommon.ois.readInt();
				int length = EncGCTaggingSystemCommon.ois.readInt();
				BigInteger[] y = new BigInteger[length];
				// y = dec.([y])
				for(int i=0; i<length; i++) {
					y[i] = mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));
					System.out.print(y[i] + " ");
				}								
				System.out.println();
				
				sInput = mergeInput(y);
				
				FindMinimumServer minimumServer 
					= new FindMinimumServer(sInput, l, length, 1, type);
				minimumServer.run();
				
				BigInteger y_min = minimumServer.getOutput();
				BigInteger y_min_Enc = mPaillier.Encryption(y_min);
				EncGCTaggingSystemCommon.oos.writeObject(y_min_Enc);
				EncGCTaggingSystemCommon.oos.flush();							
				
				for(int i=0; i<length; i++) {
					BigInteger diffEnc
						= mPaillier.Decryption(new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString()));
					System.out.println(diffEnc);
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

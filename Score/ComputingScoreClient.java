package Score;

import Crypto.CryptosystemPaillierClient;
import Program.EncTaggingSystemCommon;
import Score.ComputingScore;
import java.io.*;
import java.math.BigInteger;

public class ComputingScoreClient extends ComputingScore {
	private CryptosystemPaillierClient mPaillier;	
	
	public ComputingScoreClient(CryptosystemPaillierClient arg0) {
		this.mPaillier = arg0;
	}
	
	public void run() throws Exception {
		super.run();
	}
	
	protected void init() throws Exception {		
	}
	
	protected void execute() {		
		while(true) {
			try {
				System.out.println("[C][STRAT]\tCompute Server distance.");
				BigInteger x_dec = BigInteger.ZERO;
				BigInteger s3_c = BigInteger.ZERO;
				
				for(int i=0; i<BIN_HISTO; i++) {	
					// de[x] = x					
					x_dec = mPaillier.Decryption((new BigInteger(EncTaggingSystemCommon.ois.readObject().toString())));
					/**
					 * System.out.print(x_dec + " ");
					 */
					// S3' = S3' + x^2
					s3_c = s3_c.add(x_dec.pow(2));
					//s3_c = s3_c.add((new BigInteger(this.getQueue().poll().toString())));				
				}			
				/**
				 * System.out.println();
				 */
				System.out.println("[C][SUCCESS]\trecv Server [x].");
				// Send [S3'] to server
				System.out.println("[C][STRAT]\tsend [S3'].");
				//this.getQueue().add(s3_c);		
				EncTaggingSystemCommon.oos.writeObject(mPaillier.Encryption(s3_c));
				EncTaggingSystemCommon.oos.flush();					
			} catch(Exception e) {
				System.out.println("[C][SUCCESS]\tBuild Encrypted Bipartile Graph.");
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		
	}	
}

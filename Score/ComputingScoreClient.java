package Score;

import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Score.ComputingScore;
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
				
				int descriptor_num = EncProgCommon.ois.readInt();
				for(int i=0; i<descriptor_num; i++) {	
					// de[x] = x					
					x_dec = mPaillier.Decryption(
							(new BigInteger(EncProgCommon.ois.readObject().toString())));
					/**
					 * System.out.print(x_dec + " ");
					 */
					 
					// S3' = S3' + x^2
					s3_c = s3_c.add(x_dec.pow(2));								
				}			
				/**
				 * System.out.println();
				 */
				System.out.println("[C][SUCCESS]\trecv Server [x].");
				// Send [S3'] to server
				System.out.println("[C][STRAT]\tsend [S3'].");
				
				//System.out.println("s3_c:\t" + s3_c);
				EncProgCommon.oos.writeObject(mPaillier.Encryption(s3_c));
				EncProgCommon.oos.flush();					
			} catch(Exception e) {
				System.out.println("[C]\tComplete Compute distance.");
				break;
			}
		}
	}	
}

package Score;

import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Score.ComputingScore;
import java.math.BigInteger;

public class DistanceWeightedL2squareClient extends ComputingScore {
	private CryptosystemPaillierClient mPaillier;	
	
	public DistanceWeightedL2squareClient(CryptosystemPaillierClient arg0) {
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
				//System.out.println("[C][STRAT]\tCompute Server distance.");
				BigInteger x_dec = BigInteger.ZERO;
				BigInteger x_dec_square = BigInteger.ZERO;
				
				// de[x] = x
				x_dec = mPaillier.Decryption(
						(new BigInteger(EncProgCommon.ois.readObject().toString())));
				/**
				 * System.out.println(x_dec + " ");
				 */
				// x^2 = x * x
				x_dec_square = x_dec.multiply(x_dec);
				
				//System.out.println("[C][SUCCESS]\trecv Server [x].");
				// Send [x^2] to server
				//System.out.println("[C][STRAT]\tsend [x^2].");
								
				EncProgCommon.oos.writeObject(mPaillier.Encryption(x_dec_square));
				EncProgCommon.oos.flush();					
			} catch(Exception e) {
				System.out.println("[C]\tComplete Compute distance.");
				break;
			}
		}
	}	
}

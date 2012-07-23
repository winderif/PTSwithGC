package Score;

import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Score.ComputingScore;
import Utils.ClientState;

import java.math.BigInteger;

public class DistanceL2squarePackingClient extends ComputingScore {
	private CryptosystemPaillierClient mPaillier;
	private ClientState exit = ClientState.CLIENT_EXEC;
	
	private BigInteger MAX = null;
	private BigInteger MASK = null;
	
	public DistanceL2squarePackingClient(CryptosystemPaillierClient arg0) {
		this.mPaillier = arg0;
		this.exit = ClientState.CLIENT_EXEC;
		this.MAX = BigInteger.ONE.shiftLeft(this.RANDOM_BIT - 1);
		this.MASK = BigInteger.ONE.shiftLeft(this.RANDOM_BIT + this.DATA_BIT)
									.subtract(BigInteger.ONE);
	}
	
	public void run() throws Exception {
		super.run();
	}
	
	protected void init() throws Exception {		
	}
	
	protected void execute() {		
		while(true) {
			try {
				this.exit = (ClientState)EncProgCommon.ois.readObject();
				if(this.exit.equals(ClientState.CLIENT_EXIT)) {
					System.out.println("[C][SUCCESS]\tComplete Compute distance.");
					break;
				}
				
				int k_cipher = EncProgCommon.ois.readInt();
				
				for(int i=0; i<k_cipher; i++) {
					parsePackedValue();
				}
				
				boolean isRemaining = EncProgCommon.ois.readBoolean();				
				if(isRemaining) {
					parsePackedValue();
				}
															
			} catch(Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private void parsePackedValue() throws Exception {
		BigInteger s3_c = BigInteger.ZERO;
		
		int k_blind = EncProgCommon.ois.readInt();
		
		BigInteger xPacked = mPaillier.Decryption(
				new BigInteger(EncProgCommon.ois.readObject().toString()));		
		
		for(int j=0; j<k_blind; j++) {	
			// x' = x_j - 2^(L-1)
			BigInteger tmpX = xPacked.and(MASK).subtract(MAX);						
			// shift
			xPacked = xPacked.shiftRight(this.RANDOM_BIT + this.DATA_BIT);
			// x'^2 = x' * x'
			s3_c = s3_c.add(tmpX.multiply(tmpX));												
		}			
		// send [s3'] to server
		EncProgCommon.oos.writeObject(mPaillier.Encryption(s3_c));
		EncProgCommon.oos.flush();	
	}
}

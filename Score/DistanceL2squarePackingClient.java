package Score;

import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Score.ComputingScore;
import Utils.ClientState;

import java.math.BigInteger;

public class DistanceL2squarePackingClient extends ComputingScore {
	private CryptosystemPaillierClient mPaillier;
	private ClientState exit = ClientState.CLIENT_EXEC;
	
	private static BigInteger MAX;
	private static BigInteger MASK;
	
	public DistanceL2squarePackingClient(CryptosystemPaillierClient arg0) {
		this.mPaillier = arg0;
		this.exit = ClientState.CLIENT_EXEC;
		// 2^(L - 1)
		MAX = BigInteger.ONE.shiftLeft(DATA_BIT - 1);
		// 2^(SIGMA + L)		
		MASK = BigInteger.ONE.shiftLeft(SECURITY_BIT + DATA_BIT)
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
				
				BigInteger s3_c = BigInteger.ZERO;
				int k_cipher = EncProgCommon.ois.readInt();
				
				for(int i=0; i<k_cipher; i++) {
					s3_c = s3_c.add(parsePackedValue());
				}
				
				boolean isRemaining = EncProgCommon.ois.readBoolean();				
				if(isRemaining) {
					s3_c = s3_c.add(parsePackedValue());
				}
							
				// send [s3'] to server
				EncProgCommon.oos.writeObject(mPaillier.Encryption(s3_c));
				EncProgCommon.oos.flush();
			} catch(Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	private BigInteger parsePackedValue() throws Exception {
		BigInteger s3_c = BigInteger.ZERO;
		
		int k_blind = EncProgCommon.ois.readInt();
		
		BigInteger xPacked = mPaillier.Decryption(
				new BigInteger(EncProgCommon.ois.readObject().toString()));		
		
		for(int j=0; j<k_blind; j++) {	
			// x' = x_j - 2^(L-1)
			BigInteger tmpX = (xPacked.and(MASK)).subtract(MAX);						
			// shift
			xPacked = xPacked.shiftRight(SECURITY_BIT + DATA_BIT);
			// x'^2 = x' * x'
			s3_c = s3_c.add(tmpX.multiply(tmpX));												
		}			
		
		return s3_c;
	}
}

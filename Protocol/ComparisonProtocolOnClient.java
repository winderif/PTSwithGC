package Protocol;

import java.math.BigInteger;

import Crypto.CryptosystemDGKClient;
import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Utils.ClientState;

public class ComparisonProtocolOnClient extends ComparisonProtocol {	
	private CryptosystemPaillierClient mPaillier;
	private CryptosystemDGKClient mDGK;
	
	private ClientState exit = ClientState.CLIENT_EXEC;			
	
	public ComparisonProtocolOnClient() {	
		this.mPaillier = null;
		this.mDGK = null;		
		this.exit = ClientState.CLIENT_EXEC;
	}
	public ComparisonProtocolOnClient(
			CryptosystemPaillierClient p, CryptosystemDGKClient dgk) {		
		this.mPaillier = p;
		this.mDGK = dgk;
		this.exit = ClientState.CLIENT_EXEC;	
	}
	
	public void run(){
		while(true) {		
			try {
				this.exit = (ClientState)EncProgCommon.ois.readObject();
				if(this.exit.equals(ClientState.CLIENT_EXIT)) {
					System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");
					break;
				}
				//System.out.println("[C][START]\tReduce d mod 2^L");
				// d = dec.( [d] )								
				BigInteger d = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
				//System.out.println("d\t" + d);
				
				// d^ = d mod 2^L
				BigInteger d_head = d.mod(TwoPowL);
				//System.out.println("d_head\t" + d_head);
				BigInteger d_head_Enc = mPaillier.Encryption(d_head);
				// send [d^] to Bob
				EncProgCommon.oos.writeObject(d_head_Enc);
				
				String d_bin = Long.toBinaryString(d_head.longValue());
				//System.out.println(d_bin.toCharArray().length);
				for(int i=0; i<(L+1) - d_bin.length(); i++) {					
					EncProgCommon.oos.writeObject(Enc_ZERO);
				}
				for(int i=0; i<d_bin.length(); i++) {			
					// send [[ d_bin ]]
					//System.out.print(d_bin.charAt(i));					
					EncProgCommon.oos.writeObject(mPaillier.Encryption(new BigInteger(Character.toString(d_bin.charAt(i)))));
				}
				//System.out.println();
				
				//System.out.println("[C][START]\tCheck all bit of Mask.");
				// Check [c_i * r_i] = [c_i]^r_i one of then is zero
				BigInteger check = BigInteger.ONE;
				int isCheck = 0;				
				for(int i=0; i<L+1; i++) {
					BigInteger tmp = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
					//System.out.println("bits = " + tmp);
					if(BigInteger.ZERO.equals(tmp)) {	
						isCheck = 1;						
					}											
				}
				if(isCheck == 0) {
					// [c_i] has no zero, return [0] = 1
					check = BigInteger.ZERO;
					EncProgCommon.oos.writeObject(Enc_ZERO);	
				}
				else if(isCheck == 1) {
					// [c_i] has One zero, return [1]
					EncProgCommon.oos.writeObject(mPaillier.Encryption(check));	
				}
				else
					System.err.println("isCheck is not 0 or 1.");				
				EncProgCommon.oos.flush();
				//System.out.println("check = " + check);
				
				BigInteger z_LBS_Enc = new BigInteger(EncProgCommon.ois.readObject().toString());
				//System.out.println("(" + z_LBS_Enc + ")");
				BigInteger z_LBS = mPaillier.Decryption(z_LBS_Enc);
				//System.out.println("(" + z_LBS + ")");				
				//System.out.println(EncX + "\t" + EncY);
				// z_LBS = 1 <=> x >= y, return No  [0]
				// z_LBS = 0 <=> x <  y, return Yes [1]
				BigInteger m_head = (z_LBS == BigInteger.ZERO)?
						(mPaillier.Encryption(BigInteger.ONE)):(Enc_ZERO);
				//System.out.println("m_head\t" + mPaillier.Decryption(m_head));
				EncProgCommon.oos.writeObject(m_head);
				EncProgCommon.oos.flush();							
			} catch(Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
}

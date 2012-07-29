package Protocol;

import java.math.BigInteger;

import Crypto.CryptosystemDGKClient;
import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;
import Utils.ClientState;

public class ComparisonProtocolOnClient extends ComparisonProtocol {	
	private CryptosystemPaillierClient mPaillier;
	private CryptosystemDGKClient mDGK;
	
	private static BigInteger EncP_ONE;
	
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
		EncP_ONE = mPaillier.Encryption(BigInteger.ONE);
	}
	
	public void run(){
		while(true) {		
			try {
				this.exit = (ClientState)EncProgCommon.ois.readObject();
				if(this.exit.equals(ClientState.CLIENT_EXIT)) {
					System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");
					break;
				}
				
				// d = dec.( [d] )								
				BigInteger d = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
				//System.out.println("d\t" + d);
				
				// d^ = d mod 2^L
				BigInteger d_head = d.mod(MAX);
				//System.out.println("d_head\t" + d_head);
				BigInteger d_head_Enc = mPaillier.Encryption(d_head);
				// send [d^] to Bob
				EncProgCommon.oos.writeObject(d_head_Enc);
				
				String d_bin = d_head.toString(2);
				//System.out.println(d_bin.toCharArray().length);
				
				// send [[ d_bin ]] = Enc.DGK( d_bin )
				for(int i=0; i<(L+1) - d_bin.length(); i++) {					
					EncProgCommon.oos.writeObject(Enc_ZERO);
				}
				for(int i=0; i<d_bin.length(); i++) {			
					//System.out.print(d_bin.charAt(i));					
					if(Character.toString(d_bin.charAt(i)).equals("0")) {
						EncProgCommon.oos.writeObject(Enc_ZERO);
					}
					else {
						EncProgCommon.oos.writeObject(mDGK.Encryption(BigInteger.ONE));
					}					
				}
				//System.out.println();
								
				// Check [[c_i * r_i]] = [[c_i]]^r_i one of then is zero				
				int isCheck = 0;				
				for(int i=0; i<L+1; i++) {
					BigInteger tmp = mDGK.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
					//System.out.println("bits = " + tmp);
					if(BigInteger.ZERO.equals(tmp)) {
						isCheck = 1;						
					}											
				}
				if(isCheck == 0) {
					// [[c_i]] has no zero, return [0] = 1					
					EncProgCommon.oos.writeObject(Enc_ZERO);	
				}
				else if(isCheck == 1) {
					// [[c_i]] has One zero, return [1]
					EncProgCommon.oos.writeObject(EncP_ONE);	
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
						(EncP_ONE):(Enc_ZERO);
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

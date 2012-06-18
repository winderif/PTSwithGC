package Protocol;

import java.math.BigInteger;
import Crypto.CryptosystemPaillierClient;
import Program.EncProgCommon;

public class ComparisonProtocolOnClient {	
	private CryptosystemPaillierClient mPaillier;
	private static final int L = 25;
	private BigInteger TwoPowL = (new BigInteger("2")).pow(L);
	private BigInteger Enc_ZERO;
	
	public ComparisonProtocolOnClient() {	
		this.mPaillier = null;
	}
	public ComparisonProtocolOnClient(CryptosystemPaillierClient p) {		
		this.mPaillier = p;				
		Enc_ZERO = BigInteger.ONE;
	}
	
	public void run(){
		while(true) {		
			try {
				//System.out.println("[C][START]\tReduce d mod 2^L");
				// d = dec.( [d] )			
				BigInteger d = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
				//System.out.println("d\t" + d);
				
				// d^ = d mod 2^L
				BigInteger d_head = d.mod(TwoPowL);
				//System.out.println("d mod\t" + d_head);
				BigInteger d_head_Enc = mPaillier.Encryption(d_head);
				// send [d^] to Bob
				EncProgCommon.oos.writeObject(d_head_Enc);
				
				String d_bin = Long.toBinaryString(d_head.longValue());
				//System.out.println(d_bin.toCharArray().length);
				for(int i=0; i<(L+1) - d_bin.length(); i++) {
					//mQueue.add(0);
					EncProgCommon.oos.writeObject(Enc_ZERO);
				}
				for(int i=0; i<d_bin.length(); i++) {			
					// send [[ d_bin ]]
					//System.out.print(d_bin.charAt(i));
					//mQueue.add(d_bin.charAt(i));
					EncProgCommon.oos.writeObject(mPaillier.Encryption(new BigInteger(Character.toString(d_bin.charAt(i)))));
				}			
				//mMode.setMode(Mode.CP_MASK_S);
				
				//while(!mMode.isCheckWhetherOneOfZero()) {;}
				//System.out.println("[C][START]\tCheck all bit of Mask.");
				
				BigInteger check = BigInteger.ONE;
				int isCheck = 0;				
				for(int i=0; i<L+1; i++) {
					BigInteger tmp = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString()));
					//System.out.println("bits = " + tmp);
					if(BigInteger.ZERO.equals(tmp)) {	
						isCheck = 1;
						break;
					}						
				}
				if(isCheck == 0) {
					check = BigInteger.ZERO;
					EncProgCommon.oos.writeObject(Enc_ZERO);	
				}
				else if(isCheck == 1) {
					EncProgCommon.oos.writeObject(mPaillier.Encryption(check));	
				}
				else
					System.err.println("isCheck is not 0 or 1.");
				
				EncProgCommon.oos.flush();
				//System.out.println("check = " + check);
				//mQueue.add(check);				
				//mMode.setMode(Mode.CP_GET_LAMBDA_S);
				
				//while(!mMode.isFindMinimumOfTwoOnClient()) {;}
				int z_LBS = mPaillier.Decryption(new BigInteger(EncProgCommon.ois.readObject().toString())).signum();
				//System.out.println("(" + z_LBS + ")");
				BigInteger EncX = new BigInteger(EncProgCommon.ois.readObject().toString());
				BigInteger EncY = new BigInteger(EncProgCommon.ois.readObject().toString());
				// z_LBS = 1 <=> x >= y
				// z_LBS = 0 <=> x <  y
				BigInteger m_head = (z_LBS == 1)?(EncY):(EncX);
				EncProgCommon.oos.writeObject(m_head);
				EncProgCommon.oos.flush();
				//mMode.setMode(Mode.CP_GET_MIN_S);

			} catch(Exception e) {
				System.out.println("[C][SUCCESS]\tFind Bset Matching for Encrypted Bipartile Graph.");
				break;
			}
		}
	}
	
	public static void main(String[] args) {
		
	}
}

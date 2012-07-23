package Blinding;

import java.io.IOException;
import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;

public class AdditiveBlindingProtocol extends AdditiveBlinding {
	
	protected BigInteger[] mEncBlindValues = null;
	
	public AdditiveBlindingProtocol(
			CryptosystemPaillierServer paillier,
			BigInteger[] EncData) {
		super(paillier, EncData);
	}
	
	protected void initialize() {
		this.mEncBlindValues = new BigInteger[this.NUM_DATA];
		for(int i=0; i<this.mEncBlindValues.length; i++) {
			// [x] = [w + r] = [w] * [r] 
			mEncBlindValues[i] = mEncData[i].multiply(mEncRandomValues[i])
											.mod(this.mPaillier.nsquare);
		}
	}
	
	protected void execute() {		
		sendEncBlindValues();
	}
	
	private void sendEncBlindValues() {
		try {
			EncProgCommon.oos.writeInt(this.NUM_DATA);
			
			for(int i=0; i<this.NUM_DATA; i++) {
				EncProgCommon.oos.writeObject(mEncBlindValues[i]);
			}						
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	public BigInteger getThirdTerm(BigInteger s3_c) {
		BigInteger sum_r_square = BigInteger.ZERO;
		BigInteger neg_TWO_r = BigInteger.ZERO;
		BigInteger Enc_sum_wr_neg_TWO = BigInteger.ONE;
	
		for(int i=0; i<this.NUM_DATA; i++) {
			// (r^2) = r * r
			sum_r_square = sum_r_square.add(mRandomValues[i].multiply(mRandomValues[i]));				
			// (-2)*r
			neg_TWO_r = (mRandomValues[i].shiftLeft(1)).negate();
			// [w]^(-2r) mod N
			Enc_sum_wr_neg_TWO = 
				Enc_sum_wr_neg_TWO.multiply(mEncData[i].modPow(neg_TWO_r, mPaillier.nsquare));			
		}		
		// -(r^2) = (-1) * (r^2)
		// [-(r^2)] = Enc.( -(r^2) )
		BigInteger Enc_neg_sum_r_square = mPaillier.Encryption(sum_r_square.negate());
		
		// [s3] = [s3_c] * [- Sum(r^2)] * multiply([w]^(-2r))
		return s3_c.multiply(Enc_neg_sum_r_square)
					.multiply(Enc_sum_wr_neg_TWO)
					.mod(mPaillier.nsquare);
	}
}

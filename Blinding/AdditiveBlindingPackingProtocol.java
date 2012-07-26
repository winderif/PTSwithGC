package Blinding;

import java.io.IOException;
import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;
import Utils.Print;

public class AdditiveBlindingPackingProtocol extends AdditiveBlinding 
											implements Packing {	
	
	private int K_BLIND = 16;
	private int K_REMAINING = 0;
	private int K_CIPHER = 0;
	
	private static BigInteger MAX;
	private static BigInteger SHIFT_BASE;
		
	private BigInteger[] mEncPackedValues = null;
	private BigInteger mEncS3 = BigInteger.ONE;
	
	public AdditiveBlindingPackingProtocol(
			CryptosystemPaillierServer paillier,
			BigInteger[] EncData) {
		super(paillier, EncData);				
	}
	
	protected void initialize() {		
		K_CIPHER = this.NUM_DATA / K_BLIND;		
		K_REMAINING = this.NUM_DATA - (K_BLIND * K_CIPHER);		
		
		if(K_REMAINING != 0) {
			mEncPackedValues = new BigInteger[K_CIPHER + 1];			
		}
		else {
			mEncPackedValues = new BigInteger[K_CIPHER];
		}		
		// MAX = 2^(L-1)
		MAX = BigInteger.ONE.shiftLeft(this.DATA_BIT - 1);	
		
		SHIFT_BASE = BigInteger.ONE.shiftLeft(this.RANDOM_BIT + this.DATA_BIT);
	}
	
	protected void execute() throws Exception {	
		EncProgCommon.oos.writeInt(K_CIPHER);
		
		for(int i=0; i<this.K_CIPHER; i++) {
			this.mEncPackedValues[i] = 
				packing(i * K_BLIND, K_BLIND);
			
			sendEncPackedValue(mEncPackedValues[i], K_BLIND);
			
			BigInteger s3_p = recvPackedValue();
			
			BigInteger s3K = getThirdTerm(s3_p, i * K_BLIND, K_BLIND);
			
			mEncS3 = mEncS3.multiply(s3K);
		}		
				
		if(K_REMAINING != 0) {
			EncProgCommon.oos.writeBoolean(true);
			
			this.mEncPackedValues[K_CIPHER] = 
				packing(K_CIPHER * K_BLIND, K_REMAINING);
			
			sendEncPackedValue(mEncPackedValues[K_CIPHER], K_REMAINING);
			
			BigInteger s3_p = recvPackedValue();
			
			BigInteger s3K = getThirdTerm(s3_p, K_CIPHER * K_BLIND, K_REMAINING);	
			
			mEncS3 = mEncS3.multiply(s3K);
		}
		else {
			EncProgCommon.oos.writeBoolean(false);
		}
	}
	
	public BigInteger packing(int start, int k_blind) {
		BigInteger EncDataShift = this.mEncData[start + (k_blind - 1)];
		BigInteger randomShift = MAX.add(this.mRandomValues[start + (k_blind - 1)]);
		/**
		 *  Horner's method
		 *  for K'-1 to 1 do
		 *  	[x] = [x]^shift_base * [w_j]
		 *  end	 
		 */		
		for(int i=(k_blind-1) - 1; i>=0; i--) {
			EncDataShift = (EncDataShift.modPow(SHIFT_BASE, mPaillier.nsquare))
										.multiply(this.mEncData[start + i]).mod(mPaillier.nsquare);
			
			randomShift = (randomShift.shiftLeft(this.RANDOM_BIT + this.DATA_BIT))
										.add(MAX.add(this.mRandomValues[start + i]));
		}			
		BigInteger EncRandomShift = mPaillier.Encryption(randomShift);		
		
		// get [x]
		BigInteger xEnc = EncDataShift.multiply(EncRandomShift).mod(mPaillier.nsquare);
				
		return xEnc;
	}
	
	private void sendEncPackedValue(BigInteger xEnc, int k_blind) {
		try {
			EncProgCommon.oos.writeInt(k_blind);
			
			EncProgCommon.oos.writeObject(xEnc);
			
			EncProgCommon.oos.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}		
	}
	
	private BigInteger recvPackedValue() {
		try {
			return new BigInteger(EncProgCommon.ois.readObject().toString());
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public BigInteger getThirdTerm(BigInteger s3_c, int start, int k_blind) {
		BigInteger sum_r_square = BigInteger.ZERO;
		BigInteger neg_TWO_r = BigInteger.ZERO;
		BigInteger Enc_sum_wr_neg_TWO = BigInteger.ONE;
	
		for(int i=0; i<k_blind; i++) {
			// (r^2) = r * r
			sum_r_square = 
				sum_r_square.add(mRandomValues[start + i].multiply(mRandomValues[start + i]));				
			// (-2)*r
			neg_TWO_r = (mRandomValues[start + i].shiftLeft(1)).negate();
			// [w]^(-2r) mod N
			Enc_sum_wr_neg_TWO = 
				Enc_sum_wr_neg_TWO.multiply(mEncData[start + i].modPow(neg_TWO_r, mPaillier.nsquare));			
		}		
		// -(r^2) = (-1) * (r^2)
		// [-(r^2)] = Enc.( -(r^2) )
		BigInteger Enc_neg_sum_r_square = mPaillier.Encryption(sum_r_square.negate());
		
		// [s3] = [s3_c] * [- Sum(r^2)] * multiply([w]^(-2r))
		return s3_c.multiply(Enc_neg_sum_r_square)
					.multiply(Enc_sum_wr_neg_TWO)
					.mod(mPaillier.nsquare);
	}

	public BigInteger getmEncS3() {
		return mEncS3.mod(mPaillier.nsquare);
	}	
}

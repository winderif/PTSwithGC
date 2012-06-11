package Score;

import java.math.BigInteger;
import Crypto.CryptosystemPaillierServer;
import Utils.AdditivelyBlindProtocol;
import Program.EncGCTaggingSystemCommon;

public class ComputingScoreServer extends ComputingScore {
	private CryptosystemPaillierServer mPaillier = null;	
	private BigInteger[] EncQueryHistogram = null;
	private double[] mDatabaseHistogram = null;	
	private BigInteger[] mDatabaseHistogram_Big = null;	
	
	BigInteger tmpScore = BigInteger.ONE;
	BigInteger tmpS1 = BigInteger.ZERO;
	BigInteger tmpS2 = BigInteger.ONE;
	BigInteger tmpPow = BigInteger.ONE;	
	BigInteger s1 = BigInteger.ZERO;
	BigInteger s2 = BigInteger.ONE;
	BigInteger s3 = BigInteger.ZERO;
	BigInteger s3_c = BigInteger.ZERO;	
	
	public ComputingScoreServer(CryptosystemPaillierServer arg0, BigInteger[] arg1, double[] arg2) {
		this.mPaillier = arg0;
		this.EncQueryHistogram = arg1;		
		this.mDatabaseHistogram = arg2;			
	}	
	
	public void run() throws Exception {
		super.run();
	}
	
	protected void init() throws Exception {
		mDatabaseHistogram_Big = EncGCTaggingSystemCommon.DoubleToBigInteger1DArray(mDatabaseHistogram);		
	}
	
	protected void execute() throws Exception {
		/*** S1 ***/
		for(int i=0; i<BIN_HISTO; i++) {	
			// w*w
			tmpS1 = tmpS1.add(mDatabaseHistogram_Big[i].multiply(mDatabaseHistogram_Big[i]));
		}
		
		s1 = mPaillier.Encryption(tmpS1);
		//System.out.println("s1:\t" + mPaillier.Decryption(s1));
		
		/*** S2 ***/
		for(int i=0; i<BIN_HISTO; i++) {
			// (-2)*w
			tmpPow = mDatabaseHistogram_Big[i].multiply(new BigInteger("2"));						
			//System.out.println("tmpPow:\t" + tmpPow + " ");
			// [w_bar]^((-2)*w) and w1*w2
			BigInteger tmpMul = EncQueryHistogram[i].modPow(tmpPow, mPaillier.nsquare);			
			//System.out.println("tmpMul:\t" + mPaillier.Decryption(tmpMul));			
			tmpS2 = tmpS2.multiply(tmpMul);
			//System.out.println("tmpS2:\t" + mPaillier.Decryption(tmpS2));
		}
		// (w1*w2*...wK) mod N
		//System.out.println("s2:\t" + mPaillier.Decryption(tmpS2));
		s2 = tmpS2.modPow(BigInteger.ONE.negate(), mPaillier.nsquare);
		//System.out.println("s2_neg:\t" + mPaillier.Decryption(s2));
		
		AdditivelyBlindProtocol r = new AdditivelyBlindProtocol(mPaillier, EncQueryHistogram);
		//System.out.print("[S1]" + this.mMode.getMode());
		sendDistanceAdditivelyBlindDatas(r.getAdditivelyBlindNumbers());
		//System.out.print("[S2]" + this.mMode.getMode());
		//while(!this.mMode.isComputingScoreOnServer()) {;}
		
		// recv [S3']		
		s3_c = new BigInteger(EncGCTaggingSystemCommon.ois.readObject().toString());
		//System.out.println("s3_c = " + mPaillier.Decryption(s3_c));
		s3 = r.getSumOfThirdPart(s3_c);
		//System.out.println("s3 = " + mPaillier.Decryption(s3));
		
		tmpScore = tmpScore.multiply(s1).multiply(s2).multiply(s3).mod(mPaillier.nsquare);
		//System.out.println("tmp = " + mPaillier.Decryption(tmpScore));
	}
	
	private void sendDistanceAdditivelyBlindDatas(BigInteger[] x) throws Exception {
 		System.out.println("\t[S][STRAT]\tsend AB datas of distance.");
 		//EncTaggingSystemCommon.oos.reset();
 		for(int i=0; i<BIN_HISTO; i++) { 		
 			//System.out.println(x[i]); 			
 			EncGCTaggingSystemCommon.oos.writeObject(x[i]); 			
 		}
 		EncGCTaggingSystemCommon.oos.flush(); 		
 	}
	
	public BigInteger getScore() {
		return this.tmpScore;
	}
}

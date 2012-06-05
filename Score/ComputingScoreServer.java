package Score;

import java.math.BigInteger;
import Crypto.CryptosystemPaillierServer;
import Utils.AdditivelyBlindProtocol;
import Program.EncTaggingSystemCommon;

public class ComputingScoreServer extends ComputingScore {
	private CryptosystemPaillierServer mPaillier = null;	
	private BigInteger[] EncQueryHistogram = null;
	private double[] mDatabaseHistogram = null;
	BigInteger[] tmpTagHistogram = null;	
	
	BigInteger tmpScore = BigInteger.ONE;
	double tmpS1 = 0.0;
	BigInteger tmpS2 = BigInteger.ONE;
	BigInteger tmpPow = BigInteger.ONE;	
	BigInteger s1 = BigInteger.ZERO;
	BigInteger s2 = BigInteger.ZERO;
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
		tmpTagHistogram = EncTaggingSystemCommon.DoubleToBigInteger1DArray(mDatabaseHistogram);
	}
	
	protected void execute() throws Exception {
		/*** S1 ***/
		for(int i=0; i<BIN_HISTO; i++) {	
			// w*w
			tmpS1 += this.mDatabaseHistogram[i]*this.mDatabaseHistogram[i];
		}
		s1 = mPaillier.Encryption(EncTaggingSystemCommon.DoubleToBigInteger(tmpS1));
		
		/*** S2 ***/
		for(int i=0; i<BIN_HISTO; i++) {
			// (-2)*w
			tmpPow = tmpTagHistogram[i].multiply(new BigInteger("-2"));				
			// [w_bar]^((-2)*w) and w1*w2
			tmpS2 = tmpS2.multiply(EncQueryHistogram[i].modPow(tmpPow, mPaillier.nsquare));
		}
		// (w1*w2*...wK) mod N
		s2 = tmpS2.mod(mPaillier.nsquare);		
		
		AdditivelyBlindProtocol r = new AdditivelyBlindProtocol(mPaillier, EncQueryHistogram);
		//System.out.print("[S1]" + this.mMode.getMode());
		sendDistanceAdditivelyBlindDatas(r.getAdditivelyBlindNumbers());
		//System.out.print("[S2]" + this.mMode.getMode());
		//while(!this.mMode.isComputingScoreOnServer()) {;}
		
		// recv [S3']		
		s3_c = new BigInteger(EncTaggingSystemCommon.ois.readObject().toString());
		s3 = r.getSumOfThirdPart(s3_c);
		//System.out.println("s3 = " + s3);
		
		tmpScore = tmpScore.multiply(s1).multiply(s2).multiply(s3).mod(mPaillier.nsquare);
		//System.out.println("tmp = " + tmpScore);
	}
	
	private void sendDistanceAdditivelyBlindDatas(BigInteger[] x) throws Exception {
 		System.out.println("\t[S][STRAT]\tsend AB datas of distance.");
 		//EncTaggingSystemCommon.oos.reset();
 		for(int i=0; i<BIN_HISTO; i++) { 		
 			//System.out.println(x[i]); 			
 			EncTaggingSystemCommon.oos.writeObject(x[i]); 			
 		}
 		EncTaggingSystemCommon.oos.flush(); 		
 	}
	
	public BigInteger getScore() {
		return this.tmpScore;
	}
	
	public static void main(String[] args) {
	
	}
}

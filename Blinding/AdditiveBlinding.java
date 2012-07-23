package Blinding;

import java.math.BigInteger;
import java.util.Random;
import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;

public abstract class AdditiveBlinding {
	protected static final int RANDOM_BIT = 10;
	protected static final int DATA_BIT = 10;
	protected final int NUM_DATA;
	
	protected CryptosystemPaillierServer mPaillier = null;
	
	protected BigInteger[] mEncData = null;
	protected BigInteger[] mRandomValues = null;
	protected BigInteger[] mEncRandomValues = null;
	
	public AdditiveBlinding(
			CryptosystemPaillierServer paillier,
			BigInteger[] EncData) {
		this.mPaillier = paillier;
		this.mEncData = EncData;
		this.NUM_DATA = EncData.length;
	}
	
	public void run() throws Exception {
		init();
		
		execute();
	}
	
	protected void init() {
		createRandomValues();
		
		encryptRandomValues();
		
		initialize();	
	}
	
	abstract protected void initialize();
	
	abstract protected void execute() throws Exception;
	
	protected void createRandomValues() {
		this.mRandomValues = new BigInteger[this.NUM_DATA];
		for(int i=0; i<this.mRandomValues.length; i++) {
			this.mRandomValues[i] = 
				new BigInteger(RANDOM_BIT + DATA_BIT, new Random());
		}
	}
	
	protected void encryptRandomValues() {
		this.mEncRandomValues = 
			EncProgCommon.encryption(this.mPaillier, this.mRandomValues);			
	}
}

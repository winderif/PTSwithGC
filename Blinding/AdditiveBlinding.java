package Blinding;

import java.math.BigInteger;
import java.util.Random;
import Crypto.CryptosystemPaillierServer;

public abstract class AdditiveBlinding {
	// L-bit value
	protected static final int DATA_BIT = 15;
	// Statistical security parameter SIGMA = 15
	protected static final int SECURITY_BIT = 15;
	
	protected final int NUM_DATA;
	
	protected CryptosystemPaillierServer mPaillier = null;
	
	protected BigInteger[] mEncData = null;
	protected BigInteger[] mRandomValues = null;	
	
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
		
		initialize();	
	}
	
	abstract protected void initialize();
	
	abstract protected void execute() throws Exception;
	
	protected void createRandomValues() {
		this.mRandomValues = new BigInteger[this.NUM_DATA];
		for(int i=0; i<this.mRandomValues.length; i++) {
			this.mRandomValues[i] = 
				new BigInteger(SECURITY_BIT + DATA_BIT, new Random());
		}
	}	
}

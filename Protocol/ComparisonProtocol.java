package Protocol;

import java.math.BigInteger;

public abstract class ComparisonProtocol {
	protected static final int L = 15;
	protected static final BigInteger Enc_ZERO = BigInteger.ONE;
	protected static final BigInteger TwoPowL = (new BigInteger("2")).pow(L);	
}

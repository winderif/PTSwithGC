package Protocol;

import java.math.BigInteger;

public abstract class ComparisonProtocol {
	protected static final int L = 30;
	protected static final BigInteger Enc_ZERO = BigInteger.ONE;
	protected static final BigInteger MAX = BigInteger.ONE.shiftLeft(L);
}

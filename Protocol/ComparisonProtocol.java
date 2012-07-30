package Protocol;

import java.math.BigInteger;

public abstract class ComparisonProtocol {
	// L-bit value
	protected static final int L = 30;	
	// Statistical correctness parameter KAPA = 40
	protected static final int CORRECTNESS_BIT = 40;
	// Statistical security parameter SIGMA = 80
	protected static final int SECURITY_BIT = 80;
	// Enc.(0) = [0] = 1
	protected static final BigInteger Enc_ZERO = BigInteger.ONE;
	// 2^L
	protected static final BigInteger MAX = BigInteger.ONE.shiftLeft(L);
}

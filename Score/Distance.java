package Score;
import java.util.Map;
import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;

public abstract class Distance {
	public abstract double evaluate(double[] q, double[] d);
	public abstract double evaluate(Map<Integer, Double> q, Map<Integer, Double> d);
	public abstract BigInteger evaluate(
			CryptosystemPaillierServer mPaillier, 
			Map<Integer, BigInteger> q, 
			Map<Integer, Double> d) throws Exception;
}

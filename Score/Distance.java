package Score;
import java.util.LinkedHashMap;
import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;

public abstract class Distance {
	public abstract double evaluate(double[] q, double[] d);
	public abstract double evaluate(LinkedHashMap<Integer, Double> q, LinkedHashMap<Integer, Double> d);
	public abstract BigInteger evaluate(
			CryptosystemPaillierServer mPaillier, 
			LinkedHashMap<Integer, BigInteger> q, 
			LinkedHashMap<Integer, Double> d) throws Exception;
}

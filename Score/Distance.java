package Score;
import java.util.LinkedHashMap;

public abstract class Distance {
	public abstract double evaluate(double[] q, double[] d);
	public abstract double evaluate(LinkedHashMap<Integer, Double> q, LinkedHashMap<Integer, Double> d);
}

package Feature;

public abstract class Feature {
	public void run() {
		init();
		
		execution();
	}
	
	protected void init() {
		initialize();
	}
	
	abstract protected void initialize();
	
	protected void execution() {		
		extractFeature();
	}
	
	abstract protected void extractFeature();		
}

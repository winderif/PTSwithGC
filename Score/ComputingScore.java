package Score;

public abstract class ComputingScore {
	//protected static final int BIN_HISTO = 16;
	protected static final int BIN_HISTO = 10000;
	protected static final int RANDOM_BIT = 10;
	protected static final int DATA_BIT = 10;
		
	public void run() throws Exception {
		init();
		
		execute();
	}
	
	protected abstract void init() throws Exception;
	
	protected abstract void execute() throws Exception;		
}

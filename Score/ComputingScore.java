package Score;

public abstract class ComputingScore {
	protected static final int BIN_HISTO = 16;
		
	public void run() throws Exception {
		init();
		
		execute();
	}
	
	protected abstract void init() throws Exception;
	
	protected abstract void execute() throws Exception;		
}

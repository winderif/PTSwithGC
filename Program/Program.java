package Program;

public abstract class Program {

	public void run() throws Exception {
		init();
	}
	
    protected void init() throws Exception {
    	initialize();
    }
    
    abstract protected void initialize() throws Exception;
    
    protected void execute() throws Exception {
    	execBuildBipartiteGraph();
    	
    	execFindBestMatching();
    }
    
    abstract protected void execBuildBipartiteGraph() throws Exception;
    
    abstract protected void execFindBestMatching() throws Exception;
}

package Program;

public abstract class Program {
	protected static int iterCount;
	protected int iter;

	public void run() throws Exception {
		init();
		
		for(int i=0; i<iterCount; i++) {
			iter = i;
			execute();
		}
	}
	
    protected void init() throws Exception {
    	initialize();    	    	
    }
    
    abstract protected void initialize() throws Exception;
    
    protected void execute() throws Exception {
    	execQueryTransfer();
    	
    	//execFindCandidateTagClusters();
    	
    	execBuildBipartiteGraph();
    	
    	execFindBestMatching();
    	
    	execResultTransfer();
    }    
    
    abstract protected void execQueryTransfer() throws Exception;
    
    abstract protected void execFindCandidateTagClusters() throws Exception;
    
    abstract protected void execBuildBipartiteGraph() throws Exception;
    
    abstract protected void execFindBestMatching() throws Exception;
    
    abstract protected void execResultTransfer() throws Exception;
}

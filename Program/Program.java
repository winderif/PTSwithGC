package Program;

public abstract class Program {

	public void run() throws Exception {
		init();
		
		execute();
	}
	
    protected void init() throws Exception {
    	initialize();    	    	
    }
    
    abstract protected void initialize() throws Exception;
    
    protected void execute() throws Exception {
    	execQueryTransfer();
    	
    	execFindCandidateTagClusters();
    	
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

// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

/*
 * Fig. 1 of [KSS09]
 */
public class ADD_2KL_KL extends CompositeCircuit {
    private final int L;
    private final int K;

    public ADD_2KL_KL(int l, int k) {
    	super(2*l*k, l*k, k, "ADD_" + (2*l) + "_" + (l+1));
    	
    	L = l;
    	K = k;
    }

    protected void createSubCircuits() throws Exception {
    	for (int i = 0; i < K; i++) 
    	    subCircuits[i] = new ADD_2L_L(L);

    	super.createSubCircuits();
    }

    protected void connectWires() {
    	for(int i=0; i < K; i++) {
    		for(int j=0; j < L; j++) {
    			inputWires[j + L*i + L*K].connectTo(subCircuits[i].inputWires, X(j));
        	    inputWires[j + L*i      ].connectTo(subCircuits[i].inputWires, Y(j));	
    		}
    	}    	
    }

    protected void defineOutputWires() {
    	for(int i=0; i < K; i++) {
    		for(int j=0; j < L; j++)
    			outputWires[j + L*i] = subCircuits[i].outputWires[j];
    	}    	    
    }	    

    private int X(int i) {
    	return i + L;
    }

    private int Y(int i) {
    	return i;
    }
}
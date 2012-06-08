// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.YaoGC;

public class Minimum_KL_L extends CompositeCircuit {
    private final int L;
    private final int K;

    public final static int  GT = 0;
    public final static int MUX = 1;

    public Minimum_KL_L(int l, int k) {
    	super(l*k, l, k-1, "Minimum_" + l*k + "_" + l);
    	L = l;
    	K = k;
    }

    /*
     * Connect xWires[xStartPos...xStartPos+L] to the wires representing bits of X;
     * yWires[yStartPos...yStartPos+L] to the wires representing bits of Y;
     */
    public void connectWiresToXY(Wire[] xWires, int xStartPos, Wire[] yWires, int yStartPos) throws Exception {
    	if (xStartPos + L > xWires.length || yStartPos + L > yWires.length)
    	    throw new Exception("Unmatched number of wires.");
    	
    	for (int i = 0; i < L; i++) {
    	    xWires[xStartPos+i].connectTo(inputWires, X(i));
    	    yWires[yStartPos+i].connectTo(inputWires, Y(i));
    	}
    }

    protected void createSubCircuits() throws Exception {
    	for(int i=0; i<K-1; i++) {
    		subCircuits[i] = new MIN_2L_L(L);
    	}    	

		super.createSubCircuits();
    }

    protected void connectWires() throws Exception {    	
    	for(int i=0; i < L; i++) {
    	    inputWires[i+L].connectTo(subCircuits[0].inputWires, X(i));
    	    inputWires[i  ].connectTo(subCircuits[0].inputWires, Y(i));
    	}    	
    	for (int i=1; i < K-1; i++) {
    		for (int j=0; j < L; j++) {
    			inputWires[j + (i+1)*L].connectTo(subCircuits[i].inputWires, X(j));
    			subCircuits[i-1].outputWires[j].connectTo(subCircuits[i].inputWires, Y(j));        	            	        			
    		}    		    	    
    	}    	
    }

    protected void defineOutputWires() {
    	for (int i=0; i < L; i++)
    		outputWires[i] = subCircuits[K-2].outputWires[i];
    }

    private int X(int i) {
    	return i+L;
    }

    private int Y(int i) {
    	return i;
    }
}

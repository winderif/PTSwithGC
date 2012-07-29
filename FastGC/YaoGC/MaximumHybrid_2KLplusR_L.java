// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.YaoGC;

public class MaximumHybrid_2KLplusR_L extends CompositeCircuit {
    private final int L;
    private final int K;
    private final int C;
    private final int R;

    public final static int SUB = 0;
    public final static int MIN = 1;
    public final static int ADD = 2;

    public MaximumHybrid_2KLplusR_L(int l, int k, int r) {
    	super(2*l*k + r, r, 3, "Maximum_" + l*k + "_" + l);
    	L = l;
    	K = k;
    	C = 2*l*k;
    	R = r;
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
    	subCircuits[SUB] = new SUB_2KL_KL(L, K);
    	subCircuits[MIN] = new Maximum_KL_L(L, K);
    	subCircuits[ADD] = new ADD_2L_L(R);
    	    	
		super.createSubCircuits();
    }

    protected void connectWires() throws Exception {
    	// connect SUB circuit
    	for(int i=0; i < K; i++) {
    		for(int j=0; j < L; j++) {
    			inputWires[j + L*i + L*K].connectTo(subCircuits[SUB].inputWires, j + L*i + L*K);
    			inputWires[j + L*i      ].connectTo(subCircuits[SUB].inputWires, j + L*i);
    		}
    	}
    	// connect Minimum circuit
    	for(int i=0; i < K; i++) {
    		for (int j=0; j < L; j++) {
    			subCircuits[SUB].outputWires[j + L*i].connectTo(subCircuits[MIN].inputWires, j + L*i);
    		}
    	}
    	// connetc ADD circuit
    	for (int j=0; j < R; j++) {
    		inputWires[j + C].connectTo(subCircuits[ADD].inputWires, j + R);
			
		}
    	for (int j=0; j < L; j++) {
    		subCircuits[MIN].outputWires[j].connectTo(subCircuits[ADD].inputWires, j);
    	}    	
    }

    protected void defineOutputWires() {    	
    	for (int i=0; i < R; i++) {
			outputWires[i] = subCircuits[ADD].outputWires[i];
		}    	
    }
    
    protected void fixInternalWires() {    	
    	for(int j=L; j < R; j++) {
    		Wire internalWire = subCircuits[ADD].inputWires[j];
    		internalWire.fixWire(0);
    	}    	    
    }

    private int X(int i) {
    	return i;
    }

    private int Y(int i) {
    	return i;
    }
}

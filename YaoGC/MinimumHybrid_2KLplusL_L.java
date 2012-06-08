// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package YaoGC;

public class MinimumHybrid_2KLplusL_L extends CompositeCircuit {
    private final int L;
    private final int K;
    private final int C;

    public final static int SUB = 0;
    public final static int MIN = 1;
    public final static int ADD = 2;

    public MinimumHybrid_2KLplusL_L(int l, int k) {
    	super(2*l*k + l, l, 3, "Minimum_" + l*k + "_" + l);
    	L = l;
    	K = k;
    	C = 2*l*k;
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
    	subCircuits[MIN] = new Minimum_KL_L(L, K);
    	subCircuits[ADD] = new ADD_2L_L(L);
    	    	
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
    	for (int j=0; j < L; j++) {
    		inputWires[j + C].connectTo(subCircuits[ADD].inputWires, j + L);
			subCircuits[MIN].outputWires[j].connectTo(subCircuits[ADD].inputWires, j);
		}
    }

    protected void defineOutputWires() {    	
    	for (int i=0; i < L; i++) {
			outputWires[i] = subCircuits[ADD].outputWires[i];
		}    	
    }

    private int X(int i) {
    	return i;
    }

    private int Y(int i) {
    	return i;
    }
}
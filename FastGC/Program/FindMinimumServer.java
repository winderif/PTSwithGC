// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.Program;

import java.math.*;
import java.util.*;

import FastGC.YaoGC.*;
import FastGC.Utils.*;

public class FindMinimumServer extends ProgServer {
	private BigInteger output;
	
    private BigInteger sBits;

    private State outputState;

    private BigInteger[][] sBitslps, cBitslps;
    
    private int cInputLen;
    private int sInputLen;

    private static final Random rnd = new Random();

    public FindMinimumServer(BigInteger bv, 
    		int bitLength, int number, int type, int bitOutput) {
    	sBits = bv;
    	FindMinimumCommon.bitVecLen = bitLength;
    	FindMinimumCommon.valueNum = number;    	
    	FindMinimumCommon.circuitType = type;
    	FindMinimumCommon.bitOutputRandom = bitOutput;
    	cInputLen = bitLength*number + bitOutput;
    	sInputLen = bitLength*number;
    }

    protected void init() throws Exception {
    	FindMinimumCommon.oos.writeInt(FindMinimumCommon.bitVecLen);
    	FindMinimumCommon.oos.writeInt(FindMinimumCommon.valueNum);    	
    	FindMinimumCommon.oos.writeInt(FindMinimumCommon.circuitType);
    	FindMinimumCommon.oos.writeInt(FindMinimumCommon.bitOutputRandom);
    	FindMinimumCommon.oos.flush();

    	FindMinimumCommon.initCircuits();

    	generateLabelPairs();	
    	super.init();
    }

    private void generateLabelPairs() {
    	//System.out.println("generateLabelPairs()");
    	sBitslps = new BigInteger[sInputLen][2];
    	cBitslps = new BigInteger[cInputLen][2];
    	BigInteger glb0 = null;
	    BigInteger glb1 = null;
    	    	
    	for (int i = 0; i < sInputLen; i++) {
    	    glb0 = new BigInteger(Wire.labelBitLength, rnd);
    	    glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
    	    sBitslps[i][0] = glb0;
    	    sBitslps[i][1] = glb1;
    	}
    	for (int i = 0; i < cInputLen; i++) {    	    
    	    glb0 = new BigInteger(Wire.labelBitLength, rnd);
    	    glb1 = glb0.xor(Wire.R.shiftLeft(1).setBit(0));
    	    cBitslps[i][0] = glb0;
    	    cBitslps[i][1] = glb1;
    	}       	
    }

    protected void execTransfer() throws Exception {
    	for (int i = 0; i < sInputLen; i++) {
    	    int idx = sBits.testBit(i) ? 1 : 0;
    	    //System.out.print(idx);

    	    int bytelength = (Wire.labelBitLength-1)/8 + 1;
    	    Utils.writeBigInteger(sBitslps[i][idx], bytelength, FindMinimumCommon.oos);
    	}
    	//System.out.println();
    	FindMinimumCommon.oos.flush();
    	//StopWatch.taskTimeStamp("sending labels for selfs inputs");

    	snder.execProtocol(cBitslps);
    	//StopWatch.taskTimeStamp("sending labels for peers inputs");
    }

    protected void execCircuit() throws Exception {
    	BigInteger[] sBitslbs = new BigInteger[sInputLen];
    	BigInteger[] cBitslbs = new BigInteger[cInputLen];

    	for (int i = 0; i < sBitslps.length; i++)
    		sBitslbs[i] = sBitslps[i][0];

    	for (int i = 0; i < cBitslps.length; i++)
    		cBitslbs[i] = cBitslps[i][0];

    	outputState = FindMinimumCommon.execCircuit(sBitslbs, cBitslbs);
    }

    protected void interpretResult() throws Exception {
    	BigInteger[] outLabels = (BigInteger[]) FindMinimumCommon.ois.readObject();

    	output = BigInteger.ZERO;
    	for (int i = 0; i < outLabels.length; i++) {
    		if (outputState.wires[i].value != Wire.UNKNOWN_SIG) {
    			if (outputState.wires[i].value == 1)
    				output = output.setBit(i);
    			continue;
    		}
    		else if (outLabels[i].equals(outputState.wires[i].invd ? 
					 outputState.wires[i].lbl :
					 outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)))) {
    			output = output.setBit(i);
    		}
    		else if (!outLabels[i].equals(outputState.wires[i].invd ? 
					  outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) :
					  outputState.wires[i].lbl)) {
    			throw new Exception("Bad label encountered: i = " + i + "\t" +
				    outLabels[i] + " != (" + 
				    outputState.wires[i].lbl + ", " +
				    outputState.wires[i].lbl.xor(Wire.R.shiftLeft(1).setBit(0)) + ")");
	    	}
    	}
	
    	//System.out.println("output (pp): " + output);    	    	    
    	//StopWatch.taskTimeStamp("output labels received and interpreted");
    }

    protected void verify_result() throws Exception {
    }
    
    public BigInteger getOutput() {
    	return this.output;
    }
}
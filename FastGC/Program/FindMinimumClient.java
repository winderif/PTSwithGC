// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.Program;

import java.math.*;

import FastGC.YaoGC.*;
import FastGC.Utils.*;

public class FindMinimumClient extends ProgClient {
    private BigInteger cBits;
    private BigInteger[] sBitslbs, cBitslbs;

    private State outputState;
    
    private int sInputLen;
    private int cInputLen;

    public FindMinimumClient(BigInteger bv, int bitLength, int number, int bitOutput) {
    	cBits = bv;
    	FindMinimumCommon.bitVecLen = bitLength;
    	FindMinimumCommon.valueNum = number;    	
    	FindMinimumCommon.bitOutputRandom = bitOutput;
    }

    protected void init() throws Exception {
    	FindMinimumCommon.bitVecLen = FindMinimumCommon.ois.readInt();
    	FindMinimumCommon.valueNum = FindMinimumCommon.ois.readInt();    	
    	FindMinimumCommon.circuitType = FindMinimumCommon.ois.readInt();
    	FindMinimumCommon.bitOutputRandom = FindMinimumCommon.ois.readInt();
    	FindMinimumCommon.initCircuits();

    	sInputLen = FindMinimumCommon.bitVecLen * FindMinimumCommon.valueNum;
    	cInputLen = FindMinimumCommon.bitVecLen * FindMinimumCommon.valueNum 
    				+ FindMinimumCommon.bitOutputRandom;
    	
    	// OT initialize
    	otNumOfPairs = cInputLen;

    	super.init();
    }

    protected void execTransfer() throws Exception {
    	sBitslbs = new BigInteger[sInputLen];

    	for (int i = 0; i < sInputLen; i++) {
    		int bytelength = (Wire.labelBitLength-1)/8 + 1;
    		sBitslbs[i]   = Utils.readBigInteger(bytelength, FindMinimumCommon.ois);
    		//System.out.println(sBitslbs[i]);
    	}    	
    	//StopWatch.taskTimeStamp("receiving labels for peer's inputs");

    	cBitslbs = new BigInteger[cInputLen];
    	rcver.execProtocol(cBits);
    	cBitslbs = rcver.getData();
		//StopWatch.taskTimeStamp("receiving labels for self's inputs");
    }

    protected void execCircuit() throws Exception {
    	outputState = FindMinimumCommon.execCircuit(sBitslbs, cBitslbs);
    }


    protected void interpretResult() throws Exception {
    	FindMinimumCommon.oos.writeObject(outputState.toLabels());
    	FindMinimumCommon.oos.flush();
    }

    protected void verify_result() throws Exception {
    }
}
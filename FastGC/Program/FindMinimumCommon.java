// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.Program;

import java.math.*;

import FastGC.YaoGC.*;

class FindMinimumCommon extends ProgCommon {
	private final static int MIN = 0;
	private final static int MAX = 1;
    static int bitVecLen;
    static int valueNum;
    static int RandomValueNum;
    // 0 is Minimum, 1 is Maximum
    static int circuitType;

    static int bitLength(int x) {
    	return BigInteger.valueOf(x).bitLength();
    }

    protected static void initCircuits() {
    	ccs = new Circuit[1];
    	if(circuitType == MIN)
    		ccs[0] = new MinimumHybrid_2KLplusL_L(bitVecLen, valueNum);
    	else if(circuitType == MAX) 
    		ccs[0] = new MaximumHybrid_2KLplusL_L(bitVecLen, valueNum);
    	else {
    		System.out.println("The circuit type is error. (Not 0 or 1)");
    		ccs[0] = null;
    	}
    }

    public static State execCircuit(BigInteger[] slbs, BigInteger[] clbs) throws Exception {
    	BigInteger[] lbs = new BigInteger[2*valueNum*bitVecLen + RandomValueNum*bitVecLen];
    	System.arraycopy(slbs, 0, lbs, 0, bitVecLen*valueNum);
    	System.arraycopy(clbs, 0, lbs, bitVecLen*valueNum, bitVecLen*(valueNum + RandomValueNum));
    	State in = State.fromLabels(lbs);

    	State out = ccs[0].startExecuting(in);
    	
    	//StopWatch.taskTimeStamp("circuit garbling");

    	return out;
    }       
}
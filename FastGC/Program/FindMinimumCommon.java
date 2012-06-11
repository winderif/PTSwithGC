// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package FastGC.Program;

import java.math.*;

import FastGC.Utils.*;
import FastGC.YaoGC.*;

class FindMinimumCommon extends ProgCommon {		
    static int bitVecLen;
    static int valueNum;
    static int RandomValueNum; 

    static int bitLength(int x) {
    	return BigInteger.valueOf(x).bitLength();
    }

    protected static void initCircuits() {
    	ccs = new Circuit[1];
    	ccs[0] = new MinimumHybrid_2KLplusL_L(bitVecLen, valueNum);
    }

    public static State execCircuit(BigInteger[] slbs, BigInteger[] clbs) throws Exception {
    	BigInteger[] lbs = new BigInteger[2*valueNum*bitVecLen + RandomValueNum*bitVecLen];
    	System.arraycopy(slbs, 0, lbs, 0, bitVecLen*valueNum);
    	System.arraycopy(clbs, 0, lbs, bitVecLen*valueNum, bitVecLen*(valueNum + RandomValueNum));
    	State in = State.fromLabels(lbs);

    	State out = ccs[0].startExecuting(in);
    	
    	StopWatch.taskTimeStamp("circuit garbling");

    	return out;
    }       
}
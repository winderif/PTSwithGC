package Matching;

import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;
import Protocol.GCComparisonServer;

public class GCbasedHungarianAlgorithm extends FastEncHungarianAlgorithm {
	private GCComparisonServer gcc_s;
	
	public GCbasedHungarianAlgorithm(CryptosystemPaillierServer paillier) {
		super(paillier);
		this.gcc_s = new GCComparisonServer(paillier);		
	}
	
	protected BigInteger findEncLargest(BigInteger[][] array) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.	
		System.out.println("\t[START]\tfindEncLargest()");
		int length = array.length * array[0].length;
		BigInteger[] tmp1DArray = new BigInteger[length];	
		BigInteger tmpMax;
		
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[i].length; j++) {
				tmp1DArray[i*array[i].length + j] = array[i][j];
			}
		}
		
		// type = 1, is Max
		tmpMax = gcc_s.findMinimumOfTwoEncValues(tmp1DArray, 1);
			
		return tmpMax;
	}
	
	protected BigInteger findEncSmallest(
			BigInteger[][] cost, int[] rowCover, 
			int[] colCover, BigInteger maxCost) throws Exception {
		System.out.println("\t[START]\tfindEncSmallest()");
		BigInteger minval = maxCost;
		BigInteger tmpMin = BigInteger.ONE;
		BigInteger[] tmp1DArray = new BigInteger[cost.length * cost[0].length];
		int index = 0;

		for(int i=0; i<cost.length; i++) {
			for(int j=0; j<cost[0].length; j++) {
				if((rowCover[i]==0) && (colCover[j]==0)) {
					tmp1DArray[index++] = cost[i][j];
				}
			}
		}
		
		BigInteger[] min_Array = new BigInteger[index];
		for(int i=0; i<min_Array.length; i++) {
			min_Array[i] = tmp1DArray[i];
		}
		
		// type = 0, Min
		tmpMin = gcc_s.findMinimumOfTwoEncValues(min_Array, 0);
		if(findMinimum(tmpMin, minval).equals(tmpMin))
			return tmpMin;
		else
			return minval;
	}
	protected BigInteger findMinimum(BigInteger a, BigInteger b) throws Exception {
		// type = 0, Min
		EncProgCommon.oos.writeObject(0);  
		if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{a, b}, 0).equals(a)) {
			return a;
		}
		else {
			return b;	
		}
	}
}

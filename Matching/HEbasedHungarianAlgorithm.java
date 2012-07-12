package Matching;

import java.math.BigInteger;

import Crypto.CryptosystemPaillierServer;
import Program.EncProgCommon;
import Protocol.ComparisonProtocolOnServer;

public class HEbasedHungarianAlgorithm extends FastEncHungarianAlgorithm {
	private ComparisonProtocolOnServer cp_s;
	
	public HEbasedHungarianAlgorithm(CryptosystemPaillierServer paillier) {
		super(paillier);
		cp_s = new ComparisonProtocolOnServer(paillier);
	}
	
	protected BigInteger findEncLargest(BigInteger[][] array) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.	
		System.out.println("\t[START]\tfindEncLargest()");
		int length = array.length * array[0].length;
		BigInteger[] tmp1DArray = new BigInteger[length];	
		BigInteger[] tmpMax = new BigInteger[length];
		
		for (int i=0; i<array.length; i++) {
			for (int j=0; j<array[i].length; j++) {
				tmp1DArray[i*array[0].length + j] = array[i][j];
			}
		}
		
		int iter = 1;
		/*** Iteration stopping condition ***/
		while(length != 1) {
			System.out.print("iter (" + iter++ + ")\t");
			/*** Compare D(2k) and D(2k+1) ***/
			for(int j=0; j<length/2; j++) {
				System.out.print(j + " ");				
				if(findMinimum(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2])) {
				//if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{tmp1DArray[j*2], tmp1DArray[j*2+1]}, 0).equals(tmp1DArray[j*2])) {
					tmpMax[j] = tmp1DArray[j*2+1];
					tmpMax[j+(length+1)/2] = tmp1DArray[j*2];
				}
				else {
					tmpMax[j] = tmp1DArray[j*2];
					tmpMax[j+(length+1)/2] = tmp1DArray[j*2+1];
				}		
			}
			/*** No pair value assignment ***/
			if((length/2)*2 < length) {
				tmpMax[(length/2)] = tmp1DArray[length-1];
			}
			
			for(int k=0; k<tmp1DArray.length; k++) {
				tmp1DArray[k] = tmpMax[k];				
			}
			/** Debugging			 
			for(int k=0; k<tmp1DArray.length; k++) {				
				System.out.print(pc.Decryption(tmp1DArray[k]) + " ");
			}
			System.out.println();
			*/		
			
			length = (length+1) / 2;
			System.out.println();
		}
		System.out.println();	
		return tmp1DArray[0];	
	}
	
	protected BigInteger findEncSmallest(
			BigInteger[][] cost, int[] rowCover, 
			int[] colCover, BigInteger maxCost) throws Exception {
		System.out.println("\t[START]\tfindEncSmallest()");
		BigInteger minval = maxCost;
		BigInteger[] tmp1DArray = new BigInteger[cost.length * cost[0].length];
		int index = 0;

		for(int i=0; i<cost.length; i++) {
			for(int j=0; j<cost[0].length; j++) {
				if((rowCover[i]==0) && (colCover[j]==0)) {
					tmp1DArray[index++] = cost[i][j];
				}
			}
		}
		
		int length = index;
		BigInteger[] tmpMin = new BigInteger[length];
		int iter = 1;
		/*** Iteration stopping condition ***/
		while(length != 1) {
			System.out.print("iter:(" + iter++ + ")\t");
			/*** Compare D(2k) and D(2k+1) ***/
			for(int j=0; j<length/2; j++) {
				System.out.print(j + " ");				
				if(findMinimum(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2+1])) {
				//if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{tmp1DArray[j*2], tmp1DArray[j*2+1]}, 0).equals(tmp1DArray[j*2+1])) {
					tmpMin[j] = tmp1DArray[j*2+1];
					tmpMin[j+(length+1)/2] = tmp1DArray[j*2];
				}
				else {
					tmpMin[j] = tmp1DArray[j*2];
					tmpMin[j+(length+1)/2] = tmp1DArray[j*2+1];
				}		
			}
			/*** No pair value assignment ***/
			if((length/2)*2 < length) {
				tmpMin[(length/2)] = tmp1DArray[length-1];
			}
			
			for(int k=0; k<index; k++) {
				tmp1DArray[k] = tmpMin[k];				
			}
			/** Debugging			 
			for(int k=0; k<tmp1DArray.length; k++) {				
				System.out.print(pc.Decryption(tmp1DArray[k]) + " ");
			}
			System.out.println();
			*/		
			
			length = (length+1) / 2;	
			System.out.println();
		}
		return tmpMin[0];
	}
	
	protected BigInteger findMinimum(BigInteger a, BigInteger b) throws Exception {
		EncProgCommon.oos.writeObject(0);    	
		if(cp_s.findMinimumOfTwoEncValues(a, b).equals(a)) {
			return a;
		}
		else {
			return b;
		}
	}
}

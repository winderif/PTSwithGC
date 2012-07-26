package Utils;

import java.math.BigInteger;

import Program.EncProgCommon;
import Protocol.*;
import Crypto.*;

public class FindExtremeValue {
	public static BigInteger findEncMaximum(
			BigInteger[] array,			
			ComparisonProtocolOnServer cp_s,
			CryptosystemPaillierServer ps) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.				
				
		int length = array.length;			
		BigInteger[] tmp1DArray = array.clone();
		BigInteger[] tmpMax = new BigInteger[length];		
		
		int iter = 1;
		/*** Iteration stopping condition ***/
		while(length != 1) {
			System.out.print("iter (" + iter++ + ")\t");
			/*** Compare D(2k) and D(2k+1) ***/
			for(int j=0; j<length/2; j++) {
				System.out.print(j + " ");
				EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
				if(cp_s.findMinimumOfTwoEncValues(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2])) {				
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
			System.out.println();
			Print.printEncArray(tmp1DArray, "\t[S]\tfindEncMaximum", ps);
			*/					
			
			length = (length+1) / 2;
			System.out.println();
		}
		System.out.println();	
		
		return tmp1DArray[0];			
	}
	
	public static BigInteger findEncMinimum(
			BigInteger[] array,			
			ComparisonProtocolOnServer cp_s,
			CryptosystemPaillierServer ps) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.				
		int length = array.length;			
		BigInteger[] tmp1DArray = array.clone();		
		BigInteger[] tmpMin = new BigInteger[length];				
		
		int iter = 1;
		/*** Iteration stopping condition ***/
		while(length != 1) {
			System.out.print("iter (" + iter++ + ")\t");
			/*** Compare D(2k) and D(2k+1) ***/
			for(int j=0; j<length/2; j++) {
				System.out.print(j + " ");
				EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
				if(cp_s.findMinimumOfTwoEncValues(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2+1])) {				
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
			
			for(int k=0; k<tmp1DArray.length; k++) {
				tmp1DArray[k] = tmpMin[k];				
			}
			/** Debugging 
			System.out.println();
			Print.printEncArray(tmp1DArray, "\t[S]\tfindEncMinimum", ps);					
			*/
			length = (length+1) / 2;
			System.out.println();
		}
		System.out.println();	
			
		return tmp1DArray[0];	
	}	
	
	public static BigInteger findEncMaximumGC(BigInteger[] tmp1DArray, GCComparisonServer gcc_s) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.			
		BigInteger tmpMax;
		
		// type = 1, is Max
		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);    	
		tmpMax = gcc_s.findMinimumOfTwoEncValues(tmp1DArray, 1);
				
		return tmpMax;
	}
	
	public static BigInteger findEncMinimumGC(BigInteger[] tmp1DArray, GCComparisonServer gcc_s) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.	
		
		BigInteger tmpMin;
		
		// type = 0, is Min
		EncProgCommon.oos.writeObject(ClientState.CLIENT_EXEC);
		tmpMin = gcc_s.findMinimumOfTwoEncValues(tmp1DArray, 0);
					
		return tmpMin;
	}
}

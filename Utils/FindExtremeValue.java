package Utils;

import java.math.BigInteger;
import Protocol.*;
import Crypto.*;

public class FindExtremeValue {
	public static BigInteger findEncMaximum(
			BigInteger[] array,
			ComparisonProtocolOnServer cp_s,
			CryptosystemPaillierServer ps) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.
		System.out.println("\t[START]\findEncMaximum()");
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
				if(cp_s.findMinimumOfTwoEncValues(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2])) {
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
			/** Debugging */			 
			System.out.println();
			for(int k=0; k<tmp1DArray.length; k++) {				
				System.out.print(ps.Decryption(tmp1DArray[k]) + " ");
			}
			System.out.println();
			
			
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
		System.out.println("\t[START]\findEncMinimum()");
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
				if(cp_s.findMinimumOfTwoEncValues(tmp1DArray[j*2], tmp1DArray[j*2+1]).equals(tmp1DArray[j*2+1])) {
				//if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{tmp1DArray[j*2], tmp1DArray[j*2+1]}, 0).equals(tmp1DArray[j*2])) {
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
			/** Debugging */
			System.out.println();
			for(int k=0; k<tmp1DArray.length; k++) {				
				System.out.print(ps.Decryption(tmp1DArray[k]) + " ");
			}
			System.out.println();			
			
			length = (length+1) / 2;
			System.out.println();
		}
		System.out.println();	
		return tmp1DArray[0];	
	}	
	
	public static BigInteger findEncMaximumGC(BigInteger[] tmp1DArray, GCComparisonServer gcc_s) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.	
		System.out.println("\t[START]\findEncMaximumGC()");
		BigInteger tmpMax;
		
		// type = 1, is Max
		tmpMax = gcc_s.findMinimumOfTwoEncValues(tmp1DArray, 1);
			
		return tmpMax;
	}
	
	public static BigInteger findEncMinimumGC(BigInteger[] tmp1DArray, GCComparisonServer gcc_s) throws Exception {
		//Finds the largest element in a positive array.
		//works for arrays where all values are >= 0.	
		System.out.println("\t[START]\findEncMiniimumGC()");
		BigInteger tmpMin;
		
		// type = 0, is Min
		tmpMin = gcc_s.findMinimumOfTwoEncValues(tmp1DArray, 0);
			
		return tmpMin;
	}
}

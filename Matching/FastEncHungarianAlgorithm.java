package Matching;

import java.math.BigInteger;
import Crypto.CryptosystemPaillierServer;

public abstract class FastEncHungarianAlgorithm {
	protected CryptosystemPaillierServer mPaillier;
	protected static final BigInteger Enc_ZERO = BigInteger.ONE;
	
	public FastEncHungarianAlgorithm(CryptosystemPaillierServer paillier) {
		this.mPaillier = paillier;
	}
	
	abstract protected BigInteger findEncLargest(BigInteger[][] array) throws Exception ;
	abstract protected BigInteger findEncSmallest(
			BigInteger[][] cost, int[] rowCover, 
			int[] colCover, BigInteger maxCost) throws Exception ;
	abstract protected BigInteger findMinimum(
			BigInteger a, BigInteger b) throws Exception;
	
	public int[][] hgAlgorithm (BigInteger[][] array, String sumType) throws Exception {	
		BigInteger[][] cost = new BigInteger[array.length][array[0].length];	//Create the cost matrix
		for (int i=0; i<cost.length; i++) {
			for (int j=0; j<cost[i].length; j++) {
				cost[i][j] = array[i][j];
			}
		}
					
		if (sumType.equalsIgnoreCase("max")) {	//Then array is weight array. Must change to cost.
			BigInteger maxWeight = findEncLargest(cost);			
			/**
			 * System.out.println("maxWeight: " + pc.Decryption(maxWeight)); 
			 */					
			for (int i=0; i<cost.length; i++) {		//Generate cost by subtracting.
				for (int j=0; j<cost[i].length; j++) {
					//cost[i][j] = maxWeight.subtract(cost[i][j]);
					cost[i][j] = maxWeight.multiply(cost[i][j].modInverse(mPaillier.nsquare))
										  .mod(mPaillier.nsquare);					
				}				
			}
		}
		
		/** Debugging		 		 
		for(int i=0; i<cost.length; i++) {
			for(int j=0; j<cost[0].length; j++) {
				System.out.print(pc.Decryption(cost[i][j]) + " ");
				System.out.println(cost[i][j] + " ");
				if(cost[i][j].equals(Enc_ZERO)) {
					System.out.println("yes");					
				}
			}
			System.out.println();
		}
		System.out.println("Enc_ZERO: " + Enc_ZERO);		
		*/
		
		BigInteger maxCost = findEncLargest(cost);		//Find largest cost matrix element (needed for step 6).		
		/**
		 * System.out.println("maxCost: " + pc.Decryption(maxCost)); 
		 */		
		
		int[][] mask = new int[cost.length][cost[0].length];	//The mask array.
		int[] rowCover = new int[cost.length];					//The row covering vector.
		int[] colCover = new int[cost[0].length];				//The column covering vector.
		int[] zero_RC = new int[2];								//Position of last zero from Step 4.
		int step = 1;											
		boolean done = false;
		while (done == false) {	//main execution loop
			switch (step)
		    {
				case 1:
					step = hg_step1(step, cost);     
		    	    break;
		    	case 2:
		    	    step = hg_step2(step, cost, mask, rowCover, colCover);
					break;
		    	case 3:
		    	    step = hg_step3(step, mask, colCover);
					break;
		    	case 4:
		    	    step = hg_step4(step, cost, mask, rowCover, colCover, zero_RC);
					break;
		    	case 5:
					step = hg_step5(step, mask, rowCover, colCover, zero_RC);
					break;
		    	case 6:
		    	   	step = hg_step6(step, cost, rowCover, colCover, maxCost);
					break;
		  	    case 7:
		    	    done=true;
		    	    break;
		    }
		}//end while
		
		int[][] assignment = new int[array.length][2];	//Create the returned array.
		for (int i=0; i<mask.length; i++) {
			for (int j=0; j<mask[i].length; j++) {
				if (mask[i][j] == 1) {
					assignment[i][0] = i;
					assignment[i][1] = j;
				}
			}
		}					
		return assignment;
	}
	
	public int hg_step1(int step, BigInteger[][] cost) throws Exception {
		System.out.println("[START]\thg_step1().");
		BigInteger minval;
		
		for(int i=0; i<cost.length; i++) {	   								
	   	    minval = cost[i][0];
	   	    for(int j=0; j<cost[i].length; j++) {//1st inner loop finds min val in row.	   	    
	   	        if(findMinimum(minval, cost[i][j]).equals(cost[i][j])) {
	   	        //if(gcc_s.findMinimumOfTwoEncValues(new BigInteger[]{minval, cost[i][j]}, 0).equals(cost[i][j])) {
	   	            minval = cost[i][j];
	   	        }
			}
			for(int j=0; j<cost[i].length; j++)	{//2nd inner loop subtracts it.				
	   	        // [cost - minval] = [cost] * [minval]^(-1)
				cost[i][j] = cost[i][j].multiply(minval.modInverse(mPaillier.nsquare)).mod(mPaillier.nsquare);	   	      
	   	    }
		}
	   			    
		step=2;
		return step;
	}
	
	public int hg_step2(int step, BigInteger[][] cost, int[][] mask, int[] rowCover, int[] colCover) {
		System.out.println("[START]\thg_step2().");
		for (int i=0; i<cost.length; i++) {	    
	        for (int j=0; j<cost[i].length; j++) {	        
	            if ((cost[i][j].equals(Enc_ZERO)) && (colCover[j] == 0) && (rowCover[i] == 0)) {	            	
	                mask[i][j] = 1;	                
					colCover[j] = 1;
	                rowCover[i] = 1;	                
				}
	            /** Debugging
	            System.out.println("mask: " + mask[i][j] + "\trow:" + rowCover[i] + "\tcol:" + colCover[j]);
	            */
	        }
	    }
							
		clearCovers(rowCover, colCover);	//Reset cover vectors.
			    
		step=3;
		return step;
	}
	
	public int hg_step3(int step, int[][] mask, int[] colCover) {
		System.out.println("[START]\thg_step3().");
		for (int i=0; i<mask.length; i++) {	   
	        for (int j=0; j<mask[i].length; j++) {	        
	            if (mask[i][j] == 1) {	            
	                colCover[j] = 1;	                
				}
	        }
	    }
	    
		int count = 0;						
		for (int j=0; j<colCover.length; j++) {
	        count = count + colCover[j];
	    }		
		if (count >= mask.length)	{		    
			step=7;
		}
	    else {		
			step=4;
		}
	    	
		return step;
	}
	public int hg_step4(
			int step, BigInteger[][] cost, int[][] mask, 
			int[] rowCover, int[] colCover, int[] zero_RC) {
		System.out.println("[START]\thg_step4().");
		//What STEP 4 does:
		//Find an uncovered zero in cost and prime it (if none go to step 6). Check for star in same row:
		//if yes, cover the row and uncover the star's column. Repeat until no uncovered zeros are left
		//and go to step 6. If not, save location of primed zero and go to step 5.
		
		int[] row_col = new int[2];	//Holds row and col of uncovered zero.
		boolean done = false;
		while (done == false) {		
			row_col = findUncoveredZero(row_col, cost, rowCover, colCover);
			if (row_col[0] == -1) {			
				done = true;
				step = 6;
			}
			else {			
				mask[row_col[0]][row_col[1]] = 2;	//Prime the found uncovered zero.
				
				boolean starInRow = false;
				for (int j=0; j<mask[row_col[0]].length; j++) {				
					if (mask[row_col[0]][j]==1) {		//If there is a star in the same row...					
						starInRow = true;
						row_col[1] = j;		//remember its column.
					}
				}
							
				if (starInRow==true) {				
					rowCover[row_col[0]] = 1;	//Cover the star's row.
					colCover[row_col[1]] = 0;	//Uncover its column.
				}
				else {				
					zero_RC[0] = row_col[0];	//Save row of primed zero.
					zero_RC[1] = row_col[1];	//Save column of primed zero.
					done = true;
					step = 5;
				}
			}
		}
		
		return step;
	}
	
	public int hg_step5(int step, int[][] mask, int[] rowCover, int[] colCover, int[] zero_RC) {
		System.out.println("[START]\thg_step5().");
		//What STEP 5 does:	
		//Construct series of alternating primes and stars. Start with prime from step 4.
		//Take star in the same column. Next take prime in the same row as the star. Finish
		//at a prime with no star in its column. Unstar all stars and star the primes of the
		//series. Erasy any other primes. Reset covers. Go to step 3.
		
		int count = 0;												//Counts rows of the path matrix.
		int[][] path = new int[(mask[0].length*mask.length)][2];	//Path matrix (stores row and col).
		path[count][0] = zero_RC[0];								//Row of last prime.
		path[count][1] = zero_RC[1];								//Column of last prime.
		
		boolean done = false;
		while (done == false) {		
			int r = findStarInCol(mask, path[count][1]);
			if (r>=0) {			
				count = count+1;
				path[count][0] = r;					//Row of starred zero.
				path[count][1] = path[count-1][1];	//Column of starred zero.
			}
			else {			
				done = true;
			}
			
			if (done == false) {			
				int c = findPrimeInRow(mask, path[count][0]);
				count = count+1;
				path[count][0] = path [count-1][0];	//Row of primed zero.
				path[count][1] = c;					//Col of primed zero.
			}
		}//end while
		
		convertPath(mask, path, count);
		clearCovers(rowCover, colCover);
		erasePrimes(mask);
		
		step = 3;
		return step;
		
	}
	
	public int hg_step6(int step, BigInteger[][] cost, int[] rowCover, int[] colCover, BigInteger maxCost) throws Exception {
		System.out.println("[START]\thg_step6().");
		//What STEP 6 does:
		//Find smallest uncovered value in cost: a. Add it to every element of covered rows
		//b. Subtract it from every element of uncovered columns. Go to step 4.
		
		BigInteger minval = findEncSmallest(cost, rowCover, colCover, maxCost);				
		
		for (int i=0; i<rowCover.length; i++) {		
			for (int j=0; j<colCover.length; j++) {			
				if (rowCover[i] == 1) {									
					cost[i][j] = cost[i][j].multiply(minval).mod(mPaillier.nsquare);
				}
				if (colCover[j] == 0) {										
					cost[i][j] = cost[i][j].multiply(minval.modInverse(mPaillier.nsquare)).mod(mPaillier.nsquare);									
				}
			}
		}		
			
		step = 4;
		return step;
	}
	
	public static BigInteger[][] transpose(BigInteger[][] array) {		
		BigInteger[][] transposedArray = new BigInteger[array[0].length][array.length];
		for (int i=0; i<transposedArray.length; i++) {		
			for (int j=0; j<transposedArray[i].length; j++)
			{transposedArray[i][j] = array[j][i];}
		}
		return transposedArray;
	}
	
	public static double[][] copyOf(double[][] original) {
	//Copies all elements of an array to a new array.		
		double[][] copy = new double[original.length][original[0].length];
		for (int i=0; i<original.length; i++) {		
			//Need to do it this way, otherwise it copies only memory location
			System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
		}		
		return copy;
	}
	
	public int[] findUncoveredZero(
			int[] row_col, BigInteger[][] cost, 
			int[] rowCover, int[] colCover) {	
		row_col[0] = -1;	//Just a check value. Not a real index.
		row_col[1] = 0;
		
		int i = 0; 
		boolean done = false;
		while (done == false) {		
			int j = 0;
			while (j < cost[i].length) {			
				if ((cost[i][j].equals(Enc_ZERO)) && (rowCover[i]==0) && (colCover[j]==0)) {			
					row_col[0] = i;
					row_col[1] = j;
					done = true;
				}
				j = j+1;
			}//end inner while
			i = i+1;
			if (i >= cost.length) {			
				done = true;
			}
		}//end outer while
		
		return row_col;
	}
	
	public int findStarInCol(int[][] mask, int col) {	
		int r = -1;	//Again this is a check value.
		for (int i=0; i<mask.length; i++) {		
			if (mask[i][col]==1) {			
				r = i;
			}
		}
				
		return r;
	}
	public int findPrimeInRow(int[][] mask, int row) {
		//Aux 2 for hg_step5.
		int c = -1;
		for (int j=0; j<mask[row].length; j++) {
			if (mask[row][j]==2) {
				c = j;
			}
		}
		
		return c;
	}
	public void convertPath(int[][] mask, int[][] path, int count) {
		//Aux 3 for hg_step5.	
		for (int i=0; i<=count; i++) {
			if (mask[(path[i][0])][(path[i][1])]==1) {
				mask[(path[i][0])][(path[i][1])] = 0;
			}
			else {
				mask[(path[i][0])][(path[i][1])] = 1;
			}
		}
	}
	public void erasePrimes(int[][] mask) {	
		for (int i=0; i<mask.length; i++) {		
			for (int j=0; j<mask[i].length; j++) {			
				if (mask[i][j]==2) {				
					mask[i][j] = 0;
				}
			}
		}
	}
	public void clearCovers(int[] rowCover, int[] colCover) {
		for(int i=0; i<rowCover.length; i++) {
			rowCover[i] = 0;
		}
		for (int j=0; j<colCover.length; j++) {		
			colCover[j] = 0;
		}
	}
}

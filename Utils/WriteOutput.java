package Utils;

import java.io.*;

public class WriteOutput {		
	public static boolean writeTagResult(
			String[] mMatchingTags, int iter, int shoNum, 
			String queryDirName, String outputName) {
		
		StringBuilder output = new StringBuilder(outputName);
		StringBuilder dir = new StringBuilder(queryDirName);
		
//		String method = "_orig";
		//String method = "_impr";
//		String Enc = "_NoEnc";
//		String Enc = "_EncHE";
//		String feature = "_TopSurf";
//		String distance = "";
		//String distance = "_L2s";
		//String distance = "_WL2s";
		//String distance = "_L1";
		//String distance = "_WL1";
		
//		output.append(method).append(Enc).append(feature).append(distance);
		output.append(".txt");		
		String shotIndex = Integer.valueOf(iter+1).toString();
		output = dir.append("/").append(shotIndex).append(output);    	
    	
    	try {
    		FileWriter outFile = new FileWriter(output.toString());
    		PrintWriter out = new PrintWriter(outFile);
        	
    		out.println(shoNum);		
    		for(String tag : mMatchingTags) {
    			out.println(tag);
    		}
    		
    		out.close();
        	outFile.close();
        	
        	return true;
    	}catch(IOException e) {
    		e.printStackTrace();
    		return false;
    	}    	
	}	
}

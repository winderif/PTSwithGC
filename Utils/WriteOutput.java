package Utils;

import java.io.*;

public class WriteOutput {
	public static boolean writeTagResult(String[] mMatchingTags, int iter, int shoNum, String queryDirName) {
		String method = "_orig";
		String Enc = "_NoEnc";
		String feature = "_TopSurf";
		String distance = "_WL2s";
		
		String output = method + Enc + feature + distance + ".txt";		
		String shotIndex = Integer.valueOf(iter+1).toString();    	
    	output = queryDirName + "/" + shotIndex + output;
    	
    	try {
    		FileWriter outFile = new FileWriter(output);
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

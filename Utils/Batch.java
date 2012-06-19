package Utils;

import java.io.*;

public class Batch {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String databaseDirName 
			= "C:/Zone/javaworkspace/ForFinal/result/Search Image Dataset/YouTube-Tag";
		String commad
			= "java -Xms256m -Xmx1024m -jar TestTaggingSystemServer.jar -d ";
		
		File databaseDirFile = new File(databaseDirName);
						
		try {			
			FileWriter outFile = new FileWriter("generateDomain.bat");
			PrintWriter out = new PrintWriter(outFile);
			
			for(File categoryDirFile : databaseDirFile.listFiles()) {
				//System.out.println(categoryDirFile.getName());
				for(File tmpFile : categoryDirFile.listFiles()) {
					System.out.println(tmpFile.getAbsolutePath());
					out.println(commad + "\"" + tmpFile.getAbsolutePath() + "\"");
				}				
			}					
			
			out.close();
			outFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}

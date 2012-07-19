package Utils;

import java.io.*;

public class Batch {
	private static void tagSuggestionClient() {
		String databaseDirName =
			"C:/Zone/javaworkspace/PTSwithGC/YouTube";
		String commad =
			"java -Xms256m -Xmx1024m -jar TestTaggingSystemClient.jar -d ";
			//"java -Xms256m -Xmx1024m -jar TestEncTaggingSystemClient.jar -d ";
	
		File databaseDirFile = new File(databaseDirName);
					
		try {			
			FileWriter outFile = new FileWriter("tagSuggestionClient_orig.bat");
			//FileWriter outFile = new FileWriter("tagSuggestionClient_orig_HE.bat");
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
	
	private static void tagSuggestionServer() {
		String databaseDirName =
			"C:/Zone/javaworkspace/ForFinal/result/Search Image Dataset/YouTube-Tag";
		String commad =
			"java -Xms256m -Xmx1024m -jar TestTaggingSystemServer.jar -d ";
			//"java -Xms256m -Xmx1024m -jar TestEncTaggingSystemServer.jar -d ";
	
		File databaseDirFile = new File(databaseDirName);
					
		try {			
			FileWriter outFile = new FileWriter("tagSuggestionServer_orig.bat");
			//FileWriter outFile = new FileWriter("tagSuggestionServer_orig_HE.bat");
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
	
	private static void generateDomain() {
		String databaseDirName =
			"C:/Zone/javaworkspace/ForFinal/result/Search Image Dataset/YouTube-Tag";
		String commad =
			"java -Xms256m -Xmx1024m -jar TestTaggingSystemServer.jar -d ";
	
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
	
	private static void initialTags() {
		String databaseDirName =			
			"C:/Zone/javaworkspace/Ballan/result/YouTube";
		
		File databaseDirFile = new File(databaseDirName);
		
		FilenameFilter mFilenameFilter = new FilenameFilter() {  
			public boolean accept(File file, String name) {  
				boolean ret = !(name.endsWith(".xlsx") || name.endsWith(".txt"));   
				return ret;  
			}
		};
		
		try {			
			FileWriter outFile = new FileWriter("initialTags_YouTube.txt");
			PrintWriter out = new PrintWriter(outFile);
			
			out.println(databaseDirFile.listFiles(mFilenameFilter).length);
			out.println(databaseDirFile.listFiles()[0].listFiles().length);
			for(File categoryDirFile : databaseDirFile.listFiles()) {
				//System.out.println(categoryDirFile.getName());
				if(categoryDirFile.isDirectory()) {
					for(File tmpTopFile : categoryDirFile.listFiles()) {
						//System.out.println(tmpTopFile.getAbsolutePath());					
						System.out.println(tmpTopFile.getParentFile().getName() + "\\" + tmpTopFile.getName());
						out.println(tmpTopFile.getParentFile().getName() + "\\" + tmpTopFile.getName());
						
						File filteredTagsFile = new File(tmpTopFile.getAbsolutePath() + "\\FilteredTags.txt");
						
						FileReader inFile = new FileReader(filteredTagsFile);
						String tmpText = "";
						int in = 0;
						while((in = inFile.read()) != -1)
							tmpText = tmpText + (char)in;
						
						//System.out.println(tmpText);						
						String[] tmpLine = tmpText.split("\r\n");
						
						for(int i=0; i<tmpLine.length-1; i++) {
							System.out.print(tmpLine[i] + ", ");
							out.print(tmpLine[i] + ", ");
						}
						System.out.println();
						out.println();
					}	
				}							
			}
			
			out.close();
			outFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}	
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//generateDomain();
		tagSuggestionServer();
		tagSuggestionClient();
		//initialTags();		
	}
}

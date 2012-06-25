package Utils;

import java.io.*;

public class ExtractTopSurf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String databaseDirName 
			= "C:/Zone/javaworkspace/ForFinal/result/Search Image Dataset/YouTube-Tag";
		String dstDirName
			= "C:/Zone/javaworkspace/PTSwithGC/feature_topsurf/";
				
		String extractCommand
			= "topsurf\\topsurf.exe extract topsurf\\dict 256 100 ";			
		
		String batchFileName = "extractTopsurf.bat";		
		
		File databaseDirFile = new File(databaseDirName);
		try {			
			FileWriter outFile = new FileWriter(batchFileName);
			PrintWriter out = new PrintWriter(outFile);
			
			for(File categoryDirFile : databaseDirFile.listFiles()) {
				//System.out.println(categoryDirFile.getName());			
				File dir = new File(dstDirName + categoryDirFile.getName());
				
				for(File top3DirFile : categoryDirFile.listFiles()) {														
					//System.out.println(top3DirFile.getAbsolutePath());
					File dir2 = new File(dir.getAbsolutePath() + "\\" + top3DirFile.getName());
					
					for(File tag : top3DirFile.listFiles()) {
						//System.out.print(tag.getName() + " ");
						if(tag.isDirectory() && tag.list().length != 0) {
							//System.out.print(tag.getName() + " ");
							File dir3 = new File(dir2.getAbsolutePath() + "\\" + tag.getName());
							
							for(File img : tag.listFiles()) {
								if(img.getName().endsWith(".jpg")) {
									String[] tmp = img.getName().split(".jpg");									
									//System.out.print(tmp[0] + " ");
									
									String cmd = 
										extractCommand + "\"" +
										img.getAbsolutePath() + "\"" + " " + "\"" +  
										dir3.getCanonicalPath() + "\\" + tmp[0] + ".top" + "\"";
																		
									System.out.println(cmd);
									out.println(cmd);
								}
							}							
							System.out.println();					
						}						
					}
					System.out.println();					
				}				
			}					
			
			out.close();
			outFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}	
	}
}

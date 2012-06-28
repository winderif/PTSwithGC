package Utils;

import java.io.*;
import java.util.*;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;

public class Accuracy {
	private Vector<String[]> initialTagsTable = new Vector<String[]>();
	private Vector<Vector<String[]>> suggestedTagsTable = new Vector<Vector<String[]>>();
	
	private int categoryNum = 0;
	private int topNum = 0;
	
	public WritableWorkbook workbook = null;
	public WritableSheet excelSheet = null;
	private WritableCellFormat wcf = null;
	
	public void loadInitialTags() {
		File initialTagsFile = new File("initialTags_YouTube.txt");
		
		if(initialTagsFile.isFile() == false) {
			System.out.println("Cannot load initial tags.");
			System.exit(0);
		}
		else {
			try {
				FileReader inFile = new FileReader(initialTagsFile);
				String tmpText = "";
				int in = 0;
				while((in = inFile.read()) != -1)
					tmpText = tmpText + (char)in;
				
				//System.out.println(tmpText);
				
				String[] tmpLine = tmpText.split("\r\n"); 
								
				categoryNum = Integer.parseInt(tmpLine[0]);
				topNum = Integer.parseInt(tmpLine[1]);
				int index = 0;
				for(int i=0; i<categoryNum*topNum; i++) {
					index = i*2 + 2;
					//System.out.println(tmpLine[index]);					
					//System.out.println(tmpLine[index + 1]);
					String[] tmpTag = tmpLine[index + 1].split(", ");
					String[] tmpLowerTag = new String[tmpTag.length];									
					for(int j=0; j<tmpLowerTag.length; j++) {
						tmpLowerTag[j] = tmpTag[j].toLowerCase();
					}
					
					initialTagsTable.add(tmpLowerTag);
					/** debugging 
					for(String tag : initialTagsTable.elementAt(i)) {						
						System.out.println(tag);
					}				
					*/				
				}
				//tmpTags = null;
				
				inFile.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void loadSuggestionTags() throws WriteException  {
		String databaseDirName =
			"C:/Zone/javaworkspace/PTSwithGC/YouTube";
	
		File databaseDirFile = new File(databaseDirName);
			
		FilenameFilter mFilenameFilter = new FilenameFilter() {  
			public boolean accept(File file, String name) {  
				//boolean ret = name.endsWith("_TopSurf.txt");   
				boolean ret = name.endsWith("_WL2s.txt");
				return ret;  
			}
		};
				
		int sheetIndex = 1;
		excelSheet = workbook.getSheet(0);
		for(File categoryDirFile : databaseDirFile.listFiles()) {
			//System.out.println(categoryDirFile.getName());
			/** Create Excel sheet */
			workbook.createSheet(categoryDirFile.getName(), sheetIndex);			
			Label mLabel = new Label(0, sheetIndex, categoryDirFile.getName());
			excelSheet.addCell(mLabel);			
			sheetIndex++;
			
			for(File tmpTopFile : categoryDirFile.listFiles()) {
				//System.out.println(tmpTopFile.getAbsolutePath());
				
				Vector<String[]> tmpVec = new Vector<String[]>();
				for(File sTag : tmpTopFile.listFiles(mFilenameFilter)) {
					//System.out.println(sTag.getAbsolutePath());	
					tmpVec.add(loadFile(sTag));
				}
				suggestedTagsTable.add(tmpVec);
				tmpVec = new Vector<String[]>();
			}
		}
	}
	
	private String[] loadFile(File sTagFile) {
		if(sTagFile.isFile() == false || sTagFile == null) {
			System.out.println("Cannot load suggested tags file.");
			System.exit(0);			
		}
		else {
			try {			
				FileReader inFile = new FileReader(sTagFile);			
				String tmpText = "";
				int in = 0;
				while((in = inFile.read()) != -1)
					tmpText = tmpText + (char)in;
				
				//System.out.println(tmpText);				
				String[] tmpLine = tmpText.split("\r\n");
				
				String[] sTags = new String[Integer.parseInt(tmpLine[0])];
				for(int i=0; i<sTags.length; i++) {
					sTags[i] = tmpLine[i+1].toLowerCase();
					//System.out.println(sTags[i]);
				}				
				
				inFile.close();
				return sTags;
			} catch(IOException e) {
				e.printStackTrace();
			}				
		}		
		return null;
	}
	
	public void evaluation() throws WriteException {
		Vector<double[]> accuracy = new Vector<double[]>();
		NumberFormat nf = new NumberFormat("0.00");
		wcf = new WritableCellFormat(nf);
		
		for(int i=0; i<initialTagsTable.size(); i++) {
			
			int index = 0;
			double[] tmpAccuracy = new double[suggestedTagsTable.elementAt(i).size()];
			for(String[] sTags : suggestedTagsTable.elementAt(i)) {
				
				double match = 0;
				for(String sTag : sTags) {
					
					for(String iTag : initialTagsTable.elementAt(i)) {
						if(sTag.equals(iTag)) {
							match++;
							break;
						}
					}
				}
				tmpAccuracy[index++] = (match / sTags.length); 
			}
			accuracy.add(tmpAccuracy);			
		}
				
		double total = 0.0;
		for(int i=0; i<this.categoryNum; i++) {
			excelSheet = workbook.getSheet(i+1);
			
			double categoryAcc = 0.0;
			for(int j=0; j<this.topNum; j++) {					
				
				double averageAcc = 0.0;
				double[] num = accuracy.elementAt(i*topNum + j);
				for(int k=0; k<num.length; k++) {
					Number cellNum = new Number(j, k+1, num[k], wcf);					
					excelSheet.addCell(cellNum);					
					averageAcc += num[k];
				}
				averageAcc /= num.length;
				categoryAcc += averageAcc;
				total += averageAcc;
				System.out.println("average: " + averageAcc);
				
				Number cellTop = new Number(j, 0, j+1);
				excelSheet.addCell(cellTop);
				Number cellAveNum = new Number(j, 1, averageAcc, wcf);				
				excelSheet.addCell(cellAveNum);
			}
			categoryAcc /= topNum;
			Number cellCategoryNum = new Number(topNum, 1, categoryAcc, wcf);
			excelSheet.addCell(cellCategoryNum);
			
			excelSheet = workbook.getSheet(0);
			Number cellCategoryNumResult = new Number(1, i+1, categoryAcc, wcf);
			excelSheet.addCell(cellCategoryNumResult);
		}
		total /= 45.0;
		
		excelSheet = workbook.getSheet(0);
		Label mLabel = new Label(0, categoryNum+1, "Total");
		excelSheet.addCell(mLabel);
		Number cellTotalNum = new Number(1, categoryNum+1, total, wcf);
		excelSheet.addCell(cellTotalNum);
		
		System.out.println("total: " + total);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//File expFile = new File("Exp_YouTube_orig_NoEnc_TopSurf_10000.xls");
		File expFile = new File("Exp_YouTube_orig_NoEnc_TopSurf_10000_WL2s.xls");
		
		Accuracy mAccuracy = new Accuracy();
		try {						
			mAccuracy.workbook = Workbook.createWorkbook(expFile);		
			mAccuracy.workbook.createSheet("Setup&Result", 0);
						
			mAccuracy.loadInitialTags();
			mAccuracy.loadSuggestionTags();
			mAccuracy.evaluation();
			
			mAccuracy.workbook.write();
			mAccuracy.workbook.close();
		}catch(Exception e) {
			e.printStackTrace();
		}		
	}
}

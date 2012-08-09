package Utils;

import java.io.*;
import java.util.*;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;

public class Accuracy {
	private Vector<String[]> initialTagsTable = null;
	private Vector<Vector<String[]>> suggestedTagsTable = null;
	private File expFile = null;
	
	private int categoryNum = 0;
	private int topNum = 0;
	
	public WritableWorkbook workbook = null;
	public WritableSheet excelSheet = null;
	private WritableCellFormat wcf = null;
	
	private FilenameFilter mFilenameFilter = null;
	
	public Accuracy(String expFileName, final String filterName) {
		this.initialTagsTable = new Vector<String[]>();
		this.suggestedTagsTable = new Vector<Vector<String[]>>();
		this.expFile = new File(expFileName);		
		this.mFilenameFilter = new FilenameFilter() {  
			public boolean accept(File file, String name) {  
				boolean ret = name.endsWith(filterName);
				return ret;  
			}
		};
	}
	
	public void run() throws WriteException {
		open();				
		
		loadInitialTags();
		
		loadSuggestionTags();
		
		evaluate();
		
		close();
	}
	
	private void open() {
		try {						
			this.workbook = Workbook.createWorkbook(expFile);		
			this.workbook.createSheet("Setup&Result", 0);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void close() {
		try {				
			this.workbook.write();
			this.workbook.close();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(WriteException ew) {
			ew.printStackTrace();
		}
	}			
	
	private void combineCutTags(Vector<String> tmpMoreTags, String[] cutTag) {
		int iter = cutTag.length;
		for(int i=2; i <= iter; i++) {
			String combineTag = new String("");
			if(i == 2) {
				for(int j=0; j<iter-1; j++) {
					combineTag = cutTag[j] + cutTag[j+1];
					tmpMoreTags.add(combineTag.toLowerCase());
				}	
			}
			else if(i == 3) {
				for(int j=0; j<iter-2; j++) {
					combineTag = cutTag[j] + cutTag[j+1] + cutTag[j+2]; 
					tmpMoreTags.add(combineTag.toLowerCase());		
				}
			}
			else {
				for(String cTag : cutTag) {
					combineTag += cTag;
				}
				tmpMoreTags.add(combineTag.toLowerCase());
			}			
		}
	}
	
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
					Vector<String> tmpMoreTags = new Vector<String>();
					for(int j=0; j<tmpTag.length; j++) {
						String[] cutTag = tmpTag[j].split(" ");
						if(cutTag.length > 1) {
							for(String cTag : cutTag) {
								tmpMoreTags.add(cTag.toLowerCase());	
							}
							/** All combination of cutting tags */ 
							combineCutTags(tmpMoreTags, cutTag);
						}
						else {
							tmpMoreTags.add(tmpTag[j].toLowerCase());
						}
					}
					tmpLowerTag = new String[tmpMoreTags.size()];
					tmpMoreTags.toArray(tmpLowerTag);
					initialTagsTable.add(tmpLowerTag);
					/** Original
					for(int j=0; j<tmpLowerTag.length; j++) {
						tmpLowerTag[j] = tmpTag[j].toLowerCase();
					}					
					initialTagsTable.add(tmpLowerTag);
					*/
					
					/** debugging
					for(String tag : initialTagsTable.elementAt(i)) {						
						System.out.print(tag + ", ");
					}	
					System.out.println();
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
		String databaseDirName = "D:/EclipseWorkspace/PTSwithGC/YouTube";
		File databaseDirFile = new File(databaseDirName);
								
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
					//System.out.print(sTags[i] + ", ");
				}	
				//System.out.println();
				
				inFile.close();
				return sTags;
			} catch(IOException e) {
				e.printStackTrace();
			}				
		}		
		return null;
	}
	
	public void evaluate() throws WriteException {
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
				//System.out.print(match / sTags.length + "(" +sTags.length + "), ");
				tmpAccuracy[index++] = (match / sTags.length); 
			}
			//System.out.println();
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
					Number cellNum = new Number(j, k+2, num[k], wcf);					
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
	public static void main(String[] args) throws WriteException {
		// TODO Auto-generated method stub		
		
		Accuracy mAccuracy = new Accuracy("Exp_YouTube_orig_NoEnc_TopSurf.xls", "_orig_NoEnc_TopSurf.txt");
//		Accuracy mAccuracy = new Accuracy("Exp_YouTube_orig_EncHE_TopSurf.xls", "_orig_EncHE_TopSurf.txt");			
		
		mAccuracy.run();
	}
}

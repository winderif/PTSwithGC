package Utils;
import java.io.*;

import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;

public class Experiment {
	private Workbook mReadWorkbook = null;
	private WritableWorkbook mWorkbook = null;	
	
	private static WritableCellFormat mCellIntformat = 
		new WritableCellFormat(new NumberFormat("0.00"));
	private static WritableCellFormat mCellDoubleformat = 
		new WritableCellFormat(new NumberFormat("0"));
	
	private static StringBuilder fileName = null;
	private static File xlsFile = null;
	private static File xlsTmpFile = null;
	
	public Experiment(String dirName) {
		/***/
		//System.out.println(dirName);
		
		//fileName = new StringBuilder("Exp_YouTube_orig_NoEnc_TopSurf_10000_L2s");
		//fileName = new StringBuilder("Exp_YouTube_impr_NoEnc_TopSurf_10000_L2s");
//		fileName = new StringBuilder("Exp_YouTube_orig_EncHE_TopSurf_10000_L2s");
		fileName = new StringBuilder("Exp_YouTube_orig_NoEnc_TopSurf");
		
		String[] token1 = dirName.split("YouTube-Tag");
		//System.out.println(token1[1]);
		String[] token2 = token1[1].split("\\\\");
		//System.out.println(token2[1]);
		
		fileName.append("\\");
		fileName.append(token2[1]);
		fileName.append("_");
		fileName.append(token2[2]);
//		fileName.append(".xls");
		
		/**
		xlsFile = new File(dirName + ".xls");
		xlsTmpFile = new File(dirName + ".tmp");
		*/
		xlsFile = new File(fileName.toString() + ".xls");
		xlsTmpFile = new File(fileName.toString() + ".tmp");
		System.out.println(xlsFile.getAbsolutePath());	
	}
	
	public void open() {
		//System.out.println(xlsFile.exists());
		if(xlsFile.exists() == false) {
			try {									
				mWorkbook = Workbook.createWorkbook(xlsFile);		
			} catch(IOException e) {
				e.printStackTrace();
			}	
		}
		else {
			try {				
				mReadWorkbook = Workbook.getWorkbook(xlsFile);
				mWorkbook = Workbook.createWorkbook(xlsTmpFile, mReadWorkbook);		
			} catch(Exception e) {
				e.printStackTrace();
			}	
		}
	}
	
	public void createSheet(String sheetName, int sheetIndex) {		
		mWorkbook.createSheet(sheetName, sheetIndex);
	}
	
	public void writeSheet(
			int sheetIndex, int iter, int dataIndex, 
			String name, double number) throws WriteException {
		writeSheet(sheetIndex, iter, dataIndex, number);
		writeSheet(sheetIndex, dataIndex, name);
	}
	
	public void writeSheet(
			int sheetIndex, int iter, int dataIndex, 
			String name, int number) throws WriteException {
		writeSheet(sheetIndex, iter, dataIndex, number);
		writeSheet(sheetIndex, dataIndex, name);
	}
	
	public void writeSheet(
			int sheetIndex, int iter, 
			int dataIndex, double number) throws WriteException {
		WritableSheet mSheet = mWorkbook.getSheet(sheetIndex);		
		mSheet.addCell(createNumber(dataIndex+1, iter+1, number));
	}
	
	public void writeSheet(
			int sheetIndex, int iter, 
			int dataIndex, int number) throws WriteException {
		WritableSheet mSheet = mWorkbook.getSheet(sheetIndex);	
		mSheet.addCell(createNumber(dataIndex+1, iter+1, number));
	}
	
	public void writeSheet(
			int sheetIndex, 
			int dataIndex, String name) throws WriteException {
		WritableSheet mSheet = mWorkbook.getSheet(sheetIndex);		
		mSheet.addCell(createLabel(dataIndex+1, 0, name));
	}
	
	private static Label createLabel(int column, int row, String name) {
		return new Label(column, row, name);		 
	}
	
	private static Number createNumber(int column, int row, double number) {
		return new Number(column, row, number, mCellDoubleformat);		 
	}
	
	private static Number createNumber(int column, int row, int number) {
		return new Number(column, row, number, mCellIntformat);		 
	}		
	
	public void close() throws WriteException {
		try {
			mWorkbook.write();
			mWorkbook.close();			
		} catch(IOException e) {
			e.printStackTrace();
		}		
		
		if(xlsTmpFile.exists() == true) {
			try {				
				mReadWorkbook = Workbook.getWorkbook(xlsTmpFile);
				mWorkbook = Workbook.createWorkbook(xlsFile, mReadWorkbook);
				mWorkbook.write();
				mWorkbook.close();	
			} catch(Exception e) {
				e.printStackTrace();
			}	
		}
	}
}

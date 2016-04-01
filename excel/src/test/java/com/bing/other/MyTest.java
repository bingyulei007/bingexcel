package com.bing.other;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class MyTest {
	@Test
	public void testme() throws IOException {
		
		  Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();

	        Sheet sheet = wb.createSheet();
	        Row row = sheet.createRow((short) 2);
	        row.setHeightInPoints(30);

	        createCell(wb, row, (short) 0, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_BOTTOM);
	        createCell(wb, row, (short) 1, CellStyle.ALIGN_CENTER_SELECTION, CellStyle.VERTICAL_BOTTOM);
	        createCell(wb, row, (short) 2, CellStyle.ALIGN_FILL, CellStyle.VERTICAL_CENTER);
	        createCell(wb, row, (short) 3, CellStyle.ALIGN_GENERAL, CellStyle.VERTICAL_CENTER);
	        createCell(wb, row, (short) 4, CellStyle.ALIGN_JUSTIFY, CellStyle.VERTICAL_JUSTIFY);
	        createCell(wb, row, (short) 5, CellStyle.ALIGN_LEFT, CellStyle.VERTICAL_TOP);
	        createCell(wb, row, (short) 6, CellStyle.ALIGN_RIGHT, CellStyle.VERTICAL_TOP);

	        // Write the output to a file
	        FileOutputStream fileOut = new FileOutputStream("xssf-align.xlsx");
	        wb.write(fileOut);
	        
	        fileOut.close();
	}
	/**
     * Creates a cell and aligns it a certain way.
     *
     * @param wb     the workbook
     * @param row    the row to create the cell in
     * @param column the column number to create the cell in
     * @param halign the horizontal alignment for the cell.
     */
    private  void createCell(Workbook wb, Row row, short column, short halign, short valign) {
        Cell cell = row.createCell(column);
        cell.setCellValue("Align It");
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(halign);
        cellStyle.setVerticalAlignment(valign);
        cell.setCellStyle(cellStyle);
    }
    
    @Test
    public void test2() throws IOException{
    	 Workbook wb = new HSSFWorkbook();
    	    Sheet sheet = wb.createSheet("new sheet");

    	    // Create a row and put some cells in it. Rows are 0 based.
    	    Row row = sheet.createRow(1);

    	    // Create a cell and put a value in it.
    	    Cell cell = row.createCell(1);
    	    cell.setCellValue(4);

    	    // Style the cell with borders all around.
    	    CellStyle style = wb.createCellStyle();
    	    style.setBorderBottom(CellStyle.BORDER_THIN);
    	    style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
    	    style.setBorderLeft(CellStyle.BORDER_THIN);
    	    style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
    	    style.setBorderRight(CellStyle.BORDER_THIN);
    	    style.setRightBorderColor(IndexedColors.BLUE.getIndex());
    	    style.setBorderTop(CellStyle.BORDER_MEDIUM_DASHED);
    	    style.setTopBorderColor(IndexedColors.BLACK.getIndex());
    	    cell.setCellStyle(style);

    	    // Write the output to a file
    	    FileOutputStream fileOut = new FileOutputStream("workbook.xls");
    	    wb.write(fileOut);
    	    fileOut.close();
    }
}

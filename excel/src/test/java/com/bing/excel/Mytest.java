package com.bing.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.junit.Test;

import com.bing.excel.core.ExcelBing;

/**
 * 创建时间：2015-12-8下午12:22:38 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：Mytest.java 类说明：
 */
public class Mytest {
	@Test
	public void testM() throws EncryptedDocumentException, InvalidFormatException, IOException {
		/*
		 * File a = new File("d:/ad.s.xlsx"); System.out.println(a.getName());
		 * boolean b = Pattern.matches(".*((\\.xls)|(\\.xlsx))$", null);
		 * System.out.println(b);
		 */
		Workbook wb = WorkbookFactory.create(new File("F:/as.xlsx"));
		  Sheet sheet1 = wb.getSheetAt(0);
		    for (Row row : sheet1) {
		        for (Cell cell : row) {
		            CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
		            System.out.print(cellRef.formatAsString());
		            System.out.print(" - ");

		            switch (cell.getCellType()) {
		                case Cell.CELL_TYPE_STRING:
		                    System.out.println(cell.getRichStringCellValue().getString());
		                    break;
		                case Cell.CELL_TYPE_NUMERIC:
		                    if (DateUtil.isCellDateFormatted(cell)) {
		                        System.out.println(cell.getDateCellValue());
		                        System.out.println(cell.getNumericCellValue());
		                        System.out.println(111111);
		                    } else {
		                        System.out.println(cell.getNumericCellValue());
		                    }
		                    break;
		                case Cell.CELL_TYPE_BOOLEAN:
		                    System.out.println(cell.getBooleanCellValue());
		                    break;
		                case Cell.CELL_TYPE_FORMULA:
		                	 System.out.println(cell.getNumericCellValue());
		                    System.out.println(cell.getCellFormula());
		                    break;
		                default:
		                    System.out.println();
		            }
		        }
		    }
	}

	@Test
	public void testCreatexml() throws IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("new sheet");
		Row row = sheet.createRow(2);
		row.createCell(0).setCellValue(1.1);
		row.createCell(1).setCellValue(new Date());
		row.createCell(2).setCellValue(Calendar.getInstance());
		row.createCell(3).setCellValue("a string");
		row.createCell(4).setCellValue(true);
		row.createCell(5).setCellType(Cell.CELL_TYPE_ERROR);
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream("F:/workbook.xls");
		wb.write(fileOut);
		fileOut.close();
	}

	@Test
	public void testReadxml() throws IOException, EncryptedDocumentException, InvalidFormatException {
		Workbook wb = WorkbookFactory.create(new File("F:/workbook.xls"));
		Sheet sheet = wb.getSheetAt(0);
		for (int rowNum = 0; rowNum < 5; rowNum++) {
			Row r = sheet.getRow(rowNum);
			if (r == null) {
				System.out.println("kong");
				continue;
			}

			for (int cn = 0; cn < 10; cn++) {
				Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
				if (c == null) {
					System.out.println("null");
				} else {
					System.out.println(c.getCellType());
				}
			}
		}

	}

	@Test
	public void testMe() {
		System.out.println(Integer.class.isPrimitive());
		System.out.println("a".getClass().isAssignableFrom(String.class));
		System.out.println(String.class.isPrimitive());
		System.out.println(ExcelBing.class.isPrimitive());
		System.out.println(String.class.getName());
	}
}

package com.bing.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.SaxHandler;
import com.bing.excel.reader.hssf.DefaultHSSFHandler;
import com.bing.excel.reader.vo.CellKV;
import com.bing.excel.reader.vo.ListRow;

public class HffsTest {
	public static class Myte extends AbstractExcelReadListener {
private long start;
		@Override
		public void optRow(int curRow, ListRow rowList) {
		//	System.out.println(rowList);
			/*System.out.println(ArrayUtils.toString(rowList.toArray()));
			System.out.println(ArrayUtils.toString(rowList.toArray(5)));
			System.out.println(ArrayUtils.toString(rowList.toArray(2)));
			for (CellKV cellKV : rowList) {
				System.out.println(cellKV);
			}
			rowList=null;*/
		/*	for (CellKV cellKV : rowList) {
				System.out.println(cellKV);
			}*/
			System.out.println(curRow);
		}

		@Override
		public void startSheet(int sheetIndex, String name) {
			start= System.currentTimeMillis();
			System.out.println("start:"+sheetIndex+":"+name);
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
			System.out.println(System.currentTimeMillis()-start);
			System.out.println("end:"+sheetIndex+":"+name);
		}

		@Override
		public void endWorkBook() {
			System.out.println("end workbook");
		}

	}
	@Test
	public  void testMe() throws FileNotFoundException, IOException, SQLException, OpenXML4JException, SAXException{
		String path = "E:/a1.xls";
		DefaultHSSFHandler handler=new DefaultHSSFHandler(path,new Myte(),true);
		handler.setMaxReturnLine(5);
		handler.readSheets();
	}
	@Test
	public  void testMe1() throws Exception{
		String path = "E:/a1.xlsx";
		SaxHandler handler = ExcelReaderFactory.create(new File(path), new Myte());
		handler.readSheets();
		//handler.readSheet(1);
	}
	@Test
	public  void testDateAll() throws Exception{
		String path = "E:/bc.xlsx";
		SaxHandler handler = ExcelReaderFactory.create(new File(path), new Myte(),true);
		handler.readSheets();
		//handler.readSheet(1);
	}
}

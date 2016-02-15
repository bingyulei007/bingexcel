package com.bing.excel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.hssf.DefaultHSSFHandler;
import com.bing.excel.reader.vo.CellKV;

public class HffsTest {
	public static class Myte extends AbstractExcelReadListener {

		@Override
		public void optRow(int curRow, List<CellKV> rowList) {
			
		}

		@Override
		public void startSheet(int sheetIndex, String name) {
			System.out.println("start:"+sheetIndex+":"+name);
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
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
}

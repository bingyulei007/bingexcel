package com.bing.other;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;

public class TestTest {
	@Test
	public void testme() {
		String path = System.getProperty("java.io.tmpdir") + "TestTest_big.xlsx";
		WriteHandler handler = ExcelWriterFactory.createSXSSF(path);
List<CellKV<String>> listStr=new ArrayList<>();
listStr.add(new CellKV<String>(0, "diyi"));
		handler.createSheet("aa");
		handler.writeHeader(listStr);
		handler.writeLine(new ListLine().addValue(0, true));
		handler.flush();
		
	}
	@Test
	public void testA() {
		HSSFWorkbook wb = new HSSFWorkbook();
		System.out.println(wb.getClass().isAssignableFrom(HSSFWorkbook.class));
		System.out.println(wb.getClass().isAssignableFrom(XSSFWorkbook.class));
	}
}

package com.chinamobile.other;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.chinamobile.excel.vo.CellKV;
import com.chinamobile.excel.vo.ListLine;
import com.chinamobile.excel.writer.ExcelWriterFactory;
import com.chinamobile.excel.writer.WriteHandler;

public class TestTest {
	@Test
	public void testme() {
		WriteHandler handler = ExcelWriterFactory.createSXSSF("E:/aoptest/big.xlsx");
List<CellKV<String>> listStr=new ArrayList<>();
listStr.add(new CellKV<String>(0, "diyi"));
		handler.createSheet("aa");
		handler.writeHeader(listStr);
		handler.writeLine(new ListLine().addValue(0, true));
		handler.flush();
		
	}
}

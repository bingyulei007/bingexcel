package com.bing.excel;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;
import com.google.common.collect.Lists;

public class WriteHandlertest1 {
	@Test
	public void testWrite() {
		WriteHandler handler = ExcelWriterFactory.createXSSF("E:/test/a.xlsx");
		handler.createSheet(null);
		ArrayList<CellKV<String>> list = Lists.newArrayList();
		list.add(new CellKV<String>(0,	 "日期nihao"));
		list.add(new CellKV<String>(1,	 "数字2"));
		list.add(new CellKV<String>(2,	 "真假"));
		list.add(new CellKV<String>(3,	 "日期3"));
		handler.writeHeader(list);
		ListLine line=new ListLine();
		line.addValue(0, new Date()).addValue(1, 23).addValue(2, false).addValue(3, new Date());
		handler.writeLine(line);
		handler.writeLine(line);
		handler.writeLine(line);
		handler.writeLine(line);
		handler.writeLine(line);
		handler.writeLine(line);
		handler.flush();
	}
}

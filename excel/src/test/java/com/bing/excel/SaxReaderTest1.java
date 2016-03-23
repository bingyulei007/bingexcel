package com.bing.excel;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import com.bing.excel.ReadTestGlobalConverter6.Salary;
import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.SaxHandler;
import com.bing.excel.reader.vo.ListRow;

public class SaxReaderTest1 {
	//如果以上都不能满足你的需求 你也可以自己去处理数据。
	@Test
	public void testme() throws Exception{
		URL url = Salary.class.getResource("/salary6.xlsx");
		File f = new File(url.toURI());
		SaxHandler saxHandler = ExcelReaderFactory.create(f, new ExcelReadListener() {
			
			@Override
			public void startSheet(int sheetIndex, String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void optRow(int curRow, ListRow rowList) {
				//输出读取的数据列表。这里数据全部是string类型
				System.out.println(rowList);
			}
			
			@Override
			public void endWorkBook() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void endSheet(int sheetIndex, String name) {
				// TODO Auto-generated method stub
				
			}
		}, true);
		saxHandler.readSheets();
	}
}

package com.bing.other;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.ReadHandler;
import com.bing.excel.vo.ListRow;
import com.bing.utils.ToStringHelper;

public class ReadTestThreadLocal {

	@Test
	public void readExcelTest() {
		System.out.println("start: " + System.currentTimeMillis());
		new ThreadTest().start();
		/*new ThreadTest().start();
		new ThreadTest().start();
		new ThreadTest().start();
		new ThreadTest().start();*/

		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class ThreadTest extends Thread {

		@Override
		public void run() {
			/*BingExcelEvent builder = BingExcelEventBuilder.toBuilder()
					.builder();
			try {
				File f = new File("E:/aoptest/bc.xlsx");

				builder.readFile(f, Person.class, 1, new BingReadListener() {
					

					@Override
					public void readModel(Object object, ModelInfo modelInfo) {

						
					}
				});
				System.out.println(System.currentTimeMillis());
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			java.net.URL url = ThreadTest.class.getResource("/salary.xlsx");
			File f = new File(url.getPath());
			FileInputStream inputStream = null;
			BufferedInputStream bufferedInputStream=null;
			try {
				 inputStream = new FileInputStream(f);
				 bufferedInputStream = new BufferedInputStream(inputStream,10000);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				ReadHandler handler = ExcelReaderFactory.create(inputStream, new ExcelReadListener() {
					
					@Override
					public void startSheet(int sheetIndex, String name) {
					}
					
					@Override
					public void optRow(int curRow, ListRow rowList) {
						if (curRow<20) {
							System.out.println(rowList);
						}
					}
					
					@Override
					public void endWorkBook() {
						System.out.println(System.currentTimeMillis());
					}
					
					@Override
					public void endSheet(int sheetIndex, String name) {
					}
				});
				handler.readSheets();
			} catch (Exception e) {
				e.printStackTrace();
			}
      try {
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

	}

	public static class Person {

		@CellConfig(index = 0)
		private Date date;

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String toString() {
			return ToStringHelper.of(this.getClass()).omitNullValues()
					.add("date", date).toString();
		}
	}
}

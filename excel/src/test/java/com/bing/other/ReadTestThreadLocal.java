package com.bing.other;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.core.BingExcelEvent;
import com.bing.excel.core.BingExcelEventBuilder;
import com.bing.excel.core.BingReadListener;
import com.bing.excel.core.impl.BingExcelEventImpl.ModelInfo;
import com.google.common.base.MoreObjects;

public class ReadTestThreadLocal {

	@Test
	public void readExcelTest() {
		System.out.println("start: " + System.currentTimeMillis());
		new ThreadTest().start();
		new ThreadTest().start();
		new ThreadTest().start();
		new ThreadTest().start();
		new ThreadTest().start();

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
			BingExcelEvent builder = BingExcelEventBuilder.toBuilder()
					.builder();
			try {
				File f = new File("E:/test/bc.xlsx");

				builder.readFile(f, Person.class, 1, new BingReadListener() {
					

					@Override
					public void readModel(Object object, ModelInfo modelInfo) {

						
					}
				});
				System.out.println(System.currentTimeMillis());
			} catch (Exception e) {
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
			return MoreObjects.toStringHelper(this.getClass()).omitNullValues()
					.add("date", date).toString();
		}
	}
}
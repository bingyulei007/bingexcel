package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bing.utils.FileCreateUtils;

public class ExcelWriterFactory {
	public static WriteHandler createHSSF(String path) throws FileNotFoundException {
		File file = FileCreateUtils.createFile(path);
	return	createHSSF(file);
	}

	public static WriteHandler createHSSF(File file) throws FileNotFoundException {
		 Workbook wb = new HSSFWorkbook();
		 return new DefaultFileWriterHandler(wb, file);
	}
	public static WriteHandler createXSSF(String path) throws FileNotFoundException {
		File file = FileCreateUtils.createFile(path);
		return	createXSSF(file);
	}
	
	public static WriteHandler createXSSF(File file) throws FileNotFoundException {
		 Workbook wb = new XSSFWorkbook();
		 return new DefaultFileWriterHandler(wb, file);
	}
}

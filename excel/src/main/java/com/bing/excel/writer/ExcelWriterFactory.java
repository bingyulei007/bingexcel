package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.bing.utils.FileCreateUtils;

public class ExcelWriterFactory {
	public static WriteHandler createHSSF(String path)  {
		Workbook wb = new HSSFWorkbook();
	return	new DefaultFileWriteHandler(wb, path);
	}

	public static WriteHandler createHSSF(File file) throws FileNotFoundException {
		 Workbook wb = new HSSFWorkbook();
		 return new DefaultFileWriteHandler(wb, file);
	}
	public static WriteHandler createHSSF(OutputStream os) throws FileNotFoundException {
		Workbook wb = new HSSFWorkbook();
		return new DefaultStreamWriteHandler(wb, os);
	}
	public static WriteHandler createXSSF(String path)  {
		 Workbook wb = new XSSFWorkbook();
		 return new DefaultFileWriteHandler(wb, path);
	}
	public static WriteHandler createXSSF(OutputStream os) throws FileNotFoundException {
		 Workbook wb = new HSSFWorkbook();
		 return new DefaultStreamWriteHandler(wb, os);
	}
	
	public static WriteHandler createXSSF(File file) throws FileNotFoundException {
		 Workbook wb = new XSSFWorkbook();
		 return new DefaultFileWriteHandler(wb, file);
	}
	public static WriteHandler createSXSSF(String path) throws FileNotFoundException  {
		File file = FileCreateUtils.createFile(path);
		return	createSXSSF(file);
	}
	
	public static WriteHandler createSXSSF(File file) throws FileNotFoundException {
		 SXSSFWorkbook wb = new SXSSFWorkbook(100);
		 FileOutputStream out = new FileOutputStream(file);
		return new SXSSFWriterHandler(wb, out);
	}
	
}

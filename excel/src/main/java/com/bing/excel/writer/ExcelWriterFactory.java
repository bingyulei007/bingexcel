package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriterFactory {
	private static final Pattern OLD_EXCEL_PATH = Pattern.compile(".*\\.xls",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern EXCEL_PATH = Pattern.compile(".*\\.xlsx",
			Pattern.CASE_INSENSITIVE);

	private static void isOldPath(String path) {
		if (path == null || !OLD_EXCEL_PATH.matcher(path).matches()) {
			throw new IllegalArgumentException("the file has a illegal name");
		}
	}

	private static void isNewPath(String path) {
		if (path == null || !EXCEL_PATH.matcher(path).matches()) {
			throw new IllegalArgumentException("the file has a illegal name");
		}
	}

	public static WriteHandler createHSSF(String path) {
		isOldPath(path);
		Workbook wb = new HSSFWorkbook();
		try {
			return new DefaultFileWriteHandler(wb, path);
		} catch (RuntimeException e) {
			closeQuietly(wb);
			throw e;
		}
	}

	public static WriteHandler createHSSF(File file)
			throws FileNotFoundException {
		isOldPath(file.getAbsolutePath());
		Workbook wb = new HSSFWorkbook();
		try {
			return new DefaultFileWriteHandler(wb, file);
		} catch (FileNotFoundException e) {
			closeQuietly(wb);
			throw e;
		} catch (RuntimeException e) {
			closeQuietly(wb);
			throw e;
		}
	}

	public static WriteHandler createHSSF(OutputStream os) {
		Workbook wb = new HSSFWorkbook();
		return new DefaultStreamWriteHandler(wb, os);
	}

	public static WriteHandler createXSSF(String path) {
		isNewPath(path);
		Workbook wb = new XSSFWorkbook();
		try {
			return new DefaultFileWriteHandler(wb, path);
		} catch (RuntimeException e) {
			closeQuietly(wb);
			throw e;
		}
	}

	public static WriteHandler createXSSF(OutputStream os) {
		Workbook wb = new XSSFWorkbook();
		return new DefaultStreamWriteHandler(wb, os);
	}

	public static WriteHandler createXSSF(File file)
			throws FileNotFoundException {
		isNewPath(file.getAbsolutePath());
		Workbook wb = new XSSFWorkbook();
		try {
			return new DefaultFileWriteHandler(wb, file);
		} catch (FileNotFoundException e) {
			closeQuietly(wb);
			throw e;
		} catch (RuntimeException e) {
			closeQuietly(wb);
			throw e;
		}
	}

	public static WriteHandler createSXSSF(String path) {
		isNewPath(path);
		SXSSFWorkbook wb = new SXSSFWorkbook(200);
		try {
			return new SXSSFWriterHandler(wb, path);
		} catch (RuntimeException e) {
			closeQuietly(wb);
			wb.dispose();
			throw e;
		}
	}

	public static WriteHandler createSXSSF(File file)
			throws FileNotFoundException {
		isNewPath(file.getAbsolutePath());
		SXSSFWorkbook wb = new SXSSFWorkbook(200);
		try {
			return new SXSSFWriterHandler(wb, file);
		} catch (FileNotFoundException e) {
			closeQuietly(wb);
			wb.dispose();
			throw e;
		} catch (RuntimeException e) {
			closeQuietly(wb);
			wb.dispose();
			throw e;
		}
	}

	public static WriteHandler createSXSSF(OutputStream os) {
		SXSSFWorkbook wb = new SXSSFWorkbook(200);
		// SXSSFWriterHandler(wb, OutputStream) 构造器只做字段赋值，不会抛异常，
		// 与 createHSSF/createXSSF 的 OutputStream 变体一样无需 try/catch 清理
		return new SXSSFWriterHandler(wb, os);
	}

	private static void closeQuietly(Workbook wb) {
		try {
			wb.close();
		} catch (IOException ignored) {
			// best-effort close on construction failure
		}
	}

}

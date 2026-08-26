package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
		return new DefaultFileWriteHandler(wb, path);
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
		}
	}

	public static WriteHandler createHSSF(OutputStream os) {
		Workbook wb = new HSSFWorkbook();
		return new DefaultStreamWriteHandler(wb, os);
	}

	public static WriteHandler createXSSF(String path) {
		isNewPath(path);
		Workbook wb = new XSSFWorkbook();
		return new DefaultFileWriteHandler(wb, path);
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
		FileOutputStream out = new FileOutputStream(file);
		try {
			return new SXSSFWriterHandler(wb, out);
		} catch (RuntimeException e) {
			try {
				out.close();
			} catch (IOException ignored) {
				// best-effort close on construction failure
			}
			closeQuietly(wb);
			wb.dispose();
			throw e;
		}
	}

	private static void closeQuietly(Workbook wb) {
		try {
			wb.close();
		} catch (IOException ignored) {
			// best-effort close on construction failure
		}
	}

}

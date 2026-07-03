package com.bing.excel.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.bing.excel.writer.exception.ExcelOutException;

public class SXSSFWriterHandler extends AbstractWriteHandler {
	private transient OutputStream os;
	private SXSSFWorkbook wb;
	public SXSSFWriterHandler(Workbook wb, OutputStream outStream) {
		super(wb, outStream);
		this.os=super.os;
		this.wb=(SXSSFWorkbook) wb;
	}
	
	public SXSSFWriterHandler(Workbook wb, String path) {
		super(wb, path);
		this.os=super.os;
		this.wb=(SXSSFWorkbook) wb;
	}

	@ Override
	public void flush() {
		RuntimeException primary = null;
		try {
			if (os != null) {
				super.flush();
			}
		} catch (RuntimeException e) {
			primary = e;
		}
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				ExcelOutException ex = new ExcelOutException("Happen exception when flush", e);
				if (primary == null) {
					primary = ex;
				} else {
					primary.addSuppressed(ex);
				}
			}
		}
		try {
			this.wb.dispose();
		} catch (RuntimeException e) {
			if (primary == null) {
				primary = e;
			} else {
				primary.addSuppressed(e);
			}
		}
		if (primary != null) {
			throw primary;
		}
	}

	public void setCurrentSheetByName(String name, int lineNum){
		SXSSFSheet sheet = wb.getSheet(name);
		if(sheet==null){
			throw new NullPointerException(String.format("no sheet named [%s]", name));
		}else{
			super.currentSheet=sheet;
			super.currentRowIndex=lineNum;
		}
	}
}

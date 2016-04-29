package com.bing.excel.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
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
	@Override
	public void flush() {
		try {
			if (os != null) {
				super.flush();
				this.os.close();
				this.wb.dispose();
			}
		} catch (IOException e) {
			throw new ExcelOutException("Happen exception when flush", e);
		}
	}

}

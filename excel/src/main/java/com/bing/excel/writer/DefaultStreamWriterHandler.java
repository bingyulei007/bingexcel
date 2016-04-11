package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.omg.CORBA.portable.UnknownException;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.ListRow;
import com.bing.excel.writer.exception.ExcelOutException;
import com.bing.utils.FileCreateUtils;

public class DefaultStreamWriterHandler implements WriteHandler {
	private final Workbook wb;
	private transient OutputStream os;
	private int currentRow = -1;
	private Sheet currentSheet;

	/**
	 * @param wb
	 * @param outStream
	 *            U should close the stream by youself.
	 */
	public DefaultStreamWriterHandler(Workbook wb, OutputStream outStream) {
		this.wb = wb;
		this.os = outStream;

	}

	@Override
	public void writeSheet(String name) {
		if (StringUtils.isBlank(name)) {
			currentSheet = wb.createSheet();
		} else {
			currentSheet = wb.createSheet(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.writer.WriterHandler#writeLine()
	 */
	@Override
	public void writeLine(ListLine line) {
		currentRow++;
	}

	@Override
	public void writeHeader(List<CellKV<String>> listStr) {
		currentRow++;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.writer.WriterHandler#flush()
	 */
	@Override
	public void flush() {
		try {
			if (os != null) {
				this.os.flush();
			}
		} catch (IOException e) {

			throw new ExcelOutException("Happen exception when flush", e);
		}
	}
}

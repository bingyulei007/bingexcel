package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.omg.CORBA.portable.UnknownException;

import com.bing.excel.writer.exception.ExcelOutException;
import com.bing.utils.FileCreateUtils;

public class DefaultWriterHandler {
	private final Workbook wb;
	private transient OutputStream os;
	private boolean needClosed = false;

	public DefaultWriterHandler(Workbook wb, File file)
			throws FileNotFoundException {
		this.wb = wb;
		needClosed = true;
		os = new FileOutputStream(file);
	}

	public DefaultWriterHandler(Workbook wb, String path) {
		this.wb = wb;
		File f = FileCreateUtils.createFile(path);
		needClosed = true;
		try {
			os = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// system bug
			f.deleteOnExit();
			throw new UnknownException(e);
		}
	}

	public DefaultWriterHandler(Workbook wb, OutputStream outStream) {
		this.wb = wb;
		this.os = outStream;

	}

	public void writeLine() {

	}

	public void flush() {
		try {
			if (os != null) {
				this.os.flush();
				if (needClosed) {
					this.os.close();
				}
			}
		} catch (IOException e) {

			throw new ExcelOutException("Happen exception when flush",e);
		}
	}
}

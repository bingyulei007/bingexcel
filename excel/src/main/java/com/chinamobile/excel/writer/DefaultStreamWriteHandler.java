package com.chinamobile.excel.writer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;
import com.chinamobile.excel.writer.exception.ExcelOutException;

/**
 * 不是线程安全的
 * @author shizhongtao
 *
 */
public class DefaultStreamWriteHandler extends AbstractWriteHandler {
	private transient OutputStream os;

	/**
	 * @param wb
	 * @param outStream
	 *            U should close the stream by youself.
	 * @throws FileNotFoundException 
	 */
	 DefaultStreamWriteHandler(Workbook wb, OutputStream outStream)  {
		super(wb, outStream);
		this.os = super.os;

	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chinamobile.excel.writer.WriterHandler#flush()
	 */
	@Override
	public void flush() {
		try {
			if (os != null) {
				this.os.flush();
				super.flush();
			}
		} catch (IOException e) {

			throw new ExcelOutException("Happen exception when flush", e);
		}
	}
}

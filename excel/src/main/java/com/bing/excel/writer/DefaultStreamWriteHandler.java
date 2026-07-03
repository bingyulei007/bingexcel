package com.bing.excel.writer;

import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

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
		if (os != null) {
			super.flush();
		}
	}
}

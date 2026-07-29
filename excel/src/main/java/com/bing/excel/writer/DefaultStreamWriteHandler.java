package com.bing.excel.writer;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.bing.excel.writer.exception.ExcelOutException;

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
			try {
				os.flush();
			} catch (IOException e) {
				throw new ExcelOutException("Happen exception when flush", e);
			}
		}
	}

	/**
	 * 只关闭 Workbook，不关闭 OutputStream（调用方自行关闭流）。
	 * 与 {@link #flush()} 的契约一致：U should close the stream by yourself.
	 */
	@Override
	public void close() {
		super.close();
	}
}

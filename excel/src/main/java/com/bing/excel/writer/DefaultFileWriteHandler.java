package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.Workbook;

import com.bing.excel.writer.exception.ExcelOutException;

/**
 * 不是线程安全的
 * @author shizhongtao
 *
 */
public class DefaultFileWriteHandler extends AbstractWriteHandler {
	
	private transient OutputStream os;
	
	

	 DefaultFileWriteHandler(Workbook wb, File file)
			throws FileNotFoundException {
		super(wb,new FileOutputStream(file));
		this.os=super.os;
	}

	 DefaultFileWriteHandler(Workbook wb, String path) {
		super(wb, path);
		this.os=super.os;
	}

	
	

	@Override
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
					throw ex;
				}
				primary.addSuppressed(ex);
			}
		}
		if (primary != null) {
			throw primary;
		}
	}

	/**
	 * 只释放资源不写出。关闭 FileOutputStream 和 Workbook；
	 * flush() 已关闭后调用为 no-op（os 关闭幂等，wb.close 幂等）。
	 */
	@Override
	public void close() {
		RuntimeException primary = null;
		try {
			super.close();
		} catch (RuntimeException e) {
			primary = e;
		}
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				ExcelOutException ex = new ExcelOutException("Happen exception when close", e);
				if (primary == null) {
					throw ex;
				}
				primary.addSuppressed(ex);
			}
		}
		if (primary != null) {
			throw primary;
		}
	}
}

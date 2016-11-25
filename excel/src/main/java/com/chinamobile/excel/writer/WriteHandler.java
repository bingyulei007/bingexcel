package com.chinamobile.excel.writer;

import java.util.List;

import com.chinamobile.excel.vo.CellKV;
import com.chinamobile.excel.vo.ListLine;

/**
 * 目前的三个实现不是线程安全的
 * @author shizhongtao
 *
 */
public interface WriteHandler {

	/**
	 * 
	 */
	public abstract void writeLine(ListLine line);
	public abstract void writeHeader(List<CellKV<String>> listStr);
	void writeHeader(ListLine listLine);
	public abstract String createSheet(String name);

	/**
	 * 
	 */
	public abstract void flush();

}

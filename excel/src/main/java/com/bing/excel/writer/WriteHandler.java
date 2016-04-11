package com.bing.excel.writer;

import java.util.List;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;

public interface WriteHandler {

	/**
	 * 
	 */
	public abstract void writeLine(ListLine line);
	public abstract void writeHeader(List<CellKV<String>> listStr);
	public abstract void writeSheet(String name);

	/**
	 * 
	 */
	public abstract void flush();

}
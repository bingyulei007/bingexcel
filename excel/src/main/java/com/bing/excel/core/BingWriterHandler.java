package com.bing.excel.core;


public interface BingWriterHandler {
	/**
	 * 写入对象到excel表，默认同种对象在同一个sheet页面，不同对象另起新的sheet
	 * @param obj
	 */
	void writeLine(Object obj);
	/**
	 * 输出文件。
	 */
	void close(); 
}

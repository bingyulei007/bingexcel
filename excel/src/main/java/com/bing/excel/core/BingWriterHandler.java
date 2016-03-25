package com.bing.excel.core;


public interface BingWriterHandler {
	void writeLine(Object obj,int sheetIndex);
	void close(); 
}

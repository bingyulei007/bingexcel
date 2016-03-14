package com.bing.excel.core;

import java.io.File;
import java.io.InputStream;


public interface ExcelBingEvent {

	
	<T> void readSheet(File file, Class<T> clazz, int startRowNum,BingReadListener listener) ;
	/**
	 * 根据condition条件读取相应的sheet到list对象
	 * @param file
	 * @param clazz
	 * @param condition
	 * @return
	 */
	<T> void readSheet(File file, ReaderCondition<T> condition,BingReadListener listener) ;

	
	 /**
	  * 读取所有sheet表格，到list
	 * @param file
	 * @param conditions 每个表格对应的condition
	 * @return
	 */
	void readSheetsToList(File file,ReaderCondition[] conditions,BingReadListener listener) ;
	 
	 
	/**
	 * 读取所有sheet表格，到list
	 * @param file
	 * @param clazz 表格转换成的对象
	 * @param startRowNum
	 * @return
	 */
	<T> void readSheetsToList(File file, Class<T> clazz, int startRowNum,BingReadListener listener) ;

	/**
	 * 读取第一个sheet到SheetVo
	 * @param stream
	 * @param condition
	 * @return
	 */
	<T> void readStream(InputStream stream,ReaderCondition<T> condition,BingReadListener listener) ;

	 void readStreamToList(InputStream stream,  ReaderCondition[] condition,BingReadListener listener) ;
	 /**
	  * 适合所有sheet中数据结构一样的excel
	 * @param stream
	 * @param condition
	 * @return
	 */
	<T> void readStreamToList(InputStream stream,  ReaderCondition<T> condition,BingReadListener listener) ;

}

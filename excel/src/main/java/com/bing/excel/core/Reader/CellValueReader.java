package com.bing.excel.core.Reader;

import org.apache.poi.ss.usermodel.Cell;

import com.bing.excel.exception.ErrorValueException;

/**
 * 创建时间：2015-12-15下午2:29:27 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：CellValueReader.java 类说明：
 */
public interface CellValueReader {
	/**
	 * <p>Title: 得到对应excel中的值</p>
	 * <p>Description: </p>
	 * @return null 如果这个cell是空，
	 */
	Object getValue() throws ErrorValueException;

	
	int getCellType();
}

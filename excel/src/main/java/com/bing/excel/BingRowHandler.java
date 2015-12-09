package com.bing.excel;

import org.apache.poi.ss.usermodel.Row;

/**
 * 创建时间：2015-12-8下午7:18:43 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingRowHandler.java 类说明：
 */
public class BingRowHandler<T> {
	private Row row;

	public BingRowHandler(Row row) {
		this.row = row;
	}
	public T readRowToEntity(){
		return null;
	}
}

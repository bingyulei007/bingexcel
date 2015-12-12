package com.bing.excel;

import java.lang.reflect.Field;

import org.apache.poi.ss.usermodel.Row;

/**
 * 创建时间：2015-12-8下午7:18:43 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingRowHandler.java 类说明：
 */
public class RowHandler<T> {
	private Row row;

	public RowHandler(Row row) {
		this.row = row;
	}
	public T readRowToEntity(Class<T> clazz) throws InstantiationException, IllegalAccessException{
		T t=clazz.newInstance();
		Field[] fields = clazz.getDeclaredFields();
		return t;
	}
}

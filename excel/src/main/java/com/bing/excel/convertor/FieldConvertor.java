package com.bing.excel.convertor;

import java.lang.reflect.Field;

import org.apache.poi.ss.usermodel.Cell;

/**  
 * 创建时间：2015-12-15下午4:42:58  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：FieldConvertor.java  
 * 类说明：  
 */
public class FieldConvertor implements Convertor {

	private Class<?> clazz;
	private Cell cell;
	@Override
	public Object unmarshal(Cell cell, Class<?> clazz) {
		this.clazz=clazz;
		this.cell=cell;
		return null;
	}

	
}

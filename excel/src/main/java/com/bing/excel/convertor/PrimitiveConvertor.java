package com.bing.excel.convertor;

import org.apache.poi.ss.usermodel.Cell;



/**  
 * 创建时间：2015-12-8下午9:51:00  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：FieldConvertor.java  
 * 类说明：  
 */
public interface PrimitiveConvertor  {
	 // void marshal(Object source,   MarshallingContext context);
	  Object unmarshal(Cell cell, Class<?> calss);
}

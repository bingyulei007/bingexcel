package com.bing.excel.convertor;

import java.lang.reflect.Field;

import org.apache.poi.ss.usermodel.Cell;

/**  
 * 创建时间：2015-12-15下午2:12:56  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：Convertor.java  
 * 类说明：  这里面convertor是针对实体类的filed。主要用于扩展转换，默认的目前应该支持boolean，枚举。
 */
public interface Convertor extends ConverterMatcher {
	 // void marshal(Object source,   MarshallingContext context);
	  Object unmarshal(Cell cell);
}

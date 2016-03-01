package com.bing.excel.converter;

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
public interface PrimitiveConvertor  extends ConverterMatcher {
	 // void marshal(Object source,   MarshallingContext context);
	  /**
	 * <p>Title: unmarshal</p>
	 * <p>Description: </p>
	 * @param source excel中读取的值，首先看看有没有 注解属性，如果没有-如果与目标的基本类型一样，直接转换
	 * @param calss 对应filed的类型。
	 * @return
	 */
	Object unmarshal(Object source, Class<?> calss);
}

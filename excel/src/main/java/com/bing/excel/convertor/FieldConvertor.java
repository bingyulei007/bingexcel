package com.bing.excel.convertor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

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
public abstract class FieldConvertor implements Convertor {

	@Override
	public Object unmarshal(Cell cell) {
		return null;
	}
	/**
	 * 
	 * <p>Title: 测试对应的cell对应的类型能不能应用本转换器。</p>
	 * <p>Description:注意一点，及时返回true ，也不代表转换过程没有出错。这个是用来匹配转换器的 </p>
	 * @return
	 */
	@Override
	public abstract boolean canConvert(Class<?> clz) ;

	
}

package com.bing.excel.converter;


import com.bing.excel.mapper.FieldMapperHandler;
import com.bing.excel.vo.ListRow;



/**  
 * 创建时间：2015-12-15下午2:12:56  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * 文件名称：Convertor.java  
 */
public interface ModelAdapter  {
	  void marshal(Object source);
	Object unmarshal(ListRow source, FieldMapperHandler handler);
}
 
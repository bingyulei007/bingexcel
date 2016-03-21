package com.bing.excel.converter;


import com.bing.excel.mapper.OrmMapper;
import com.bing.excel.reader.vo.ListRow;



/**  
 * 创建时间：2015-12-15下午2:12:56  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * 文件名称：Convertor.java  
 */
public interface Converter  {
	  void marshal(Object source);
	Object unmarshal(ListRow source, OrmMapper ormMapper);
}
 
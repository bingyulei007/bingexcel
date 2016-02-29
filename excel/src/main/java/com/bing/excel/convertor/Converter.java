package com.bing.excel.convertor;



/**  
 * 创建时间：2015-12-15下午2:12:56  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * 文件名称：Convertor.java  
 * 类说明：  这里面convertor是针对实体类的filed。主要用于扩展转换,目前版本中，convertor中必须有无参的构造方法。
 */
public interface Converter extends ConverterMatcher {
	 // void marshal(Object source,   MarshallingContext context);
	  Object unmarshal(Object cell);
}

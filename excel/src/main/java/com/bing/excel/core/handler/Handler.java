package com.bing.excel.core.handler;
/**  
 * 创建时间：2015-12-14下午4:44:52  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：Handler.java  
 * 类说明：  转换excel数据到类实体时候  的抽象对象
 */
public interface Handler<T> {

	/**
	 * <p>Title: 处理转换的逻辑</p>
	 * <p>Description: </p>
	 */
	void process( Class<T> clazz);
	
	
}

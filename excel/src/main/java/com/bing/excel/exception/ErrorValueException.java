package com.bing.excel.exception;
/**  
 * 创建时间：2015-12-15下午3:52:43  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：ErrorValueException.java  
 * 类说明：  
 */
public class ErrorValueException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public ErrorValueException() {
		super("单元格类型错误");
	}

}

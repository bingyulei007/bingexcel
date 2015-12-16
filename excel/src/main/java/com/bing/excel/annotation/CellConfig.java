package com.bing.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bing.excel.convertor.Convertor;

/**
 * 创建时间：2015-12-8下午9:41:18 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingCell.java 类说明：
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CellConfig {
	/**
	 * <p>Title: 下标值</p>
	 * <p>Description: 从0开始</p>
	 * @return
	 */
	public int index() default 0;
	/**
	 * <p>Title: format 格式化结果输出</p>
	 * <p>Description: see at <code>java.util.String#format()</code>,这个值就是format参数，当对应属性是 String类型时候才起作用。</p>
	 * @return
	 */
	public String format() default "";
	
}

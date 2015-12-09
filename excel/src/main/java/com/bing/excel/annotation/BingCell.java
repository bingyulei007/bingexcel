package com.bing.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bing.excel.convertor.FieldConvertor;

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
public @interface BingCell {
	public int index() default 0;
	public String format() default "";
	public Class<FieldConvertor> convertor();
}

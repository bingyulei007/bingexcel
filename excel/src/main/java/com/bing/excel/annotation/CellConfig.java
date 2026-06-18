package com.bing.excel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 创建时间：2015-12-8下午9:41:18 项目名称：excel
 *
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingCell.java 类说明：
 *
 * <p>用于实体类字段，标记该字段在 Excel 中的列位置及读取配置。</p>
 *
 * <p><b>读/写不对称说明：</b>读取时，{@code index} 与 {@code aliasName} 二选一即可——
 * 当 {@code index<0} 且 {@code aliasName} 非空时，库会按 Excel 表头匹配列；
 * 写入时，{@code index} 必须 {@code >=0}，{@code aliasName} 仅作为表头文本。</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CellConfig {
	/**
	 * <p>Title: 下标值</p>
	 * <p>Description: 从0开始。读取时若该值为 -1 且 {@link #aliasName()} 非空，
	 *                  则按表头文本匹配列；写入时必须 >=0。</p>
	 * @return 转换为orm模型中java的类，如果不能转换返回null，基本类型中为默认值。
	 */
	public int index() default -1;

	/**
	 * excel 读取数据时候，是不是为必须参数。默认是false
	 * @return true or false
	 */
	public boolean readRequired() default false;
	/**
	 * excel导出时候，字段是否忽略导出，default <code>false</code>。
	 * @return ture or false
	 */
	//public boolean omitOutput() default false;

	/**
	 * 输出时候的title名称。读取时若 {@link #index()} 为 -1 且本字段非空，
	 *                  则按 Excel 表头匹配列；写入时仅作为表头文本。
	 *                  留空时使用 Java 字段名（兼容旧行为）。
	 * @return  别名
	 */
	public String aliasName() default "";
}

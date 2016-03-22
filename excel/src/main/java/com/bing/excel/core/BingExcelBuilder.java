package com.bing.excel.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bing.common.Builder;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.impl.BingExcelImpl;
import com.bing.excel.exception.ConversionException;
import com.google.common.primitives.Primitives;

/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <code>BingExcel</code>的构造类，可以添加自定义转换器等。<／p>
 * <p>
 * Company: bing<／p>
 * 
 * @author zhongtao.shi
 * @date 2015-12-8
 */
/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Company: bing<／p>
 * 
 * @author zhongtao.shi
 * @date 2015-12-8
 */
public class BingExcelBuilder implements Builder<BingExcel> {
	private final Map<Class<?>, FieldValueConverter> defaultLocalConverter = Collections
			.synchronizedMap(new HashMap<Class<?>, FieldValueConverter>());
	/**
	 * bingExcel:对应的excel工具类。
	 */
	private BingExcel bingExcel;

	/**
	 * <p>
	 * Title: <／p>
	 * <p>
	 * Description: 构造新的builder对象<／p>
	 */
	private BingExcelBuilder() {

	}

	public static Builder<BingExcel> toBuilder() {

		return new BingExcelBuilder();

	}
	@Override
	public Builder<BingExcel> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter) {
		if (converter.canConvert(clazz)) {

			if (clazz.isPrimitive()) {
				defaultLocalConverter.put(Primitives.wrap(clazz), converter);
			} else {
				defaultLocalConverter.put(clazz, converter);
			}
		} else {
			throw new ConversionException("register converter for["
					+ clazz.getName() + "] failed!");
		}
		return this;
	}

	@Override
	public BingExcel builder() {
		if (bingExcel == null) {
			bingExcel = new BingExcelImpl(defaultLocalConverter);
		}

		return this.bingExcel;
	}

}

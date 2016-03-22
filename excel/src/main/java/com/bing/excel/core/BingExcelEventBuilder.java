package com.bing.excel.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bing.common.Builder;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.impl.BingExcelEventImpl;
import com.bing.excel.core.impl.BingExcelImpl;
import com.bing.excel.exception.ConverterException;
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
public class BingExcelEventBuilder implements Builder<BingExcelEvent> {
	private final Map<Class<?>, FieldValueConverter> defaultLocalConverter = Collections
			.synchronizedMap(new HashMap<Class<?>, FieldValueConverter>());

	private BingExcelEvent bingExcelEvent;

	/**
	 * <p>
	 * Title: <／p>
	 * <p>
	 * Description:<／p>
	 */
	private BingExcelEventBuilder() {

	}

	public static Builder<BingExcelEvent> toBuilder() {

		return new BingExcelEventBuilder();

	}

	public void registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter) {
		if (converter.canConvert(clazz)) {

			if (clazz.isPrimitive()) {
				defaultLocalConverter.put(Primitives.wrap(clazz), converter);
			} else {
				defaultLocalConverter.put(clazz, converter);
			}
		} else {
			throw new ConverterException("register converter for["
					+ clazz.getName() + "] failed!");
		}
	}

	@Override
	public BingExcelEvent builder() {
		if (bingExcelEvent == null) {
			bingExcelEvent = new BingExcelEventImpl(defaultLocalConverter);
		}

		return this.bingExcelEvent;
	}

}

package com.bing.common;

import com.bing.excel.converter.FieldValueConverter;

/**
 * @author shizhongtao
 *
 * Description:
 */
public interface Builder<T> {
	
	T builder();

	Builder<T> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter);
}

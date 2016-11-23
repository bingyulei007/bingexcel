package com.chinamobile.common;

import com.chinamobile.excel.converter.FieldValueConverter;

/**
 * @author shizhongtao
 *
 * @date 2015-12-17
 * Description:  
 */
public interface Builder<T> {
	
	T builder();

	Builder<T> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter);
}

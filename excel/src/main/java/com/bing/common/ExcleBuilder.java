package com.bing.common;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.BingExcel;

/**
 * @author shizhongtao
 *
 * Description:
 */
public interface ExcleBuilder<T> {

	ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
			String filedName, int index);

	ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
			String filedName, int index, String alias);

	ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
			String filedName, int index, String alias, FieldValueConverter converter);

	ExcleBuilder<BingExcel> addClassNameAlias(Class<?> clazz,
			String alias);

	T build();

	ExcleBuilder<T> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter);
}

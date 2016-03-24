package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;

public final class StringFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(String.class);
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {
		return cell;
	}

	

}

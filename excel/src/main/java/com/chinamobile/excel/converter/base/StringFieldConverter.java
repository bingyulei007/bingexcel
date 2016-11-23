package com.chinamobile.excel.converter.base;

import java.lang.reflect.Type;

import com.chinamobile.excel.converter.AbstractFieldConvertor;
import com.chinamobile.excel.core.handler.ConverterHandler;

public final class StringFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(String.class);
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {
		return cell;
	}

	

}

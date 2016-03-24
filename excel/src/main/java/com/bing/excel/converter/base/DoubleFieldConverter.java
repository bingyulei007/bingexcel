package com.bing.excel.converter.base;

import org.apache.commons.lang3.StringUtils;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.google.common.base.Strings;

public final class DoubleFieldConverter extends AbstractFieldConvertor {

	@Override
	 public boolean canConvert(Class type) {
	        return type.equals(double.class) || type.equals(Double.class);
	    }

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {
		if(StringUtils.isBlank(cell)){
			return null;
		}
		return Double.valueOf( cell);
	}

}

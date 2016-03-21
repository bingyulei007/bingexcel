package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;

public final class StringFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(String.class);
	}

	@Override
	public Object fromString(String cell) {
		return super.fromString(cell);
	}

	

}

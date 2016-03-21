package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.google.common.base.Strings;

public final class ShortFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(short.class) || clz.equals(Short.class);
	}

	@Override
	public Object fromString(String cell) {
		if(Strings.isNullOrEmpty(cell)){
			return null;
		}
		int value = Integer.valueOf(cell).intValue();
    	if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
    		throw new NumberFormatException("For input string: \"" + cell + '"');
    	}
        return new Short((short)value);
	}

}

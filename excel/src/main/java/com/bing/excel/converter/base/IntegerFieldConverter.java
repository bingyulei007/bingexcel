package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;

public final class IntegerFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(int.class) || clz.equals(Integer.class);
	}

	@Override
	public Object fromString(Object cell) {
		long value = Long.valueOf(cell.toString()).longValue();
    	if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
    		throw new NumberFormatException("For input string: \"" + cell + '"');
    	}
        return new Integer((int)value);
	}

}

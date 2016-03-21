package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.google.common.base.Strings;

/**
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  
 */
public final class ByteFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(byte.class) || clz.equals(Byte.class);
	}

	@Override
	public void toObject(Object source) {
		// TODO Auto-generated method stub
		super.toObject(source);
	}

	@Override
	public Object fromString(String cell) {
		if (Strings.isNullOrEmpty(cell)) {
			return null;
		}
		int value = Integer.decode(cell).intValue();
    	if(value < Byte.MIN_VALUE || value > 0xFF) {
    		throw new NumberFormatException("For input string: \"" + cell + '"');
    	}
        return new Byte((byte)value);
	}


}

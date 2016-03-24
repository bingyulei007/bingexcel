package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.google.common.base.Strings;

/**
 * @author shizhongtao
 * 
 * @date 2016-3-10 Description:
 */
public final class IntegerFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(int.class) || clz.equals(Integer.class);
	}

	/**
	 * @return return the long value; return Long.decode(str),only in this case  the str start with "0x"
	 */
	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {

		if (Strings.isNullOrEmpty(cell)) {
			return null;
		}
		long value;
		char c1 = cell.charAt(1);
		if (c1 == 'x' || c1 == 'X') {
			value = Long.decode(cell);
		} else {
			value = Long.valueOf(cell.toString()).longValue();
		}
		if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
			throw new NumberFormatException("For input string: \"" + cell + '"');
		}
		return new Integer((int) value);
	}

}

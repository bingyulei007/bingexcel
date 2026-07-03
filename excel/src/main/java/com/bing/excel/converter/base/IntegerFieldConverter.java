package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shizhongtao
 * 
 * date 2016-3-10 Description:
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
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {

		if (StringUtils.isBlank(cell)) {
			return null;
		}
		long value= Long.decode(cell.trim()).longValue();
    	if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
    		throw new NumberFormatException("For input string: \"" + cell + '"');
    	}
        return Integer.valueOf((int)value);
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.intValue(source);
	}

}

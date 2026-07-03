package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.vo.OutValue;
import org.apache.commons.lang3.StringUtils;

public final class LongFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(long.class) || clz.equals(Long.class);
	}

	@Override
	public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
		if (StringUtils.isBlank(cell)) {
            return null;
        }
		return Long.decode(cell.trim());
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.longValue(source);
	}

}

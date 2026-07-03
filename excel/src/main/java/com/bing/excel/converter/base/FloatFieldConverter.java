package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.vo.OutValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shizhongtao
 *
 * date 2016-3-21
 * Description:  
 */
public final class FloatFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(float.class) || clz.equals(Float.class);
	}

	@Override
	public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
		if(StringUtils.isBlank(cell)){
			return null;
		}
		return Float.valueOf(cell.trim());
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.doubleValue(((Float)source).doubleValue());
	}

}

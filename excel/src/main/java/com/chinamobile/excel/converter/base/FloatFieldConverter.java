package com.chinamobile.excel.converter.base;

import java.lang.reflect.Type;

import com.chinamobile.excel.converter.AbstractFieldConvertor;
import com.chinamobile.excel.core.handler.ConverterHandler;
import com.chinamobile.excel.vo.OutValue;
import com.google.common.base.Strings;

/**
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  
 */
public final class FloatFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(float.class) || clz.equals(Float.class);
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {
		if(Strings.isNullOrEmpty(cell)){
			return null;
		}
		return Float.valueOf(cell);
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.dateValue(((Float)source).doubleValue());
	}

}

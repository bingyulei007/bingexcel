package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
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
	public Object fromString(String cell) {
		if(Strings.isNullOrEmpty(cell)){
			return null;
		}
		return Float.valueOf(cell);
	}

}

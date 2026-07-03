package com.bing.excel.converter.enums;

import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.OutValue;

/**
 * @author shizhongtao
 *
 * date 2016-3-21
 * Description:  
 */
public class EnumConVerter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.isEnum() || Enum.class.isAssignableFrom(clz);
	}

	

	@Override
	public OutValue toObject(Object source, ConverterHandler converterHandler) {
		if (source == null) {
			return null;
		}
		return OutValue.stringValue(((Enum<?>) source).name());
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type type) {
		if (type == null || StringUtils.isBlank(cell)) {
			return null;
		}
		Class targetType=(Class) type;
		if (targetType.getSuperclass() != Enum.class) {
			targetType = targetType.getSuperclass(); // polymorphic enums
		}
		String enumName = cell.trim();
		try {
			return Enum.valueOf(targetType, enumName);
		} catch (IllegalArgumentException e) {
			Enum[] enumConstants = (Enum[])targetType.getEnumConstants();
			for (Enum item : enumConstants) {
				if(item.name().equalsIgnoreCase(enumName)){
					return item;
				}
			}
			throw new ConversionException("Cannot parse value '" + cell + "' to enum "
					+ targetType.getName(), e);
		}
	}

}

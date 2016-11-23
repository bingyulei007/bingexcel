package com.chinamobile.excel.converter.base;

import java.lang.reflect.Type;

import com.chinamobile.excel.converter.AbstractFieldConvertor;
import com.chinamobile.excel.core.handler.ConverterHandler;
import com.chinamobile.excel.vo.OutValue;

/**
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  
 */
public final class CharacterFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(char.class) || clz.equals(Character.class);
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {
		if (cell==null) {
			return null;
		}
		 if (cell.length() == 0) {
	            return new Character('\0');
	        } else {
	            return new Character(cell.charAt(0));
	        }
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if (source==null) {
			return null;
		}
		return OutValue.stringValue(source.toString());
	}

}

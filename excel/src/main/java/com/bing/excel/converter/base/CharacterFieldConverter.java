package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.google.common.base.Strings;

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
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {
		if (cell==null) {
			return null;
		}
		 if (cell.length() == 0) {
	            return new Character('\0');
	        } else {
	            return new Character(cell.charAt(0));
	        }
	}

}

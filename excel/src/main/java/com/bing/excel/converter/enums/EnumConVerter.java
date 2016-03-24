package com.bing.excel.converter.enums;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;

/**
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  
 */
public class EnumConVerter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.isEnum() || Enum.class.isAssignableFrom(clz);
	}

	@Override
	public void toObject(Object source) {
		
	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {
			if(targetType==null){
				return null;
			}
		  if (targetType.getSuperclass() != Enum.class) {
			  targetType = targetType.getSuperclass(); // polymorphic enums
	        }
		  try {
	            return Enum.valueOf(targetType, cell);
	        } catch (IllegalArgumentException e) {
	        	Enum[] enumConstants = (Enum[])targetType.getEnumConstants();
	        	for (Enum item : enumConstants) {
					if(item.name().equals(cell)){
						return item;
					}
				}
	        	
	            throw e;
	        }
	}

}

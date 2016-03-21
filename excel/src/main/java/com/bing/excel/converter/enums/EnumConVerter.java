package com.bing.excel.converter.enums;

import com.bing.excel.converter.AbstractFieldConvertor;

/**
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  
 */
public class EnumConVerter extends AbstractFieldConvertor {

	private Class targetType;
	@Override
	public boolean canConvert(Class<?> clz) {
			targetType=clz;
		 return clz.isEnum() || Enum.class.isAssignableFrom(clz);
	}

	@Override
	public void toObject(Object source) {
		
	}

	@Override
	public Object fromString(String cell) {
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

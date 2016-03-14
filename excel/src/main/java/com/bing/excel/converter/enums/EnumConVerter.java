package com.bing.excel.converter.enums;

import com.bing.excel.converter.AbstractFieldConvertor;

public class EnumConVerter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.isEnum() || Enum.class.isAssignableFrom(clz);
	}

	@Override
	public void toObject(Object source) {
		// TODO Auto-generated method stub
		super.toObject(source);
	}

}

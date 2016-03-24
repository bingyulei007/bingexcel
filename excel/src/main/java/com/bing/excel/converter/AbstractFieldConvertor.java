package com.bing.excel.converter;

import com.bing.excel.core.handler.ConverterHandler;

public class AbstractFieldConvertor implements FieldValueConverter {

	@Override
	public boolean canConvert(Class<?> clz) {
		
		return false;
	}

	@Override
	public void toObject(Object source) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class targetType) {
		if(cell==null){
			return null;
		}
		return cell;
	}

}

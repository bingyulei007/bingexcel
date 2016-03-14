package com.bing.excel.converter;

public class AbstractFieldConvertor implements FieldValueConverter {

	@Override
	public boolean canConvert(Class<?> clz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void toObject(Object source) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object fromString(Object cell) {
		// TODO Auto-generated method stub
		return null;
	}

}

package com.bing.excel.converter;

public class AbstractConvertor implements Converter {

	@Override
	public boolean canConvert(Class<?> clz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void marshal(Object source) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object unmarshal(Object cell) {
		// TODO Auto-generated method stub
		return null;
	}

}

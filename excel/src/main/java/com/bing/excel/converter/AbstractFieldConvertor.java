package com.bing.excel.converter;

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
	public Object fromString(Object cell) {
		if(cell==null){
			return null;
		}
		return cell;
	}

}

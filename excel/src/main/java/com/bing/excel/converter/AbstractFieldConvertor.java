package com.bing.excel.converter;

import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import com.bing.excel.vo.OutValue.OutType;

public class AbstractFieldConvertor implements FieldValueConverter {

	@Override
	public boolean canConvert(Class<?> clz) {

		return false;
	}

	@Override
	public OutValue toObject(Object source) {
		return new OutValue(OutType.STRING, source.toString());
	}

	@Override
	public Object fromString(String cell, ConverterHandler converterHandler,
			Class targetType) {
		if (cell == null) {
			return null;
		}
		return cell;
	}

}

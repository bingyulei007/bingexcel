package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.OutValue;
import com.google.common.base.Strings;

/**
 * 
 * @author shizhongtao
 * 
 * @date 2016-3-21 Description: thanks for Joe Walnes and David Blevins
 */
public final class BooleanFieldConverter extends AbstractFieldConvertor {
	private final boolean caseSensitive;
	private final String trueCaseStr;
	private final String falseCaseStr;

	public BooleanFieldConverter(String trueCaseStr, String falseCaseStr,
			boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		this.trueCaseStr = trueCaseStr;
		this.falseCaseStr = falseCaseStr;
	}

	public BooleanFieldConverter() {
		this("TRUE", "FALSE", false);
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(boolean.class) || clz.equals(Boolean.class);
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		String re;
		if((boolean)source){
			re=trueCaseStr;
		}else{
			re=falseCaseStr;
		}
		return OutValue.stringValue(re);
	}

	/*
	 * in other case ,return false?FIXME
	 */
	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {
		if (Strings.isNullOrEmpty(cell)) {
			return null;
		}
		Boolean re;
		if (caseSensitive) {
			re = trueCaseStr.equals(cell) ? Boolean.TRUE : Boolean.FALSE;
		} else {
			re = trueCaseStr.equalsIgnoreCase(cell) ? Boolean.TRUE
					: Boolean.FALSE;
		}
		if (!re) {
			if (caseSensitive) {
				if (!falseCaseStr.equals(cell)) {
					throw new ConversionException("Cann't parse value '"+cell+"' to java.lang.Boolean");
				}
			} else {
				if (!falseCaseStr.equalsIgnoreCase(cell)) {

				}
			}
		}
		return re;
	}

}

package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.converters.ConversionException;

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
		this("true", "false", false);
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(boolean.class) || clz.equals(Boolean.class);
	}

	/*
	 * in other case ,return false?FIXME
	 */
	@Override
	public Object fromString(String cell) {
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

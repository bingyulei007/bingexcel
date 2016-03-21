package com.bing.excel.converter.base;

import com.bing.excel.converter.AbstractFieldConvertor;

/**
 * 
 * @author shizhongtao
 *
 * @date 2016-3-21
 * Description:  thanks for Joe Walnes and David Blevins
 */
public final class BooleanFieldConverter extends AbstractFieldConvertor {
	 private final boolean caseSensitive;
	 private final String trueCaseStr;
	 private final String falseCaseStr;
	 
	 
	 
	 
	public BooleanFieldConverter(String trueCaseStr,String falseCaseStr,boolean caseSensitive) {
		this.caseSensitive=caseSensitive;
		this.trueCaseStr=trueCaseStr;
		this.falseCaseStr=falseCaseStr;
	}

	public BooleanFieldConverter() {
		this("true","false",false);
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(boolean.class) || clz.equals(Boolean.class);
	}

	/* 
	 * in other case  ,return false?FIXME
	 */
	@Override
	public Object fromString(String cell) {
		 if (caseSensitive) {
	            return trueCaseStr.equals(cell) ? Boolean.TRUE : Boolean.FALSE;
	        } else {
	            return trueCaseStr.equalsIgnoreCase(cell) ? Boolean.TRUE : Boolean.FALSE;
	        }
	}

}

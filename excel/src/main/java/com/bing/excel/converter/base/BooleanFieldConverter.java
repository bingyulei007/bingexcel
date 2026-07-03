package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.OutValue;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author shizhongtao
 * 
 * date 2016-3-21
 * Description: thanks for Joe Walnes and David Blevins
 */
public final class BooleanFieldConverter extends AbstractFieldConvertor {
	private final boolean caseSensitive;
	private final String trueCaseStr;
	private final String falseCaseStr;
	private final boolean defaultBooleanWords;

	/**
	 * @param trueCaseStr 为真时候的输入
	 * @param falseCaseStr 为家时候的输入
	 * @param caseSensitive 是不是忽略大小写
	 */
	public BooleanFieldConverter(String trueCaseStr, String falseCaseStr,
			boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
		this.trueCaseStr = trueCaseStr;
		this.falseCaseStr = falseCaseStr;
		this.defaultBooleanWords = false;
	}

	/**
	 * 默认的boolean类型转换器，支持true/false、on/off、yes/no、y/n、1/0字符的转换
	 */
	public BooleanFieldConverter() {
		this.caseSensitive = false;
		this.trueCaseStr = "TRUE";
		this.falseCaseStr = "FALSE";
		this.defaultBooleanWords = true;
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(boolean.class) || clz.equals(Boolean.class);
	}

	@Override
	public OutValue toObject(Object source, ConverterHandler converterHandler) {
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
		if (StringUtils.isBlank(cell)) {
			return null;
		}
		String valueText = cell.trim();
		if (defaultBooleanWords) {
			Boolean value = BooleanUtils.toBooleanObject(valueText);
			if (value == null) {
				throw new ConversionException("Cann't parse value '"+cell+"' to java.lang.Boolean");
			}
			return value;
		}
		Boolean re;
		if (caseSensitive) {
			re = trueCaseStr.equals(valueText) ? Boolean.TRUE : Boolean.FALSE;
		} else {
			re = trueCaseStr.equalsIgnoreCase(valueText) ? Boolean.TRUE
					: Boolean.FALSE;
		}
		if (!re) {
			if (caseSensitive) {
				if (!falseCaseStr.equals(valueText)) {
					throw new ConversionException("Cann't parse value '"+cell+"' to java.lang.Boolean");
				}
			} else {
				if (!falseCaseStr.equalsIgnoreCase(valueText)) {
					throw new ConversionException("Cann't parse value '"+cell+"' to java.lang.Boolean");
				}
			}
		}
		return re;
	}

}

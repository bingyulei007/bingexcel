package com.bing.excel.converter.base;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.vo.OutValue;

/**
 * @author shizhongtao
 * 
 * date 2016-3-21 Description:
 */
public final class DateFieldConverter extends AbstractFieldConvertor {

	private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String[] SMART_FORMATS = {DEFAULT_FORMAT, "yy-MM-dd HH:mm", "yy-MM-dd"};
	private final String inFormatStr;
	private final boolean smartConversion;

	public DateFieldConverter(boolean smartConversion) {
		this(DEFAULT_FORMAT, smartConversion);
	}

	public DateFieldConverter() {
		this(false);
	}

	public DateFieldConverter(String formats, boolean smartConversion) {
		this.inFormatStr = formats;
		this.smartConversion = smartConversion;
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(Date.class);
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.dateValue(source);
	}

	@Override
	public   Object  fromString(String cell,ConverterHandler converterHandler,Type targetType) {
		if (StringUtils.isBlank(cell)) {
			return null;
		}
		String temp = cell.trim();
		String[] formats = smartConversion ? SMART_FORMATS : new String[] {inFormatStr};
		ParseException parseException = null;
		for (String format : formats) {
			try {
				return getFormat(format).parse(temp);
			} catch (ParseException e) {
				parseException = e;
			}
		}
		throw new ConversionException("Cannot parse date: " + cell, parseException);
	}

	private SimpleDateFormat getFormat(String format) {
		return new SimpleDateFormat(format);
	}
}

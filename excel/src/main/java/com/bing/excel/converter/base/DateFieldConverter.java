package com.bing.excel.converter.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.DateUtil;


import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.exception.ConverterException;
import com.bing.utils.StringParseUtil;

/**
 * @author shizhongtao
 * 
 * @date 2016-3-21 Description:
 */
public final class DateFieldConverter extends AbstractFieldConvertor {
	
	private final static SimpleDateFormat DEFAULT_FORMATS = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final SimpleDateFormat inFormats;
	private final SimpleDateFormat outFormats;
	private final boolean smartConversion;

	public DateFieldConverter(boolean smartConversion) {
		this(DEFAULT_FORMATS, smartConversion);
	}

	public DateFieldConverter() {
		this(false);
	}

	public DateFieldConverter(SimpleDateFormat formats, boolean smartConversion) {
		this(formats, formats, smartConversion);
	}

	public DateFieldConverter(SimpleDateFormat inFormats,
			SimpleDateFormat outFormats, boolean smartConversion) {
		this.inFormats = inFormats;
		this.outFormats = outFormats;
		this.smartConversion = smartConversion;
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(Date.class);
	}

	@Override
	public Object fromString(String cell) {
		
		
		String temp;
		if (smartConversion) {
			try {
				return StringParseUtil.parseDate(cell);
			} catch (ParseException e) {
				throw new ConverterException("Cannot parse date" + cell, e);
			}

		} else {
			temp = cell;
		}
		Date date;
		try {
			date = inFormats.parse(temp);
			return date;
		} catch (ParseException e) {
			try {
				inFormats.applyPattern("yy-MM-dd HH:mm");
				date = inFormats.parse(temp);
				return date;
			} catch (ParseException e1) {
				try {
					inFormats.applyPattern("yy-MM-dd");
					date = inFormats.parse(temp);
					return date;
				} catch (ParseException e2) {
					throw new ConverterException("Cannot parse date" + cell, e2);
				}
			}

		}

	}

}

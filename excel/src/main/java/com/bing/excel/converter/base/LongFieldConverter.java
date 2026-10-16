package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.OutValue;
import org.apache.commons.lang3.StringUtils;

public final class LongFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		 return clz.equals(long.class) || clz.equals(Long.class);
	}

	/**
	 * 按纯十进制解析单元格文本为 long，不接受十六进制/八进制前缀
	 * （Excel 中 "012" 这类前导零编号应解析为 12，而非八进制的 10）。
	 */
	@Override
	public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
		if (StringUtils.isBlank(cell)) {
            return null;
        }
		try {
			return Long.parseLong(cell.trim());
		} catch (NumberFormatException e) {
			throw new ConversionException("Cannot parse value '" + cell + "' to Long", e);
		}
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.longValue(source);
	}

}

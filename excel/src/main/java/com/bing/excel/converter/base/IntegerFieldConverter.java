package com.bing.excel.converter.base;

import java.lang.reflect.Type;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.OutValue;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shizhongtao
 *
 * date 2016-3-10 Description:
 */
public final class IntegerFieldConverter extends AbstractFieldConvertor {

	@Override
	public boolean canConvert(Class<?> clz) {
		return clz.equals(int.class) || clz.equals(Integer.class);
	}

	/**
	 * 按纯十进制解析单元格文本为 int，不接受十六进制/八进制前缀
	 * （Excel 中 "012" 这类前导零编号应解析为 12，而非八进制的 10）。
	 */
	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Type targetType) {

		if (StringUtils.isBlank(cell)) {
			return null;
		}
		long value;
		try {
			value = Long.parseLong(cell.trim());
		} catch (NumberFormatException e) {
			throw new ConversionException("Cannot parse value '" + cell + "' to Integer", e);
		}
    	if(value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
    		throw new ConversionException("Value '" + cell + "' is out of range for Integer");
    	}
        return Integer.valueOf((int)value);
	}

	@Override
	public OutValue toObject(Object source,ConverterHandler converterHandler) {
		if(source==null){
			return null;
		}
		return OutValue.intValue(source);
	}

}

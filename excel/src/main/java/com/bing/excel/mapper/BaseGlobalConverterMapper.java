package com.bing.excel.mapper;

import com.bing.excel.converter.BooleanFieldConverter;
import com.bing.excel.converter.ByteFieldConverter;
import com.bing.excel.converter.CharFieldConverter;
import com.bing.excel.converter.Converter;
import com.bing.excel.converter.DateFieldConverter;
import com.bing.excel.converter.DoubleFieldConverter;
import com.bing.excel.converter.FloatFieldConverter;
import com.bing.excel.converter.IntegerFieldConverter;
import com.bing.excel.converter.LongFieldConverter;
import com.bing.excel.converter.NullFieldConverter;
import com.bing.excel.converter.ShortFieldConverter;
import com.bing.excel.converter.StringFieldConverter;
import com.google.common.collect.ImmutableMap;

public class BaseGlobalConverterMapper {
	static{
		ImmutableMap.Builder<Class<?>, Converter> builder=ImmutableMap.builder();
		builder.put(String.class,new StringFieldConverter());
		builder.put(Integer.class,new IntegerFieldConverter());
		builder.put(String.class,new LongFieldConverter());
		builder.put(String.class,new BooleanFieldConverter());
		builder.put(String.class,new ByteFieldConverter());
		builder.put(String.class,new CharFieldConverter());
		builder.put(String.class,new DateFieldConverter());
		builder.put(String.class,new DoubleFieldConverter());
		builder.put(String.class,new FloatFieldConverter());
		builder.put(String.class,new NullFieldConverter());
		builder.put(String.class,new ShortFieldConverter());
	}
}

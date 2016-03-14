package com.bing.excel.mapper;

import java.util.Date;

import com.bing.excel.converter.CharacterFieldConverter;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.converter.base.BooleanFieldConverter;
import com.bing.excel.converter.base.ByteFieldConverter;
import com.bing.excel.converter.base.DateFieldConverter;
import com.bing.excel.converter.base.DoubleFieldConverter;
import com.bing.excel.converter.base.FloatFieldConverter;
import com.bing.excel.converter.base.IntegerFieldConverter;
import com.bing.excel.converter.base.LongFieldConverter;
import com.bing.excel.converter.base.NullFieldConverter;
import com.bing.excel.converter.base.ShortFieldConverter;
import com.bing.excel.converter.base.StringFieldConverter;
import com.google.common.collect.ImmutableMap;

public class BaseGlobalConverterMapper {
	static ImmutableMap.Builder<Class<?>, FieldValueConverter>   builder;
	static{
		builder=ImmutableMap.builder();
		builder.put(String.class,new StringFieldConverter());
		builder.put(Integer.class,new IntegerFieldConverter());
		builder.put(Long.class,new LongFieldConverter());
		builder.put(Boolean.class,new BooleanFieldConverter());
		builder.put(Byte.class,new ByteFieldConverter());
		builder.put(Character.class,new CharacterFieldConverter());
		builder.put(Date.class,new DateFieldConverter());
		builder.put(Double.class,new DoubleFieldConverter());
		builder.put(Float.class,new FloatFieldConverter());
		builder.put(Short.class,new ShortFieldConverter());
	}
	public final static ImmutableMap<Class<?>, FieldValueConverter> globalMapper=builder.build();
	
}

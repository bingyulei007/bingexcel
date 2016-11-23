package com.chinamobile.excel.mapper;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;

import com.chinamobile.excel.converter.FieldValueConverter;
import com.chinamobile.excel.converter.base.BooleanFieldConverter;
import com.chinamobile.excel.converter.base.ByteFieldConverter;
import com.chinamobile.excel.converter.base.CharacterFieldConverter;
import com.chinamobile.excel.converter.base.DateFieldConverter;
import com.chinamobile.excel.converter.base.DoubleFieldConverter;
import com.chinamobile.excel.converter.base.FloatFieldConverter;
import com.chinamobile.excel.converter.base.IntegerFieldConverter;
import com.chinamobile.excel.converter.base.LongFieldConverter;
import com.chinamobile.excel.converter.base.ShortFieldConverter;
import com.chinamobile.excel.converter.base.StringFieldConverter;
import com.chinamobile.excel.converter.collections.ArrayConverter;
import com.chinamobile.excel.converter.collections.CollectionConverter;
import com.chinamobile.excel.converter.enums.EnumConVerter;
import com.google.common.collect.ImmutableMap;

/**
 * 默认的全局转换类，先静态吧，容我想想
 * @author shizhongtao
 *
 * @date 2016-3-19
 * Description:  
 */
public class BaseGlobalConverterMapper {
	static ImmutableMap.Builder<Class<?>, FieldValueConverter>   builder;
	static{
		builder=ImmutableMap.builder();
		builder.put(String.class,new StringFieldConverter());
		builder.put(Date.class,new DateFieldConverter());
		builder.put(Enum.class,new EnumConVerter());
		builder.put(Array.class,new ArrayConverter());
		//builder.put(Collections.class,new ArrayConverter());
		
		
		builder.put(Integer.class,new IntegerFieldConverter());
		builder.put(Long.class,new LongFieldConverter());
		builder.put(Boolean.class,new BooleanFieldConverter());
		builder.put(Byte.class,new ByteFieldConverter());
		builder.put(Character.class,new CharacterFieldConverter());
		builder.put(Double.class,new DoubleFieldConverter());
		builder.put(Float.class,new FloatFieldConverter());
		builder.put(Short.class,new ShortFieldConverter());
		builder.put(Collection.class,new CollectionConverter());
	}
	public final static ImmutableMap<Class<?>, FieldValueConverter> globalFieldConverterMapper=builder.build();
	
}

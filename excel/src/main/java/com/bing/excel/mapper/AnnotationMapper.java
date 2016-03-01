package com.bing.excel.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.converter.Converter;
import com.bing.excel.converter.ConverterMatcher;
import com.bing.excel.exception.InitializationException;
import com.bing.excel.exception.MissingCellConfigException;
import com.bing.utils.ReflectDependencyFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thoughtworks.xstream.mapper.LocalConversionMapper;

/**
 * 创建时间：2015-12-11下午8:33:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：AnnotationMapper.java 类说明：
 */
public class AnnotationMapper {

	private Cache<Class<?>, Map<List<Object>, Converter>> converterCache = null;

	private transient  ConversionMapper fieldMapper = new ConversionMapper();

	// private transient Object[] arguments;

	public AnnotationMapper() {
		converterCache = CacheBuilder.newBuilder().maximumSize(500)
				.expireAfterWrite(10, TimeUnit.MINUTES).build();
	}

	/**
	 * <p>
	 * Title: addConvertor
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param key
	 * @param cls
	 */
	public void addMapper(Field field) {
		CellConfig cellConfig = field.getAnnotation(CellConfig.class);
		BingConvertor bingConvertor = field.getAnnotation(BingConvertor.class);
		if (cellConfig == null) {
			throw new MissingCellConfigException("转化类实体配置错误");
		}
		Converter converter=null;
		if (bingConvertor != null) {
			Class<? extends Converter> value = bingConvertor.value();
			if (value != null) {
				try {
					converter = cacheConverter(bingConvertor,field.getType());
				} catch (ExecutionException e) {
					throw new InitializationException("No "
	                        + value
	                        + " available");
				}
			}
		}
		fieldMapper.registerLocalConverter(field.getDeclaringClass(), field.getName(), cellConfig.index(), field.getType(), converter);
	}


	private Converter cacheConverter(final BingConvertor annotation,
			final Class targetType) throws ExecutionException {
		Converter result = null;
		final Object[] args;
		final List<Object> parameter = new ArrayList<Object>();
	
		final List<Object> arrays = new ArrayList<Object>();
		arrays.add(annotation.booleans());
		arrays.add(annotation.bytes());
		arrays.add(annotation.chars());
		arrays.add(annotation.doubles());
		arrays.add(annotation.floats());
		arrays.add(annotation.ints());
		arrays.add(annotation.longs());
		arrays.add(annotation.shorts());
		arrays.add(annotation.strings());
		arrays.add(annotation.types());
		for (Object array : arrays) {
			if (array != null) {
				int length = Array.getLength(array);
				for (int i = 0; i < length; i++) {
					Object object = Array.get(array, i);
					if (!parameter.contains(object)) {
						parameter.add(object);
					}
				}
			}
		}
		final Class<? extends ConverterMatcher> converterType = annotation
				.value();
		Map<List<Object>, Converter> converterMapping = converterCache.get(
				converterType, new Callable<Map<List<Object>, Converter>>() {

					@Override
					public Map<List<Object>, Converter> call() throws Exception {

						Map<List<Object>, Converter> converterMappingTemp = new HashMap<List<Object>, Converter>();
						return converterMappingTemp;
					}

				});
		result = converterMapping.get(parameter);
		if (result == null) {
			int size = parameter.size();
			
			if (size > 0) {
				args = new Object[size];
				System.arraycopy(
						parameter.toArray(new Object[size]), 0,
						args, 0, size);
			} else {
				args = null;
			}
			final Converter converter;
			try {

				converter = (Converter) ReflectDependencyFactory
						.newInstance(converterType, args);
			} catch (final Exception e) {
				throw new InitializationException(
						"Cannot instantiate converter "
								+ converterType.getName()
								+ (targetType != null ? " for type "
										+ targetType.getName()
										: ""), e);
			}

			converterMapping.put(parameter, converter);
		}
		return result;
	}


}

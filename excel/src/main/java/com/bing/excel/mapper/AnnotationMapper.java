package com.bing.excel.mapper;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.convertor.Converter;
import com.bing.excel.convertor.ConverterMatcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 创建时间：2015-12-11下午8:33:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：AnnotationMapper.java 类说明：
 */
public class AnnotationMapper {

	private Cache<Class<?>, Map<List<Object>, Converter>> converterCache = null;

	private Map<String, Mapper> fieldMapper = new HashMap<>();
//	private transient Object[] arguments;

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
		Mapper mapper = new Mapper();
		if (cellConfig != null) {
			int index = cellConfig.index();
			mapper.setIndex(index);
			if (field.getType().isPrimitive()) {
				mapper.setPrimitive(true);

			}
		}
		BingConvertor bingConvertor = field.getAnnotation(BingConvertor.class);
		if (bingConvertor != null) {
			Class<? extends Converter> value = bingConvertor.value();
			if (value != null) {

			}
		}
		fieldMapper.put(field.getName(), mapper);
	}

	public Map<String, Mapper> getFieldMapper() {
		return fieldMapper;
	}

	private Converter cacheConverter(final BingConvertor annotation,
			final Class targetType) {
		Converter result = null;
		final Object[] args;
		final List<Object> parameter = new ArrayList<Object>();
		if (targetType != null) {
			parameter.add(targetType);
		}
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
		Map<List<Object>, Converter> converterMapping = converterCache
				.get(converterType, new Callable<Map<List<Object>,Converter>>() {

					@Override
					public Map<List<Object>, Converter> call() throws Exception {
						int size = parameter.size();
						if (size > 0) {
							args = new Object[ size];
							System.arraycopy(parameter.toArray(new Object[size]), 0, args,
									0, size);
						} else {
							args = null;
						}
						final Converter converter;
						
						
					}
				
				});
			result = converterMapping.get(parameter);
		if (result == null) {
			
			
			try {
				if (SingleValueConverter.class.isAssignableFrom(converterType)
						&& !Converter.class.isAssignableFrom(converterType)) {
					final SingleValueConverter svc = (SingleValueConverter) DependencyInjectionFactory
							.newInstance(converterType, args);
					converter = new SingleValueConverterWrapper(svc);
				} else {
					converter = (Converter) DependencyInjectionFactory
							.newInstance(converterType, args);
				}
			} catch (final Exception e) {
				throw new InitializationException(
						"Cannot instantiate converter "
								+ converterType.getName()
								+ (targetType != null ? " for type "
										+ targetType.getName() : ""), e);
			}
			if (converterMapping == null) {
				converterMapping = new HashMap<List<Object>, Converter>();
				converterCache.put(converterType, converterMapping);
			}
			converterMapping.put(parameter, converter);
			result = converter;
		}
		return result;
	}

	public static class Mapper {
		private int index;
		private Converter convertor;
		private boolean isPrimitive = false;

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public Converter getConvertor() {
			return convertor;
		}

		public void setConvertor(Converter convertor) {
			this.convertor = convertor;
		}

		public boolean isPrimitive() {
			return isPrimitive;
		}

		public void setPrimitive(boolean isPrimitive) {
			this.isPrimitive = isPrimitive;
		}

	}
}

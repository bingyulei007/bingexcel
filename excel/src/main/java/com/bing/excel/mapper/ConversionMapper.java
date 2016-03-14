package com.bing.excel.mapper;

import java.util.HashMap;
import java.util.Map;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.common.FieldRelation;

public class ConversionMapper {
	private final Map<FieldRelation, Mapper> fieldMapper = new HashMap<>();

	public ConversionMapper() {
	}

	public void registerLocalConverter(Class definedIn,
			String fieldName, int index, Class<?> targetType,
			FieldValueConverter converter) {
		registerLocalConverter(definedIn, fieldName, new Mapper(index,
				converter, targetType));
	}

	public void registerLocalConverter(Class definedIn,
			String fieldName, Mapper mapper) {
		fieldMapper.put(new FieldRelation(definedIn, fieldName), mapper);
	}

	public FieldValueConverter getLocalConverter(Class definedIn,
			String fieldName) {
		return fieldMapper.get(new FieldRelation(definedIn, fieldName))
				.getConverter();
	}

	public Mapper getLocalConverterMapper(Class definedIn,
			String fieldName) {
		return fieldMapper.get(new FieldRelation(definedIn, fieldName));
	}

	public static class Mapper {
		private int index;
		private boolean isPrimitive = true;
		private Class<?> clazz;
		private FieldValueConverter converter;

		public int getIndex() {
			return index;
		}

		public boolean isPrimitive() {
			return isPrimitive;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public FieldValueConverter getConverter() {
			return converter;
		}

		public Mapper(int index, FieldValueConverter converter, Class<?> clazz) {
			super();
			this.index = index;
			this.isPrimitive = clazz.isPrimitive();
			this.clazz = clazz;
			this.converter = converter;
		}

	}
}

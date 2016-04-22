package com.bing.excel.mapper;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.bing.excel.annotation.OutAlias;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.common.FieldRelation;
import com.bing.other.AnotationTest;
import com.google.common.base.Strings;

public class ConversionMapper {
	private final Map<FieldRelation, FieldConverterMapper> fieldMapper = new HashMap<>();
	private final Map<Class<?>, String> modelAlias = new HashMap<>();

	public ConversionMapper() {
	}

	public void registerLocalConverter(Class definedIn, String fieldName,
			int index, String alias,Class<?> fieldType,
			FieldValueConverter converter) {

		registerLocalConverter(definedIn, fieldName, new FieldConverterMapper(
				index, converter, alias, fieldType));
	}

	private void registerLocalConverter(Class definedIn, String fieldName,
			FieldConverterMapper mapper) {
		Annotation annotation = definedIn.getAnnotation(OutAlias.class);
		if (annotation != null) {
			String value = ((OutAlias) annotation).value();
			if (Strings.isNullOrEmpty(value)) {
				value = definedIn.getSimpleName();
			}
			modelAlias.put(definedIn, value);
		}

		fieldMapper.put(new FieldRelation(definedIn, fieldName), mapper);
	}

	public FieldValueConverter getLocalConverter(Class definedIn,
			String fieldName) {
		return fieldMapper.get(new FieldRelation(definedIn, fieldName))
				.getFieldConverter();
	}

	public FieldConverterMapper getLocalConverterMapper(Class definedIn,
			String fieldName) {
		return fieldMapper.get(new FieldRelation(definedIn, fieldName));
	}

	public String getModelName(Class<?> key) {
        return modelAlias.get(key);
	}

	public static class FieldConverterMapper {
		private int index;
		private boolean isPrimitive = true;
		private Class<?> clazz;
		private FieldValueConverter converter;
		private String alias;

		public int getIndex() {
			return index;
		}

		public boolean isPrimitive() {
			return isPrimitive;
		}

		public Class<?> getFieldClass() {
			return clazz;
		}

		public String getAlias() {
			return alias;
		}


		public FieldValueConverter getFieldConverter() {
			return converter;
		}

		public void setFieldConverter(FieldValueConverter converter) {
			this.converter = converter;
		}

		public FieldConverterMapper(int index, FieldValueConverter converter,
				String alias,  Class<?> clazz) {
			super();
			this.index = index;
			this.isPrimitive = clazz.isPrimitive();
			this.clazz = clazz;
			this.alias = alias;
			this.converter = converter;
		}

	}
}

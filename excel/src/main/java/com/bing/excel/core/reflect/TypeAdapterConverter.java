package com.bing.excel.core.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bing.excel.converter.HeaderReflectConverter;
import com.bing.excel.converter.ModelAdapter;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.exception.IllegalEntityException;
import com.bing.excel.mapper.ConversionMapper.FieldConverterMapper;
import com.bing.excel.mapper.ExcelConverterMapperHandler;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.ListRow;
import com.google.common.primitives.Primitives;

public class TypeAdapterConverter<T> implements ModelAdapter,
		HeaderReflectConverter {
	private final Constructor<T> constructor;
	/**
	 * 名称和
	 */
	private final Map<String, BoundField> boundFields;
	private final Class<T> clazz;
	private final ConverterHandler defaultLocalConverterHandler;

	public TypeAdapterConverter(Constructor<T> constructor,
			List<Field> tempConverterFields, ConverterHandler converterHandler) {
		Map<String, BoundField> boundFields = new HashMap<>();
		for (Field field : tempConverterFields) {
			String name = field.getName();
			boundFields.put(name, new BoundField(field, name));
		}

		this.constructor = constructor;
		this.boundFields = boundFields;
		defaultLocalConverterHandler = converterHandler;
		clazz = constructor.getDeclaringClass();
	}

	@Override
	public List<CellKV<String>> getHeader(ExcelConverterMapperHandler handler) {
		List<CellKV<String>> list = new ArrayList<>();
		for (Map.Entry<String, BoundField> kv : boundFields.entrySet()) {
			FieldConverterMapper fieldConverterMapper = handler
					.getLocalFieldConverterMapper(clazz, kv.getKey());
			list.add(new CellKV<String>(fieldConverterMapper.getIndex(),
					fieldConverterMapper.getAlias()));
		}
		return list;
	}

	@Override
	public ListLine marshal(Object source, ExcelConverterMapperHandler handler) {
		ListLine line=new ListLine();
		for (Map.Entry<String, BoundField> kv : boundFields.entrySet()) {
			FieldConverterMapper converterMapper = handler
					.getLocalFieldConverterMapper(clazz, kv.getKey());
			BoundField boundField = kv.getValue();
			if (converterMapper.getFieldConverter() == null) {

				setLocalConverter(converterMapper);
			}
			//TODO 写出部分 2016年4月17日21:37:36
			//line.addValue(converterMapper.hashCode(), value)
		}
		return line;
	}

	@Override
	public T unmarshal(ListRow source, ExcelConverterMapperHandler fieldHandler) {
		final Object obj;
		try {
			obj = constructor.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalEntityException(constructor.getName() + "构造实例失败",
					e);
		}
		String[] fullArray = source.toFullArray();
		int length = fullArray.length;

		if (length > 0) {
			for (Map.Entry<String, BoundField> kv : boundFields.entrySet()) {
				FieldConverterMapper converterMapper = fieldHandler
						.getLocalFieldConverterMapper(clazz, kv.getKey());
				BoundField boundField = kv.getValue();
				if (converterMapper.getFieldConverter() == null) {

					setLocalConverter(converterMapper);
				}

				int index = converterMapper.getIndex();
				String fieldValue = length > index ? fullArray[index] : null;
				boundField.initializeValue(obj, fieldValue, converterMapper);
			}
		}
		return (T) obj;
	}

	private void setLocalConverter(FieldConverterMapper converterMapper) {
		// it is not good for wrap clazz in this place
		Class<?> keyFieldType = converterMapper.isPrimitive() ? Primitives
				.wrap(converterMapper.getFieldClass()) : converterMapper
				.getFieldClass();
		FieldValueConverter fieldValueConverter = defaultLocalConverterHandler
				.getLocalConverter(keyFieldType);

		if (fieldValueConverter == null) {
			throw new IllegalEntityException(clazz,
					"can find the converter for fieldType ["
							+ converterMapper.getFieldClass() + "]");
		}
		converterMapper.setFieldConverter(fieldValueConverter);

	}

	private class BoundField {
		private final String name;
		private final Field field;

		public BoundField(Field field, String name) {
			this.field = field;
			this.name = name;
		}

		protected Object initializeValue(Object obj, String value,
				FieldConverterMapper converterMapper) {
			// field.set(obj, value);
			if (value != null) {
				if (converterMapper != null) {
					FieldValueConverter converter = converterMapper
							.getFieldConverter();
					if (converter == null) {
						throw new NullPointerException("the converter for ["
								+ name + "] is null");
					}
					boolean canConvert = converter.canConvert(converterMapper
							.getFieldClass());
					if (!canConvert) {
						throw new ConversionException(
								"the selected converter ["
										+ converter.getClass()
										+ "] cannot handle type ["
										+ converterMapper.getFieldClass() + "]");
					}

					Object fieldValue = converter.fromString(value,
							defaultLocalConverterHandler,
							converterMapper.getFieldClass());
					if (fieldValue != null) {
						try {
							field.set(obj, fieldValue);
						} catch (IllegalArgumentException
								| IllegalAccessException e) {
							throw new IllegalArgumentException(
									"It happened an error when set the value of the Entity !",
									e);
						}
					}

				} else {
					throw new NullPointerException("the converter for [" + name
							+ "] is null");
				}
			}
			return obj;
		}

	}
}

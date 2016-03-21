package com.bing.excel.core.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.bing.excel.converter.Converter;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.exception.IllegalEntityException;
import com.bing.excel.mapper.ConversionMapper.FieldConverterMapper;
import com.bing.excel.mapper.BaseGlobalConverterMapper;
import com.bing.excel.mapper.OrmMapper;
import com.bing.excel.reader.vo.CellKV;
import com.bing.excel.reader.vo.ListRow;
import com.google.common.primitives.Primitives;

public class TypeAdapterConverter<T> implements Converter {
	private final Constructor<T> constructor;
	/**
	 * 名称和
	 */
	private final Map<String, BoundField> boundFields;
	private final Class<T> clazz;
	private final Map<Class<?>, FieldValueConverter> defaultLocalConverter;

	public TypeAdapterConverter(Constructor<T> constructor,
			Map<String, BoundField> boundFields,
			Map<Class<?>, FieldValueConverter> converter) {
		this.constructor = constructor;
		this.boundFields = boundFields;
		defaultLocalConverter = converter;
		clazz = constructor.getDeclaringClass();
	}

	// public Object readToObject

	@Override
	public void marshal(Object source) {
		// TODO Auto-generated method stub

	}

	@Override
	public T unmarshal(ListRow source, OrmMapper ormMapper) {
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
				FieldConverterMapper converterMapper = ormMapper
						.getLocalFieldConverterMapper(clazz, kv.getKey());
				BoundField boundField = kv.getValue();
				if(converterMapper.getFieldConverter()==null){
					
					setLocalConverter(converterMapper);
				}
				
				int index = converterMapper.getIndex();
				String fieldValue = length > index  ? fullArray[index] : null;
				boundField.initializeValue(obj, fieldValue, converterMapper);
			}
		}
		return (T) obj;
	}

	private void setLocalConverter(FieldConverterMapper converterMapper) {
		//it is  not  good for wrap clazz in this place
		Class<?> keyFieldType=converterMapper.isPrimitive()?Primitives.wrap(converterMapper.getClazz()):converterMapper.getClazz();
		FieldValueConverter fieldValueConverter = defaultLocalConverter.get(keyFieldType);
		if(fieldValueConverter==null){
			final Class<?> keyType;
			if (keyFieldType.isEnum() || Enum.class.isAssignableFrom(keyFieldType)) {
				keyType=Enum.class;
			} else if(keyFieldType.isArray()){
				keyType=Array.class;
			}else if(Collection.class.isAssignableFrom(keyFieldType)){
				keyType=Collection.class;
			}else{
				keyType=keyFieldType;
			}
			fieldValueConverter=BaseGlobalConverterMapper.globalFieldConverterMapper.get(keyType);
			defaultLocalConverter.put(keyFieldType, fieldValueConverter);
		}
		if(fieldValueConverter==null){
			throw new IllegalEntityException(clazz, "can find the converter for fieldType ["+converterMapper.getClazz()+"]");
		}
		converterMapper.setFieldConverter(fieldValueConverter);
		
	}

}

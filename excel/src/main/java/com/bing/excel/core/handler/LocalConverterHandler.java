package com.bing.excel.core.handler;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.mapper.BaseGlobalConverterMapper;

import org.apache.commons.lang3.ClassUtils;

public class LocalConverterHandler implements ConverterHandler {
	private final Map<Class<?>, FieldValueConverter> defaultLocalConverter = new ConcurrentHashMap<>();


@Override
	public void registerConverter(Class<?> clazz,
			FieldValueConverter converter) {
		if (converter.canConvert(clazz)) {

			if (clazz.isPrimitive()) {
				defaultLocalConverter.put(ClassUtils.primitiveToWrapper(clazz), converter);
			} else {
				defaultLocalConverter.put(clazz, converter);
			}
		} else {
			throw new ConversionException("register converter for["
					+ clazz.getName() + "] failed!");
		}
	}

	
	@Override
	public FieldValueConverter getLocalConverter(Class<?> keyFieldType) {
		// computeIfAbsent 保证「查 local -> 查 global -> 回写 local」三步原子化，
		// 消除原先 get+put 复合操作在 synchronizedMap 上的竞态窗口。
		// globalFieldConverterMapper 是不可变 Map，重复回写同一 key 是幂等的。
		return defaultLocalConverter.computeIfAbsent(keyFieldType, k -> {
			final Class<?> keyType;
			if (k.isEnum() || Enum.class.isAssignableFrom(k)) {
				keyType = Enum.class;
			} else if (k.isArray()) {
				keyType = Array.class;
			} else if (Collection.class.isAssignableFrom(k)) {
				keyType = Collection.class;
			} else {
				keyType = k;
			}
			return BaseGlobalConverterMapper.globalFieldConverterMapper.get(keyType);
		});
	}
}

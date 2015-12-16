package com.bing.excel.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.bing.excel.convertor.Convertor;

/**
 * 创建时间：2015-12-11下午8:33:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：AnnotationMapper.java 类说明：
 */
public class AnnotationMapper {
	private Map<String, Class<? extends Convertor>> convertorMapper = new HashMap<>();
	private Map<String, Integer> fieldOrderMapper = new HashMap<>();
	private Map<String, String> fieldformatMapper = new HashMap<>();

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
		if (cellConfig != null) {
			int index = cellConfig.index();
			String format = cellConfig.format();
			fieldOrderMapper.put(field.getName(), index);
			if (StringUtils.isNoneEmpty(format)) {
				fieldformatMapper.put(field.getName(), format);
			}
		}
		BingConvertor bingConvertor = field.getAnnotation(BingConvertor.class);
		if (bingConvertor != null) {
			Class<? extends Convertor> value = bingConvertor.value();
			if (value != null) {
				convertorMapper.put(field.getName(), value);
			}
		}
	}

	/**
	 * <p>
	 * Title: getConvertorByKey
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param key
	 * @return null if not found
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Convertor getConvertorByKey(String key) throws InstantiationException, IllegalAccessException {
		Class<? extends Convertor> clz = convertorMapper.get(key);
		if (clz != null) {

			return clz.newInstance();
		}
		return null;
	}

	public boolean containConvertor(String key) {
		return convertorMapper.containsKey(key);
	}

	public Map<String, Class<? extends Convertor>> getConvertorMapper() {
		return convertorMapper;
	}

	public Map<String, Integer> getFieldOrderMapper() {
		return fieldOrderMapper;
	}

	public Map<String, String> getFieldformatMapper() {
		return fieldformatMapper;
	}
	
}

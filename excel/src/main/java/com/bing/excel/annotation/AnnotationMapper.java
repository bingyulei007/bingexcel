package com.bing.excel.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	private Map<String, Mapper> fieldMapper = new HashMap<>();

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
		Mapper mapper=new Mapper();
		if (cellConfig != null) {
			int index = cellConfig.index();
			mapper.setIndex(index);
			if(field.getType().equals(String.class)){
				String format = cellConfig.format();
				if (StringUtils.isNoneEmpty(format)) {
					mapper.setFormat(format);
				}
				
			}
		}
		BingConvertor bingConvertor = field.getAnnotation(BingConvertor.class);
		if (bingConvertor != null) {
			Class<? extends Convertor> value = bingConvertor.value();
			if (value != null) {
				mapper.setConvertor(value);
			}
		}
		fieldMapper.put(field.getName(), mapper);
	}

	
	public Map<String, Mapper> getFieldMapper() {
		return fieldMapper;
	}


	private class Mapper{
		private int index;
		private  Class<? extends Convertor> convertor;
		private String format;
		
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
		public Class<? extends Convertor> getConvertor() {
			return convertor;
		}
		public void setConvertor(Class<? extends Convertor> convertor) {
			this.convertor = convertor;
		}
		public String getFormat() {
			return format;
		}
		public void setFormat(String format) {
			this.format = format;
		}
		
		
	}
}

package com.bing.excel.core.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.bing.excel.annotation.AnnotationMapper;
import com.bing.excel.annotation.AnnotationMapper.Mapper;
import com.bing.excel.exception.ConvertorException;
import com.bing.excel.reader.vo.ListRow;

/**
 * 创建时间：2015-12-8下午7:08:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingSheetHandler.java 类说明： 处理Sheet辅助类
 */
public class UnmarshallHandler  implements Handler  {
	private Class<?> defaultClass;
	private AnnotationMapper mapper;


	public Object ReadToEntity(ListRow row) {
		for (int j = fromRow; j <= sheet.getLastRowNum(); j++) {
			Row r = sheet.getRow(j);
			if (r == null) {
				// This whole row is empty
				// Handle it as needed
				continue;
			}
			
			T instance;
			try {
				instance = defaultClass.newInstance();
			} catch (Exception e) {
				throw new ConvertorException("构造实例失败，请确认构造方法等", e);
			}
			Map<String, Mapper> fieldMapper = mapper.getFieldMapper();
			for (Map.Entry<String, Mapper> entity : fieldMapper.entrySet()) {
				String key = entity.getKey();
//				ent
			}
		/*	Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
			if (c == null) {

			} else {

			}*/
			
			
		}
		return null;
	}

	/*
	 * 注册转换类型
	 */
	@Override
	public void process(Class<?> clazz) {
		if (null == clazz) {
			throw new NullPointerException("获得的实体类型为空");
		}
		defaultClass = clazz;
		mapper = new AnnotationMapper();
		Field[] declaredFields = clazz.getDeclaredFields();
		for (Field field : declaredFields) {
			mapper.addMapper(field);
		}
	}


}

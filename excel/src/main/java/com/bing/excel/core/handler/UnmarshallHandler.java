package com.bing.excel.core.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.bing.excel.annotation.AnnotationMapper;
import com.bing.excel.exception.ConvertorException;

/**
 * 创建时间：2015-12-8下午7:08:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingSheetHandler.java 类说明： 处理Sheet辅助类
 */
public class UnmarshallHandler<T> implements Handler<T> {
	private Class<T> defaultClass;
	private Sheet sheet;
	private AnnotationMapper mapper;
	private List<T> list = new ArrayList<>();

	public UnmarshallHandler(Sheet sheet) {
		this.sheet = sheet;
	}

	public List<T> ReadToList(int fromRow) {
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
			
		/*	Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
			if (c == null) {

			} else {

			}*/
			
			
		}
		return list;
	}

	/*
	 * 注册转换类型
	 */
	@Override
	public void process(Class<T> clazz) {
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

	public List<T> getResult() {
		return this.list;
	}

}

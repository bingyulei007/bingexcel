package com.bing.excel.core.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.ExcelBing;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.ExcelBingImpl.SheetVo;
import com.bing.excel.core.reflect.BoundField;
import com.bing.excel.core.reflect.TypeAdapterConverter;
import com.bing.excel.exception.IllegalEntityException;
import com.bing.excel.mapper.AnnotationMapper;
import com.bing.excel.mapper.OrmMapper;
import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.SaxHandler;
import com.bing.excel.reader.vo.ListRow;

/**
 * 创建时间：2015-12-8上午11:56:30 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingExcelImpl.java 类说明：
 */
public class ExcelBingImpl implements ExcelBing {

	private final Map<Class<?>, TypeAdapterConverter<?>> typeTokenCache = new HashMap<Class<?>, TypeAdapterConverter<?>>();
	private final Set<Class<?>> targetTypes = Collections
			.synchronizedSet(new HashSet<Class<?>>());
	private OrmMapper ormMapper = new AnnotationMapper();

	@Override
	public <T> SheetVo<T> readSheet(File file, Class<T> clazz, int startRowNum)
			throws Exception {
		return readSheet(file, new ReaderCondition<T>(0, startRowNum, clazz));
	}

	@Override
	public <T> SheetVo<T> readSheet(File file, ReaderCondition<T> condition)
			throws Exception {
		ReaderCondition[] arr = new ReaderCondition[] { condition };

		return readSheetsToList(file, arr).get(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.core.ExcelBing#readSheetsToList(java.io.File,
	 * com.bing.excel.core.ReaderCondition[])
	 */
	@Override
	public List<SheetVo> readSheetsToList(File file,
			ReaderCondition[] conditions) throws Exception {
		BingExcelReaderListener listner = new BingExcelReaderListener(
				conditions);
		SaxHandler handler = ExcelReaderFactory.create(file, listner, true);
		int[] indexArr = new int[conditions.length];
		int minNum = 0;
		for (int i = 0; i < conditions.length; i++) {
			int sheetNum = conditions[i].getSheetIndex();
			indexArr[i] = sheetNum;
			if (minNum > conditions[i].getEndRow()) {
				minNum = conditions[i].getEndRow();
			}
		}
		handler.readSheet(indexArr, minNum);
		return null;
	}

	@Override
	public <T> List<SheetVo<T>> readSheetsToList(File file, Class<T> clazz,
			int startRowNum) {
		return null;
	}

	@Override
	public <T> SheetVo<T> readStream(InputStream stream,
			ReaderCondition<T> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SheetVo> readStreamToList(InputStream stream,
			ReaderCondition[] condition) {
		return null;
	}

	@Override
	public <T> List<SheetVo<T>> readStreamToList(InputStream stream,
			ReaderCondition<T> condition) {
		// TODO Auto-generated method stub
		return null;
	}

	public void registerConvertor(FieldValueConverter convertor) {
		// 如果是基本类型，
	}

	private class BingExcelReaderListener extends AbstractExcelReadListener {

		private final ReaderCondition[] conditions;
		private Class tagertClazz = null;
		private List<SheetVo> list;
		private SheetVo currentSheetVo;

		public BingExcelReaderListener(ReaderCondition[] conditions) {
			super();
			this.conditions = conditions;
			Class[] arr = new Class[conditions.length];
			for (int i = 0; i < conditions.length; i++) {
				Class targetClazz = conditions[i].getTargetClazz();
				arr[i] = targetClazz;
			}
			ormMapper.processAnnotations(arr);
		}

		@Override
		public void optRow(int curRow, ListRow rowList) {
			if (tagertClazz != null) {
				TypeAdapterConverter<?> typeAdapter = typeTokenCache
						.get(tagertClazz);
				if (typeAdapter == null) {
					if (targetTypes.contains(tagertClazz)) {
						throw new IllegalEntityException(tagertClazz, "类型定义错误");
					} else {
						throw new NullPointerException("没有对应的适配器，无法转换");
					}
				} else {
					Object object = typeAdapter.unmarshal(rowList, ormMapper);
					currentSheetVo.addObject(object);
				}

			}
		}

		@Override
		public void startSheet(int sheetIndex, String name) {

			tagertClazz = null;
			for (int i = 0; i < conditions.length; i++) {
				if (conditions[i].getSheetIndex() == sheetIndex) {
					tagertClazz = conditions[i].getTargetClazz();
					registeAdapter(tagertClazz);
					currentSheetVo=new SheetVo<>(sheetIndex, name);
					break;
				}
			}
		}

		private void registeAdapter(Class type) {

			synchronized (type) {
				if (targetTypes.contains(type)) {
					return;
				}
				try {
					// 转换的类型不可能对应的是基本类型
					if (type.isPrimitive()) {
						return;
					}
					// 目前先不考虑model的接口继承问题 TODO
					if (type.isInterface()
							|| (type.getModifiers() & Modifier.ABSTRACT) > 0) {
						return;
					}
					final Field[] fields = type.getDeclaredFields();
					List<Field> tempConverterFields = new LinkedList<>();
					for (int i = 0; i < fields.length; i++) {
						final Field field = fields[i];

						if (field.isEnumConstant()
								|| (field.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) > 0) {
							continue;
						}
						// 应该不会出现
						if (field.isSynthetic()) {
							continue;
						}
						tempConverterFields.add(field);

					}
					Constructor<?> constructor;
					try {
						constructor = type.getDeclaredConstructor();
					} catch (NoSuchMethodException | SecurityException e) {
						throw new IllegalEntityException(type, "获取无参构造函数失败");
					}
					TypeAdapterConverter typeAdapterConverter = getTypeAdapterConverter(
							constructor, tempConverterFields);
					typeTokenCache.put(type, typeAdapterConverter);

				} finally {
					targetTypes.add(type);
				}

			}

		}

		private TypeAdapterConverter getTypeAdapterConverter(
				Constructor<?> constructor, List<Field> tempConverterFields) {
			Map<String, BoundField> boundFields = new HashMap<>();
			TypeAdapterConverter adConverter = new TypeAdapterConverter<>(
					constructor, boundFields);
			for (Field field : tempConverterFields) {
				String name = field.getName();
				boundFields.put(name, new BoundField(field, name));
			}
			return adConverter;
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
		}

		@Override
		public void endWorkBook() {
		}

	}

	public static class SheetVo<E> {
		private int sheetIndex;
		private String sheetName;
		private List<E> list=new LinkedList<>();

		public SheetVo(int sheetIndex, String sheetName) {
			super();
			this.sheetIndex = sheetIndex;
			this.sheetName = sheetName;
		}

		public int getSheetIndex() {
			return sheetIndex;
		}

		public String getSheetName() {
			return sheetName;
		}

		public List<E> getObjectList() {
			return list;
		}

		void addObject(E obj) {
			this.list.add(obj);
		}

	}

}

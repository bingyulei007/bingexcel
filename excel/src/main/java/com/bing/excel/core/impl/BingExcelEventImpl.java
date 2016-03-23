package com.bing.excel.core.impl;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.BingExcelEvent;
import com.bing.excel.core.BingReadListener;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.reflect.BoundField;
import com.bing.excel.core.reflect.TypeAdapterConverter;
import com.bing.excel.exception.IllegalEntityException;
import com.bing.excel.mapper.AnnotationMapper;
import com.bing.excel.mapper.OrmMapper;
import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.SaxHandler;
import com.bing.excel.reader.vo.ListRow;
import com.google.common.base.MoreObjects;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.*;

/**
 * 创建时间：2015-12-8上午11:56:30 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingExcelImpl.java 类说明：
 */
public class BingExcelEventImpl implements BingExcelEvent {

	/**
	 * model entity Converter,the relationship is sheet-to-entity
	 */
	private final Map<Class<?>, TypeAdapterConverter<?>> typeTokenCache = new HashMap<Class<?>, TypeAdapterConverter<?>>();
	/**
	 * globe filed converter
	 */
	private final Map<Class<?>, FieldValueConverter> defaultLocalConverter;
	private final Set<Class<?>> targetTypes = Collections
			.synchronizedSet(new HashSet<Class<?>>());
	private OrmMapper ormMapper = new AnnotationMapper();
	private BingReadListener listener = null;

	public BingExcelEventImpl(
			Map<Class<?>, FieldValueConverter> defaultLocalConverter) {
		this.defaultLocalConverter = defaultLocalConverter;
	}

	public BingExcelEventImpl() {
		this.defaultLocalConverter = Collections
				.synchronizedMap(new HashMap<Class<?>, FieldValueConverter>());
	}

	@Override
	public <T> void readFile(File file, Class<T> clazz, int startRowNum,
			BingReadListener listener) throws Exception {
		readFile(file, new ReaderCondition<T>(0, startRowNum, clazz), listener);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> void readFile(File file, ReaderCondition<T> condition,
			BingReadListener listener) throws Exception {

		ReaderCondition[] arr = new ReaderCondition[] { condition };
		readFileToList(file, arr, listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.core.ExcelBing#readSheetsToList(java.io.File,
	 * com.bing.excel.core.ReaderCondition[])
	 */
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void readFileToList(File file, ReaderCondition[] conditions,
			BingReadListener listener) throws Exception {
		this.listener = listener;
		BingExcelReaderListener excelListener = new BingExcelReaderListener(
				conditions);
		SaxHandler handler = ExcelReaderFactory.create(file, excelListener,
				true);
		int[] indexArr = new int[conditions.length];
		int minNum = -1;
		for (int i = 0; i < conditions.length; i++) {
			int sheetNum = conditions[i].getSheetIndex();
			indexArr[i] = sheetNum;
			if (minNum == -1) {
				minNum = conditions[i].getEndRow();
			} else if (minNum > conditions[i].getEndRow()) {
				minNum = conditions[i].getEndRow();
			}
		}
		handler.readSheet(indexArr, minNum);
	}

	@Override
	public <T> void readStream(InputStream stream, Class<T> clazz,
			int startRowNum, BingReadListener listener) throws IOException,
			SQLException, OpenXML4JException, SAXException {
		readStream(stream, new ReaderCondition<T>(0, startRowNum, clazz),
				listener);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T> void readStream(InputStream stream,
			ReaderCondition<T> condition, BingReadListener listener)
			throws IOException, SQLException, OpenXML4JException, SAXException {
		ReaderCondition[] arr = new ReaderCondition[] { condition };
		readStreamToList(stream, arr, listener);
	}

	@Override
	public void readStreamToList(InputStream stream,
			ReaderCondition[] conditions, BingReadListener listener)
			throws IOException, SQLException, OpenXML4JException, SAXException {
		this.listener = listener;
		BingExcelReaderListener listner = new BingExcelReaderListener(
				conditions);
		SaxHandler handler = ExcelReaderFactory.create(stream, listner, true);
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
	}

	private class BingExcelReaderListener extends AbstractExcelReadListener {

		private final ReaderCondition[] conditions;
		private Class tagertClazz = null;
		private int startRow = 0;// start to read from first lines;
		private ModelInfo modelInfo;

		public BingExcelReaderListener(ReaderCondition[] conditions) {
			super();
			this.conditions = conditions;
			Class[] arr = new Class[conditions.length];
			for (int i = 0; i < conditions.length; i++) {
				arr[i] = conditions[i].getTargetClazz();
			}
			ormMapper.processAnnotations(arr);
		}

		@Override
		public void optRow(int curRow, ListRow rowList) {
			if (curRow < startRow) {
				return;
			}
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
					modelInfo.setRow(curRow);
					listener.writeModel(object, modelInfo);
				}

			}
		}

		@Override
		public void startSheet(int sheetIndex, String name) {

			tagertClazz = null;
			startRow = 0;
			for (int i = 0; i < conditions.length; i++) {
				if (conditions[i].getSheetIndex() == sheetIndex) {
					tagertClazz = conditions[i].getTargetClazz();
					registeAdapter(tagertClazz);
					startRow = conditions[i].getStartRow();
					modelInfo = new ModelInfo(sheetIndex, name);
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
						field.setAccessible(true);
						tempConverterFields.add(field);

					}
					Constructor<?> constructor;
					try {
						constructor = type.getDeclaredConstructor();
					} catch (NoSuchMethodException | SecurityException e) {
						throw new IllegalEntityException(type,
								"Gets the default constructor failed");
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
			for (Field field : tempConverterFields) {
				String name = field.getName();
				boundFields.put(name, new BoundField(field, name));
			}
			TypeAdapterConverter adConverter = new TypeAdapterConverter<>(
					constructor, boundFields, defaultLocalConverter);
			return adConverter;
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
			this.modelInfo = null;
		}

		@Override
		public void endWorkBook() {

		}

	}

	public static class ModelInfo {
		private int sheetIndex;
		private String sheetName;
		private Integer row;

		protected ModelInfo() {

		}

		protected ModelInfo(int sheetIndex, String sheetName) {
			this.sheetIndex = sheetIndex;
			this.sheetName = sheetName;
		}

		ModelInfo getCopy() {
			ModelInfo newOne = new ModelInfo(this.sheetIndex, this.sheetName);
			newOne.setRow(this.row);
			return newOne;
		}

		public int getSheetIndex() {
			return sheetIndex;
		}

		void setSheetIndex(int sheetIndex) {
			this.sheetIndex = sheetIndex;
		}

		public String getSheetName() {
			return sheetName;
		}

		void setSheetName(String sheetName) {
			this.sheetName = sheetName;
		}

		public int getRow() {
			return row;
		}

		void setRow(int row) {
			this.row = row;
		}

		@Override
		public String toString() {

			return MoreObjects.toStringHelper(getClass()).omitNullValues()
					.add("sheetName", sheetName)
					.add("sheetIndex", sheetIndex)
					.add("row", row)
					.toString();
		}

	}

}

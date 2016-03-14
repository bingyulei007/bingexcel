package com.bing.excel.core.impl;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.ExcelBing;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.ExcelBingImpl.SheetVo;
import com.bing.excel.mapper.AnnotationMapper;
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
public  class ExcelBingImpl implements ExcelBing {

	
	private AnnotationMapper annotationMapper=new AnnotationMapper();
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
		int minNum=0;
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

	private  class BingExcelReaderListener extends
			AbstractExcelReadListener {

		private final ReaderCondition[] conditions;
		private Class tagertClazz = null;
		private List<SheetVo> list;
		public BingExcelReaderListener(ReaderCondition[] conditions) {
			super();
			this.conditions = conditions;
			Class[] arr=new Class[conditions.length];
			for (int i = 0; i < conditions.length; i++) {
				Class targetClazz = conditions[i].getTargetClazz();
				arr[i]=targetClazz;
			}
			annotationMapper.processAnnotations(arr);
		}

		@Override
		public void optRow(int curRow, ListRow rowList) {
			if(tagertClazz!=null){
				
			}
		}

		@Override
		public void startSheet(int sheetIndex, String name) {
			tagertClazz=null;
			for (int i = 0; i < conditions.length; i++) {
				if (conditions[i].getSheetIndex() == sheetIndex) {
					tagertClazz = conditions[i].getTargetClazz();
					break;
				}
			}
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
		private List<E> list;

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

		public List<E> getList() {
			return list;
		}

		void setList(List<E> list) {
			this.list = list;
		}

	}

}

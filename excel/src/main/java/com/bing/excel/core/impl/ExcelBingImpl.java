package com.bing.excel.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.bing.excel.convertor.Convertor;
import com.bing.excel.core.ExcelBing;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.handler.UnmarshallHandler;
import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.vo.ListRow;


/**  
 * 创建时间：2015-12-8上午11:56:30  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：BingExcelImpl.java  
 * 类说明：  
 */
public  abstract class ExcelBingImpl implements ExcelBing {
	private Set<Convertor> globalConvertor;
	
	
	
	@Override
	public <T> List<T> readFileToList(File file, Class<T> clazz, int startRowNum) {
		return readFileToList(file,clazz,(new ReaderCondition()).startRow(startRowNum));
	}
	@Override
	public <T> List<T> readFileToList(File file, Class<T> clazz,
			ReaderCondition condition) {
		return null;
	}
	@Override
	public List[] readFileToList(File file, Class[] clazzArr,
			int[] sheetIndexArr, int startRowNum) {
		return null;
	}
	@Override
	public <T> List<T> readStreamToList(InputStream stream, Class<T> clazz,
			int startRowNum) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T> List<T> readStreamToList(InputStream stream, Class<T> clazz,
			ReaderCondition condition) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List[] readStreamToList(InputStream stream, Class[] clazzArr,
			int[] sheetIndexArr, int startRowNum) {
		// TODO Auto-generated method stub
		return null;
	}
	public void registerConvertor(Convertor convertor){
		//如果是基本类型，
	}
	public static class BingExcelReaderListener extends AbstractExcelReadListener {

		@Override
		public void optRow(int curRow, ListRow rowList) {
		}

		@Override
		public void startSheet(int sheetIndex, String name) {
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
		}

		@Override
		public void endWorkBook() {
		}

	}
}

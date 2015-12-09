package com.bing.excel.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.bing.excel.BingExcel;

/**  
 * 创建时间：2015-12-8上午11:56:30  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：BingExcelImpl.java  
 * 类说明：  
 */
public class BingExcelImpl implements BingExcel {
	@Override
	public <T> List<T> readFileToList(File file, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException {
		
		return readFileToList(file, clazz, 0, startRowNum);
	}

	@Override
	public <T> List<T> readFileToList(File file, Class<T> clazz, int sheetIndex, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException {
		Workbook workbook = WorkbookFactory.create(file);
		return null;
	}

	@Override
	public <T> List<T> readStreamToList(InputStream stream, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException {
		return readStreamToList(stream, 0, clazz, startRowNum);
	}

	@Override
	public <T> List<T> readStreamToList(InputStream stream, int sheetIndex, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException {
		Workbook workbook = WorkbookFactory.create(stream);
		Sheet sheetAt = workbook.getSheetAt(sheetIndex);
		return null;
	}
	
}

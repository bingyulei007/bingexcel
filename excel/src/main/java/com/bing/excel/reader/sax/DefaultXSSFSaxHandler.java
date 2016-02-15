package com.bing.excel.reader.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bing.excel.exception.BingSaxReadStopException;
import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.reader.SaxHandler;
import com.bing.excel.reader.sax.ExcelXSSFSheetXMLHandler.BingSheetContentsHandler;
import com.bing.excel.reader.vo.CellKV;

/**
 * @author shizhongtao
 * 
 * @date 2016-2-2 Description:
 *       读取07excel的sax方法，解析器默认使用org.apache.xerces.parsers.SAXParser
 *       。可以痛痛set方法手动设置
 */
public class DefaultXSSFSaxHandler implements SaxHandler {
	public final static Logger logger = LoggerFactory
			.getLogger(DefaultXSSFSaxHandler.class);
	private OPCPackage pkg;
	private XMLReader parser;
	private ExcelReadListener excelReader;
	private boolean ignoreNumFormat = false;
	private DefaultSheetContentsHandler handler ;
	public DefaultXSSFSaxHandler(String path, ExcelReadListener rowReader)
			throws InvalidFormatException, IOException {
		this(path, rowReader, false);
	}

	public DefaultXSSFSaxHandler(File file, ExcelReadListener rowReader)
			throws InvalidFormatException, IOException {
		this(file, rowReader, false);
	}

	public DefaultXSSFSaxHandler(InputStream in, ExcelReadListener rowReader)
			throws InvalidFormatException, IOException {
		this(in, rowReader, false);
	}

	public DefaultXSSFSaxHandler(String path, ExcelReadListener rowReader,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		
		this(OPCPackage.open(path, PackageAccess.READ),rowReader,ignoreNumFormat);
		
	}

	public DefaultXSSFSaxHandler(File file, ExcelReadListener rowReader,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		
		this(OPCPackage.open(file, PackageAccess.READ),rowReader,ignoreNumFormat);
	}

	public DefaultXSSFSaxHandler(InputStream in, ExcelReadListener rowReader,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		
		this(OPCPackage.open(in),rowReader,ignoreNumFormat);
	}
	public DefaultXSSFSaxHandler(OPCPackage pkg, ExcelReadListener rowReader,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		
		this.pkg = pkg;
		this.excelReader = rowReader;
		this.ignoreNumFormat = ignoreNumFormat;
		this.handler = new DefaultSheetContentsHandler(
				excelReader);
	}

	@Override
	public void readSheets() throws IOException, OpenXML4JException, SAXException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage 对象为空");
		}
		XSSFReader xssfReader = new XSSFReader(pkg);
		XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
				pkg);
		int sheetIndex = 0;
		
		ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
				xssfReader.getStylesTable(), strings, handler, false);
		// 是不按照格式化输出字符
		sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
		getParser().setContentHandler(sheetXMLHandler);
		while (sheets.hasNext()) {
			try (InputStream sheet = sheets.next()) {
				String name = sheets.getSheetName();
				excelReader.startSheet(sheetIndex, name);
				InputSource sheetSource = new InputSource(sheet);
				logger.debug("读取07excel第{}个sheet,名称为{}", sheetIndex, name);
				try {
					getParser().parse(sheetSource);
				} catch (SAXException e) {
					if (e instanceof BingSaxReadStopException) {
						logger.warn("SaxRead 方式通过抛出stop异常结束");
					} else {
						throw e;
					}
				}
				excelReader.endSheet(sheetIndex, name);
				sheetIndex++;
			}
		}
		excelReader.endWorkBook();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.reader.SaxHandler#process(int)
	 */
	@Override
	public void readSheet(int index) throws IOException, OpenXML4JException,
			SAXException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage 对象为空");
		}
		XSSFReader xssfReader = new XSSFReader(pkg);
		XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
				pkg);
		int sheetIndex = 0;
		
		ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
				xssfReader.getStylesTable(), strings, handler, false);
		// 是不按照格式化输出字符
		sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
		getParser().setContentHandler(sheetXMLHandler);
		while (sheets.hasNext()) {
			try (InputStream sheet = sheets.next()) {
				String name = sheets.getSheetName();

				if (sheetIndex != index) {
					sheetIndex++;
					continue;
				}
				sheetIndex++;
				excelReader.startSheet(sheetIndex, name);
				InputSource sheetSource = new InputSource(sheet);
				logger.debug("读取07excel第{}个sheet,名称为{}", sheetIndex, name);
				try {
					getParser().parse(sheetSource);
				} catch (SAXException e) {
					if (e instanceof BingSaxReadStopException) {
						logger.warn("SaxRead 方式通过抛出stop异常结束");
					} else {
						throw e;
					}
				}
				excelReader.endSheet(sheetIndex, name);
				break;
			}
		}
		excelReader.endWorkBook();
	}

	@Override
	public void readSheet(String sheetName) throws IOException, SAXException,
			OpenXML4JException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage 对象为空");
		}
		XSSFReader xssfReader = new XSSFReader(pkg);
		XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
				pkg);
		int sheetIndex = 0;
		
		ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
				xssfReader.getStylesTable(), strings, handler, false);
		// 是不按照格式化输出字符
		sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
		getParser().setContentHandler(sheetXMLHandler);
		while (sheets.hasNext()) {
			try (InputStream sheet = sheets.next()) {
				String name = sheets.getSheetName();
				sheetIndex++;
				if (!name.equals(sheetName)) {
					continue;
				}
				excelReader.startSheet(sheetIndex, name);
				InputSource sheetSource = new InputSource(sheet);
				logger.debug("读取07excel第{}个sheet,名称为{}", sheetIndex, sheetName);
				try {
					getParser().parse(sheetSource);
				} catch (SAXException e) {
					if (e instanceof BingSaxReadStopException) {
						logger.warn("SaxRead 方式通过抛出stop异常结束");
					} else {
						throw e;
					}
				}
				excelReader.endSheet(sheetIndex, name);
				break;
			}
		}
	}

	public XMLReader getParser() throws SAXException {
		if (parser == null) {
			parser = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");
		}
		return parser;
	}

	public void setParser(XMLReader parser) {
		this.parser = parser;
	}

	public void setMaxReturnLine(int num) {
		this.handler.setMaxReadLine(num);
	}

	private static class DefaultSheetContentsHandler implements
			BingSheetContentsHandler {
		private List<CellKV> rowList;
		private ExcelReadListener rowReader;
		private int maxReadLine = Integer.MAX_VALUE;

		public DefaultSheetContentsHandler(ExcelReadListener rowReader) {
			this.rowReader = rowReader;
		}

		/**
		 * Converts an Excel column name like "C" to a zero-based index.
		 * 
		 * @param name
		 * @return Index corresponding to the specified name
		 */
		private int nameToColumn(String name) {
			int firstDigit = -1;
			char[] array = name.toCharArray();
			for (int c = 0; c < array.length; ++c) {
				if (Character.isDigit(name.charAt(c))) {
					firstDigit = c;
					break;
				}
			}

			int column = -1;
			for (int i = 0; i < firstDigit; ++i) {
				int c = array[i];
				column = (column + 1) * 26 + c - 'A';
			}
			return column;
		}

		@Override
		public void startRow(int rowNum) throws BingSaxReadStopException {
			if (rowNum > maxReadLine) {
				throw new BingSaxReadStopException("stop mark");
			}
			rowList = new ArrayList<>();
		}

		@Override
		public void endRow(int rowNum) {

			rowReader.optRow(rowNum, rowList);
		}

		@Override
		public void cell(int rowNum, String cellReference,
				String formattedValue, XSSFComment comment)
				 {
			
			int column = nameToColumn(cellReference);

			rowList.add(new CellKV(column, formattedValue));
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
		}

		public void setMaxReadLine(int maxReadLine) {
			if (maxReadLine > 0){
				this.maxReadLine = maxReadLine-1;
			}
		}

	}

	
}

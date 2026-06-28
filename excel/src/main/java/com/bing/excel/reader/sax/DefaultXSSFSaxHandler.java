package com.bing.excel.reader.sax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bing.excel.exception.BingSaxReadStopException;
import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.vo.CellKV;
import com.bing.excel.reader.ReadHandler;
import com.bing.excel.reader.sax.ExcelXSSFSheetXMLHandler.BingSheetContentsHandler;
import com.bing.excel.vo.ListRow;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author shizhongtao
 * 
 * date 2016-2-2 Description:
 *       读取07excel的sax方法，解析器默认使用org.apache.xerces.parsers.SAXParser
 *       。可以痛痛set方法手动设置
 */
public class DefaultXSSFSaxHandler implements ReadHandler {
	private OPCPackage pkg;
	private XMLReader parser;
	private ExcelReadListener excelReadListener;
	private boolean ignoreNumFormat = false;
	private DefaultSheetContentsHandler handler ;
	public DefaultXSSFSaxHandler(String path, ExcelReadListener excelReadListener)
			throws InvalidFormatException, IOException {
		this(path, excelReadListener, false);
	}

	public DefaultXSSFSaxHandler(File file, ExcelReadListener excelReadListener)
			throws InvalidFormatException, IOException {
		this(file, excelReadListener, false);
	}

	public DefaultXSSFSaxHandler(InputStream in, ExcelReadListener excelReadListener)
			throws InvalidFormatException, IOException {
		this(in, excelReadListener, false);
	}

	public DefaultXSSFSaxHandler(String path, ExcelReadListener excelReadListener,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		
		this(new File(path), excelReadListener,ignoreNumFormat);
		
	}

	public DefaultXSSFSaxHandler(File file, ExcelReadListener excelReadListener,
			boolean ignoreNumFormat) throws InvalidFormatException {
		
		
		try {
			this.pkg = OPCPackage.open(file, PackageAccess.READ);
			this.excelReadListener = excelReadListener;
			this.ignoreNumFormat = ignoreNumFormat;
			this.handler = new DefaultSheetContentsHandler(
					excelReadListener);
		} catch (IllegalArgumentException  e) {
			if (this.pkg != null) {
				pkg.revert();
			}
			throw e;
		} catch (InvalidFormatException e) {
			if (this.pkg != null) {
				pkg.revert();
			}
			throw e;
		}
	}

	public DefaultXSSFSaxHandler(InputStream in, ExcelReadListener excelReadListener,
			boolean ignoreNumFormat) throws InvalidFormatException, IOException {
		OPCPackage p = OPCPackage.open(in);
		try {
			this.pkg = p;
			this.excelReadListener = excelReadListener;
			this.ignoreNumFormat = ignoreNumFormat;
			this.handler = new DefaultSheetContentsHandler(
					excelReadListener);
		} catch (Exception e) {
			p.revert();
			throw e;
		}
	}
	public DefaultXSSFSaxHandler(OPCPackage pkg, ExcelReadListener excelReadListener,
			boolean ignoreNumFormat) throws InvalidFormatException {
		
		this.pkg = pkg;
		this.excelReadListener = excelReadListener;
		this.ignoreNumFormat = ignoreNumFormat;
		this.handler = new DefaultSheetContentsHandler(
				excelReadListener);
	}

	@Override
	public void readSheets(int maxReadLine) throws IOException, OpenXML4JException, SAXException {
		setMaxReturnLine(maxReadLine);
		readSheets();
	}
	
	@Override
	public void readSheets() throws IOException, OpenXML4JException, SAXException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage 对象为空");
		}
		try {
			XSSFReader xssfReader = new XSSFReader(pkg);
			XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
					.getSheetsData();
			ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
					pkg);
			int sheetIndex = 0;
			
			ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
					xssfReader.getStylesTable(), strings, handler, false);
			sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
			getParser().setContentHandler(sheetXMLHandler);
			while (sheets.hasNext()) {
				try (InputStream sheet = sheets.next()) {
					String name = sheets.getSheetName();
					excelReadListener.startSheet(sheetIndex, name);
					InputSource sheetSource = new InputSource(sheet);
				
					try {
						getParser().parse(sheetSource);
					} catch (SAXException e) {
						if (e instanceof BingSaxReadStopException) {
							
						} else {
							throw e;
						}
					}
					excelReadListener.endSheet(sheetIndex, name);
					sheetIndex++;
				}
			}
		} finally {
			pkg.revert();
		}
		excelReadListener.endWorkBook();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.chinamobile.excel.reader.SaxHandler#process(int)
	 */
	@Override
	public void readSheet(int index,int maxReadLine) throws IOException, OpenXML4JException,
			SAXException {
		setMaxReturnLine(maxReadLine);
		readSheet(new int[]{index});
	}
	@Override
	public void readSheet(int index) throws IOException, OpenXML4JException,
	SAXException {
		readSheet(new int[]{index});
	}

	@Override
	public void readSheet(int[] indexs,int maxReadLine) throws IOException, OpenXML4JException,
	SAXException {
		setMaxReturnLine(maxReadLine);
		readSheet(indexs);
	}
	@Override
	public void readSheet(int[] indexs) throws IOException, OpenXML4JException,
			SAXException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage is null");
		}
		//ImmutableCollection<Integer> sheetSelect=
		Set<Integer> build = new HashSet<>();
		for (int i : indexs) {
			if(i<0){
				throw new IllegalArgumentException("index of sheet is a number greater than 0");
			}else{
				build.add(i);
			}
		}
		Set<Integer> setSheets = Set.copyOf(build);
		try {
	 		XSSFReader xssfReader = new XSSFReader(pkg);
			XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
					.getSheetsData();
			ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
					pkg);
			int sheetIndex = 0;
			
			ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
					xssfReader.getStylesTable(), strings, handler, false);
			sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
			getParser().setContentHandler(sheetXMLHandler);
			while (sheets.hasNext()) {
				try (InputStream sheet = sheets.next()) {
					String name = sheets.getSheetName();
					if (!setSheets.contains(sheetIndex)) {
						sheetIndex++;
						continue;
					}
					
					
					excelReadListener.startSheet(sheetIndex, name);
					InputSource sheetSource = new InputSource(sheet);
					
					try {
						getParser().parse(sheetSource);
					} catch (SAXException e) {
						if (e instanceof BingSaxReadStopException) {
							
						} else {
							throw e;
						}
					}
					excelReadListener.endSheet(sheetIndex, name);
					sheetIndex++;
				}
			}
			excelReadListener.endWorkBook();
		} finally {
			pkg.revert();
		}
	}

	@Override
	public void readSheet(String indexName, int maxReadLine)
			throws IOException, OpenXML4JException, SAXException {
		setMaxReturnLine(maxReadLine);
		readSheet(indexName);
	}

	@Override
	public void readSheet(String sheetName) throws IOException, SAXException,
			OpenXML4JException {
		if (pkg == null) {
			throw new NullPointerException("OPCPackage 对象为空");
		}
		try {
			XSSFReader xssfReader = new XSSFReader(pkg);
			XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
					.getSheetsData();
			ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
					pkg);
			int sheetIndex = 0;
			
			ExcelXSSFSheetXMLHandler sheetXMLHandler = new ExcelXSSFSheetXMLHandler(
					xssfReader.getStylesTable(), strings, handler, false);
			sheetXMLHandler.ignoreNumFormat(ignoreNumFormat);
			getParser().setContentHandler(sheetXMLHandler);
			while (sheets.hasNext()) {
				try (InputStream sheet = sheets.next()) {
					String name = sheets.getSheetName();
					
					if (!name.equals(sheetName)) {
						sheetIndex++;
						continue;
					}
					excelReadListener.startSheet(sheetIndex, name);
					InputSource sheetSource = new InputSource(sheet);
					try {
						getParser().parse(sheetSource);
					} catch (SAXException e) {
						if (e instanceof BingSaxReadStopException) {
						} else {
							throw e;
						}
					}
					excelReadListener.endSheet(sheetIndex, name);
					sheetIndex++;
					break;
				}
			}
			excelReadListener.endWorkBook();
		} finally {
			this.pkg.revert();
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

	protected void setMaxReturnLine(int num) {
		this.handler.setMaxReadLine(num);
	}

	private static class DefaultSheetContentsHandler implements
			BingSheetContentsHandler {
		private ListRow rowList;
		private ExcelReadListener excelReader;
		private int maxReadLine = Integer.MAX_VALUE;

		public DefaultSheetContentsHandler(ExcelReadListener excelReader) {
			this.excelReader = excelReader;
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
			rowList = new ListRow();
		}

		@Override
		public void endRow(int rowNum) {

			excelReader.optRow(rowNum, rowList);
		}

		@Override
		public void cell(String cellReference,
				String formattedValue, XSSFComment comment)
				 {
			
			int column = nameToColumn(cellReference);
			rowList.add(new CellKV<String>(column, StringUtils.isEmpty(formattedValue) ? null : formattedValue));
			
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

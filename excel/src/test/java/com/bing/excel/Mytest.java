package com.bing.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bing.excel.exception.BingSaxReadStopException;
import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.sax.DefaultXSSFSaxHandler;
import com.bing.excel.reader.sax.ExcelReadOnlySharedStringsTable;
import com.bing.excel.reader.sax.ExcelXSSFSheetXMLHandler;
import com.bing.excel.reader.sax.ExcelXSSFSheetXMLHandler.BingSheetContentsHandler;
import com.bing.excel.reader.vo.CellKV;
import com.bing.excel.reader.vo.ListRow;
import com.bing.utils.DataTypeDetect;
import com.google.common.primitives.Doubles;

/**
 * 创建时间：2015-12-8下午12:22:38 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：Mytest.java 类说明：
 */
public class Mytest {
	@Test
	public void testM() throws EncryptedDocumentException,
			InvalidFormatException, IOException {
		/*
		 * File a = new File("d:/ad.s.xlsx"); System.out.println(a.getName());
		 * boolean b = Pattern.matches(".*((\\.xls)|(\\.xlsx))$", null);
		 * System.out.println(b);
		 */
		Workbook wb = WorkbookFactory.create(new File("E:/b.xlsx"));
		Sheet sheet1 = wb.getSheetAt(0);
		for (Row row : sheet1) {
			for (Cell cell : row) {
				CellReference cellRef = new CellReference(row.getRowNum(),
						cell.getColumnIndex());
				System.out.print(cellRef.formatAsString());
				System.out.print(" - ");

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					System.out.println(cell.getRichStringCellValue()
							.getString());
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						System.out.println(cell.getDateCellValue());
						System.out.println(cell.getNumericCellValue());
						System.out.println(111111);
					} else {
						System.out.println(cell.getNumericCellValue());
					}
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					System.out.println(cell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_FORMULA:
					System.out.println(cell.getNumericCellValue());
					System.out.println(cell.getCellFormula());
					break;
				default:
					System.out.println();
				}
			}
		}
	}

	@Test
	public void testCreatexml() throws IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("new sheet");
		Row row = sheet.createRow(2);
		row.createCell(0).setCellValue(1.1);
		row.createCell(1).setCellValue(new Date());
		row.createCell(2).setCellValue(Calendar.getInstance());
		row.createCell(3).setCellValue("a string");
		row.createCell(4).setCellValue(true);
		row.createCell(5).setCellType(Cell.CELL_TYPE_ERROR);
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream("F:/workbook.xls");
		wb.write(fileOut);
		fileOut.close();
	}

	@Test
	public void testReadxml() throws IOException, EncryptedDocumentException,
			InvalidFormatException {
		Workbook wb = WorkbookFactory.create(new File("E:/b.xlsx"));
		Sheet sheet = wb.getSheetAt(0);
		for (int rowNum = 0; rowNum < 5; rowNum++) {
			Row r = sheet.getRow(rowNum);
			if (r == null) {
				System.out.println("kong");
				continue;
			}

			for (int cn = 0; cn < 10; cn++) {
				Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
				if (c == null) {
					System.out.println("null");
				} else {
					System.out.println(c.getCellType());
				}
			}
		}

	}

	@Test
	public void testMe() {
		// System.out.println(Integer.class.isPrimitive());
		// System.out.println("a".getClass().isAssignableFrom(String.class));
		// System.out.println(String.class.isPrimitive());
		// System.out.println(ExcelBing.class.isPrimitive());
		// System.out.println(String.class.getName());
		// System.out.println(Collection.class.isAssignableFrom(ArrayList.class));
		StringBuilder bu = new StringBuilder();
		bu.append("hello world");
		bu.setLength(0);
		System.out.println(bu.toString());
		System.out.println(bu.append(2l));
		System.out.println(bu.length());
	}

	@Test
	public void testReaderxlsx() {
		String path = "E:/a1.xlsx";
		ExcelUtil e = new ExcelUtil();
		try {
			e.process(path);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private double convertToDaouble(Date date) {
		double excelDate = HSSFDateUtil.getExcelDate(date);
		System.out.println(excelDate);
		String startDate = "1899-12-31";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",
				Locale.SIMPLIFIED_CHINESE);
		Date firstDate;
		try {
			firstDate = format.parse(startDate);
		} catch (ParseException e) {
			throw new IllegalStateException("java 未知错误");
		}
		/*
		 * Calendar cal =
		 * Calendar.getInstance(TimeZone.getTimeZone("GMT+8"),Locale
		 * .SIMPLIFIED_CHINESE); cal.setTime(firstDate); long
		 * startLong=cal.getTimeInMillis(); cal.setTime(date); long
		 * endLong=cal.getTimeInMillis();
		 */
		// long diff = endLong - startLong;
		long diff = date.getTime() - firstDate.getTime();
		double re = -1;
		if (!(diff < (86400 * 1000))) {
			re = diff / (86400d * 1000d);
		}
		return re;
	}

	private long convertToLong(Date date) {
		return ((Double) convertToDaouble(date)).longValue();
	}

	private Date convertToDate(double d) {
		String startDate = "1889-12-30";
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd",
				Locale.SIMPLIFIED_CHINESE);
		Date firstDate;
		try {
			firstDate = format.parse(startDate);
		} catch (ParseException e) {
			throw new IllegalStateException("java 未知错误");
		}
		if (d < 1d) {
			return firstDate;
		} else {
			try {
				firstDate = format.parse("1899-12-31");
			} catch (ParseException e) {
				throw new IllegalStateException("java 未知错误");
			}

			Calendar c = Calendar.getInstance();
			c.setTime(firstDate);
			c.set(Calendar.HOUR_OF_DAY, 0);

			long diff = ((Double) (d * 86400 * 1000)).longValue();
			long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数129600000
			long nh = 1000 * 60 * 60;// 一小时的毫秒数
			long nm = 1000 * 60;// 一分钟的毫秒数
			long ns = 1000;// 一秒钟的毫秒数
			int day = (int) (diff / nd);// 计算差多少天
			c.add(Calendar.DATE, day);
			long remaining = diff % nd;
			int hour = (int) (remaining / nh);// 计算差多少小时
			remaining %= nh;
			int min = (int) (remaining / nm);// 计算差多少分钟
			remaining %= nm;
			int sec = (int) (remaining / ns);// 计算差多少秒
			int milliSec = (int) (remaining % 1000);
			if (hour > 0) {
				c.roll(Calendar.HOUR, hour);
			}
			c.set(Calendar.MINUTE, 0);

			if (min > 0) {
				c.roll(Calendar.MINUTE, min);
			}
			c.set(Calendar.SECOND, 0);

			if (sec > 0) {
				c.roll(Calendar.SECOND, sec);
			}
			c.set(Calendar.MILLISECOND, 0);
			if (milliSec > 0) {
				c.roll(Calendar.MILLISECOND, (int) milliSec);
			}
			return c.getTime();
		}
	}

	private Date convertToDate(long l) {
		return convertToDate((double) l);
	}

	@Test
	public void testDateConvertor() {
		// String startDate = "2015-10-21";
		// String startDate="2015-10-21";
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		// String startDate="1900-01-01 00:00:00.000";
		String startDate = "2015-10-20 23:59:59.009";
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		Date date;
		try {
			date = format.parse(startDate);
		} catch (ParseException e) {
			throw new IllegalStateException("java 未知错误");
		}
		System.out.println(convertToDaouble(date));
		System.out.println(convertToLong(date));
	}

	@Test
	public void testDoubleConvertor() {
		System.out.println(convertToDate(42297.99d));
		System.out.println(convertToDate(42297l));
		System.out.println();
		Date javaDate = HSSFDateUtil.getJavaDate(42297.99d,
				TimeZone.getTimeZone("GMT+8"));
		System.out.println(javaDate);
	}

	@Test
	public void testDateFormat() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yy-M-d");
		System.out.println(format.parse("89-02-02 "));
		Pattern p = Pattern.compile("^\\d+\\\\\\d+$");
		Matcher m = p.matcher("56\\56");
		System.out.println(m.find());
		System.out.println("2105\\sd4".replaceAll("[/\\\\年-]", "@"));
	}

	@Test
	public void testRegexTime() {
		String arg = "2012\"年\"12\"月\"d\"d\"";
		Pattern date_chinese = Pattern.compile("\".\"");
		Matcher matcher = date_chinese.matcher(arg);
		// System.out.println(matcher.start());
		List<Integer> m = new ArrayList<>();
		while (matcher.find()) {
			m.add(matcher.start());
			m.add(matcher.end());
		}
		char[] chars = arg.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < chars.length; j++) {
			if (!m.contains(j)) {
				sb.append(chars[j]);
			}
		}
		System.out.println(sb.toString());
	}

	@Test
	public void testReadString() throws Exception {
		String path = "E:/a1.xlsx";
		OPCPackage pkg = OPCPackage.open(path, PackageAccess.READ);

		XSSFReader xssfReader = new XSSFReader(pkg);
		StylesTable stylesTable = xssfReader.getStylesTable();
		try {
			ExcelReadOnlySharedStringsTable strings = new ExcelReadOnlySharedStringsTable(
					pkg);
			ExcelXSSFSheetXMLHandler e = new ExcelXSSFSheetXMLHandler(
					stylesTable, strings, new Read(), false);
			XMLReader parser = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");
			// org.apache.xerces.parsers.AbstractSAXParser
			parser.setContentHandler(e);
			try (InputStream sheet = xssfReader.getSheet("rId1")) {
				InputSource sheetSource = new InputSource(sheet);
				parser.parse(sheetSource);
			} catch (Exception e2) {
				throw e2;
			}
		} catch (Exception e) {
			if (e instanceof BingSaxReadStopException) {

			} else {
				throw e;
			}
		}

	}

	public static class Read implements BingSheetContentsHandler {
		int i = 0;

		@Override
		public void startRow(int rowNum) {
			System.out.println("start====" + rowNum);
		}

		@Override
		public void endRow(int rowNum) {
			System.out.println("end====" + rowNum);
		}

		@Override
		public void cell(int rowNum, String cellReference,
				String formattedValue, XSSFComment comment)
				 {
			
			if (comment != null) {

				System.out.println("cell====" + cellReference + ":"
						+ formattedValue + ":" + comment.getColumn() + "@"
						+ comment.getRow() + ":" + comment.getString());
			} else {
				System.out.println("cell====" + cellReference + ":"
						+ formattedValue);
			}
			i++;
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
			System.out.println("header====" + text + ":" + isHeader + ":"
					+ tagName);
		}

	}

	public static class Myte extends AbstractExcelReadListener {

		@Override
		public void optRow(int curRow, ListRow rowList) {
			System.out.println("行：" + curRow);
				System.out.println(rowList);
		}

		@Override
		public void startSheet(int sheetIndex, String name) {
			System.out.println("start:"+sheetIndex+":"+name);
		}

		@Override
		public void endSheet(int sheetIndex, String name) {
			System.out.println("end:"+sheetIndex+":"+name);
		}

		@Override
		public void endWorkBook() {
			System.out.println("end workbook");
		}

	}

	@Test
	public void testbing() throws IOException, OpenXML4JException, SAXException {
		String path = "E:/a1.xlsx";
		DefaultXSSFSaxHandler handler = new DefaultXSSFSaxHandler(path,
				new Myte(), false);
		handler.setMaxReturnLine(5);
		handler.readSheets();

	}
	@Test
	public void testRe(){
		 System.out.println(DataTypeDetect.isYMD("20140203"));
		 System.out.println(DataTypeDetect.isYMD("2014-12-12"));
		 System.out.println(DataTypeDetect.isYMD("2014/02/02"));
		 System.out.println(DataTypeDetect.isYMD("2014年12月01"));
		 System.out.println(DataTypeDetect.isYMD("2014年12月01  "));
		 System.out.println(DataTypeDetect.isYMD("2014-12-01 0:0:0"));
		 System.out.println(DataTypeDetect.isYMD("2014-12-01 00:00:00"));
		 System.out.println("__________________________________");
		 System.out.println(DataTypeDetect.isYMD("2014-12-01 00:00:000"));
		 System.out.println(DataTypeDetect.isYMD("2014-13-01 a"));
		 System.out.println(DataTypeDetect.isYMD("2014-13-01 "));
		 System.out.println(DataTypeDetect.isYMD("201563"));
		 System.out.println(DataTypeDetect.isYMD("2014年13月01"));
		 System.out.println(DataTypeDetect.isYMD("2014-13-01 00:00:"));
		 System.out.println(DataTypeDetect.isYMD("2014-13-01 00:00"));
		 System.out.println(DataTypeDetect.isYMD("150102"));
		 System.out.println(DataTypeDetect.isYMD("1952020300"));
		 System.out.println(DataTypeDetect.isYMD("150102"));
	}
	@Test
	public void testReNum(){
		System.out.println(DataTypeDetect.isNumType("20140203"));
		System.out.println(DataTypeDetect.isNumType("20140203.3225"));
		System.out.println(DataTypeDetect.isNumType("201402.03.3225"));
		System.out.println(DataTypeDetect.isNumType("201 40203.3225"));
		System.out.println(DataTypeDetect.isNumType("201a40203.3225"));
		System.out.println(DataTypeDetect.isNumType("201a402"));
		System.out.println(Integer.MAX_VALUE);
		System.out.println(Long.MAX_VALUE);
		
	}
	@Test
	public void testIntegerNum(){
		System.out.println(DataTypeDetect.isIntegerType("20140203"));
		System.out.println(DataTypeDetect.isIntegerType("201402.00000"));
		System.out.println(DataTypeDetect.isIntegerType("214748364"));
		System.out.println(DataTypeDetect.isIntegerType("2147483646"));
		System.out.println(DataTypeDetect.isIntegerType("1147483647.0000"));
		System.out.println(DataTypeDetect.isIntegerType("1147483646"));
		System.out.println(Integer.MAX_VALUE);
		System.out.println(DataTypeDetect.isIntegerType("11147483647"));
		System.out.println(DataTypeDetect.isIntegerType("201 40203"));
		System.out.println(DataTypeDetect.isIntegerType("20140203.3225"));
		System.out.println(DataTypeDetect.isIntegerType("201a40203.3225"));
		System.out.println(DataTypeDetect.isIntegerType("2147483647"));
		System.out.println(DataTypeDetect.isIntegerType("011147483647"));
		System.out.println(DataTypeDetect.isIntegerType("201a402"));
		
		
	}
	@Test
	public void testBoolean(){
		System.out.println(DataTypeDetect.isBooleanType("是"));
		System.out.println(DataTypeDetect.isBooleanType("否"));
		System.out.println(DataTypeDetect.isBooleanType("0"));
		System.out.println(DataTypeDetect.isBooleanType("真"));
		System.out.println(DataTypeDetect.isBooleanType("假"));
		System.out.println(DataTypeDetect.isBooleanType("20"));
		System.out.println(DataTypeDetect.isBooleanType("h"));
		System.out.println(DataTypeDetect.isBooleanType("-6"));
		
	}
	@Test
	public void testCd(){
	
		System.out.println(Double.parseDouble("0x0.21p0"));
		System.out.println(Double.parseDouble("0x1.2ap4"));
		System.out.println(Boolean.parseBoolean("1"));
		System.out.println(Boolean.parseBoolean("10"));
		System.out.println(Boolean.parseBoolean("0"));
		System.out.println(Boolean.parseBoolean("false"));
		
	}
	@Test
	public void testCad(){
		Pattern p = Pattern.compile("(?:ab)\\d+(bc)\\d(\\1)|a.+f");
		Matcher m=p.matcher("ab121bc2bcfa");
		System.out.println(m.matches());
		System.out.println(m.group());
		
	}
	@Test
	public void testp()
	{
		System.out.println(int.class.isPrimitive());
		System.out.println(double.class.isPrimitive());
		System.out.println(boolean.class.isPrimitive());
		System.out.println(Boolean.class.isPrimitive());
		System.out.println(Double.class.isPrimitive());
	}
	
}

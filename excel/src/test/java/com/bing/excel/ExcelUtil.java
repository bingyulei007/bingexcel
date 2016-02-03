package com.bing.excel;

import java.io.InputStream;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bing.excel.reader.usermodel.ExcelBuiltinFormats;
import com.bing.excel.reader.usermodel.ExcelDataFormatter;
import com.bing.excel.reader.sax.ExcelReadOnlySharedStringsTable;

public class ExcelUtil extends DefaultHandler {
	enum XssfDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER,
	}

	private XssfDataType nextDataType;

	private int sheetIndex = -1;
	private int curRow = 0;
	// Set when V start element is seen
	private boolean vIsOpen;
	private int thisColumn = -1;

	// Gathers characters as they are seen.
	private StringBuffer value = new StringBuffer();;
	private StylesTable stylesTable;

	// Used to format numeric cell values.
	private short formatIndex;
	private String formatString;
	// private SharedStringsTable sst ;
	private ExcelReadOnlySharedStringsTable sst;
	// The last column printed to the output stream
	// private int lastColumnNumber = -1;
	private final ExcelDataFormatter formatter = new ExcelDataFormatter();

	// private boolean isCellNull = false;
	/**
	 * 读取第一个工作簿的入口方法
	 * 
	 * @param path
	 */
	public void readOneSheet(String path) throws Exception {
		OPCPackage pkg = OPCPackage.open(path);
		// XSSFReader r = new XSSFReader(pkg);
		// ReadOnlySharedStringsTable sst = r.getSharedStringsTable();

		// XMLReader parser = fetchSheetParser(sst);

		// InputStream sheet = r.getSheet("rId1");

		// InputSource sheetSource = new InputSource(sheet);
		// parser.parse(sheetSource);

		// sheet.close();
	}

	/**
	 * 读取所有工作簿的入口方法
	 * 
	 * @param path
	 * @throws Exception
	 */
	public void process(String path) throws Exception {
		OPCPackage pkg = OPCPackage.open(path, PackageAccess.READ);
		XSSFReader xssfReader = new XSSFReader(pkg);
		// SharedStringsTable sst = r.getSharedStringsTable();
		// sst = xssfReader.getSharedStringsTable();
		stylesTable = xssfReader.getStylesTable();
		sst = new ExcelReadOnlySharedStringsTable(pkg);
		XMLReader parser = fetchSheetParser();

		XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader
				.getSheetsData();
		while (sheets.hasNext()) {
			curRow = 0;
			sheetIndex++;
			InputStream sheet = sheets.next();
			String sheetName = sheets.getSheetName();
			System.out.println(sheetName);
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
		}
	}

	/**
	 * 该方法自动被调用，每读一行调用一次，在方法中写自己的业务逻辑即可
	 * 
	 * @param sheetIndex
	 *            工作簿序号
	 * @param curRow
	 *            处理到第几行
	 * @param rowList
	 *            当前数据行的数据集合
	 */
	public void optRow(int sheetIndex, int curRow, List<String> rowList) {

	}

	public XMLReader fetchSheetParser() throws SAXException {
		XMLReader parser = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		// org.apache.xerces.parsers.AbstractSAXParser
		parser.setContentHandler(this);
		return parser;
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {

		if ("inlineStr".equals(name) || "v".equals(name)) {
			vIsOpen = true;
			// Clear contents cache
			value.setLength(0);
		}
		// c => 单元格
		else if (name.equals("c")) {
			vIsOpen = false;
			String r = attributes.getValue("r");
			int firstDigit = -1;
			for (int c = 0; c < r.length(); ++c) {
				if (Character.isDigit(r.charAt(c))) {
					firstDigit = c;
					break;
				}
			}
			thisColumn = nameToColumn(r.substring(0, firstDigit));

			nextDataType = XssfDataType.NUMBER;
			this.formatIndex = -1;
			this.formatString = null;
			String cellType = attributes.getValue("t");
			String cellStyleStr = attributes.getValue("s");
			if ("b".equals(cellType))
				nextDataType = XssfDataType.BOOL;
			else if ("e".equals(cellType))
				nextDataType = XssfDataType.ERROR;
			else if ("inlineStr".equals(cellType))
				nextDataType = XssfDataType.INLINESTR;
			else if ("s".equals(cellType))
				nextDataType = XssfDataType.SSTINDEX;
			else if ("str".equals(cellType))
				nextDataType = XssfDataType.FORMULA;
			else if (cellStyleStr != null) {
				// It's a number, but almost certainly one
				// with a special style or format
				int styleIndex = Integer.parseInt(cellStyleStr);
				XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
				this.formatIndex = style.getDataFormat();
				System.out.print("{v:" + styleIndex + ", formatIndex:"
						+ formatIndex + "}   ");
				this.formatString = style.getDataFormatString();
				if( this.formatString == null||this.formatString.startsWith("reserved-0x")){
					this.formatString = ExcelBuiltinFormats
							.getBuiltinFormat(Integer.valueOf(this.formatString.replace("reserved-0x", ""), 16));
				}
				System.out.print(formatString + "  ");

			}

		} else if (name.equals("row")) {
			System.out.println(" 第" + curRow + "行：\n");
		}

	}

	/**
	 * Converts an Excel column name like "C" to a zero-based index.
	 * 
	 * @param name
	 * @return Index corresponding to the specified name
	 */
	private int nameToColumn(String name) {
		
		int column = -1;
		for (int i = 0; i < name.length(); ++i) {
			int c = name.charAt(i);
			column = (column + 1) * 26 + c - 'A';
		}
		return column;
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// 根据SST的索引值的到单元格的真正要存储的字符串
		// 这时characters()方法可能会被调用多次

		// v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
		if (name.equals("v")) {
			String thisStr = null;
			if (!nextDataType.equals(XssfDataType.SSTINDEX)) {
				if (this.formatString == null) {
					System.out.print(nextDataType + "  ");
					System.out.print(value + "  ");
					System.out.println(thisColumn + "  ");
				} else {
					String re = formatter.formatRawCellContents(
							Double.parseDouble(value.toString()), formatIndex,
							formatString);
					System.out.print(nextDataType + "  ");
					System.out.print(re + "  ");
					System.out.println(thisColumn + "  ");
				}
			} else {
				String sstIndex = value.toString();
				try {
					int idx = Integer.parseInt(sstIndex);
					XSSFRichTextString rtss = new XSSFRichTextString(
							sst.getEntryAt(idx));
					thisStr = '"' + rtss.getString() + '"';
					System.out.print(nextDataType + "  ");
					System.out.print(thisStr + "  ");
					System.out.println(thisColumn);
				} catch (NumberFormatException ex) {
					ex.printStackTrace();
				}
			}

		} else {
			// 如果标签名称为 row ，这说明已到行尾
			if (name.equals("row")) {
				System.out.println("以上 第" + curRow + "行：\n");
				curRow++;

			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// 得到单元格内容的值
		if (vIsOpen) {
			value.append(ch, start, length);
		}
	}

}

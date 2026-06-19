package com.bing.excel.reader.sax;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author shizhongtao
 *
 * date 2016-1-26
 * Description:  解决读取mac上xlsx结尾的excel文件读取中文问题
 *
 * 不再继承 ReadOnlySharedStringsTable，避免字段遮蔽和双重状态问题。
 * 使用独立的 SAXParserFactory 替代 POI 的 XMLHelper，以兼容 Mac 生成的 xlsx 文件。
 */
public class ExcelReadOnlySharedStringsTable extends DefaultHandler {

	private int count;
	private int uniqueCount;
	private List<String> strings = new ArrayList<String>();

	public ExcelReadOnlySharedStringsTable(OPCPackage pkg) throws IOException,
			SAXException {
		ArrayList<org.apache.poi.openxml4j.opc.PackagePart> parts =
				pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());
		if (!parts.isEmpty()) {
			readFrom(parts.get(0).getInputStream());
		}
	}

	public void readFrom(InputStream is) throws IOException, SAXException {
		if (is.available() > 0) {
			InputSource sheetSource = new InputSource(is);
			try {
				SAXParserFactory saxFactory = SAXParserFactory.newInstance();
				SAXParser saxParser = saxFactory.newSAXParser();
				XMLReader sheetParser = saxParser.getXMLReader();
				sheetParser.setContentHandler(this);
				sheetParser.parse(sheetSource);
			} catch (Exception e) {
				throw new RuntimeException("SAX parser appears to be broken - "
						+ e.getMessage());
			}
		}
	}

	public int getCount() {
		return this.count;
	}

	public int getUniqueCount() {
		return this.uniqueCount;
	}

	public String getEntryAt(int idx) {
		return strings.get(idx);
	}

	public List<String> getItems() {
		return strings;
	}

	private StringBuffer characters;
	private boolean rPhIsOpen = false;
	private boolean tIsOpen;

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if ("sst".equals(name)) {
			String count = attributes.getValue("count");
			if (count != null)
				this.count = Integer.parseInt(count);
			String uniqueCount = attributes.getValue("uniqueCount");
			if (uniqueCount != null)
				this.uniqueCount = Integer.parseInt(uniqueCount);

			this.strings = new ArrayList<String>(this.uniqueCount);

			characters = new StringBuffer();
		} else if ("si".equals(name)) {
			characters.setLength(0);
		} else if ("t".equals(name)) {
			tIsOpen = true;
		} else if ("rPh".equals(name)) {
			rPhIsOpen = true;
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if ("si".equals(name)) {
			strings.add(characters.toString());
		} else if ("t".equals(name)) {
			tIsOpen = false;
		} else if ("rPh".equals(name)) {
			rPhIsOpen = false;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (tIsOpen) {
			if (!rPhIsOpen) {
				characters.append(ch, start, length);
			}
		}
	}

}

package com.bing.excel.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.sql.SQLException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.bing.excel.reader.hssf.DefaultHSSFHandler;
import com.bing.excel.reader.sax.DefaultXSSFSaxHandler;

/**
 * @author shizhongtao
 *
 * date 2016-3-1
 * Description:
 */
public class ExcelReaderFactory {
	/**
	 * @param file
	 * @param excelReadListener
	 * @param ignoreNumFormat  是否忽略数据格式  (default=false，按照格式读取)
	 * @return
	 * @throws Exception
	 */
	public static ReadHandler create(File file, ExcelReadListener excelReadListener,
			boolean ignoreNumFormat) throws Exception {
		if (!file.exists()) {
			throw new FileNotFoundException(file.toString());
		}
		try {
			POIFSFileSystem fs = new POIFSFileSystem(file);
			try {
				return create(fs, excelReadListener, ignoreNumFormat);
			} catch (Exception e) {
				fs.close();
				throw e;
			}
		} catch (OfficeXmlFileException e) {
			OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ);
			try {
				return create(pkg, excelReadListener, ignoreNumFormat);
			} catch (Exception e1) {
				pkg.revert();
				throw e1;
			}
		}

	}

	/**
	 * @param file
	 * @param excelReader
	 * @return
	 * @throws Exception
	 */
	public static ReadHandler create(File file, ExcelReadListener excelReader)
			throws Exception {
		return create(file, excelReader, false);

	}


	/**
	 * @param inp
	 * @param excelReader
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static ReadHandler create(InputStream inp,
			ExcelReadListener excelReader) throws InvalidFormatException, IOException, SQLException {
		  return create(inp, excelReader, false);
	}
	/**
	 * @param inp
	 * @param excelReader
	 * @param ignoreNumFormat 是否忽略数据格式  (default=false，按照格式读取)
	 * @return  jie
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws SQLException
	 */
	public static ReadHandler create(InputStream inp,
			ExcelReadListener excelReader, boolean ignoreNumFormat)
			throws InvalidFormatException, IOException, SQLException {
		if (!inp.markSupported()) {
			inp = new PushbackInputStream(inp, 8);
		}
		InputStream is = FileMagic.prepareToCheckMagic(inp);
		FileMagic fm = FileMagic.valueOf(is);

		switch (fm) {
			case OLE2:
				POIFSFileSystem fs = new POIFSFileSystem(is);
				try {
					return create(fs, excelReader, ignoreNumFormat);
				} catch (Exception e) {
					fs.close();
					throw e;
				}
			case OOXML:
				OPCPackage pkg = OPCPackage.open(is);
				try {
					return create(pkg, excelReader, ignoreNumFormat);
				} catch (Exception e) {
					pkg.revert();
					throw e;
				}
			default:
				throw new InvalidFormatException(
						"Your InputStream was neither an OLE2 stream, nor an OOXML stream");
		}
	}

	/**
	 * @param pkg
	 * @param excelReader
	 * @return
	 * @throws SQLException
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static ReadHandler create(OPCPackage pkg,
			ExcelReadListener excelReader) throws SQLException,
			InvalidFormatException, IOException {
		return create(pkg, excelReader, false);
	}

	public static ReadHandler create(OPCPackage pkg,
			ExcelReadListener excelReadListener, boolean ignoreNumFormat) throws SQLException,
			InvalidFormatException, IOException {
		DefaultXSSFSaxHandler handler = new DefaultXSSFSaxHandler(pkg,
				excelReadListener, ignoreNumFormat);

		return handler;
	}

	public static ReadHandler create(POIFSFileSystem fs,
			ExcelReadListener excelReader) throws SQLException {
		return create(fs, excelReader, false);
	}

	public static ReadHandler create(POIFSFileSystem fs,
			ExcelReadListener excelReader, boolean ignoreNumFormat) throws SQLException {
		DefaultHSSFHandler handler = new DefaultHSSFHandler(fs, excelReader,
				ignoreNumFormat);

		return handler;
	}
}

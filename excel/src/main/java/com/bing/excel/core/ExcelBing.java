package com.bing.excel.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


/**
 * 操作excel的类，需要poi3.13的jar包<br>
 * maven地址,目前仅支持03版本
 * <p>
 * &ltdependency&gt<br>
 * &nbsp;&ltgroupId&gtorg.apache.poi&lt/groupId&gt<br>
 * &nbsp;&ltartifactId&gtpoi&lt/artifactId&gt<br>
 * &nbsp; &ltversion&gt3.8&lt/version&gt<br>
 * &lt/dependency&gt
 * </p>
 * 
 * @author shizhongtao
 * 
 *         2015 2015-4-24 下午5:49:55
 * 
 */
public interface ExcelBing {
	/**
	 * <p>
	 * Title: readFileToList<／p>
	 * <p>
	 * Description:读取表 <／p>
	 * 
	 * @param file
	 * @param clazz
	 * @param startRowNum
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 * @throws EncryptedDocumentException
	 */
	<T> List<T> readFileToList(File file, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException;

	<T> List<T> readFileToList(File file, Class<T> clazz, int sheetIndex, int startRowNum) throws EncryptedDocumentException, InvalidFormatException,
			IOException;

	<T> List<T> readStreamToList(InputStream stream, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException;

	<T> List<T> readStreamToList(InputStream stream, int sheetIndex, Class<T> clazz, int startRowNum) throws EncryptedDocumentException, InvalidFormatException, IOException;
}

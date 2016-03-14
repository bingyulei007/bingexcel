package com.bing.excel.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.bing.excel.core.impl.ExcelBingImpl.SheetVo;


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
	 * Description:读取excel 的第一个sheet页到list<／p>
	 * 
	 * @param file
	 * @param clazz
	 * @param startRowNum
	 * @return
	 * @throws Exception 
	 */
	<T> SheetVo<T> readSheet(File file, Class<T> clazz, int startRowNum) throws Exception ;
	/**
	 * 根据condition条件读取相应的sheet到list对象
	 * @param file
	 * @param clazz
	 * @param condition
	 * @return
	 * @throws Exception 
	 */
	<T> SheetVo<T> readSheet(File file, ReaderCondition<T> condition) throws Exception ;

	
	 /**
	  * 读取所condition 对应 sheet表格，到list
	 * @param file
	 * @param conditions 每个表格对应的condition，注：对于返回的条数，取conditions中 endNum的最小值
	 * @return 
	 * @throws Exception 
	 */
	List<SheetVo> readSheetsToList(File file,ReaderCondition[] conditions) throws Exception ;
	 
	 
	/**
	 * 读取所有sheet表格，到list
	 * @param file
	 * @param clazz 表格转换成的对象
	 * @param startRowNum
	 * @return
	 */
	<T> List<SheetVo<T>> readSheetsToList(File file, Class<T> clazz, int startRowNum) ;

	/**
	 * 读取第一个sheet到SheetVo
	 * @param stream
	 * @param condition
	 * @return
	 */
	<T> SheetVo<T> readStream(InputStream stream,ReaderCondition<T> condition) ;

	 List<SheetVo> readStreamToList(InputStream stream,  ReaderCondition[] condition) ;
	 /**
	  * 适合所有sheet中数据结构一样的excel
	 * @param stream
	 * @param condition
	 * @return
	 */
	<T> List<SheetVo<T>> readStreamToList(InputStream stream,  ReaderCondition<T> condition) ;
}

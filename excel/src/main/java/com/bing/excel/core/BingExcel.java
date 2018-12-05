package com.bing.excel.core;

import com.bing.excel.converter.FieldValueConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;

import com.bing.excel.core.impl.BingExcelImpl;


/**
 * 操作excel的类，需要poi3.13的jar包<br> maven地址,目前仅支持03版本 <p> &ltdependency&gt<br>
 * &nbsp;&ltgroupId&gtorg.apache.poi&lt/groupId&gt<br> &nbsp;&ltartifactId&gtpoi&lt/artifactId&gt<br>
 * &nbsp; &ltversion&gt3.8&lt/version&gt<br> &lt/dependency&gt </p>
 *
 * @author shizhongtao
 *
 * 2015 2015-4-24 下午5:49:55
 */
public interface BingExcel {

    /**
     * <p> Title: readFileToList<／p> <p> Description:读取excel 的第一个sheet页到list<／p>
     *
     * @param file excel对应的文件
     * @param clazz 要转换类型的class对象
     * @param startRowNum 从第几行开始读取
     */
    <T> BingExcelImpl.SheetVo<T> readFile(File file, Class<T> clazz, int startRowNum)
            throws Exception;

    /**
     * 根据condition条件读取相应的sheet到list对象
     */
    <T> BingExcelImpl.SheetVo<T> readFile(File file, ReaderCondition<T> condition) throws Exception;


    /**
     * 读取所condition 对应 sheet表格，到list
     *
     * @param conditions 每个表格对应的condition，注：对于返回的条数，取conditions中 endNum的最小值
     * @return sheetVo的list对象，如果没有符合conditions的结果，返回empetyList对象
     */
    List<BingExcelImpl.SheetVo> readFileToList(File file, ReaderCondition[] conditions)
            throws Exception;


    <T> BingExcelImpl.SheetVo<T> readStream(InputStream stream, ReaderCondition<T> condition)
            throws InvalidFormatException, IOException, SQLException, OpenXML4JException, SAXException;

    /**
     * read sheet witch index equal 0
     */
    <T> BingExcelImpl.SheetVo<T> readStream(InputStream stream, Class<T> clazz, int startRowNum)
            throws InvalidFormatException, IOException, SQLException, OpenXML4JException, SAXException;

    /**
     * read sheets
     */
    List<BingExcelImpl.SheetVo> readStreamToList(InputStream stream, ReaderCondition[] condition)
            throws InvalidFormatException, IOException, SQLException, OpenXML4JException, SAXException;

    /**
     * 输出model集合到excel 文件。
     *
     * @param iterables 要输出到文件的集合对象，
     * @param file 文件对象
     */
    void writeExcel(File file, Iterable... iterables) throws FileNotFoundException;

    void writeOldExcel(File file, Iterable... iterables) throws FileNotFoundException;

    /**
     * 输出model集合到excel 文件。
     *
     * @param path 文件路径
     */
    void writeExcel(String path, Iterable... iterables);

    /**
     * 写出xls格式的excel文件
     */
    void writeOldExcel(String path, Iterable... iterables);

    /**
     * 写出xls格式的excel到输出流
     */
    void writeExcel(OutputStream stream, Iterable... iterables);

    void writeOldExcel(OutputStream stream, Iterable... iterables);

    void writeCSV(String path, Iterable iterable) throws IOException;

    void writeCSV(OutputStream os, Iterable iterable) throws IOException;

    /**
     * 写出指定分隔符、指定是否写header的文件到输出流
     *
     * @param os 输出流
     * @param iterable 带转换的对象
     * @param delimiter 分隔符
     * @param isWithHeader 是否写入header行
     * @param isWithBOM 是否带BOM
     */
    void writeCSV(OutputStream os, Iterable iterable, char delimiter, boolean isWithHeader, boolean isWithBOM)
            throws IOException;

    void modelName(Class<?> clazz, String alias);

    void fieldConverter(Class<?> clazz, String filedName, int index, String alias,
            FieldValueConverter converter);
}

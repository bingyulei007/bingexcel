package com.bing.excel.core;

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

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.impl.BingExcelImpl;
import com.bing.excel.core.impl.BingExcelImpl.SheetExcel;


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
 */
public interface BingExcel {

  /**
   * <p>
   * Title: readFileToList<／p>
   * <p>
   * Description:读取excel 的第一个sheet页到list<／p>
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
   * @throws FileNotFoundException 文件存在但为目录、不可创建或无法打开
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   */
  void writeXlsx(File file, Iterable... iterables) throws FileNotFoundException;

  /**
   * 写出xls格式的excel文件到 File。
   *
   * @throws FileNotFoundException 文件存在但为目录、不可创建或无法打开
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   * @see #writeXlsx(File, Iterable...)
   */
  void writeXls(File file, Iterable... iterables) throws FileNotFoundException;

  /**
   * 输出model集合到excel 文件。
   *
   * <p>文件创建失败时抛出 {@code UnknownException}(RuntimeException)，
   * 而非 {@link FileNotFoundException}，因为内部通过路径创建 FileOutputStream
   * 时已将 FNFE 包装为未检异常。
   *
   * @param path 文件路径
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws com.bing.excel.exception.UnknownException 文件创建失败（路径非法/无权限）
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   */
  void writeXlsx(String path, Iterable... iterables);

  /**
   * 写出xls格式的excel文件。
   *
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws com.bing.excel.exception.UnknownException 文件创建失败（路径非法/无权限）
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   * @see #writeXlsx(String, Iterable...)
   */
  void writeXls(String path, Iterable... iterables);

  /**
   * 写出xlsx格式的excel到输出流。
   *
   * <p>调用方负责关闭 OutputStream。写出失败时流可能处于不完整状态。
   *
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   */
  void writeXlsx(OutputStream stream, Iterable... iterables);

  /**
   * 写出xls格式的excel到输出流。
   *
   * <p>调用方负责关闭 OutputStream。写出失败时流可能处于不完整状态。
   *
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   * @see #writeXlsx(OutputStream, Iterable...)
   */
  void writeXls(OutputStream stream, Iterable... iterables);

  void writeCsv(String path, Iterable iterable) throws IOException;

  void writeCsv(OutputStream os, Iterable iterable) throws IOException;

  /**
   * 写出指定分隔符、指定是否写header的CSV到输出流。
   *
   * @param os 输出流
   * @param iterable 带转换的对象
   * @param delimiter 分隔符
   * @param isWithHeader 是否写入header行
   * @param isWithBOM 是否带BOM
   */
  void writeCsv(OutputStream os, Iterable iterable, char delimiter, boolean isWithHeader,
      boolean isWithBOM) throws IOException;

  void modelName(Class<?> clazz, String alias);

  void fieldConverter(Class<?> clazz, String filedName, int index, String alias,
      FieldValueConverter converter);

  /**
   * 写出多sheet页的xlsx格式excel文件。
   *
   * @param path 文件路径
   * @param sheetExcels sheet页描述
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws com.bing.excel.exception.UnknownException 文件创建失败（路径非法/无权限）
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   */
  void writeXlsx(String path, SheetExcel... sheetExcels);

  /**
   * 写出多sheet页的xls格式excel文件。
   *
   * @param path 文件路径
   * @param sheetExcels sheet页描述
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws com.bing.excel.exception.UnknownException 文件创建失败（路径非法/无权限）
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   * @see #writeXlsx(String, SheetExcel...)
   */
  void writeXls(String path, SheetExcel... sheetExcels);

  /**
   * 写出多sheet页的xlsx格式excel到输出流。
   *
   * <p>调用方负责关闭 OutputStream。写出失败时流可能处于不完整状态。
   *
   * @param stream 输出流
   * @param sheetExcels sheet页描述
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   */
  void writeXlsx(OutputStream stream, SheetExcel... sheetExcels);

  /**
   * 写出多sheet页的xls格式excel到输出流。
   *
   * <p>调用方负责关闭 OutputStream。写出失败时流可能处于不完整状态。
   *
   * @param stream 输出流
   * @param sheetExcels sheet页描述
   * @throws IllegalCellConfigException 字段缺少 index 配置（aliasName-only 字段无法写出）
   * @throws IllegalEntityException 实体类缺少无参构造器
   * @throws IllegalStateException POI 写出失败（由 flush 包装）
   * @see #writeXlsx(OutputStream, SheetExcel...)
   */
  void writeXls(OutputStream stream, SheetExcel... sheetExcels);
}

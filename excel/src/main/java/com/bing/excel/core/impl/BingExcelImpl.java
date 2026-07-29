package com.bing.excel.core.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.core.handler.LocalConverterHandler;
import com.bing.excel.core.reflect.TypeAdapterConverter;
import com.bing.excel.exception.IllegalCellConfigException;
import com.bing.excel.exception.IllegalEntityException;
import com.bing.excel.mapper.AnnotationMapperHandler;
import com.bing.excel.mapper.ConversionMapperBuilder;
import com.bing.excel.mapper.UserDefineMapperHandler;
import com.bing.excel.reader.AbstractExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.ReadHandler;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.ListRow;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;
import com.bing.utils.FileCreateUtils;

/**
 * 创建时间：2015-12-8上午11:56:30 项目名称：excel
 *
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：BingExcelImpl.java 类说明：
 */
public class BingExcelImpl implements BingExcel {

  private static final Logger logger = Logger.getLogger(BingExcelImpl.class.getName());

  /**
   * model entity Converter,the relationship is sheet-to-entity
   */
  private final Map<Class<?>, TypeAdapterConverter<?>> typeTokenCache =
      new ConcurrentHashMap<Class<?>, TypeAdapterConverter<?>>();
  /**
   * globe filed converter
   */
  private final ConverterHandler localConverterHandler;
  private final AnnotationMapperHandler annotationMapperHandler = new AnnotationMapperHandler();
  private volatile UserDefineMapperHandler userDefineMapperHandler;

  public BingExcelImpl(ConverterHandler localConverterHandler) {
    this.localConverterHandler = localConverterHandler;
  }


  public UserDefineMapperHandler getUserDefineMapperHandler() {
    UserDefineMapperHandler h = userDefineMapperHandler;
    if (h == null) {
      synchronized (this) {
        h = userDefineMapperHandler;
        if (h == null) {
          h = new UserDefineMapperHandler(ConversionMapperBuilder.toBuilder());
          userDefineMapperHandler = h;
        }
      }
    }
    return h;
  }

  public BingExcelImpl() {
    this.localConverterHandler = new LocalConverterHandler();
  }

  @Override
  public <T> SheetVo<T> readFile(File file, Class<T> clazz, int startRowNum) throws Exception {
    return readFile(file, new ReaderCondition<T>(0, startRowNum, clazz));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> SheetVo<T> readFile(File file, ReaderCondition<T> condition) throws Exception {

    ReaderCondition[] arr = new ReaderCondition[] {condition};
    List<SheetVo> list = readFileToList(file, arr);

    return list.size() == 0 ? null : list.get(0);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.chinamobile.excel.core.ExcelBing#readSheetsToList(java.io.File,
   * com.chinamobile.excel.core.ReaderCondition[])
   */
  @SuppressWarnings({"rawtypes"})
  @Override
  public List<SheetVo> readFileToList(File file, ReaderCondition[] conditions) throws Exception {
    List<SheetVo> resultList = new ArrayList<>();
    BingExcelReaderListener listner = new BingExcelReaderListener(conditions, resultList);
    ReadHandler handler = ExcelReaderFactory.create(file, listner, true);
    readWithHandler(handler, conditions);
    return resultList.isEmpty() ? Collections.emptyList() : resultList;
  }

  @Override
  public <T> SheetVo<T> readStream(InputStream stream, Class<T> clazz, int startRowNum)
      throws IOException, SQLException, OpenXML4JException, SAXException {
    return readStream(stream, new ReaderCondition<T>(0, startRowNum, clazz));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public <T> SheetVo<T> readStream(InputStream stream, ReaderCondition<T> condition)
      throws IOException, SQLException, OpenXML4JException, SAXException {
    ReaderCondition[] arr = new ReaderCondition[] {condition};
    List<SheetVo> list = readStreamToList(stream, arr);
    return list.size() == 0 ? null : list.get(0);
  }

  @Override
  public List<SheetVo> readStreamToList(InputStream stream, ReaderCondition[] conditions)
      throws IOException, SQLException, OpenXML4JException, SAXException {
    List<SheetVo> resultList = new ArrayList<>();
    BingExcelReaderListener listner = new BingExcelReaderListener(conditions, resultList);
    ReadHandler handler = ExcelReaderFactory.create(stream, listner, true);
    readWithHandler(handler, conditions);
    return resultList.isEmpty() ? Collections.emptyList() : resultList;
  }

  private void readWithHandler(ReadHandler handler, ReaderCondition[] conditions)
      throws IOException, OpenXML4JException, SAXException {
    int[] indexArr = new int[conditions.length];
    int minNum = Integer.MAX_VALUE;
    for (int i = 0; i < conditions.length; i++) {
      int sheetNum = conditions[i].getSheetIndex();
      indexArr[i] = sheetNum;
      int endRow = conditions[i].getEndRow();
      if (endRow <= 0) {
        throw new IllegalArgumentException("endRow must be > 0, got " + endRow
            + " for condition[" + i + "]; use Integer.MAX_VALUE for unlimited.");
      }
      minNum = Math.min(minNum, endRow);
    }
    handler.readSheet(indexArr, minNum);
  }

  @Override
  public void writeXlsx(File file, Iterable... iterables) throws FileNotFoundException {
    WriteHandler handler = ExcelWriterFactory.createXSSF(file);
    writeToExcel(handler, iterables);

  }

  @Override
  public void writeXls(File file, Iterable... iterables) throws FileNotFoundException {
    WriteHandler handler = ExcelWriterFactory.createHSSF(file);
    writeToExcel(handler, iterables);
  }

  @Override
  public void writeXlsx(String path, Iterable... iterables) {
    WriteHandler handler = ExcelWriterFactory.createXSSF(path);
    writeToExcel(handler, iterables);
  }

  @Override
  public void writeXlsx(OutputStream stream, Iterable... iterables) {
    WriteHandler handler = ExcelWriterFactory.createXSSF(stream);
    writeToExcel(handler, iterables);
  }

  @Override
  public void writeXls(String path, Iterable... iterables) {
    WriteHandler handler = ExcelWriterFactory.createHSSF(path);
    writeToExcel(handler, iterables);

  }

  @Override
  public void writeXls(OutputStream stream, Iterable... iterables) {
    WriteHandler handler = ExcelWriterFactory.createHSSF(stream);
    writeToExcel(handler, iterables);
  }

  @Override
  // 临时使用下
  public void writeCsv(String path, Iterable iterable) throws IOException {
    File file = FileCreateUtils.createFile(path);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      writeCsv(fos, iterable, ',', true, true);
    }
  }

  @Override
  // 临时使用下,后面再改
  public void writeCsv(OutputStream os, Iterable iterable) throws IOException {
    writeCsv(os, iterable, ',', true, true);
  }

  @Override
  public void writeCsv(OutputStream os, Iterable iterable, char delimiter, boolean isWithHeader,
      boolean isWithBOM) throws IOException {
    Writer out = new OutputStreamWriter(os, "UTF-8");
    CSVPrinter csvPrinter = null;
    try {
      if (isWithBOM) {
        out.write(new String(new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
      }
      CSVFormat format;
      boolean isAdd = false;
      TypeAdapterConverter<?> typeAdapter = null;
      for (Object object : iterable) {
        if (!isAdd) {
          if (object != null) {
            isAdd = true;
            Class clazz = object.getClass();
            annotationMapperHandler.processEntity(clazz);
            registeAdapter(clazz);
            typeAdapter = typeTokenCache.get(clazz);
            ListLine header =
                typeAdapter.getHeadertoListLine(getUserDefineMapperHandler(), annotationMapperHandler);
            ListLine listLine =
                typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
            int maxIndex = header.getMaxIndex();
            if (isWithHeader) {
              String[] headerArr = new String[maxIndex + 1];
              for (CellKV<String> kv : header.getListStr()) {
                headerArr[kv.getIndex()] = kv.getValue();
              }
              format = CSVFormat.DEFAULT.withDelimiter(delimiter).withHeader(headerArr);
            } else {
              format = CSVFormat.DEFAULT.withDelimiter(delimiter);
            }
            csvPrinter = new CSVPrinter(out, format);
            csvPrinter.printRecord(listLine.toFullArray());
          }

        } else {
          ListLine listLine =
              typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
          csvPrinter.printRecord(listLine.toFullArray());
        }
      }
    } finally {
      // csvPrinter.close() 会一并 flush 并关闭底层 OutputStreamWriter；
      // csvPrinter 为 null 时（iterable 为空或全 null）单独关 out 确保 Writer 缓冲被释放。
      // 不关底层 os：与 writeCsv(OutputStream) 的「调用方自行关流」契约一致。
      if (csvPrinter != null) {
        csvPrinter.close();
      } else {
        out.close();
      }
    }
  }

  @Override
  public void modelName(Class<?> clazz, String alias) {
    annotationMapperHandler.processEntity(clazz);
    registeAdapter(clazz);
    getUserDefineMapperHandler().getObjConversionMapper().addModelName(clazz, alias);

  }

  /**
   * Registers a user-defined field mapper into {@link #userDefineMapperHandler}.
   *
   * <p>The user mapper wins over the annotation mapper via first-non-null handler
   * precedence (see {@code unmarshal}/{@code marshal}). This is a whole-field
   * replacement: the registered index/alias/converter fully mask the field's
   * {@code @CellConfig} mapper and unspecified properties are not merged back
   * from the annotation.
   */
  @Override
  public void fieldConverter(Class<?> clazz, String filedName, int index, String alias,
      FieldValueConverter converter) {
    annotationMapperHandler.processEntity(clazz);
    registeAdapter(clazz);
    Field field = this.typeTokenCache.get(clazz).getFieldByName(filedName);
    getUserDefineMapperHandler().getObjConversionMapper().registerLocalConverter(clazz, filedName,
        index, alias, field.getType(), false, converter);

  }

  private void writeToExcel(WriteHandler handler, Iterable... iterables) {
    try {
      for (Iterable list : iterables) {
        boolean isAdd = false;
        TypeAdapterConverter<?> typeAdapter = null;
        if (!list.iterator().hasNext()) {
          handler.createSheet("sheet1");
        }
        for (Object object : list) {
          if (!isAdd) {
            if (object != null) {
              isAdd = true;
              Class clazz = object.getClass();
              annotationMapperHandler.processEntity(clazz);
              registeAdapter(clazz);
              handler.createSheet(resolveModelName(clazz));
              typeAdapter = typeTokenCache.get(clazz);
              List<CellKV<String>> header =
                  typeAdapter.getHeader(getUserDefineMapperHandler(), annotationMapperHandler);
              handler.writeHeader(header);
              ListLine listLine =
                  typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
              handler.writeLine(listLine);
            } else {
              logger.warning("Skipping null element in iterable before first non-null row; "
                  + "header will not be written until a non-null element is found.");
            }

          } else {
            if (object == null) {
              logger.warning("Skipping null element in iterable; row will be omitted.");
              continue;
            }
            ListLine listLine =
                typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
            handler.writeLine(listLine);
          }
        }
      }
      handler.flush();
    } finally {
      // flush() 成功后 close() 为 no-op；异常路径下保证 Workbook/句柄/SXSSF 临时文件被释放
      handler.close();
    }

  }

  private void registeAdapter(Class<?> type) {
    // 转换的类型不可能对应的是基本类型
    if (type.isPrimitive()) {
      return;
    }
    // 目前先不考虑model的接口继承问题 TODO
    if (type.isInterface() || (type.getModifiers() & Modifier.ABSTRACT) > 0) {
      return;
    }

    synchronized (type) {
      // 已注册则直接返回
      if (typeTokenCache.containsKey(type)) {
        return;
      }
      Constructor<?> constructor;
      try {
        constructor = type.getDeclaredConstructor();
      } catch (NoSuchMethodException | SecurityException e) {
        throw new IllegalEntityException(type,
            "Gets the default constructor failed,the Objet must contains a  [no-args&public constructor] ",
            e);
      }
      final Field[] fields = type.getDeclaredFields();
      typeTokenCache.put(type, new TypeAdapterConverter<>(constructor, fields, localConverterHandler));
    }
  }

  /**
   * 解析 sheet 名称：优先用户自定义 mapper，其次注解 {@code @OutAlias}，最后类简名。
   */
  private String resolveModelName(Class<?> clazz) {
    String name = getUserDefineMapperHandler().getModelName(clazz);
    if (name == null) {
      name = annotationMapperHandler.getModelName(clazz);
    }
    if (name == null) {
      name = clazz.getSimpleName();
    }
    return name;
  }

  /**
   * reade Class
   *
   * @author shizhongtao date 2016-4-12 Description:
   */
  private class BingExcelReaderListener extends AbstractExcelReadListener {

    private final ReaderCondition[] conditions;
    private Class tagertClazz = null;
    private int startRow = 0;// start to read from first lines;
    private List<SheetVo> list;
    private SheetVo currentSheetVo;
    private TitleAliasResolver titleAliasResolver;

    public BingExcelReaderListener(ReaderCondition[] conditions, List<SheetVo> resultList) {
      super();
      this.conditions = conditions;
      Class[] arr = new Class[conditions.length];
      for (int i = 0; i < conditions.length; i++) {
        arr[i] = conditions[i].getTargetClazz();
      }
      annotationMapperHandler.processEntity(arr);
      this.list = resultList;
    }

    @Override
    public void optRow(int curRow, ListRow rowList) {
      // aliasName → index resolution: capture the title row BEFORE the startRow skip,
      // because startRow is "data starts here" — the title sits at startRow-1 when startRow>=1.
      if (tagertClazz != null && startRow >= 1 && curRow == startRow - 1
          && titleAliasResolver != null && !titleAliasResolver.isResolved()) {
        titleAliasResolver.captureTitleRow(rowList);
        titleAliasResolver.resolve(tagertClazz, currentSheetVo.getSheetIndex(),
            userDefineMapperHandler, annotationMapperHandler);
        return;
      }
      if (curRow < startRow) {
        return;
      }
      if (tagertClazz != null) {
        TypeAdapterConverter<?> typeAdapter = typeTokenCache.get(tagertClazz);
        if (typeAdapter == null) {
          throw new NullPointerException("没有对应的适配器，无法转换");
        }
        Object object = typeAdapter.unmarshal(rowList, titleAliasResolver,
            userDefineMapperHandler, annotationMapperHandler);
        // 空行（无任何单元格）时 unmarshal 返回 null，跳过不加入结果 List
        if (object != null) {
          currentSheetVo.addObject(object);
        }
      }
    }

    @Override
    public void startSheet(int sheetIndex, String name) {

      tagertClazz = null;
      startRow = 0;
      titleAliasResolver = null;
      for (int i = 0; i < conditions.length; i++) {
        if (conditions[i].getSheetIndex() == sheetIndex) {
          tagertClazz = conditions[i].getTargetClazz();
          int conditionStartRow = conditions[i].getStartRow();
          if (tagertClazz != null) {
            registeAdapter(tagertClazz);
            if (conditionStartRow == 0
                && TitleAliasResolver.hasAliasOnlyFields(tagertClazz, userDefineMapperHandler,
                    annotationMapperHandler)) {
              throw new IllegalCellConfigException("class[" + tagertClazz.getName()
                  + "] declares aliasName-based fields but startRow=0 means there is "
                  + "no title row to resolve against; set startRow>=1 or set an explicit "
                  + "index on every @CellConfig.");
            }
            if (conditionStartRow >= 1) {
              titleAliasResolver = new TitleAliasResolver();
            }
          }
          startRow = conditionStartRow;
          currentSheetVo = new SheetVo<>(sheetIndex, name);
          break;
        }
      }
    }

    @Override
    public void endSheet(int sheetIndex, String name) {
      if (currentSheetVo != null) {

        list.add(currentSheetVo);
        currentSheetVo = null;
      }
    }

    @Override
    public void endWorkBook() {

    }

  }


  public static class SheetVo<E> {

    private int sheetIndex;
    private String sheetName;
    private List<E> list = new ArrayList<>();

    public SheetVo(int sheetIndex, String sheetName) {
      super();
      this.sheetIndex = sheetIndex;
      this.sheetName = sheetName;
    }

    public int getSheetIndex() {
      return sheetIndex;
    }

    public String getSheetName() {
      return sheetName;
    }

    public List<E> getObjectList() {
      return list;
    }

    void addObject(E obj) {
      this.list.add(obj);
    }

  }


  /**
   * @author liluzhong
   * @date 2019/03/07 sheet excel model
   *
   */
  public static class SheetExcel {
    private String sheetName;
    private List<?> list = new ArrayList<>();

    public SheetExcel(String sheetName, List<?> list) {
      super();
      this.sheetName = sheetName;
      this.list = list;
    }

    public SheetExcel() {
      // TODO Auto-generated constructor stub
    }

    public String getSheetName() {
      return sheetName;
    }

    public void setSheetName(String sheetName) {
      this.sheetName = sheetName;
    }

    public List<?> getList() {
      return list;
    }

    public void setList(List<?> list) {
      this.list = list;
    }
  }



  @Override
  public void writeXlsx(String path, SheetExcel... sheetExcels) {
    WriteHandler handler = ExcelWriterFactory.createXSSF(path);
    writeToSheetExcel(handler, sheetExcels);
  }

  @Override
  public void writeXls(String path, SheetExcel... sheetExcels) {
    WriteHandler handler = ExcelWriterFactory.createHSSF(path);
    writeToSheetExcel(handler, sheetExcels);
  }

  @Override
  public void writeXlsx(OutputStream stream, SheetExcel... sheetExcels) {
    WriteHandler handler = ExcelWriterFactory.createXSSF(stream);
    writeToSheetExcel(handler, sheetExcels);
  }

  @Override
  public void writeXls(OutputStream stream, SheetExcel... sheetExcels) {
    WriteHandler handler = ExcelWriterFactory.createHSSF(stream);
    writeToSheetExcel(handler, sheetExcels);
  }


  /**
   * write sheet excel 写数据到多sheet页的excel
   * 
   * @param handler
   */
  private void writeToSheetExcel(WriteHandler handler, SheetExcel... sheetExcels) {
    try {
      TypeAdapterConverter<?> typeAdapter = null;
      for (int i = 0; i < sheetExcels.length; i++) {
        String sheetName = null;
        if (sheetExcels[i] != null) {
          boolean isAdd = false;
          // 获得定义的sheet的名称
          sheetName = sheetExcels[i].getSheetName();
          // 获取该sheet页的数据
          List<?> sheetList = sheetExcels[i].getList();
          if (sheetList == null || sheetList.size() == 0) {
            String emptySheetName = sheetName != null ? sheetName : "sheet" + (i + 1);
            handler.createSheet(emptySheetName);
            continue;
          }

          for (Object object : sheetList) {
            if (!isAdd) {
              if (object != null) {
                isAdd = true;
                Class clazz = object.getClass();
                annotationMapperHandler.processEntity(clazz);
                registeAdapter(clazz);
                if (sheetName == null) {
                  sheetName = resolveModelName(clazz);
                }
                handler.createSheet(sheetName);
                typeAdapter = typeTokenCache.get(clazz);
                List<CellKV<String>> header =
                    typeAdapter.getHeader(getUserDefineMapperHandler(), annotationMapperHandler);
                handler.writeHeader(header);
                ListLine listLine =
                    typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
                handler.writeLine(listLine);
              } else {
                logger.warning("Skipping null element in sheet[" + i
                    + "] before first non-null row; header will not be written "
                    + "until a non-null element is found.");
              }
            } else {
              if (object == null) {
                logger.warning("Skipping null element in sheet[" + i + "]; row will be omitted.");
                continue;
              }
              ListLine listLine =
                  typeAdapter.marshal(object, getUserDefineMapperHandler(), annotationMapperHandler);
              handler.writeLine(listLine);
            }
          }
        }
      }
      handler.flush();
    } finally {
      handler.close();
    }
  }
}

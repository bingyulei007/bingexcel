package com.bing.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.core.impl.BingExcelImpl.SheetExcel;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.OutValue;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;

/**
 * 回归测试：覆盖写出链路 P0/P1 修复的边界场景。
 *
 * <p>每个测试对应一个具体修复项，修复前会 NPE 或资源泄漏，修复后应正常通过。
 */
public class WritePathRegressionTest {

  private BingExcel bing;
  private final List<Path> tempFiles = new ArrayList<>();

  @Before
  public void setUp() {
    bing = BingExcelBuilder.toBuilder().build();
  }

  @After
  public void tearDown() {
    for (Path p : tempFiles) {
      try {
        Files.deleteIfExists(p);
      } catch (IOException ignored) {
      }
    }
    tempFiles.clear();
  }

  private Path newTempFile(String suffix) throws IOException {
    Path p = Files.createTempFile("bing-regression-", suffix);
    tempFiles.add(p);
    return p;
  }

  // ------------------------------------------------------------------
  // A1 + B2: writeSheetsExcel 空/null list 不再 NPE，且尊重用户传入的 sheetName
  // ------------------------------------------------------------------

  @Test
  public void writeSheetsExcel_nullList_noNpe_sheetNameHonored() throws Exception {
    Path file = newTempFile(".xlsx");
    SheetExcel se = new SheetExcel("MyNullSheet", null);

    bing.writeXlsx(file.toString(), se);

    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals("MyNullSheet", wb.getSheetName(0));
      assertEquals(1, wb.getNumberOfSheets());
    }
  }

  @Test
  public void writeSheetsExcel_emptyList_noNpe_sheetNameHonored() throws Exception {
    Path file = newTempFile(".xlsx");
    SheetExcel se = new SheetExcel("MyEmptySheet", Collections.emptyList());

    bing.writeXlsx(file.toString(), se);

    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals("MyEmptySheet", wb.getSheetName(0));
      assertEquals(1, wb.getNumberOfSheets());
    }
  }

  // ------------------------------------------------------------------
  // A2: writeHeader 对 null alias 不再 NPE
  // ------------------------------------------------------------------

  @Test
  public void writeHeader_nullAlias_noNpe() throws Exception {
    Path file = newTempFile(".xlsx");
    WriteHandler handler = ExcelWriterFactory.createXSSF(file.toString());

    List<CellKV<String>> header = new ArrayList<>();
    header.add(new CellKV<>(0, null));
    header.add(new CellKV<>(1, "Name"));

    handler.createSheet("test");
    handler.writeHeader(header);
    handler.flush();

    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      Row headerRow = wb.getSheetAt(0).getRow(0);
      assertEquals("", headerRow.getCell(0).getStringCellValue());
      assertEquals("Name", headerRow.getCell(1).getStringCellValue());
    }
  }

  @Test
  public void fieldConverter_nullAlias_writeNoNpe() throws Exception {
    Path file = newTempFile(".xlsx");
    BingExcel b = BingExcelBuilder.toBuilder()
        .addFieldConversionMapper(NullAliasModel.class, "field", 0, null, null)
        .build();

    List<NullAliasModel> list = new ArrayList<>();
    list.add(new NullAliasModel("hello"));
    b.writeXlsx(file.toString(), list);

    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      Cell headerCell = wb.getSheetAt(0).getRow(0).getCell(0);
      assertEquals("", headerCell.getStringCellValue());
      Cell dataCell = wb.getSheetAt(0).getRow(1).getCell(0);
      assertEquals("hello", dataCell.getStringCellValue());
    }
  }

  // ------------------------------------------------------------------
  // A3: 自定义 converter 返回 OutValue(value=null) 不再 NPE
  // ------------------------------------------------------------------

  @Test
  public void marshal_outValueWithNullValue_noNpe() throws Exception {
    Path file = newTempFile(".xlsx");
    BingExcel b = BingExcelBuilder.toBuilder()
        .addFieldConversionMapper(NullOutModel.class, "skipField", 1, "Skip",
            new NullOutValueConverter())
        .build();

    List<NullOutModel> list = new ArrayList<>();
    list.add(new NullOutModel("visible", "should-be-skipped"));
    b.writeXlsx(file.toString(), list);

    ReaderCondition<NullOutModel> cond = new ReaderCondition<>(0, 1, NullOutModel.class);
    SheetVo<NullOutModel> result = b.readFile(file.toFile(), cond);
    assertNotNull(result);
    List<NullOutModel> rows = result.getObjectList();
    assertEquals(1, rows.size());
    assertEquals("visible", rows.get(0).visible);
    // skipField cell 未写入，读取后为 null
    assertEquals(null, rows.get(0).skipField);
  }

  // ------------------------------------------------------------------
  // 空 iterable 写出：产物为合法最小 xlsx
  // ------------------------------------------------------------------

  @Test
  public void writeXlsx_emptyList_validMinimalXlsx() throws Exception {
    Path file = newTempFile(".xlsx");
    List<SimpleModel> empty = Collections.emptyList();
    bing.writeXlsx(file.toString(), empty);

    assertTrue(Files.size(file) > 0);
    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals(1, wb.getNumberOfSheets());
    }
  }

  // ------------------------------------------------------------------
  // CSV: writeCsv(OutputStream) 不得关闭调用方的流
  // ------------------------------------------------------------------

  @Test
  public void writeCsv_toOutputStream_streamStillOpenAfterReturn() throws Exception {
    CloseTrackingStream out = new CloseTrackingStream();
    List<SimpleModel> list = Arrays.asList(
        new SimpleModel("a"), new SimpleModel("b"));

    bing.writeCsv(out, list);

    assertFalse("writeCsv must not close caller's stream", out.closed);
    String csv = new String(out.toByteArray(), StandardCharsets.UTF_8);
    assertTrue(csv.contains("a"));
    // 流仍可用：可以继续写数据
    out.write(0x41);
    out.flush();
    assertEquals('A', out.toByteArray()[out.size() - 1]);
    out.close();
  }

  @Test
  public void writeCsv_emptyIterable_streamStillOpenAndFlushed() throws Exception {
    CloseTrackingStream out = new CloseTrackingStream();

    bing.writeCsv(out, Collections.emptyList());

    assertFalse("writeCsv must not close caller's stream", out.closed);
    out.close();
  }

  /** 记录 close 调用的 OutputStream，用于验证调用方流未被关闭。 */
  public static class CloseTrackingStream extends ByteArrayOutputStream {
    boolean closed = false;

    @Override
    public void close() throws IOException {
      closed = true;
      super.close();
    }
  }

  // ------------------------------------------------------------------
  // 空 iterable 变体：全 null 元素、零 varargs、null iterable
  // ------------------------------------------------------------------

  @Test
  public void writeXlsx_allNullElements_validMinimalXlsx() throws Exception {
    Path file = newTempFile(".xlsx");
    List<SimpleModel> list = Arrays.asList(null, null);
    bing.writeXlsx(file.toString(), list);

    assertTrue(Files.size(file) > 0);
    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals(1, wb.getNumberOfSheets());
      // 无数据行：POI 空 sheet 的 lastRowNum 为 -1
      assertEquals(-1, wb.getSheetAt(0).getLastRowNum());
    }
  }

  @Test
  public void writeXlsx_zeroVarargs_validMinimalXlsx() throws Exception {
    Path file = newTempFile(".xlsx");
    // String+零 varargs 在 Iterable/SheetExcel 两个重载间有歧义，用 File 重载验证
    bing.writeXlsx(file.toFile());

    assertTrue(Files.size(file) > 0);
    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals(1, wb.getNumberOfSheets());
    }
  }

  @Test
  public void writeSheetsExcel_mixedNullEntries_sheetPerEntry() throws Exception {
    Path file = newTempFile(".xlsx");
    SheetExcel se1 = new SheetExcel("Real", Arrays.asList(new SimpleModel("a")));
    SheetExcel se2 = new SheetExcel("Empty", Collections.emptyList());
    SheetExcel se3 = null;

    bing.writeXlsx(file.toString(), se1, se2, se3);

    try (Workbook wb = WorkbookFactory.create(file.toFile())) {
      assertEquals(3, wb.getNumberOfSheets());
      assertEquals("Real", wb.getSheetName(0));
      assertEquals("Empty", wb.getSheetName(1));
      // null SheetExcel 按位置 i+1 命名
      assertEquals(2, wb.getSheetIndex("sheet3"));
    }
  }

  // ==================== 测试模型 ====================

  public static class NullAliasModel {
    @CellConfig(index = 0)
    public String field;

    public NullAliasModel() {}

    public NullAliasModel(String field) {
      this.field = field;
    }
  }

  public static class NullOutModel {
    @CellConfig(index = 0, aliasName = "Visible")
    public String visible;

    public String skipField;

    public NullOutModel() {}

    public NullOutModel(String visible, String skipField) {
      this.visible = visible;
      this.skipField = skipField;
    }
  }

  public static class SimpleModel {
    @CellConfig(index = 0)
    public String name;

    public SimpleModel() {}

    public SimpleModel(String name) {
      this.name = name;
    }
  }

  /** 返回 OutValue(value=null) 的自定义 converter，用于验证 A3 修复。 */
  public static class NullOutValueConverter implements FieldValueConverter {
    @Override
    public boolean canConvert(Class<?> clz) {
      return clz.equals(String.class);
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
      return OutValue.stringValue(null);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
      return cell;
    }
  }
}

package com.bing.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;

/**
 * Tests for per-condition {@code endRow}.
 *
 * Fixtures: 2 sheets, row 0 = header (Name, Age), rows 1..30 = data.
 * {@code endRow} is a 0-based exclusive end index, so {@code startRow=1, endRow=10}
 * yields rows 1..9.
 */
public class ReadEndRowTest {

  private static final int ROWS = 30;

  private static InputStream build(boolean xls) throws Exception {
    Workbook wb = xls ? new HSSFWorkbook() : new XSSFWorkbook();
    for (int s = 0; s < 2; s++) {
      Sheet sheet = wb.createSheet("s" + s);
      Row header = sheet.createRow(0);
      header.createCell(0).setCellValue("Name");
      header.createCell(1).setCellValue("Age");
      for (int r = 1; r <= ROWS; r++) {
        Row row = sheet.createRow(r);
        Cell name = row.createCell(0);
        name.setCellValue("s" + s + "-r" + r);
        row.createCell(1).setCellValue(r);
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    wb.write(baos);
    wb.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  private static ReaderCondition<Item> condition(int sheetIndex, int endRow) {
    return new ReaderCondition<Item>(sheetIndex, 1, Item.class).setEndRow(endRow);
  }

  private static void assertPerSheetEndRow(boolean xls) throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = build(xls)) {
      List<SheetVo> list = bing.readStreamToList(in,
          new ReaderCondition[] {condition(0, 10), condition(1, Integer.MAX_VALUE)});
      Assert.assertEquals(2, list.size());

      List<Item> sheet0 = list.get(0).getObjectList();
      List<Item> sheet1 = list.get(1).getObjectList();

      // sheet0 的 endRow=10 只截断 sheet0
      Assert.assertEquals(9, sheet0.size());
      Assert.assertEquals("s0-r1", sheet0.get(0).getName());
      Assert.assertEquals("s0-r9", sheet0.get(sheet0.size() - 1).getName());

      // sheet1 不受 sheet0 的 endRow 影响
      Assert.assertEquals(ROWS, sheet1.size());
      Assert.assertEquals("s1-r1", sheet1.get(0).getName());
      Assert.assertEquals("s1-r" + ROWS, sheet1.get(sheet1.size() - 1).getName());
    }
  }

  @Test
  public void perSheetEndRow_xlsx() throws Exception {
    assertPerSheetEndRow(false);
  }

  @Test
  public void perSheetEndRow_xls() throws Exception {
    assertPerSheetEndRow(true);
  }

  @Test
  public void singleCondition_endRowStillLimits() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = build(false)) {
      SheetVo<Item> vo = bing.readStream(in, condition(0, 10));
      List<Item> list = vo.getObjectList();
      Assert.assertEquals(9, list.size());
      Assert.assertEquals("s0-r9", list.get(list.size() - 1).getName());
    }
  }

  /** endRow 不能吃掉 startRow-1 的表头行，否则 aliasName 解析拿不到标题。 */
  @Test
  public void endRowKeepsAliasTitleRow() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = build(false)) {
      ReaderCondition<Aliased> c =
          new ReaderCondition<Aliased>(0, 1, Aliased.class).setEndRow(5);
      SheetVo<Aliased> vo = bing.readStream(in, c);
      List<Aliased> list = vo.getObjectList();
      Assert.assertEquals(4, list.size());
      Assert.assertEquals("s0-r1", list.get(0).getName());
      Assert.assertEquals(1, list.get(0).getAge());
    }
  }

  public static class Item {
    @CellConfig(index = 0)
    private String name;
    @CellConfig(index = 1)
    private int age;

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }
  }

  public static class Aliased {
    @CellConfig(aliasName = "Name")
    private String name;
    @CellConfig(aliasName = "Age")
    private int age;

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }
  }
}

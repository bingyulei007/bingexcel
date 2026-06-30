package com.bing.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import com.bing.excel.exception.IllegalCellConfigException;
import com.bing.utils.ToStringHelper;
import java.util.ArrayList;

/**
 * Tests for the aliasName-based column matching feature.
 *
 * Excel fixtures are generated on-the-fly via POI to avoid committing binary files.
 * Layout used: row 0 = header (Name, Age, Salary), rows 1-3 = data.
 */
public class ReadTestAlias {

  private static final String[] HEADER = {"Name", "Age", "Salary"};
  private static final Object[][] DATA = {
      {"Alice", 28, 9000.0},
      {"Bob", 35, 12500.5},
      {"Carol", 42, 18000.75}};

  private static InputStream buildExcelStream(String... titles) throws IOException {
    XSSFWorkbook wb = new XSSFWorkbook();
    Sheet sheet = wb.createSheet();
    Row header = sheet.createRow(0);
    for (int i = 0; i < titles.length; i++) {
      header.createCell(i).setCellValue(titles[i]);
    }
    for (int r = 0; r < DATA.length; r++) {
      Row row = sheet.createRow(r + 1);
      for (int c = 0; c < titles.length && c < DATA[r].length; c++) {
        Cell cell = row.createCell(c);
        Object v = DATA[r][c];
        if (v instanceof Number) {
          cell.setCellValue(((Number) v).doubleValue());
        } else {
          cell.setCellValue(String.valueOf(v));
        }
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    wb.write(baos);
    wb.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /**
   * Builds a sheet with no header row: data rows start at {@code firstDataRow}, so row 0
   * (the title row when startRow=1) is genuinely absent from the serialized XML and is
   * never delivered to optRow.
   */
  private static InputStream buildExcelStreamDataOnly(int firstDataRow) throws IOException {
    XSSFWorkbook wb = new XSSFWorkbook();
    Sheet sheet = wb.createSheet();
    for (int r = 0; r < DATA.length; r++) {
      Row row = sheet.createRow(firstDataRow + r);
      for (int c = 0; c < DATA[r].length; c++) {
        Cell cell = row.createCell(c);
        Object v = DATA[r][c];
        if (v instanceof Number) {
          cell.setCellValue(((Number) v).doubleValue());
        } else {
          cell.setCellValue(String.valueOf(v));
        }
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    wb.write(baos);
    wb.close();
    return new ByteArrayInputStream(baos.toByteArray());
  }

  @Test
  public void readByAlias_succeeds() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream(HEADER)) {
      SheetVo<Person> vo = bing.readStream(in, Person.class, 1);
      List<Person> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      org.junit.Assert.assertEquals(28, list.get(0).getAge());
      org.junit.Assert.assertEquals(Double.valueOf(9000.0), list.get(0).getSalary());
      org.junit.Assert.assertEquals("Bob", list.get(1).getName());
      org.junit.Assert.assertEquals("Carol", list.get(2).getName());
    }
  }

  @Test
  public void readByAlias_explicitIndexWins() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream(HEADER)) {
      SheetVo<IndexedPerson> vo = bing.readStream(in, IndexedPerson.class, 1);
      List<IndexedPerson> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      // index=0 → first column → "Alice", "Bob", "Carol"
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      org.junit.Assert.assertEquals(28, list.get(0).getAge());
      org.junit.Assert.assertEquals(Double.valueOf(9000.0), list.get(0).getSalary());
    }
  }

  @Test(expected = IllegalCellConfigException.class)
  public void readByAlias_titleMissing_throws() throws Exception {
    // Drop the "Salary" column from the header so the aliasName "Salary" cannot resolve.
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream("Name", "Age")) {
      bing.readStream(in, Person.class, 1);
    }
  }

  @Test
  public void readByAlias_titleMissing_messageHasAvailableTitles() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream("Name", "Age")) {
      bing.readStream(in, Person.class, 1);
      org.junit.Assert.fail("expected IllegalCellConfigException");
    } catch (IllegalCellConfigException e) {
      String msg = e.getMessage();
      org.junit.Assert.assertTrue(
          "message should mention aliasName 'Salary': " + msg,
          msg.contains("Salary"));
      org.junit.Assert.assertTrue(
          "message should mention 'available titles': " + msg,
          msg.contains("available titles"));
    }
  }

  @Test(expected = IllegalCellConfigException.class)
  public void readByAlias_startRowZero_throws() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream(HEADER)) {
      // startRow=0 → no title row to resolve aliasNames against.
      bing.readStream(in, Person.class, 0);
    }
  }

  @Test
  public void readByAlias_headerWithSurroundingSpaces_matches() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream(" Name ", " Age ", " Salary ")) {
      SheetVo<Person> vo = bing.readStream(in, Person.class, 1);
      List<Person> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      org.junit.Assert.assertEquals(28, list.get(0).getAge());
      org.junit.Assert.assertEquals(Double.valueOf(9000.0), list.get(0).getSalary());
    }
  }

  @Test
  public void readByAlias_headerCaseDiffers_matches() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream("name", "age", "salary")) {
      SheetVo<Person> vo = bing.readStream(in, Person.class, 1);
      List<Person> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      org.junit.Assert.assertEquals(28, list.get(0).getAge());
      org.junit.Assert.assertEquals(Double.valueOf(9000.0), list.get(0).getSalary());
    }
  }

  @Test
  public void readByAlias_titleRowAbsent_throwsReadable_notAioobe() throws Exception {
    // The title row at startRow-1 (=0) is absent: data starts at row 1, so resolve()
    // never runs and the alias-only fields keep index -1. Reading the first data row
    // must throw IllegalCellConfigException, not ArrayIndexOutOfBoundsException.
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStreamDataOnly(1)) {
      bing.readStream(in, Person.class, 1);
      org.junit.Assert.fail("expected IllegalCellConfigException");
    } catch (ArrayIndexOutOfBoundsException e) {
      org.junit.Assert.fail("should throw IllegalCellConfigException, not AIOOBE: " + e);
    } catch (IllegalCellConfigException e) {
      org.junit.Assert.assertTrue(
          "message should mention the unresolved required field: " + e.getMessage(),
          e.getMessage().contains("salary") || e.getMessage().contains("Salary"));
    }
  }

  @Test
  public void readByAlias_userDefinedIndexOverridesMissingTitle() throws Exception {
    BingExcel bing = BingExcelBuilder.toBuilder()
        .addFieldConversionMapper(Person.class, "name", 0, "Name")
        .addFieldConversionMapper(Person.class, "age", 1, "Age")
        .addFieldConversionMapper(Person.class, "salary", 2, "Salary")
        .build();
    try (InputStream in = buildExcelStream("WrongName", "WrongAge", "WrongSalary")) {
      SheetVo<Person> vo = bing.readStream(in, Person.class, 1);
      List<Person> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      org.junit.Assert.assertEquals(28, list.get(0).getAge());
      org.junit.Assert.assertEquals(Double.valueOf(9000.0), list.get(0).getSalary());
    }
  }

  @Test
  public void readByAlias_userDefinedIndexAllowsStartRowZero() throws Exception {
    BingExcel bing = BingExcelBuilder.toBuilder()
        .addFieldConversionMapper(NameOnly.class, "name", 0, "Name")
        .build();
    try (InputStream in = buildExcelStream("WrongName", "WrongAge", "WrongSalary")) {
      SheetVo<NameOnly> vo = bing.readStream(in, NameOnly.class, 0);
      List<NameOnly> list = vo.getObjectList();
      org.junit.Assert.assertEquals(4, list.size());
      org.junit.Assert.assertEquals("WrongName", list.get(0).getName());
      org.junit.Assert.assertEquals("Alice", list.get(1).getName());
    }
  }

  @Test(expected = IllegalCellConfigException.class)
  public void writeAliasOnly_throws() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    List<Person> list = new ArrayList<>();
    list.add(new Person());
    File tmp = File.createTempFile("alias_only_write", ".xlsx");
    tmp.deleteOnExit();
    bing.writeXlsx(new FileOutputStream(tmp), list);
  }

  @Test(expected = IllegalCellConfigException.class)
  public void writeCsvAliasOnly_throws() throws Exception {
    BingExcel bing = BingExcelBuilder.builderInstance();
    List<Person> list = new ArrayList<>();
    list.add(new Person());
    File tmp = File.createTempFile("alias_only_write", ".csv");
    tmp.deleteOnExit();
    bing.writeCsv(tmp.getAbsolutePath(), list);
  }

  @Test(expected = IllegalCellConfigException.class)
  public void noIndexNoAlias_throwsAtRegistration() throws Exception {
    // Without explicit index or aliasName, registration must fail.
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream(HEADER)) {
      bing.readStream(in, BareFieldPerson.class, 1);
    }
  }

  public static class Person {
    @CellConfig(aliasName = "Name")
    private String name;
    @CellConfig(aliasName = "Age")
    private int age;
    @CellConfig(aliasName = "Salary", readRequired = true)
    private Double salary;

    public String getName() { return name; }
    public int getAge() { return age; }
    public Double getSalary() { return salary; }

    @Override
    public String toString() {
      return ToStringHelper.of(this).add("name", name)
          .add("age", age).add("salary", salary).toString();
    }
  }

  public static class NameOnly {
    @CellConfig(aliasName = "Name")
    private String name;

    public String getName() { return name; }
  }

  /** Both index and aliasName set: index must take precedence. */
  public static class IndexedPerson {
    @CellConfig(index = 0, aliasName = "WrongName")
    private String name;
    @CellConfig(index = 1, aliasName = "WrongAge")
    private int age;
    @CellConfig(index = 2, aliasName = "WrongSalary")
    private Double salary;

    public String getName() { return name; }
    public int getAge() { return age; }
    public Double getSalary() { return salary; }
  }

  /** Neither index nor aliasName: registration must fail fast. */
  public static class BareFieldPerson {
    @CellConfig
    private String name;

    public String getName() { return name; }
  }

  // ---- Tests for optional aliasName fields (not readRequired) ----

  @Test
  public void readByAlias_optionalFieldMissing_returnsNull() throws Exception {
    // Non-required aliasName fields should NOT throw when the header column is
    // missing; the field simply stays null.
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream("NameOnly")) {
      SheetVo<OptionalPerson> vo = bing.readStream(in, OptionalPerson.class, 1);
      List<OptionalPerson> list = vo.getObjectList();
      org.junit.Assert.assertEquals(3, list.size());
      org.junit.Assert.assertEquals("Alice", list.get(0).getName());
      // "Age" column is missing → should be null (not throw)
      org.junit.Assert.assertNull(list.get(0).getAge());
      org.junit.Assert.assertEquals("Bob", list.get(1).getName());
      org.junit.Assert.assertNull(list.get(1).getAge());
    }
  }

  @Test
  public void readByAlias_optionalFieldMissing_requiredStillThrows() throws Exception {
    // Person has Salary with readRequired=true → missing "Salary" header still throws.
    BingExcel bing = BingExcelBuilder.builderInstance();
    try (InputStream in = buildExcelStream("Name", "Age")) {
      bing.readStream(in, Person.class, 1);
      org.junit.Assert.fail("expected IllegalCellConfigException for required field Salary");
    } catch (IllegalCellConfigException e) {
      org.junit.Assert.assertTrue(
          "message should mention 'required': " + e.getMessage(),
          e.getMessage().contains("required"));
    }
  }

  public static class OptionalPerson {
    @CellConfig(aliasName = "NameOnly")
    private String name;
    @CellConfig(aliasName = "Age")
    private Integer age;

    public String getName() { return name; }
    public Integer getAge() { return age; }
  }
}

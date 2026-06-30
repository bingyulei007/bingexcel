package com.bing.demo;

import com.bing.demo.model.AliasOnlyPerson;
import com.bing.demo.model.MixedIndexPerson;
import com.bing.demo.model.Person;
import com.bing.demo.model.RequiredPerson;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import com.bing.excel.exception.IllegalCellConfigException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BingExcel 读取场景全面测试。
 *
 * <p>覆盖以下场景:
 * <ol>
 *   <li>头匹配读取 (aliasName-only, 无 index)</li>
 *   <li>索引读取 (index-based)</li>
 *   <li>混合 index + aliasName 读取</li>
 *   <li>列缺失 (可选字段 → null)</li>
 *   <li>列缺失 (必填字段 → 抛异常)</li>
 *   <li>空单元格 → null</li>
 *   <li>错误头名称 → 抛异常</li>
 *   <li>多余列 → 忽略</li>
 *   <li>仅表头无数据 → 空列表</li>
 *   <li>.xls 格式读取</li>
 *   <li>写后读 (数据完整性)</li>
 *   <li>头匹配大小写不敏感</li>
 * </ol>
 */
class BingExcelReadTest {

    private BingExcel bingExcel;
    private List<Path> tempFiles;

    @BeforeEach
    void setUp() {
        bingExcel = BingExcelBuilder.builderInstance();
        tempFiles = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        for (Path f : tempFiles) {
            try { Files.deleteIfExists(f); } catch (IOException ignored) {}
        }
        tempFiles.clear();
    }

    private Path newTempFile(String suffix) throws IOException {
        Path p = Files.createTempFile("bing-test-", suffix);
        tempFiles.add(p);
        return p;
    }

    // ================================================================
    // 1. aliasName 头匹配读取
    // ================================================================

    @Test
    void readByAliasName_matchesByHeader() throws Exception {
        // 构造 Excel: 表头行 + 2 行数据
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Score");
            header.createCell(3).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue(30);
            r1.createCell(2).setCellValue(95.5);
            r1.createCell(3).setCellValue("alice@test.com");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Bob");
            r2.createCell(1).setCellValue(25);
            r2.createCell(2).setCellValue(88.0);
            r2.createCell(3).setCellValue("bob@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<AliasOnlyPerson> list = result.getObjectList();
        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).getName());
        assertEquals(30, list.get(0).getAge());
        assertEquals(95.5, list.get(0).getScore(), 0.0001);
        assertEquals("alice@test.com", list.get(0).getEmail());
        assertEquals("Bob", list.get(1).getName());
        assertEquals(25, list.get(1).getAge());
    }

    @Test
    void readByAliasName_columnsOutOfOrder() throws Exception {
        // 头顺序与字段声明顺序不同，应能正确匹配
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Email");
            header.createCell(1).setCellValue("Score");
            header.createCell(2).setCellValue("Age");
            header.createCell(3).setCellValue("Name");  // Name 在最后一列

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("alice@test.com");
            r1.createCell(1).setCellValue(95.5);
            r1.createCell(2).setCellValue(30);
            r1.createCell(3).setCellValue("Alice");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals("Alice", result.getObjectList().get(0).getName());
        assertEquals(30, result.getObjectList().get(0).getAge());
    }

    @Test
    void readByAliasName_caseInsensitive() throws Exception {
        // 头 "name" 应匹配 aliasName="Name"
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("name");
            header.createCell(1).setCellValue("age");
            header.createCell(2).setCellValue("score");
            header.createCell(3).setCellValue("email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Carol");
            r1.createCell(1).setCellValue(22);
            r1.createCell(2).setCellValue(77.0);
            r1.createCell(3).setCellValue("carol@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals("Carol", result.getObjectList().get(0).getName());
    }

    @Test
    void readByAliasName_headerWithSpaces() throws Exception {
        // 头 "  Name  " 应匹配 aliasName="Name" (前后空格)
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("  Name  ");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Score");
            header.createCell(3).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Dave");
            r1.createCell(1).setCellValue(28);
            r1.createCell(2).setCellValue(66.0);
            r1.createCell(3).setCellValue("dave@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals("Dave", result.getObjectList().get(0).getName());
    }

    // ================================================================
    // 2. index 索引读取
    // ================================================================

    @Test
    void readByIndex_success() throws Exception {
        // BingExcel.writeXlsx 写出 Person，再读回
        Path file = newTempFile(".xlsx");
        List<Person> data = new ArrayList<>();
        data.add(new Person("Alice", 30, 8000.0, 0));
        data.add(new Person("Bob", 25, 6000.0, 1));
        data.add(new Person("Carol", 22, 5000.0, 0));

        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            bingExcel.writeXlsx(fos, data);
        }

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals(3, result.getObjectList().size());
        assertEquals("Alice", result.getObjectList().get(0).getName());
        assertEquals(30, result.getObjectList().get(0).getAge());
    }

    @Test
    void readByIndex_overridesAliasName() throws Exception {
        // 即使有表头行，index 不为 -1 时用 index 读取
        // Excel 表头故意交换: col0="Age", col1="Name"
        // Person: col0→name(String), col1→age(Integer)
        // col1 放入非数字字符串 → NumberFormatException
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Age");   // 故意把 Age 放在第 0 列
            header.createCell(1).setCellValue("Name");  // 故意把 Name 放在第 1 列

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(33);          // 数字 → name(String) "33" OK
            r1.createCell(1).setCellValue("NOT_A_NUMBER"); // 文本 → age(Integer) 解析失败

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        // Person 的 index=0 是 name, index=1 是 age
        // col0=33 → name="33" OK; col1="NOT_A_NUMBER" → NumberFormatException
        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        assertThrows(Exception.class, () -> bingExcel.readFile(file.toFile(), cond));
    }

    // ================================================================
    // 3. 混合 index + aliasName
    // ================================================================

    @Test
    void readMixedIndexAndAliasName() throws Exception {
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Department");
            header.createCell(3).setCellValue("Level");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1);
            r1.createCell(1).setCellValue("Alice");
            r1.createCell(2).setCellValue("Engineering");
            r1.createCell(3).setCellValue(5);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<MixedIndexPerson> cond = new ReaderCondition<>(0, 1, MixedIndexPerson.class);
        SheetVo<MixedIndexPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        MixedIndexPerson person = result.getObjectList().get(0);
        assertEquals(1, person.getId());           // index=0
        assertEquals("Alice", person.getName());    // index=1
        assertEquals("Engineering", person.getDepartment()); // aliasName match
        assertEquals(5, person.getLevel());        // aliasName match
    }

    // ================================================================
    // 4. 列缺失 (可选字段 → null)
    // ================================================================

    @Test
    void readMissingOptionalColumn_returnsNull() throws Exception {
        // AliasOnlyPerson 有 4 个 aliasName-only 字段 (都不是 readRequired)
        // Excel 只有 Name/Age 两列 → Score,Email 头缺失 → 应为 null
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Partial");
            r1.createCell(1).setCellValue(40);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        AliasOnlyPerson person = result.getObjectList().get(0);
        assertEquals("Partial", person.getName());
        assertEquals(40, person.getAge());
        assertNull(person.getScore());   // aliasName 不在表头 → null
        assertNull(person.getEmail());   // aliasName 不在表头 → null
    }

    // ================================================================
    // 5. 列缺失 (必填字段 → 抛异常)
    // ================================================================

    @Test
    void readMissingRequiredColumn_throws() throws Exception {
        // RequiredPerson.name 是 readRequired=true (index=1)
        // Excel 只写 ID(index=0) 和 Email(index=2), 跳过 Name(index=1)
        // → fullArray[1] 为空/null → required 检查应抛异常
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1);
            // col1 (Name) 不写值 — 空白单元格
            r1.createCell(2).setCellValue("test@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<RequiredPerson> cond = new ReaderCondition<>(0, 1, RequiredPerson.class);
        assertThrows(Exception.class, () -> bingExcel.readFile(file.toFile(), cond));
    }

    @Test
    void readRequiredFieldPresent_success() throws Exception {
        // RequiredPerson 所有字段都提供 → 正常读取
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1);
            r1.createCell(1).setCellValue("Alice");
            r1.createCell(2).setCellValue("alice@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<RequiredPerson> cond = new ReaderCondition<>(0, 1, RequiredPerson.class);
        SheetVo<RequiredPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals(1, result.getObjectList().get(0).getId());
        assertEquals("Alice", result.getObjectList().get(0).getName());
    }

    // ================================================================
    // 6. 空单元格 → null
    // ================================================================

    @Test
    void readEmptyCells_returnsNull() throws Exception {
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Score");
            header.createCell(3).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("HasEmail");
            r1.createCell(1).setCellValue(30);
            // col2 (Score) 不设置 → blank → null
            r1.createCell(3).setCellValue("has@test.com");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("NoEmail");
            r2.createCell(1).setCellValue(25);
            r2.createCell(2).setCellValue(88.0);
            // col3 (Email) 不设置 → blank → null

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<AliasOnlyPerson> list = result.getObjectList();
        assertEquals(2, list.size());
        assertNull(list.get(0).getScore());
        assertEquals("has@test.com", list.get(0).getEmail());
        assertNull(list.get(1).getEmail());
        assertEquals(88.0, list.get(1).getScore(), 0.0001);
    }

    // ================================================================
    // 7. 错误头名称 → 抛异常
    // ================================================================

    @Test
    void readMismatchedHeaders_optionalFieldsNull() throws Exception {
        // AliasOnlyPerson 的非必填字段:
        // 所有 aliasName 都不匹配表头 → 全部跳过 → 对象字段均为 null
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("FullName");
            header.createCell(1).setCellValue("Years");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Test");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        AliasOnlyPerson person = result.getObjectList().get(0);
        // 所有 aliasName 不匹配 → 全部字段跳过 → null
        assertNull(person.getName());
        assertNull(person.getAge());
        assertNull(person.getScore());
        assertNull(person.getEmail());
    }

    @Test
    void readWrongHeader_noTitleRow() throws Exception {
        // AliasOnlyPerson 使用 alias-only，但 startRow=0 → 没有标题行 → 应在 startSheet 就抛异常
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row r1 = sheet.createRow(0);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue(30);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 0, AliasOnlyPerson.class);
        // startRow=0 且 aliasOnly → BingExcelReaderListener.startSheet 会检测并抛异常
        assertThrows(IllegalCellConfigException.class, () -> bingExcel.readFile(file.toFile(), cond));
    }

    @Test
    void readRequiredAliasNameMissing_throws() throws Exception {
        // RequiredPerson.name 是 readRequired=true (index=1)
        // Excel 表头只提供 ID/Email 两列，缺少 "Name"
        // → 必填字段的 aliasName 缺失 → 应抛异常
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue(1);
            r1.createCell(1).setCellValue("test@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<RequiredAliasPerson> cond = new ReaderCondition<>(0, 1, RequiredAliasPerson.class);
        assertThrows(IllegalCellConfigException.class, () -> bingExcel.readFile(file.toFile(), cond));
    }

    /** alias-only 实体，Name 字段为 readRequired */
    public static class RequiredAliasPerson {
        @com.bing.excel.annotation.CellConfig(index = -1, aliasName = "Name", readRequired = true)
        private String name;
        @com.bing.excel.annotation.CellConfig(index = -1, aliasName = "Age")
        private Integer age;
        @com.bing.excel.annotation.CellConfig(index = -1, aliasName = "Email")
        private String email;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ================================================================
    // 8. 多余列 → 忽略
    // ================================================================

    @Test
    void readExtraColumns_ignored() throws Exception {
        // Excel 有 6 列，Person 只配了 4 个字段 → 多余列被忽略
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Salary");
            header.createCell(3).setCellValue("Gender");
            header.createCell(4).setCellValue("Extra1");
            header.createCell(5).setCellValue("Extra2");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue(28);
            r1.createCell(2).setCellValue(8500.0);
            r1.createCell(3).setCellValue(0);
            r1.createCell(4).setCellValue("ignored");
            r1.createCell(5).setCellValue(999);

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        Person person = result.getObjectList().get(0);
        assertEquals("Alice", person.getName());
        assertEquals(28, person.getAge());
    }

    // ================================================================
    // 9. 仅表头无数据 → 空列表
    // ================================================================

    @Test
    void readHeaderOnly_returnsEmptyOrNull() throws Exception {
        // index-based Person, header-only (no data rows)
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Salary");
            header.createCell(3).setCellValue("Gender");
            // 没有数据行
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        if (result != null) {
            assertTrue(result.getObjectList().isEmpty());
        }
    }

    // ================================================================
    // 10. .xls 格式读取
    // ================================================================

    @Test
    void readXlsFormat_success() throws Exception {
        Path file = newTempFile(".xls");
        try (Workbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Age");
            header.createCell(2).setCellValue("Score");
            header.createCell(3).setCellValue("Email");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("XlsAlice");
            r1.createCell(1).setCellValue(20);
            r1.createCell(2).setCellValue(90.0);
            r1.createCell(3).setCellValue("xls@test.com");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<AliasOnlyPerson> cond = new ReaderCondition<>(0, 1, AliasOnlyPerson.class);
        SheetVo<AliasOnlyPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        assertEquals("XlsAlice", result.getObjectList().get(0).getName());
        assertEquals(20, result.getObjectList().get(0).getAge());
    }

    // ================================================================
    // 11. 写后读 (数据完整性)
    // ================================================================

    @Test
    void readRoundtrip_dataIntegrity() throws Exception {
        // 用 BingExcel 写出 Person → 再读回 → 验证数据一致
        List<Person> original = new ArrayList<>();
        original.add(new Person("Alice", 28, 8500.5, 0));
        original.add(new Person("Bob", 35, 12000.0, 1));
        original.add(new Person("Charlie", 42, 15000.75, 1));

        Path file = newTempFile(".xlsx");
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            bingExcel.writeXlsx(fos, original);
        }

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<Person> roundtrip = result.getObjectList();
        assertEquals(original.size(), roundtrip.size());

        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).getName(), roundtrip.get(i).getName());
            assertEquals(original.get(i).getAge(), roundtrip.get(i).getAge());
            assertEquals(original.get(i).getSalary(), roundtrip.get(i).getSalary(), 0.001);
            assertEquals(original.get(i).getGender(), roundtrip.get(i).getGender());
        }
    }

    // ================================================================
    // 12. 无 @CellConfig 注解的类 → 该忽略
    // ================================================================

    @Test
    void readClassWithoutCellConfig_ignored() throws Exception {
        // NoConfigPerson 没有 @CellConfig → 所有字段都应该被跳过
        // 实际上在 addMapper() 中, 字段没有 @CellConfig 时直接 return; → 不会被注册
        // 因此 boundFields 为空 → 创建出来的对象所有字段都是 null
        // 但 construct 和 read 本身不应抛异常
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row r1 = sheet.createRow(0);
            r1.createCell(0).setCellValue("no annotation");

            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<NoConfigPerson> cond = new ReaderCondition<>(0, 0, NoConfigPerson.class);
        SheetVo<NoConfigPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        NoConfigPerson person = result.getObjectList().get(0);
        assertNull(person.getValue());
        assertNull(person.getCount());
    }

    /** 无 @CellConfig 的实体 */
    public static class NoConfigPerson {
        private String value;
        private Integer count;
        public String getValue() { return value; }
        public Integer getCount() { return count; }
    }

    // ================================================================
    // 13. 空文件 / 不存在的文件
    // ================================================================

    @Test
    void readNonExistentFile_throws() {
        java.io.File nonExistent = new java.io.File("/nonexistent/path/test.xlsx");
        assertThrows(Exception.class, () -> {
            ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
            bingExcel.readFile(nonExistent, cond);
        });
    }

    @Test
    void readEmptyIterable_writeStillWorks() throws Exception {
        // 空列表写出 → 应创建一个空 sheet
        Path file = newTempFile(".xlsx");
        List<Person> empty = new ArrayList<>();

        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            assertDoesNotThrow(() -> bingExcel.writeXlsx(fos, empty));
        }

        // 空文件可能无法读取，但写入不应抛异常
        assertTrue(Files.exists(file));
        assertTrue(Files.size(file) > 0);
    }
}

package com.bing.excel;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.converter.base.BooleanFieldConverter;
import com.bing.excel.converter.base.ByteFieldConverter;
import com.bing.excel.converter.base.DateFieldConverter;
import com.bing.excel.converter.base.FloatFieldConverter;
import com.bing.excel.converter.base.IntegerFieldConverter;
import com.bing.excel.converter.base.LongFieldConverter;
import com.bing.excel.converter.enums.EnumConVerter;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.impl.BingExcelImpl.SheetExcel;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.OutValue;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;
import com.bing.utils.ToStringHelper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Excel写入功能综合测试
 *
 * 覆盖场景：
 * 1. 基础xlsx格式导出
 * 2. 老版xls格式导出
 * 3. OutputStream导出
 * 4. 多Sheet导出
 * 5. CSV导出（默认和自定义配置）
 * 6. 使用@BingConvertor自定义转换器
 * 7. 空列表和null值处理
 * 8. 低级WriteHandler API
 */
public class WriteTest7 {

    private BingExcel bing;

    @Before
    public void before() {
        bing = BingExcelBuilder.toBuilder().build();
    }

    /**
     * 测试1: 基础xlsx格式导出
     */
    @Test
    public void testWriteXlsx() throws IOException {
        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_basic.xlsx";
        bing.writeXlsx(path, list);
        System.out.println("xlsx导出成功: " + path);
    }

    /**
     * 测试2: 老版xls格式导出
     */
    @Test
    public void testWriteXls() throws IOException {
        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_basic.xls";
        bing.writeXls(path, list);
        System.out.println("xls导出成功: " + path);
    }

    /**
     * 测试3: 通过OutputStream导出（可写入任何输出目标）
     */
    @Test
    public void testWriteToOutputStream() throws IOException {
        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_stream.xlsx";

        try (FileOutputStream fos = new FileOutputStream(path)) {
            bing.writeXlsx(fos, list);
        }
        System.out.println("OutputStream导出成功: " + path);
    }

    /**
     * 测试4: 多Sheet导出
     */
    @Test
    public void testWriteMultiSheets() throws IOException {
        SheetExcel sheet1 = new SheetExcel();
        sheet1.setSheetName("员工信息");
        sheet1.setList(createPersonList());

        SheetExcel sheet2 = new SheetExcel();
        sheet2.setSheetName("部门信息");
        List<Department> deptList = Arrays.asList(
            new Department("研发部", 50),
            new Department("市场部", 30),
            new Department("人事部", 10)
        );
        sheet2.setList(deptList);

        String path = System.getProperty("java.io.tmpdir") + "write_test_multi_sheets.xlsx";
        bing.writeXlsx(path, sheet1, sheet2);
        System.out.println("多Sheet xlsx导出成功: " + path);
    }

    /**
     * 测试4.1: xls格式多Sheet导出
     */
    @Test
    public void testWriteMultiSheetsXls() throws IOException {
        SheetExcel sheet1 = new SheetExcel();
        sheet1.setSheetName("员工信息");
        sheet1.setList(createPersonList());

        SheetExcel sheet2 = new SheetExcel();
        sheet2.setSheetName("部门信息");
        sheet2.setList(Arrays.asList(
            new Department("研发部", 50),
            new Department("市场部", 30),
            new Department("人事部", 10)
        ));

        String path = System.getProperty("java.io.tmpdir") + "write_test_multi_sheets.xls";
        bing.writeXls(path, sheet1, sheet2);
        System.out.println("多Sheet xls导出成功: " + path);
    }

    /**
     * 测试5: CSV导出（默认配置，逗号分隔，带表头和BOM）
     */
    @Test
    public void testWriteCsv() throws IOException {
        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_basic.csv";
        bing.writeCsv(path, list);
        System.out.println("CSV导出成功: " + path);
    }

    /**
     * 测试6: CSV导出（自定义配置 - 分号分隔、不带表头、无BOM）
     */
    @Test
    public void testWriteCsvWithConfig() throws IOException {
        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_custom_csv.csv";

        try (FileOutputStream fos = new FileOutputStream(path)) {
            // 参数: stream, list, 分隔符, 是否带表头, 是否带BOM
            bing.writeCsv(fos, list, ';', false, false);
        }
        System.out.println("自定义CSV导出成功: " + path);
    }

    /**
     * 测试7: 使用@BingConvertor自定义布尔转换
     * 布尔值转换为"是/否"而不是默认的"true/false"
     */
    @Test
    public void testWriteWithBingConvertor() throws IOException {
        List<User> list = Arrays.asList(
            new User("张三", true),
            new User("李四", false),
            new User("王五", true)
        );
        String path = System.getProperty("java.io.tmpdir") + "write_test_converter.xlsx";
        bing.writeXlsx(path, list);
        System.out.println("自定义转换器导出成功: " + path);
    }

    /**
     * 测试8: 空列表导出（应生成仅有表头的文件）
     */
    @Test
    public void testWriteEmptyList() throws IOException {
        List<Person> list = new ArrayList<>();
        String path = System.getProperty("java.io.tmpdir") + "write_test_empty.xlsx";
        bing.writeXlsx(path, list);
        System.out.println("空列表导出成功: " + path);
    }

    /**
     * 测试9: 使用低级WriteHandler API进行精细控制
     * 包含数据有效性（下拉框）和自定义表头
     */
    @Test
    public void testWriteWithHandler() throws IOException {
        String path = System.getProperty("java.io.tmpdir") + "write_test_handler.xlsx";
        WriteHandler handler = ExcelWriterFactory.createXSSF(path);
        handler.createSheet("用户管理");

        // 写入表头
        ListLine header = new ListLine()
            .addValue(0, "姓名")
            .addValue(1, "性别")
            .addValue(2, "年龄")
            .addValue(3, "状态");
        handler.writeHeader(header);

        // 写入数据行
        handler.writeLine(new ListLine().addValue(0, "张三").addValue(1, "男").addValue(2, 25).addValue(3, "激活"));
        handler.writeLine(new ListLine().addValue(0, "李四").addValue(1, "女").addValue(2, 30).addValue(3, "停用"));
        handler.writeLine(new ListLine().addValue(0, "王五").addValue(1, "男").addValue(2, 28).addValue(3, "激活"));

        // 设置数据有效性（第4列E，添加下拉选项）
        handler.setDataValidationList((short)1, (short)1000, (short)3, (short)3,
            new String[]{"激活", "停用", "未验证"});

        handler.flush();
        System.out.println("低级API导出成功: " + path);
    }

    /**
     * 测试10: 运行时字段映射配置（不适用注解）
     */
    @Test
    public void testWriteWithRuntimeMapping() throws IOException {
        BingExcel bingWithMapping = BingExcelBuilder.toBuilder()
            .addClassNameAlias(Person.class, "人员信息")
            .addFieldConversionMapper(Person.class, "name", 0, "姓名")
            .addFieldConversionMapper(Person.class, "age", 1, "年龄")
            .addFieldConversionMapper(Person.class, "salary", 2, "薪资")
            .build();

        List<Person> list = createPersonList();
        String path = System.getProperty("java.io.tmpdir") + "write_test_runtime_mapping.xlsx";
        bingWithMapping.writeXlsx(path, list);
        System.out.println("运行时映射导出成功: " + path);
    }

    /**
     * 测试11: Float基础转换器写出时应按数值类型输出。
     */
    @Test
    public void testFloatConverterToObject() {
        OutValue outValue = new FloatFieldConverter().toObject(12.5f, null);

        assertEquals(OutValue.OutType.DOUBLE, outValue.getOutType());
        assertEquals(12.5d, (Double) outValue.getValue(), 0.000001d);
    }

    /**
     * 测试12: Boolean基础转换器默认支持常见布尔文本，遇到非法文本应抛转换异常。
     */
    @Test
    public void testBooleanConverterDefaultWords() {
        BooleanFieldConverter converter = new BooleanFieldConverter();

        assertEquals(Boolean.TRUE, converter.fromString("true", null, Boolean.class));
        assertEquals(Boolean.TRUE, converter.fromString("YES", null, Boolean.class));
        assertEquals(Boolean.TRUE, converter.fromString("y", null, Boolean.class));
        assertEquals(Boolean.TRUE, converter.fromString("on", null, Boolean.class));
        assertEquals(Boolean.TRUE, converter.fromString("1", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("false", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("NO", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("n", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("off", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("0", null, Boolean.class));
        try {
            converter.fromString("not-boolean", null, Boolean.class);
            fail("Expected ConversionException");
        } catch (ConversionException expected) {
            // expected
        }
    }

    /**
     * 测试13: 自定义Boolean基础转换器仍只接受配置的文本。
     */
    @Test
    public void testCustomBooleanConverterRejectsInvalidValue() {
        BooleanFieldConverter converter = new BooleanFieldConverter("是", "否", false);

        assertEquals(Boolean.TRUE, converter.fromString("是", null, Boolean.class));
        assertEquals(Boolean.FALSE, converter.fromString("否", null, Boolean.class));
        try {
            converter.fromString("yes", null, Boolean.class);
            fail("Expected ConversionException");
        } catch (ConversionException expected) {
            // expected
        }
    }

    /**
     * 测试14: Long基础转换器应支持单字符数字和十六进制，且不越界访问字符串。
     */
    @Test
    public void testLongConverterDecode() {
        LongFieldConverter converter = new LongFieldConverter();

        assertEquals(Long.valueOf(1L), converter.fromString("1", null, Long.class));
        assertEquals(Long.valueOf(16L), converter.fromString("0x10", null, Long.class));
    }

    /**
     * 测试15: Date基础转换器应使用输入格式解析，且连续解析不会污染格式。
     */
    @Test
    public void testDateConverterUsesInputFormatWithoutPatternPollution() throws Exception {
        DateFieldConverter converter = new DateFieldConverter("yyyy/MM/dd", false);
        SimpleDateFormat assertFormat = new SimpleDateFormat("yyyy/MM/dd");

        assertEquals(assertFormat.parse("2026/07/02"),
            converter.fromString("2026/07/02", null, java.util.Date.class));
        try {
            converter.fromString("26-07-02", null, java.util.Date.class);
            fail("Expected ConversionException");
        } catch (ConversionException expected) {
            // expected
        }
        assertEquals(assertFormat.parse("2026/07/03"),
            converter.fromString("2026/07/03", null, java.util.Date.class));
    }

    /**
     * 测试16: Date基础转换器开启smartConversion后支持历史兼容格式。
     */
    @Test
    public void testDateConverterSmartConversion() throws Exception {
        DateFieldConverter converter = new DateFieldConverter(true);
        SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat fallbackFormat = new SimpleDateFormat("yy-MM-dd");

        assertEquals(defaultFormat.parse("2026-07-02 08:09:10"),
            converter.fromString("2026-07-02 08:09:10", null, java.util.Date.class));
        assertEquals(fallbackFormat.parse("26-07-02"),
            converter.fromString("26-07-02", null, java.util.Date.class));
    }

    /**
     * 测试17: Byte基础转换器应按Java Byte范围校验，不允许无符号溢出。
     */
    @Test
    public void testByteConverterRange() {
        ByteFieldConverter converter = new ByteFieldConverter();

        assertEquals(Byte.valueOf((byte) 127), converter.fromString("127", null, Byte.class));
        assertEquals(Byte.valueOf((byte) -128), converter.fromString("-128", null, Byte.class));
        try {
            converter.fromString("255", null, Byte.class);
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }
    }

    /**
     * 测试18: Integer基础转换器应按Java Integer范围校验，不允许无符号溢出。
     */
    @Test
    public void testIntegerConverterRange() {
        IntegerFieldConverter converter = new IntegerFieldConverter();

        assertEquals(Integer.valueOf(Integer.MAX_VALUE),
            converter.fromString(String.valueOf(Integer.MAX_VALUE), null, Integer.class));
        assertEquals(Integer.valueOf(Integer.MIN_VALUE),
            converter.fromString(String.valueOf(Integer.MIN_VALUE), null, Integer.class));
        try {
            converter.fromString("4294967295", null, Integer.class);
            fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
            // expected
        }
    }

    /**
     * 测试19: Enum基础转换器应支持读写枚举名称。
     */
    @Test
    public void testEnumConverterReadWrite() {
        EnumConVerter converter = new EnumConVerter();
        OutValue outValue = converter.toObject(Status.ENABLED, null);

        assertEquals(OutValue.OutType.STRING, outValue.getOutType());
        assertEquals("ENABLED", outValue.getValue());
        assertEquals(Status.ENABLED, converter.fromString("ENABLED", null, Status.class));
        assertEquals(Status.DISABLED, converter.fromString(" disabled ", null, Status.class));
        try {
            converter.fromString("unknown", null, Status.class);
            fail("Expected ConversionException");
        } catch (ConversionException expected) {
            // expected
        }
    }

    // ==================== 辅助方法 ====================

    private List<Person> createPersonList() {
        return Arrays.asList(
            new Person("张三", 25, 8000.0),
            new Person("李四", 30, 12000.0),
            new Person("王五", 28, 10000.0)
        );
    }

    // ==================== 测试用的实体类 ====================

    private enum Status {
        ENABLED, DISABLED
    }

    @OutAlias("人员信息")
    public static class Person {
        @CellConfig(index = 0)
        private String name;

        @CellConfig(index = 1, aliasName = "年龄")
        private int age;

        @CellConfig(index = 2, aliasName = "薪资")
        private Double salary;

        public Person() {}

        public Person(String name, int age, Double salary) {
            this.name = name;
            this.age = age;
            this.salary = salary;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }

        @Override
        public String toString() {
            return ToStringHelper.of(this)
                .add("name", name)
                .add("age", age)
                .add("salary", salary)
                .toString();
        }
    }

    @OutAlias("部门信息")
    public static class Department {
        @CellConfig(index = 0)
        private String deptName;

        @CellConfig(index = 1, aliasName = "人数")
        private int employeeCount;

        public Department() {}

        public Department(String deptName, int employeeCount) {
            this.deptName = deptName;
            this.employeeCount = employeeCount;
        }

        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public int getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(int employeeCount) { this.employeeCount = employeeCount; }

        @Override
        public String toString() {
            return ToStringHelper.of(this)
                .add("deptName", deptName)
                .add("employeeCount", employeeCount)
                .toString();
        }
    }

    @OutAlias("用户信息")
    public static class User {
        @CellConfig(index = 0)
        private String name;

        @CellConfig(index = 1)
        @BingConvertor(value = BooleanFieldConverter.class, strings = {"是", "否"}, booleans = {true})
        private boolean active;

        public User() {}

        public User(String name, boolean active) {
            this.name = name;
            this.active = active;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        @Override
        public String toString() {
            return ToStringHelper.of(this)
                .add("name", name)
                .add("active", active)
                .toString();
        }
    }
}
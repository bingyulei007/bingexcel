# BingExcel 使用教程

## 简介

BingExcel 是一个 Java 库，用于将 Excel 文件（xls/xlsx）与 Java 实体类相互转换，支持注解配置、自定义转换器、CSV 导出等功能。

**最新版本：4.0**

**Maven 依赖：**

```xml
<dependency>
    <groupId>cn.com.binging</groupId>
    <artifactId>excel</artifactId>
    <version>4.0</version>
</dependency>
```

---

## 一、快速开始

### 1.1 读取 Excel 到实体类

定义一个实体类，使用 `@CellConfig` 注解标记字段对应的 Excel 列索引：

```java
public class Person {
    @CellConfig(index = 0)
    private String name;
    @CellConfig(index = 1)
    private int age;
    @CellConfig(index = 3)
    private Double salary;

    // getter/setter 省略
}
```

读取 Excel 文件：

```java
BingExcel bing = BingExcelBuilder.builderInstance();
File file = new File("person.xlsx");
SheetVo<Person> vo = bing.readFile(file, Person.class, 1); // 1 = 从第2行开始读取
System.out.println(vo.getSheetName());    // Sheet1
System.out.println(vo.getObjectList());  // List<Person>
```

从输入流读取（xls 格式）：

```java
InputStream in = Person.class.getResourceAsStream("/person.xls");
SheetVo<Person> vo = bing.readStream(in, Person.class, 1);
```

### 1.2 导出实体类到 Excel

```java
List<Person> list = new ArrayList<>();
list.add(new Person(12, "张三", 5000.0));
list.add(new Person(23, "李四", 8000.0));

bing.writeExcel("/path/to/output.xlsx", list);
```

导出为老格式 xls：

```java
bing.writeOldExcel("/path/to/output.xls", list);
```

---

## 二、核心注解

### 2.1 @CellConfig

用于实体类字段，标记该字段在 Excel 中的列位置及读取配置。

| 属性 | 类型 | 说明 |
|------|------|------|
| `index` | int | Excel 列索引（从 0 开始），**读取时必须指定** |
| `aliasName` | String | Excel 表头别名，用于导出时自定义列名 |
| `readRequired` | boolean | 读取时该字段是否为必填，默认 false |

```java
@CellConfig(index = 0)
private String name;

@CellConfig(index = 1, aliasName = "年龄")
private int age;

@CellConfig(index = 2, readRequired = true, aliasName = "薪水")
private Double salary;
```

### 2.2 @OutAlias

标注在类上，用于指定导出 Excel 时的 Sheet 名称。

```java
@OutAlias("销售数据")
public class SaleRecord {
    @CellConfig(index = 0)
    private String product;
    // ...
}
```

### 2.3 @BingConvertor

用于字段上，指定自定义转换器。

```java
@BingConvertor(value = MyConverter.class, strings = { "是", "否" }, booleans = { true })
private boolean valid;
```

---

## 三、读取 Excel

### 3.1 基础读取

```java
// 读取第一个 Sheet，从第2行开始
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);

// 读取指定 Sheet
ReaderCondition<Person> condition = new ReaderCondition<>(0, Person.class); // sheetIndex=0
condition.setStartRow(2); // 从第3行开始
SheetVo<Person> vo = bing.readFile(file, condition);
```

### 3.2 条件读取（ReaderCondition）

```java
// 指定 Sheet 索引、起始行、结束行
ReaderCondition<Person> condition = new ReaderCondition<>(0, Person.class);
condition.setStartRow(2);   // 从第3行开始（0-based，排除表头）
condition.setEndRow(100);    // 读到第100行

SheetVo<Person> vo = bing.readFile(file, condition);
```

### 3.3 多 Sheet 读取

```java
ReaderCondition[] conditions = new ReaderCondition[] {
    new ReaderCondition<>(0, Person.class).setStartRow(1),
    new ReaderCondition<>(1, Student.class).setStartRow(1)
};
List<SheetVo> voList = bing.readFileToList(file, conditions);
```

### 3.4 SAX 模式大文件读取（节省内存）

适合读取超大 Excel 文件，基于 SAX 事件解析，不占用大量内存：

```java
@Test
public void testSAX() throws Exception {
    File f = new File("test.xlsx");
    ReadHandler saxHandler = ExcelReaderFactory.create(f, new ExcelReadListener() {
        @Override
        public void startSheet(int sheetIndex, String name) {
            System.out.println("开始读取 Sheet: " + name);
        }

        @Override
        public void optRow(int curRow, ListRow rowList) {
            // rowList 是当前行的数据列表，全部为 String 类型
            System.out.println("第 " + curRow + " 行: " + rowList);
        }

        @Override
        public void endWorkBook() {
            System.out.println("读取完成");
        }

        @Override
        public void endSheet(int sheetIndex, String name) {
        }
    }, true);
    saxHandler.readSheets();
}
```

---

## 四、导出 Excel

### 4.1 基础导出

```java
List<Person> list = Lists.newArrayList();
list.add(new Person(12, "nihoa", 23434.9));
list.add(new Person(23, "nihoa", 234.9));

bing.writeExcel("/path/to/output.xlsx", list);
```

### 4.2 多 Sheet 导出

```java
SheetExcel sheet1 = new SheetExcel();
sheet1.setSheetName("员工");
sheet1.setList(personList);

SheetExcel sheet2 = new SheetExcel();
sheet2.setSheetName("部门");
sheet2.setList(deptList);

bing.writeSheetsExcel("/path/to/output.xlsx", sheet1, sheet2);
```

### 4.3 导出到 OutputStream

```java
OutputStream os = new FileOutputStream("output.xlsx");
bing.writeExcel(os, list);
```

---

## 五、CSV 导出

### 5.1 基础导出

```java
bing.writeCSV("/path/to/output.csv", list);
```

### 5.2 自定义分隔符和 BOM

```java
bing.writeCSV(os, list, ',', true, true);
// 参数：输出流, 数据, 分隔符, 是否带表头, 是否带BOM
```

---

## 六、自定义转换器

### 6.1 全局转换器（Builder 方式）

实现 `FieldValueConverter` 接口，并通过 `BingExcelBuilder` 注册：

```java
// 1. 定义转换器
public class MyDateConverter extends AbstractFieldConvertor {
    @Override
    public boolean canConvert(Class<?> clz) {
        return EmploryAttribute.class.equals(clz);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type type) {
        if (StringUtils.isBlank(cell)) {
            return null;
        }
        String[] split = cell.split(":");
        EmploryAttribute attr = new EmploryAttribute();
        attr.key = split[0];
        attr.value = split[1];
        return attr;
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
        // 导出时的转换逻辑
        return new OutValue(source.toString());
    }
}

// 2. 注册并使用
BingExcel bing = BingExcelBuilder.toBuilder()
    .registerFieldConverter(EmploryAttribute.class, new MyDateConverter())
    .builder();

SheetVo<Salary> vo = bing.readFile(file, condition);
```

### 6.2 内置转换器

框架内置了以下类型转换器，直接可用：

- `BooleanFieldConverter` — 布尔类型
- `ByteFieldConverter` — 字节类型
- `CharacterFieldConverter` — 字符类型
- `DateFieldConverter` — 日期类型
- `DoubleFieldConverter` — 双精度浮点
- `FloatFieldConverter` — 单精度浮点
- `IntegerFieldConverter` — 整型
- `LongFieldConverter` — 长整型
- `ShortFieldConverter` — 短整型
- `StringFieldConverter` — 字符串
- `EnumConVerter` — 枚举类型
- `ArrayConverter` — 数组类型
- `CollectionConverter` — 集合类型

### 6.3 枚举转换示例

字段声明枚举类型，框架会自动处理：

```java
@CellConfig(index = 0)
private Status status;

public enum Status {
    ACTIVE, INACTIVE
}
```

---

## 七、低级写入（WriteHandler）

如果需要更细粒度地控制单元格写入，可以使用 `WriteHandler`：

```java
@Test
public void testWriteHandler() {
    WriteHandler handler = ExcelWriterFactory.createXSSF("/path/to/output.xlsx");
    handler.createSheet("数据");

    // 写入表头
    ListLine header = new ListLine()
        .addValue(0, "姓名")
        .addValue(1, "性别");
    handler.writeHeader(header);

    // 写入数据行
    handler.writeLine(new ListLine().addValue(0, "张三").addValue(1, "男"));
    handler.writeLine(new ListLine().addValue(0, "李四").addValue(1, "女"));

    // 数据有效性（下拉框）
    handler.setDataValidationList((short)1, (short)1000, (short)1, (short)1,
        new String[]{"男", "女", "其他"});

    handler.flush();
}
```

---

## 八、字段映射别名（运行时覆盖注解）

如果不想用注解指定列名，也可以在运行时通过 Builder 指定：

```java
BingExcel bing = BingExcelBuilder.toBuilder()
    .addFieldConversionMapper(Person.class, "name", 0, "姓名")
    .addFieldConversionMapper(Person.class, "age", 1, "年龄")
    .builder();
```

---

## 九、支持的类型

| 类型 | 备注 |
|------|------|
| 基本类型 | int, long, double, float, boolean, byte, char, short |
| 包装类型 | Integer, Long, Double 等 |
| String | — |
| Date | 支持多种日期格式 |
| 枚举 | 自动转换 |
| 数组 | 如 String[] |
| 集合 | List, Set 等 |
| 自定义类型 | 需注册转换器 |

---

## 十、完整示例

### 10.1 读取并导出

```java
public class ExcelDemo {
    public static void main(String[] args) throws Exception {
        BingExcel bing = BingExcelBuilder.builderInstance();

        // 读取
        File input = new File("person.xlsx");
        SheetVo<Person> vo = bing.readFile(input, Person.class, 1);
        List<Person> persons = vo.getObjectList();

        // 处理数据...

        // 导出
        File output = new File("output.xlsx");
        bing.writeExcel(output, persons);
    }
}
```

---

## 更新日志

### v4.0
- 升级依赖版本至最新稳定版
- 启用 Maven Central 发布配置（Sonatype Nexus Staging）
- 支持快照版本发布

### v3.0
- 依赖版本升级
- POI 版本升级至 4.1.2
- commons-lang3 升级至 3.14.0
- commons-csv 升级至 1.10.0
- junit 升级至 4.13.2

### v2.4
- 修复 SAX 模式读取稳定性问题

### v2.3
- 新增多 Sheet 导出功能

### v2.2
- 新增 CSV 导出支持
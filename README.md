# bingExcel

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

处理 Excel 与 Java Model 之间双向转换的轻量级框架，基于 Apache POI 构建。

BingExcel 是一个轻量级 Java 库，用于实现 Excel 文件（xls/xlsx）与 Java 实体类之间的双向转换。框架基于 Apache POI 构建，提供注解驱动的配置方式，支持自定义转换器、大文件流式读取、多 Sheet 操作等功能。

## 核心特性

- 支持 `.xls` 和 `.xlsx` 格式
- 注解驱动的列映射配置
- 灵活的类型转换器机制
- 多 Sheet 读写支持
- CSV 导出支持
- 运行时字段映射配置

## 快速开始

```java
BingExcel bing = BingExcelBuilder.builderInstance();

// 读取 Excel
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);

// 导出 Excel
bing.writeExcel("/path/to/output.xlsx", list);
```

## 环境依赖

| 组件 | 版本 |
|------|------|
| Java | 1.8+ |
| Apache POI | 4.1.2 |
| commons-lang3 | 3.14.0 |
| commons-csv | 1.10.0 |

---

## 目录

- [一、快速开始](#一快速开始)
- [二、核心概念与架构](#二核心概念与架构)
- [三、读取 Excel](#三读取-excel)
- [四、导出 Excel](#四导出-excel)
- [五、CSV 导出](#五csv-导出)
- [六、自定义转换器](#六自定义转换器)
- [七、低级 API（流式读写）](#七低级-api流式读写)
- [八、运行时配置](#八运行时配置)
- [九、映射与转换器优先级](#九映射与转换器优先级)
- [十、扩展开发指南](#十扩展开发指南)
- [十一、常见问题](#十一常见问题)
- [十二、更新日志](#十二更新日志)

---

## 一、快速开始

### 1.1 环境要求

- JDK 1.8 或更高版本
- Maven 3.x

### 1.2 定义实体类

使用 `@CellConfig` 注解标记字段与 Excel 列的映射关系：

```java
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;

@OutAlias("人员信息")
public class Person {
    @CellConfig(index = 0)
    private String name;

    @CellConfig(index = 1)
    private int age;

    @CellConfig(index = 2)
    private Double salary;

    // getter/setter 省略
}
```

### 1.3 读取 Excel

```java
import com.bing.excel.BingExcel;
import com.bing.excel.BingExcelBuilder;
import com.bing.excel.core.SheetVo;
import java.io.File;

BingExcel bing = BingExcelBuilder.builderInstance();
File file = new File("person.xlsx");

// 读取第一个 Sheet，从第 2 行开始（索引为 1，跳过表头）
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);

System.out.println("Sheet名称: " + vo.getSheetName());
System.out.println("数据列表: " + vo.getObjectList());
```

### 1.4 导出 Excel

```java
import com.bing.excel.BingExcel;
import com.bing.excel.BingExcelBuilder;
import java.util.ArrayList;
import java.util.List;

BingExcel bing = BingExcelBuilder.builderInstance();
List<Person> list = new ArrayList<>();
list.add(new Person("张三", 12, 5000.0));
list.add(new Person("李四", 23, 8000.0));

bing.writeExcel("/path/to/output.xlsx", list);
```

---

## 二、核心概念与架构

### 2.1 包结构

```
com.bing.excel
├── annotation          # 注解定义
│   ├── CellConfig      # 字段映射注解
│   ├── OutAlias        # 导出别名注解
│   └── BingConvertor   # 转换器注解
├── core                # 核心接口与类
│   ├── BingExcel       # 主入口类
│   ├── BingExcelBuilder# 构建器
│   ├── SheetVo         # Sheet 数据封装
│   ├── ReaderCondition # 读取条件配置
│   └── SheetExcel      # 多 Sheet 导出封装
├── converter           # 类型转换器
│   ├── FieldValueConverter    # 转换器接口
│   ├── AbstractFieldConvertor # 抽象基类
│   └── converter.*             # 内置转换器实现
├── reader              # 读取相关
│   └── usermodel                 # 用户模式
└── writer              # 写入相关
    ├── ExcelWriterFactory       # 写入工厂
    └── handler.*                 # 写入处理器
```

### 2.2 核心类说明

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `BingExcel` | com.bing.excel.core | 主入口类，提供读写 Excel 的核心方法 |
| `BingExcelBuilder` | com.bing.excel.core | 构建器，用于注册转换器和配置映射关系 |
| `SheetVo<T>` | com.bing.excel.core | Sheet 数据封装类，包含 Sheet 名称和数据列表 |
| `ReaderCondition<T>` | com.bing.excel.core | 读取条件配置类，可指定 Sheet 索引、起始行、结束行 |
| `SheetExcel` | com.bing.excel.core | 多 Sheet 导出时的数据封装类 |

### 2.3 核心接口

| 接口 | 包路径 | 说明 | 扩展点 |
|------|--------|------|--------|
| `FieldValueConverter` | com.bing.excel.converter | 类型转换器接口，用于字符串与目标类型的双向转换 | 自定义类型转换 |
| `WriteHandler` | com.bing.excel.writer.handler | 低级写入处理器 | 精细控制单元格写入 |
| `ReadHandler` | com.bing.excel.reader.handler | 低级读取处理器 | 精细控制单元格读取 |

### 2.4 数据流转图

```
┌─────────────────────────────────────────────────────────────────┐
│                           读取流程                               │
├─────────────────────────────────────────────────────────────────┤
│  Excel File ──► POI 解析 ──► CellValue ──► FieldValueConverter │
│                                                              ──► 实体对象
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                           写入流程                               │
├─────────────────────────────────────────────────────────────────┤
│  实体对象 ──► FieldValueConverter ──► CellValue ──► POI 写入    │
│                                                              ──► Excel File
└─────────────────────────────────────────────────────────────────┘
```

---

## 三、读取 Excel

### 3.1 核心注解

#### @CellConfig

用于实体类字段，标记该字段在 Excel 中的列位置及读取配置。

| 属性 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `index` | int | 否 | -1 | Excel 列索引（从 0 开始）。读取时若为 -1 且 `aliasName` 非空，则按表头匹配列；写入时必须 >= 0 |
| `aliasName` | String | 否 | 字段名 | 读取时按表头匹配的标题；写入时作为列名 |
| `readRequired` | boolean | 否 | false | 读取时是否必填 |

```java
import com.bing.excel.annotation.CellConfig;

public class User {
    @CellConfig(index = 0)
    private String name;

    @CellConfig(index = 1, aliasName = "年龄")
    private int age;

    @CellConfig(index = 2, readRequired = true, aliasName = "薪水")
    private Double salary;
}
```

#### 通过 aliasName 匹配列（读取）

当 Excel 列顺序不稳定、或中间插入列时，可以只声明 `aliasName` 而不指定 `index`，
库会按 Excel 表头文本自动定位列。

```java
public class User {
    @CellConfig(aliasName = "姓名")
    private String name;

    @CellConfig(aliasName = "年龄")
    private int age;

    @CellConfig(aliasName = "薪水")
    private Double salary;
}
```

**注意事项**：

- 仅**读取**方向支持 `aliasName` 匹配；**写入**时 `index` 必须 >= 0（否则抛 `IllegalCellConfigException`）。
- 表头比较为严格相等匹配，不做 trim 或大小写无关。Excel 表头包含前后空格时匹配失败。
- 同时设置 `index` 和 `aliasName` 时，`index` 优先（不会查表头）。
- 通过 `addFieldConversionMapper(clazz, field, index, ...)` 给出显式 `index >= 0` 时，同样会覆盖 aliasName 匹配（详见 [§九 映射与转换器优先级](#九映射与转换器优先级)）。
- `startRow` 必须 >= 1（默认值），保证存在标题行可读取；若所有 aliasName 字段都已被显式 index 覆盖，则 `startRow=0` 也可正常读取。
- 表头匹配失败时抛出 `IllegalCellConfigException`，异常消息中包含 `available titles=[...]` 便于排查。

#### @OutAlias

标注在类上，指定导出 Excel 时的 Sheet 名称。

```java
import com.bing.excel.annotation.OutAlias;

@OutAlias("销售数据")
public class SaleRecord {
    @CellConfig(index = 0)
    private String product;
}
```

#### @BingConvertor

用于字段上，指定自定义转换器。

```java
import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.converter.custom.YesNoConverter;

@BingConvertor(value = YesNoConverter.class, strings = { "是", "否" }, booleans = { true })
private boolean valid;
```

### 3.2 基础读取

```java
// 方式一：读取指定 Sheet，从指定行开始
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);

// 方式二：使用 ReaderCondition 精确控制
ReaderCondition<Person> condition = new ReaderCondition<>(0, Person.class);
condition.setStartRow(2);  // 从第 3 行开始（行索引从 0 开始）
condition.setEndRow(100);   // 读到第 100 行结束
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

### 3.4 从 InputStream 读取

```java
import java.io.InputStream;
import com.bing.excel.core.SheetReader;

// 读取 xlsx 格式
InputStream in = new FileInputStream("person.xlsx");
SheetVo<Person> vo = bing.readStream(in, Person.class, 1);

// 读取 xls 格式（老格式）
InputStream in = getClass().getResourceAsStream("/person.xls");
SheetReader<Person> reader = bing.readOldStream(in, Person.class, 1);
```

### 3.5 异常处理

读取操作可能抛出以下异常：

| 异常类型 | 说明 |
|----------|------|
| `ExcelException` | 通用 Excel 处理异常 |
| `FileNotFoundException` | 文件不存在 |
| `IOException` | IO 读写错误 |

```java
try {
    SheetVo<Person> vo = bing.readFile(file, Person.class, 1);
} catch (ExcelException e) {
    // 处理 Excel 解析错误
    e.printStackTrace();
} catch (FileNotFoundException e) {
    // 处理文件不存在
    e.printStackTrace();
}
```

---

## 四、导出 Excel

### 4.1 基础导出

```java
import java.util.ArrayList;
import java.util.List;

List<Person> list = new ArrayList<>();
list.add(new Person("张三", 12, 5000.0));
list.add(new Person("李四", 23, 8000.0));

// 导出为 xlsx 格式
bing.writeExcel("/path/to/output.xlsx", list);

// 导出为 xls 格式（老格式）
bing.writeOldExcel("/path/to/output.xls", list);
```

**导出API完整列表：**

| 方法 | 说明 |
|------|------|
| `writeExcel(String path, Iterable... iterables)` | 导出xlsx格式，支持多个Iterable（每个Iterable一个Sheet） |
| `writeOldExcel(String path, Iterable... iterables)` | 导出xls老格式 |
| `writeExcel(OutputStream stream, Iterable... iterables)` | 导出到输出流（xlsx） |
| `writeOldExcel(OutputStream stream, Iterable... iterables)` | 导出到输出流（xls） |
| `writeSheetsExcel(String path, SheetExcel... sheetExcels)` | 多Sheet导出，支持不同类型的对象列表 |

**多数据集直接导出（无需SheetExcel封装）：**

```java
// 第一个Iterable导出到第一个Sheet，第二个Iterable导出到第二个Sheet
List<Person> personList = Arrays.asList(new Person("张三", 25), new Person("李四", 30));
List<Department> deptList = Arrays.asList(new Department("研发部", 50));

bing.writeExcel("/path/to/output.xlsx", personList, deptList);
```

### 4.2 导出到 OutputStream

```java
import java.io.OutputStream;
import java.io.FileOutputStream;

OutputStream os = new FileOutputStream("output.xlsx");
bing.writeExcel(os, list);
os.close();

// 或者使用try-with-resources自动关闭
try (OutputStream os = new FileOutputStream("output.xlsx")) {
    bing.writeExcel(os, list);
}
```

### 4.3 多 Sheet 导出

```java
SheetExcel sheet1 = new SheetExcel();
sheet1.setSheetName("员工");
sheet1.setList(personList);

SheetExcel sheet2 = new SheetExcel();
sheet2.setSheetName("部门");
sheet2.setList(deptList);

bing.writeSheetsExcel("/path/to/output.xlsx", sheet1, sheet2);
```

### 4.4 导出配置

| 配置项 | 说明 |
|--------|------|
| `@OutAlias` | 指定 Sheet 名称 |
| `@CellConfig(aliasName)` | 指定列名称 |
| `@CellConfig(index)` | 指定列索引（从0开始） |
| `@BingConvertor` | 指定自定义转换器 |

### 4.5 使用 @BingConvertor 自定义导出转换

通过 `@BingConvertor` 注解可以在字段级别指定自定义转换器，将Java类型转换为Excel单元格值：

```java
import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.converter.base.BooleanFieldConverter;

public class User {
    @CellConfig(index = 0)
    private String name;

    // 布尔值导出时转换为"是"/"否"，读取时反向转换
    @CellConfig(index = 1)
    @BingConvertor(value = BooleanFieldConverter.class, strings = {"是", "否"}, booleans = {true})
    private boolean active;
}
```

### 4.6 空列表和null值处理

- **空列表**：导出仅有表头的Excel文件
- **null值**：对于引用类型字段，null值会输出为空单元格
- **基本类型**：建议使用包装类型（Integer、Double等）以便正确处理null值

---

## 五、CSV 导出

### 5.1 基础导出

```java
bing.writeCSV("/path/to/output.csv", list);
```

### 5.2 自定义配置

```java
import java.io.OutputStream;
import java.io.FileOutputStream;

OutputStream os = new FileOutputStream("output.csv");
bing.writeCSV(os, list, ',', true, true);
// 参数说明：
//   os      - 输出流
//   list    - 数据列表
//   ','     - 分隔符（默认逗号）
//   true    - 是否带表头
//   true    - 是否带 BOM（解决 Excel 打开中文乱码）
```

---

## 六、自定义转换器

### 6.1 转换器接口

```java
import com.bing.excel.converter.ConverterMatcher;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import java.lang.reflect.Type;

// canConvert 继承自父接口 ConverterMatcher
public interface FieldValueConverter extends ConverterMatcher {
    /**
     * 判断是否支持该类型的转换（来自 ConverterMatcher）
     */
    boolean canConvert(Class<?> clz);

    /**
     * 将字符串转换为目标类型（读取时调用）
     */
    Object fromString(String cell, ConverterHandler converterHandler, Type targetType);

    /**
     * 将目标类型转换为 OutValue（导出时调用）
     */
    OutValue toObject(Object source, ConverterHandler converterHandler);
}
```

### 6.2 实现自定义转换器

```java
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.converter.ConverterHandler;
import com.bing.excel.converter.OutValue;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

public class CustomDateConverter extends AbstractFieldConvertor {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean canConvert(Class<?> clz) {
        return LocalDate.class.equals(clz);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type type) {
        if (StringUtils.isBlank(cell)) {
            return null;
        }
        return LocalDate.parse(cell, FORMATTER);
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
        if (source == null) {
            return new OutValue("");
        }
        LocalDate date = (LocalDate) source;
        return new OutValue(date.format(FORMATTER));
    }
}
```

### 6.3 注册并使用转换器

```java
BingExcel bing = BingExcelBuilder.toBuilder()
    .registerFieldConverter(LocalDate.class, new CustomDateConverter())
    .build();

SheetVo<Person> vo = bing.readFile(file, Person.class, 1);
```

### 6.4 内置转换器

框架已内置以下类型转换器，无需额外注册：

| 转换器 | 支持类型 |
|--------|----------|
| `BooleanFieldConverter` | boolean, Boolean |
| `ByteFieldConverter` | byte, Byte |
| `CharacterFieldConverter` | char, Character |
| `DateFieldConverter` | Date, java.sql.Date |
| `DoubleFieldConverter` | double, Double |
| `FloatFieldConverter` | float, Float |
| `IntegerFieldConverter` | int, Integer |
| `LongFieldConverter` | long, Long |
| `ShortFieldConverter` | short, Short |
| `StringFieldConverter` | String |
| `EnumConVerter` | 所有枚举类型 |
| `ArrayConverter` | 数组类型（如 String[]） |
| `CollectionConverter` | 集合类型（List, Set 等） |

### 6.5 枚举转换示例

字段声明枚举类型，框架会自动根据枚举名称进行转换：

```java
public class User {
    @CellConfig(index = 0)
    private Status status;
}

public enum Status {
    ACTIVE, INACTIVE
}

// Excel 中 "ACTIVE" → Status.ACTIVE
// Status.ACTIVE → Excel 中 "ACTIVE"
```

---

## 七、低级 API（流式读写）

### 7.1 WriteHandler - 精细控制写入

适用于需要完全控制单元格写入的场景，如设置数据有效性、合并单元格等。

**WriteHandler API 列表：**

| 方法 | 说明 |
|------|------|
| `createSheet(String name)` | 创建指定名称的Sheet |
| `writeHeader(ListLine)` | 写入表头行 |
| `writeLine(ListLine)` | 写入数据行 |
| `setDataValidationList(short, short, short, short, String[])` | 设置下拉数据有效性 |
| `setDataValidationList(int, int, int, int, String[])` | 设置下拉数据有效性（int版） |
| `flush()` | 刷新并关闭流 |
| `close()` | 关闭处理器 |

**示例代码：**

```java
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.handler.WriteHandler;
import com.bing.excel.vo.ListLine;

WriteHandler handler = ExcelWriterFactory.createXSSF("/path/to/output.xlsx");
handler.createSheet("数据");

// 写入表头
ListLine header = new ListLine()
    .addValue(0, "姓名")
    .addValue(1, "性别")
    .addValue(2, "年龄");
handler.writeHeader(header);

// 写入数据行
handler.writeLine(new ListLine().addValue(0, "张三").addValue(1, "男").addValue(2, 25));
handler.writeLine(new ListLine().addValue(0, "李四").addValue(1, "女").addValue(2, 30));

// 设置数据有效性（下拉框）
// 参数：起始行索引, 结束行索引, 起始列索引, 结束列索引, 选项数组
handler.setDataValidationList((short)1, (short)1000, (short)1, (short)1,
    new String[]{"男", "女", "其他"});

handler.flush();
```

**创建不同格式的处理器：**

```java
// 创建xlsx格式处理器（2007+格式）
WriteHandler xssfHandler = ExcelWriterFactory.createXSSF("/path/to/output.xlsx");

// 创建xls格式处理器（老格式）
WriteHandler hssfHandler = ExcelWriterFactory.createHSSF("/path/to/output.xls");
```

### 7.2 ReadHandler - 精细控制读取

使用 `ReadHandler` 配合 `ExcelReadListener` 进行精细化读取控制。

---

## 八、运行时配置

### 8.1 运行时字段映射

如果不希望使用注解，可以在运行时通过 Builder 指定字段映射关系：

```java
BingExcel bing = BingExcelBuilder.toBuilder()
    .addFieldConversionMapper(Person.class, "name", 0, "姓名")
    .addFieldConversionMapper(Person.class, "age", 1, "年龄")
    .addFieldConversionMapper(Person.class, "salary", 2, "薪水")
    .build();
```

**方法参数说明：**
- 第一个参数：实体类 Class
- 第二个参数：字段名
- 第三个参数：Excel 列索引
- 第四个参数：导出时的列名（可选）

### 8.2 全局 Builder 模式

```java
// 创建可复用的 BingExcel 实例
BingExcel bing = BingExcelBuilder.toBuilder()
    .registerFieldConverter(LocalDate.class, new CustomDateConverter())
    .addFieldConversionMapper(Person.class, "name", 0, "姓名")
    .build();

// 后续可直接使用
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);
```

---

## 九、映射与转换器优先级

BingExcel 提供四种配置方式：`@CellConfig`、`@BingConvertor`（注解），以及
`addFieldConversionMapper`、`registerFieldConverter`（Builder 运行时注册）。当它们
同时存在时，按以下三条相互独立的轴决定最终生效结果。

### 9.1 字段映射来源：用户注册 > 注解（整字段替换）

同一个字段的映射（列位置、别名、转换器）来源按优先级为：

1. **`addFieldConversionMapper(...)`** —— Builder 运行时注册（用户自定义）
2. **`@CellConfig` / `@BingConvertor`** —— 注解

二者都存在时取**用户注册**，且这是**整字段替换**而非按属性合并：一旦对某字段调用了
`addFieldConversionMapper`，该字段的注解映射会被**整体屏蔽**——你传入的 index / alias /
converter 直接生效，未显式提供的属性**不会**回退到注解值（例如该字段上的 `@BingConvertor`
会被一并丢弃）。因此用 Builder 注册某字段时，请在一次调用中给全它的完整映射。

> **使用约定：同一字段（建议同一个类）只用一种方式 —— 注解或 Builder，不要混用。**
> 能改实体类源码时优先用注解；不能改源码、或列序随场景变化、或希望把映射配置外置时，
> 用 `addFieldConversionMapper` 并让整个类都走 Builder。

### 9.2 列定位：显式 index > aliasName 表头匹配

单个字段内部，列位置的确定规则：

- `@CellConfig(index = N)` 且 `N >= 0` —— 直接按列索引 N 读取，**不查表头**；
- 仅 `@CellConfig(aliasName = "...")`（index 为默认 -1）—— 读取时按表头文本匹配列。

注意 9.1 与 9.2 的交叉：若通过 `addFieldConversionMapper(clazz, field, index, ...)` 给了
显式 `index >= 0`，它会覆盖注解的 aliasName 匹配（用户注册整体替换了注解映射）。

### 9.3 转换器：字段级 > 按类型注册 > 内置默认

转换器的选择分三档，由高到低：

1. **字段级转换器** —— `@BingConvertor`，或 `addFieldConversionMapper(..., converter)`
   传入的转换器。两者都会让该字段“自带”转换器，优先级最高。
2. **按类型注册** —— `registerFieldConverter(type, converter)`，按**字段类型**索引，
   仅用于那些没有字段级转换器的字段。
3. **内置全局默认** —— 框架自带的按类型默认转换器（见 [§六 内置转换器](#六自定义转换器)）。

由此可推出一个易错点：若对某字段调用了不带 converter 的 `addFieldConversionMapper`
重载（converter 为 null），该字段的注解 `@BingConvertor` 会因整字段替换而失效，转换器
退回到第 2 / 3 档（按类型注册或内置默认），而非沿用注解里的转换器。这也是 9.1 “二选一”
约定的又一理由。

### 9.4 小结

| 轴 | 高 → 低 |
|----|---------|
| 字段映射来源 | `addFieldConversionMapper` ＞ 注解（整字段替换） |
| 列定位 | 显式 `index` ＞ `aliasName` 表头匹配 |
| 转换器 | 字段级（`@BingConvertor` / Builder 传入）＞ `registerFieldConverter`（按类型）＞ 内置默认 |

> 注：流式/事件读取（`BingExcelEvent`，已废弃）仅使用注解 mapper，不支持 Builder 注册的
> 用户自定义映射；如需用户自定义映射请使用 `BingExcelImpl`（即 `BingExcelBuilder` 构建的实例）。

---

## 十、扩展开发指南

本节介绍如何基于 BingExcel 进行二次开发。

### 9.1 添加新的类型转换器

1. 继承 `AbstractFieldConvertor` 抽象类
2. 实现 `canConvert`、`fromString`、`toObject` 方法
3. 通过 `BingExcelBuilder.registerFieldConverter()` 注册

### 9.2 集成 Spring 框架

```java
@Configuration
public class ExcelConfig {

    @Bean
    public BingExcel bingExcel() {
        return BingExcelBuilder.toBuilder()
            .registerFieldConverter(LocalDate.class, new CustomDateConverter())
            // 添加其他全局配置
            .build();
    }
}
```

### 9.3 注意事项

| 项目 | 说明 |
|------|------|
| 线程安全 | `BingExcel` 实例创建后不建议在多线程间共享 |
| 内存管理 | 处理大文件时注意内存使用 |
| 类型转换 | 自定义转换器需考虑 null 值和空字符串的处理 |

---

## 十一、常见问题

### Q1: 读取时如何跳过表头？

在 `readFile` 方法中指定起始行索引：

```java
// 从第 2 行开始读取（跳过第 1 行表头）
SheetVo<Person> vo = bing.readFile(file, Person.class, 1);
```

### Q2: 如何处理空值？

字段声明为包装类型或使用 `@CellConfig(readRequired = false)`：

```java
@CellConfig(index = 2, readRequired = false)
private Double salary;  // 可为 null
```

### Q3: Excel 打开 CSV 中文乱码？

导出时设置 BOM 参数：

```java
bing.writeCSV(os, list, ',', true, true);  // 最后一个参数 true 表示添加 BOM
```

### Q4: 如何自定义日期格式？

实现自定义转换器（见 [六、自定义转换器](#六自定义转换器)）。

### Q5: 多个 Sheet 如何读取？

使用 `readFileToList` 方法（见 [3.3 多 Sheet 读取](#33-多-sheet-读取)）。

---

## 十二、更新日志

### v4.1
- 支持 `@CellConfig(aliasName=...)` 通过表头匹配列（仅读取方向，写入仍需显式 index）
- aliasName 表头解析改为遵循与读取一致的 mapper 优先级（用户自定义 mapper 优先于注解）；
  当用户通过 `addFieldConversionMapper` 提供显式 index 时，覆盖 aliasName 匹配，
  且 `startRow=0` 时不再误判为“无表头可解析”而报错
- 写出方向（Excel 数据/表头、CSV）遇到字段 index < 0 时统一抛 `IllegalCellConfigException`，
  不再把负 index 继续向下传递
- 新增“映射与转换器优先级”章节，说明注解与 Builder 注册并存时的生效规则

### v4.0
- 升级依赖版本至最新稳定版
- 启用 Maven Central 发布配置
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
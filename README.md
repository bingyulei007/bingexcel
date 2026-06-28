# BingExcel

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

BingExcel 是一个基于 Apache POI 的轻量级 Java Excel 工具库，用注解或 Builder 配置完成 Excel（`.xls` / `.xlsx` / CSV）与 Java 实体对象之间的读写转换。

它适合以下场景：

- 将 Excel 数据读取为 Java Bean 列表；
- 将 Java Bean 列表导出为 Excel 或 CSV；
- 通过表头名称读取列顺序不固定的 Excel；
- 为字段定制类型转换逻辑；
- 读写多个 Sheet，或使用底层 Handler 做更细粒度控制。

## 目录

- [特性](#特性)
- [环境要求](#环境要求)
- [安装](#安装)
- [快速开始](#快速开始)
- [核心注解](#核心注解)
- [读取 Excel](#读取-excel)
- [写出 Excel](#写出-excel)
- [写出 CSV](#写出-csv)
- [自定义转换器](#自定义转换器)
- [运行时映射配置](#运行时映射配置)
- [配置优先级](#配置优先级)
- [底层 Handler API](#底层-handler-api)
- [常见问题](#常见问题)
- [作者与许可](#作者与许可)
- [更新日志](#更新日志)

## 特性

- 支持 `.xls` 与 `.xlsx` 格式读写；
- 支持 CSV 导出，可选择分隔符、表头与 BOM；
- 注解驱动字段映射，代码侵入低；
- 读取时支持按列索引或表头名称定位字段；
- 支持字段级、类型级、内置默认三级转换器；
- 支持多 Sheet 读取与多 Sheet 写出；
- 支持 Builder 方式在运行时注册字段映射与转换器。

## 环境要求

| 组件 | 版本 |
| --- | --- |
| JDK | 17+ |
| Apache POI | 5.4.1 |
| commons-lang3 | 3.17.0 |
| commons-csv | 1.11.0 |
| Caffeine | 3.2.3 |
| JUnit（测试） | 4.13.2 |

## 安装

本项目不再发布到 Maven 中央仓库，请在本地构建安装：

```bash
mvn clean install
```

这会将 `cn.com.binging:excel:5.0-SNAPSHOT` 安装到本地 `~/.m2` 仓库，随后可在本地其他工程中引用：

```xml
<dependency>
    <groupId>cn.com.binging</groupId>
    <artifactId>excel</artifactId>
    <version>5.0-SNAPSHOT</version>
</dependency>
```

> 如需跳过测试加快安装：`mvn clean install -DskipTests`。

## 快速开始

### 1. 定义实体类

使用 `@CellConfig` 声明 Java 字段与 Excel 列的映射关系；使用 `@OutAlias` 声明导出时的 Sheet 名称。

```java
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;

@OutAlias("人员信息")
public class Person {
    @CellConfig(index = 0, aliasName = "姓名")
    private String name;

    @CellConfig(index = 1, aliasName = "年龄")
    private Integer age;

    @CellConfig(index = 2, aliasName = "薪资")
    private Double salary;

    // getter / setter 省略
}
```

### 2. 读取 Excel

```java
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import java.io.File;

BingExcel bingExcel = BingExcelBuilder.builderInstance();

// 从第 2 行开始读取：行索引从 0 开始，因此 1 表示跳过第 1 行表头
SheetVo<Person> sheet = bingExcel.readFile(new File("person.xlsx"), Person.class, 1);

System.out.println(sheet.getSheetName());
System.out.println(sheet.getObjectList());
```

### 3. 写出 Excel

```java
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import java.util.Arrays;
import java.util.List;

BingExcel bingExcel = BingExcelBuilder.builderInstance();

List<Person> people = Arrays.asList(
    new Person("张三", 25, 10000.0),
    new Person("李四", 30, 15000.0)
);

bingExcel.writeExcel("output.xlsx", people);
```

## 核心注解

### `@CellConfig`

标注在字段上，描述字段与 Excel 列的关系。

| 属性 | 默认值 | 说明 |
| --- | --- | --- |
| `index` | `-1` | Excel 列索引，从 0 开始。写出 Excel / CSV 时必须 `>= 0`；读取时如果为 `-1` 且配置了 `aliasName`，会按表头匹配列。 |
| `aliasName` | `""` | 表头名称。读取时可用于表头匹配；写出时作为列标题。为空时通常使用字段名。 |
| `readRequired` | `false` | 读取时该字段是否必填。 |

```java
public class User {
    @CellConfig(index = 0, aliasName = "姓名")
    private String name;

    @CellConfig(index = 1, aliasName = "年龄", readRequired = true)
    private Integer age;
}
```

### `@OutAlias`

标注在类上，指定写出 Excel 时的 Sheet 名称。

```java
@OutAlias("销售数据")
public class SaleRecord {
    @CellConfig(index = 0, aliasName = "商品")
    private String product;
}
```

### `@BingConvertor`

标注在字段上，为单个字段指定转换器。

```java
import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.converter.base.BooleanFieldConverter;

public class User {
    @CellConfig(index = 0, aliasName = "是否启用")
    @BingConvertor(value = BooleanFieldConverter.class, strings = {"是", "否"}, booleans = {true})
    private Boolean active;
}
```

## 读取 Excel

### 读取单个 Sheet

```java
// 读取第一个 Sheet，从第 2 行开始
SheetVo<Person> sheet = bingExcel.readFile(file, Person.class, 1);
```

使用 `ReaderCondition` 可以控制 Sheet、起始行和结束行：

```java
import com.bing.excel.core.ReaderCondition;

ReaderCondition<Person> condition = new ReaderCondition<>(0, Person.class)
    .setStartRow(1)
    .setEndRow(100);

SheetVo<Person> sheet = bingExcel.readFile(file, condition);
```

### 按表头名称匹配列

当 Excel 列顺序不稳定时，可以只配置 `aliasName`，不配置 `index`：

```java
public class User {
    @CellConfig(aliasName = "姓名")
    private String name;

    @CellConfig(aliasName = "年龄")
    private Integer age;

    @CellConfig(aliasName = "薪资")
    private Double salary;
}
```

注意：

- 该能力仅用于读取；写出 Excel / CSV 时字段必须有合法的 `index`；
- 表头匹配时会自动去除前后空白并忽略大小写，例如表头 ` Name ` 或 `name` 都能匹配 `aliasName = "Name"`；
- 表头行必须位于 `startRow - 1`：常见用法是第 1 行为表头、`startRow = 1` 从第 2 行开始读取。若该行缺失或为空，按表头匹配的字段无法解析，读取数据行时会抛出 `IllegalCellConfigException`，而不会读到错位的数据；
- 当表头中存在重名列时，`aliasName` 会绑定到最后一个同名列；
- 同时配置 `index` 与 `aliasName` 时，优先使用 `index`，不会再查找表头；
- 表头匹配失败时会抛出 `IllegalCellConfigException`，异常信息中包含可用表头，便于排查。

### 读取多个 Sheet

```java
ReaderCondition[] conditions = new ReaderCondition[] {
    new ReaderCondition<>(0, Person.class).setStartRow(1),
    new ReaderCondition<>(1, Department.class).setStartRow(1)
};

List<SheetVo> sheets = bingExcel.readFileToList(file, conditions);
```

### 从 `InputStream` 读取

```java
try (InputStream input = new FileInputStream("person.xlsx")) {
    SheetVo<Person> sheet = bingExcel.readStream(input, Person.class, 1);
}
```

## 写出 Excel

### 写出单个 Sheet

```java
bingExcel.writeExcel("output.xlsx", people);     // xlsx
bingExcel.writeOldExcel("output.xls", people);  // xls
```

也可以写入 `OutputStream`：

```java
try (OutputStream output = new FileOutputStream("output.xlsx")) {
    bingExcel.writeExcel(output, people);
}
```

### 写出多个 Sheet

直接传入多个集合时，每个集合会写入一个 Sheet：

```java
bingExcel.writeExcel("company.xlsx", people, departments);
```

需要为不同 Sheet 指定名称或列表时，可以使用 `SheetExcel`：

```java
import com.bing.excel.core.impl.BingExcelImpl.SheetExcel;

SheetExcel peopleSheet = new SheetExcel();
peopleSheet.setSheetName("人员");
peopleSheet.setList(people);

SheetExcel departmentSheet = new SheetExcel();
departmentSheet.setSheetName("部门");
departmentSheet.setList(departments);

bingExcel.writeSheetsExcel("company.xlsx", peopleSheet, departmentSheet);
```

### 常用写出 API

| 方法 | 说明 |
| --- | --- |
| `writeExcel(String path, Iterable... iterables)` | 写出 `.xlsx` 文件。 |
| `writeOldExcel(String path, Iterable... iterables)` | 写出 `.xls` 文件。 |
| `writeExcel(OutputStream stream, Iterable... iterables)` | 写出 `.xlsx` 到输出流。 |
| `writeOldExcel(OutputStream stream, Iterable... iterables)` | 写出 `.xls` 到输出流。 |
| `writeSheetsExcel(String path, SheetExcel... sheetExcels)` | 按 `SheetExcel` 描述写出多个 Sheet。 |

## 写出 CSV

### 基础用法

```java
bingExcel.writeCSV("output.csv", people);
```

### 自定义分隔符、表头和 BOM

```java
try (OutputStream output = new FileOutputStream("output.csv")) {
    bingExcel.writeCSV(output, people, ',', true, true);
}
```

参数含义：

| 参数 | 说明 |
| --- | --- |
| `output` | 输出流。 |
| `people` | 待写出的对象集合。 |
| `','` | CSV 分隔符。 |
| `true` | 是否写出表头。 |
| `true` | 是否写入 UTF-8 BOM；用 Excel 打开中文 CSV 时建议开启。 |

## 自定义转换器

转换器用于在 Excel 单元格文本与 Java 字段值之间做双向转换。

### 实现转换器

```java
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends AbstractFieldConvertor {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean canConvert(Class<?> clz) {
        return LocalDate.class.equals(clz);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
        if (cell == null || cell.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(cell, FORMATTER);
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
        if (source == null) {
            return new OutValue("");
        }
        return new OutValue(((LocalDate) source).format(FORMATTER));
    }
}
```

### 按类型全局注册

```java
BingExcel bingExcel = BingExcelBuilder.toBuilder()
    .registerFieldConverter(LocalDate.class, new LocalDateConverter())
    .build();
```

### 内置转换器

| 转换器 | 支持类型 |
| --- | --- |
| `BooleanFieldConverter` | `boolean`、`Boolean` |
| `ByteFieldConverter` | `byte`、`Byte` |
| `CharacterFieldConverter` | `char`、`Character` |
| `DateFieldConverter` | `Date`、`java.sql.Date` |
| `DoubleFieldConverter` | `double`、`Double` |
| `FloatFieldConverter` | `float`、`Float` |
| `IntegerFieldConverter` | `int`、`Integer` |
| `LongFieldConverter` | `long`、`Long` |
| `ShortFieldConverter` | `short`、`Short` |
| `StringFieldConverter` | `String` |
| `EnumConVerter` | 枚举类型 |
| `ArrayConverter` | 数组类型 |
| `CollectionConverter` | 集合类型 |

## 运行时映射配置

如果不能修改实体类源码，或希望按场景动态配置字段映射，可以使用 Builder 注册字段映射：

```java
BingExcel bingExcel = BingExcelBuilder.toBuilder()
    .addFieldConversionMapper(Person.class, "name", 0, "姓名")
    .addFieldConversionMapper(Person.class, "age", 1, "年龄")
    .addFieldConversionMapper(Person.class, "salary", 2, "薪资")
    .build();
```

也可以为字段级别指定转换器：

```java
BingExcel bingExcel = BingExcelBuilder.toBuilder()
    .addFieldConversionMapper(Person.class, "birthday", 3, "生日", new LocalDateConverter())
    .build();
```

建议同一个字段只选择一种配置方式：要么使用注解，要么使用 Builder。对于同一个实体类，也建议保持配置方式一致，避免阅读和维护成本。

## 配置优先级

BingExcel 的映射和转换器配置由三条独立规则决定。

### 1. 字段映射来源：Builder 高于注解

同一个字段同时存在 `@CellConfig` / `@BingConvertor` 与 `addFieldConversionMapper(...)` 时，Builder 注册的字段映射优先。

需要特别注意：Builder 是“整字段替换”，不是“按属性合并”。一旦某个字段通过 `addFieldConversionMapper(...)` 注册，字段上的注解映射会被整体屏蔽，未传入的属性不会回退到注解值。

> 以上优先级适用于 `BingExcel` 的 `readFile` / `readStream` 等读取接口。底层事件模型 `BingExcelEvent` 仅使用注解映射，不会应用 Builder 通过 `addFieldConversionMapper(...)` 注册的字段映射；需要 Builder 映射生效时请使用 `BingExcel` 的常规读取方法。

### 2. 列定位：显式 `index` 高于 `aliasName`

读取时：

1. 如果 `index >= 0`，直接按列索引读取；
2. 如果 `index < 0` 且 `aliasName` 非空，按表头名称匹配列。

写出时：

- 必须提供合法的 `index`；
- `aliasName` 只作为表头文本。

### 3. 转换器：字段级高于类型级，高于内置默认

优先级从高到低：

1. 字段级转换器：`@BingConvertor` 或 `addFieldConversionMapper(..., converter)`；
2. 类型级转换器：`registerFieldConverter(type, converter)`；
3. 内置默认转换器。

## 底层 Handler API

当注解和 Builder 无法满足需求时，可以使用底层读写 Handler。例如：需要手动写表头、控制每一行内容、设置下拉框数据有效性等。

```java
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.handler.WriteHandler;
import com.bing.excel.vo.ListLine;

WriteHandler handler = ExcelWriterFactory.createXSSF("template.xlsx");
handler.createSheet("数据");

handler.writeHeader(new ListLine()
    .addValue(0, "姓名")
    .addValue(1, "性别")
    .addValue(2, "年龄"));

handler.writeLine(new ListLine()
    .addValue(0, "张三")
    .addValue(1, "男")
    .addValue(2, 25));

handler.setDataValidationList(1, 1000, 1, 1, new String[] {"男", "女", "其他"});
handler.flush();
```

常用工厂方法：

```java
WriteHandler xlsxHandler = ExcelWriterFactory.createXSSF("output.xlsx");
WriteHandler xlsHandler = ExcelWriterFactory.createHSSF("output.xls");
```

## 常见问题

### Q1：如何跳过表头？

行索引从 0 开始。如果第 1 行是表头，从第 2 行开始读取时传入 `startRow = 1`：

```java
SheetVo<Person> sheet = bingExcel.readFile(file, Person.class, 1);
```

### Q2：列顺序会变，怎么读取？

使用 `@CellConfig(aliasName = "表头名称")`，并且不要配置 `index`。读取时框架会通过表头文本定位列。

### Q3：为什么写出时报 `index < 0` 相关异常？

`aliasName` 表头匹配只支持读取。写出 Excel / CSV 时必须为每个要写出的字段提供 `index >= 0` 的列索引。

### Q4：如何处理空值？

建议实体字段使用包装类型，例如 `Integer`、`Double`、`Boolean`。基本类型无法表达 `null`，空单元格会落到类型默认值或转换器处理逻辑。

### Q5：用 Excel 打开 CSV 中文乱码怎么办？

写出 CSV 时开启 BOM：

```java
bingExcel.writeCSV(output, people, ',', true, true);
```

### Q6：注解和 Builder 可以混用吗？

不建议对同一个字段混用。Builder 注册字段映射后会整体替换该字段的注解映射，字段上的 `@BingConvertor` 也不会自动继承。

## 作者与许可

- 作者：shizhongtao（bingyulei008@gmail.com）
- 版权：Copyright 2015 http://bingExcel.svend.cc
- 许可协议：本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 开源发布，完整许可条款请参见 [LICENCE](LICENCE)。

## 更新日志

### v5.0-SNAPSHOT（未发布）

- 升级 JDK 基线至 17；
- Apache POI 升级至 5.4.1，并适配 4→5 的 API 变更；
- commons-lang3 升级至 3.17.0、commons-csv 升级至 1.11.0；
- 移除 Guava 依赖：缓存改用 Caffeine 3.2.3，工具类改用 JDK 与 commons-lang3；
- 不再发布到 Maven 中央仓库，改为本地 `mvn install` 安装。

### v4.1（未发布）

- 支持 `@CellConfig(aliasName = "...")` 通过表头匹配列（仅读取方向）；
- 表头匹配时自动去除前后空白并忽略大小写；
- 读取时若按表头匹配的字段未能解析出列索引（如表头行缺失），改为抛出 `IllegalCellConfigException`，不再以 `ArrayIndexOutOfBoundsException` 形式暴露；
- 统一说明并修正注解映射、Builder 映射和转换器之间的优先级；
- 写出 Excel / CSV 时遇到非法字段索引会抛出 `IllegalCellConfigException`，避免负索引继续向下传递。

### v4.0

- 升级依赖版本；
- 启用 Maven Central 发布配置；
- 支持快照版本发布。

### v3.0

- POI 升级至 4.1.2；
- commons-lang3 升级至 3.14.0；
- commons-csv 升级至 1.10.0；
- JUnit 升级至 4.13.2。

### v2.4

- 修复 SAX 模式读取稳定性问题。

### v2.3

- 新增多 Sheet 导出功能。

### v2.2

- 新增 CSV 导出支持。

# BingExcel 模块设计逻辑

本文档描述 `excel` 模块的整体设计、核心链路与关键约束，作为后续优化的参照基线。
写作时间：2026-07，对应 `5.0-SNAPSHOT`（JDK 17 / POI 5.4.1）。

---

## 1. 模块定位

BingExcel 是一个基于 Apache POI 的轻量级 Excel 读写工具库，核心目标：

- 把 Excel 行 ↔ Java Bean 互相转换；
- 注解驱动字段映射（`@CellConfig` / `@OutAlias` / `@BingConvertor`），代码侵入低；
- 支持按列索引或表头名称（aliasName）读取；
- 支持字段级、类型级、内置默认三级转换器；
- 读取走 SAX（`.xlsx`）/ HSSF Listener（`.xls`）低内存事件模型；
- 写出走 POI `Workbook` API，支持 XSSF / HSSF / SXSSF；
- 单例 `BingExcel` 可并发使用。

---

## 2. 顶层组件与分层

```
+--------------------------------------------------------------+
|                     API 入口层                                |
|   BingExcel(接口) / BingExcelBuilder / BingExcelImpl         |
+--------------------------------------------------------------+
|                     映射层 (mapper)                           |
|   AnnotationMapperHandler  UserDefineMapperHandler           |
|   ConversionMapper / FieldConverterMapper                    |
|   BaseGlobalConverterMapper (内置默认 converter 注册表)        |
+--------------------------------------------------------------+
|              反射适配层 (core.reflect)                        |
|   TypeAdapterConverter  (实体 ↔ ListLine/ListRow)            |
|   TitleAliasResolver    (aliasName → 列索引)                  |
+--------------------------------------------------------------+
|        转换器层 (converter)                                   |
|   FieldValueConverter / AbstractFieldConvertor               |
|   base/* (String/Number/Date/Boolean/Char/Enum)              |
+--------------------------------------------------------------+
|        读取层 (reader)                                        |
|   ExcelReaderFactory / ReadHandler                           |
|   DefaultXSSFSaxHandler (xlsx, SAX 事件)                     |
|   DefaultHSSFHandler + HSSFListenerAbstract (xls, Listener)  |
|   ExcelXSSFSheetXMLHandler / ExcelFormatTrackingHSSFListener |
|   (POI fork, 用 java.util.logging 替换 POI logger)           |
+--------------------------------------------------------------+
|        写出层 (writer)                                        |
|   ExcelWriterFactory / WriteHandler                          |
|   AbstractWriteHandler / DefaultFileWriteHandler             |
|   DefaultStreamWriteHandler / SXSSFWriterHandler             |
+--------------------------------------------------------------+
|        值对象 (vo)                                            |
|   ListRow / ListLine / CellKV / OutValue                     |
+--------------------------------------------------------------+
```

---

## 3. 关键数据结构

### 3.1 `ListRow`（读取侧一行）

- 稀疏存储 `List<CellKV<String>>`，每个 `CellKV(index, value)`。
- `toFullArray()` 展开成稠密 `String[]`，空缺位置为 `null`。
- `minIndex` / `maxIndex` 记录列范围。
- XSSF 读取：始终 add 一个 CellKV，空值 value 为 `null`。
- HSSF 读取：空值 cell 一般跳过，仅 `MissingCellDummyRecord` 补 `null`。

### 3.2 `ListLine`（写出侧一行）

- 按类型分桶：`listStr / listDouble / listBoolean / listDate / listLong`。
- 每个 bucket 存 `List<CellKV<T>>`。
- 写出时由 `WriteHandler` 按类型分别 `setCellValue`。
- `addValue(index, T)` 链式构建。

### 3.3 `CellKV<T>`

- `(int index, T value)`，index 不能为负。
- 读取侧 `T = String`；写出侧 `T ∈ {String, Double, Boolean, Date, Long}`。

### 3.4 `OutValue`（converter → ListLine 的中间值）

- `OutType { INTEGER, LONG, DOUBLE, STRING, DATE, UNDEFINED }`。
- converter 的 `toObject()` 返回 `OutValue`，`TypeAdapterConverter` 按 OutType 路由到 `ListLine.addValue(...)`。
- 静态工厂：`intValue / longValue / doubleValue / stringValue / dateValue`。

---

## 4. 映射体系

### 4.1 三级 converter 来源（优先级高 → 低）

1. **字段级注解** `@BingConvertor`：通过 `AnnotationMapperHandler.cacheConverter` 反射实例化，按 `(converterType, 参数列表)` 缓存。
2. **用户 Builder 注册**：`BingExcelBuilder.registerFieldConverter(Class, converter)` 或 `addFieldConversionMapper(...)`。`addFieldConversionMapper` 是**整字段替换**，会完全屏蔽该字段上的 `@CellConfig` / `@BingConvertor`。
3. **内置默认** `BaseGlobalConverterMapper.globalFieldConverterMapper`：不可变 Map，按 Class 注册（String/Date/Enum/Integer/Long/Boolean/Byte/Character/Double/Float/Short）。

### 4.2 `FieldConverterMapper`（字段映射元数据）

每个被处理的字段在 `ConversionMapper.fieldMapper` 里有一条 `FieldConverterMapper`：

- `index`：列索引（`-1` 表示仅依赖 aliasName）
- `alias`：表头名（写出时作列标题；读取时若 `index<0` 则用于表头匹配）
- `clazz`：字段类型
- `isPrimitive`：是否基本类型
- `readRequired`：读取时是否必填
- `converter`：字段级 converter（可为 `null`，落到默认查找）

### 4.3 `TypeAdapterConverter`（实体级反射适配）

- 每个 entity class 对应一个 `TypeAdapterConverter`，缓存在 `BingExcelImpl.typeTokenCache`（`ConcurrentHashMap`）。
- 构造时遍历 `getDeclaredFields()`，跳过 static / transient / synthetic / enum constant，其余字段（无论有无 `@CellConfig`）都进 `boundFields`。
- `marshal` / `unmarshal` 通过 `getFieldConverterMapper(fieldName, handlers)` 在传入的 handler 链里找 `FieldConverterMapper`，第一个非空命中。
- **默认 converter 解析不再回写共享 mapper**：`getLocalConverter(...)` 是纯读取，converter 作为局部变量传给 `BoundField.serializeValue` / `initializeValue`。这是 5.0 的线程安全关键点。

### 4.4 列定位规则

- `@CellConfig(index = N, aliasName = "X")`：读用 index，写用 index+aliasName。
- `@CellConfig(index = -1, aliasName = "X")`：纯 aliasName 字段。
  - 写出方向：`index < 0` 直接抛 `IllegalCellConfigException`（aliasName 是**只读**的，不允许写）。
  - 读取方向：靠 `TitleAliasResolver` 在 `startRow-1` 行抓表头，按 `normalize()`（trim + toLowerCase ROOT）匹配。
    - 找到 → 映射到实际列索引。
    - 找不到 + `readRequired=true` → 抛异常。
    - 找不到 + `readRequired=false` → 跳过，字段保持默认值。
  - `startRow=0` 且存在 alias-only 字段 → 立即抛异常（没有标题行可解析）。

---

## 5. 读取链路详解

### 5.1 入口

`BingExcelImpl.readFile(file, condition)` / `readFileToList(file, conditions[])` / `readStream(...)`。

### 5.2 整体流程（以 `readFileToList` 为例）

```
BingExcelImpl.readFileToList(file, conditions)
  └─ new BingExcelReaderListener(conditions, resultList)   # 内部类，extends AbstractExcelReadListener
  └─ ExcelReaderFactory.create(file, listener, true)       # 按 magic 选 XSSF SAX 或 HSSF
  └─ 计算 indexArr + minNum (各 condition endRow 的最小值)
  └─ handler.readSheet(indexArr, minNum)
        ├─ XSSF: DefaultXSSFSaxHandler.readSheet(int[])
        │     ├─ OPCPackage.open 已在 factory 完成
        │     ├─ XSSFReader + ExcelReadOnlySharedStringsTable
        │     ├─ ExcelXSSFSheetXMLHandler (POI fork) 作为 ContentHandler
        │     ├─ 遍历 sheets，命中 setSheets 的才 startSheet/parse/endSheet
        │     ├─ 每个 cell → DefaultSheetContentsHandler.cell(ref, value, comment)
        │     │     └─ nameToColumn(ref) → column index
        │     │     └─ rowList.add(CellKV(column, isEmpty(value)?null:value))
        │     ├─ endRow → excelReader.optRow(rowNum, rowList)
        │     └─ finally pkg.revert()
        └─ HSSF: DefaultHSSFHandler.readSheet(int[])
              └─ HSSFListenerAbstract.process()
                    ├─ MissingRecordAwareHSSFListener + ExcelFormatTrackingHSSFListener
                    ├─ HSSFEventFactory.processWorkbookEvents
                    ├─ processRecord(Record) 按 sid 分发
                    │     ├─ BOFRecord(TYPE_WORKSHEET) → sheetIndex++, 按 aimSheetIndex/aimSheetName 决定 startRead
                    │     ├─ BoundSheetRecord → 记录 sheet 名
                    │     ├─ LabelRecord/LabelSSTRecord/NumberRecord/RKRecord/FormulaRecord/...
                    │     │     └─ formatListener.formatNumberDateCell → value, 非空才 add CellKV
                    │     ├─ BlankRecord → add CellKV(column, null)
                    │     ├─ MissingCellDummyRecord → add CellKV(column, null)
                    │     └─ LastCellOfRowDummyRecord → optRows(sheetIndex, curRow, rowlist) + rowlist=new ListRow()
                    └─ finally closeFs()
```

### 5.3 `BingExcelReaderListener.optRow`（实体转换核心）

```
optRow(curRow, rowList):
  if (tagertClazz != null && startRow >= 1 && curRow == startRow-1
      && titleAliasResolver != null && !resolved):
      titleAliasResolver.captureTitleRow(rowList)      # 抓标题行
      titleAliasResolver.resolve(...)                  # 解析 alias → index
      return
  if (curRow < startRow) return
  if (tagertClazz != null):
      typeAdapter = typeTokenCache.get(tagertClazz)
      obj = typeAdapter.unmarshal(rowList, titleAliasResolver, userDefineMapperHandler, annotationMapperHandler)
      currentSheetVo.addObject(obj)
```

### 5.4 `TypeAdapterConverter.unmarshal`

```
obj = constructor.newInstance()
fullArray = source.toFullArray()
for each boundField:
    mapper = getFieldConverterMapper(fieldName, handlers)   # 用户/注解 handler 优先
    converter = getLocalConverter(mapper)                   # mapper.converter ?? default lookup (不回写)
    index = resolver != null ? resolver.getResolvedIndex(name, mapper) : mapper.index
    if (index < 0):
        if mapper.readRequired → throw
        else continue                                        # 字段保持默认
    value = fullArray[index] (越界则 null)
    boundField.initializeValue(obj, value, mapper, converter):
        if value != null:
            canConvert 校验
            fieldValue = converter.fromString(value, handler, mapper.fieldClass)
            field.set(obj, fieldValue)
        else if mapper.readRequired → throw illegalValueException
```

### 5.5 资源管理

- `ExcelReaderFactory.create(File)`：先尝试 `POIFSFileSystem`（xls），`OfficeXmlFileException` 时 fallback 到 `OPCPackage`（xlsx）；失败分别 close/revert。
- `ExcelReaderFactory.create(InputStream)`：用 `PushbackInputStream` + `FileMagic` 判型。
- XSSF：`readSheet` / `readSheets` 在 `finally` 里 `pkg.revert()`。
- HSSF：`process()` 在 `finally` 里 `closeFs()`；`EOFRecord` 处理时也会 closeFs（双保险）。

---

## 6. 写出链路详解

### 6.1 入口

`BingExcelImpl.writeXlsx / writeXls / writeCSV / writeSheetsExcel`。

### 6.2 整体流程（以 `writeXlsx(path, iterables...)` 为例）

```
BingExcelImpl.writeXlsx(file, iterables)
  └─ ExcelWriterFactory.createXSSF(file) → DefaultFileWriteHandler(new XSSFWorkbook, file)
  └─ writeToExcel(handler, iterables):
       for each iterable:
         if empty → handler.createSheet("sheet1")
         for each object:
           if 首个非空:
             clazz = object.class
             annotationMapperHandler.processEntity(clazz)
             registeAdapter(clazz)                          # 建 TypeAdapterConverter 入 typeTokenCache
             modelName = userDefine.modelName ?? annotation.modelName ?? clazz.simpleName
             handler.createSheet(modelName)
             header = typeAdapter.getHeader(userDefine, annotation)
             handler.writeHeader(header)
             line = typeAdapter.marshal(object, userDefine, annotation)
             handler.writeLine(line)
           else:
             line = typeAdapter.marshal(object, ...)
             handler.writeLine(line)
       handler.flush()  → wb.write(os); wb.close()
```

### 6.2.1 `TypeAdapterConverter.marshal`

```
for each boundField:
    mapper = getFieldConverterMapper(name, handlers)
    if mapper.index < 0 → throw (写必须有 index)
    converter = getLocalConverter(mapper)
    boundField.serializeValue(entity, mapper, converter, line):
        obj = field.get(entity)
        outValue = converter.toObject(obj, defaultHandler)
        按 outValue.outType 路由：
            DATE   → line.addValue(index, (Date) value)
            DOUBLE → line.addValue(index, (double) value)
            INTEGER→ line.addValue(index, (int) value)
            LONG   → line.addValue(index, (long) value)
            STRING → line.addValue(index, value.toString())
            UNDEFINED → line.addValue(index, value.toString())
```

### 6.3 `WriteHandler` 实现

- `AbstractWriteHandler`：
  - 持有 `Workbook wb` / `OutputStream os` / `Sheet currentSheet` / `currentRowIndex`。
  - `writeHeader` / `writeLine` 按 `ListLine` 的各 bucket 分别 `createCell + setCellValue`。
  - `createSheet(name)`：重名时自动加 `-N` 后缀。
  - `flush()`：`wb.write(os); wb.close()`（**注意：不关闭 os，由调用方负责**）。
  - 日期单元格用固定 style `m/d/yy h:mm`。
  - `setDataValidationList(short,short,short,short,String[])` 下拉校验。
- `DefaultFileWriteHandler` / `DefaultStreamWriteHandler`：构造时区分 File / OutputStream。
- `SXSSFWriterHandler`：基于 `SXSSFWorkbook(200)` 流式写，适合大数据量；但目前 `BingExcel.writeXlsx` 默认仍走 `XSSFWorkbook`，SXSSF 需用户直接调 `ExcelWriterFactory.createSXSSF`。

### 6.4 CSV 写出

- `BingExcelImpl.writeCSV(...)` 三个重载，逻辑高度重复（已知技术债）。
- 用 `commons-csv` `CSVPrinter`，BOM 手写 `0xEF 0xBB 0xBF`。
- 表头来自 `typeAdapter.getHeadertoListLine(...)`。
- `commons-csv` 在 pom 里是 `optional`，调用方需自声明。

---

## 7. 线程安全模型

`BingExcel` 单例并发安全，关键点：

| 状态 | 机制 |
|------|------|
| `typeTokenCache` | `ConcurrentHashMap` |
| `userDefineMapperHandler` | `volatile` + DCL |
| `annotatedTypes` | `Collections.synchronizedSet` |
| `converterCache`（注解 converter 缓存） | `ConcurrentHashMap` |
| `ConversionMapper.fieldMapper` | `ConcurrentHashMap` |
| 默认 converter 查找 | 5.0 起**不再回写**共享 `FieldConverterMapper`，`TypeAdapterConverter` 每次 marshal/unmarshal 局部解析 |
| 读写 I/O 状态 | 每次 read/write 创建独立 handler/listener，无共享 |

`LocalConverterHandler.defaultLocalConverter` 是 `Collections.synchronizedMap(HashMap)`，`getLocalConverter` 里 fallback 到全局表后会把结果 put 回 local map（局部缓存优化，map 本身同步）。

---

## 8. 已知设计约束（不可破坏）

- `registeAdapter` / `tagertClazz` 是故意拼错，跨 `BingExcelImpl` + `BingExcelEventImpl`，**不改名**。
- `aliasName` 是**只读**语义，**不扩展**到写路径。
- `addFieldConversionMapper(...)` 是**整字段替换**，不做属性合并。
- `BingExcelEvent*` 系列、`ArrayConverter` / `CollectionConverter`、`ListRow.getList()`、`BingExcelBuilder.builder()` 都是 `@Deprecated` 但保留功能，未明确要求不删。
- 升级 POI 时需检查 fork 的 `ExcelXSSFSheetXMLHandler` / `ExcelFormatTrackingHSSFListener`（用 `java.util.logging` 替换了 POI logger）。
- 源码用 tab 缩进 + CRLF。

---

## 9. 当前已识别的可优化方向

> 更新时间：2026-07-03。标记 ✅ 为已完成，⬜ 为待处理。

### 读取侧

1. ✅ `readStreamToList` 的 `minNum` 初始化为 0 导致 `endRow` 限制失效（P0 bug，已修复）。
2. ✅ `readFileToList` / `readStreamToList` 重复代码抽取为 `readWithHandler`。
3. ✅ `DefaultXSSFSaxHandler` 三个 readSheet 方法重复逻辑抽取为 `readSheetsInternal`。
4. ✅ `readSheet(int[])` 里 `Set.copyOf(build)` 冗余，已简化。
5. ✅ `ExcelReaderFactory.create(InputStream)` 嵌套层数多，已清除死代码 + 移除冗余 `IOUtils.peekFirst8Bytes`。
6. ✅ XSSF `nameToColumn` 对空 `cellReference` 无防御，已加 null/empty 判断。
7. ✅ `XMLReaderFactory` 已 deprecated，已迁移到标准 `SAXParserFactory`。
8. ✅ `HSSFListenerAbstract` 的 `LabelRecord` / `LabelSSTRecord` / `NumberRecord` 的 `trim()` 副作用已移除。
9. ⬜ `HSSFListenerAbstract.processRecord` 是巨型方法（200+ 行），按 sid 分发可拆（低优先，重构风险高）。
10. ⬜ `BingExcelReaderListener` 在 `readFile` 单 condition 时走 `readFileToList` 拿 list[0]，可短路（收益小）。
11. ⬜ 资源关闭路径已审查，无明显的泄露问题，但异常处理可进一步收紧。

### 映射/转换侧

12. ✅ `TypeAdapterConverter` 默认 converter 查询不再回写共享 `FieldConverterMapper`，根治并发懒初始化。
13. ✅ 所有基础转换器统一 `StringUtils.isBlank` 空白策略并 `trim()` 输入。
14. ✅ `BooleanFieldConverter` 默认模式改用 `BooleanUtils`。
15. ✅ `DateFieldConverter` 移除 ThreadLocal，修复格式污染，`smartConversion` 生效。
16. ✅ `EnumConVerter` 新增 `toObject`，读写一致用 `Enum.name()`。
17. ✅ `FloatFieldConverter` 修复 `dateValue` → `doubleValue`。
18. ✅ `LongFieldConverter` 修复 `charAt(1)` 越界。
19. ✅ `ByteFieldConverter` / `IntegerFieldConverter` 收紧为 Java 有符号范围。
20. ⬜ `OutValue` 缺 `BOOLEAN` / `BIG_DECIMAL` / `LOCAL_DATE` 类型。
21. ⬜ `FieldValueConverter.fromString` 的 `Type targetType` 实际只传 `Class`，泛型信息丢失。
22. ⬜ `converter` 包混入 `ModelAdapter` / `HeaderReflectConverter`（实体级接口），职责偏宽。

### 写出侧

23. ⬜ CSV 三个 `writeCSV` 重载代码重复严重。
24. ⬜ `AbstractWriteHandler.flush()` 不关闭/不 flush `OutputStream`，依赖调用方。
25. ⬜ `BingExcel.writeXlsx` 默认 `XSSFWorkbook`，大数据量易 OOM；可考虑默认或可选 SXSSF。
26. ⬜ `AbstractWriteHandler` 里 `currentRowIndex < 2` 判断列宽的逻辑脆弱（与 header 写入耦合）。
27. ⬜ 日期单元格格式 `m/d/yy h:mm` 硬编码，不可配置。

### 测试侧

28. ⬜ converter 测试堆在 `WriteTest7`，应拆 `BaseFieldConverterTest`。
29. ⬜ `LocalConverterHandler` / `TypeAdapterConverter` / `AnnotationMapperHandler` 无单测覆盖。

---

## 10. 后续优化执行顺序建议

已完成（commit `a90f918` + `f76282b`）：
- 转换器层修复与线程安全优化
- 读取链路 bug 修复与重构优化

下一步按"风险低、收益明确"优先：

1. ⬜ CSV 写出去重重构（P1）。
2. ⬜ `AbstractWriteHandler.flush()` 资源语义明确化（P1）。
3. ⬜ `OutValue` 类型扩展 + BigDecimal/LocalDate converter（P2，功能增强）。
4. ⬜ converter 测试拆分（P2）。
5. ⬜ `HSSFListenerAbstract.processRecord` 拆方法（P2，可读性，重构风险中）。
6. ⬜ 包结构/命名整理（P3，大版本再做）。

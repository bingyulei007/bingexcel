# Excel数据读取到Java实体类转化优化设计

**日期：** 2026-05-19
**状态：** 进行中（已确认方案，待完成详细设计）

---

## 1. 背景与目标

### 1.1 优化目标
- **主要目标：** 提升转化性能
- **场景：** 高频小文件读取
- **预期提升：** 20-50%

### 1.2 约束条件
- 允许API调整，但保持API兼容
- 现有测试用例必须全部通过

### 1.3 优化范围
- 数据结构优化（ListRow.toFullArray()）
- 映射查找优化（TypeAdapterConverter中的字段映射查找）

---

## 2. 现有实现分析

### 2.1 数据流向
```
Excel -> POI -> ListRow -> TypeAdapterConverter.unmarshal() -> Java对象
```

### 2.2 核心类
- `BingExcelImpl.readFileToList()` - 入口
- `ListRow` - 行数据容器，内部是`List<CellKV<String>>`
- `TypeAdapterConverter.unmarshal()` - 实体类转化核心

### 2.3 性能瓶颈
- `ListRow.toFullArray()` 每行调用时创建新String[]
- `TypeAdapterConverter.unmarshal` 中`getFieldConverterMapper()`线性遍历handler数组
- 每行对象通过`constructor.newInstance()`反射创建

---

## 3. 已确认的优化方案

### 3.1 方案选择
**选择方案1：数据结构优化**

### 3.2 已确认的设计

#### 第1部分：ListRow的改造
**当前实现：**
- ListRow内部是`List<CellKV<String>>`，每个CellKV包含(index, value)
- `toFullArray()`方法创建新的String[]，遍历所有CellKV赋值
- 每行调用都会创建新数组，高频场景下GC压力大

**优化方案：**
- 新增`getString(int colIndex)`方法，按列索引直接获取值
- 内部实现：遍历CellKV列表查找匹配的索引，返回值
- 避免创建中间String[]，减少对象分配

**API变化：**
- 保持`toFullArray()`方法不变（向后兼容）
- 新增`getString(int colIndex)`高效访问方法

#### 第2部分：TypeAdapterConverter.unmarshal()的优化
**当前实现：**
- 调用`ListRow.toFullArray()`获取String[]
- 通过数组下标访问各列值
- 每行都创建新的String[]

**优化方案：**
- 修改`unmarshal()`方法，直接调用`ListRow.getString(colIndex)`
- 遍历BoundField时，按字段对应的列索引直接获取值
- 跳过toFullArray()，避免数组创建

**数据流变化：**
```
原：ListRow -> toFullArray() -> String[] -> 按下标访问
新：ListRow -> getString(colIndex) -> 直接获取值
```

---

## 4. 待完成设计

### 4.1 第3部分：API调整（待确认）
- 保持现有API兼容
- 新增高效访问方法
- 需要调整哪些接口？

### 4.2 第4部分：性能测试（待确认）
- 如何验证优化效果？
- 需要哪些性能测试用例？

### 4.3 第5部分：实现计划（待确认）
- 实现步骤和优先级
- 风险评估和回滚方案

---

## 5. 下次继续

**待完成事项：**
1. 完成第3部分设计（API调整）
2. 完成第4部分设计（性能测试）
3. 完成第5部分设计（实现计划）
4. 用户审查完整设计
5. 调用writing-plans skill创建实现计划

**继续方式：**
- 下次对话时，可以直接说"继续Excel转化优化设计"
- 或者引用此设计文档继续讨论

---

## 6. 相关代码位置

- `ListRow`: `/Users/shi/workspace/excel/bingExcel/excel/src/main/java/com/bing/excel/vo/ListRow.java`
- `TypeAdapterConverter`: `/Users/shi/workspace/excel/bingExcel/excel/src/main/java/com/bing/excel/core/reflect/TypeAdapterConverter.java`
- `BingExcelImpl`: `/Users/shi/workspace/excel/bingExcel/excel/src/main/java/com/bing/excel/core/impl/BingExcelImpl.java`

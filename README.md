# bingExcel

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

处理 Excel 与 Java Model 之间双向转换的轻量级框架，基于 Apache POI 构建。

详细使用文档请查看 [BINGEXCEL_WIKI.md](./excel/BINGEXCEL_WIKI.md)

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
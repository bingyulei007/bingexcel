package com.bing.excel;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.ReadHandler;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListRow;

/**
 * SaxReader测试用例 - 展示各种用法
 */
public class SaxReaderTest2 {

    /**
     * 测试1: 基础用法 - 读取Excel文件所有sheet
     * 使用File对象，默认不忽略数字格式
     */
    @Test
    public void testReadAllSheets() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("开始读取Sheet: " + name + " (索引:" + sheetIndex + ")");
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                // 打印每行数据
                System.out.println("第" + curRow + "行: " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("结束读取Sheet: " + name);
            }

            @Override
            public void endWorkBook() {
                System.out.println("全部读取完成!");
            }
        });

        handler.readSheets();
    }

    /**
     * 测试2: 读取指定索引的单个sheet
     * readSheet(int index) - 读取指定索引的sheet，从0开始
     */
    @Test
    public void testReadSheetByIndex() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("第" + curRow + "行: " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("Sheet " + name + " 读取完成");
            }

            @Override
            public void endWorkBook() {
                System.out.println("读取完成!");
            }
        });

        // 只读取第一个sheet (索引0)
        handler.readSheet(0);
    }

    /**
     * 测试3: 通过sheet名称读取
     * readSheet(String indexName) - 通过sheet名称精确匹配
     */
    @Test
    public void testReadSheetByName() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("第" + curRow + "行: " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("Sheet " + name + " 读取完成");
            }

            @Override
            public void endWorkBook() {
                System.out.println("读取完成!");
            }
        });

        // 通过名称读取sheet (需要确切知道sheet名称)
        handler.readSheet("Sheet1");
    }

    /**
     * 测试4: 限制读取行数
     * readSheet(int index, int maxReadLine) - 只读取前maxReadLine行
     */
    @Test
    public void testReadWithMaxLines() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("开始读取Sheet: " + name + "，限制前5行");
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("第" + curRow + "行: " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("读取完成!");
            }

            @Override
            public void endWorkBook() {
                System.out.println("全部完成!");
            }
        });

        // 只读取前5行数据
        handler.readSheet(0, 5);
    }

    /**
     * 测试5: 读取所有sheet但限制总行数
     * readSheets(int maxReadLine) - 所有sheet加起来只读maxReadLine行
     */
    @Test
    public void testReadAllSheetsWithMaxLines() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("开始读取: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("行" + curRow + ": " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("结束: " + name);
            }

            @Override
            public void endWorkBook() {
                System.out.println("全部完成!");
            }
        });

        handler.readSheets(10); // 所有sheet加起来只读10行
    }

    /**
     * 测试6: 使用InputStream读取
     * 适用于从网络下载或数据库读取的场景
     */
    @Test
    public void testReadWithInputStream() throws Exception {
        InputStream stream = Salary.class.getResourceAsStream("/salary.xlsx");

        ReadHandler handler = ExcelReaderFactory.create(stream, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("行" + curRow + ": " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheet(0);
    }

    /**
     * 测试7: 忽略数字格式化
     * 设置ignoreNumFormat为true，数字将以原始字符串形式返回
     * 例如: 日期2016-3-23将不会格式化为日期对象
     */
    @Test
    public void testReadIgnoringNumFormat() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        // 第三个参数ignoreNumFormat设为true
        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取: " + name + " (忽略数字格式)");
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("第" + curRow + "行: " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        }, true); // ignoreNumFormat = true

        handler.readSheets();
    }

    /**
     * 测试8: 读取多个指定sheet
     * readSheet(int[] indexs) - 通过索引数组指定要读取的sheet
     */
    @Test
    public void testReadMultipleSheets() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println(">>> 开始读取Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("行" + curRow + ": " + rowList.toFullArray());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("<<< Sheet " + name + " 读取完成\n");
            }

            @Override
            public void endWorkBook() {
                System.out.println("全部完成!");
            }
        });

        // 读取索引为0和1的sheet
        handler.readSheet(new int[]{0, 1});
    }

    /**
     * 测试9: 将读取的数据映射到对象列表
     * 展示如何将ListRow转换为具体的业务对象
     */
    @Test
    public void testMapToObject() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        // 用于存储解析后的对象
        final List<Salary> salaryList = new ArrayList<>();

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                // 跳过表头(第0行)
                if (curRow == 0) {
                    return;
                }
                // 将ListRow转换为String数组
                String[] rowData = rowList.toFullArray();
                Salary salary = new Salary();
                // 根据实际列索引映射 (这里假设:
                // 0=id, 1=employNum, 12=salary, 13=atypiaDate, 14=trueDate)
                if (rowData.length > 1) {
                    salary.setId(rowData[0]);
                    salary.setEmployNum(rowData[1]);
                }
                if (rowData.length > 12) {
                    salary.setSalary(rowData[12] != null ?
                        Double.parseDouble(rowData[12]) : null);
                }
                salaryList.add(salary);
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
                System.out.println("共读取 " + salaryList.size() + " 条数据");
                for (Salary s : salaryList) {
                    System.out.println(s);
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 测试10: 遍历ListRow中的CellKV
     * 展示如何访问每个单元格的索引和值
     */
    @Test
    public void testIterateCells() throws Exception {
        URL url = Salary.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("--- 第" + curRow + "行 ---");
                // 遍历每一行的单元格
                for (CellKV<String> cell : rowList) {
                    System.out.println("  列" + cell.getIndex() + ": " + cell.getValue());
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheet(0);
    }

    // 测试用内部类 - 实际项目中应该是独立的类
    public static class Salary {
        private String id;
        private String employNum;
        private Double salary;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmployNum() { return employNum; }
        public void setEmployNum(String employNum) { this.employNum = employNum; }
        public Double getSalary() { return salary; }
        public void setSalary(Double salary) { this.salary = salary; }

        @Override
        public String toString() {
            return "Salary{id=" + id + ", employNum=" + employNum + ", salary=" + salary + "}";
        }
    }
}
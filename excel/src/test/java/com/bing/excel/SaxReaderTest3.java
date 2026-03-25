package com.bing.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.bing.excel.exception.BingSaxReadStopException;
import com.bing.excel.reader.ExcelReadListener;
import com.bing.excel.reader.ExcelReaderFactory;
import com.bing.excel.reader.ReadHandler;
import com.bing.excel.reader.sax.DefaultXSSFSaxHandler;
import com.bing.excel.vo.ListRow;

/**
 * SaxReader高级用法测试
 */
public class SaxReaderTest3 {

    /**
     * 测试1: 读取.xls格式(HSSF)文件
     * ExcelReaderFactory自动根据文件格式选择处理方式
     */
    @Test
    public void testReadXlsFile() throws Exception {
        // 使用项目中的xssf-align.xlsx测试
        URL url = SaxReaderTest3.class.getResource("/xssf-align.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("第" + curRow + "行: " + rowList.toString());
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheets();
    }

    /**
     * 测试2: 使用InputStream读取
     * 适用于从网络、数据库等场景获取数据
     */
    @Test
    public void testReadFromInputStream() throws Exception {
        InputStream stream = SaxReaderTest3.class.getResourceAsStream("/salary.xlsx");

        ReadHandler handler = ExcelReaderFactory.create(stream, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("行" + curRow + ": " + java.util.Arrays.toString(rowList.toFullArray()));
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheets();
    }

    /**
     * 测试3: 读取时进行数据验证和过滤
     * 只处理符合条件的数据
     */
    @Test
    public void testDataValidation() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final List<String[]> validRows = new ArrayList<>();

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("开始验证数据...");
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                String[] row = rowList.toFullArray();
                // 示例: 只保留有完整数据的行(这里简单判断第一列不为空)
                if (row.length > 0 && row[0] != null && !row[0].trim().isEmpty()) {
                    validRows.add(row);
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("有效数据: " + validRows.size() + " 行");
            }

            @Override
            public void endWorkBook() {
                for (String[] row : validRows) {
                    System.out.println(java.util.Arrays.toString(row));
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 测试4: 多sheet数据分别收集
     * 将不同sheet的数据分别存储到不同集合
     */
    @Test
    public void testCollectDataPerSheet() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        // 存储每个sheet的数据
        final Map<Integer, List<String[]>> sheetDataMap = new HashMap<>();
        final Map<Integer, String> sheetNameMap = new HashMap<>();
        final int[] currentSheetIndex = {0};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                currentSheetIndex[0] = sheetIndex;
                sheetDataMap.put(sheetIndex, new ArrayList<>());
                sheetNameMap.put(sheetIndex, name);
                System.out.println("开始读取: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                // 跳过表头
                if (curRow > 0) {
                    sheetDataMap.get(currentSheetIndex[0]).add(rowList.toFullArray());
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println(name + " 读取完成，共 " +
                    sheetDataMap.get(sheetIndex).size() + " 条数据");
            }

            @Override
            public void endWorkBook() {
                System.out.println("\n========== 汇总 ==========");
                for (Integer index : sheetDataMap.keySet()) {
                    System.out.println("Sheet[" + index + "] " + sheetNameMap.get(index) +
                        ": " + sheetDataMap.get(index).size() + " 行");
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 测试5: 统计Excel信息(行数、列数等)
     * 不存储全部数据，只做统计
     */
    @Test
    public void testExcelStatistics() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final int[] totalRows = {0};
        final int[] totalSheets = {0};
        final int[] maxCols = {0};
        final int[] currentRows = {0};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                totalSheets[0]++;
                currentRows[0] = 0;
                System.out.println("统计Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                currentRows[0]++;
                if (rowList.size() > maxCols[0]) {
                    maxCols[0] = rowList.size();
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                totalRows[0] += currentRows[0];
                System.out.println("  - 行数: " + currentRows[0]);
            }

            @Override
            public void endWorkBook() {
                System.out.println("\n========== Excel统计 ==========");
                System.out.println("Sheet数量: " + totalSheets[0]);
                System.out.println("总行数: " + totalRows[0]);
                System.out.println("最大列数: " + maxCols[0]);
            }
        });

        handler.readSheets();
    }

    /**
     * 测试6: 使用路径字符串创建handler
     */
    @Test
    public void testReadFromPath() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        String path = url.toURI().getPath();

        ReadHandler handler = ExcelReaderFactory.create(new File(path), new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("Sheet: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                if (curRow <= 3) {
                    System.out.println("行" + curRow + ": " + rowList.toString());
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheets();
    }

    /**
     * 测试7: 直接使用DefaultXSSFSaxHandler
     * 当需要更细粒度控制时
     */
    @Test
    public void testDirectXSSFSaxHandler() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        DefaultXSSFSaxHandler handler = new DefaultXSSFSaxHandler(file,
            new ExcelReadListener() {
                @Override
                public void startSheet(int sheetIndex, String name) {
                    System.out.println("读取: " + name);
                }

                @Override
                public void optRow(int curRow, ListRow rowList) {
                    System.out.println("行" + curRow + ": " + rowList.toString());
                }

                @Override
                public void endSheet(int sheetIndex, String name) {
                }

                @Override
                public void endWorkBook() {
                }
            }, false);

        handler.readSheets();
    }

    /**
     * 测试8: 按列索引提取数据
     * 将Excel数据转换为列导向的结构
     */
    @Test
    public void testColumnOrientedData() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        // 按列存储数据: columnIndex -> List<value>
        final Map<Integer, List<String>> columnData = new HashMap<>();

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("按列提取: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                for (int i = 0; i < rowList.toFullArray().length; i++) {
                    columnData.computeIfAbsent(i, k -> new ArrayList<>())
                        .add(rowList.toFullArray()[i]);
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
                // 打印前3列的数据
                for (int i = 0; i < Math.min(3, columnData.size()); i++) {
                    System.out.println("第" + i + "列: " + columnData.get(i).subList(0,
                        Math.min(5, columnData.get(i).size())));
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 测试9: 异常处理示例
     * 展示如何处理读取过程中的异常
     */
    @Test
    public void testExceptionHandling() {
        try {
            File nonExistFile = new File("/non/exist/file.xlsx");
            ReadHandler handler = ExcelReaderFactory.create(nonExistFile,
                new ExcelReadListener() {
                    @Override
                    public void startSheet(int sheetIndex, String name) {}
                    @Override
                    public void optRow(int curRow, ListRow rowList) {}
                    @Override
                    public void endSheet(int sheetIndex, String name) {}
                    @Override
                    public void endWorkBook() {}
                });
            handler.readSheets();
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("读取异常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /**
     * 测试10: 使用ReadHandler接口的多种readSheet重载
     */
    @Test
    public void testVariousReadMethods() throws Exception {
        URL url = SaxReaderTest3.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println(">>> " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                System.out.println("行" + curRow);
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("<<< " + name + " 完成\n");
            }

            @Override
            public void endWorkBook() {
                System.out.println("=== 全部完成 ===");
            }
        });

        handler.readSheets();
    }
}
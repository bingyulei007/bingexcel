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
import com.bing.excel.vo.ListRow;

/**
 * SaxReader实用场景测试
 */
public class SaxReaderTest4 {

    /**
     * 场景1: 导入用户数据
     * 将Excel数据批量导入系统
     */
    @Test
    public void testImportUsers() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final List<User> users = new ArrayList<>();

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("导入数据表: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                // 跳过表头(第0行)和空行
                if (curRow == 0 || rowList.size() == 0) {
                    return;
                }

                String[] data = rowList.toFullArray();
                User user = new User();
                // 根据列索引映射: 0=ID, 1=工号, 2=姓名, 3=部门...
                if (data.length > 0) user.setId(data[0]);
                if (data.length > 1) user.setEmployeeNum(data[1]);
                if (data.length > 2) user.setName(data[2]);
                if (data.length > 3) user.setDepartment(data[3]);
                users.add(user);
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("已导入 " + users.size() + " 条用户数据");
            }

            @Override
            public void endWorkBook() {
                System.out.println("导入完成!");
                // 实际应用中这里会调用service保存到数据库
                for (User u : users) {
                    System.out.println("  " + u);
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 场景2: 导出前预览数据
     * 导出前先读取并展示，让用户确认
     */
    @Test
    public void testPreviewBeforeExport() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        System.out.println("========== 数据预览 ==========");

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            private boolean isFirstSheet = true;

            @Override
            public void startSheet(int sheetIndex, String name) {
                if (!isFirstSheet) {
                    System.out.println();
                }
                isFirstSheet = false;
                System.out.println("Sheet: " + name);
                System.out.println("---------------------------");
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                String[] data = rowList.toFullArray();
                // 只显示前5行
                if (curRow <= 5) {
                    System.out.format("行%-3d: %s%n", curRow,
                        curRow == 0 ? "[表头]" : java.util.Arrays.toString(data));
                } else if (curRow == 6) {
                    System.out.println("... (共 " + getActualRowCount(rowList) + " 行)");
                }
            }

            private int getActualRowCount(ListRow rowList) {
                // 实际项目中需要统计，这里简化处理
                return 100;
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
                System.out.println("===========================");
                System.out.println("预览结束，确认导出?");
            }
        });

        handler.readSheets();
    }

    /**
     * 场景3: 数据校验与错误收集
     * 读取时检查数据合法性，汇总所有错误
     */
    @Test
    public void testDataValidationWithErrors() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final List<String> errors = new ArrayList<>();
        final int[] rowNum = {0};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("校验数据表: " + name);
                rowNum[0] = 0;
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                rowNum[0]++;
                if (curRow == 0) return; // 跳过表头

                String[] data = rowList.toFullArray();

                // 示例校验规则
                if (data.length < 3) {
                    errors.add("第" + curRow + "行: 数据列数不足(需要至少3列)");
                    return;
                }

                // 校验第1列是否是数字
                if (data[1] != null && !data[1].matches("\\d+")) {
                    errors.add("第" + curRow + "行: 第2列必须是数字，当前值=" + data[1]);
                }

                // 校验必填项
                if (data[0] == null || data[0].trim().isEmpty()) {
                    errors.add("第" + curRow + "行: 第1列不能为空");
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
            }

            @Override
            public void endWorkBook() {
                System.out.println("\n========== 校验结果 ==========");
                if (errors.isEmpty()) {
                    System.out.println("✓ 数据校验通过!");
                } else {
                    System.out.println("✗ 发现 " + errors.size() + " 个错误:");
                    for (String err : errors) {
                        System.out.println("  - " + err);
                    }
                }
            }
        });

        handler.readSheets();
    }

    /**
     * 场景4: 分批处理大数据
     * 每读取一定行数就处理一次，避免内存溢出
     */
    @Test
    public void testBatchProcessing() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final int BATCH_SIZE = 100;
        final List<String[]> batch = new ArrayList<>();
        final int[] totalProcessed = {0};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("开始分批处理: " + name);
                batch.clear();
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                if (curRow == 0) return; // 跳过表头

                batch.add(rowList.toFullArray());

                // 达到批次大小时处理
                if (batch.size() >= BATCH_SIZE) {
                    processBatch(batch, totalProcessed[0]);
                    totalProcessed[0] += batch.size();
                    batch.clear();
                }
            }

            private void processBatch(List<String[]> batchData, int startIndex) {
                System.out.println("处理批次: " + startIndex + " ~ " + (startIndex + batchData.size()));
                // 实际应用中这里会批量写入数据库
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                // 处理剩余数据
                if (!batch.isEmpty()) {
                    processBatch(batch, totalProcessed[0]);
                    totalProcessed[0] += batch.size();
                    batch.clear();
                }
                System.out.println("处理完成, 总计: " + totalProcessed[0] + " 条");
            }

            @Override
            public void endWorkBook() {
            }
        });

        handler.readSheets();
    }

    /**
     * 场景5: 选择性读取列
     * 只读取需要的列，减少内存占用
     */
    @Test
    public void testSelectColumns() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        // 假设我们只需要第0, 2, 5列
        final int[] neededColumns = {0, 2, 5};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("选择性读取: " + name);
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                if (curRow == 0) {
                    System.out.println("表头: " + java.util.Arrays.toString(neededColumns) + " 列");
                    return;
                }

                String[] fullData = rowList.toFullArray();
                String[] selected = new String[neededColumns.length];
                for (int i = 0; i < neededColumns.length; i++) {
                    if (neededColumns[i] < fullData.length) {
                        selected[i] = fullData[neededColumns[i]];
                    }
                }
                System.out.println("行" + curRow + ": " + java.util.Arrays.toString(selected));
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
     * 场景6: 父子表数据关联
     * 主表和明细表的数据关联处理
     */
    @Test
    public void testMasterDetailRelation() throws Exception {
        URL url = SaxReaderTest4.class.getResource("/salary.xlsx");
        File file = new File(url.toURI());

        final List<Order> orders = new ArrayList<>();
        final int[] currentOrderIndex = {-1};

        ReadHandler handler = ExcelReaderFactory.create(file, new ExcelReadListener() {
            @Override
            public void startSheet(int sheetIndex, String name) {
                System.out.println("读取订单数据: " + name);
                orders.clear();
            }

            @Override
            public void optRow(int curRow, ListRow rowList) {
                String[] data = rowList.toFullArray();
                if (curRow == 0) return;

                // 假设结构: 第0列是订单号，第1列是商品
                // 当订单号变化时创建新订单
                if (data.length > 0) {
                    String orderId = data[0];
                    if (orders.isEmpty() || !orders.get(orders.size() - 1).getOrderId().equals(orderId)) {
                        Order order = new Order();
                        order.setOrderId(orderId);
                        orders.add(order);
                        currentOrderIndex[0] = orders.size() - 1;
                    }

                    if (data.length > 1 && currentOrderIndex[0] >= 0) {
                        orders.get(currentOrderIndex[0]).addItem(data[1]);
                    }
                }
            }

            @Override
            public void endSheet(int sheetIndex, String name) {
                System.out.println("共 " + orders.size() + " 个订单");
            }

            @Override
            public void endWorkBook() {
                for (Order o : orders) {
                    System.out.println(o);
                }
            }
        });

        handler.readSheets();
    }

    // ==================== 测试用类 ====================

    public static class User {
        private String id;
        private String employeeNum;
        private String name;
        private String department;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmployeeNum() { return employeeNum; }
        public void setEmployeeNum(String employeeNum) { this.employeeNum = employeeNum; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        @Override
        public String toString() {
            return "User{id=" + id + ", emp=" + employeeNum + ", name=" + name + ", dept=" + department + "}";
        }
    }

    public static class Order {
        private String orderId;
        private List<String> items = new ArrayList<>();

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public void addItem(String item) { items.add(item); }
        public List<String> getItems() { return items; }

        @Override
        public String toString() {
            return "Order{" + orderId + ", items=" + items + "}";
        }
    }
}
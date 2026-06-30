package com.bing.demo;

import com.bing.demo.converter.YesNoBooleanConverter;
import com.bing.demo.model.ConverterPerson;
import com.bing.demo.model.GlobalConverterPerson;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自定义转换器读取场景测试。
 *
 * <p>覆盖：
 * <ol>
 *   <li>属性级转换器（{@code @BingConvertor}）：字段级绑定 GenderConverter，"男/女"→1/0</li>
 *   <li>全局级转换器（{@code registerFieldConverter}）：按 {@code Boolean} 类型注册
 *       YesNoBooleanConverter，"是/否"→true/false</li>
 * </ol>
 */
class BingExcelConverterReadTest {

    private BingExcel bingExcel;
    private List<Path> tempFiles;

    @BeforeEach
    void setUp() {
        bingExcel = BingExcelBuilder.builderInstance();
        tempFiles = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        for (Path f : tempFiles) {
            try {
                Files.deleteIfExists(f);
            } catch (IOException ignored) {
            }
        }
        tempFiles.clear();
    }

    private Path newTempFile(String suffix) throws IOException {
        Path p = Files.createTempFile("bing-converter-", suffix);
        tempFiles.add(p);
        return p;
    }

    // ================================================================
    // 1. 属性级转换器：@BingConvertor 绑定到字段
    // ================================================================

    @Test
    void readByFieldConverter_annotationConvertsValue() throws Exception {
        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Gender");
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue("男");
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Bob");
            r2.createCell(1).setCellValue("女");
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<ConverterPerson> cond =
                new ReaderCondition<>(0, 1, ConverterPerson.class);
        SheetVo<ConverterPerson> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<ConverterPerson> list = result.getObjectList();
        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).getName());
        assertEquals(Integer.valueOf(1), list.get(0).getGender());
        assertEquals("Bob", list.get(1).getName());
        assertEquals(Integer.valueOf(0), list.get(1).getGender());
    }

    // ================================================================
    // 2. 全局级转换器：registerFieldConverter 按 Boolean 类型注册
    // ================================================================

    @Test
    void readByGlobalConverter_registeredTypeConverts() throws Exception {
        BingExcel custom = BingExcelBuilder.toBuilder()
                .registerFieldConverter(Boolean.class, new YesNoBooleanConverter())
                .build();

        Path file = newTempFile(".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Sheet1");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Active");
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Alice");
            r1.createCell(1).setCellValue("是");
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Bob");
            r2.createCell(1).setCellValue("否");
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                wb.write(fos);
            }
        }

        ReaderCondition<GlobalConverterPerson> cond =
                new ReaderCondition<>(0, 1, GlobalConverterPerson.class);
        SheetVo<GlobalConverterPerson> result = custom.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<GlobalConverterPerson> list = result.getObjectList();
        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).getName());
        assertEquals(Boolean.TRUE, list.get(0).getActive());
        assertEquals("Bob", list.get(1).getName());
        assertEquals(Boolean.FALSE, list.get(1).getActive());
    }
}

package com.bing.demo;

import com.bing.demo.model.Person;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BingExcel 写出（下载）场景测试。
 *
 * <p>覆盖：
 * <ol>
 *   <li>{@code writeXlsx} 写 .xlsx 文件后读回，验证数据完整</li>
 *   <li>{@code writeXls} 写 .xls 文件后读回，验证数据完整</li>
 *   <li>{@code writeXlsx} 写到 OutputStream，校验产物为合法 xlsx (zip, PK 头)</li>
 * </ol>
 */
class BingExcelWriteTest {

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
        Path p = Files.createTempFile("bing-write-", suffix);
        tempFiles.add(p);
        return p;
    }

    private List<Person> samplePersons() {
        List<Person> list = new ArrayList<>();
        list.add(new Person("Alice", 28, 8500.0, 1));
        list.add(new Person("Bob", 35, 12000.0, 0));
        list.add(new Person("Carol", 22, 5000.0, 1));
        return list;
    }

    @Test
    void writeXlsx_thenReadBack_roundtrip() throws Exception {
        Path file = newTempFile(".xlsx");
        bingExcel.writeXlsx(file.toFile(), samplePersons());

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<Person> list = result.getObjectList();
        assertEquals(3, list.size());
        assertEquals("Alice", list.get(0).getName());
        assertEquals(Integer.valueOf(28), list.get(0).getAge());
        assertEquals(Double.valueOf(8500.0), list.get(0).getSalary());
        assertEquals("Carol", list.get(2).getName());
    }

    @Test
    void writeXls_thenReadBack_roundtrip() throws Exception {
        Path file = newTempFile(".xls");
        bingExcel.writeXls(file.toFile(), samplePersons());

        ReaderCondition<Person> cond = new ReaderCondition<>(0, 1, Person.class);
        SheetVo<Person> result = bingExcel.readFile(file.toFile(), cond);

        assertNotNull(result);
        List<Person> list = result.getObjectList();
        assertEquals(3, list.size());
        assertEquals("Alice", list.get(0).getName());
        assertEquals(Integer.valueOf(35), list.get(1).getAge());
        assertEquals(Double.valueOf(5000.0), list.get(2).getSalary());
    }

    @Test
    void writeXlsx_toOutputStream_producesValidZip() throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bingExcel.writeXlsx(bos, samplePersons());
            byte[] bytes = bos.toByteArray();
            assertTrue(bytes.length > 0);
            // xlsx 是 zip 容器，魔数 "PK" (0x50 0x4B)
            assertEquals('P', (char) (bytes[0] & 0xFF));
            assertEquals('K', (char) (bytes[1] & 0xFF));
        }
    }
}

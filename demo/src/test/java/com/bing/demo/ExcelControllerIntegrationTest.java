package com.bing.demo;

import com.bing.demo.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelController 三端点 HTTP 集成测试。
 *
 * <p>使用真实 Spring Boot 上下文 + RANDOM_PORT，验证端点层组装（状态码、
 * Content-Type、Content-Disposition 文件名、下载产物是否合法 xlsx），
 * 防止 {@code writeXlsx(OutputStream)} 这类底层缺陷悄悄回归到 HTTP 层。
 *
 * <p>覆盖：
 * <ol>
 *   <li>{@code GET /api/excel/download-sample} — 下载 sample.xlsx</li>
 *   <li>{@code POST /api/excel/read} — 上传 xlsx，返回 Person JSON</li>
 *   <li>{@code POST /api/excel/write} — 提交 JSON，下载 xlsx</li>
 *   <li>端到端闭环：write 中文 → read 回，验证 UTF-8 中文不丢失</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExcelControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    /** 校验给定字节数组为合法 xlsx（ZIP 容器，魔数 "PK" = 0x50 0x4B）。 */
    private static void assertValidXlsx(byte[] bytes) {
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertEquals(0x50, bytes[0] & 0xFF, "xlsx 应以 ZIP 魔数 'P' (0x50) 开头");
        assertEquals(0x4B, bytes[1] & 0xFF, "xlsx 应以 ZIP 魔数 'K' (0x4B) 开头");
    }

    // ================================================================
    // 1. GET /api/excel/download-sample
    // ================================================================

    @Test
    void downloadSample_returnsXlsx() {
        ResponseEntity<byte[]> resp = restTemplate.getForEntity(
                url("/api/excel/download-sample"), byte[].class);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(MediaType.valueOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                resp.getHeaders().getContentType());

        // 文件名应为 person_sample.xlsx
        String disposition = resp.getHeaders().getFirst("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("person_sample.xlsx"),
                "Content-Disposition 应含文件名 person_sample.xlsx: " + disposition);

        assertValidXlsx(resp.getBody());
    }

    // ================================================================
    // 2. POST /api/excel/read — 先下载 sample 再回传解析
    // ================================================================

    @Test
    void read_parsesUploadedXlsxToPersons() {
        byte[] sample = restTemplate.getForObject(
                url("/api/excel/download-sample"), byte[].class);
        assertValidXlsx(sample);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(sample) {
            @Override
            public String getFilename() {
                return "sample.xlsx";
            }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Person[]> resp = restTemplate.postForEntity(
                url("/api/excel/read"), new HttpEntity<>(body, headers), Person[].class);

        assertEquals(200, resp.getStatusCode().value());
        Person[] persons = resp.getBody();
        assertNotNull(persons);
        assertEquals(3, persons.length, "sample 应含 3 个 Person");
        assertEquals("Alice", persons[0].getName());
        assertEquals(Integer.valueOf(28), persons[0].getAge());
        assertEquals(Double.valueOf(8500.0), persons[0].getSalary());
        assertEquals("Charlie", persons[2].getName());
    }

    // ================================================================
    // 3. POST /api/excel/write — 提交 JSON，下载 xlsx
    //    顺带用中文 Person，补全 curl 在 Windows GBK 下无法验证的 UTF-8 闭环
    // ================================================================

    @Test
    void write_returnsXlsxAndRoundtripPreservesChinese() {
        // JVM 内部构造 JSON body，由 Jackson 按 UTF-8 序列化，无 curl GBK 编码问题
        List<Person> input = List.of(
                new Person("张三", 25, 10000.0, 1),
                new Person("李四", 30, 15000.0, 0));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<byte[]> resp = restTemplate.exchange(
                url("/api/excel/write"), HttpMethod.POST,
                new HttpEntity<>(input, headers), byte[].class);

        assertEquals(200, resp.getStatusCode().value());
        assertEquals(MediaType.valueOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                resp.getHeaders().getContentType());
        String disposition = resp.getHeaders().getFirst("Content-Disposition");
        assertNotNull(disposition);
        assertTrue(disposition.contains("person_export.xlsx"),
                "Content-Disposition 应含文件名 person_export.xlsx: " + disposition);

        byte[] xlsx = resp.getBody();
        assertValidXlsx(xlsx);

        // 闭环：把写出的 xlsx 回传给 /read，验证中文不丢失
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(xlsx) {
            @Override
            public String getFilename() {
                return "export.xlsx";
            }
        });
        HttpHeaders readHeaders = new HttpHeaders();
        readHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        ResponseEntity<Person[]> readResp = restTemplate.postForEntity(
                url("/api/excel/read"), new HttpEntity<>(body, readHeaders), Person[].class);

        assertEquals(200, readResp.getStatusCode().value());
        Person[] persons = readResp.getBody();
        assertNotNull(persons);
        assertEquals(2, persons.length);
        assertEquals("张三", persons[0].getName(), "中文姓名经写出→读回应保持不变");
        assertEquals(Integer.valueOf(25), persons[0].getAge());
        assertEquals("李四", persons[1].getName());
    }
}

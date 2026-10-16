package com.bing.demo.controller;

import com.bing.demo.model.Person;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    private static final Logger log = LoggerFactory.getLogger(ExcelController.class);

    private final BingExcel bingExcel;

    public ExcelController(BingExcel bingExcel) {
        this.bingExcel = bingExcel;
    }

    /**
     * 上传 Excel 文件，返回解析后的 Person 列表。
     */
    @PostMapping("/read")
    public ResponseEntity<?> readExcel(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("file is empty");
        }
        String original = file.getOriginalFilename();
        String suffix = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : ".xlsx";
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("upload_", suffix);
            file.transferTo(tempFile.toFile());

            ReaderCondition<Person> condition = new ReaderCondition<>(0, 1, Person.class);
            SheetVo<Person> result = bingExcel.readFile(tempFile.toFile(), condition);
            List<Person> persons = result != null ? result.getObjectList() : List.of();
            return ResponseEntity.ok(persons);
        } catch (Exception e) {
            log.error("read excel failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("read failed: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 下载示例 Person Excel 文件。
     */
    @GetMapping("/download-sample")
    public void downloadSample(HttpServletResponse response) throws IOException {
        writePersonsToResponse(response, "person_sample.xlsx", createSampleData());
    }

    /**
     * 提交 Person 列表 JSON，导出为 Excel 下载。
     */
    @PostMapping("/write")
    public void writeExcel(@RequestBody List<Person> persons, HttpServletResponse response)
            throws IOException {
        if (persons == null) {
            persons = List.of();
        }
        writePersonsToResponse(response, "person_export.xlsx", persons);
    }

    /**
     * 统一导出逻辑。异常不在本方法 catch，由 {@link com.bing.demo.exception.GlobalExceptionHandler}
     * 统一处理：marshaling 类异常全部发生在 wb.write 落盘之前，响应尚未提交，
     * advice 会 reset 后返回 500，客户端不会拿到 200 + 截断的 xlsx；
     * 若失败时响应已提交（如客户端断连），advice 只记录日志。
     *
     * <p>注意不要在 finally 里 close 输出流：ServletOutputStream.close() 会提交响应，
     * 会使 advice 的 isCommitted() 判断失效（reset 永远走不到）。请求结束时容器负责关闭流。
     */
    private void writePersonsToResponse(HttpServletResponse response, String fileName,
            List<Person> persons) throws IOException {
        setExcelResponseHeaders(response, fileName);
        bingExcel.writeXlsx(response.getOutputStream(), persons);
    }

    private void setExcelResponseHeaders(HttpServletResponse response, String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
    }

    private List<Person> createSampleData() {
        List<Person> list = new ArrayList<>();
        list.add(new Person("Alice", 28, 8500.0, 0));
        list.add(new Person("Bob", 35, 12000.0, 1));
        list.add(new Person("Charlie", 42, 15000.0, 1));
        return list;
    }
}

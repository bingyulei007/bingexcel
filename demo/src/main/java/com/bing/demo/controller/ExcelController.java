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
import java.io.OutputStream;
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
        List<Person> persons = createSampleData();

        setExcelResponseHeaders(response, "person_sample.xlsx");

        try (OutputStream os = response.getOutputStream()) {
            bingExcel.writeXlsx(os, persons);
        }
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
        setExcelResponseHeaders(response, "person_export.xlsx");

        try (OutputStream os = response.getOutputStream()) {
            bingExcel.writeXlsx(os, persons);
        }
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

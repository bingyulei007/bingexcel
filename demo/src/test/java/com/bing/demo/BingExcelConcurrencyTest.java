package com.bing.demo;

import com.bing.demo.model.Person;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.ReaderCondition;
import com.bing.excel.core.impl.BingExcelImpl.SheetVo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 直接并发压测 BingExcel 单例，验证线程安全。
 *
 * <p>不经过 HTTP/Spring，直接调用 BingExcel API，用多线程同时读+写同一个实体类，
 * 重点压测 {@code FieldConverterMapper} 懒初始化、{@code typeTokenCache} 注册、
 * {@code AnnotationMapperHandler.processEntity} 的并发行为。
 */
class BingExcelConcurrencyTest {

    private static BingExcel bingExcel;
    private static Path sampleFile;

    private static final int THREADS = 16;
    private static final int ITERATIONS_PER_THREAD = 50;

    @BeforeAll
    static void setUp() throws Exception {
        bingExcel = BingExcelBuilder.builderInstance();

        // 准备一份示例 xlsx，供并发读取
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            persons.add(new Person("name-" + i, 20 + i, 1000.0 + i, i % 2));
        }
        sampleFile = Files.createTempFile("stress-sample-", ".xlsx");
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            bingExcel.writeXlsx(bos, persons);
            Files.write(sampleFile, bos.toByteArray());
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (sampleFile != null) {
            Files.deleteIfExists(sampleFile);
        }
    }

    @Test
    void concurrentReadAndWrite_noException() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger readSuccess = new AtomicInteger();
        AtomicInteger writeSuccess = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            final int taskId = i;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        if ((taskId + j) % 2 == 0) {
                            // 并发读
                            ReaderCondition<Person> cond =
                                    new ReaderCondition<>(0, 1, Person.class);
                            SheetVo<Person> vo = bingExcel.readFile(sampleFile.toFile(), cond);
                            List<Person> list = vo != null ? vo.getObjectList() : List.of();
                            if (list.size() == 100) {
                                readSuccess.incrementAndGet();
                            }
                        } else {
                            // 并发写
                            List<Person> data = new ArrayList<>();
                            for (int k = 0; k < 10; k++) {
                                data.add(new Person(
                                        "t" + taskId + "-" + j + "-" + k,
                                        20 + k, 2000.0 + k, k % 2));
                            }
                            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                                bingExcel.writeXlsx(bos, data);
                                if (bos.size() > 0) {
                                    writeSuccess.incrementAndGet();
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    errors.incrementAndGet();
                    t.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        long startNs = System.nanoTime();
        start.countDown();
        boolean finished = done.await(120, TimeUnit.SECONDS);
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;

        pool.shutdown();

        System.out.println("=== concurrentReadAndWrite_noException ===");
        System.out.println("Threads            : " + THREADS);
        System.out.println("Iterations/thread  : " + ITERATIONS_PER_THREAD);
        System.out.println("Total ops          : " + (THREADS * ITERATIONS_PER_THREAD));
        System.out.println("Read success       : " + readSuccess.get());
        System.out.println("Write success      : " + writeSuccess.get());
        System.out.println("Errors             : " + errors.get());
        System.out.println("Elapsed            : " + elapsedMs + " ms");

        assertTrue(finished, "timeout: not all threads finished");
        assertEquals(0, errors.get(), "errors occurred during concurrent access");
        assertTrue(readSuccess.get() > 0, "no successful reads");
        assertTrue(writeSuccess.get() > 0, "no successful writes");
    }

    @Test
    void concurrentWriteToDifferentFiles_noException() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger errors = new AtomicInteger();
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            final int taskId = i;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        List<Person> data = new ArrayList<>();
                        for (int k = 0; k < 50; k++) {
                            data.add(new Person(
                                    "w" + taskId + "-" + j + "-" + k,
                                    25 + k, 3000.0 + k, k % 2));
                        }
                        Path out = Files.createTempFile("stress-out-", ".xlsx");
                        try {
                            bingExcel.writeXlsx(out.toFile(), data);
                            if (Files.size(out) > 0) {
                                success.incrementAndGet();
                            }
                        } finally {
                            Files.deleteIfExists(out);
                        }
                    }
                } catch (Throwable t) {
                    errors.incrementAndGet();
                    t.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
        }

        long startNs = System.nanoTime();
        start.countDown();
        boolean finished = done.await(180, TimeUnit.SECONDS);
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;

        pool.shutdown();

        System.out.println("=== concurrentWriteToDifferentFiles_noException ===");
        System.out.println("Threads            : " + THREADS);
        System.out.println("Iterations/thread  : " + ITERATIONS_PER_THREAD);
        System.out.println("Write success      : " + success.get());
        System.out.println("Errors             : " + errors.get());
        System.out.println("Elapsed            : " + elapsedMs + " ms");

        assertTrue(finished, "timeout: not all threads finished");
        assertEquals(0, errors.get(), "errors occurred during concurrent writes");
        assertEquals(THREADS * ITERATIONS_PER_THREAD, success.get());
    }
}

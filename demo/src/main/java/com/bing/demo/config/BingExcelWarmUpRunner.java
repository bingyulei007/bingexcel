package com.bing.demo.config;

import com.bing.demo.model.Person;
import com.bing.excel.core.BingExcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 启动时对 BingExcel 做预热：写入一份示例数据，触发实体类注解解析、
 * TypeAdapterConverter 注册、FieldConverterMapper 的 setFieldConverter 懒加载。
 * 让运行时只剩只读访问，规避多并发下的可见性瑕疵。
 */
@Configuration
public class BingExcelWarmUpRunner {

    private static final Logger log = LoggerFactory.getLogger(BingExcelWarmUpRunner.class);

    @Bean
    public CommandLineRunner warmUpBingExcel(BingExcel bingExcel) {
        return args -> {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                bingExcel.writeXlsx(bos, List.of(new Person("__warmup__", 0, 0.0, 0)));
                log.info("BingExcel warmed up: Person mapper initialized");
            } catch (Exception e) {
                log.warn("BingExcel warm-up failed", e);
            }
        };
    }
}

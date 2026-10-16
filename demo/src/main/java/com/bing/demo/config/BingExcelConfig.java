package com.bing.demo.config;

import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BingExcel 单例配置。
 *
 * <p>BingExcel 单例可安全并发使用：内部共享状态全部由并发容器或同步原语保护
 * （{@code typeTokenCache} 为 ConcurrentHashMap、{@code userDefineMapperHandler}
 * 为 volatile + DCL、{@code annotatedTypes} 为 synchronizedSet，
 * ConversionMapper 的字段同样是 ConcurrentHashMap）；默认转换器查找在
 * marshal/unmarshal 时按需解析，不回写共享状态。无需启动预热。
 */
@Configuration
public class BingExcelConfig {

    @Bean
    public BingExcel bingExcel() {
        return BingExcelBuilder.builderInstance();
    }
}

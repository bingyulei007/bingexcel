package com.bing.demo.config;

import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BingExcel 单例配置。
 *
 * <p>BingExcel 内部使用 ConcurrentHashMap / volatile / synchronized 保护共享状态，
 * 基本线程安全。唯一瑕疵：{@code FieldConverterMapper.setFieldConverter(...)} 在首次
 * marshal/unmarshal 时被懒加载调用，缺乏 happens-before 保证。这里通过启动时预热
 * （见 {@link BingExcelWarmUpRunner}）让所有 lazy init 在容器启动阶段完成，
 * 运行时只剩只读访问，规避该瑕疵。
 */
@Configuration
public class BingExcelConfig {

    @Bean
    public BingExcel bingExcel() {
        return BingExcelBuilder.builderInstance();
    }
}

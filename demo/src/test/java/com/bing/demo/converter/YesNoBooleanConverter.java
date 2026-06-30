package com.bing.demo.converter;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;

import java.lang.reflect.Type;

/**
 * 全局级自定义转换器示例：Excel 中以中文"是/否"存储，Java 侧映射为 {@code Boolean}。
 *
 * <p>通过 {@code BingExcelBuilder.registerFieldConverter(Boolean.class, new YesNoBooleanConverter())}
 * 全局注册，作用于该 BingExcel 实例下所有 {@code Boolean} 类型字段（覆盖默认 BooleanFieldConverter）。
 * {@code canConvert} 必须对注册类型返回 true，否则注册时抛 {@code ConversionException}。</p>
 */
public class YesNoBooleanConverter extends AbstractFieldConvertor {

    @Override
    public boolean canConvert(Class<?> clz) {
        return Boolean.class.equals(clz) || boolean.class.equals(clz);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
        if (cell == null) {
            return null;
        }
        return "是".equals(cell.trim());
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
        if (source == null) {
            return null;
        }
        boolean v = Boolean.TRUE.equals(source);
        return new OutValue(OutValue.OutType.STRING, v ? "是" : "否");
    }
}

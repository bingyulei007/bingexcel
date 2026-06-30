package com.bing.demo.converter;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;

import java.lang.reflect.Type;

/**
 * 属性级自定义转换器示例：Excel 中以中文"男/女"存储，Java 侧映射为 {@code Integer} (1/0)。
 *
 * <p>通过 {@code @BingConvertor(GenderConverter.class)} 绑定到字段，仅作用于该字段。
 * 必须提供无参构造（{@code @BingConvertor} 通过反射实例化）。</p>
 */
public class GenderConverter extends AbstractFieldConvertor {

    @Override
    public boolean canConvert(Class<?> clz) {
        return Integer.class.equals(clz) || int.class.equals(clz);
    }

    @Override
    public Object fromString(String cell, ConverterHandler converterHandler, Type targetType) {
        if (cell == null) {
            return null;
        }
        String v = cell.trim();
        if ("男".equals(v)) {
            return 1;
        }
        if ("女".equals(v)) {
            return 0;
        }
        return null;
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
        if (source == null) {
            return null;
        }
        int v = ((Number) source).intValue();
        return new OutValue(OutValue.OutType.STRING, v == 1 ? "男" : "女");
    }
}

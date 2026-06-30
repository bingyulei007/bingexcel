package com.bing.demo.model;

import com.bing.excel.annotation.CellConfig;

/**
 * 全局自定义转换器演示实体：{@code active} 为 {@code Boolean} 包装类型。
 *
 * <p>当通过 {@code BingExcelBuilder.registerFieldConverter(Boolean.class, YesNoBooleanConverter)}
 * 全局注册后，Excel 中存"是/否"，读取后映射为 {@code Boolean}。
 * 使用包装类型而非 {@code boolean}，方能命中按 {@code Boolean.class} 注册的全局转换器。</p>
 */
public class GlobalConverterPerson {

    @CellConfig(index = 0, aliasName = "Name")
    private String name;

    @CellConfig(index = 1, aliasName = "Active")
    private Boolean active;

    public GlobalConverterPerson() {
    }

    public GlobalConverterPerson(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "GlobalConverterPerson{name='" + name + "', active=" + active + '}';
    }
}

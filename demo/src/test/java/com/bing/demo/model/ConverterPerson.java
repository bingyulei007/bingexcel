package com.bing.demo.model;

import com.bing.demo.converter.GenderConverter;
import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;

/**
 * 属性级自定义转换器演示实体：{@code gender} 字段通过 {@code @BingConvertor} 绑定
 * {@link GenderConverter}，Excel 中存"男/女"，读取后映射为 {@code Integer} 1/0。
 */
public class ConverterPerson {

    @CellConfig(index = 0, aliasName = "Name")
    private String name;

    @CellConfig(index = 1, aliasName = "Gender")
    @BingConvertor(GenderConverter.class)
    private Integer gender;

    public ConverterPerson() {
    }

    public ConverterPerson(String name, Integer gender) {
        this.name = name;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "ConverterPerson{name='" + name + "', gender=" + gender + '}';
    }
}

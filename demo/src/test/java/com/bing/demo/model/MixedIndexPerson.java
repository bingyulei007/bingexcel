package com.bing.demo.model;

import com.bing.excel.annotation.CellConfig;

/**
 * 混合索引 + aliasName 字段的实体，验证两种方式在同一实体中共存。
 */
public class MixedIndexPerson {

    @CellConfig(index = 0, aliasName = "ID")
    private Integer id;

    @CellConfig(index = 1, aliasName = "Name")
    private String name;

    @CellConfig(index = -1, aliasName = "Department")
    private String department;

    @CellConfig(index = -1, aliasName = "Level")
    private Integer level;

    public MixedIndexPerson() {}

    public MixedIndexPerson(Integer id, String name, String department, Integer level) {
        this.id = id; this.name = name; this.department = department; this.level = level;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    @Override
    public String toString() {
        return "MixedIndexPerson{id=" + id + ", name='" + name + "', department='" + department + "', level=" + level + "}";
    }
}

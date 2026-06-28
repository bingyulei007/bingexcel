package com.bing.demo.model;

import com.bing.excel.annotation.CellConfig;

/**
 * 包含 readRequired 字段的实体，验证缺失必填列时的异常。
 */
public class RequiredPerson {

    @CellConfig(index = 0, aliasName = "ID")
    private Integer id;

    @CellConfig(index = 1, aliasName = "Name", readRequired = true)
    private String name;

    @CellConfig(index = 2, aliasName = "Email")
    private String email;

    public RequiredPerson() {}

    public RequiredPerson(Integer id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "RequiredPerson{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}

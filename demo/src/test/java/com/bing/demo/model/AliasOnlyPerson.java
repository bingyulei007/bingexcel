package com.bing.demo.model;

import com.bing.excel.annotation.CellConfig;

/**
 * 仅使用 aliasName (index=-1) 的实体，完全依赖表头匹配列。
 */
public class AliasOnlyPerson {

    @CellConfig(index = -1, aliasName = "Name")
    private String name;

    @CellConfig(index = -1, aliasName = "Age")
    private Integer age;

    @CellConfig(index = -1, aliasName = "Score")
    private Double score;

    @CellConfig(index = -1, aliasName = "Email")
    private String email;

    public AliasOnlyPerson() {}

    public AliasOnlyPerson(String name, Integer age, Double score, String email) {
        this.name = name; this.age = age; this.score = score; this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "AliasOnlyPerson{name='" + name + "', age=" + age + ", score=" + score + ", email='" + email + "'}";
    }
}

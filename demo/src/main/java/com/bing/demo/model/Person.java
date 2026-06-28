package com.bing.demo.model;

import com.bing.excel.annotation.CellConfig;

/**
 * Demo entity mapped to Excel columns via {@code @CellConfig}.
 * <p>
 * Column A (index=0, aliasName="Name"): name<br>
 * Column B (index=1, aliasName="Age"): age<br>
 * Column C (index=2, aliasName="Salary"): salary<br>
 * Column D (index=3, aliasName="Gender"): gender (0=female, 1=male)
 * </p>
 */
public class Person {

    @CellConfig(index = 0, aliasName = "Name")
    private String name;

    @CellConfig(index = 1, aliasName = "Age")
    private Integer age;

    @CellConfig(index = 2, aliasName = "Salary")
    private Double salary;

    @CellConfig(index = 3, aliasName = "Gender")
    private Integer gender;

    public Person() {
    }

    public Person(String name, Integer age, Double salary, Integer gender) {
        this.name = name;
        this.age = age;
        this.salary = salary;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + ", salary=" + salary + ", gender=" + gender + '}';
    }
}

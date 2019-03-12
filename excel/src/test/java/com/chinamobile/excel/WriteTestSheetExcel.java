package com.chinamobile.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.impl.BingExcelImpl.SheetExcel;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * @author liluzhong
 * @date 2019/03/07
 *
 */
public class WriteTestSheetExcel {
  BingExcel bing;

  @Before
  public void before() {
    bing = BingExcelBuilder.toBuilder().builder();
  }

  @Test
  public void testWrite() throws IOException {
    List<SheetExcel> sheetList = new ArrayList<>();
    SheetExcel sheetExcels1 = new SheetExcel();

    // 造数据 List<Person>
    SheetExcel seExcel1 = new SheetExcel();
    List<Person> list = Lists.newArrayList();
    list.add(new Person(23, RandomStringUtils.randomAlphanumeric(4), Math.random() * 1000));
    list.add(new Person(24, RandomStringUtils.randomAlphanumeric(4), Math.random() * 1000));
    list.add(new Person(25, RandomStringUtils.randomAlphanumeric(4), Math.random() * 1000));
    seExcel1.setSheetName("org");
    seExcel1.setList(list);

    SheetExcel seExcel2 = new SheetExcel(); // 造数据 List<Student>
    List<Student> list1 = Lists.newArrayList();

    list1.add(new Student(RandomStringUtils.randomAlphanumeric(4),
        RandomStringUtils.randomAlphanumeric(4), RandomStringUtils.randomAlphanumeric(4)));
    list1.add(new Student(RandomStringUtils.randomAlphanumeric(4),
        RandomStringUtils.randomAlphanumeric(4), RandomStringUtils.randomAlphanumeric(4)));
    list1.add(new Student(RandomStringUtils.randomAlphanumeric(4),
        RandomStringUtils.randomAlphanumeric(4), RandomStringUtils.randomAlphanumeric(4)));

     seExcel2.setSheetName("business");
    seExcel2.setList(list1);


   // bing.writeSheetsExcel("C:\\Users\\shi\\workspace/sheets.xlsx", seExcel1, seExcel2);

     bing.writeSheetsExcel("/Users/shi/workspace/aa/adb.xlsx", seExcel2,seExcel1);
    // bing.writeCSV("/Users/shi/workspace/aa/adb.csv", list);
  }

  @OutAlias("xiaoshou")
  public static class Person {

    public Person(int age, String name, Double salary) {
      super();
      this.age = age;
      this.name = name;
      this.salary = salary;
    }

    public Person() {
      super();
    }


    @CellConfig(index = 0)
    private String name;
    @CellConfig(index = 1, aliasName = "年龄")
    private int age;
    @CellConfig(index = 2)
    private Double salary;

    public Person getFriends() {
      return friends;
    }

    public void setFriends(Person friends) {
      this.friends = friends;
    }

    private Person friends;

    private transient boolean testProperty = false;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAge() {
      return age;
    }

    public Double getSalary() {
      return salary;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this.getClass()).omitNullValues().add("name", name)
          .add("age", age).add("salary", salary).toString();
    }
  }
  @OutAlias("测试名字")
  public static class Student {
    @CellConfig(index = 0)
    private String schoolName;
    @CellConfig(index = 1)
    private String className;
    @CellConfig(index = 2)
    private String name;

    public Student() {}

    public Student(String schoolName, String className, String name) {
      this.schoolName = schoolName;
      this.className = className;
      this.name = name;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues().add("schoolName", schoolName)
          .add("className", className).add("name", name).toString();
    }
  }

}

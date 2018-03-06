package com.chinamobile.excel;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import com.bing.excel.vo.OutValue.OutType;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * @author shizhongtao
 */
public class WriteTest6 {
  BingExcel bing;

  @Before
  public void before() {
    bing = BingExcelBuilder.toBuilder().addClassNameAlias(Person.class,"xuesheng1")
        .addFieldConversionMapper(Person.class,"name",0)
        .addFieldConversionMapper(Person.class,"age",1,"年龄").build();
  }

  @Test
  public void testWrite() throws IOException {
    List<Person>  list= Lists.newArrayList();

    Person person = new Person(23, "wori",2.0);
    Person person2 = new Person(20, "lily",13.5);
    list.add(person);
    list.add(person2);
    list.add(person);

    //bing.writeExcel("/Users/shi/workspace/gaoxinqu/student.xlsx",list);
    bing.writeCSV("/Users/shi/workspace/gaoxinqu/student.csv",list);


  }

  @OutAlias("xiaoshou1")
  public static class Person {

    private int age;
    private String name;
    @CellConfig(index = 2,aliasName = "工资")
    private Double salary;
    private List<Student>  friends;

    public List<Student> getFriends() {
      return friends;
    }

    public void setFriends(List<Student> friends) {
      this.friends = friends;
    }

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
    public Person(int age, String name, Double salary) {
      super();
      this.age = age;
      this.name = name;
      this.salary = salary;
    }
    public Person() {
      super();
    }
    public String toString() {
      return MoreObjects.toStringHelper(this.getClass()).omitNullValues()
          .add("name", name).add("age", age).add("salary", salary)
          .toString();
    }
  }

  public static class Student {
    @CellConfig(index = 0)
    private String schoolName;
    @CellConfig(index = 1)
    private String className;
    @CellConfig(index = 2)
    private String name;

    public Student() {
    }

    public Student(String schoolName, String className, String name) {
      this.schoolName = schoolName;
      this.className = className;
      this.name = name;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).omitNullValues()
          .add("schoolName", schoolName)
          .add("className", className)
          .add("name", name)
          .toString();
    }
  }


  public static class MyListConverter extends AbstractFieldConvertor {

    @Override
    public boolean canConvert(Class<?> clz) {

      return List.class.isAssignableFrom(clz);
    }

    @Override
    public OutValue toObject(Object source, ConverterHandler converterHandler) {
      if (source==null) {
        return null;
      }

        return new OutValue(OutType.STRING,source.toString());

    }

  }
}

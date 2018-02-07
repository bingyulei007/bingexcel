package com.chinamobile.excel;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.vo.OutValue;
import com.bing.excel.vo.OutValue.OutType;
import com.bing.utils.StringParseUtil;
import com.chinamobile.excel.WriteTest2.Person;
import com.google.common.base.MoreObjects;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.core.BingExcelEvent;
import com.bing.excel.core.BingExcelEventBuilder;
import com.bing.excel.core.rw.BingWriterHandler;

import com.google.common.collect.Lists;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @author shizhongtao
 */
public class WriteTest5 {
  BingExcel bing;

  @Before
  public void before() {
    bing = BingExcelBuilder.builderInstance();
  }

  @Test
  public void testWrite() {
    List<Person>  list= Lists.newArrayList();
    List<Student>  listFriend= Lists.newArrayList();

    Person person = new Person(23, "wori",3.45);
    Student obj1 = new Student("高中", "friends1","3.45");
    Student obj2 = new Student("初中", "friends2","3.45");
    listFriend.add(obj1);
    listFriend.add(obj2);
    person.setFriends(listFriend);
    list.add(person);
    bing.writeExcel("/Users/shi/workspace/student.xlsx",list);


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

    @CellConfig(index = 1, aliasName = "年龄")
    private int age;
    @CellConfig(index = 0)
    private String name;
    @CellConfig(index = 3)
    private Double salary;
    @CellConfig(index = 2)
    @BingConvertor(MyListConverter.class)
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

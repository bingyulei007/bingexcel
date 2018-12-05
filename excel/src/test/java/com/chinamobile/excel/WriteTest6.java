package com.chinamobile.excel;

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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

  @Test
  public void csvWrite_semiColon_noHead() throws IOException {
    BingExcel bingExcel = BingExcelBuilder.toBuilder().build();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    List<ApiUsedNumForCSV>  list= Lists.newArrayList();
    ApiUsedNumForCSV api1 = new ApiUsedNumForCSV();
    api1.setDate("2018-10");
    api1.setOwnerId("5632643255427352877312350");
    api1.setTopicName("test1");
    api1.setUsedNum(100000L);
    ApiUsedNumForCSV api2 = new ApiUsedNumForCSV();
    api2.setDate("2018-10");
    api2.setOwnerId("5632643255427352877312350");
    api2.setTopicName("test2");
    api2.setUsedNum(200000L);
    ApiUsedNumForCSV api3 = new ApiUsedNumForCSV();
    api3.setDate("2018-10");
    api3.setOwnerId("5632643255427352877312350");
    api3.setTopicName("test3");
    api3.setUsedNum(300000L);
    list.add(api1);
    list.add(api2);
    list.add(api3);

    bingExcel.writeCSV(os, list, ';', false, false);
    String result = new String(os.toByteArray());
    System.out.println(result);

    File file = new File("E:\\test.csv");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    fileOutputStream.write(os.toByteArray());
    fileOutputStream.close();
  }

  /**
   * Model类。生成CSV文件用。表示每月的api调用次数计量信息。
   *
   * @author chengxiangwang
   * @create 2018/10/30
   */
  static class ApiUsedNumForCSV{
    //Format: 2018-10
    @CellConfig(index = 0)
    private String month;
    @CellConfig(index = 1)
    private String topicName;
    @CellConfig(index = 2)
    private String ownerId;
    @CellConfig(index = 3)
    private Long apiUsedNum;

    public ApiUsedNumForCSV() {

    }
    public String getTopicName() {
      return topicName;
    }

    public void setTopicName(String topicName) {
      this.topicName = topicName;
    }

    public String getOwnerId() {
      return ownerId;
    }

    public void setOwnerId(String ownerId) {
      this.ownerId = ownerId;
    }

    public String getDate() {
      return month;
    }

    public void setDate(String month) {
      this.month = month;
    }

    public Long getUsedNum() {
      return apiUsedNum;
    }

    public void setUsedNum(Long apiUsedNum) {
      this.apiUsedNum = apiUsedNum;
    }
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

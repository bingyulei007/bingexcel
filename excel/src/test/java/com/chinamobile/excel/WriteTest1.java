package com.chinamobile.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.converter.base.BooleanFieldConverter;
import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class WriteTest1 {
	BingExcel	 bing;
	@Before
	public void before(){
		bing = BingExcelBuilder.toBuilder().builder();
	}
	@Test
	public void testWrite() {
		List<Person> list = Lists.newArrayList();
		list.add(new Person(12, "nihoa", 23434.9));
		list.add(new Person(23, "nihoa", 234.9));
		list.add(new Person(122, "nihoa", 23434.9));

		bing.writeExcel("D:/aoptest/adb.xlsx", list);
	/*	try (FileOutputStream os = new FileOutputStream("D:/aoptest/adb1.csv")){
		//bing.writeCSV("D:/aoptest/adb.csv",list);
			bing.writeCSV(os,list);
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		//bing.writeExcel("D:/aoptest/adb.xlsx", list,list,list);
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
		@CellConfig(index = 2,readRequired = true,aliasName = "玩玩")
		@BingConvertor(value = BooleanFieldConverter.class, strings = { "1","0" }, booleans = { true })
		private  boolean testProperty = false;

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
			return MoreObjects.toStringHelper(this.getClass()).omitNullValues().add("name", name).add("age", age)
					.add("salary", salary).toString();
		}
	}
}

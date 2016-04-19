package com.bing.excel;

import java.util.List;

import org.junit.Test;

import com.bing.excel.annotation.CellConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

public class WriteTest1 {
	
	@Test
	public void testWrite(){
		List<Person> list=Lists.newArrayList();
	}
	
	
	public static class Person {
		@CellConfig(index = 1)
		private int age;
		@CellConfig(index = 0)
		private String name;
		@CellConfig(index = 3)
		private Double salary;

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
}

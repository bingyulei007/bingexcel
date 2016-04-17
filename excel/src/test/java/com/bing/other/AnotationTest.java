package com.bing.other;


import java.lang.reflect.Field;

import org.junit.Test;

import com.bing.excel.annotation.OutAlias;
import com.bing.excel.vo.OutValue.OutType;

public class AnotationTest {

	@Test
	public void testOut() throws  IllegalAccessException{
		System.out.println(OutType.INTEGER);
		System.out.println(OutType.INTEGER.toString());
	}
}
@OutAlias("nihao")
class Person{
	private String name;
	private Integer age;
}
package com.bing.excel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.google.gson.Gson;

public class GsonTEst {
	@Test
	public void testMashall() {
		Gson g = new Gson();
		Person p = new Person("hello", 123);
		System.out.println(g.toJson(p));
	}
	@Test
	public void testUnmashall(){
		String p="{\"name\":\"hello\",\"age\":123}";
		Gson g=new Gson();
		Person fromJson = g.fromJson(p, Person.class);
		System.out.println(fromJson);
	}
	@Test
	public void tesP() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Constructor<Person> constructor = Person.class.getDeclaredConstructor(String.class);
		Person instance = constructor.newInstance("limin");
		System.out.println(constructor.getTypeParameters());
	}
}

class Person {

	public Person() {
		super();
	}
	public Person(String name) {
		this.name=name;
	}

	public Person(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

	private String name;
	private int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

}
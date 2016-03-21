package com.bing.excel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.google.gson.Gson;

public class GsonTEst {
	@Test
	public void testMashall() throws IllegalArgumentException, IllegalAccessException {
		Person p=new Person();
		Field[] fields = Person.class.getDeclaredFields();
		for (Field field : fields) {
		if(field.getType().equals(int.class)){
			
			field.setAccessible(true);
			field.set(p, null);
		}
		}
		System.out.println(p.getAge());
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
		System.out.println(Person.class.equals(constructor.getDeclaringClass()));
		System.out.println(constructor.getTypeParameters());
	}
	
	@Test
	public void testL(){
		Long decode = Long.decode("012");
		System.out.println(decode.intValue());
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
private Integer age2;
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
	public Integer getAge2() {
		return age2;
	}
	public void setAge2(Integer age2) {
		this.age2 = age2;
	}

}
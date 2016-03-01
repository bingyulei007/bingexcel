package com.bing.excel;

import java.lang.reflect.Field;

import org.junit.Test;

import com.bing.excel.GuavalTEst.Person;
import com.bing.utils.ReflectDependencyFactory;

public class ObjectReflectTEst {

	@Test
	public void testContr(){
		Object newInstance = ReflectDependencyFactory.newInstance(Person.class, new Object[]{23,"0",12});
		System.out.println(newInstance);
	}
	
	@Test
	public void testFinal(){
		Person  a=new Person(1, "zhizhang", 13);
		dos(a);
		Field[] declaredFields = Person.class.getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			Class<?> clazz = declaredFields[i].getDeclaringClass();
			System.out.println(clazz);
		}
		System.out.println(a.getName()+":"+a.getId());
	}
	
	public void dos( Person p){
		p=new Person("nihao", 12);
		p.setId(5);
	}
}

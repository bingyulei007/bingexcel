package com.bing.other;

import java.lang.reflect.Field;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class MyTest {
	@Test
	public void testme() {
		
		Field[] fields = P.class.getDeclaredFields();
		for (Field field : fields) {
			if(field.getType().isArray()){
				System.out.println(field.getType().getComponentType().getName());
			}else{
				System.out.println(field.getType()+":");
			}
		}
		
	}
	public static class P{
		String[] a;
		int[] b;
		String c;
		public String[] getA() {
			return a;
		}
		public void setA(String[] a) {
			this.a = a;
		}
		public int[] getB() {
			return b;
		}
		public void setB(int[] b) {
			this.b = b;
		}
		public String getC() {
			return c;
		}
		public void setC(String c) {
			this.c = c;
		}
		
	}
}

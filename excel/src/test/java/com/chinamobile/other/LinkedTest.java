package com.chinamobile.other;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

public class LinkedTest {
	@Test
	public void testTime(){
		
		/*System.out.println(getArrayListTime());
		System.out.println("--------------");
		System.out.println(getLinkedArrayTime());*/
		byte a='a';
		byte c=78;
		char b='a';
		System.out.println(a);
		System.out.println(c);
		System.out.println(b);
		int i=23;
		double d=(double)i;
		System.out.println(d);
	}
	
	public   long getArrayListTime(){
        Collection cl = new ArrayList();
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000000; i++){
            cl.add(new Date());
            cl.add("a");
        }
        
      
        return System.currentTimeMillis() - start;
    }
    public  long  getLinkedArrayTime(){
        
        Collection cl = new LinkedList();
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1000000; i++){
        	 cl.add(new Date());
            cl.add("a");
        }
    
       
        return System.currentTimeMillis() - start;
    }
    
    @Test
    public void arrayTest(){
    	int[] arr={1,12,45,63,25};
    	Object b=arr;
    	
    	//_____-----以上模仿传过来的参数b---------------
    	Class<?> type = b.getClass().getComponentType();
    	Object[] arrObj=null;
    	int length = Array.getLength(b);
    	for(int i=0;i<length;i++){
    		System.out.println(Array.get(b, i));
    	}
    	
    }
	class Student {
		Integer num;
		String name;
		Student(int num, String name) {
			this.num = num;
			this.name = name;
		}
		public int hashCode() {
			return num.hashCode();
		}
		public boolean equals(Object o) {
			Student s = (Student) o;
			return num == s.num && name.equals(s.name);
		}
	}
	@Test
	public void testForlistandhashset() {
		HashSet hs = new HashSet();
		hs.add(new Student(1, "zhangsan"));
		hs.add(new Student(2, "lisi"));
		hs.add(new Student(3, "wangwu"));
		hs.add(new Student(1, "zhangsan2"));
		hs.add(new Student(1, "zhangsan"));
		Iterator it = hs.iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}

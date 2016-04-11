package com.bing.other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.junit.Test;

public class LinkedTest {
	@Test
	public void testTime(){
		
		System.out.println(getArrayListTime());
		System.out.println("--------------");
		System.out.println(getLinkedArrayTime());
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
}

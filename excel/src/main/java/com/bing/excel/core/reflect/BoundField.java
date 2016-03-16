package com.bing.excel.core.reflect;

import java.lang.reflect.Field;

public class BoundField {
	final String name;
	final Field field;
	 public BoundField(Field field,String name) {
		 this.field=field;
	      this.name = field.getName();
	    }
	 protected Object initializeValue(Object obj,Object value) throws IllegalArgumentException, IllegalAccessException{
		 field.set(obj, value);
		 return obj;
	 }
}

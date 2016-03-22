package com.bing.excel.core.reflect;

import java.lang.reflect.Field;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.exception.ConversionException;
import com.bing.excel.mapper.ConversionMapper.FieldConverterMapper;

public class BoundField {
	private final String name;
	private final Field field;
	 public BoundField(Field field,String name) {
		 this.field=field;
	      this.name = field.getName();
	    }
	 protected Object initializeValue(Object obj,String value,FieldConverterMapper converterMapper) {
		 //field.set(obj, value);
		 if(value!=null){
			 if(converterMapper!=null){
				 FieldValueConverter converter = converterMapper.getFieldConverter();
				 if(converter==null){
					 throw new NullPointerException("the converter for ["+name+"] is null");
				 }
				 boolean canConvert = converter.canConvert(converterMapper.getClazz());
				 if(!canConvert){
					 throw new ConversionException ("the selected converter ["+converter.getClass()+"] cannot handle type ["+converterMapper.getClazz()+"]");
				 }
				 
				 Object fieldValue = converter.fromString(value);
				 if(fieldValue!=null){
					 try {
						field.set(obj, fieldValue);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new IllegalArgumentException("It happened an error when set the value of the Entity !", e);
					}
				 }
				 
			 }else{
				 throw new NullPointerException("the converter for ["+name+"] is null");
			 }
		 }
		 return obj;
	 }
	 
}

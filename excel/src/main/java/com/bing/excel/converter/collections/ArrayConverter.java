package com.bing.excel.converter.collections;

import java.lang.reflect.Array;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.handler.ConverterHandler;
import com.google.common.base.Strings;

/**
 * @author shizhongtao
 *
 * @date 2016-3-24
 * Description:  
 */
public class ArrayConverter extends AbstractFieldConvertor {

	
	
	private final String splitCharacter;
	public final static String SPACE_SPLIT=" ";
	public final static String SPACE_COMMA=",";
	public final static String SPACE_SEMICOLON=";";
	
	public ArrayConverter() {
		splitCharacter=SPACE_COMMA;
	}

	public ArrayConverter(String splitCharacter) {
		this.splitCharacter=splitCharacter;
	}

	@Override
	public boolean canConvert(Class<?> clz) {
		
		 return  clz.isArray();
		
	}


	@Override
	public Object fromString(String cell,ConverterHandler converterHandler,Class type) {
		if(Strings.isNullOrEmpty(cell)){
			return null;
		}
		if(type==null){
			return null;
		}
		String[] splitArr = cell.split(splitCharacter);
		
		FieldValueConverter converter = converterHandler.getLocalConverter(type);
		
		 Object array = Array.newInstance(type, splitArr.length);
		 for (int i = 0; i < splitArr.length; i++) {
			 Object object = converter.fromString(splitArr[i], converterHandler,type);
			 Array.set(array, i, object);
			}
		
		return array;
	}

}

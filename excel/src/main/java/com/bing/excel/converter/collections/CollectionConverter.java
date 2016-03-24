package com.bing.excel.converter.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import com.bing.excel.converter.AbstractFieldConvertor;
import com.bing.excel.core.handler.ConverterHandler;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.JVM;

/**
 * @author shizhongtao
 *
 * @date 2016-3-24
 * Description:  
 */
public class CollectionConverter extends AbstractFieldConvertor {
	@Override
	public boolean canConvert(Class type) {
		return type.equals(ArrayList.class) || type.equals(HashSet.class)
				|| type.equals(LinkedList.class);
	}

	@Override
	public Object fromString(String cell, ConverterHandler converterHandler,
			Class targetType) {
		Collection collection = (Collection) createCollection(targetType);
		//TODO 
		
		return collection;
	}

	private Collection createCollection(Class type) {
		if (type == null) {
			return null;
		}
		if(type.equals(ArrayList.class)){
			return Lists.newArrayList();
		}else if(type.equals(HashSet.class)){
			return Sets.newHashSet();
		}else if(type.equals(LinkedList.class)){
			return Lists.newArrayList();
		}else{
			return null;
		}
		
	}

}

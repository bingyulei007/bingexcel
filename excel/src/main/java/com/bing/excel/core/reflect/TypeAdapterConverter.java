package com.bing.excel.core.reflect;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import com.bing.excel.converter.Converter;
import com.bing.excel.mapper.OrmMapper;
import com.bing.excel.reader.vo.CellKV;
import com.bing.excel.reader.vo.ListRow;


public  class  TypeAdapterConverter<T> implements Converter{
	private final Constructor<T> constructor;
	/**
	 * 名称和
	 */
	private final Map<String ,BoundField> boundFields;

	public TypeAdapterConverter(Constructor<T> constructor, Map<String ,BoundField> boundFields) {
		this.constructor = constructor;
		this.boundFields = boundFields;
	}
	//public Object readToObject

	@Override
	public void marshal(Object source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object unmarshal(Iterable<CellKV> source, OrmMapper ormMapper) {
		return null;
	}

	
	
}

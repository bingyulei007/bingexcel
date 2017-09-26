package com.bing.excel.converter;


/**
 * <p>Title: ConverterMatcher</p>
 * <p>Description: ConverterMatcher is the base interface of any converter</p>
 * <p>Company: chinamobile</p>
 * 
 * @author zhongtao.shi
  */
public interface ConverterMatcher {

    /**
     * Determines whether the converter can marshall a particular type.
     * @param clz the Class representing the object type to be converted
     * @return true or false
     */
    boolean canConvert(Class<?> clz);

}

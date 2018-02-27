package com.bing.excel.mapper;

import com.bing.excel.converter.FieldValueConverter;
import com.sun.tools.javac.util.Assert;

/**
 * @author shizhongtao
 * @version 1.0
 * @date 2018-2-22
 * @since JDK 1.8 文件名称：UserDefineMapperHandler.java 类说明：
 */
public class UserDefineMapperHandler implements ExcelConverterMapperHandler {


  private ConversionMapper objConversionMapper;


  public UserDefineMapperHandler(ConversionMapper conversionMapper) {

    this.objConversionMapper = conversionMapper;
  }

  public UserDefineMapperHandler(ConversionMapperBuilder conversionMapperBuilder) {

    this.objConversionMapper = conversionMapperBuilder.build();
    Assert.checkNonNull(this.objConversionMapper);
  }


  /*
   * (non-Javadoc)
   *
   * @see com.chinamobile.excel.mapper.OrmMapper#getLocalConverter(java.lang.Class,
   * java.lang.String)
   */

  @Override
  public FieldValueConverter getLocalConverter(Class definedIn,
      String fieldName) {

    return objConversionMapper.getLocalConverter(definedIn, fieldName);
  }

  @Override
  public ConversionMapper.FieldConverterMapper getLocalFieldConverterMapper(Class definedIn,
      String fieldName) {

    return objConversionMapper.getLocalConverterMapper(definedIn, fieldName);
  }


  @Override
  public String getModelName(Class<?> key) {
    return objConversionMapper.getAliasName(key);
  }


}

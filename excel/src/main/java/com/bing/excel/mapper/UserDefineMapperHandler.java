package com.bing.excel.mapper;

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
    if (this.objConversionMapper == null) {
      throw new NullPointerException("objConversionMapper is null");

    }
  }

  @Override
  public ConversionMapper getObjConversionMapper() {
    return objConversionMapper;
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

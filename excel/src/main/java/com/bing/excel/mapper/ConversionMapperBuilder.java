package com.bing.excel.mapper;

import com.bing.excel.converter.FieldValueConverter;
import javafx.util.Builder;

/**
 * ConversionMapperBuildder 用于
 *
 * @author shi
 * @create 2018-02-22.
 */
public class ConversionMapperBuilder implements Builder<ConversionMapper> {

  private ConversionMapper conversionMapper = new ConversionMapper();

  private ConversionMapperBuilder() {

  }

  public static ConversionMapperBuilder toBuilder() {
    return new ConversionMapperBuilder();
  }

  @Override
  public ConversionMapper build() {
    return this.conversionMapper;
  }

  public ConversionMapperBuilder modelName(Class<?> clazz, String name) {
    this.conversionMapper.addModelName(clazz, name);
    return this;
  }

  public ConversionMapperBuilder fieldConverter(Class<?> clazz, String filedName,
      Class<?> filedType, int index) {
    this.fieldConverter(clazz, filedName, filedType, index, null, null, false);
    return this;
  }

  public ConversionMapperBuilder fieldConverter(Class<?> clazz, String filedName,
      Class<?> filedType, int index,
      FieldValueConverter converter) {
    this.fieldConverter(clazz, filedName, filedType, index, null, converter, false);
    return this;
  }

  public ConversionMapperBuilder fieldConverter(Class<?> clazz, String filedName,
      Class<?> filedType, int index,
      String alias) {
    this.fieldConverter(clazz, filedName, filedType, index, alias, null, false);
    return this;
  }

  public ConversionMapperBuilder fieldConverter(Class<?> clazz, String filedName,
      Class<?> filedType, int index,
      String alias, FieldValueConverter converter) {
    this.fieldConverter(clazz, filedName, filedType, index, alias, converter, false);
    return this;
  }

  public ConversionMapperBuilder fieldConverter(Class<?> clazz, String filedName,
      Class<?> filedType, int index,
      String alias, FieldValueConverter converter, boolean required) {
    if (alias == null) {
      alias = filedName;
    }

    this.conversionMapper
        .registerLocalConverter(clazz, filedName, index, alias, filedType, required, converter);
    return this;
  }

}

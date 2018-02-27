package com.bing.excel.mapper;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.common.FieldRelation;
import java.util.HashMap;
import java.util.Map;

public class ConversionMapper {

  private final Map<FieldRelation, FieldConverterMapper> fieldMapper = new HashMap<>();
  private final Map<Class<?>, String> modelAlias = new HashMap<>();

  public ConversionMapper() {
  }

  public void addModelName(Class<?> clazz, String name) {
    this.modelAlias.put(clazz, name);
  }



  public void registerLocalConverter(Class definedIn, String fieldName,
      int index, String alias, Class<?> fieldType, boolean readRequired,
      FieldValueConverter converter) {

    registerLocalConverter(definedIn, fieldName, new FieldConverterMapper(
        index, converter, alias, fieldType, readRequired));
  }

  private void registerLocalConverter(Class definedIn, String fieldName,
      FieldConverterMapper mapper) {

    mapper.setContainer(definedIn);
    fieldMapper.put(new FieldRelation(definedIn, fieldName), mapper);
  }

  public FieldValueConverter getLocalConverter(Class definedIn,
      String fieldName) {
    return fieldMapper.get(new FieldRelation(definedIn, fieldName))
        .getFieldConverter();
  }

  public FieldConverterMapper getLocalConverterMapper(Class definedIn,
      String fieldName) {
    return fieldMapper.get(new FieldRelation(definedIn, fieldName));
  }


  public String getAliasName(Class<?> key) {
    return modelAlias.get(key);
  }

  public static class FieldConverterMapper {

    private int index;
    private boolean isPrimitive = true;
    private Class<?> clazz;
    private FieldValueConverter converter;
    private String alias;
    private boolean readRequired = false;

    private Class container;

    public Class getContainer() {
      return container;
    }

    public void setContainer(Class container) {
      this.container = container;
    }

    public int getIndex() {
      return index;
    }

    public boolean isPrimitive() {
      return isPrimitive;
    }

    public Class<?> getFieldClass() {
      return clazz;
    }

    public String getAlias() {
      return alias;
    }


    public boolean isReadRequired() {
      return readRequired;
    }

    public FieldValueConverter getFieldConverter() {
      return converter;
    }

    public void setFieldConverter(FieldValueConverter converter) {
      this.converter = converter;
    }

    public FieldConverterMapper(int index, FieldValueConverter converter,
        String alias, Class<?> clazz) {
      super();
      this.index = index;
      this.isPrimitive = clazz.isPrimitive();
      this.clazz = clazz;
      this.alias = alias;
      this.converter = converter;
    }

    public FieldConverterMapper(int index, FieldValueConverter converter,
        String alias, Class<?> clazz, boolean readRequired) {
      super();
      this.index = index;
      this.isPrimitive = clazz.isPrimitive();
      this.clazz = clazz;
      this.alias = alias;
      this.readRequired = readRequired;
      this.converter = converter;
    }

  }
}

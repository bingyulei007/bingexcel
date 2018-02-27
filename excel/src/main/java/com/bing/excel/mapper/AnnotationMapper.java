package com.bing.excel.mapper;

public interface AnnotationMapper {

  /**
   * 注册转换类实体
   */
  void processEntity(final Class[] initialTypes);

  /**
   * 注册转换类实体
   */
  void processEntity(final Class initialType);
}

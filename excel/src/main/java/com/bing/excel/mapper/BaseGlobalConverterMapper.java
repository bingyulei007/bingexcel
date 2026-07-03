package com.bing.excel.mapper;

import java.util.Collections;
import java.util.Date;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.converter.base.ByteFieldConverter;
import com.bing.excel.converter.base.DoubleFieldConverter;
import com.bing.excel.converter.base.ShortFieldConverter;
import com.bing.excel.converter.enums.EnumConVerter;
import com.bing.excel.converter.base.BooleanFieldConverter;
import com.bing.excel.converter.base.CharacterFieldConverter;
import com.bing.excel.converter.base.DateFieldConverter;
import com.bing.excel.converter.base.FloatFieldConverter;
import com.bing.excel.converter.base.IntegerFieldConverter;
import com.bing.excel.converter.base.LongFieldConverter;
import com.bing.excel.converter.base.StringFieldConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认的全局转换类，先静态吧，容我想想
 *
 * @author shizhongtao
 *
 * date 2016-3-19
 * Description:
 */
public class BaseGlobalConverterMapper {

  public final static Map<Class<?>, FieldValueConverter> globalFieldConverterMapper;

  static {
    Map<Class<?>, FieldValueConverter> m = new HashMap<>();
    m.put(String.class, new StringFieldConverter());
    m.put(Date.class, new DateFieldConverter());
    m.put(Enum.class, new EnumConVerter());
    m.put(Integer.class, new IntegerFieldConverter());
    m.put(Long.class, new LongFieldConverter());
    m.put(Boolean.class, new BooleanFieldConverter());
    m.put(Byte.class, new ByteFieldConverter());
    m.put(Character.class, new CharacterFieldConverter());
    m.put(Double.class, new DoubleFieldConverter());
    m.put(Float.class, new FloatFieldConverter());
    m.put(Short.class, new ShortFieldConverter());
    globalFieldConverterMapper = Collections.unmodifiableMap(m);
  }

}

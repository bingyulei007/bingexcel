package com.bing.excel.core;


import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.common.ExcleBuilder;
import com.bing.excel.core.handler.LocalConverterHandler;
import com.bing.excel.core.impl.BingExcelImpl;

/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <code>BingExcel</code>的构造类，可以添加自定义转换器等。<／p>
 * <p>
 * Company: chinamobile<／p>
 *
 * @author zhongtao.shi
 * date 2015-12-8
 */

/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Company: chinamobile<／p>
 *
 * @author zhongtao.shi
 * date 2015-12-8
 */
public class BingExcelBuilder implements ExcleBuilder<BingExcel> {

  private final ConverterHandler localConverterHandler = new LocalConverterHandler();

  /**
   * bingExcel:对应的excel工具类。
   */
  private BingExcel bingExcel;

  /**
   * <p>
   * Title: <／p>
   * <p>
   * Description: 构造新的builder对象<／p>
   */
  private BingExcelBuilder() {

  }

  public static ExcleBuilder<BingExcel> toBuilder() {

    return new BingExcelBuilder();

  }

  /**
   * @return BingExcel 实例
   */
  public static BingExcel builderInstance() {
    return (new BingExcelBuilder()).builder();
  }

  @Override
  public ExcleBuilder<BingExcel> registerFieldConverter(Class<?> clazz,
      FieldValueConverter converter) {
    localConverterHandler.registerConverter(clazz, converter);
    return this;
  }

  @Override
  public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
      String filedName, int index) {
    return addFieldConversionMapper(clazz, filedName, index, null, null);

  }

  @Override
  public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
      String filedName, int index, String alias) {
    return addFieldConversionMapper(clazz, filedName, index, alias, null);
  }

  @Override
  public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
      String filedName, int index, String alias, FieldValueConverter converter) {
    getBingExcel().fieldConverter(clazz, filedName, index, alias, converter);
    return this;
  }

  @Override
  public ExcleBuilder<BingExcel> addClassNameAlias(Class<?> clazz,
      String alias) {
    getBingExcel().modelName(clazz, alias);
    return this;
  }


  @Deprecated
  @Override
  public BingExcel builder() {
    if (bingExcel == null) {
      bingExcel = new BingExcelImpl(localConverterHandler);
    }

    return this.bingExcel;
  }

  @Override
  public BingExcel build() {

    return this.getBingExcel();
  }

  private BingExcel getBingExcel() {
    if (bingExcel == null) {
      bingExcel = new BingExcelImpl(localConverterHandler);
    }
    return this.bingExcel;
  }
}

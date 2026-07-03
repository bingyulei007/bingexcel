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
    return (new BingExcelBuilder()).build();
  }

  @Override
  public ExcleBuilder<BingExcel> registerFieldConverter(Class<?> clazz,
      FieldValueConverter converter) {
    localConverterHandler.registerConverter(clazz, converter);
    return this;
  }

  /**
   * Registers a user-defined mapper for a single field. This builder is an
   * <em>alternative</em> to annotations — a code-side way to declare alias,
   * column index and converter without {@code @CellConfig}/{@code @BingConvertor}.
   *
   * <p><b>Use one mechanism per field — annotations OR this builder, not both;</b>
   * prefer keeping a whole class on a single mechanism. A user mapper takes
   * precedence over the annotation mapper as a per-field whole-replacement, not a
   * per-property merge: once a field is registered here its {@code @CellConfig}
   * mapper is fully masked — the supplied index/alias/converter are used as-is and
   * unspecified properties do <em>not</em> fall back to annotation values (e.g. a
   * {@code @BingConvertor} on the same field is dropped). Always provide the
   * field's complete mapping in one call.
   */
  @Override
  public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz,
      String filedName, int index) {
    return addFieldConversionMapper(clazz, filedName, index, filedName, null);

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

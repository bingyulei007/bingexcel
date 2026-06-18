package com.bing.excel.mapper;

import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.mapper.ConversionMapper.FieldConverterMapper;

/**
 * Supplies the {@link FieldConverterMapper} for a field. Two implementations
 * exist: {@link UserDefineMapperHandler} (builder-registered overrides) and
 * {@link AnnotationMapperHandler} ({@code @CellConfig} / {@code @BingConvertor}).
 *
 * <p>How a field's effective mapping is resolved, across three independent axes:
 * <ol>
 *   <li><b>Handler precedence</b> — callers pass handlers in priority order and
 *       take the first non-null match: user-defined &gt; annotation. When neither
 *       supplies a converter, the per-type default converter is used. Registering
 *       a user mapper for a field replaces that field's annotation mapper whole
 *       (not a per-property merge).</li>
 *   <li><b>Within a field</b> — an explicit {@code index >= 0} wins; otherwise a
 *       non-empty {@code aliasName} is matched against the title row
 *       (see {@link com.bing.excel.core.impl.TitleAliasResolver}).</li>
 *   <li><b>Converter</b> — three tiers, highest first: a field-level converter
 *       (declared via {@code @BingConvertor}, or passed to
 *       {@code addFieldConversionMapper(..., converter)}) wins, because it leaves
 *       {@link FieldConverterMapper#getFieldConverter()} non-null; otherwise a
 *       per-type converter registered via
 *       {@code BingExcelBuilder.registerFieldConverter(type, converter)} is used;
 *       otherwise the framework's built-in global default for the type
 *       ({@link com.bing.excel.mapper.BaseGlobalConverterMapper}) applies. Note
 *       {@code registerFieldConverter} is keyed by field <em>type</em>, so it only
 *       fills in fields that declare no field-level converter.</li>
 * </ol>
 *
 * <p>Note: the deprecated event path ({@link com.bing.excel.core.BingExcelEvent})
 * uses only the annotation handler, so axis 1 collapses to annotation-only there.
 */
public interface ExcelConverterMapperHandler {


  ConversionMapper getObjConversionMapper();

  @Deprecated
  FieldValueConverter getLocalConverter(Class definedIn,
      String fieldName);

  FieldConverterMapper getLocalFieldConverterMapper(Class definedIn,
      String fieldName);

  String getModelName(Class<?> definedIn);

}

package com.bing.excel.core.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.exception.IllegalCellConfigException;
import com.bing.excel.mapper.ConversionMapper.FieldConverterMapper;
import com.bing.excel.mapper.ExcelConverterMapperHandler;
import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListRow;
import com.google.common.base.Strings;

/**
 * Resolves {@link CellConfig#aliasName()} to actual column indices by reading
 * the Excel title row. Holds resolved mappings in a per-read-session map so
 * that the shared, cached {@link FieldConverterMapper} instances are never mutated.
 *
 * <p>Usage:
 * <ol>
 *   <li>Create one instance per sheet read.</li>
 *   <li>Call {@link #captureTitleRow(ListRow)} when the title row is encountered.</li>
 *   <li>Call {@link #resolve(Class, int, ExcelConverterMapperHandler...)} to validate
 *       that all aliasNames were found.</li>
 *   <li>Pass the resolver to
 *       {@link com.bing.excel.core.reflect.TypeAdapterConverter#unmarshal(ListRow, TitleAliasResolver, ExcelConverterMapperHandler...)}</li>
 * </ol>
 */
public class TitleAliasResolver {

    private Map<String, Integer> titleAliasToIndex;
    private Map<String, Integer> resolvedFieldIndices;

    /**
     * Captures the title row cells into an alias→column-index map.
     *
     * @param rowList the title row
     */
    public void captureTitleRow(ListRow rowList) {
        titleAliasToIndex = new HashMap<>();
        for (CellKV<String> kv : rowList) {
            if (kv.getValue() != null) {
                titleAliasToIndex.put(kv.getValue(), kv.getIndex());
            }
        }
    }

    /**
     * Resolves all aliasName-only fields of the given class against the captured
     * title row. Populates the internal field→index map.
     *
     * @param clazz      the entity class
     * @param sheetIndex the current sheet index (for error messages)
     * @param handlers   the mapper handlers to look up FieldConverterMapper, in
     *                   priority order (first non-null match wins, matching unmarshal)
     */
    public void resolve(Class<?> clazz, int sheetIndex, ExcelConverterMapperHandler... handlers) {
        if (titleAliasToIndex == null) {
            throw new IllegalStateException("captureTitleRow must be called before resolve");
        }
        resolvedFieldIndices = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            CellConfig cellConfig = field.getAnnotation(CellConfig.class);
            if (cellConfig == null) {
                continue;
            }
            FieldConverterMapper mapper = getFieldConverterMapper(clazz, field.getName(), handlers);
            if (mapper == null || mapper.getIndex() >= 0) {
                continue;
            }
            String userAlias = cellConfig.aliasName();
            Integer found = titleAliasToIndex.get(userAlias);
            if (found == null) {
                throw new IllegalCellConfigException("field[" + clazz.getName() + "#"
                    + field.getName() + "] with aliasName[" + userAlias
                    + "] was not found in the title row of sheet[" + sheetIndex
                    + "]; available titles=" + titleAliasToIndex.keySet());
            }
            resolvedFieldIndices.put(field.getName(), found);
        }
    }

    private static FieldConverterMapper getFieldConverterMapper(Class<?> clazz, String fieldName,
        ExcelConverterMapperHandler... handlers) {
        if (handlers == null) {
            return null;
        }
        for (int i = 0; i < handlers.length; i++) {
            ExcelConverterMapperHandler handler = handlers[i];
            if (handler == null) {
                continue;
            }
            FieldConverterMapper mapper = handler.getLocalFieldConverterMapper(clazz, fieldName);
            if (mapper != null) {
                return mapper;
            }
        }
        return null;
    }

    /**
     * Returns the resolved column index for the given field, or -1 if this field
     * was not resolved via aliasName (i.e. it has an explicit index on the annotation).
     *
     * @param fieldName the Java field name
     * @param mapper    the field's FieldConverterMapper (used as fallback)
     * @return the column index to use for reading
     */
    public int getResolvedIndex(String fieldName, FieldConverterMapper mapper) {
        if (resolvedFieldIndices != null) {
            Integer resolved = resolvedFieldIndices.get(fieldName);
            if (resolved != null) {
                return resolved;
            }
        }
        return mapper.getIndex();
    }

    /**
     * Returns true if the title row has been captured and resolution has been performed.
     */
    public boolean isResolved() {
        return resolvedFieldIndices != null;
    }

    /**
     * Checks whether any {@link CellConfig}-annotated declared field of the given class
     * has {@code index < 0} and a non-empty {@code aliasName}. Used to fail fast when
     * {@code startRow=0} means no title row is available for resolution.
     *
     * @param clazz    the entity class to check
     * @param handlers the mapper handlers to look up FieldConverterMapper, in
     *                 priority order; an explicit index here suppresses the alias requirement
     * @return true if at least one field relies on aliasName-based resolution
     */
    public static boolean hasAliasOnlyFields(Class<?> clazz,
        ExcelConverterMapperHandler... handlers) {
        for (Field field : clazz.getDeclaredFields()) {
            CellConfig cellConfig = field.getAnnotation(CellConfig.class);
            if (cellConfig == null) {
                continue;
            }
            FieldConverterMapper mapper = getFieldConverterMapper(clazz, field.getName(),
                handlers);
            if (mapper != null && mapper.getIndex() >= 0) {
                continue;
            }
            if (cellConfig.index() < 0 && !Strings.isNullOrEmpty(cellConfig.aliasName())) {
                return true;
            }
        }
        return false;
    }
}

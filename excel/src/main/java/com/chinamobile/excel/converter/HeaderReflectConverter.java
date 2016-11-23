package com.chinamobile.excel.converter;

import java.util.List;

import com.chinamobile.excel.mapper.ExcelConverterMapperHandler;
import com.chinamobile.excel.vo.CellKV;


public interface HeaderReflectConverter {
   List<CellKV<String>> getHeader(ExcelConverterMapperHandler handler);
}

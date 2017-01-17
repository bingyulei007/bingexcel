package com.chinamobile.excel;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by szt on 2017/1/17.
 */
public class WriteHandlerTest {
  @Test
  public void test() {
    WriteHandler handler = ExcelWriterFactory.createXSSF("D:/aa.xlsx");
    List<CellKV<String>> listStr=new ArrayList<>();
    listStr.add(new CellKV<String>(0, "diyi"));
    listStr.add(new CellKV<String>(1, "date"));
    handler.createSheet("aa");
    handler.writeHeader(listStr);
    Date  date=null;
    String a=null;
    handler.writeLine(new ListLine().addValue(0, true).addValue(1,date).addValue(2,a));
    handler.flush();

  }

}

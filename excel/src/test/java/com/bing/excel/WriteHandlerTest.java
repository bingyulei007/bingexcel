package com.bing.excel;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
  @Test
  public void test2() {
    WriteHandler handler = ExcelWriterFactory.createXSSF("/Users/shi/workspace/aa/测试.xlsx");
    handler.createSheet("列表视图");
    ListLine listLine = new ListLine();
    listLine.addValue(0,"姓名").addValue(1,"性别");
    handler.writeHeader(listLine);
    handler.setDataValidationList( (short)1,(short)1000,(short)1,(short)1,new String[]{"男","女","其他"});
    handler.flush();

  }
}

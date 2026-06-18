package com.bing.excel;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.writer.ExcelWriterFactory;
import com.bing.excel.writer.WriteHandler;

import com.bing.utils.FileCreateUtils;
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
    String path = System.getProperty("java.io.tmpdir") + "WriteHandlerTest_aa.xlsx";
    WriteHandler handler = ExcelWriterFactory.createXSSF(path);
    List<CellKV<String>> listStr = new ArrayList<CellKV<String>>();
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
    String path = System.getProperty("java.io.tmpdir") + "WriteHandlerTest_bb.xlsx";
    FileCreateUtils.deleteFile(path);
    WriteHandler handler = ExcelWriterFactory.createXSSF(path);
    handler.createSheet("列表视图");
    ListLine listLine = new ListLine();
    listLine.addValue(0,"姓名").addValue(1,"性别");
    handler.writeHeader(listLine);
    handler.setDataValidationList( (short)1,(short)1000,(short)1,(short)1,new String[]{"男","女","其他"});
    handler.flush();

  }
}

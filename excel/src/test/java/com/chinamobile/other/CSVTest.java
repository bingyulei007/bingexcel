package com.chinamobile.other;

import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;
import com.bing.utils.FileCreateUtils;

import com.chinamobile.excel.WriteTest2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by szt on 2016/11/24.
 */
public class CSVTest {
  @Test
  public void csvWrite() throws IOException {
    Writer out = new FileWriter(FileCreateUtils.createFile("/Users/shi/workspace/a.csv"));
    out.write(new String(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF }));

    final String[] FILE_HEADER = {"ID", "我日", "Gender", "Major"};
    CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER);
    CSVPrinter csvPrinter = new CSVPrinter(out, format);
    csvPrinter.printRecord(12, "乱码", null, "gis");
    csvPrinter.close();
  }


  @Test
  public void terst() {
    List<String> list=new ArrayList<>();
    list.add("sadf");
    list.add(0,"sdf");
//    list.add(1,"adf");
//    list.add(3,"asdf");
    System.out.println(list.size());
  }


  @Test
  public void tersWrite() throws IOException {
    BingExcel excel= BingExcelBuilder.builderInstance();
    List<WriteTest2.Person> ps = new ArrayList<>();
    ps.add(new WriteTest2
        .Person(23,"he",3.45));
ps.add(new WriteTest2
        .Person(213,"你好",3.45));

    excel.writeCSV("/Users/shi/workspace/ab.csv",ps);
  }
}

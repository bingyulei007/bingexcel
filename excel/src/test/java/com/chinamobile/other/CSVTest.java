package com.chinamobile.other;

import com.bing.utils.FileCreateUtils;

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
    Writer out = new FileWriter(FileCreateUtils.createFile("D:/aoptest2/a.csv"));
    final String[] FILE_HEADER = {"ID", "Name", "Gender", "Major"};
    CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER);
    CSVPrinter csvPrinter = new CSVPrinter(out, format);
    csvPrinter.printRecord(12, "hello", null, "gis");
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
}

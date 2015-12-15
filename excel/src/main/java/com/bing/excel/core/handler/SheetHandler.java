package com.bing.excel.core.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**  
 * 创建时间：2015-12-8下午7:08:01  
 * 项目名称：excel  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：BingSheetHandler.java  
 * 类说明：  处理Sheet辅助类
 */
public  class SheetHandler <T>{
	private Sheet sheet;
	private List<T> list =new ArrayList<>();
	public SheetHandler(Sheet sheet) {
		this.sheet=sheet;
	}

	public List<T>  ReadToList(int fromRow){
		  for (int j = fromRow; j <= sheet.getLastRowNum(); j++) {
			  Row r = sheet.getRow(j);
			  if (r == null) {
		          // This whole row is empty
		          // Handle it as needed
		          continue;
		       }
			  int	  lastColumn=  r.getLastCellNum();
		       for (int cn = 0; cn < lastColumn; cn++) {
		          Cell c = r.getCell(cn, Row.RETURN_BLANK_AS_NULL);
		          if (c == null) {
		             
		          } else {
		            
		          }
		       }
		  }
		  return list;
	}
}

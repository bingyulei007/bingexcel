package com.bing.excel.core;


/**
 * @author shizhongtao
 *
 * @date 2016-2-17
 * Description:  
 */
public class ReaderCondition {
	private int startRow = 0;
	private int endRow = Integer.MAX_VALUE;
	private int sheetIndex = 0;
	
	public ReaderCondition startRow(int startRow){
		this.startRow=startRow;
		return this;
	}
	public ReaderCondition endRow(int endRow){
		this.endRow=endRow;
		return this;
	}
	public ReaderCondition sheetIndex(int sheetIndex){
		this.sheetIndex=sheetIndex;
		return this;
	}
	public int getStartRow() {
		return startRow;
	}
	public int getEndRow() {
		return endRow;
	}
	public int getSheetIndex() {
		return sheetIndex;
	}
	
}

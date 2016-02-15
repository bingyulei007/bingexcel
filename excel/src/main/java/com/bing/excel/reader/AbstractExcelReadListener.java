package com.bing.excel.reader;

import java.util.List;

import com.bing.excel.reader.vo.CellKV;


public abstract class AbstractExcelReadListener implements ExcelReadListener {

	@Override
	public void optRow(int curRow, List<CellKV> rowList) {
		
	}

	@Override
	public void startSheet(int sheetIndex, String name) {
		
	}

	@Override
	public void endSheet(int sheetIndex, String name) {
		
	}

	@Override
	public void endWorkBook() {
		
	}

}

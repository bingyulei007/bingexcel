package com.bing.excel.reader;


import com.bing.excel.reader.vo.ListRow;


public abstract class AbstractExcelReadListener implements ExcelReadListener {

	@Override
	public void optRow(int curRow, ListRow rowList) {
		
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

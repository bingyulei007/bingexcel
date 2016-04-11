package com.bing.excel.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.omg.CORBA.portable.UnknownException;

import com.bing.excel.vo.CellKV;
import com.bing.excel.vo.ListLine;
import com.bing.excel.vo.ListRow;
import com.bing.excel.writer.exception.ExcelOutException;
import com.bing.utils.FileCreateUtils;

public class DefaultFileWriterHandler implements WriteHandler {
	private final Workbook wb;
	private transient OutputStream os;
	private int currentRowIndex = -1;
	private Row currentRow;
	private Sheet currentSheet;

	public DefaultFileWriterHandler(Workbook wb, File file)
			throws FileNotFoundException {
		this.wb = wb;
		os = new FileOutputStream(file);
	}

	public DefaultFileWriterHandler(Workbook wb, String path) {
		this.wb = wb;
		File f = FileCreateUtils.createFile(path);
		try {
			os = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// system bug
			f.deleteOnExit();
			throw new UnknownException(e);
		}
	}

	@Override
	public void writeSheet(String name) {
		if (StringUtils.isBlank(name)) {
			currentSheet = wb.createSheet();
		} else {
			currentSheet = wb.createSheet(name);
		}
		
	}

	@Override
	public void writeLine(ListLine line) {
		currentRowIndex++;
		currentRow = currentSheet.createRow(currentRowIndex);
	}

	@Override
	public void writeHeader(List<CellKV<String>> listStr) {
		currentRowIndex++;
		CellStyle style = wb.createCellStyle();
		// 设置这些样式
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		style.setAlignment(CellStyle.ALIGN_CENTER);
		// 生成一个字体
		Font font = wb.createFont();
		font.setColor(IndexedColors.BLACK.index);
		font.setFontHeightInPoints((short) 14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		// 把字体应用到当前的样式
		style.setFont(font);
		currentRow = currentSheet.createRow(currentRowIndex);
		currentRow.setHeight((short) 0x249);
		for (CellKV<String> cellKV : listStr) {
			Cell cell = currentRow.createCell(cellKV.getIndex());
			cell.setCellValue(cellKV.getValue());
			cell.setCellStyle(style);
			//currentSheet.autoSizeColumn(cellKV.getIndex());
			int size=cellKV.getValue().length();
			if(size>10){
				size=10;
			}
			if(size<4){
				size=4;
			}
			currentSheet.setColumnWidth((short) (cellKV.getIndex() + 1), (short) ((50 *size ) / ((double) 1 / 20)));
		}
	}

	

	@Override
	public void flush() {
		try {
			if (os != null) {
				this.os.flush();
				this.os.close();
			}
		} catch (IOException e) {
			throw new ExcelOutException("Happen exception when flush", e);
		}
	}
}

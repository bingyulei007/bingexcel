package com.bing.excel.core.Reader;

import java.text.SimpleDateFormat;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;

/**
 * 创建时间：2015-12-15下午2:59:01 项目名称：excel
 * 
 * @author shizhongtao
 * @version 1.0
 * @since JDK 1.7 文件名称：CellValueReaderImpl.java 类说明：
 */
public class CellValueReaderImpl implements CellValueReader {
	public final static int TYPE_NUMERIC = 0;
	public final static int TYPE_STRING = 1;
	public final static int TYPE_BOOLEAN = 2;
	private Cell cell;
	private boolean date1904;
	private int type;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bing.excel.core.Reader.CellValueReader#getValue()
	 */
	@Override
	public Object getValue() {
		if (null != cell) {
			
    		//return obj;
    	} 
		return null;
	}

	/**
	 * Return the cell type.
	 * 
	 * @return the cell type
	 * @see CellValueReaderImpl#TYPE_NUMERIC
	 * @see CellValueReaderImpl#TYPE_STRING
	 * @see CellValueReaderImpl#TYPE_BOOLEAN
	 */
	@Override
	public int getCellType() {
		return this.type;
	}

	/**
	 * <p>Title: isDate1904</p>
	 * <p>Description: 确定excel基础年费是以1991还是1904</p>
	 * @return
	 */
	public boolean isDate1904() {
		return date1904;
	}

	/**
	 * <p>Title: getCell</p>
	 * <p>Description: 得到pio 中 对应Cell对象</p>
	 * @return
	 */
	public Cell getCell() {
		return cell;
	}


}

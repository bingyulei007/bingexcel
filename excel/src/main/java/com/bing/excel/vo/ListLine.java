package com.bing.excel.vo;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * listrow 对象
 * 
 * @author shizhongtao
 * 
 * @date 2016-2-17 Description:
 */
public class ListLine {
	private List<CellKV<String>> listStr = null;
	private List<CellKV<Double>> listDouble = null;
	private List<CellKV<Boolean>> listBoolean = null;
	private List<CellKV<Date>> listDate = null;

	public void addValue(int index, int value) {
		addValue(index, (double) value);
	}

	public void addValue(int index, double value) {
		if (listDouble == null) {
			listDouble = new ArrayList<>();
		}
		listDouble.add(new CellKV<Double>(index, value));
	}

	public void addString(int index, String value) {
		if (listStr == null) {
			listStr = new ArrayList<>();
		}
		listStr.add(new CellKV<String>(index, value));
	}

	public void addBoolean(int index, boolean value) {
		if (listBoolean == null) {
			listBoolean = new ArrayList<>();
		}
		listBoolean.add(new CellKV<Boolean>(index, value));
	}

	public void addDate(int index, Date value) {
		if (listDate == null) {
			listDate = new ArrayList<>();
		}
		listDate.add(new CellKV<Date>(index, value));
	}
}

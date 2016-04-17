package com.bing.excel.vo;

public class OutValue {
	public enum OutType {
		INTEGER, LONG, DOUBLE, STRING, DATE, UNDEFINE
	}
	
	private OutType outType;
	private Object value;
	
	public OutValue() {
		super();
	}
	
	public OutValue(OutType outType, Object value) {
		super();
		this.outType = outType;
		this.value = value;
	}

	public OutType getOutType() {
		return outType;
	}
	public void setOutType(OutType outType) {
		this.outType = outType;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
}

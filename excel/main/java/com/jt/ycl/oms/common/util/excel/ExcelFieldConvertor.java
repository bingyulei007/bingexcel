package com.jt.ycl.oms.common.util.excel;

public abstract class ExcelFieldConvertor {
	public   Object marshal(String fieldName,Object obj){
		return obj;
	};
	public  String unMarshal(String fieldName,String obj){
		return obj;
	};
}

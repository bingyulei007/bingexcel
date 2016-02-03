package com.jt.ycl.oms.report.util;

public abstract class ExcelFieldConvertor {
	public   Object marshal(String fieldName,Object obj){
		return obj;
	};
	public  String unMarshal(String fieldName,String obj){
		return obj;
	};
}

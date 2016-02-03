package com.jt.ycl.oms.report.bean;

public enum ExpressStatus {
	FINISHED("完成"),NO_ACQUIRE("未提到"),IN_THE_WAY("在途"),RETURNED("已退"),UNDEFINDE("_未定义_");
	private String value;
	ExpressStatus(String value){
		this.value=value;
	}
	public ExpressStatus from(String value){
		ExpressStatus[] values = ExpressStatus.values();
		for (ExpressStatus item : values) {
			if(item.value.equals(value.trim())){
				return item;
			}
		}
		return UNDEFINDE;
	}
	public String value() {
		return value;
	}
	public String toString(){
		return this.value();
	}
	
}

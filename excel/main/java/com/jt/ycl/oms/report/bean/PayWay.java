package com.jt.ycl.oms.report.bean;


public enum PayWay {
	COD("COD"), MONEY("现金支付"),PREPAY( "预付" ), POS("银联POS" ),POS2("电商POS"),UNDEFINED( "_未定义_" );
	private String value;


	PayWay(String value) {
		this.value = value;
	}

	public PayWay from(String value) {
		PayWay[] values = PayWay.values();
		for (PayWay item : values) {
			if(item.value.equals(value.trim())){
			this.value=value;
			return item;
			}
		}
		return UNDEFINED;
	}

	public String value() {
		return this.value;
	}
	public String toString(){
		return this.value();
	}
}

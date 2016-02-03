package com.jt.ycl.oms.common.vo;

import java.util.Date;


public class YiGaoExpress {
	public static final double posRate=0.0038;
	/**
	 * 保单号
	 */
	private String policyId;
	/**
	 * 配送状态
	 */
	private ExpressStatus status;
	/**
	 * 提单时间
	 */
	private Date hasPolicyTime;
	/**
	 * 完成时间
	 */
	private Date finishedTime;
	private String desc;
	private PayWay payWay;
	private String expressNo;
	/**
	 * 被保险人
	 */
	private String insurant;
	private String carNumber;
	private double totlePremium;
	private String addr;
	private String phoneNum;
	private String insuranceCompany;
	public String getPolicyId() {
		return policyId;
	}
	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
	public ExpressStatus getStatus() {
		return status;
	}
	public void setStatus(ExpressStatus status) {
		this.status = status;
	}
	public Date getHasPolicyTime() {
		return hasPolicyTime;
	}
	public void setHasPolicyTime(Date hasPolicyTime) {
		this.hasPolicyTime = hasPolicyTime;
	}
	public Date getFinishedTime() {
		return finishedTime;
	}
	public void setFinishedTime(Date finishedTime) {
		this.finishedTime = finishedTime;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public PayWay getPayWay() {
		return payWay;
	}
	public void setPayWay(PayWay payWay) {
		this.payWay = payWay;
	}
	public String getExpressNo() {
		return expressNo;
	}
	public void setExpressNo(String expressNo) {
		this.expressNo = expressNo;
	}
	public String getInsurant() {
		return insurant;
	}
	public void setInsurant(String insurant) {
		this.insurant = insurant;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public double getTotlePremium() {
		return totlePremium;
	}
	public void setTotlePremium(double totlePremium) {
		this.totlePremium = totlePremium;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getInsuranceCompany() {
		return insuranceCompany;
	}
	public void setInsuranceCompany(String insuranceCompany) {
		this.insuranceCompany = insuranceCompany;
	}
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
	public enum ExpressStatus {
		/**
		 * 配送完成
		 */
		  FINISHED("完成"),
		  /**
			 * 未提到保单
			 */
		  NO_ACQUIRE("未提到"),IN_THE_WAY("在途"),RETURNED("已退"),UNDEFINDE("_未定义_");
		 
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
}

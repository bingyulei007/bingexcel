package com.jt.ycl.oms.report.bean;

import java.util.Date;

public class ExpressVo {
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
		if (null == status) {
			this.status = ExpressStatus.UNDEFINDE;
		} else {
			this.status = status;
		}
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
		if (null == payWay) {
			this.payWay = PayWay.UNDEFINED;
		} else {
			this.payWay = payWay;
		}
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

}

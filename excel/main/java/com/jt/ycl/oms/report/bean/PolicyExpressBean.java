/**
 * 
 */
package com.jt.ycl.oms.report.bean;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author wuqh
 *
 */
public class PolicyExpressBean {
	
	/**
	 * 保单号
	 */
	private String policyId;
	
	/**
	 * 城市名称
	 */
	private String cityName;
	
	/**
	 * 快递公司
	 */
	private int expressCompany;
	
	/**
	 * 被保险人
	 */
	private String insurant;
	
	/**
	 * 车牌号
	 */
	private String carNumber;
	
	/**
	 * 应收保费=商业险总保费+交强险保费+车船税
	 */
	private float premium = 0f;
	
	/**
	 * 送单地址
	 */
	private String expressAddress;
	
	/**
	 * 购买者的电话，可以是渠道商，也可以是个人
	 */
	private String phone;
	
	/**
	 * 付款方式
	 */
	private int payMode = 3;
	
	/**
	 * 保险公司
	 */
	private int companyCode;
	
	/**
	 * 配送状态
	 */
	private int status;

	/**
	 * 快递费
	 */
	private float expressCost=0f;
	
	/**
	 * 快递单号
	 */
	private String expressSerialNo;
	
	/**
	 * 刷卡手续费
	 */
	private float posFee = 0f;
	
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date expressDate;
	
	@JsonFormat(pattern="yyyy-MM-dd", timezone="GMT+8")
	private Date finishedDate;
	
	private String desc;
	
	/**
	 * 收件人，一般情况下，收件人为商户名称，但也有是车主的情况
	 */
	private String recipient;
	
	/**
	 * 风险系数
	 */
	private String risk;
	
	public int getExpressCompany() {
		return expressCompany;
	}

	public Date getFinishedDate() {
		return finishedDate;
	}

	public void setFinishedDate(Date finishedDate) {
		this.finishedDate = finishedDate;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setExpressCompany(int expressCompany) {
		this.expressCompany = expressCompany;
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

	public String getExpressAddress() {
		return expressAddress;
	}

	public void setExpressAddress(String expressAddress) {
		this.expressAddress = expressAddress;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getPayMode() {
		return payMode;
	}

	public void setPayMode(int payMode) {
		this.payMode = payMode;
	}

	public float getExpressCost() {
		return expressCost;
	}

	public void setExpressCost(float expressCost) {
		this.expressCost = expressCost;
	}

	public float getPosFee() {
		return posFee;
	}

	public void setPosFee(float posFee) {
		this.posFee = posFee;
	}

	public float getPremium() {
		return premium;
	}

	public void setPremium(float premium) {
		this.premium = premium;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(int companyCode) {
		this.companyCode = companyCode;
	}

	public String getExpressSerialNo() {
		return expressSerialNo;
	}

	public void setExpressSerialNo(String expressSerialNo) {
		this.expressSerialNo = expressSerialNo;
	}

	public Date getExpressDate() {
		return expressDate;
	}

	public void setExpressDate(Date expressDate) {
		this.expressDate = expressDate;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getPolicyId() {
		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public String getRisk() {
		return risk;
	}

	public void setRisk(String risk) {
		this.risk = risk;
	}
}
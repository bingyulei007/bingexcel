/**
 * 
 */
package com.jt.ycl.oms.task;

/**
 * @author Andy Cui
 */
public class PaymentRecord {
	
	/**
	 * 付款金额
	 */
	private double amount;
	
	/**
	 * 收款人开户行
	 */
	private String bank;
	
	/**
	 * 收款人银行帐户名
	 */
	private String bankAcountName;
	
	/**
	 * 收款人银行账号
	 */
	private String bankCard;
	
	/**
	 * 收款方所在省份
	 */
	private String province;
	
	/**
	 * 收款方所在城市
	 */
	private String city;
	
	/**
	 * 结算序列号
	 */
	private String serialNo;
	
	/**
	 * 备注
	 */
	private String remark;
	
	private String channelCode;
	
	private String channelName;

	private String policyId;
	
	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getBankAcountName() {
		return bankAcountName;
	}

	public void setBankAcountName(String bankAcountName) {
		this.bankAcountName = bankAcountName;
	}

	public String getBankCard() {
		return bankCard;
	}

	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getPolicyId() {
		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}
}
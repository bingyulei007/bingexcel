package com.jt.ycl.oms.insurance.vo;

/**
 * 对应excel导出数据。佣金发放流水导入时候使用
 * 
 * @author bing
 * 
 * 
 */
public class BrokerageBill {
	/**
	 * 佣金费用
	 */
	private double fee;
	/**
	 * 付款人账号
	 */
	private String payerCarNo;
	private String receiverbankName;
	private String receiverCarNo;
	/** 收款人名称 */
	private String receiverName;
	/** 汇款用途 */
	private String feeUse;
	/** 收款方所在省份 */
	private String receiverProvince;
	/** 收款方所在城市 */
	private String receiverCity;
	/** 币种 */
	private String currency;
	/** 渠道商编码 */
	private String channelCode;
	/** 渠道商 */
	private String merchant;
	/** 结算车辆 */
	private String carNo;
	/** 保单ID ,多个以逗号隔开*/
	private String policyIds;
	/** 转账结果   OK与 F：代表没有转账
	 */
	private String result;
	public double getFee() {
		return fee;
	}
	public void setFee(double fee) {
		this.fee = fee;
	}
	public String getPayerCarNo() {
		return payerCarNo;
	}
	public void setPayerCarNo(String payerCarNo) {
		this.payerCarNo = payerCarNo;
	}
	public String getReceiverbankName() {
		return receiverbankName;
	}
	public void setReceiverbankName(String receiverbankName) {
		this.receiverbankName = receiverbankName;
	}
	public String getReceiverCarNo() {
		return receiverCarNo;
	}
	public void setReceiverCarNo(String receiverCarNo) {
		this.receiverCarNo = receiverCarNo;
	}
	public String getReceiverName() {
		return receiverName;
	}
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	public String getFeeUse() {
		return feeUse;
	}
	public void setFeeUse(String feeUse) {
		this.feeUse = feeUse;
	}
	public String getReceiverProvince() {
		return receiverProvince;
	}
	public void setReceiverProvince(String receiverProvince) {
		this.receiverProvince = receiverProvince;
	}
	public String getReceiverCity() {
		return receiverCity;
	}
	public void setReceiverCity(String receiverCity) {
		this.receiverCity = receiverCity;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getChannelCode() {
		return channelCode;
	}
	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}
	public String getMerchant() {
		return merchant;
	}
	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}
	public String getCarNo() {
		return carNo;
	}
	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}
	
	public String getPolicyIds() {
		return policyIds;
	}
	public void setPolicyIds(String policyIds) {
		this.policyIds = policyIds;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}

}

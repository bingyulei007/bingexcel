package com.jt.ycl.oms.task;

public class MerchantSmsInfo {
	
	/**
	 * 商家名称
	 */
	private String name;
	
	/**
	 * 商家编码
	 */
	private String merchantCode;
	
	/**
	 * 商家联系电话
	 */
	private String phone;
	
	/**
	 * 商家总总保单数
	 */
	private int total = 0;
	
	/**
	 * 已出单数
	 */
	private int chudan_finished = 0;
	
	/**
	 * 正在核保单数
	 */
	private int need_underwrite = 0;
	
	/**
	 * 核保通过单数
	 */
	private int underwrite_ok= 0;
	
	/**
	 * 已反馈报价单数
	 */
	private int response_baojia = 0;
	
	/**
	 * 出单失败数
	 */
	private int transaction_failed = 0;

	/**
	 * 预计佣金收入
	 */
	private float expectCommission = 0;
	
	/**
	 * 总佣金收入
	 */
	private float totalCommission = 0;
	
	/**
	 * 销售负责人
	 */
	private String salesMan;
	
	/**
	 * 销售负责人电话
	 */
	private String salesManPhone;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getChudan_finished() {
		return chudan_finished;
	}

	public void setChudan_finished(int chudan_finished) {
		this.chudan_finished = chudan_finished;
	}

	public int getNeed_underwrite() {
		return need_underwrite;
	}

	public void setNeed_underwrite(int need_underwrite) {
		this.need_underwrite = need_underwrite;
	}

	public int getUnderwrite_ok() {
		return underwrite_ok;
	}

	public void setUnderwrite_ok(int underwrite_ok) {
		this.underwrite_ok = underwrite_ok;
	}

	public int getResponse_baojia() {
		return response_baojia;
	}

	public void setResponse_baojia(int response_baojia) {
		this.response_baojia = response_baojia;
	}

	public int getTransaction_failed() {
		return transaction_failed;
	}

	public void setTransaction_failed(int transaction_failed) {
		this.transaction_failed = transaction_failed;
	}

	public float getExpectCommission() {
		return expectCommission;
	}

	public void setExpectCommission(float expectCommission) {
		this.expectCommission = expectCommission;
	}

	public float getTotalCommission() {
		return totalCommission;
	}

	public void setTotalCommission(float totalCommission) {
		this.totalCommission = totalCommission;
	}

	public String getSalesMan() {
		return salesMan;
	}

	public void setSalesMan(String salesMan) {
		this.salesMan = salesMan;
	}

	public String getSalesManPhone() {
		return salesManPhone;
	}

	public void setSalesManPhone(String salesManPhone) {
		this.salesManPhone = salesManPhone;
	}
	
}

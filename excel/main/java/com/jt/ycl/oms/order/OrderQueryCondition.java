package com.jt.ycl.oms.order;

import java.util.Date;


public class OrderQueryCondition {
	
	/**
	 * 用户手机号
	 */
	private String phone;
	
	/**
	 * 商家名称
	 */
	private String merchantName;
	
	/**
	 * 城市编码
	 */
	private int cityCode = 0;
	
	/**
	 * 订单下单查询开始时间
	 */
	private Date startTime;
	
	/**
	 * 订单下单查询结束时间
	 */
	private Date endTime;
	
	/**
	 * 当前页
	 */
	private int pageNumber = 1;
	
	/**
	 * 每页显示条目数
	 */
	private int pageSize = 20;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public int getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}

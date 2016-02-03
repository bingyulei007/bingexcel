/**
 * 
 */
package com.jt.ycl.oms.sales.bean;

import java.util.Date;


import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author wuqh
 *
 */
public class MerchantPolicyInfo {
	
	/**
	 * 渠道编号
	 */
	private String channelCode;
	
	/**
	 * 商户名称
	 */
	private String name;
	
	/**
	 * 商家地址
	 */
	private String address;
	
	/**
	 * 商家联系人
	 */
	private String contact;
	
	/**
	 * 商家联系电话
	 */
	private String phone;
	
	/**
	 * 商家级别
	 */
	private int level;
	
	/**
	 * 本月总保单数
	 */
	private int totalPolicyCount = 0;
	
	/**
	 * 挂起保单数
	 */
	private int suspendCount = 0;
	
	/**
	 * 核保通过的保单数
	 */
	private int hebaoPassedCount = 0;
	
	/**
	 * 已返馈报价的保单数
	 */
	private int responseCount = 0;
	
	/**
	 * 登录次数
	 */
	private int loginCounts;
	
	/**
	 * 最后一次登录时间
	 */
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date lastLoginDate;
	
	/**
	 * 商户车辆数
	 */
	private int vehicleCount;
	
	/**
	 * 商户上月保单数
	 */
	private int lastMonthPolicyCount;
	
	/**
	 * 可投保的车辆数
	 */
	private int canPolicyVehicleCount;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getTotalPolicyCount() {
		return totalPolicyCount;
	}

	public void setTotalPolicyCount(int totalPolicyCount) {
		this.totalPolicyCount = totalPolicyCount;
	}

	public int getSuspendCount() {
		return suspendCount;
	}

	public void setSuspendCount(int suspendCount) {
		this.suspendCount = suspendCount;
	}

	public int getHebaoPassedCount() {
		return hebaoPassedCount;
	}

	public void setHebaoPassedCount(int hebaoPassedCount) {
		this.hebaoPassedCount = hebaoPassedCount;
	}

	public int getResponseCount() {
		return responseCount;
	}

	public void setResponseCount(int responseCount) {
		this.responseCount = responseCount;
	}

	public String getChannelCode() {
		return channelCode;
	}

	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	public int getLoginCounts() {
		return loginCounts;
	}

	public void setLoginCounts(int loginCounts) {
		this.loginCounts = loginCounts;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public int getVehicleCount() {
		return vehicleCount;
	}

	public void setVehicleCount(int vehicleCount) {
		this.vehicleCount = vehicleCount;
	}

	public int getLastMonthPolicyCount() {
		return lastMonthPolicyCount;
	}

	public void setLastMonthPolicyCount(int lastMonthPolicyCount) {
		this.lastMonthPolicyCount = lastMonthPolicyCount;
	}

	public int getCanPolicyVehicleCount() {
		return canPolicyVehicleCount;
	}

	public void setCanPolicyVehicleCount(int canPolicyVehicleCount) {
		this.canPolicyVehicleCount = canPolicyVehicleCount;
	}
}

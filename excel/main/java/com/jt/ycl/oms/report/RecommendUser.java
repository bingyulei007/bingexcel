package com.jt.ycl.oms.report;

/**
 * 推荐用户数据
 * @author xiaojiapeng
 *
 */
public class RecommendUser {
	
	/**
	 * 商家推荐码
	 */
	private String merchantCode;
	
	/**
	 * 推荐用户数
	 */
	private int userCount;
	
	/**
	 * 商家名称
	 */
	private String name;
	
	/**
	 * 商家地址
	 */
	private String address;
	
	/**
	 * 商家负责人
	 */
	private String manager;
	
	/**
	 * 商家负责人电话
	 */
	private String managerPhone;
	
	/**
	 * 推荐车辆数
	 */
	private int carCount;

	public String getMerchantCode() {
		return merchantCode;
	}

	public void setMerchantCode(String merchantCode) {
		this.merchantCode = merchantCode;
	}

	public int getUserCount() {
		return userCount;
	}

	public void setUserCount(int userCount) {
		this.userCount = userCount;
	}

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

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getManagerPhone() {
		return managerPhone;
	}

	public void setManagerPhone(String managerPhone) {
		this.managerPhone = managerPhone;
	}

	public int getCarCount() {
		return carCount;
	}

	public void setCarCount(int carCount) {
		this.carCount = carCount;
	}

}

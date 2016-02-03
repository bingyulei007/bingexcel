/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.util.Date;

/**
 * @author Andy Cui
 */
public class BoardItem {

	/**
	 * 数据库主键
	 */
	private int id; 
	
	/**
	 * 城市编码
	 */
	private int cityCode;
	
	/**
	 * 销售经理ID
	 */
	private String salesId;
	
	/**
	 * 销售经理
	 */
	private String name;
	
	/**
	 * 本月保单数，Policy Current Month
	 */
	private int pcm;
	
	/**
	 * 总保单数
	 */
	private int total;
	
	/**
	 * 商户数
	 */
	private int channels;
	
	/**
	 * 渠道周活跃率，Weekly active channel
	 */
	private float wac;
	
	/**
	 * 渠道月活跃率，Monthly active channel
	 */
	private float mac;
	
	/**
	 * 统计周期
	 */
	private Date createDate;
	
	/**
	 * 本月总保费
	 */
	private double pmPolicyFee;
	
	/**
	 * 总保费
	 */
	private double totalPolicyFee;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPcm() {
		return pcm;
	}

	public void setPcm(int pcm) {
		this.pcm = pcm;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public float getWac() {
		return wac;
	}

	public void setWac(float wac) {
		this.wac = wac;
	}

	public float getMac() {
		return mac;
	}

	public void setMac(float mac) {
		this.mac = mac;
	}

	public String getSalesId() {
		return salesId;
	}

	public void setSalesId(String salesId) {
		this.salesId = salesId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public double getPmPolicyFee() {
		return pmPolicyFee;
	}

	public void setPmPolicyFee(double pmPolicyFee) {
		this.pmPolicyFee = pmPolicyFee;
	}

	public double getTotalPolicyFee() {
		return totalPolicyFee;
	}

	public void setTotalPolicyFee(double totalPolicyFee) {
		this.totalPolicyFee = totalPolicyFee;
	}
}
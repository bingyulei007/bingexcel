/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.util.Set;

import com.jt.core.model.Comments;

/**
 * @author Andy Cui
 */
public class PolicyItem {

	private String policyId;
	
	private String channelName;
	
	private String channelContactor;
	
	private String telephone;
	
	/**
	 * 客服
	 */
	private String customerService;
	
	/**
	 * 下单时间
	 */
	private String createDate;
	
	private String icCompany;
	
	private int iccode;
	
	private String licenseNo;
	
	private String carOwner;
	
	private float totalPremium;
	
	private String biDays;
	
	private String ciDays;
	
	private Set<Comments> comments;

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelContactor() {
		return channelContactor;
	}

	public void setChannelContactor(String channelContactor) {
		this.channelContactor = channelContactor;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getCustomerService() {
		return customerService;
	}

	public void setCustomerService(String customerService) {
		this.customerService = customerService;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public int getIccode() {
		return iccode;
	}

	public void setIccode(int iccode) {
		this.iccode = iccode;
	}

	public String getLicenseNo() {
		return licenseNo;
	}

	public void setLicenseNo(String licenseNo) {
		this.licenseNo = licenseNo;
	}

	public String getCarOwner() {
		return carOwner;
	}

	public void setCarOwner(String carOwner) {
		this.carOwner = carOwner;
	}

	public float getTotalPremium() {
		return totalPremium;
	}

	public void setTotalPremium(float totalPremium) {
		this.totalPremium = totalPremium;
	}

	public Set<Comments> getComments() {
		return comments;
	}

	public void setComments(Set<Comments> comments) {
		this.comments = comments;
	}

	public String getIcCompany() {
		return icCompany;
	}

	public void setIcCompany(String icCompany) {
		this.icCompany = icCompany;
	}

	public String getPolicyId() {
		return policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
	}

	public String getBiDays() {
		return biDays;
	}

	public void setBiDays(String biDays) {
		this.biDays = biDays;
	}

	public String getCiDays() {
		return ciDays;
	}

	public void setCiDays(String ciDays) {
		this.ciDays = ciDays;
	}
}
/**
 * 
 */
package com.jt.ycl.oms.coupon;


import javax.persistence.Id;

/**
 * 卡券模板
 * @author wuqh
 *
 */
public class CouponTemplateFormBean {
	
	@Id
	private int id;
	
	/**
	 * 券种类
	 */
	private int category;
	
	/**
	 * 卡券名称
	 */
	private String name;
	
	/**
	 * 如80元，100元， 1次等
	 */
	private int faceValue;
	
	/**
	 * 原价，可为NULL
	 */
	private String originalPrice;
	
	/**
	 * 结算价
	 */
	private String settlementPrice;
	
	/**
	 * 券开始使用日期
	 */
	private String startDate;
	
	
	/**
	 * 使用期限
	 */
	private String expireDate;
	
	/**
	 * 数量
	 */
	private int count;
	
	/**
	 * 数量
	 */
	private String address;
	
	/**
	 * 商家ID
	 */
	private int o2oMerchantId;
	
	/**
	 * 是否需要生成二集码
	 */
	private boolean isNeedCode;
	
	/**
	 * 备注
	 */
	private String remark;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getO2oMerchantId() {
		return o2oMerchantId;
	}

	public void setO2oMerchantId(int o2oMerchantId) {
		this.o2oMerchantId = o2oMerchantId;
	}

	public String getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}

	public int getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(int faceValue) {
		this.faceValue = faceValue;
	}


	public boolean getIsNeedCode() {
		return isNeedCode;
	}

	public void setIsNeedCode(boolean isNeedCode) {
		this.isNeedCode = isNeedCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(String originalPrice) {
		this.originalPrice = originalPrice;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSettlementPrice() {
		return settlementPrice;
	}

	public void setSettlementPrice(String settlementPrice) {
		this.settlementPrice = settlementPrice;
	}

	public void setNeedCode(boolean isNeedCode) {
		this.isNeedCode = isNeedCode;
	}

}

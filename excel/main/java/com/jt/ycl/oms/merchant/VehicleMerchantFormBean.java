/**
 * 
 */
package com.jt.ycl.oms.merchant;

/**
 * @author wuqh
 *
 */
public class VehicleMerchantFormBean {
	private long merchantId;
	private String userName;
	private String name; //商家名称
	private int level; //商家类别，A,B,C,D,E
	private String alias;
	private String province;
	private int cityCode = 0;
	private int regionId = 0;
	private String IDNumber;
	private String address;
	private String legalPerson;
	private String manager;
	private String managerPhone;
	private String workingTime;
	private String hotline;
	private double longitude = 0; //
	private double latitude = 0;
	private String bankAcountName;
	private String bank;
	private String bankCard;
	private float normalPrice = 0; //普洗门市价
	private float normalAccountPrice = 0; //普洗结算价
	private float finePrice = 0; //精洗门市价
	private float fineAccountPrice = 0; //精洗结算价
	private boolean isContainTax = false;
	private String salesId; //业务员
	private String signedDate; //签约日期
	private boolean chexian = false;
	private boolean washcar = false;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public int getCityCode() {
		return cityCode;
	}
	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}
	public int getRegionId() {
		return regionId;
	}
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getLegalPerson() {
		return legalPerson;
	}
	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
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
	public String getHotline() {
		return hotline;
	}
	public void setHotline(String hotline) {
		this.hotline = hotline;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public String getBankAcountName() {
		return bankAcountName;
	}
	public void setBankAcountName(String bankAcountName) {
		this.bankAcountName = bankAcountName;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getBankCard() {
		return bankCard;
	}
	public void setBankCard(String bankCard) {
		this.bankCard = bankCard;
	}
	public float getNormalPrice() {
		return normalPrice;
	}
	public void setNormalPrice(float normalPrice) {
		this.normalPrice = normalPrice;
	}
	public float getNormalAccountPrice() {
		return normalAccountPrice;
	}
	public void setNormalAccountPrice(float normalAccountPrice) {
		this.normalAccountPrice = normalAccountPrice;
	}
	public float getFinePrice() {
		return finePrice;
	}
	public void setFinePrice(float finePrice) {
		this.finePrice = finePrice;
	}
	public float getFineAccountPrice() {
		return fineAccountPrice;
	}
	public void setFineAccountPrice(float fineAccountPrice) {
		this.fineAccountPrice = fineAccountPrice;
	}
	public boolean isContainTax() {
		return isContainTax;
	}
	public void setIsContainTax(boolean isContainTax) {
		this.isContainTax = isContainTax;
	}
	
	public long getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(long merchantId) {
		this.merchantId = merchantId;
	}
	public String getSignedDate() {
		return signedDate;
	}
	public void setSignedDate(String signedDate) {
		this.signedDate = signedDate;
	}

	public String getSalesId() {
		return salesId;
	}
	public void setSalesId(String salesId) {
		this.salesId = salesId;
	}
	public String getIDNumber() {
		return IDNumber;
	}
	public void setIDNumber(String IDNumber) {
		this.IDNumber = IDNumber;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getWorkingTime() {
		return workingTime;
	}
	public void setWorkingTime(String workingTime) {
		this.workingTime = workingTime;
	}
	public boolean isChexian() {
		return chexian;
	}
	public void setChexian(boolean chexian) {
		this.chexian = chexian;
	}
	public boolean isWashcar() {
		return washcar;
	}
	public void setWashcar(boolean washcar) {
		this.washcar = washcar;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
}

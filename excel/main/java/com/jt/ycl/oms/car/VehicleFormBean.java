/**
 * 
 */
package com.jt.ycl.oms.car;

/**
 * 添加车辆、修改车辆信息时，作为FormBean封装
 * @author wuqh
 */
public class VehicleFormBean {
	
	private String id;
	private String merchantId;//可以为0，如果大于0表示这辆车是属于蜂巢代理商的车辆，如果=0，表示是C端用户车辆 
	private String number;
	private int cityCode;
	private String vin;
	private String enrollDate;
	private String makeDate;
	private String engineNo;
	private String modelName;
	private boolean guohu;
	private String owner;
	private String ownerId;
	private int vehicleModelId;
	private boolean companyCar;
	private String lastYearEndDate;
	private String lastYearCIEndDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getCityCode() {
		return cityCode;
	}

	public void setCityCode(int cityCode) {
		this.cityCode = cityCode;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getEnrollDate() {
		return enrollDate;
	}

	public void setEnrollDate(String enrollDate) {
		this.enrollDate = enrollDate;
	}

	public String getMakeDate() {
		return makeDate;
	}

	public void setMakeDate(String makeDate) {
		this.makeDate = makeDate;
	}

	public String getEngineNo() {
		return engineNo;
	}

	public void setEngineNo(String engineNo) {
		this.engineNo = engineNo;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public boolean isGuohu() {
		return guohu;
	}

	public void setGuohu(boolean guohu) {
		this.guohu = guohu;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public int getVehicleModelId() {
		return vehicleModelId;
	}

	public void setVehicleModelId(int vehicleModelId) {
		this.vehicleModelId = vehicleModelId;
	}

	public boolean isCompanyCar() {
		return companyCar;
	}

	public void setCompanyCar(boolean companyCar) {
		this.companyCar = companyCar;
	}

	public String getLastYearEndDate() {
		return lastYearEndDate;
	}

	public void setLastYearEndDate(String lastYearEndDate) {
		this.lastYearEndDate = lastYearEndDate;
	}

	public String getLastYearCIEndDate() {
		return lastYearCIEndDate;
	}

	public void setLastYearCIEndDate(String lastYearCIEndDate) {
		this.lastYearCIEndDate = lastYearCIEndDate;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
}
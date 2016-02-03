/**
 * 
 */
package com.jt.ycl.oms.sales.bean;

/**
 * @author Andy Cui
 */
public class Price {

	private String riskCode;
	
	private float price;

	public Price(String riskCode, float price) {
		this.riskCode = riskCode;
		this.price = price;
	}

	public String getRiskCode() {
		return riskCode;
	}

	public void setRiskCode(String riskCode) {
		this.riskCode = riskCode;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
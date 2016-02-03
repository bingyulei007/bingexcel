package com.jt.ycl.oms.auth;

/**
 * 权限定义
 * @author xiaojiapeng
 *
 */
public enum Permission {
	/**
	 * 行驶证审核
	 */
	LICENSE_AUDIT_MGMT("license_audit_mgmt"),
	
	/**
	 * 行驶证审核记录查看
	 */
	LICENSE_AUDIT_RECORD_MGMT("license_audit_record_mgmt"),
	
	/**
	 * 微信自定义菜单
	 */
	WX_MENU_MGMT("wx_menu_mgmt"),
	
	/**
	 * 微信自动回复设置
	 */
	WX_SUBSCRIBE_REPLY_MGMT("wx_subscribe_reply_mgmt"),
	
	/**
	 * 商家查询
	 */
	MERCHANT_MGMT("merchant_mgmt"),
	
	/**
	 * 保单查询
	 */
	INSURANCE_POLICY_MGMT("insurance_policy_mgmt"),
	
	/**
	 * 保单删除
	 */
	INSURANCE_POLICY_DELETE_MGMT("insurance_policy_delete_mgmt"),
	
	/**
	 * 保单分配
	 */
	INSURANCE_POLICY_ASSIGN("insurance_policy_assign"),
	
	/**
	 * 报价查询
	 */
	BAOJIA_MGMT("baojia_mgmt"),
	
	/**
	 * 用户订单查询
	 */
	USER_ORDER_MGMT("user_order_mgmt"),
	
	/**
	 * 用户车辆管理
	 */
	CAR_MGMT("car_mgmt"),
	
	/**
	 * O2O商家管理
	 */
	O2O_MERCHANT_MGMT("o2o_merchant_mgmt"),
	
	/**
	 * 用户数据报表管理
	 */
	REPORT_USER_MGMT("report_user_mgmt"),
	
	/**
	 * O2O商家及洗车商家删除权限 
	 */
	MERCHANT_DELETE("merchant_delete"),
	
	/**
	 * 发放卡券
	 */
	GRANT_COUPON("grant_coupon"),
	
	/**
	 * 用户管理
	 */
	USER_MGMT("user_mgmt"),
	
	/**
	 * 商家佣金返点配置
	 */
	COMMISSION_RATE_CONFIG("commission_rate_config")
	;
	
	private String name;
	
	private Permission(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}

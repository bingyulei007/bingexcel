package com.jt.ycl.oms.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Role {
	
	/**
	 * 行驶证审核员
	 */
	public static final String LICENSE_AUDITOR = "license-auditor";
	
	/**
	 * 超级用户
	 */
	public static final String OMS_MANAGER = "oms-manager";
	
	/**
	 * 核保专员
	 */
	public static final String OMS_HEBAO_USER = "oms-user";
	
	/**
	 * 客服，负责信息校对工作
	 */
	public static final String CUSTOMER_SERVICE = "customer-service";
	
	/**
	 * 销售经理
	 */
	public static final String BD_USER = "bd-user";
	
	/**
	 * 角色名称
	 */
	private String name;
	
	/**
	 * 角色具有的权限
	 */
	private List<Permission> permissions;
	
	/**
	 * 保险公司编码
	 * 保险公司专员角色该值有效
	 */
	private int icCode = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public int getIcCode() {
		return icCode;
	}

	public void setIcCode(int icCode) {
		this.icCode = icCode;
	}

	public static List<Permission> getPermissionsByRoleName(String roleName){
		List<Permission> permissionList = new ArrayList<>();
		if(StringUtils.equals(roleName, LICENSE_AUDITOR)) {
			permissionList.add(Permission.LICENSE_AUDIT_MGMT);
			permissionList.add(Permission.LICENSE_AUDIT_RECORD_MGMT);
		} else if(StringUtils.equals(roleName, OMS_MANAGER)){
			//管理员具备所有权限
			permissionList.addAll(Arrays.asList(Permission.values()));
		} else if(StringUtils.equals(roleName, OMS_HEBAO_USER) || StringUtils.equals(roleName, CUSTOMER_SERVICE)) {
			permissionList.add(Permission.LICENSE_AUDIT_MGMT);
			permissionList.add(Permission.LICENSE_AUDIT_RECORD_MGMT);
			
			permissionList.add(Permission.MERCHANT_MGMT);
			permissionList.add(Permission.USER_ORDER_MGMT);
			permissionList.add(Permission.INSURANCE_POLICY_MGMT);
			permissionList.add(Permission.CAR_MGMT);
			permissionList.add(Permission.O2O_MERCHANT_MGMT);
			
			permissionList.add(Permission.BAOJIA_MGMT);
			permissionList.add(Permission.REPORT_USER_MGMT);
			
			permissionList.add(Permission.INSURANCE_POLICY_ASSIGN);
		} else if(StringUtils.equals(roleName, BD_USER)) {
			permissionList.add(Permission.MERCHANT_MGMT);
			permissionList.add(Permission.O2O_MERCHANT_MGMT);
			permissionList.add(Permission.INSURANCE_POLICY_MGMT);
			permissionList.add(Permission.REPORT_USER_MGMT);
			permissionList.add(Permission.CAR_MGMT);
		}
		return permissionList;
	}
}
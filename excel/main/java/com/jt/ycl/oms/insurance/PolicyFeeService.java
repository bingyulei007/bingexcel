/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.PolicyFeeDao;
import com.jt.core.model.PolicyFee;

/**
 * @author wuqh
 */
@Service
@Transactional
public class PolicyFeeService {
	
	@Autowired
	private PolicyFeeDao policyFeeDao;

	public PolicyFee findByPolicyId(String orderId) {
		return policyFeeDao.findByPolicyId(orderId);
	}
	
	public void savePolicyFee(PolicyFee policyFee){
		policyFeeDao.save(policyFee);
	}
	
	public List<PolicyFee> findByPolicyIdIn(List<String> policyIds){
		return policyFeeDao.findByPolicyIdIn(policyIds);
	}
	
	public void savePolicyFees(List<PolicyFee> policyFees){
		policyFeeDao.save(policyFees);
	}
}

/**
 * 
 */
package com.jt.ycl.oms.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.InsurancePolicyDaoImpl;
import com.jt.core.model.InsurancePolicy;

/**
 * @author wuqh
 *
 */
@Service
public class WillBeLostPolicyService {

	@Autowired
	private InsurancePolicyDaoImpl insurancePolicyDaoImpl;

	/**
	 *统计即将流失的保单
	 */
	public List<InsurancePolicy> willBeLostPolicy(final String salesman, final int days, int pageNumber, int pageSize){
		return insurancePolicyDaoImpl.queryWillBeLostPolicy(salesman, days, pageNumber, pageSize);
	}
	
}

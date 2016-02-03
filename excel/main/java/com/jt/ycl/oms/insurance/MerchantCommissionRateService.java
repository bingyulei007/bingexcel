package com.jt.ycl.oms.insurance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.MerchantCommissionRateDao;
import com.jt.core.model.MerchantCommissionRate;

@Service
public class MerchantCommissionRateService {

	@Autowired
	private MerchantCommissionRateDao configDao;
	
	public void update(List<MerchantCommissionRate> configs){
		configDao.save(configs);
	}
	
	public List<MerchantCommissionRate> getAll(){
		return configDao.findAll();
	}
	
	public MerchantCommissionRate findByCompanyCode(int companyCode){
		return configDao.findByCompanyCode(companyCode);
	}
}

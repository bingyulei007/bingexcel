/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.util.ArrayList;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.MerchantMaintenanceDao;
import com.jt.core.model.MerchantMaintenance;

/**
 * @author wuqh
 * 商家维护记录
 */
@Service
public class MerchantMaintenanceService {
	
	@Autowired
	private MerchantMaintenanceDao merchantMaintenanceDao;

	/**
	 * 查询商家的所有维护记录
	 * @param merchantCode
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<MerchantMaintenance> findAll(final String merchantCode, int pageNumber,	int pageSize) {
		Page<MerchantMaintenance> pageResult = merchantMaintenanceDao.findAll(new Specification<MerchantMaintenance>() {
			@Override
			public Predicate toPredicate(Root<MerchantMaintenance> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotEmpty(merchantCode)) {
					predicateList.add(cb.equal(root.get("merchantCode"), merchantCode));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.ASC,null));
		return pageResult;
	}

	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "maintenanceDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

	public MerchantMaintenance addRecord(MerchantMaintenance record) {
		return merchantMaintenanceDao.save(record);
	}

}

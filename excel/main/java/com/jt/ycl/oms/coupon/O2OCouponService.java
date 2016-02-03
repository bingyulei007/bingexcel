/**
 * 
 */
package com.jt.ycl.oms.coupon;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.CouponTemplateDao;
import com.jt.core.model.CouponTemplate;

/**
 * @author wuqh
 *
 */
@Service
@Transactional
public class O2OCouponService {
	
	@Autowired
	private CouponTemplateDao couponTemplateDao;

	public Page<CouponTemplate> findCouponsByMerchantId(final int merchantId,
			int pageNumber, int pageSize) {
		Page<CouponTemplate> pageResult = couponTemplateDao.findAll(new Specification<CouponTemplate>() {
			@Override
			public Predicate toPredicate(Root<CouponTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(merchantId > 0) {
					predicateList.add(cb.equal(root.get("o2oMerchantId"), merchantId));
				} 
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.DESC,null));
		return pageResult;
	}

	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "expireDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

	public void createCouponTemplate(CouponTemplate couponTemplate) {
		couponTemplateDao.save(couponTemplate);
	}

	public CouponTemplate findById(int id) {
		return couponTemplateDao.findOne(id);
	}

	public void deleteCouponTemplateById(int id) {
		couponTemplateDao.delete(id);
	}

	public List<CouponTemplate> findCouponsByMerchantId(int merchantId) {
		return couponTemplateDao.findByO2oMerchantId(merchantId);
	}

	public CouponTemplate findCouponTemplateId(int id) {
		return couponTemplateDao.findOne(id);
	}

	public int findCouponByO2oMerchantIdAndName(int o2oMerchantId, String couponName) {
		CouponTemplate couponTemplate = couponTemplateDao.findByO2oMerchantIdAndName(o2oMerchantId, couponName);
		if(couponTemplate != null){
			return 0;
		}else{
			return 1;
		}
	}
	
	public List<CouponTemplate> findCouponTemplatesByIds(List<Integer> templateIds) {
		return couponTemplateDao.findAll(templateIds);
	}

}

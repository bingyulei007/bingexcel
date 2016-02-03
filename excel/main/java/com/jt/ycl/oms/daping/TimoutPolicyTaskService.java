/**
 * 
 */
package com.jt.ycl.oms.daping;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.PolicyStatus;
import com.jt.utils.DateUtils;

/**
 * @author wuqh
 *
 */
@Service
public class TimoutPolicyTaskService {
	
	@Autowired
	private InsurancePolicyDao insurancePolicyDao;

	/**
	 * 列出录入车辆后超过10分钟还没有返馈给客户的虚拟保单，显示车牌号，客服，超时时间
	 */
	public Page<InsurancePolicy> listNoReponsePolicy(int pageNumber, int pageSize, final long timeout){
		Page<InsurancePolicy> pageResult = insurancePolicyDao.findAll(new Specification<InsurancePolicy>() {
			@Override
			public Predicate toPredicate(Root<InsurancePolicy> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				predicateList.add(cb.equal(root.get("status"), PolicyStatus.NO_QUERY_BAOJIA.value()));
				predicateList.add(cb.isNotNull(root.get("carId")));
				Path<Date> updateTime = root.get("updateTime");
				Date date = DateUtils.convertDateToStr((new Date().getTime()-timeout), "yyyy-MM-dd HH:mm:ss");
				predicateList.add(cb.lessThanOrEqualTo(updateTime, date));
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.ASC,null));
		return pageResult;
	}
	
	/**
	 * 列出进入待核保后，但超过2小时还未来核保通过的保单信息
	 */
	public Page<InsurancePolicy> listNoPassUnderwritingPolicy(int pageNumber, int pageSize, final long timeout){
		Page<InsurancePolicy> pageResult = insurancePolicyDao.findAll(new Specification<InsurancePolicy>() {
			@Override
			public Predicate toPredicate(Root<InsurancePolicy> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				predicateList.add(cb.equal(root.get("status"), PolicyStatus.NEED_UNDERWRITE.value()));
				Path<Date> updateTime = root.get("updateTime");
				Date date = DateUtils.convertDateToStr((new Date().getTime()-timeout), "yyyy-MM-dd HH:mm:ss");
				predicateList.add(cb.lessThanOrEqualTo(updateTime, date));
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.ASC,null));
		return pageResult;
	}
	
	/**
	 * 列出同意出单，但超过2小时还未出单的保单信息
	 */
	public Page<InsurancePolicy> listNoChudanPolicy(int pageNumber, int pageSize, final long timeout){
		Page<InsurancePolicy> pageResult = insurancePolicyDao.findAll(new Specification<InsurancePolicy>() {
			@Override
			public Predicate toPredicate(Root<InsurancePolicy> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				predicateList.add(cb.equal(root.get("status"), PolicyStatus.CHUDAN_AGREE.value()));
				Path<Date> updateTime = root.get("updateTime");
				Date date = DateUtils.convertDateToStr((new Date().getTime()-timeout), "yyyy-MM-dd HH:mm:ss");
				predicateList.add(cb.lessThanOrEqualTo(updateTime, date));
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.ASC,null));
		return pageResult;
	}
	
	/**
     * 创建分页请求和排序.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "updateTime");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

}

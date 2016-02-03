package com.jt.ycl.oms.order;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.OrderDao;
import com.jt.core.model.City;
import com.jt.core.model.Order;
import com.jt.ycl.oms.city.CityService;

@Service
public class OrderService {
	
	@Autowired
	private OrderDao orderDao;
	
	@Autowired
	private CityService cityService;
	
	public Page<Order> findOrders(final OrderQueryCondition condition){
		Page<Order> pageResult = orderDao.findAll(new Specification<Order>() {

			@Override
			public Predicate toPredicate(Root<Order> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotBlank(condition.getPhone())) {
					predicateList.add(cb.equal(root.get("phone"), condition.getPhone()));
				}
				if(condition.getCityCode() > 0) {
					predicateList.add(cb.equal(root.get("cityCode"), condition.getCityCode()));
				} 
				if(StringUtils.isNotBlank(condition.getMerchantName())) {
					Path<String> merchantNamePath = root.get("merchantName");
					predicateList.add(cb.like(merchantNamePath, "%" + condition.getMerchantName() + "%"));
				}
				if(condition.getStartTime() != null){
					Path<Date> createdatePath = root.get("createdate");
					predicateList.add(cb.greaterThan(createdatePath, condition.getStartTime()));
				}
				if(condition.getEndTime() != null){
					Path<Date> createdatePath = root.get("createdate");
					predicateList.add(cb.lessThanOrEqualTo(createdatePath, condition.getEndTime()));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
			
		},buildPageRequest(condition.getPageNumber(), condition.getPageSize(), Direction.DESC, null));
		
		if(pageResult != null && CollectionUtils.isNotEmpty(pageResult.getContent())){
			for(Order order : pageResult.getContent()){
				//从缓存中获取城市名称
				City city = cityService.getByCityCode(order.getCityCode());
				order.setCityName(city.getName());
			}
		}
		return pageResult;
	}
	
	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "createdate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

}

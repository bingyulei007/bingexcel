package com.jt.ycl.oms.account;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
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

import com.jt.core.dao.UserDao;
import com.jt.core.model.User;

@Service
public class UserService {
	
	@Autowired
	private UserDao userDao;
	
	/**
	 * 根据用户id查询用户
	 * @param userId
	 * @return
	 */
	public User getUserById(String userId){
		return userDao.findOne(userId);
	}
	
	public Page<User> findUsers(final String phone, final Date startDate, final Date endDate,
			final int pageNumber, final int pageSize){
		
		return userDao.findAll(new Specification<User>(){
			@Override
			public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotBlank(phone)){
					predicateList.add(cb.equal(root.get("phone"), phone));
				}
				if(startDate != null){
					Path<Date> createdatePath = root.get("createDate");
					predicateList.add(cb.greaterThan(createdatePath, startDate));
				}
				if(endDate != null){
					Path<Date> createdatePath = root.get("createDate");
					predicateList.add(cb.lessThanOrEqualTo(createdatePath, endDate));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
			
		}, buildPageRequest(pageNumber, pageSize, Direction.DESC,null));
	}
	
	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "createDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

}

/**
 * 
 */
package com.jt.ycl.oms.account;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.OmsUserDao;
import com.jt.core.model.OmsUser;

/**
 * 用户管理
 * @author xiaojiapeng
 */
@Service
@Transactional
public class AccountService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private OmsUserDao omsUserDao;
	
	public Page<OmsUser> findUsers(final String name, int pageNumber, int pageSize) {
		Page<OmsUser> pageResult = omsUserDao.findAll(new Specification<OmsUser>() {
			@Override
			public Predicate toPredicate(Root<OmsUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotEmpty(name)&& !"".equals(name)) { 
					Path<String> pathName = root.get("name");
					predicateList.add(cb.equal(pathName, name));
				}
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
    		sort = new Sort(sortType, "createDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

	public OmsUser save(OmsUser user){
		return omsUserDao.save(user);
	}
	
	public OmsUser findOmsUserByUserId(String userId) {
		return omsUserDao.findOne(userId);
	}

	public void deleteOmsUser(String userId) {
		omsUserDao.delete(userId);
	}

	public OmsUser findByName(String name) {
		return omsUserDao.findByName(name);
	}

	public OmsUser findByEmployeeID(String employeeID) {
		return omsUserDao.findByEmployeeID(employeeID);
	}
	
	public List<OmsUser> findByRole(String role){
		return omsUserDao.findByRole(role);
	}
}

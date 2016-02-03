package com.jt.ycl.oms.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jt.core.dao.BaojiaRecordDao;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.UserDao;
import com.jt.core.dao.WXSubscribeDao;
import com.jt.core.model.Merchant;
import com.jt.core.model.WXSubscribe;

@Service
public class ReportUserService {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CarDao carDao;
	
	@Autowired
	private BaojiaRecordDao baojiaRecordDao;
	
	@Autowired
	private MerchantDao merchantDao;
	
	@Autowired
	private WXSubscribeDao subscribeDao;
	
	@Autowired
	private InsurancePolicyDao policyDao;
	
	public int loginChannelCount(final Date startDate) {
		return (int) merchantDao.count(new Specification<Merchant>() {
			@Override
			public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Path<Date> path = root.get("lastUpdateDate");
				return cb.greaterThanOrEqualTo(path, startDate);
			}
		});
	}
	
	/**
	 * 根据注册时间统计用户数
	 * @param createDate 如果为空，则返回总数量
	 * @return
	 */
	public long countUserByCreateDate(Date createDate){
		if(createDate == null){
			return userDao.countAll();
		}else{
			return userDao.countByCreateDate(createDate);
		}
	}
	
	/**
	 * 根据车辆添加时间统计车辆数
	 * @param createDate 如果为空，则返回总数量
	 * @return
	 */
	public long countCarByCreateDate(Date createDate){
		if(createDate == null){
			return carDao.countValidCar();
		}else{
			return carDao.countValidCarByCreateDate(createDate);
		}
	}
	
	/**
	 * 根据最后一次报价查询时间统计车辆报价数量
	 * @param queryDate
	 * @return
	 */
	public long countBaojiaRecordByQueryDate(Date queryDate){
		if(queryDate == null){
			return baojiaRecordDao.count();
		}else{
			return baojiaRecordDao.countByQueryDate(queryDate);
		}
	}
	
	/**
	 * 查询当日新增车辆询价数
	 */
	public long countNewCarBaojiaByQueryDate(Date queryDate) {
		List<String> carIdList = this.baojiaRecordDao.selectCarIdByQueryDate(queryDate);
		List<String> carIdList2 = this.carDao.selectCarIdByCreateDate(queryDate);
		HashMap<String, Boolean> tempMap = new HashMap<>();
		for(String carId : carIdList2) {
			tempMap.put(carId, true);
		}
		
		long result = 0;
		for(String carId : carIdList) {
			if(tempMap.get(carId) != null) {
				result ++;
			}
		}
		return result;
	}
	
	/**
	 * 统计推荐用户数据
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public List<RecommendUser> countRecommendData(Date startDate, Date endDate){
		//查出所有指定时间内商家推荐的用户
		List<Object[]> users = userDao.findByCreateDate(startDate, endDate);
		
		//每个用户对应的商家
		Map<String, String> userMerchantMap = new HashMap<>();
		//统计商家推荐用户数
		Map<String, Integer> userCount = new HashMap<>();
		//统计每个用户添加的车辆数
		Map<String, Integer> carCount = new HashMap<>();
		Map<Long, Merchant> allMerchant = new HashMap<>();
		if(CollectionUtils.isNotEmpty(users)){
			ArrayList<String> userIds = new ArrayList<>();
			ArrayList<Long> merchantCodes = new ArrayList<>();
			int i = 0;
			for(Object[] user : users){
				String merchantId = user[1].toString();
				if(StringUtils.isNumeric(merchantId)){
					long id = Long.parseLong(merchantId);
					if(!merchantCodes.contains(id)){
						merchantCodes.add(id);
					}
				}
				if(merchantCodes.size() == 20 || users.size()-1 == i){
					List<Merchant> merchants = merchantDao.findByIdIn(merchantCodes);
					for(Merchant merchant : merchants){
						allMerchant.put(merchant.getId(), merchant);
					}
					merchantCodes.clear();
				}
				
				userMerchantMap.put(user[0].toString(), merchantId);
				if(userCount.containsKey(merchantId)){
					userCount.put(merchantId, userCount.get(merchantId) + 1);
				}else{
					userCount.put(merchantId, 1);
				}
				
				userIds.add(user[0].toString());
				if(userIds.size() == 20 || users.size()-1 == i){
					//统计商家推荐的用户添加的车辆数量
					List<Object[]> objs = carDao.findByUserIdAndCreateDate(userIds, startDate, endDate);
					if(CollectionUtils.isNotEmpty(objs)){
						for(Object[] obj : objs){
							int count = Integer.parseInt(obj[0].toString());
							String userId = obj[1].toString();
							carCount.put(userId, count);
						}
					}
					userIds.clear();
				}
				i++;
				
			}
		}
		//统计商家推荐的车辆数，商家推荐的用户所添加的车辆数量之和
		Map<String, Integer> allCarCount = new HashMap<>();
		for(Map.Entry<String, Integer> entry : carCount.entrySet()){
			String userId = entry.getKey();
			int count = entry.getValue();
			String merchantId = userMerchantMap.get(userId);
			if(count > 0){
				if(allCarCount.containsKey(merchantId)){
					allCarCount.put(merchantId, allCarCount.get(merchantId) + 1);
				}else{
					allCarCount.put(merchantId, 1);
				}
			}
		}
		List<RecommendUser> recommendUsers = new ArrayList<>();
		for(Map.Entry<String, Integer> entry : allCarCount.entrySet()){
			String merchantId = entry.getKey();
			RecommendUser recommendUser = new RecommendUser();
			recommendUser.setCarCount(entry.getValue());
			recommendUser.setMerchantCode(merchantId);
			if(StringUtils.isNumeric(merchantId)){
				Merchant merchant = allMerchant.get(Long.parseLong(merchantId));
				if(merchant != null){
					recommendUser.setName(merchant.getAlias());
					recommendUser.setAddress(merchant.getAddress());
					recommendUser.setManager(merchant.getManager());
					recommendUser.setManagerPhone(merchant.getManagerPhone());
				}
			}
			
			recommendUsers.add(recommendUser);
		}
		return recommendUsers;
	}
	
	public List<WXSubscribe> getWxSubscribe(){
		return subscribeDao.findAll();
	}
}
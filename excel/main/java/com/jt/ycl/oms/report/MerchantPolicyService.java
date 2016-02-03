package com.jt.ycl.oms.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.impl.MerchantDaoImpl;
import com.jt.core.model.MerchantPolicyStatistics;
import com.jt.core.model.MerchantQueryCondition;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.sales.bean.MerchantPolicyInfo;

/**
 * @author wuqh
 *
 */
@Service
public class MerchantPolicyService {
	
	@Autowired
	private MerchantDaoImpl merchantDaoImpl;
	
	@Autowired
	private MerchantDao merchantDao;

	@Autowired
	private InsurancePolicyDao policyDao;
	
	/**
	 * 统计DB人员签约的商家数以及签约商家保单数
	 * @param date
	 * @return
	 */
	public List<MerchantPolicyStatistics> staticsMerchantPolicy(int cityCode, String date){
		List<MerchantPolicyStatistics> results = new ArrayList<MerchantPolicyStatistics>();
		List<String> salesmans = new ArrayList<String>();
		//1. 总的签约商家数, 第一步取出DB人员总签约商家数，可防止当天签约的商家为0的情况
		List<Object[]> signedMerchantTotalCount = merchantDaoImpl.querySignedMerchantTotalCount(cityCode,salesmans);
		if(CollectionUtils.isNotEmpty(signedMerchantTotalCount)){
			for(Object[] obj : signedMerchantTotalCount){
				if(obj[0] != null){
					MerchantPolicyStatistics mps = new MerchantPolicyStatistics();
					mps.setSalesman(obj[0].toString());
					mps.setSignedMerchantTotalCount(Integer.parseInt(obj[1].toString()));
					salesmans.add(obj[0].toString());
					results.add(mps);
				}
			}
		}
		
		//2. 今日签约商家数
		List<Object[]> signedMerchantCount =  merchantDaoImpl.querySignedMerchantCount(cityCode, date);
		if(CollectionUtils.isNotEmpty(signedMerchantCount)){
			for(Object[] obj : signedMerchantCount){
				if(obj[0] != null){
					for(MerchantPolicyStatistics mps : results){
						if(mps.getSalesman().equals(obj[0].toString())){
							mps.setSignedMerchantCount(Integer.parseInt(obj[1].toString()));
							break;
						}else{
							if(!salesmans.contains(obj[0].toString())){ //处理新进入的BD人员， 之前总的商家数为0
								MerchantPolicyStatistics newSaleman = new MerchantPolicyStatistics();
								newSaleman.setSalesman(obj[0].toString());
								newSaleman.setSignedMerchantCount(Integer.parseInt(obj[1].toString()));
								salesmans.add(obj[0].toString());
								results.add(newSaleman);
							}
						}
					}
				}
			}
		}
		
		//3. 签约商家今日保单数
		if(salesmans.size()>0){
			List<Object[]> signedMerchantPolicyCount =  merchantDaoImpl.querySignedMerchantPolicyCount(cityCode, date, salesmans);
			if(CollectionUtils.isNotEmpty(signedMerchantPolicyCount)){
				for(Object[] obj : signedMerchantPolicyCount){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setSignedMerchantPolicyCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
					}
				}
			}
		}
		
		//4. 签约商家总保单数
		if(salesmans.size()>0){
			List<Object[]> signedMerchantPolicyTotalCount =  merchantDaoImpl.querySignedMerchantPolicyTotalCount(cityCode,salesmans);
			if(CollectionUtils.isNotEmpty(signedMerchantPolicyTotalCount)){
				for(Object[] obj : signedMerchantPolicyTotalCount){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setSignedMerchantPolicyTotalCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
						
					}
				}
			}
		}
		//5. 签约商家总保单数为0的商家
		if(salesmans.size()>0){
			List<Object[]> signedMerchantNoPolicy = merchantDaoImpl.querySignedMerchantNoPolicy(cityCode,salesmans);
			if(CollectionUtils.isNotEmpty(signedMerchantNoPolicy)){
				for(Object[] obj : signedMerchantNoPolicy){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setNoPlicyMerchantCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
						
					}
				}
			}
		}
		
		//6. 统计各位BD签约商户本月的出单数
		if(salesmans.size()>0){
			List<Object[]> thisMonthPolicyCount = merchantDaoImpl.queryThisMonthPolicyCount(cityCode,salesmans);
			if(CollectionUtils.isNotEmpty(thisMonthPolicyCount)){
				for(Object[] obj : thisMonthPolicyCount){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setThisMonthPolicyCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
						
					}
				}
			}
		}
		
		//6. 统计各位BD签约商户本月的出单数
		if(salesmans.size()>0){
			List<Object[]> thisMonthPolicyCount = this.queryThisMonthPolicyCount(cityCode,salesmans);
			if(CollectionUtils.isNotEmpty(thisMonthPolicyCount)){
				for(Object[] obj : thisMonthPolicyCount){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setThisMonthPolicyCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
						
					}
				}
			}
		}
		
		//7. 统计各BD名下本月出单的商户数
		if(salesmans.size()>0){
			List<Object[]> thisMonthHasPolicyMerchantCount = this.queryThisMonthHasPolicyMerchantCount(cityCode,salesmans);
			if(CollectionUtils.isNotEmpty(thisMonthHasPolicyMerchantCount)){
				for(Object[] obj : thisMonthHasPolicyMerchantCount){
					if(obj[0] != null){
						for(MerchantPolicyStatistics mps : results){
							if(mps.getSalesman().equals(obj[0].toString())){
								mps.setThisMonthHasPolicyMerchantCount(Integer.parseInt(obj[1].toString()));
								break;
							}
						}
						
					}
				}
			}
		}
		return results;
	}
	
	/**
	 * 每一销售总的签约商家数
	 * @param cityCode
	 * @param salesmans
	 * @return
	 */
	public List<Object[]> querySignedMerchantTotalCount(int cityCode, List<String> salesmans){
		return merchantDaoImpl.querySignedMerchantTotalCount(cityCode, salesmans);
	}
	
	/**
	 * 销售签约商家今日保单数
	 * @param cityCode
	 * @param salesmans
	 * @return
	 */
	public List<Object[]> querySignedMerchantPolicyCount(int cityCode, String date, List<String> salesmans){
		return merchantDaoImpl.querySignedMerchantPolicyCount(cityCode, date, salesmans);
	}
	
	/**
	 * 签约商家总保单数为0的商家
	 * @param cityCode
	 * @param salesmans
	 * @return
	 */
	public List<Object[]> querySignedMerchantNoPolicy(int cityCode, List<String> salesmans){
		return merchantDaoImpl.querySignedMerchantNoPolicy(cityCode,salesmans);
	}
	
	/**
	 * 统计各位销售签约商户本月的出单数
	 * @param cityCode
	 * @param salesmans
	 * @return
	 */
	public List<Object[]> queryThisMonthPolicyCount(int cityCode, List<String> salesmans){
		return merchantDaoImpl.queryThisMonthPolicyCount(cityCode,salesmans);
	}
	
	/**
	 * 统计各销售名下本月出单的商户数
	 * @param cityCode
	 * @param salesmans
	 * @return
	 */
	public List<Object[]> queryThisMonthHasPolicyMerchantCount(int cityCode, List<String> salesmans){
		return merchantDaoImpl.queryThisMonthHasPolicyMerchantCount(cityCode,salesmans);
	}
	
	public Map<Integer, Integer> dailyActiveCount(int cityCode, int year, int month) {
		Date now = new Date();
		Date begin = DateUtils.getBeginTimeOfMonth(now);
		Date end = DateUtils.getEndTimeOfMonth(now);
		
		TreeMap<Integer, HashSet<Long>> tempMap = new TreeMap<>();
		//初始化
		Calendar a = Calendar.getInstance();
		if(a.get(Calendar.YEAR) != year){
			a.set(Calendar.YEAR, year);
		}
		if(a.get(Calendar.MONTH)+1 != month){
			a.set(Calendar.MONTH, month-1);
			begin = DateUtils.getBeginTimeOfMonth(a.getTime());
			end = DateUtils.getEndTimeOfMonth(a.getTime());
		}
		a.set(Calendar.DATE, 1);
		a.roll(Calendar.DATE, -1);
		
		int days = a.get(Calendar.DATE);
		
		for(int i = 1; i <= days; i++) {
			tempMap.put(i, new HashSet<Long>());
		}
		
		TreeMap<Integer, Integer> resultMap = new TreeMap<>();
		
		List<Object[]> results = policyDao.findPolicyBetween(cityCode, begin, end);
		
		for(Object[] result : results) {
			Date createDate = (Date) result[0];
			Calendar c = Calendar.getInstance();
			c.setTime(createDate);
			int day = c.get(Calendar.DAY_OF_MONTH);
			
			long merchantCode = (long) result[1];
			HashSet<Long> set = tempMap.get(day);
			set.add(merchantCode);
			tempMap.put(day, set);
		}
		for(Map.Entry<Integer, HashSet<Long>> entry : tempMap.entrySet()) {
			resultMap.put(entry.getKey(), entry.getValue().size());
		}
		return resultMap;
	}
	
	public Map<Integer, Float> dailyActiveRate(int cityCode, int year, int month) {
		Date now = new Date();
		Date begin = DateUtils.getBeginTimeOfMonth(now);
		Date end = DateUtils.getEndTimeOfMonth(now);
		TreeMap<Integer, HashSet<Long>> tempMap = new TreeMap<>();
		//初始化
		Calendar a = Calendar.getInstance();
		if(a.get(Calendar.YEAR) != year){
			a.set(Calendar.YEAR, year);
		}
		if(a.get(Calendar.MONTH)+1 != month){
			a.set(Calendar.MONTH, month-1);
			begin = DateUtils.getBeginTimeOfMonth(a.getTime());
			end = DateUtils.getEndTimeOfMonth(a.getTime());
		}
		a.set(Calendar.DATE, 1);
		a.roll(Calendar.DATE, -1);
		int days = a.get(Calendar.DATE);
		
		for(int i = 1; i <= days; i++) {
			tempMap.put(i, new HashSet<Long>());
		}
		
		TreeMap<Integer, Float> resultMap = new TreeMap<Integer, Float>();
		
		List<Object[]> policies = policyDao.findPolicyBetween(cityCode, begin, end);
		
		//计逄出每一天签约数加上前一天的存量
		int count = merchantDao.countSignedMerchantsBefore(cityCode, begin); //上一个月签约的存量
		List<Object[]> signedMerchants = merchantDaoImpl.countSignedMerchantsBetween(cityCode, begin, end); 
		TreeMap<Integer, Integer> everyDaySignedCountMap = new TreeMap<Integer, Integer>(); //每天签约数及存量
		if(CollectionUtils.isNotEmpty(signedMerchants)){
			for(Object[] objects : signedMerchants){
				int day = Integer.parseInt(objects[0].toString());
				int signedCount = Integer.parseInt(objects[1].toString());
				everyDaySignedCountMap.put(day, signedCount);
			}
		}
		int beforeDayCount = 0;//前一天的存量
		for(int i = 1; i <= days; i++) {
			if(everyDaySignedCountMap.get(i) !=null){
				beforeDayCount += everyDaySignedCountMap.get(i);
			}
			everyDaySignedCountMap.put(i, beforeDayCount+count);
		}
		
		for(Object[] result : policies) {
			Date createDate = (Date) result[0];
			Calendar c = Calendar.getInstance();
			c.setTime(createDate);
			int day = c.get(Calendar.DAY_OF_MONTH);
			long merchantCode = (long) result[1];
			HashSet<Long> set = tempMap.get(day);
			set.add(merchantCode);
			tempMap.put(day, set);
		}
		int j=1;
		for(Map.Entry<Integer, HashSet<Long>> entry : tempMap.entrySet()) {
			if(everyDaySignedCountMap.get(j) >0){
				double activeRate = entry.getValue().size()*100d/everyDaySignedCountMap.get(j);
				BigDecimal b = new BigDecimal(activeRate);  
				resultMap.put(entry.getKey(), b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
			}else{
				resultMap.put(entry.getKey(),0f);
			}
			j++;
		}
		return resultMap;
	}
	
	/**
	 * 统计销售名下在本月已出单的商户，以及本月的总出保单数，挂起保单数，核保通过保单数，已反馈报价的保单数
	 * @param condition
	 * @return
	 */
	public List<MerchantPolicyInfo> findHasPolicyMerchant(MerchantQueryCondition condition) {
		List<MerchantPolicyInfo> mpis = new ArrayList<MerchantPolicyInfo>();
		List<Object[]> results = merchantDaoImpl.findHasPolicyMerchant(condition);
		if(CollectionUtils.isNotEmpty(results)){
			for(Object[] objects : results){
				MerchantPolicyInfo mpi = new MerchantPolicyInfo();
				if(objects[0] != null){
					mpi.setChannelCode(objects[0].toString());
				}
				if(objects[1] != null){
					mpi.setName(objects[1].toString());
				}
				if(objects[2] != null){
					mpi.setAddress(objects[2].toString());
				}
				if(objects[3] != null){
					mpi.setContact(objects[3].toString());
				}
				if(objects[4] != null){
					mpi.setPhone(objects[4].toString());
				}
				if(objects[5] != null){
					mpi.setLevel(Integer.parseInt(objects[5].toString()));
				}
				if(objects[6] != null){
					mpi.setLastLoginDate((Date)objects[6]);
				}
				if(objects[7] != null){
					mpi.setSuspendCount(Integer.parseInt(objects[7].toString()));
				}
				if(objects[8] != null){
					mpi.setResponseCount(Integer.parseInt(objects[8].toString()));
				}
				if(objects[9] != null){
					mpi.setHebaoPassedCount(Integer.parseInt(objects[9].toString()));
				}
				if(objects[10] != null){
					mpi.setTotalPolicyCount(Integer.parseInt(objects[10].toString()));
				}
				if(objects[11] != null){
					mpi.setVehicleCount(Integer.parseInt(objects[11].toString()));
				}
				if(objects[12] != null){
					mpi.setLastMonthPolicyCount(Integer.parseInt(objects[12].toString()));
				}
				if(objects[13] != null){
					mpi.setCanPolicyVehicleCount(Integer.parseInt(objects[13].toString()));
				}
				mpis.add(mpi);
			}
		}
		return mpis;
	}

	/**
	 * 统计销售名下在本月没有出单的商户，以该商户以往总出保单数，处理中的的保单数
	 * @param condition
	 * @return
	 */
	public List<MerchantPolicyInfo> findNoPolicyMerchant(MerchantQueryCondition condition) {
		List<MerchantPolicyInfo> mpis = new ArrayList<MerchantPolicyInfo>();
		List<Object[]> results = merchantDaoImpl.findNoPolicyMerchant(condition);
		if(CollectionUtils.isNotEmpty(results)){
			for(Object[] objects : results){
				MerchantPolicyInfo mpi = new MerchantPolicyInfo();
				if(objects[0] != null){
					mpi.setChannelCode(objects[0].toString());
				}
				if(objects[1] != null){
					mpi.setName(objects[1].toString());
				}
				if(objects[2] != null){
					mpi.setAddress(objects[2].toString());
				}
				if(objects[3] != null){
					mpi.setContact(objects[3].toString());
				}
				if(objects[4] != null){
					mpi.setPhone(objects[4].toString());
				}
				if(objects[5] != null){
					mpi.setLevel(Integer.parseInt(objects[5].toString()));
				}
				if(objects[6] != null){
					mpi.setLastLoginDate((Date)objects[6]);
				}
				if(objects[7] != null){
					mpi.setTotalPolicyCount(Integer.parseInt(objects[7].toString()));
				}
				if(objects[8] != null){
					mpi.setVehicleCount(Integer.parseInt(objects[8].toString()));
				}
				if(objects[9] != null){
					mpi.setLastMonthPolicyCount(Integer.parseInt(objects[9].toString()));
				}
				if(objects[10] != null){
					mpi.setCanPolicyVehicleCount(Integer.parseInt(objects[10].toString()));
				}
				mpis.add(mpi);
			}
		}
		return mpis;
	}
}

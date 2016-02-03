/**
 * 
 */
package com.jt.ycl.oms.report;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.BaojiaRecordDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.utils.DateUtils;

/**
 * @author wuqh
 *
 */
@Service
public class BaoJiaConvertRateService {
	
	@Autowired
	private BaojiaRecordDao baojiaRecordDao;
	
	@Autowired
	private InsurancePolicyDao  insurancePolicyDao;

	/**
	 * 统计时间段内询价车辆数
	 * @param cityName
	 * @param year
	 * @param timeType
	 * @return
	 */
	public Map<Integer, Integer> baoJiaCarCount(String cityName, int year, int timeType) {
		Map<Integer, Integer> resultMap = new TreeMap<Integer, Integer>();
		List<Object[]> results = null;
		if(timeType == 1){ //按月
			results = baojiaRecordDao.countBaoJiaCarsByYear(cityName, year);
		}else if(timeType == 2){  //按周
			results = baojiaRecordDao.countBaoJiaCarsByWeek(cityName, year);
		}
		if(CollectionUtils.isNotEmpty(results)){
			for(Object[] objects : results){
				int key = Integer.parseInt(objects[0].toString());
				int value = Integer.parseInt(objects[1].toString());
				resultMap.put(key, value);
			}
		}
		if(timeType==2){ //按周统计
			int weeks = DateUtils.getWeeks(year);
			for(int i=1; i<=weeks; i++){
				if(!resultMap.containsKey(i)){
					resultMap.put(i, 0);
				}
			}
		}else if(timeType==1){ //按年统计
			for(int i=1; i<=12; i++){
				if(!resultMap.containsKey(i)){
					resultMap.put(i, 0);
				}
			}
		}
		return resultMap;
	}

	/**
	 * 统计询价车辆转化为保单的转化率
	 * @param cityCode
	 * @param cityName
	 * @param year
	 * @param timeType
	 * @return
	 */
	public Map<Integer, Float> baoJiaConvertRate(int cityCode, String cityName, int year, int timeType) {
		Map<Integer, Float> resultMap = new TreeMap<Integer, Float>();
		Map<Integer, Integer> carCountMap = this.baoJiaCarCount(cityName, year, timeType);
		List<Object[]> results = null;
		if(timeType == 1){ //按月
			results = insurancePolicyDao.countPolicyByYear(cityCode, year);
		}else if(timeType == 2){  //按周
			results = insurancePolicyDao.countPolicyByWeek(cityCode, year);
		}
		Map<Integer, Integer> policyMap = new HashMap<Integer, Integer>();
		if(CollectionUtils.isNotEmpty(results)){
			for(Object[] objects : results){
				int key = Integer.parseInt(objects[0].toString());
				int value = Integer.parseInt(objects[1].toString());
				policyMap.put(key, value);
			}
		}
		for(int i : carCountMap.keySet()){
			if(policyMap.get(i) != null && policyMap.get(i)>0 && carCountMap.get(i)>0){
				int policyCount = policyMap.get(i);
				double convertRate = policyCount*100d/carCountMap.get(i);
				BigDecimal b = new BigDecimal(convertRate);  
				resultMap.put(i, b.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue());
			}else{
				resultMap.put(i, 0f);
			}
		}
		return resultMap;
	}

}

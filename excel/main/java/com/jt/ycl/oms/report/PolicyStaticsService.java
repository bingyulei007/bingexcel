/**
 * 
 */
package com.jt.ycl.oms.report;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.CarDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.utils.DateUtils;

/**
 * @author wuqh
 */
@Service
public class PolicyStaticsService {

	@Autowired
	private InsurancePolicyDao policyDao;
	
	@Autowired
	private CarDao carDao;
	
	public Map<Integer, Integer> policyDailyActive(int year, int month) {
		TreeMap<Integer, Integer> resultMap = new TreeMap<>();
		Date now = new Date();
		Date begin = DateUtils.getBeginTimeOfMonth(now);
		Date end = DateUtils.getEndTimeOfMonth(now);
		
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
			resultMap.put(i, 0);
		}
		List<Object[]> results = policyDao.countPolicyBetween(begin, end);
		for(Object[] result : results) {
			if(result[0] !=null && result[1] !=null) {
				int day =  Integer.parseInt(result[0].toString());
				int count = Integer.parseInt(result[1].toString());
				resultMap.put(day, count);
			}
		}
		return resultMap;
	}
	
	/**
	 * 指定年月，统计每天新添加的车辆数，这个数据不能下降
	 */
	public Map<Integer, Integer> countNewCarByDay(int year, int month) {
		TreeMap<Integer, Integer> resultMap = new TreeMap<>();
		Date now = new Date();
		Date begin = DateUtils.getBeginTimeOfMonth(now);
		Date end = DateUtils.getEndTimeOfMonth(now);
		//初始化
		Calendar a = Calendar.getInstance();
		if(a.get(Calendar.YEAR) != year){
			a.set(Calendar.YEAR, year);
		}
		if(a.get(Calendar.MONTH) + 1 != month){
			a.set(Calendar.MONTH, month-1);
			begin = DateUtils.getBeginTimeOfMonth(a.getTime());
			end = DateUtils.getEndTimeOfMonth(a.getTime());
		}
		a.set(Calendar.DATE, 1);
		a.roll(Calendar.DATE, -1);
		int days = a.get(Calendar.DATE);
		for(int i = 1; i <= days; i++) {
			resultMap.put(i, 0);
		}
		List<Object[]> results = carDao.countCarBetween(begin, end);
		for(Object[] result : results) {
			if(result[0] !=null && result[1] !=null) {
				int day =  Integer.parseInt(result[0].toString());
				int count = Integer.parseInt(result[1].toString());
				resultMap.put(day, count);
			}
		}
		return resultMap;
	}
}

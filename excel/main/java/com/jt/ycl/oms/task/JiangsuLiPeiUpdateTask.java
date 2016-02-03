/**
 * 
 */
package com.jt.ycl.oms.task;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.ClaimHistoryDao;
import com.jt.core.model.Car;
import com.jt.utils.BaiduOCR;
import com.jt.utils.DateUtils;
import com.jt.utils.HttpService;
import com.jt.utils.YunSu;

/**
 * 定期执行，更新每辆车的上年度投保公司、交强险终保日期、商业险终保日期
 * 
 * <p>仅支持江苏省内上牌的车辆。
 * 
 * @author Andy Cui
 */
@Component
public class JiangsuLiPeiUpdateTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CarDao carDao;
	
	private HttpService httpService = HttpService.getInstance();
	
	@Autowired
	private ClaimHistoryDao claimHistoryDao;

	private int count = 0;
	
	private boolean useBaiduOCR = true;
	
	@Scheduled(cron = "0 0 8 * * ?")//每天早上8点开始执行
	public void execute() {
		logger.info("开始执行JiangsuLiPeiUpdateTask");
		count = 0;
		Page<Car> page = carDao.findAll(new Specification<Car>() {
			@Override
			public Predicate toPredicate(Root<Car> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				predicateList.add(cb.isNotNull(root.get("merchantCode")));
				predicateList.add(cb.equal(root.get("cityCode"), 224));//读取所有苏州地区商户的车辆
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, new PageRequest(0, 100));
		
		int totalPages = page.getTotalPages();
		
		int currentPage = 0;
		while(currentPage <= totalPages - 1) {
			page = carDao.findAll(new Specification<Car>() {
				@Override
				public Predicate toPredicate(Root<Car> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					predicateList.add(cb.isNotNull(root.get("merchantCode")));
					predicateList.add(cb.equal(root.get("cityCode"), 224));//读取所有苏州地区商户的车辆
					return cb.and(predicateList.toArray(new Predicate[0]));
				}
			}, new PageRequest(currentPage, 100));
			
			List<Car> carList = page.getContent();
			for(Car car : carList) {
				if(!car.getNumber().startsWith("苏")) {
					continue;
				}
				boolean updated = false;
				//如果发现某辆车已经投保了，在没进入下一年度车险可投保期内时，不去查询。
				Date lastYearBizEndDate = car.getLastYearEndDate();
				if(lastYearBizEndDate == null) {
					updatePolicyStatus(car);
					updated = true;
				} else {
					//比如是2015-09-25，2015-10-23，2016-04-23
					//只要车辆进入40天可投保期，就天天更新
					int days = DateUtils.difference(new Date(), lastYearBizEndDate);
					if(days <= 40) {
						updatePolicyStatus(car);
						updated = true;
					}
				}
				if(updated) {
					continue;
				}
				Date lastYearJQEndDate = car.getLastYearCIEndDate();
				if(lastYearJQEndDate == null) {
					updatePolicyStatus(car);
				} else {
					//比如是2015-09-25，2015-10-23，2016-04-23
					//只要车辆进入40天可投保期，就天天更新
					int days = DateUtils.difference(new Date(), lastYearJQEndDate);
					if(days <= 40) {
						updatePolicyStatus(car);
					}
				}
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
				}
			}
			currentPage++;
		}
		logger.info("JiangsuLiPeiUpdateTask执行结束，本次更新{}辆车的理赔记录。", count);
	}
	
	String[] ips = new String[]{"111.205.191.231", "106.37.176.173"};
	
	/**
	 * 更新这辆车的保险投保状态，如果发现这辆车进入40天可投保期，就每天都去省厅系统读取是否投保的状态，及时反馈给代理商。
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	private void updatePolicyStatus(Car car) {
		try {
			int seed = RandomUtils.nextInt(0, 2);
			String ipAddress = ips[seed];
			HttpGet mainPage = new HttpGet("http://" + ipAddress + ":9080/iastat_js/");
			CloseableHttpResponse response = httpService.execute(mainPage);
			response.close();
			
			HttpGet picGet = new HttpGet("http://" + ipAddress + ":9080/iastat_js/pages/login/RandomNumUtil.jsp?d=" + System.currentTimeMillis());
			response = httpService.execute(picGet);
			InputStream imageStream = response.getEntity().getContent();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(imageStream, output);
			byte[] imgBytes = output.toByteArray();
			
			String code = null;
			ObjectMapper mapper = new ObjectMapper();
			if (useBaiduOCR) {
				for (int i = 0; i < 3; i++) {
					code = BaiduOCR.ocr(imgBytes);
					if (StringUtils.isNotBlank(code) && code.length() == 4) {
						break;
					}
				}
			} else {
				code = YunSu.createByPost("andycui", "andycui", "3040", "60", "25750", "7f9a1327ea8b48a38e3e5cd4d4ae96ae", imgBytes);
				Map<String, Object> jsonMap = mapper.readValue(code, Map.class);
				if (jsonMap.get("Result") != null && jsonMap.get("Id") != null) {
					code = jsonMap.get("Result").toString();
				}
			}
			HttpPost post = new HttpPost("http://" + ipAddress + ":9080/iastat_js/peruser/queryByFrame.do");
			post.addHeader("Origin", "http://" + ipAddress + ":9080");
			post.addHeader("Proxy-Connection", "keep-alive");
			post.addHeader("Referer", "http://" + ipAddress + ":9080/iastat_js/");
			post.addHeader("Upgrade-Insecure-Requests", "1");
			
			List<NameValuePair> nvpList = new ArrayList<>();
			nvpList.add(new BasicNameValuePair("panduan", "1"));
			nvpList.add(new BasicNameValuePair("queryMethod", "queryByFrame"));
			nvpList.add(new BasicNameValuePair("policyNo", ""));
			nvpList.add(new BasicNameValuePair("licenseNo", ""));
			nvpList.add(new BasicNameValuePair("frameLastSixNo2", ""));
			nvpList.add(new BasicNameValuePair("frameLastSixNo1", ""));
			nvpList.add(new BasicNameValuePair("engineLastSixNo", ""));
			nvpList.add(new BasicNameValuePair("frameNo", car.getVin()));
			nvpList.add(new BasicNameValuePair("random", code));
			
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvpList, "UTF-8");
			post.setEntity(entity);
			response = httpService.execute(post);
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");
			response.close();
			
			BufferedReader reader = new BufferedReader(new StringReader(result));
			String line = null;
			String portal_softDownMore_list = null;
			while((line = reader.readLine()) != null) {
				if(line.trim().startsWith("portal_softDownMore_list = [")) {
					portal_softDownMore_list = line.trim();
					break;
				}
			}
			int start = portal_softDownMore_list.indexOf("=") + 2;
			int end = portal_softDownMore_list.length() - 1;
			portal_softDownMore_list = portal_softDownMore_list.substring(start, end);
			
			List<Map<String, Object>> records = mapper.readValue(portal_softDownMore_list, List.class);
			List<String> bzEndDateList = new ArrayList<>();
			Map<String, String> bzEndDateToCompanyCodeMapping = new HashMap<>();
			List<String> jqEndDateList = new ArrayList<>();
			for(Map<String, Object> record : records) {
				String risktype = record.get("risktype").toString();
				String enddate = record.get("enddate").toString();
				
				if("商业险".equals(risktype)) {
					//这是一条商业险记录
					bzEndDateList.add(enddate);
					bzEndDateToCompanyCodeMapping.put(enddate, record.get("companycode").toString());
				} else {
					//这是一条交强险记录
					jqEndDateList.add(enddate);
				}
			}
			
			String lastYearEndDate = null;
			if(bzEndDateList.size() == 1) {
				lastYearEndDate = bzEndDateList.get(0);
			} else if(bzEndDateList.size() > 1) {
				Date current = DateUtils.convertStrToDate(bzEndDateList.get(0), "yyyy-MM-dd");
				for(String endDate : bzEndDateList) {
					Date cDate = DateUtils.convertStrToDate(endDate, "yyyy-MM-dd");
					if(cDate.after(current)) {
						current = cDate;
					}
				}
				lastYearEndDate = DateUtils.convertDateToStr(current, "yyyy-MM-dd");
			}
			
			String lastYearCIEndDate = null;
			if(jqEndDateList.size() == 1) {
				lastYearCIEndDate = jqEndDateList.get(0);
			} else if(jqEndDateList.size() > 1) {
				Date current = DateUtils.convertStrToDate(jqEndDateList.get(0), "yyyy-MM-dd");
				for(String endDate : jqEndDateList) {
					Date cDate = DateUtils.convertStrToDate(endDate, "yyyy-MM-dd");
					if(cDate.after(current)) {
						current = cDate;
					}
				}
				lastYearCIEndDate = DateUtils.convertDateToStr(current, "yyyy-MM-dd");
			}
			
			boolean change = false;
			String lastYearEndDateInDB = DateUtils.convertDateToStr(car.getLastYearEndDate(), "yyyy-MM-dd");
			if(lastYearEndDate != null && !lastYearEndDate.equals(lastYearEndDateInDB)) {
				car.setLastYearEndDate(DateUtils.convertStrToDate(lastYearEndDate, "yyyy-MM-dd"));
				car.setLastYearICName(bzEndDateToCompanyCodeMapping.get(lastYearEndDate));
				change = true;
			}
			
			String lastYearCIEndDateInDB = DateUtils.convertDateToStr(car.getLastYearCIEndDate(), "yyyy-MM-dd");
			if(lastYearCIEndDate != null && !lastYearCIEndDate.equals(lastYearCIEndDateInDB)) {
				car.setLastYearCIEndDate(DateUtils.convertStrToDate(lastYearCIEndDate, "yyyy-MM-dd"));
				change = true;
			}
			
			if(change) {
				carDao.save(car);
				logger.info("更新{}的投保记录，上年度保险公司：{}，上年度商业险到期：{}，上年度交强险到期：{}", car.getNumber(), car.getLastYearICName(), 
						lastYearEndDate, lastYearCIEndDate);
			}
			count++;
		} catch (Exception e) {
			logger.error(car.getNumber() + "理赔记录查询失败：", e);
		}
	}
}
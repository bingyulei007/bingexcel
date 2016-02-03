/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.BaojiaRecordSearchCondition;
import com.jt.core.SmsService;
import com.jt.core.SmsTemplateId;
import com.jt.core.insurance.ForceSubmitPolicyFormBean;
import com.jt.core.insurance.ICCode;
import com.jt.core.insurance.InquiryResult;
import com.jt.core.insurance.QueryPriceCondition;
import com.jt.core.insurance.SubmitPolicyFormBean;
import com.jt.core.model.BaojiaRecord;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.ClaimHistory;
import com.jt.core.model.Insurance;
import com.jt.core.model.Merchant;
import com.jt.core.model.OmsUser;
import com.jt.core.model.VehicleConfigModel;
import com.jt.utils.DWZUtils;
import com.jt.utils.DateUtils;
import com.jt.utils.HttpService;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.account.AccountService;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;
import com.jt.ycl.oms.car.CarService;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.merchant.VehicleMerchantService;
import com.jt.ycl.oms.wxmgt.WXConfigInfo;

/**
 * @author wuqh
 */
@Controller
@RequestMapping(value = { "/insurance/baojia/record" })
@ResponseBody
@OMSPermission(permission = Permission.BAOJIA_MGMT)
public class BaojiaRecordController {

	@Autowired
	private BaojiaRecordService baojiaRecordService;

	@Autowired
	private CarService carService;

	@Autowired
	private CityService cityService;

	@Autowired
	private WXConfigInfo configInfo;

	@Autowired
	private SmsService smsService;

	@Autowired
	private VehicleMerchantService vehicleMerchantService;

	@Autowired
	private AccountService accountService;

	private HttpService httpService = HttpService.getInstance();

	private String rootURL = "http://127.0.0.1:6060/apigateway";

	@PostConstruct
	public void init() {
		if (SystemUtils.IS_OS_LINUX) {
			rootURL = "http://" + configInfo.getAppUrl() + "/apigateway";
		}
	}

	/**
	 * 进入商家页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public ModelAndView query() {
		ModelAndView mv = new ModelAndView("/insurance/baojiaRecord");
		return mv;
	}

	/**
	 * 商家按条件查询
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/query/condition", method = RequestMethod.POST)
	public ModelMap queryByConditon(String provinceName, String cityName, String vehicleNumber, String owner, int feedback, int dateType, String startDate,
			String endDate, int pageNumber, int pageSize) throws Exception {
		BaojiaRecordSearchCondition condition = new BaojiaRecordSearchCondition();
		condition.setProvinceName(provinceName);
		condition.setCityName(cityName);
		condition.setVehicleNumber(vehicleNumber);
		condition.setFeedback(feedback);
		condition.setPageNumber(pageNumber);
		condition.setOwner(owner);
		condition.setPageSize(pageSize);
		if (dateType != 7) {// 非自定义
			Map<String, Date> result = calQueryDate(dateType);
			condition.setStartDate(result.get("startDate"));
			condition.setEndDate(result.get("endDate"));
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(DateUtils.convertStrToDate(startDate, "yyyy-MM-dd"));
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			condition.setStartDate(cal.getTime());
			cal.setTime(DateUtils.convertStrToDate(endDate, "yyyy-MM-dd"));
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			condition.setEndDate(cal.getTime());
		}
		Map<String, Object> results = baojiaRecordService.pageBaojiaRecords(condition);
		List<BaojiaRecord[]> baojiaRecords = (List<BaojiaRecord[]>) results.get("baojiaRecords");
		List<BaojiaRecord> records = new ArrayList<BaojiaRecord>();
		if (CollectionUtils.isNotEmpty(baojiaRecords)) {
			List<String> carIds = new ArrayList<>();
			for (Object[] object : baojiaRecords) {
				BaojiaRecord record = new BaojiaRecord();
				if (object[0] != null) {
					record.setCarId((String) object[0]);
					carIds.add((String) object[0]);
				}
				if (object[1] != null) {
					record.setVehicleNumber((String) object[1]);
				}
				if (object[2] != null) {
					record.setProvinceName((String) object[2]);
				}
				if (object[3] != null) {
					record.setCityName((String) object[3]);
				}
				if (object[4] != null) {
					record.setOwner((String) object[4]);
				}
				if (object[5] != null) {
					record.setPhone((String) object[5]);
				}
				if (object[6] != null) {
					Date lastYearEndDate = (Date) object[6];
					record.setLastYearEndDate(lastYearEndDate);
				}
				if (object[7] != null) {
					Date lastYearCIEndDate = (Date) object[7];
					record.setLastYearCIEndDate(lastYearCIEndDate);
				}
				if (object[8] != null) {
					record.setQueryCount((int) object[8]);
				}
				if (object[9] != null) {
					record.setQueryDate((Date) object[9]);
				}
				if (object[10] != null) {
					record.setSource((int) object[10]);
				}
				record.setResult("");
				records.add(record);
			}

			Map<String, Date> bInsuranceEndDateMap = carService.getbInsuranceEndDateByIds(carIds);
			for (BaojiaRecord record : records) {
				Date lastYearEndDate = bInsuranceEndDateMap.get(record.getCarId());
				if (lastYearEndDate != null) {
					record.setEndDays(caculateEndDays(lastYearEndDate));
				}
			}
		}
		ModelMap mv = new ModelMap("retcode", 0);
		mv.addAttribute("totalItems", results.get("totalItems"));
		mv.addAttribute("totalPages", results.get("totalPages"));
		mv.addAttribute("baojiaRecords", records);
		return mv;
	}

	@RequestMapping(value = "query/price/{carId}", method = RequestMethod.GET)
	public ModelAndView preQueryPrice(@PathVariable String carId) {
		Car car = carService.findCar(carId);
		ModelMap mm = new ModelMap();
		if (car != null) {
			mm.put("guohu", car.isGuohu() ? "是" : "否");
			mm.put("usedYears", caculateUsedYears(car.getEnrollDate()));
			mm.put("cityName", cityService.getByCityCode(car.getCityCode()).getName());
			mm.put("car", car);

			try {
				HttpPost post = new HttpPost(rootURL + "/api/v1/cars/configmodel");
				List<NameValuePair> pairList = new ArrayList<>();
				NameValuePair pair = new BasicNameValuePair("modelName", car.getModelName());
				pairList.add(pair);

				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
				post.setEntity(entity);
				post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
				CloseableHttpResponse response = httpService.execute(post);
				if (response != null) {
					String result = EntityUtils.toString(response.getEntity());
					if (StringUtils.isNotEmpty(result)) {
						ObjectMapper om = new ObjectMapper();
						VehicleConfigModel[] models = om.readValue(result, VehicleConfigModel[].class);
						mm.put("models", models);
					}
					response.close();
				}
			} catch (Exception e) {

			}
			City city = cityService.getByCityCode(car.getCityCode());
			Map<Integer, String> provinces = cityService.getAllProvince();
			mm.put("provinces", provinces);
			if (city != null) {
				mm.put("provinceId", city.getProvinceCode());
			}
		}

		return new ModelAndView("insurance/baojia", mm);
	}

	@RequestMapping(value = "query/price/{carId}", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap queryPrice(@PathVariable String carId, HttpServletRequest request) throws IOException {
		QueryPriceCondition condition = new QueryPriceCondition();
		condition.setCarId(carId);

		List<Insurance> insurances = new ArrayList<Insurance>();
		Insurance insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter("A")));
		insurance.setRiskKindCode("A");
		insurance.setRiskCode("0505");
		insurance.setName("机动车损失保险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (StringUtils.equals("1", request.getParameter("A"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MA")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MA");
		insurance.setRiskCode("0505");
		insurance.setName("车损不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(!StringUtils.equals("0", request.getParameter("B")));
		insurance.setRiskKindCode("B");
		insurance.setAmount(Float.parseFloat(request.getParameter("B")) * 10000);
		insurance.setRiskCode("0505");
		insurance.setName("第三者责任保险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (!StringUtils.equals("0", request.getParameter("B"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MB")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MB");
		insurance.setRiskCode("0505");
		insurance.setName("三者不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter("G")));
		insurance.setRiskKindCode("G");
		insurance.setRiskCode("0505");
		insurance.setName("机动车盗抢保险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (StringUtils.equals("1", request.getParameter("G"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MG")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MG");
		insurance.setRiskCode("0505");
		insurance.setName("全车盗抢不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(!StringUtils.equals("0", request.getParameter("D3")));
		insurance.setRiskKindCode("D3");
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter("D3")) * 10000);
		insurance.setName("车上人员责任险司机座位");
		insurances.add(insurance);

		insurance = new Insurance();
		if (!StringUtils.equals("0", request.getParameter("D3"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MD3")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MD3");
		insurance.setRiskCode("0505");
		insurance.setName("车上人员责任司机不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(!StringUtils.equals("0", request.getParameter("D4")));
		insurance.setRiskKindCode("D4");
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter("D4")) * 10000);
		insurance.setName("车上人员责任险乘客座位");
		insurances.add(insurance);

		insurance = new Insurance();
		if (!StringUtils.equals("0", request.getParameter("D4"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MD4")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MD4");
		insurance.setRiskCode("0505");
		insurance.setName("车上人员责任乘客不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(!StringUtils.equals("0", request.getParameter("F")));
		insurance.setRiskKindCode("F");
		insurance.setRiskCode("0505");
		insurance.setGlassType(Integer.parseInt(request.getParameter("F")));
		insurance.setName("玻璃单独破碎险");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(!StringUtils.equals("0", request.getParameter("L")));
		insurance.setRiskKindCode("L");
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter("L")));
		insurance.setName("车身划痕损失险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (!StringUtils.equals("0", request.getParameter("L"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("ML")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("ML");
		insurance.setRiskCode("0505");
		insurance.setName("车身划痕不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter("Z")));
		insurance.setRiskKindCode("Z");
		insurance.setRiskCode("0505");
		insurance.setName("自燃损失险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (StringUtils.equals("1", request.getParameter("Z"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MZ")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MZ");
		insurance.setRiskCode("0505");
		insurance.setName("自燃损失不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter("X1")));
		insurance.setRiskKindCode("X1");
		insurance.setRiskCode("0505");
		insurance.setName("发动机特别损失险");
		insurances.add(insurance);

		insurance = new Insurance();
		if (StringUtils.equals("1", request.getParameter("X1"))) {
			insurance.setBuy(StringUtils.equals("on", request.getParameter("MX1")));
		} else {
			insurance.setBuy(false);
		}
		insurance.setRiskKindCode("MX1");
		insurance.setRiskCode("0505");
		insurance.setName("发动机特别损失不计免赔");
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(true);
		insurance.setRiskKindCode("BZ");
		insurance.setRiskCode("0594");
		insurance.setName("机动车交通事故责任强制保险");
		insurances.add(insurance);

		condition.setInsuranceList(insurances);

		ModelMap mm = new ModelMap();
		HttpPost post = new HttpPost(rootURL + "/api/v1/insurance/baojia");
		ObjectMapper om = new ObjectMapper();
		StringEntity entity = new StringEntity(om.writeValueAsString(condition), "UTF-8");
		entity.setContentType("application/json; charset=utf-8");// 发送json数据需要设置contentType
		post.setEntity(entity);
		post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(post);
		if (response != null) {
			int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.ACCEPTED.value()) {
				mm.put("result", "success");
			} else {
				mm.put("result", "failed");
			}
			response.close();
		} else {
			mm.put("result", "failed");
		}

		return mm;
	}

	@RequestMapping(value = "{carId}/result", method = RequestMethod.GET)
	@ResponseBody
	public InquiryResult[] getPriceList(@PathVariable String carId) throws Exception {
		HttpGet get = new HttpGet(rootURL + "/api/v1/insurance/baojia/" + carId + "/result");
		get.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(get);
		if (response != null) {
			String result = EntityUtils.toString(response.getEntity());
			response.close();

			if (StringUtils.isNotEmpty(result)) {
				ObjectMapper om = new ObjectMapper();
				InquiryResult[] results = om.readValue(result, InquiryResult[].class);
				return results;
			}
		}

		return null;
	}

	/**
	 * 进入提交保单页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/enter/{carId}/{iCCode}/submit/{submitType}", method = RequestMethod.POST)
	public ModelAndView enterSubmitOrder(@PathVariable String carId, @PathVariable int iCCode, @PathVariable String submitType) throws Exception {
		ModelAndView mv = new ModelAndView("insurance/policySubmit");
		Car car = carService.findCar(carId);
		if (StringUtils.isNoneEmpty(car.getMerchantCode())) {
			Merchant merchant = vehicleMerchantService.findMerchantById(car.getMerchantCode());
			mv.addObject("merchantAddress", merchant.getAddress());
		}
		mv.addObject("car", car);
		mv.addObject("iCCode", iCCode);
		mv.addObject("icName", ICCode.getICNameByCode(iCCode));
		City city = cityService.getByCityCode(car.getCityCode());
		Map<Integer, String> provinces = cityService.getAllProvince();
		mv.addObject("provinces", provinces);
		if (city != null) {
			mv.addObject("cityName", city.getName());
			mv.addObject("provinceId", city.getProvinceCode());
			mv.addObject("cityCode", city.getCityCode());
		}
		mv.addObject("submitType", submitType);
		return mv;
	}

	/**
	 * 提交保单
	 */
	@RequestMapping(value = "/submit", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap submitOrder(SubmitPolicyFormBean bean, HttpSession session) throws Exception {
		Car car = carService.findCar(bean.getCarId());
		bean.setUserId(car.getUserId());

		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		if (StringUtils.equals(accountInfo.getRole().getName(), Role.OMS_HEBAO_USER)) {
			bean.setCustomerservice(accountInfo.getId());
		}

		HttpPost post = new HttpPost(rootURL + "/api/v1/insurance/submit/orders");
		ObjectMapper om = new ObjectMapper();
		StringEntity entity = new StringEntity(om.writeValueAsString(bean), "UTF-8");
		entity.setContentType("application/json; charset=utf-8");// 发送json数据需要设置contentType
		post.setEntity(entity);
		post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(post);
		if (response != null) {
			String result = EntityUtils.toString(response.getEntity());
			response.close();
			if (StringUtils.isNotEmpty(result)) {
				ModelMap mm = om.readValue(result, ModelMap.class);
				return mm;
			}
		}
		return null;
	}

	private int caculateEndDays(Date enrollDate) {
		Calendar start = Calendar.getInstance();
		Calendar current = Calendar.getInstance();
		start.setTime(enrollDate);
		int dayFrom = start.get(Calendar.DAY_OF_YEAR);
		int dayTo = current.get(Calendar.DAY_OF_YEAR);
		int days = Math.abs(dayTo - dayFrom);
		if (current.get(Calendar.MONTH) > start.get(Calendar.MONTH)) {
			days = 365 - days;
		} else if (current.get(Calendar.MONTH) == start.get(Calendar.MONTH)) {
			if (current.get(Calendar.DAY_OF_MONTH) >= start.get(Calendar.DAY_OF_MONTH)) {
				days = 365 - days;
			}
		}
		return days;
	}

	private int caculateUsedYears(Date startDate) {
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		int startYear = start.get(Calendar.YEAR);
		Calendar current = Calendar.getInstance();
		int endYear = current.get(Calendar.YEAR);
		return endYear - startYear;
	}

	/**
	 * 报价详情
	 * 
	 * @return
	 */
	@RequestMapping(value = "handle", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap handleResult(String carId, String result, int feedback, HttpSession session) {
		ModelMap modelMap = new ModelMap("retcode", 0);
		if (StringUtils.isNotEmpty(result)) {
			AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
			String userName = accountInfo.getUserName();
			result += "@<font color='#3954F7'>" + userName + "</font>";
		}
		baojiaRecordService.updateHandleResult(carId, result, feedback);
		return modelMap;
	}

	@RequestMapping(value = "show/{carId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap showHandleResult(@PathVariable("carId") String carId) {
		ModelMap modelMap = new ModelMap("retcode", 0);
		BaojiaRecord baojiaRecord = baojiaRecordService.getHandleResult(carId);
		modelMap.put("feedback", baojiaRecord.getFeedback());
		return modelMap;
	}

	/**
	 * 报价详情
	 * 
	 * @return
	 */
	@RequestMapping(value = "{carId}/detail", method = RequestMethod.GET)
	public ModelAndView baojiaDetail(@PathVariable("carId") String carId) {
		ModelMap mm = new ModelMap();
		Car car = carService.findCar(carId);
		mm.put("car", car);
		
		BaojiaRecord baojiaRecord = baojiaRecordService.getBaojiaRecordByCardId(carId);
		mm.put("cityName", baojiaRecord.getCityName());
		mm.put("phone", baojiaRecord.getPhone());
		mm.put("guohu", car.isGuohu() ? "是" : "否");
		mm.put("usedYears", caculateUsedYears(car.getEnrollDate()));
		mm.put("result", baojiaRecord.getResult());
		if (baojiaRecord != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				if (baojiaRecord.getCCIC() != null) { // CCIC, 大地
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getCCIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("CCIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("CCIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("CCIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("CCIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("CCIC_M_TOTAL", m_total.doubleValue());
								mm.put("CCIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getCICP() != null) { // CICP, 中华
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getCICP(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("CICP", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("CICP_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("CICP_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("CICP_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("CICP_M_TOTAL", m_total.doubleValue());
								mm.put("CICP_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getCPIC() != null) { // CPIC, 太平洋保险
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getCPIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("CPIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("CPIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("CPIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("CPIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("CPIC_M_TOTAL", m_total.doubleValue());
								mm.put("CPIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getDBIC() != null) { // DBIC, 都邦
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getDBIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("DBIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("DBIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("DBIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("DBIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("DBIC_M_TOTAL", m_total.doubleValue());
								mm.put("DBIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getGPIC() != null) { // GPIC, 中国人寿
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getGPIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("GPIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("GPIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("GPIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("GPIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));

								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("GPIC_M_TOTAL", m_total.doubleValue());
								mm.put("GPIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getPAIC() != null) { // PAIC, 平安
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getPAIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("PAIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("PAIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("PAIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("PAIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("PAIC_M_TOTAL", m_total.doubleValue());
								mm.put("PAIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getPICC() != null) { // PICC, 人保
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getPICC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("PICC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("PICC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("PICC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("PICC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("PICC_M_TOTAL", m_total.doubleValue());
								mm.put("PICC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getTPIC() != null) { // TPIC, 太平保险
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getTPIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("TPIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("TPIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("TPIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("TPIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("TPIC_M_TOTAL", m_total.doubleValue());
								mm.put("TPIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getYGBX() != null) { // YGBX, 阳光保险
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getYGBX(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("YGBX", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("YGBX_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("YGBX_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("YGBX_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0);
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("YGBX_M_TOTAL", m_total.doubleValue());
								mm.put("YGBX_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
				if (baojiaRecord.getZKIC() != null) { // ZKIC, 紫金保险
					InquiryResult inquiryResult = mapper.readValue(baojiaRecord.getZKIC(), InquiryResult.class);
					if (inquiryResult != null) {
						List<Insurance> insuranceList = inquiryResult.getInsuranceList();
						mm.put("ZKIC", insuranceList);
						if (inquiryResult.getCarShipTaxInfo() != null) {
							mm.put("ZKIC_CST", inquiryResult.getCarShipTaxInfo().getCarShipTax());
						}
						if (inquiryResult.getcInsurance() != null) {
							mm.put("ZKIC_BZ_Premium", inquiryResult.getcInsurance().getSumPremium());
						}
						mm.put("ZKIC_totalPremium", inquiryResult.getTotalPremium());
						// 统计不计免赔投保总金额
						if (CollectionUtils.isNotEmpty(insuranceList)) {
							BigDecimal b_total = new BigDecimal(0); // 商业险
							for (Insurance insurance : insuranceList) {
								if ("A".equals(insurance.getRiskKindCode()) || "B".equals(insurance.getRiskKindCode())
										|| "Z".equals(insurance.getRiskKindCode()) || "X1".equals(insurance.getRiskKindCode())
										|| "D3".equals(insurance.getRiskKindCode()) || "D4".equals(insurance.getRiskKindCode())
										|| "G".equals(insurance.getRiskKindCode()) || "L".equals(insurance.getRiskKindCode())
										|| "F".equals(insurance.getRiskKindCode())) {
									b_total = b_total.add(new BigDecimal(insurance.getPremium() + ""));
								}
								if ("B".equals(insurance.getRiskKindCode())) {
									mm.put("B_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D3".equals(insurance.getRiskKindCode())) {
									mm.put("D3_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("D4".equals(insurance.getRiskKindCode())) {
									mm.put("D4_amount", "(" + insurance.getAmount() / 10000 + "万)");
								}
								if ("L".equals(insurance.getRiskKindCode())) {
									mm.put("L_amount", "(" + insurance.getAmount() + ")");
								}
								if ("F".equals(insurance.getRiskKindCode())) {
									mm.put("F_TYPE", insurance.getGlassType() == 1 ? "(国产玻璃)" : "(进口玻璃)");
								}
							}
							if (inquiryResult.getbInsurance() != null) {
								BigDecimal total = new BigDecimal(inquiryResult.getbInsurance().getSumPremium() + "");
								BigDecimal m_total = total.subtract(b_total);
								mm.put("ZKIC_M_TOTAL", m_total.doubleValue());
								mm.put("ZKIC_B_TOTAL", total.doubleValue());
							}
						}
					}
				}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			List<ClaimHistory> claimHistoryList = carService.getClaimHistoryByCarId(baojiaRecord.getCarId());
			Iterator<ClaimHistory> it = claimHistoryList.iterator();
			while (it.hasNext()) {
				ClaimHistory claim = it.next();
				if (claim.getLossFee() == 0) {
					it.remove();
				}
			}
			mm.put("claimHistoryList", claimHistoryList);
		}
		return new ModelAndView("insurance/baojiaDetail", mm);
	}

	/**
	 * 发送短链接报价给渠道商和相应的sales
	 */
	@RequestMapping(value = "send/{carId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap sendMessage(@PathVariable String carId) throws IOException {
		ModelMap mm = new ModelMap();
		BaojiaRecord record = baojiaRecordService.getBaojiaRecordByCardId(carId);
		if (record == null) {
			mm.put("result", "failed");
			return mm;
		}
		Car car = carService.findCar(carId);
		if (car == null) {
			mm.put("result", "failed");
			return mm;
		}
		String url = "http://api.ykcare.cn/wx/insurance/baojia/record/" + carId + "/" + car.getUserId();
		String tinyUrl = DWZUtils.generate(url);

		// 1. 发短信给渠道商
		boolean result = smsService.send(SmsTemplateId.BAOJIA_TINY_URL, car.getPhone(), new String[] { car.getNumber(), tinyUrl });

		Merchant merchant = vehicleMerchantService.findMerchantById(car.getMerchantCode());
		if (merchant != null) {
			OmsUser account = accountService.findByName(merchant.getSalesman());
			// 发短信给相应的sales
			smsService.send(SmsTemplateId.SHORT_URL_NOTIFY_BD, account.getPhone(), new String[] { account.getName(), merchant.getName(), merchant.getManager(),
					merchant.getManagerPhone(), car.getNumber(), car.getOwner(), car.getModelDescr(), tinyUrl });
		}
		if (result) {
			mm.put("result", "success");
		} else {
			mm.put("result", "failed");
		}
		return mm;
	}

	private static Map<String, Date> calQueryDate(int dateType) {
		Map<String, Date> dateMap = new HashMap<String, Date>();
		Calendar cal = Calendar.getInstance();
		if (dateType == 1) { // 今天
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.SECOND, -1);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		} else if (dateType == 2) { // 昨天
			cal.add(Calendar.DAY_OF_MONTH, -1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			cal.add(Calendar.SECOND, -1);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		} else if (dateType == 3) {// 最近7天
			cal.add(Calendar.DAY_OF_MONTH, -6);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		} else if (dateType == 4) {// 最近15天
			cal.add(Calendar.DAY_OF_MONTH, -14);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		} else if (dateType == 5) {// 最近30天
			cal.add(Calendar.DAY_OF_MONTH, -29);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		} else if (dateType == 6) {// 最近90天
			cal.add(Calendar.DAY_OF_MONTH, -89);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date startDate = cal.getTime();
			dateMap.put("startDate", startDate);
			cal.setTime(new Date());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			Date endDate = cal.getTime();
			dateMap.put("endDate", endDate);
		}
		return dateMap;
	}
	
	/**
	 * 强行提交保单
	 */
	@RequestMapping(value="/force/submit", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap forceSubmitPolicy(ForceSubmitPolicyFormBean bean) throws Exception {
		ModelMap modelMap = new ModelMap("errcode",0);
		baojiaRecordService.forceSumbitPolicy(bean);
		return modelMap;
	}
}

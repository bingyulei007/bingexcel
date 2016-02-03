/**
 * 
 */
package com.jt.ycl.oms.car;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.ErrorCode;
import com.jt.core.insurance.InquiryResult;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.ClaimHistory;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.VehicleConfigModel;
import com.jt.core.model.VehicleLicense;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.utils.HttpService;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.insurance.PolicyService;

/**
 * @author wuqh
 */
@Controller
@RequestMapping(value = { "/vehicle" })
@ResponseBody
@OMSPermission(permission = Permission.CAR_MGMT)
public class VehicleController {
	@Autowired
	private PolicyService policyService;
	@Autowired
	private CarService carService;

	@Autowired
	private CityService cityService;

	private HttpService httpService = HttpService.getInstance();

	private String rootURL = "http://127.0.0.1:6060/apigateway";

	@PostConstruct
	public void init() {
		if (SystemUtils.IS_OS_LINUX) {
			rootURL = "http://api.ykcare.cn/apigateway";
		}
	}

	/**
	 * 进入车辆管理首页
	 */
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public ModelAndView query() {
		ModelAndView mv = new ModelAndView("/vehicle/vehicle");
		return mv;
	}

	@RequestMapping(value = "/query/condition", method = RequestMethod.POST)
	public ModelMap queryByConditon(String vehicleNumber, int cityCode, int month, int pageNumber, int pageSize) {
		ModelMap mv = new ModelMap("retcode", 0);
		// 如果使用车牌号查询，另外两个条件不生效
		if (StringUtils.isNotEmpty(vehicleNumber)) {
			cityCode = 0;
			month = 0;
		}
		Object[] results = carService.findByMonth(vehicleNumber, cityCode, month, pageNumber, pageSize);
		mv.addAttribute("cars", results[2]);
		mv.addAttribute("totalPages", results[1]);
		mv.addAttribute("totalItems", results[0]);
		return mv;
	}

	@RequestMapping(value = "{carId}/delete", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap delete(@PathVariable("carId") String carId) throws Exception {
		HttpGet get = new HttpGet(rootURL + "/api/v1/cars/delete/" + carId);
		get.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(get);
		String result = EntityUtils.toString(response.getEntity());
		response.close();
		ObjectMapper om = new ObjectMapper();
		ModelMap resultMap = om.readValue(result, ModelMap.class);
		return resultMap;
	}

	@RequestMapping(value = "car/{id}/edit/go", method = RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable("id") String id) {
		ModelAndView mv = new ModelAndView("vehicle/editVehicle");
		Car car = carService.findCar(id);
		List<VehicleConfigModel> vehicleConfigModelList = carService.findVehicleConfigModels(car.getModelName());
		City city = cityService.getByCityCode(car.getCityCode());
		Map<Integer, String> provinces = cityService.getAllProvince();
		mv.addObject("provinces", provinces);
		if (city != null) {
			mv.addObject("provinceId", city.getProvinceCode());
		}
		mv.addObject("vehicleConfigModelList", vehicleConfigModelList);
		mv.addObject("car", car);
		return mv;
	}

	@RequestMapping(value = "add", method = RequestMethod.GET)
	public ModelAndView openAddCarDlg(HttpSession session) {
		ModelAndView mv = new ModelAndView("vehicle/addVehicle");
		Map<Integer, String> provinces = cityService.getAllProvince();
		mv.addObject("provinces", provinces);
		return mv;
	}

	@RequestMapping(value = "number/fetch", method = RequestMethod.POST)
	public VehicleLicense getVehicleLicenseByNumber(String number) throws Exception {
		HttpPost post = new HttpPost(rootURL + "/api/v1/cars/vehicle/license");
		List<NameValuePair> pairList = new ArrayList<>();
		NameValuePair pair = new BasicNameValuePair("licenseNo", number);
		pairList.add(pair);

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
		post.setEntity(entity);
		post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		try (CloseableHttpResponse response = httpService.execute(post)) {
			if (response != null) {
				String result = EntityUtils.toString(response.getEntity());
				if (StringUtils.isNotEmpty(result)) {
					ObjectMapper om = new ObjectMapper();
					return om.readValue(result, VehicleLicense.class);
				}
			}
		}
		return null;
	}

	/**
	 * 添加新车辆
	 */
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public ModelMap addCar(VehicleFormBean vehicleFormBean, HttpSession session) throws Exception {
		ModelMap mm = new ModelMap();
		Car car = new Car();
		car.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		car.setMerchantCode(vehicleFormBean.getMerchantId());
		car.setNumber(vehicleFormBean.getNumber());
		car.setVehicleModelId(vehicleFormBean.getVehicleModelId());
		car.setVin(vehicleFormBean.getVin());
		car.setEngineNo(vehicleFormBean.getEngineNo());
		car.setCreateDate(new Date());
		car.setEnrollDate(DateUtils.convertStrToDate(vehicleFormBean.getEnrollDate(), "yyyy-MM-dd"));
		car.setGuohu(vehicleFormBean.isGuohu());
		if (car.isGuohu()) {
			if (StringUtils.isEmpty(vehicleFormBean.getMakeDate())) {
				throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "过户车辆的过户日期必填！");
			} else {
				car.setMakeDate(DateUtils.convertStrToDate(vehicleFormBean.getMakeDate(), "yyyy-MM-dd"));
			}
		}
		car.setOwner(vehicleFormBean.getOwner());
		car.setOwnerId(vehicleFormBean.getOwnerId());
		car.setModelName(vehicleFormBean.getModelName());
		car.setCompanyCar(vehicleFormBean.isCompanyCar());
		car.setCityCode(vehicleFormBean.getCityCode());

		carService.addCar(car, vehicleFormBean.getMerchantId());

		// 获取车辆的历史投保记录和出险理赔信息，并存入数据库
		HttpPost post = new HttpPost(rootURL + "/api/v1/lp/" + car.getId() + "/" + car.getVin() + "/query?refresh=true");
		try {
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(3 * 1000).setSocketTimeout(20 * 1000).setRedirectsEnabled(true)
					.setConnectionRequestTimeout(5 * 1000).setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).build();
			post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
			CloseableHttpResponse response = httpService.execute(post, requestConfig);
			response.close();
		} catch (Exception e) {
		}
		return mm;
	}

	@RequestMapping(value = "car/update", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap update(VehicleFormBean vehicleFormBean) throws JsonParseException, JsonMappingException, IOException {
		Car car = carService.findCar(vehicleFormBean.getId());
		if (car == null) {
			throw new CommonLogicException(ErrorCode.CAR_NOT_FOUND, vehicleFormBean.getNumber() + "不存在，请刷新页面！");
		}
		car.setMerchantCode(vehicleFormBean.getMerchantId());
		if(!car.getNumber().equals(vehicleFormBean.getNumber())){
			car.setNumber(vehicleFormBean.getNumber());
			//更新carNum时候，也要同步更新保单中内容。
			List<InsurancePolicy> policyList = policyService.getInsurancePolicyByCarId(car.getId());
			if(policyList!=null && policyList.size()>0){
				for (InsurancePolicy insurancePolicy : policyList) {
					insurancePolicy.setCarNumber(car.getNumber());
					ObjectMapper mapper = new ObjectMapper();
					InquiryResult inquiryResult;
					
						if (StringUtils.isNotBlank(insurancePolicy.getContent())) {
							inquiryResult = mapper.readValue(insurancePolicy.getContent(), InquiryResult.class);
							String newContent = mapper.writeValueAsString(inquiryResult);
							insurancePolicy.setCarNumber(car.getNumber());
							insurancePolicy.setContent(newContent);
							policyService.updateInsurancePolicy(insurancePolicy);
						}
					
				
				
				}
			}
		}
		car.setCityCode(vehicleFormBean.getCityCode());
		car.setVin(vehicleFormBean.getVin());
		car.setEngineNo(vehicleFormBean.getEngineNo());
		car.setEnrollDate(DateUtils.convertStrToDate(vehicleFormBean.getEnrollDate(), "yyyy-MM-dd"));
		car.setModelName(vehicleFormBean.getModelName());
		car.setVehicleModelId(vehicleFormBean.getVehicleModelId());
		car.setOwner(vehicleFormBean.getOwner());
		car.setOwnerId(vehicleFormBean.getOwnerId());
		car.setGuohu(vehicleFormBean.isGuohu());

		if (car.isGuohu()) {
			if (StringUtils.isEmpty(vehicleFormBean.getMakeDate())) {
				throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "过户车辆的过户日期必填！");
			} else {
				car.setMakeDate(DateUtils.convertStrToDate(vehicleFormBean.getMakeDate(), "yyyy-MM-dd"));
			}
		} else {
			car.setMakeDate(null);
		}

		car.setCompanyCar(vehicleFormBean.isCompanyCar());
		if (StringUtils.isNotEmpty(vehicleFormBean.getLastYearEndDate())) {
			car.setLastYearEndDate(DateUtils.convertStrToDate(vehicleFormBean.getLastYearEndDate(), "yyyy-MM-dd"));
		}
		if (StringUtils.isNotEmpty(vehicleFormBean.getLastYearCIEndDate())) {
			car.setLastYearCIEndDate(DateUtils.convertStrToDate(vehicleFormBean.getLastYearCIEndDate(), "yyyy-MM-dd"));
		}
		ModelMap mm = new ModelMap();
		Car newCar = carService.update(car);
		mm.put("result", "success");
		mm.put("car", newCar);
		return mm;
	}

	/**
	 * 更新车辆信息，坐席询价系统中修改车辆信息调用
	 */
	@RequestMapping(value = "car/update/{carId}", method = RequestMethod.POST)
	public ModelMap update(@PathVariable String carId, int vehicleModelId, int cityCode, int guohu, String makeDate) {
		ModelMap mm = new ModelMap();
		Car car = carService.findCar(carId);
		if (car != null) {
			car.setVehicleModelId(vehicleModelId);
			car.setCityCode(cityCode);
			if (guohu == 1) {
				car.setGuohu(true);
			} else {
				car.setGuohu(false);
			}
			if (car.isGuohu()) {
				car.setMakeDate(DateUtils.convertStrToDate(makeDate, "yyyy-MM-dd"));
			} else {
				car.setMakeDate(null);
			}

			carService.update(car);
			mm.put("result", "success");
		} else {
			mm.put("result", "failed");
		}
		return mm;
	}

	@RequestMapping(value = "{vin}/modelname", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap getVehicleModelName(@PathVariable("vin") String vinNo) throws Exception {
		HttpGet get = new HttpGet(rootURL + "/api/v1/cars/" + vinNo + "/modelname");
		get.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(get);
		String result = EntityUtils.toString(response.getEntity());
		response.close();

		ObjectMapper om = new ObjectMapper();
		ModelMap resultMap = om.readValue(result, ModelMap.class);
		return resultMap;
	}

	@RequestMapping(value = "configmodel", method = RequestMethod.POST)
	public VehicleConfigModel[] getConfigModelList(String modelName) throws Exception {
		HttpPost post = new HttpPost(rootURL + "/api/v1/cars/configmodel");
		List<NameValuePair> pairList = new ArrayList<>();
		NameValuePair pair = new BasicNameValuePair("modelName", modelName);
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
				return models;
			}
			response.close();
		}
		return null;
	}

	@RequestMapping(value = { "unique/configModel", "unique/configmodel" }, method = RequestMethod.POST)
	@ResponseBody
	public VehicleConfigModel getUniqueVehicleConfigModelFromPICCWebSite(String licenseNo, String vin, String engineNo, String modelName) throws Exception {
		HttpPost post = new HttpPost(rootURL + "/api/v1/cars/unique/configModel");
		List<NameValuePair> pairList = new ArrayList<>();
		NameValuePair pair = new BasicNameValuePair("modelName", modelName);
		pairList.add(pair);
		pair = new BasicNameValuePair("licenseNo", licenseNo);
		pairList.add(pair);
		pair = new BasicNameValuePair("vinNo", vin);
		pairList.add(pair);
		pair = new BasicNameValuePair("engineNo", engineNo);
		pairList.add(pair);

		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
		post.setEntity(entity);
		post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
		CloseableHttpResponse response = httpService.execute(post);
		if (response != null) {
			String result = EntityUtils.toString(response.getEntity());
			if (StringUtils.isNotEmpty(result)) {
				ObjectMapper om = new ObjectMapper();
				VehicleConfigModel model = om.readValue(result, VehicleConfigModel.class);
				return model;
			}
			response.close();
		}
		return null;
	}

	@RequestMapping(value = { "picc/configModel", "picc/configmodel" }, method = RequestMethod.POST)
	@ResponseBody
	public VehicleConfigModel[] getVehicleConfigModelFromPICCWebSite(String licenseNo, String vin, String engineNo, String modelName) throws Exception {
		HttpPost post = new HttpPost(rootURL + "/api/v1/cars/picc/configModel");
		List<NameValuePair> pairList = new ArrayList<>();
		NameValuePair pair = new BasicNameValuePair("modelName", modelName);
		pairList.add(pair);
		pair = new BasicNameValuePair("licenseNo", licenseNo);
		pairList.add(pair);
		pair = new BasicNameValuePair("vinNo", vin);
		pairList.add(pair);
		pair = new BasicNameValuePair("engineNo", engineNo);
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
				return models;
			}
			response.close();
		}
		return null;
	}

	/**
	 * 查询这辆车的理赔信息
	 */
	@RequestMapping(value = "lipei/{carId}/query", method = RequestMethod.GET)
	public ModelAndView queryLiPei(@PathVariable String carId) {
		Car car = carService.findCar(carId);
		ModelMap mm = new ModelMap("result", "success");
		if (car != null) {
			mm.put("carId", car.getId());
			try {
				HttpPost post = new HttpPost(rootURL + "/api/v1/lp/" + car.getVin() + "/query");
				post.addHeader("accessKey", "VNqqYYfUyC5EVjO6rIgIrFqYsuhmErD4");
				CloseableHttpResponse response = httpService.execute(post);
				if(response != null){
					String result = EntityUtils.toString(response.getEntity());
					if(StringUtils.isNotEmpty(result)){
						ObjectMapper om = new ObjectMapper();
						JavaType javaType = om.getTypeFactory().constructParametricType(List.class, ClaimHistory.class);
						List<ClaimHistory> list = om.readValue(result, javaType);
						List<String> bzEndDateList = new ArrayList<>();
						List<String> jqEndDateList = new ArrayList<>();
						
						if(CollectionUtils.isNotEmpty(list)){
							for(ClaimHistory history : list){
								if("商业险".equals(history.getRisktype())) {
									//这是一条商业险记录
									bzEndDateList.add(history.getEndTime());
								} else {
									//这是一条交强险记录
									jqEndDateList.add(history.getEndTime());
								}
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
						int endDays = 0;
						if(StringUtils.isNotEmpty(lastYearEndDate)){
							endDays = DateUtils.difference(new Date(), DateUtils.convertStrToDate(lastYearEndDate, "yyyy-MM-dd"));
						}
						int ciEndDays = 0;
						if(StringUtils.isNotEmpty(lastYearCIEndDate)){
							ciEndDays = DateUtils.difference(new Date(), DateUtils.convertStrToDate(lastYearCIEndDate, "yyyy-MM-dd"));
						}
						mm.put("endDays", endDays);
						mm.put("ciEndDays", ciEndDays);
						mm.put("lastYearEndDate", lastYearEndDate);
						mm.put("lastYearCIEndDate", lastYearCIEndDate);
						mm.put("claimHistoryList", list);
						mm.put("result", "success");
					}
					response.close();
				}
			} catch (Exception ex) {
				mm.put("result", "failed");
			}
		}
		return new ModelAndView("vehicle/lipeiRecord", mm);
	}
}
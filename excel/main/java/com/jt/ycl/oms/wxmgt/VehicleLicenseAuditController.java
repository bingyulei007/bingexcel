package com.jt.ycl.oms.wxmgt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.ErrorCode;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.User;
import com.jt.core.model.VehicleLicenseAudit;
import com.jt.exception.CommonLogicException;
import com.jt.utils.HttpService;
import com.jt.ycl.oms.account.UserService;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.car.CarService;
import com.jt.ycl.oms.city.CityService;

/**
 * 行驶证人工审核
 * @author xiaojiapeng
 *
 */
@Controller
@RequestMapping(value="wxmgt")
@OMSPermission(permission = Permission.LICENSE_AUDIT_MGMT)
public class VehicleLicenseAuditController {
	
	@Autowired
	private VehicleLicenseAuditService auditService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CarService carService;
	
	@Autowired
	private CityService cityService;
	
	private HttpService httpService = HttpService.getInstance();
	
	@RequestMapping(value="audit",method=RequestMethod.GET)
	public ModelAndView audit(){
		VehicleLicenseAudit audit = auditService.getOneVehicleLicense();
		int count = auditService.countByStatus(1);
		ModelMap mm = new ModelMap();
		mm.put("audit", audit);
		mm.put("count", count);
		if(audit!=null){
			Car car = carService.findCar(audit.getCarId());
			if(car != null){
				mm.put("number", car.getNumber());
				List<String> info = preFetchVehicleLicense(car.getNumber());
				if(CollectionUtils.isNotEmpty(info)) {
					mm.put("vinNo", info.get(0));
					mm.put("modelName", info.get(1));
					mm.put("engineNo", info.get(2));
					mm.put("enroll", info.get(3));
					mm.put("ownerName", info.get(4));
				}
			}
		}
		return new ModelAndView("wxmgt/audit", mm);
	}
	
	@RequestMapping(value="audit",method=RequestMethod.POST)
	public String audit(HttpServletRequest request){
		String carId = request.getParameter("carId");
		String owner = request.getParameter("owner"); 
		String number = request.getParameter("number");
		String vin = request.getParameter("vin");
		String engineNo = request.getParameter("engineNo");
		String modelName = request.getParameter("modelName");
		String registerDate = request.getParameter("registerDate");
		String userId = request.getParameter("userId");
		String guohuStr = request.getParameter("guohu");
		String makeDate = request.getParameter("makeDate");
		String auditId = request.getParameter("auditId");
		String companyCarStr = request.getParameter("companyCar");
		
		if(StringUtils.isBlank(owner) || StringUtils.isBlank(number) || StringUtils.isBlank(vin) || StringUtils.isBlank(engineNo)
				|| StringUtils.isBlank(modelName) || StringUtils.isBlank(registerDate)){
			return "redirect:/wxmgt/audit";
		}
		User user = userService.getUserById(userId);
		if(user==null){
			throw new CommonLogicException(ErrorCode.USER_NOT_FOUND, "车辆不存在");
		}
		
		boolean guohu = false;
		if(StringUtils.equals(guohuStr, "1")){
			guohu = true;
		}
		boolean companyCar = false;
		if(StringUtils.equals(companyCarStr, "1")){
			companyCar = true;
		}
		Car car = carService.updateCar(carId, owner, number, vin, engineNo, modelName, registerDate, userId, guohu, makeDate, companyCar);
		if(car != null){
			auditService.updateStatusSuccess(Long.parseLong(auditId));
		}
		
		return "redirect:/wxmgt/audit";
	}
	
	@RequestMapping(value="audit/{vin}/modelname",method=RequestMethod.GET)
	@ResponseBody
	public ModelMap getVehicleModelName(@PathVariable("vin")String vinNo) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		
		List<NameValuePair> pairList = new ArrayList<>();
		pairList.add(new BasicNameValuePair("q", ""));	
		pairList.add(new BasicNameValuePair("limit", "0"));
		pairList.add(new BasicNameValuePair("timestamp", String.valueOf(System.currentTimeMillis())));
		pairList.add(new BasicNameValuePair("frameNo", vinNo));
		pairList.add(new BasicNameValuePair("queryVehicle", ""));
		pairList.add(new BasicNameValuePair("frameNoFlag", "true"));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
		HttpPost vehicleModelPost = new HttpPost("http://chexian.sinosig.com/Net/vehicleStandard.action");
		vehicleModelPost.setEntity(entity);
		CloseableHttpResponse response = client.execute(vehicleModelPost);
		String responseJson = EntityUtils.toString(response.getEntity(), "UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> valueMap = mapper.readValue(responseJson, Map.class);
		List<Map<String, Object>> contentList = (List<Map<String, Object>>) valueMap.get("content");
		ModelMap resultMap = new ModelMap();
		for(Map<String, Object> content : contentList) {
			String standardName = content.get("standardName").toString();
			String vehicleFgwCode = content.get("vehicleFgwCode").toString();
			if(StringUtils.isNotEmpty(standardName) && StringUtils.isNotEmpty(vehicleFgwCode)) {
				resultMap.put(standardName, vehicleFgwCode);
			}
		}
		return resultMap;
	}
	
	@RequestMapping(value="audit/{auditId}/failed",method=RequestMethod.POST)
	public String auditFailed(@PathVariable("auditId")long auditId, String errmsg){
		auditService.updateStatusFailed(auditId, errmsg);
		return "redirect:/wxmgt/audit";
	}
	
	@RequestMapping(value="audit/list",method=RequestMethod.GET)
	@OMSPermission(permission=Permission.LICENSE_AUDIT_RECORD_MGMT)
	public ModelAndView preList(){
		return new ModelAndView("wxmgt/auditList");
	}
	
	@RequestMapping(value="audit/list",method=RequestMethod.POST)
	@ResponseBody
	@OMSPermission(permission=Permission.LICENSE_AUDIT_RECORD_MGMT)
	public ModelMap auditList(int pageNumber, int pageSize){
		Page<VehicleLicenseAudit> page = auditService.list(pageNumber, pageSize);
		ModelMap mm = new ModelMap();
		mm.addAttribute("retcode", "0");
		mm.addAttribute("audits", page.getContent());
		mm.addAttribute("totalPages", page.getSize());
		mm.addAttribute("totalItems", page.getTotalElements());
		return mm;
	}
	
	@RequestMapping(value="audit/cardetail/{carId}",method=RequestMethod.GET)
	@OMSPermission(permission=Permission.LICENSE_AUDIT_RECORD_MGMT)
	public ModelAndView carDetail(@PathVariable() String carId){
		ModelMap mm = new ModelMap();
		Car car = carService.findCar(carId);
		mm.put("car", car);
		if(car != null){
			if(car.getEnrollDate() != null){
				mm.put("usedYears", caculateUsedYears(car.getEnrollDate()));
			}
			mm.put("guohu", car.isGuohu()?"是":"否");
			City city = cityService.getByCityCode(car.getCityCode());
			mm.put("cityName", city.getName());
		}
		return new ModelAndView("wxmgt/carDetail", mm);
	}
	
	private int caculateUsedYears(Date startDate){
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		int startYear = start.get(Calendar.YEAR);
		Calendar current = Calendar.getInstance();
		int endYear = current.get(Calendar.YEAR);
		return endYear - startYear;
	}
	
	private List<String> preFetchVehicleLicense(String carNumber) {
		try {
			//1. http://chexian.sinosig.com/NetCar/NewInsurance.shtml
			HttpGet newInsuranceGet = new HttpGet("http://chexian.sinosig.com/NetCar/NewInsurance.shtml");
			CloseableHttpResponse response = httpService.execute(newInsuranceGet);
		    response.close();
			        
			//2. 获取Token
		  	HttpGet get = new HttpGet("http://chexian.sinosig.com/Net/nCitySureAction.action?agentId=&agentName=&agentMobile=&accountName=&city=&cityCode=&licenseNum=&"
		  				+ "carOwnerMobile=&comCode=&areaCode=W00110002&paraMap.cno=&paraMap.areaCode=W00110002&paraMap.id=&paraMap.spsource=NET&"
		  				+ "paraMap.agentCode=W00110002&paraMap.tmUserCode=&paraMap.tmdes=&paraMap.simpleChannel=&paraMap.premiumMin=&paraMap.unid=&paraMap.exxFusion=");
		  		
		  	response = httpService.execute(get);
	  		String result = EntityUtils.toString(response.getEntity(), "UTF-8");
	  		response.close();
	  		/*
	  		 * <input type="hidden" name="paraMap.token" value="1516" id="step_2_form_paraMap_token"/> 
	  		 * <input type="hidden" name="paraMap.agentCode" value="W00110002" id="agentCode"/>
	  		 */
	  		Document htmlDoc = Jsoup.parse(result);
	  		String token = htmlDoc.select("input[name=paraMap.token]").val();
	  		String agentCode =  htmlDoc.select("input[name=paraMap.agentCode]").val();
	  		response.close();
			
	  		List<NameValuePair> pairList = new ArrayList<>();
				
			pairList.add(new BasicNameValuePair("paraMap.usedImmeValid", ""));
			pairList.add(new BasicNameValuePair("paraMap.immevalidPattern", ""));
			pairList.add(new BasicNameValuePair("paraMap.usedImmeValidHours", ""));
			pairList.add(new BasicNameValuePair("paraMap.isyingxiao", ""));
			pairList.add(new BasicNameValuePair("paraMap.simpleChannel", ""));
			pairList.add(new BasicNameValuePair("paraMap.hasKind", ""));
			pairList.add(new BasicNameValuePair("paraMap.id", ""));
			pairList.add(new BasicNameValuePair("paraMap.token", token));
			pairList.add(new BasicNameValuePair("paraMap.agentCode", agentCode));
			pairList.add(new BasicNameValuePair("paraMap.comCode", ""));
			pairList.add(new BasicNameValuePair("paraMap.purgeCode", ""));
			pairList.add(new BasicNameValuePair("paraMap.allowNewCar", ""));
			pairList.add(new BasicNameValuePair("paraMap.orgID", "03528000"));//州03528000
			pairList.add(new BasicNameValuePair("paraMap.fuzzyFlag", ""));
			pairList.add(new BasicNameValuePair("paraMap.backFlag", ""));
			pairList.add(new BasicNameValuePair("paraMap.premiumRepeatedly", ""));
			pairList.add(new BasicNameValuePair("paraMap.spsource", "NET"));
			pairList.add(new BasicNameValuePair("paraMap.redirectControl", "1"));
			pairList.add(new BasicNameValuePair("paraMap.dataBackFlag", "0"));
			pairList.add(new BasicNameValuePair("paraMap.tmUserCode", ""));
			pairList.add(new BasicNameValuePair("paraMap.cno", ""));
			pairList.add(new BasicNameValuePair("paraMap.pageId", "2"));
			pairList.add(new BasicNameValuePair("paraMap.partnerOrderNo", ""));
			pairList.add(new BasicNameValuePair("paraMap.buyerNick", ""));
			pairList.add(new BasicNameValuePair("paraMap.isRegist", "1"));
			pairList.add(new BasicNameValuePair("paraMap.buyerId", ""));
			pairList.add(new BasicNameValuePair("paraMap.auctionId", ""));
			pairList.add(new BasicNameValuePair("paraMap.auctionTitle", ""));
			pairList.add(new BasicNameValuePair("paraMap.promotionInfo", ""));
			pairList.add(new BasicNameValuePair("paraMap.modelNameSearch", ""));
			pairList.add(new BasicNameValuePair("paraMap.agreedDriver", ""));
			pairList.add(new BasicNameValuePair("paraMap.unid", ""));
			pairList.add(new BasicNameValuePair("paraMap.exxFusion", ""));
			pairList.add(new BasicNameValuePair("paraMap.orgName", "苏州市"));
			pairList.add(new BasicNameValuePair("initLicence", "苏E"));
			
			pairList.add(new BasicNameValuePair("paraMap.engineNo", ""));
			pairList.add(new BasicNameValuePair("paraMap.frameNo", ""));
			pairList.add(new BasicNameValuePair("paraMap.capitalAccount", ""));
			pairList.add(new BasicNameValuePair("paraMap.contactor", "张伟"));
			pairList.add(new BasicNameValuePair("paraMap.phone", "13111232232"));
			pairList.add(new BasicNameValuePair("paraMap.agentid", ""));
			pairList.add(new BasicNameValuePair("paraMap.email", "111@qq.com"));
			pairList.add(new BasicNameValuePair("paraMap.premiumType", "1"));
			pairList.add(new BasicNameValuePair("paraMap.price", ""));
			pairList.add(new BasicNameValuePair("paraMap.count", "0"));
			pairList.add(new BasicNameValuePair("paraMap.integralShow", "1"));
			
	  		pairList.add(new BasicNameValuePair("paraMap.licence", carNumber));
			HttpPost humanExactPost = new HttpPost("http://chexian.sinosig.com/Net/Human_exact.action");
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
			humanExactPost.setEntity(entity);
			response = httpService.execute(humanExactPost);
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
			response.close();
			htmlDoc = Jsoup.parse(result);
			
			String vinNo = htmlDoc.select("#frameNo").val();
			String modelName = htmlDoc.select("#queryVehicle").val();
			String engineNo = htmlDoc.select("#engineNo").val();
			String enroll = htmlDoc.select("#enroll").val();
			String ownerName = htmlDoc.select("#ownerName").val();
			String idno = htmlDoc.select("#idno").val();
			
			if(StringUtils.isNotBlank(vinNo)) {
				List<String> resultList = new ArrayList<>();
				resultList.add(vinNo);
				resultList.add(modelName);
				resultList.add(engineNo);
				resultList.add(enroll);
				resultList.add(ownerName);
				
				return resultList;
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
}
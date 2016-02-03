package com.jt.ycl.oms.merchant;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.model.Car;
import com.jt.core.model.MerchantBaoJiaRequest;
import com.jt.core.model.VehicleLicense;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;
import com.jt.ycl.oms.city.CityService;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value={"/merchant/baojia/request"})
@OMSPermission(permission = Permission.MERCHANT_MGMT)
public class MerchantBaoJiaRequestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MerchantBaoJiaRequestService merchantBaoJiaRequestService;
	
	@Autowired
	private CityService cityService;
	
	private final String accessKeyId = "qlo4BoLGXaAXU7FA";
	private final String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	
	OSSClient client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
	
	/**
	 * 进入商家报价请求
	 * @return
	 */
	@RequestMapping(value = "/overview", method = RequestMethod.GET)
	public ModelAndView query(HttpSession session){
		ModelAndView mv = new ModelAndView("/merchant/baojiaRequest");
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		Role role = accountInfo.getRole();
		if("bd-user".equals(role.getName())){ //DB用户进入商家报价请求页面时默认显示该DB维护的商家请求
			mv.addObject("dbusername", accountInfo.getUserName());
		}
		return mv;
	}
	
	/**
	 * 商家按条件查询
	 * @return
	 */
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap query(String carNumber, int state, int policyState, String salesman, String customerService,
			String merchantName, String startDate, String endDate, int pageNumber, int pageSize, HttpSession session){
		ModelMap mv = new ModelMap("retcode",0);
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		mv.addAttribute("username", accountInfo.getUserName());
		Page<MerchantBaoJiaRequest> page = merchantBaoJiaRequestService.query(carNumber, state, policyState, salesman, 
				customerService, merchantName, startDate, endDate, pageNumber, pageSize);
		mv.addAttribute("baojiaRequests", page.getContent());
		mv.addAttribute("totalPages", page.getTotalPages());
		mv.addAttribute("totalItems", page.getTotalElements());
		
		return mv;
	}
	
	/**
	 * 商家报价请求预处理
	 * @param requestId
	 * @param session
	 * @return
	 */
	@RequestMapping(value="/go/prehandle/{requestId}", method=RequestMethod.GET)
	@ResponseBody
	public ModelMap preHandle(@PathVariable int requestId,HttpSession session){
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		String username = accountInfo.getUserName();
		ModelMap mm = new ModelMap("retcode",0);
		MerchantBaoJiaRequest baoJiaRequest = merchantBaoJiaRequestService.getMerchantBaoJiaRequestById(requestId);
		if(baoJiaRequest == null){ //请求被删除或被作废
			mm.addAttribute("result", 0);
		}else{
			String handler = baoJiaRequest.getCustomerService();
			if(baoJiaRequest.getState()==2){ //
				mm.addAttribute("result", 1);
				mm.addAttribute("handler", handler);
			}else{ 
				merchantBaoJiaRequestService.updateStateById(requestId, username, 2); //2为已分配
			}
		}
		return mm;
	}
	
	@RequestMapping(value="/go/handle/{requestId}", method=RequestMethod.GET)
	public ModelAndView goHandle(@PathVariable int requestId){
		MerchantBaoJiaRequest baoJiaRequest = merchantBaoJiaRequestService.getMerchantBaoJiaRequestById(requestId);
		Map<Integer, String> provinces = cityService.getAllProvince();
		ModelAndView mv = new ModelAndView("/merchant/baojiaRequestHandle");
		mv.addObject("baoJiaRequest", baoJiaRequest);
		mv.addObject("provinces", provinces);
		return mv;
	}
	
	/**
	 * 获取行驶证信息
	 * @param carNumber
	 * @return
	 */
	@RequestMapping(value="/fetch/vehicle", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap fetchVehicleInfo(String carNumber){
		ModelMap mm = new ModelMap("retcode",0);
		VehicleLicense vehicleLicense = merchantBaoJiaRequestService.getVehicleLicenseByNumber(carNumber);
		mm.addAttribute("vehicleLicense", vehicleLicense);
		return mm;
	}
	
	@RequestMapping(value="/discard/{requestId}", method=RequestMethod.GET)
	public String discardBaojiaRequest(@PathVariable int requestId, HttpSession session){
		merchantBaoJiaRequestService.deleteBaoJiaRequest(requestId);
		return "redirect:/merchant/baojia/request/overview";
	}
	
	@RequestMapping(value="/save/vehicle",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap saveVehicleInfo(HttpServletRequest request){
		Car car = new Car();
		String cityCode = request.getParameter("cityCode");
		car.setCityCode(Integer.parseInt(cityCode)); 
		car.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		String merchantId = request.getParameter("merchantId");
		car.setMerchantCode(merchantId);
		String owner = request.getParameter("owner"); 
		car.setOwner(owner);
		String ownerId = request.getParameter("ownerId"); 
		car.setOwnerId(ownerId);
		String number = request.getParameter("number");
		car.setNumber(number);
		String vin = request.getParameter("vin");
		car.setVin(vin);
		String engineNo = request.getParameter("engineNo");
		car.setEngineNo(engineNo);
		String modelName = request.getParameter("modelName");
		car.setModelName(modelName);
		String vehicleModelId = request.getParameter("vehicleModelId");
		car.setVehicleModelId(Integer.parseInt(vehicleModelId));
		String registerDate = request.getParameter("registerDate");
		car.setEnrollDate(DateUtils.convertStrToDate(registerDate,"yyyy-MM-dd"));
		String guohuStr = request.getParameter("guohu");
		boolean guohu = false;
		if(StringUtils.equals(guohuStr, "1")){
			guohu = true;
		}
		car.setGuohu(guohu);
		String makeDate = request.getParameter("makeDate");
		if(guohu){
			car.setMakeDate(DateUtils.convertStrToDate(makeDate, "yyyy-MM-dd"));
		}
		String companyCarStr = request.getParameter("companyCar");
		boolean companyCar = false;
		if(StringUtils.equals(companyCarStr, "1")){
			companyCar = true;
		}
		car.setCompanyCar(companyCar);
		
		String reqeustIdStr = request.getParameter("requestId");
		int requestId = Integer.parseInt(reqeustIdStr);
		ModelMap mm = new ModelMap("retcode",0);
		//1. 根据车牌号和商家ID去查一下该车是否已经在该商家名下
		Car result = merchantBaoJiaRequestService.findCarByCarNumberAndMerchantId(number,merchantId);
		if(result == null){
			//2. 将车辆添加到该商家名下。
			result = merchantBaoJiaRequestService.addCar(car, requestId);
			mm.addAttribute("carId", result.getId());
		}else{
			mm.addAttribute("errorcode", "exist"); //商家已经添加过这辆车
			mm.addAttribute("carId", result.getId());
			
			//判断商家是否为重复上传
			int mId = Integer.parseInt(merchantId);
			MerchantBaoJiaRequest baoJiaRequest = merchantBaoJiaRequestService.judgeDuplicateRequest(mId, result.getId());
			if(baoJiaRequest != null){ //重复提交, 直接删除
				mm.addAttribute("errorcode", "duplicate");
				//更新一下报价方案
				MerchantBaoJiaRequest newRequest = merchantBaoJiaRequestService.findByRequestId(requestId);
				merchantBaoJiaRequestService.updateBaoJiaFangan(baoJiaRequest.getId(),newRequest.getBaojiaPackage());
				merchantBaoJiaRequestService.deleteBaoJiaRequest(requestId);
			}else{
				//更新一下商家报价请求信息
				merchantBaoJiaRequestService.updateMerchantBaoJiaRequest(number, result.getId(), requestId, 2); //2已分配
			}
		}
		return mm;
	}

	@RequestMapping(value="/go/add",method=RequestMethod.GET)
	public ModelAndView enterAdd(){
		ModelAndView mv = new ModelAndView("/merchant/addBaoJiaRequest");
		return mv;
	}
	
	@RequestMapping(value="/go/assign/{requestId}/other",method=RequestMethod.GET)
	public ModelAndView goAssignToOther(@PathVariable int requestId){
		ModelAndView mv = new ModelAndView("/merchant/assignToOther");
		mv.addObject("requestId", requestId);
		return mv;
	}
	
	@RequestMapping(value="/go/add/{requestId}/comment",method=RequestMethod.GET)
	public ModelAndView goAddComment(@PathVariable int requestId){
		ModelAndView mv = new ModelAndView("/merchant/addComments");
		mv.addObject("requestId", requestId);
		return mv;
	}
	
	@RequestMapping(value="/assign",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap assignToOther(int requestId, String customerService, String remark, HttpSession session){
		ModelMap mm = new ModelMap("errcode",0);
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		String username = accountInfo.getUserName();
		if(StringUtils.isNotEmpty(remark)){
			remark = DateUtils.convertDateToStr(new Date(), "yyyy-MM-dd HH:mm:ss")+"【"+remark+"】<font color='#3954F7'>@"+username+"</font>";
		}
		merchantBaoJiaRequestService.assign(requestId, customerService, remark);
		return mm;
	}
	
	@RequestMapping(value="/add/comment",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap addComments(int requestId, String remark, HttpSession session){
		ModelMap mm = new ModelMap("errcode",0);
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		String username = accountInfo.getUserName();
		merchantBaoJiaRequestService.assign(requestId, username, DateUtils.convertDateToStr(new Date(), "yyyy-MM-dd HH:mm:ss")+"【"+remark+"】<font color='#3954F7'>@"+username+"</font>");
		return mm;
	}
	
	@RequestMapping(value="/assign/{requestId}/me",method=RequestMethod.GET)
	@ResponseBody
	public ModelMap assignToMe(@PathVariable int requestId, HttpSession session){
		ModelMap mm = new ModelMap("errcode",0);
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		String username = accountInfo.getUserName();
		mm.put("userName", username);
		merchantBaoJiaRequestService.assign(requestId, username, null);
		return mm;
	}
	
	/**
	 * 删除报价请求 
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value="/delete/{requestId}",method=RequestMethod.GET)
	@ResponseBody
	public ModelMap deleteRequest(@PathVariable int requestId){
		ModelMap modelMap = new ModelMap("retcode", 0);
		merchantBaoJiaRequestService.deleteBaoJiaRequest(requestId);
		return modelMap;
	}
	
	/**
	 * 更新紧急度
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value="/update/priority/{requestId}/{priority}",method=RequestMethod.GET)
	public void updatePriority(@PathVariable int requestId, @PathVariable int priority){
		merchantBaoJiaRequestService.updatePriority(requestId,priority);
	}
	
	/**
	 * 更新标记
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value="/update/flag/{requestId}/{flag}",method=RequestMethod.GET)
	public void updateFlag(@PathVariable int requestId, @PathVariable int flag){
		merchantBaoJiaRequestService.updateFlag(requestId,flag);
	}
	
	/**
	 * 更新为已向客服反馈
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value="/update/state/{requestId}/{state}",method=RequestMethod.GET)
	@ResponseBody
	public ModelMap updateState(@PathVariable int requestId, @PathVariable int state, HttpSession session){
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		String username = accountInfo.getUserName();
		ModelMap mm = new ModelMap();
		if(state==3){
			mm.put("state","已反馈报价");
		}else if(state==4){
			mm.put("state", "已关闭");
		}
		merchantBaoJiaRequestService.updateStateById(requestId,username,state);
		return mm;
	}
	
	@RequestMapping(value="/nohandle/count",method=RequestMethod.GET)
	@ResponseBody
	public int getNoHandleRequestCount(){
		int count = merchantBaoJiaRequestService.countNoHandleRequestCount();
		return count;
	}
	
	/**
	 * 开始超级上传，一次性提交行驶证、身份证、投保方案
	 */
	@RequestMapping(value={"/superupload"}, method=RequestMethod.POST)
	@ResponseBody
	public ModelMap superupload(String fangan, String iccode, int merchantId, String licenseImg, String idCardImg) {
		ModelMap resultMap = new ModelMap("errcode", 0);
		try {
			fangan = URLEncoder.encode(fangan, "UTF-8");
			int start = licenseImg.indexOf("/");
			int end = licenseImg.indexOf(";");
			String suffix1 = licenseImg.substring(start + 1, end);
			String object1 = "merchant_vehicle_license/" + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix1;
			
			start = idCardImg.indexOf("/");
			end = idCardImg.indexOf(";");
			String suffix2 = idCardImg.substring(start + 1, end);
			String object2 = "merchant_vehicle_license/" + UUID.randomUUID().toString().replaceAll("-", "") + "." + suffix2;
			
			//保存上传的图片到阿里云上
			start = licenseImg.indexOf(",");
			licenseImg = licenseImg.substring(start + 1);
			byte[] bytes = Base64.decodeBase64(licenseImg);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
			ObjectMetadata metadata1 = new ObjectMetadata();
			metadata1.setContentLength(inputStream.available());
			client.putObject("auditimg", object1, inputStream, metadata1);
			
			start = idCardImg.indexOf(",");
			idCardImg = idCardImg.substring(start + 1);
			bytes = Base64.decodeBase64(idCardImg);
			inputStream = new ByteArrayInputStream(bytes);
			ObjectMetadata metadata2 = new ObjectMetadata();
			metadata2.setContentLength(inputStream.available());
			client.putObject("auditimg", object2, inputStream, metadata2);
			
			String vehicleLicenseUrl = "http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + object1;
			String idCardUrl = "http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + object2;
			
			merchantBaoJiaRequestService.saveMerchantUploadRecord(merchantId, fangan, iccode, vehicleLicenseUrl, idCardUrl);
		} catch (Exception e) {
			logger.error("商家行驶证、身份证上传失败.", e);
			resultMap.put("errcode", 1);
		}
		return resultMap;
	}
}

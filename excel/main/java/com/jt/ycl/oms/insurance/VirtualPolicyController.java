/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.model.Car;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.OmsUser;
import com.jt.core.model.VehicleLicense;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.account.AccountService;
import com.jt.ycl.oms.auth.Role;
import com.jt.ycl.oms.city.CityService;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value={"/virtual/policy"})
public class VirtualPolicyController {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String accessKeyId = "qlo4BoLGXaAXU7FA";
	private final String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	OSSClient client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
	
	@Autowired
	private VirtualPolicyService virtualPolicyService;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private AccountService accountService;
	
	@RequestMapping(value="/go/add",method=RequestMethod.GET)
	public ModelAndView enterAdd(){
		ModelAndView mv = new ModelAndView("/insurance/addVirtualPolicy");
		return mv;
	}
	
	@RequestMapping(value="/go/input/{policyId}/vehicle", method=RequestMethod.GET)
	public ModelAndView goInputVehicle(@PathVariable String policyId){
		InsurancePolicy insurancePolicy = virtualPolicyService.findById(policyId);
		ModelAndView mv = new ModelAndView("/insurance/inputVehicleInfo");
		Map<Integer, String> provinces = cityService.getAllProvince();
		mv.addObject("insurancePolicy", insurancePolicy);
		mv.addObject("provinces", provinces);
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest("auditimg");
		listObjectsRequest.setPrefix(policyId + "/");
		ObjectListing listing = client.listObjects(listObjectsRequest);
		List<OSSObjectSummary> imgList = listing.getObjectSummaries();
		List<OmsUser> hebaoUsers = accountService.findByRole(Role.OMS_HEBAO_USER);
		mv.addObject("hebaoUsers", hebaoUsers);
		if(CollectionUtils.isNotEmpty(imgList)){
			mv.addObject("licenseImageUrl", "http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + imgList.get(1).getKey());
			mv.addObject("idImageUrl", "http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + imgList.get(0).getKey());
		}
		return mv;
	}
	
	@RequestMapping(value="/input/vehicle",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap saveVehicleInfo(HttpServletRequest request, HttpSession session){
		Car car = new Car();
		String cityCode = request.getParameter("cityCode");
		car.setCityCode(Integer.parseInt(cityCode)); 
		car.setId(UUID.randomUUID().toString().replaceAll("-", ""));
		String channelCode = request.getParameter("channelCode");
		car.setMerchantCode(channelCode);
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
		
		String policyId = request.getParameter("policyId");
		ModelMap mm = new ModelMap("retcode",0);
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		int customerservice = accountInfo.getId(); //客服ID
		int assignCustomerService = Integer.parseInt(request.getParameter("customerservice"));
		if(customerservice != assignCustomerService){
			customerservice = assignCustomerService;
		}
		//1. 根据车牌号和商家ID去查一下该车是否已经在该商家名下
		Car result = virtualPolicyService.findCarByCarNumberAndMerchantId(number,channelCode);
		if(result == null){
			//2. 将车辆添加到该商家名下。
			result = virtualPolicyService.addCar(car, policyId, customerservice);
			mm.addAttribute("carId", result.getId());
		}else{
			mm.addAttribute("errorcode", "exist"); //商家已经添加过这辆车
			mm.addAttribute("carId", result.getId());
			InsurancePolicy virtualPolicy = virtualPolicyService.findById(policyId);
			//判断商家是否为重复上传
			InsurancePolicy insurancePolicy = virtualPolicyService.isDuplicateVirtualPolicy(result.getId());
			if(insurancePolicy != null){ //重复提交, 直接删除
				mm.addAttribute("errorcode", "duplicate");
				//更新一下报价方案
				virtualPolicyService.updateVirtualPolicy(virtualPolicy.getOrderId(), virtualPolicy.getBaojiaPackage());
				//virtualPolicyService.deleteVirtualPolicy(policyId); //删除重复的虚拟保单
			}else{
				//更新一下商家报价请求信息
				virtualPolicy.setOwner(car.getOwner());
				virtualPolicy.setOwnerId(car.getOwnerId());
				virtualPolicy.setCarNumber(car.getNumber());
				virtualPolicy.setCarId(result.getId());
				virtualPolicy.setCustomerservice(customerservice);
				virtualPolicy.setCityCode(car.getCityCode());
				virtualPolicy.setUpdateTime(new Date());
				virtualPolicyService.updateVirtualPolicy(virtualPolicy);
			}
		}
		return mm;
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
		VehicleLicense vehicleLicense = virtualPolicyService.getVehicleLicenseByNumber(carNumber);
		mm.addAttribute("vehicleLicense", vehicleLicense);
		return mm;
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
			String policyId = "JTK" + UUID.randomUUID().toString().replace("-", "");
			String suffix1 = licenseImg.substring(start + 1, end);
			String object1 = policyId+"/license." + suffix1;
			
			start = idCardImg.indexOf("/");
			end = idCardImg.indexOf(";");
			String suffix2 = idCardImg.substring(start + 1, end);
			String object2 = policyId+"/idcard." + suffix2;
			
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
			
			virtualPolicyService.createVirtualPolicy(policyId, merchantId, fangan, iccode);
		} catch (Exception e) {
			logger.error("商家行驶证、身份证上传失败.", e);
			resultMap.put("errcode", 1);
		}
		return resultMap;
	}
	
	@RequestMapping(value="/request/nohandle/count",method=RequestMethod.GET)
	@ResponseBody
	public int getNoHandleRequestCount(){
		int count = virtualPolicyService.countNoHandleRequestCount();
		return count;
	}
	
	@RequestMapping(value="/delete/{policyId}", method=RequestMethod.GET)
	@ResponseBody
	public ModelMap deleteVirtualPolicy(@PathVariable String policyId){
		virtualPolicyService.deleteVirtualPolicy(policyId);
		return new ModelMap();
	}
	
	@RequestMapping(value="/go/assign/to/channel/{policyId}",method=RequestMethod.GET)
	public ModelAndView assignToChannel(@PathVariable String policyId){
		ModelAndView mv = new ModelAndView("/insurance/assignToChannel", "orderId", policyId);
		return mv;
	}
	
	/**
	 * 将保单迁移到指定的渠道商明细
	 * 
	 * @param orderId			保单ID
	 * @param channelCode		目标渠道商编码
	 */
	@RequestMapping(value="assign/to/channel",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap assignToChannel(String orderId, String channelCode){
		ModelMap modelMap = new ModelMap("errcode",0);
		virtualPolicyService.transferToChannel(orderId,channelCode);
		return modelMap;
	}
}

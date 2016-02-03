package com.jt.ycl.oms.merchant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.ServiceCategory;
import com.jt.core.model.City;
import com.jt.core.model.Goods;
import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantMaintenance;
import com.jt.core.model.Region;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.sales.MerchantMaintenanceService;

/**
 * @author Wuqh
 */
@Controller
@RequestMapping(value={"/vehicle/merchant"})
@ResponseBody
@OMSPermission(permission = Permission.MERCHANT_MGMT)
public class VehicleMerchantController {
	
	@Autowired
	private VehicleMerchantService merchantService;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private MerchantMaintenanceService maintenanceService;
	
	/**
	 * 进入商家页面
	 */
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public ModelAndView query(HttpServletRequest request, HttpSession session) {
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		if(Role.BD_USER.equals(accountInfo.getRole().getName())) {
			//BD不能访问该页面，只能通过公司内网访问该页面
			String realIpAddress = null;
			if(request.getHeader("X-Forwarded-For") != null) {
				realIpAddress = request.getHeader("X-Forwarded-For");
			} else {
				realIpAddress = request.getRemoteAddr();
			}
			if(!"218.4.200.38".equals(realIpAddress)) {
				return new ModelAndView("ipInvalid");
			}
		}
		ModelAndView  mv = new ModelAndView("/merchant/vehicleMerchant");
		return mv;
	}
	
	/**
	 * 商家按条件查询
	 */
	@RequestMapping(value = "/query/condition", method = RequestMethod.POST)
	public ModelMap queryByConditon(String province, int cityCode, int regionId, String name, String merchantCode, int merchantType,
			int level, String salesman, int deadMerchant, int pageNumber, int pageSize, HttpSession session) {
		com.jt.core.model.MerchantQueryCondition condition = new com.jt.core.model.MerchantQueryCondition();
		condition.setProvince(province);
		condition.setCityCode(cityCode);
		condition.setRegionId(regionId);
		condition.setKeyName(name);
		condition.setMerchantType(merchantType);
		condition.setMerchantCode(merchantCode);
		condition.setLevel(level);
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		if(Role.BD_USER.equals(accountInfo.getRole().getName())){
			condition.setSalesman(accountInfo.getUserName());
		}else{
			condition.setSalesman(salesman);
		}
		condition.setDeadMerchant(deadMerchant);
		condition.setPageNumber(pageNumber+1);
		condition.setPageSize(pageSize);
		Map<String, Object> results = merchantService.findVehicleMerchants(condition);
		ModelMap  mv = new ModelMap("retcode",0);
		mv.addAttribute("totalItems", results.get("totalItems"));
		mv.addAttribute("totalPages", results.get("totalPages"));
		mv.addAttribute("merchants", results.get("merchants"));
		return mv;
	}
	
	@RequestMapping(value="/openarea", method=RequestMethod.GET)
	@ResponseBody
	public String getOpenArea(){
		String areaJson = merchantService.findOpenedArea();
		return areaJson;
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ModelMap createMerchant(@RequestBody Merchant merchant){
		ModelMap modelMap = new ModelMap("retcode",0);
		merchantService.saveMerchant(merchant);
		return modelMap;
	}
	
	@RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable("id") long id, HttpSession session) {
		ModelAndView mv = new ModelAndView("merchant/editMerchant");
		Merchant merchant = merchantService.getByMerchatId(id);
		List<Region> regions = merchantService.getAllRegionsByCityCode(merchant.getCityCode());
		ArrayList<Integer> c = new ArrayList<>();
		c.add(ServiceCategory.NORMAL_WASH_CAR_5);
		c.add(ServiceCategory.CAREFULLY_WASH_CAR_5);
		//赋默认值，避免vehiclMerchantBean提交时FLOAT型为空时出错
		mv.addObject("normalPrice", 0);
		mv.addObject("normalAccountPrice",0);
		mv.addObject("finePrice",0);
		mv.addObject("fineAccountPrice",0);
		
		List<Goods> goodss = merchantService.getGoodsByMerchantId(merchant.getId(), true, c);
		if(goodss != null && goodss.size()>0){
			for(Goods goods : goodss){
				if(goods.getServiceCategory() == ServiceCategory.NORMAL_WASH_CAR_5){
					mv.addObject("normalPrice", goods.getOriginalPrice());
					mv.addObject("normalAccountPrice",goods.getPrice());
				}else if(goods.getServiceCategory() == ServiceCategory.CAREFULLY_WASH_CAR_5){
					mv.addObject("finePrice",goods.getOriginalPrice());
					mv.addObject("fineAccountPrice",goods.getPrice());
				}
				mv.addObject("isContainTax",goods.isContainTax());
			}
		}
		Map<Integer, String> provinces = cityService.getAllProvince();
		City city = cityService.getByCityCode(merchant.getCityCode());
		mv.addObject("city",city);
		mv.addObject("provinces",provinces);
		mv.addObject("regions",regions);
		mv.addObject("merchant",merchant);
		
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user"); 
		Role role = accountInfo.getRole();
		if("oms-manager".equals(role.getName())){
			mv.addObject("isomsmanager", true);
		}else{
			mv.addObject("isomsmanager", false);
		}
		return mv;
	}

	@RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
	@OMSPermission(permission = Permission.MERCHANT_DELETE)
	public ModelMap delete(@PathVariable Long id) {
		ModelMap modelMap = new ModelMap("retcode",0);
		try{
			merchantService.deleteMerchantById(id);
		}catch(CommonLogicException e){
			modelMap.put("retcode", e.getErrorCode());
		}
		return modelMap;
	}
	
	@RequestMapping(value = {"/update/detail"}, method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView update(HttpServletRequest request, VehicleMerchantFormBean merchantFormBean)throws IOException {
		Merchant merchant = merchantService.getByMerchatId(merchantFormBean.getMerchantId());
		merchant.setRegionId(merchantFormBean.getRegionId());
		merchant.setUsername(merchantFormBean.getUserName());
		merchant.setName(merchantFormBean.getName());
		merchant.setLevel(merchantFormBean.getLevel());
		merchant.setAlias(merchantFormBean.getAlias());
		merchant.setAddress(merchantFormBean.getAddress());
		merchant.setLegalPerson(merchantFormBean.getLegalPerson());
		merchant.setIDNumber(merchantFormBean.getIDNumber());
		merchant.setManager(merchantFormBean.getManager());
		merchant.setManagerPhone(merchantFormBean.getManagerPhone());
		merchant.setHotline(merchantFormBean.getHotline());
		merchant.setBankAcountName(merchantFormBean.getBankAcountName());
		merchant.setBank(merchantFormBean.getBank());
		merchant.setBankCard(merchantFormBean.getBankCard());
		merchant.setLatitude(merchantFormBean.getLatitude());
		merchant.setLongitude(merchantFormBean.getLongitude());
		if(StringUtils.isNotEmpty(merchantFormBean.getSignedDate())) {
			merchant.setSignedDate(DateUtils.convertStrToDate(merchantFormBean.getSignedDate(), "yyyy-MM-dd"));
		}
		merchant.setSalesId(merchantFormBean.getSalesId());
		merchant.setWashcar(merchantFormBean.isWashcar());
		merchant.setChexian(merchantFormBean.isChexian());
		float normalPrice = merchantFormBean.getNormalPrice();
		float normalAccountPrice = merchantFormBean.getNormalAccountPrice();
		float finePrice = merchantFormBean.getFinePrice();
		float fineAccountPrice = merchantFormBean.getFineAccountPrice();
		boolean isContainTax = merchantFormBean.isContainTax();
		if(merchantFormBean.isWashcar()){
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;  
			MultipartFile file = multipartRequest.getFile("showPicture");
			merchantService.updatMerchant(merchant, file, normalPrice, normalAccountPrice,
					finePrice, fineAccountPrice, isContainTax);
		}else{
			merchantService.updatMerchant(merchant, null, normalPrice, normalAccountPrice,
					finePrice, fineAccountPrice, isContainTax);
		}
		return new ModelAndView("redirect:/vehicle/merchant/query");
	}
	
	@RequestMapping(value={"/add"}, method = RequestMethod.GET)
	public ModelAndView preAdd(){
		Map<Integer, String> provinces = cityService.getAllProvince();
		return new ModelAndView("merchant/addMerchant", "provinces", provinces);
	}
	
	@RequestMapping(value={"/check"}, method = RequestMethod.POST)
	@ResponseBody
	public ModelMap checkUserName(String userName){
		ModelMap mm = new ModelMap("retcode",0);
		Merchant merchant = merchantService.findByUsername(userName);
		if(merchant != null){
			mm.put("retcode", 1);
		}
		return mm;
	}
	
	@RequestMapping(value = { "/add" }, method = RequestMethod.POST)
	public ModelAndView add(HttpServletRequest request, VehicleMerchantFormBean merchantFormBean) throws CommonLogicException {
		if (merchantFormBean.isWashcar()) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("showPicture");
			merchantService.add(merchantFormBean, file);
		} else {
			merchantService.add(merchantFormBean, null);
		}
		return new ModelAndView("redirect:/vehicle/merchant/query");
	}
	
	@RequestMapping(value={"/map"}, method = RequestMethod.GET)
	public ModelAndView enterMap(){
		return new ModelAndView("merchant/merchantMap");
	}
	
	@RequestMapping(value={"/map/data"}, method = RequestMethod.GET)
	@ResponseBody
	public List<Merchant> getAllForMap(){
		List<Merchant> list = merchantService.getAllMerchantForMap();
		return list;
	}
	
	@RequestMapping(value={"/query/merchantcode"}, method = RequestMethod.POST)
	@ResponseBody
	public List<Merchant> queryMerchantsByName(String merchantName){
		List<Merchant> list = merchantService.queryMerchantsByName(merchantName);
		return list;
	}
	
	/**
	 * 查询商家的维护记录
	 * @param merchantCode
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/maintenance/{merchantCode}/record/{pageNumber}/{pageSize}", method = RequestMethod.GET)
	public ModelAndView queryMaintenanceRecord(@PathVariable String merchantCode, @PathVariable int pageNumber, @PathVariable int pageSize){
		ModelAndView mm = new ModelAndView("merchant/maintenanceRecords");
		Page<MerchantMaintenance> records = maintenanceService.findAll(merchantCode, pageNumber, pageSize);
		mm.addObject("records", records.getContent());
		return mm;
	}
}

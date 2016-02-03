/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantMaintenance;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.merchant.VehicleMerchantService;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="sales/merchant")
public class MerchantMaintenanceController {
	
	@Autowired
	private VehicleMerchantService merchantService;
	
	@Autowired
	private MerchantMaintenanceService maintenanceService;
	
	@RequestMapping(value = "/maintenance/{merchantId}/enter", method = RequestMethod.GET)
	public ModelAndView enter(@PathVariable long merchantId){
		ModelAndView mv = new ModelAndView("/sales/merchantMaintenance");
		Merchant merchant = merchantService.getByMerchatId(merchantId);
		mv.addObject("merchant", merchant);
		return mv;
	}
	
	@RequestMapping(value = "/maintenance/{merchantId}/jwd", method = RequestMethod.GET)
	public ModelAndView enterUpdateJWD(@PathVariable long merchantId){
		ModelAndView mv = new ModelAndView("/sales/update-longitude-latitude");
		Merchant merchant = merchantService.getByMerchatId(merchantId);
		mv.addObject("merchant", merchant);
		return mv;
	}
	
	/**
	 * 查询商家的维护记录
	 * @param merchantCode
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/maintenance/{merchantCode}/record/{pageNumber}/{pageSize}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap queryMaintenanceRecord(@PathVariable String merchantCode, @PathVariable int pageNumber, @PathVariable int pageSize){
		ModelMap modelMap = new ModelMap("retcode",0);
		Page<MerchantMaintenance> records = maintenanceService.findAll(merchantCode, pageNumber, pageSize);
		modelMap.put("records", records.getContent());
		return modelMap;
	}
	
	@RequestMapping(value="/maintenance/add", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap addRecord(@CookieValue(required = false, value = "salesId") String salesId, String merchantCode, 
			String content, HttpSession session){
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
	    String userName = accountInfo.getUserName();
		ModelMap result = new ModelMap("retcode",0);
		MerchantMaintenance merchantMaintenance = new MerchantMaintenance();
		merchantMaintenance.setMerchantCode(merchantCode);
		merchantMaintenance.setContent(content);
		merchantMaintenance.setMaintenanceDate(new Date());
		merchantMaintenance.setSalesman(userName);
		maintenanceService.addRecord(merchantMaintenance);
		return result;
	}

	@RequestMapping(value="/maintenance/lnglat/update", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap updateMerchantLngLat(long merchantId, double lng, double lat){
		ModelMap result = new ModelMap("retcode",0);
		Merchant merchant = merchantService.findMerchantById(merchantId+"");
		merchant.setLongitude(lng);
		merchant.setLatitude(lat);
		merchantService.saveMerchant(merchant);
		return result;
	}
}

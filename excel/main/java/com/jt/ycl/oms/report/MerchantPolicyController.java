package com.jt.ycl.oms.report;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.MerchantPolicyStatistics;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.merchant.VehicleMerchantService;

@Controller
@RequestMapping(value="report/merchant")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class MerchantPolicyController {
	
	@Autowired
	private MerchantPolicyService merchantPolicyService;
	
	@Autowired
	private VehicleMerchantService merchantService;
	
	/**
	 * 进入商家日活统计页面
	 * @return
	 */
	@RequestMapping(value = "overview", method = RequestMethod.GET)
	public ModelAndView query(){
		ModelAndView  mv = new ModelAndView("/report/merchant/merchantPolicy");
		return mv;
	}
	
	@RequestMapping(value="/openarea", method=RequestMethod.GET)
	@ResponseBody
	public String getOpenArea(){
		String areaJson = merchantService.findOpenedArea();
		return areaJson;
	}
	
	@RequestMapping(value = "/policy/statitics", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap overview(int cityCode, String date){
		ModelMap mv = new ModelMap("retcode",0);
		List<MerchantPolicyStatistics> results = merchantPolicyService.staticsMerchantPolicy(cityCode, date);
		mv.addAttribute("results", results);
		return mv;
	}
	
	@RequestMapping(value = "/daily/active/enter", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/report/merchant/merchantDailyActive");
		return mv;
	}
	
	@RequestMapping(value="/daily/active/count", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap dailyActiveCount(int cityCode, int year, int month) {
		Map<Integer, Integer> resultMap = merchantPolicyService.dailyActiveCount(cityCode, year, month);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
	
	@RequestMapping(value="/daily/active/rate", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap dailyActiveRate(int cityCode, int year, int month) {
		Map<Integer, Float> resultMap = merchantPolicyService.dailyActiveRate(cityCode, year, month);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
}

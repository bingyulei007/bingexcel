/**
 * 
 */
package com.jt.ycl.oms.report;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="report/baojia")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class BaoJiaConvertRateController {

	@Autowired
	private BaoJiaConvertRateService baoJiaConvertRateService;
	
	@RequestMapping(value = "/enter", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/report/insurance/carBaoJiaConvertRate");
		return mv;
	}
	
	@RequestMapping(value="/car/count", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap baoJiaCarCount(String cityName, int year, int timeType) {
		Map<Integer, Integer> resultMap = baoJiaConvertRateService.baoJiaCarCount(cityName, year, timeType);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
	
	@RequestMapping(value="/convert/rate", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap baoJiaConvertRate(int cityCode, String cityName, int year, int timeType) {
		Map<Integer, Float> resultMap = baoJiaConvertRateService.baoJiaConvertRate(cityCode, cityName, year, timeType);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
}

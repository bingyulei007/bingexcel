/**
 * 
 */
package com.jt.ycl.oms.report;

import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 */
@Controller
@RequestMapping(value="report/insurance")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class PolicyStaticsController {
	
	@Autowired
	private PolicyStaticsService policyStaticsService;
	
	@RequestMapping(value = "/enter", method = RequestMethod.GET)
	public ModelAndView enter() {
		ModelAndView mv = new ModelAndView("/report/insurance/policyDailyActive");
		return mv;
	}
	
	@RequestMapping(value="/policy/daily/active/{year}/{month}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap dailyActive(@PathVariable int year, @PathVariable int month) {
		Map<Integer, Integer> resultMap = policyStaticsService.policyDailyActive(year, month);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
	
	@RequestMapping(value = "/car/daily", method = RequestMethod.GET)
	public ModelAndView showCarReportPage(){
		ModelAndView mv = new ModelAndView("/report/carDailyActive");
		return mv;
	}
	
	/**
	 * 统计每日新增车辆
	 */
	@RequestMapping(value="/car/day/{year}/{month}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap countNewCarByDay(@PathVariable int year, @PathVariable int month) {
		Map<Integer, Integer> resultMap = policyStaticsService.countNewCarByDay(year, month);
		ModelMap mm = new ModelMap();
		mm.addAttribute("days", resultMap.keySet());
		mm.addAttribute("nums", resultMap.values());
		return mm;
	}
}
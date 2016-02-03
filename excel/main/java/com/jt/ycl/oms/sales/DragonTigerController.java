/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.utils.DateUtils;

/**
 * 龙虎榜
 */
@Controller
@RequestMapping(value="sales/policy")
public class DragonTigerController {

	@Autowired
	private MyPolicyService myPolicyService;
	
	@RequestMapping(value = "/lhb/enter", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/sales/sales-dragon-tiger");
		return mv;
	}
	
	@RequestMapping(value = "/lhb/{page}/{pageSize}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap list(@PathVariable int page, @PathVariable int pageSize) {
		ModelMap mm = new ModelMap("retcode",0);
		String startDate = DateUtils.convertDateToStr(DateUtils.getBeginTimeOfMonth(new Date()),"yyyy-MM-dd 00:00:00");
		String endDate = DateUtils.convertDateToStr(DateUtils.getEndTimeOfMonth(new Date()), "yyyy-MM-dd 23:59:59");
		List<BoardItem> boardItems = myPolicyService.staticsSalesPolicy(224, page, pageSize, startDate, endDate, null);
		mm.put("boardItems", boardItems);
		return mm;
	}
}
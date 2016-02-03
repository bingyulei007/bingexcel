package com.jt.ycl.oms.report;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.jt.utils.DateUtils;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * 商家推荐用户数管理
 * @author xiaojiapeng
 *
 */
@Controller
@RequestMapping(value="report/recommend")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class MerchantRecommendController {
	
	@Autowired
	private ReportUserService reportUserService;
	
	@RequestMapping
	public ModelAndView init(String startTime, String endTime){
		Date startDate = null;
		Date endDate = null;
		ModelMap mm = new ModelMap();
		if(StringUtils.isEmpty(startTime)){
			startDate = DateUtils.getFirstDayOfThisWeek(new Date());
			mm.put("startTime", DateUtils.convertDateToStr(startDate, "yyyy-MM-dd"));
		}else{
			mm.put("startTime", startTime);
			startTime = startTime + " 00:00:00";
			startDate = DateUtils.convertStrToDate(startTime, "yyyy-MM-dd HH:mm:ss");
		}
		if(StringUtils.isEmpty(endTime)){
			endDate = new Date();
			mm.put("endTime", DateUtils.convertDateToStr(endDate, "yyyy-MM-dd"));
		}else{
			mm.put("endTime", endTime);
			endTime = endTime + " 23:59:59";
			endDate = DateUtils.convertStrToDate(endTime, "yyyy-MM-dd HH:mm:ss");
		}
		List<RecommendUser> recommendUsers = reportUserService.countRecommendData(startDate, endDate);
		
		mm.put("recommendUsers", recommendUsers);
		return new ModelAndView("report/user/recommend", mm);
	}
	

}

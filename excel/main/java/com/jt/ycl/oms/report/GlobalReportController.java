package com.jt.ycl.oms.report;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.WXSubscribe;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author xiaojiapeng
 */
@Controller
@RequestMapping(value="report/user")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class GlobalReportController {
	
	@Autowired
	private ReportUserService reportUserService;
	
	@RequestMapping(value="overview")
	public ModelAndView overview(){
		ModelMap mm = new ModelMap();
		Date now = new Date();
		//今日开始时间
		Date currDayStartDate = DateUtils.getStartTimeOfDay(now);
		//本周开始时间
		Date weekStartDate = DateUtils.getFirstDayOfThisWeek(now);
		//本月开始时间
		Date monthStartDate = DateUtils.getBeginTimeOfMonth(now);
		
		long userCurrDayCount = reportUserService.countUserByCreateDate(currDayStartDate);
		long userWeekCount = reportUserService.countUserByCreateDate(weekStartDate);
		long userMonthCount = reportUserService.countUserByCreateDate(monthStartDate);
		long userTotalCount = reportUserService.countUserByCreateDate(null);
		mm.put("userCurrDayCount", userCurrDayCount);
		mm.put("userWeekCount", userWeekCount);
		mm.put("userMonthCount", userMonthCount);
		mm.put("userTotalCount", userTotalCount);
		
		long carCurrDayCount = reportUserService.countCarByCreateDate(currDayStartDate);
		long carWeekCount = reportUserService.countCarByCreateDate(weekStartDate);
		long carMonthCount = reportUserService.countCarByCreateDate(monthStartDate);
		long carTotalCount = reportUserService.countCarByCreateDate(null);
		mm.put("carCurrDayCount", carCurrDayCount);
		mm.put("carWeekCount", carWeekCount);
		mm.put("carMonthCount", carMonthCount);
		mm.put("carTotalCount", carTotalCount);
		
		long bjRecordCurrDayCount = reportUserService.countBaojiaRecordByQueryDate(currDayStartDate);
		long newCarBjCount = reportUserService.countNewCarBaojiaByQueryDate(currDayStartDate);//今天新增车辆报价数
		long bjRecordWeekCount = reportUserService.countBaojiaRecordByQueryDate(weekStartDate);
		long bjRecordMonthCount = reportUserService.countBaojiaRecordByQueryDate(monthStartDate);
		long bjRecordTotalCount = reportUserService.countBaojiaRecordByQueryDate(null);
		mm.put("bjRecordCurrDayCount", bjRecordCurrDayCount);
		mm.put("newCarBjCount", newCarBjCount);
		mm.put("bjRecordWeekCount", bjRecordWeekCount);
		mm.put("bjRecordMonthCount", bjRecordMonthCount);
		mm.put("bjRecordTotalCount", bjRecordTotalCount);
		
		Date today = DateUtils.getStartTimeOfDay(new Date());
		int todayLogins = reportUserService.loginChannelCount(today);
		mm.put("todayLogins", todayLogins);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		int last2DayLogins = reportUserService.loginChannelCount(calendar.getTime());
		mm.put("last2DayLogins", last2DayLogins);

		calendar.add(Calendar.DAY_OF_MONTH, -1);
		int last3DayLogins = reportUserService.loginChannelCount(calendar.getTime());
		mm.put("last3DayLogins", last3DayLogins);
		
		Date thisWeek = DateUtils.getFirstDayOfThisWeek(new Date());
		int thisWeekLogins = reportUserService.loginChannelCount(thisWeek);
		mm.put("thisWeekLogins", thisWeekLogins);
		
		return new ModelAndView("report/user/overview", mm);
	}
	
	@RequestMapping(value="subscribe")
	public ModelAndView subscribeData() {
		List<WXSubscribe> subscribes = reportUserService.getWxSubscribe();
		return new ModelAndView("report/user/subscribe","subscribes",subscribes);
	}
}
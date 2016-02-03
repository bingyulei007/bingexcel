/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.report.MerchantPolicyService;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="sales/index")
public class SalesIndexController {
	
	@Autowired
	private MerchantPolicyService merchantPolicyService;
	
	@RequestMapping(value = "/enter", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/sales/salesIndex");
		return mv;
	}
	
	/**
	 * 显示销售经理相关统计数据，如本月的任务完率、光头商户数、商户活跃率，本月商户出单数等
	 */
	@RequestMapping(value="/task",method=RequestMethod.GET)
	@ResponseBody
	public ModelMap queryStaticsData(HttpSession session){
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
	    String userName = accountInfo.getUserName();
		ModelMap mm = new ModelMap("retcode",0);
		List<String> salesman = new ArrayList<String>();
		salesman.add(userName);
		int cityCode = accountInfo.getCityCode();
		mm.put("employeeID", accountInfo.getId());
		mm.put("salesId", accountInfo.getUserId());
		//1.本月完成保单数，即本月销售商户出单总数
		List<Object[]> policyCount = merchantPolicyService.queryThisMonthPolicyCount(cityCode,salesman);
		int totalPolicyCount = 0;
		if(CollectionUtils.isNotEmpty(policyCount)){
			totalPolicyCount = Integer.parseInt(policyCount.get(0)[1].toString());
			mm.put("finishedTaskCount", totalPolicyCount);
		}else{
			mm.put("finishedTaskCount", 0);
		}
		
		//2.本月商户活跃率，指出单总数除总的商户数
		List<Object[]> merchantCount = merchantPolicyService.queryThisMonthHasPolicyMerchantCount(cityCode,salesman);
		List<Object[]> totalMerchantCount = merchantPolicyService.querySignedMerchantTotalCount(cityCode, salesman);
		int totalCount = 0;
		if(CollectionUtils.isNotEmpty(totalMerchantCount)){
			totalCount = Integer.parseInt(totalMerchantCount.get(0)[1].toString());
		}
		if(CollectionUtils.isNotEmpty(merchantCount)){
			int hasPolicyMerchantCount = Integer.parseInt(merchantCount.get(0)[1].toString());
			float activeRate = new BigDecimal(hasPolicyMerchantCount*100f/totalCount).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			mm.put("merchantActiveRate", activeRate);
		}else{
			mm.put("merchantActiveRate", 0);
		}
		
		//3.今日保单数，即销售当天的出单数
		Calendar calendar = Calendar.getInstance();
		String date = DateUtils.convertDateToStr(calendar.getTime(), "yyyy-MM-dd");
		List<Object[]> todayPolicyCount = merchantPolicyService.querySignedMerchantPolicyCount(cityCode, date, salesman);
		if(CollectionUtils.isNotEmpty(todayPolicyCount)){
			mm.put("totayPolicyCount", Integer.parseInt(todayPolicyCount.get(0)[1].toString()));
		}else{
			mm.put("totayPolicyCount",0);
		}
		
		//4.日均保单数，指销售本月出单数除以当月天数
		int days = calendar.getActualMaximum(Calendar.DATE);
		if(totalPolicyCount>0){
			float averagePolicyCount =  new BigDecimal(totalPolicyCount*1f/days).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			mm.put("averagePolicyCount", averagePolicyCount);
		}else{
			mm.put("averagePolicyCount", 0);
		}
		
		//5.光头商户数，指的是从签约截至当天还没有出单的商户数
		List<Object[]> noPolicyMerchantCount = merchantPolicyService.querySignedMerchantNoPolicy(cityCode, salesman);
		if(CollectionUtils.isNotEmpty(noPolicyMerchantCount)){
			mm.put("noPolicyMerchantCount", noPolicyMerchantCount.get(0)[1].toString());
		}else{
			mm.put("noPolicyMerchantCount", 0);
		}
		return mm;
	}
}

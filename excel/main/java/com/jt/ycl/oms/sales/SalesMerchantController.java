package com.jt.ycl.oms.sales;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.MerchantQueryCondition;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.report.MerchantPolicyService;

@Controller
@RequestMapping(value="sales/merchant")
public class SalesMerchantController {
	
	@Autowired
	private MerchantPolicyService merchantPolicyService;
	
	@RequestMapping(value = "/enter", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/sales/salesMerchants");
		return mv;
	}
	
	@RequestMapping(value="/query",method=RequestMethod.POST)
	@ResponseBody
	public ModelMap queryMyMerchant(int type, int page, String merchantName, HttpSession session){
		ModelMap mm = new ModelMap("retcode", 0);
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
	    String userName = accountInfo.getUserName();
	    MerchantQueryCondition condition = new MerchantQueryCondition();
	    condition.setCityCode(accountInfo.getCityCode());
	    condition.setSalesman(userName);
	    condition.setDeadMerchant(type);
	    condition.setKeyName(merchantName);
	    condition.setPageNumber(page);
	    condition.setPageSize(10);
	    String startDate = DateUtils.convertDateToStr(DateUtils.getBeginTimeOfMonth(new Date()), "yyyy-MM-dd 00:00:00");
	    String endDate = DateUtils.convertDateToStr(DateUtils.getEndTimeOfMonth(new Date()), "yyyy-MM-dd 23:59:59");
	    condition.setStartDate(startDate);
	    condition.setEndDate(endDate);
	    Object merchants = null;
	    if(type==1){
	    	merchants= merchantPolicyService.findHasPolicyMerchant(condition);
	    }else if(type==2){
	    	merchants= merchantPolicyService.findNoPolicyMerchant(condition);
	    }
	    mm.put("merchants", merchants);
		return mm;
	}
	
}

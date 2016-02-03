/**
 * 
 */
package com.jt.ycl.oms.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.InsurancePolicy;
import com.jt.ycl.oms.account.AccountInfo;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="/report/policy")
public class WillBeLostPolicyController {
	
	@Autowired
	private WillBeLostPolicyService wilBeLostPolicyService;
	

	/**
	 * 进入即将飞单的保单列表页面
	 * @return
	 */
	@RequestMapping(value = "/go/will/be/lost", method = RequestMethod.GET)
	public ModelAndView enter(){
		ModelAndView mv = new ModelAndView("/report/insurance/willBeLostPolicy");
		return mv;
	}
	
	/**
	 * 列出进来的保单超过2天未来进入核保状态的保单
	 * @return
	 */
	@RequestMapping(value="/list/will/be/lost", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap query(String salesMan, int pageNumber, int pageSize, HttpSession session){
		AccountInfo accountInfo = (AccountInfo)session.getAttribute("user");
		if("0".equals(salesMan)&&"bd-user".equals(accountInfo.getRole().getName())){
			salesMan =  accountInfo.getUserName();
		}
		//超过2天未来进入待核保
		List<InsurancePolicy> twoDaysPolicy = wilBeLostPolicyService.willBeLostPolicy(salesMan, 2, pageNumber, pageSize);
		ModelMap mm = new ModelMap("retcode",0);
		mm.addAttribute("twoDaysPolicy", twoDaysPolicy);
		int twoDaysPolicyCount = 0;
		Map<String, Integer> map = new HashMap<String, Integer>();
		if(CollectionUtils.isNotEmpty(twoDaysPolicy)){
			twoDaysPolicyCount = twoDaysPolicy.size();
			mm.addAttribute("twoDaysPolicyCount", twoDaysPolicyCount);
			for(InsurancePolicy policy : twoDaysPolicy){
				if(!map.containsKey(policy.getSalesMan())){
					map.put(policy.getSalesMan(), 1);
				}else{
					map.put(policy.getSalesMan(), map.get(policy.getSalesMan())+1);
				}
			}
		}
		
		//超过3天未来进入待核保
		List<InsurancePolicy> threeDaysPolicy = wilBeLostPolicyService.willBeLostPolicy(salesMan, 3, pageNumber, pageSize);
		mm.addAttribute("threeDaysPolicy", threeDaysPolicy);
		int threeDaysPolicyCount = 0;
		if(CollectionUtils.isNotEmpty(threeDaysPolicy)){
			threeDaysPolicyCount = threeDaysPolicy.size();
			mm.addAttribute("threeDaysPolicyCount", threeDaysPolicyCount);
			for(InsurancePolicy policy : threeDaysPolicy){
				if(!map.containsKey(policy.getSalesMan())){
					map.put(policy.getSalesMan(), 1);
				}else{
					map.put(policy.getSalesMan(), map.get(policy.getSalesMan())+1);
				}
			}
		}
		
		//超过4天未来进入待核保
		List<InsurancePolicy> fourDaysPolicy = wilBeLostPolicyService.willBeLostPolicy(salesMan, 4, pageNumber, pageSize);
		mm.addAttribute("fourDaysPolicy", fourDaysPolicy);
		int fourDaysPolicyCount = 0;
		if(CollectionUtils.isNotEmpty(fourDaysPolicy)){
			fourDaysPolicyCount = fourDaysPolicy.size();
			mm.addAttribute("fourDaysPolicyCount", fourDaysPolicyCount);
			for(InsurancePolicy policy : fourDaysPolicy){
				if(!map.containsKey(policy.getSalesMan())){
					map.put(policy.getSalesMan(), 1);
				}else{
					map.put(policy.getSalesMan(), map.get(policy.getSalesMan())+1);
				}
			}
		}
		
		//超过5天未来进入待核保
		List<InsurancePolicy> fiveDaysPolicy = wilBeLostPolicyService.willBeLostPolicy(salesMan, 5, pageNumber, pageSize);
		mm.addAttribute("fiveDaysPolicy", fiveDaysPolicy);
		int fiveDaysPolicyCount = 0;
		if(CollectionUtils.isNotEmpty(fiveDaysPolicy)){
			fiveDaysPolicyCount = fiveDaysPolicy.size();
			mm.addAttribute("fiveDaysPolicyCount", fiveDaysPolicyCount);
			for(InsurancePolicy policy : fiveDaysPolicy){
				if(!map.containsKey(policy.getSalesMan())){
					map.put(policy.getSalesMan(), 1);
				}else{
					map.put(policy.getSalesMan(), map.get(policy.getSalesMan())+1);
				}
			}
		}
		
		//5天以上未来进入待核保
		List<InsurancePolicy> moreThanFiveDaysPolicy = wilBeLostPolicyService.willBeLostPolicy(salesMan, 6, pageNumber, pageSize);
		mm.addAttribute("moreThanFiveDaysPolicy", moreThanFiveDaysPolicy);
		int moreThanFiveDaysPolicyCount = 0;
		if(CollectionUtils.isNotEmpty(moreThanFiveDaysPolicy)){
			moreThanFiveDaysPolicyCount = moreThanFiveDaysPolicy.size();
			mm.addAttribute("moreThanFiveDaysPolicyCount", moreThanFiveDaysPolicyCount);
			for(InsurancePolicy policy : moreThanFiveDaysPolicy){
				if(!map.containsKey(policy.getSalesMan())){
					map.put(policy.getSalesMan(), 1);
				}else{
					map.put(policy.getSalesMan(), map.get(policy.getSalesMan())+1);
				}
			}
		}
		mm.addAttribute("map", map);
		mm.addAttribute("totalCount", twoDaysPolicyCount+threeDaysPolicyCount+fourDaysPolicyCount+fiveDaysPolicyCount+moreThanFiveDaysPolicyCount);
		return mm;
	}
}

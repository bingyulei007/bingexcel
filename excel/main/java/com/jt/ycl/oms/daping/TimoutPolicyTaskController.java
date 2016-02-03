/**
 * 
 */
package com.jt.ycl.oms.daping;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.InsurancePolicy;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="daping")
public class TimoutPolicyTaskController {
	
	@Autowired
	private TimoutPolicyTaskService TimoutPolicyTaskService;
	
	
	/**
	 * 进入大屏展示页面
	 * @return
	 */
	@RequestMapping(value = "/timeout/task", method = RequestMethod.GET)
	public ModelAndView query(HttpSession session){
		ModelAndView mv = new ModelAndView("/daping/timeoutPolicyTask");
		return mv;
	}
	

	/**
	 * 列出录入车辆后超过10分钟还没有返馈给客户的虚拟保单，显示车牌号，客服，超时时间
	 * @return
	 */
	@RequestMapping(value="/list/noresponse/policy", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap listNoReponsePolicy(int pageNumber, int pageSize, long timeout){
		Page<InsurancePolicy> page = TimoutPolicyTaskService.listNoReponsePolicy(pageNumber, pageSize, timeout);
		ModelMap mm = new ModelMap("retcode",0);
		mm.addAttribute("tasks", page.getContent());
		mm.addAttribute("totalPages", page.getTotalPages());
		mm.addAttribute("totalItems", page.getTotalElements());
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if(CollectionUtils.isNotEmpty(page.getContent())){
			for(InsurancePolicy policy : page.getContent()){
				if(!map.containsKey(policy.getCustomerservice())){
					map.put(policy.getCustomerservice(), 1);
				}else{
					map.put(policy.getCustomerservice(), map.get(policy.getCustomerservice())+1);
				}
			}
		}
		mm.addAttribute("map", map);
		return mm;
	}
	
	/**
	 * 列出进入待核保后，但超过2小时还未来核保通过的保单信息
	 * @return
	 */
	@RequestMapping(value="/list/nopassunderwriting/policy", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap listNoPassUnderwritingPolicy(int pageNumber, int pageSize, long timeout){
		Page<InsurancePolicy> page = TimoutPolicyTaskService.listNoPassUnderwritingPolicy(pageNumber, pageSize, timeout);
		ModelMap mm = new ModelMap("retcode",0);
		mm.addAttribute("tasks", page.getContent());
		mm.addAttribute("totalPages", page.getTotalPages());
		mm.addAttribute("totalItems", page.getTotalElements());
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if(CollectionUtils.isNotEmpty(page.getContent())){
			for(InsurancePolicy policy : page.getContent()){
				if(!map.containsKey(policy.getCustomerservice())){
					map.put(policy.getCustomerservice(), 1);
				}else{
					map.put(policy.getCustomerservice(), map.get(policy.getCustomerservice())+1);
				}
			}
		}
		mm.addAttribute("map", map);
		return mm;
	}
	
	/**
	 * 列出同意出单，但超过2小时还未出单的保单信息
	 * @return
	 */
	@RequestMapping(value="/list/nochudan/policy", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap listNoChudanPolicy(int pageNumber, int pageSize, long timeout){
		Page<InsurancePolicy> page = TimoutPolicyTaskService.listNoChudanPolicy(pageNumber, pageSize, timeout);
		ModelMap mm = new ModelMap("retcode",0);
		mm.addAttribute("tasks", page.getContent());
		mm.addAttribute("totalPages", page.getTotalPages());
		mm.addAttribute("totalItems", page.getTotalElements());
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if(CollectionUtils.isNotEmpty(page.getContent())){
			for(InsurancePolicy policy : page.getContent()){
				if(!map.containsKey(policy.getCustomerservice())){
					map.put(policy.getCustomerservice(), 1);
				}else{
					map.put(policy.getCustomerservice(), map.get(policy.getCustomerservice())+1);
				}
			}
		}
		mm.addAttribute("map", map);
		return mm;
	}
}

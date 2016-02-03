package com.jt.ycl.oms.insurance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.insurance.ICCode;
import com.jt.core.model.MerchantCommissionRate;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

@Controller
@RequestMapping("commission/config")
@OMSPermission(permission=Permission.COMMISSION_RATE_CONFIG)
public class MerchantCommissionRateController {
	
	@Autowired
	private MerchantCommissionRateService configService;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView init(){
		List<MerchantCommissionRate> list = configService.getAll();
		return new ModelAndView("insurance/commissionConfig", "list", list);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String save(HttpServletRequest request){
		Set<Integer> companyCodes = ICCode.codeNameMapping.keySet();
		List<MerchantCommissionRate> configs = new ArrayList<>();
		for(Integer companyCode : companyCodes){
			String idStr = request.getParameter("config" + companyCode.intValue());
			if(StringUtils.isEmpty(idStr)){
				continue;
			}
			String commissionRateStr = request.getParameter("rate" + companyCode.intValue());
			int id = Integer.parseInt(idStr);
			float rate = Float.parseFloat(commissionRateStr);
			MerchantCommissionRate config = new MerchantCommissionRate();
			config.setId(id);
			config.setCompanyCode(companyCode.intValue());
			config.setRate(rate);
			configs.add(config);
		}
		if(CollectionUtils.isNotEmpty(configs)){
			configService.update(configs);
		}
		return "redirect:/commission/config";
	}

}

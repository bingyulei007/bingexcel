package com.jt.ycl.oms.insurance;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.auth.Role;

/**
 * 
 * @author bing
 * 
 */

@Controller
@RequestMapping(value = { "/monitor" })
public class PremiumMonitorController {
	@Autowired
	private  PremiumMonitorService premiumMonitorService;
	
	@RequestMapping(value = { "/preminu/view" },method=RequestMethod.GET)
	public ModelAndView returnMonitorView(){
		return new ModelAndView("PremiumMonitor/premiumAccound");
	}
	/**
	 * 返回保单超期列表，以销售分组
	 * @param salerName
	 * @param cityCode
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/saler/group/premium" },method=RequestMethod.POST)
	public ModelMap queryFroPremiumDate(@RequestParam(required=false)String  salerName,@RequestParam(required=false)String cityCode){
		ModelMap mm = new ModelMap();
		
		List<?> list=	premiumMonitorService.queryFroPreminuDate(salerName,cityCode);
		mm.put("errcode", 0);
		mm.put("body", list);
		return mm;
	}
	/**
	 * 返回对应销售的保单详情
	 * @param saler
	 * @param cityCode
	 * @param deadline
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = { "/saler/policy/premium" },method=RequestMethod.POST)
	public ModelMap querySalerPremium(@RequestParam(required=true)String  saler,@RequestParam(required=false)String cityCode,int deadline){
		ModelMap mm = new ModelMap();
		mm.put("errcode", 0);
		
		List<?> list=	premiumMonitorService.querySalerPremium(saler,cityCode,deadline);
		
		mm.put("body", list);
		return mm;
	}
}

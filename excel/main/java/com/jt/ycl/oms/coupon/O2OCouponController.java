/**
 * 
 */
package com.jt.ycl.oms.coupon;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jt.core.model.CouponTemplate;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="o2o/coupon")
@OMSPermission(permission = Permission.O2O_MERCHANT_MGMT)
public class O2OCouponController {
	
	@Autowired
	private O2OCouponService o2oCouponService;
	
	/**
	 * 进入新增卡券模板页面
	 * @return
	 */
	@RequestMapping(value = "enter/{o2oMerchantId}/add", method = RequestMethod.GET)
	public ModelAndView enterAdd(@PathVariable("o2oMerchantId") int o2oMerchantId){
		ModelAndView mv = new ModelAndView("/coupon/couponTemplate");
		mv.addObject("o2oMerchantId", o2oMerchantId);
		mv.addObject("id", 0); //为避免提交时为空，这里设置为0
		return mv;
	}
	
	@RequestMapping(value = "/check/couponname", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap checkCouponName(String couponName, int o2oMerchantId){
		ModelMap modelMap = new ModelMap();
		int result = o2oCouponService.findCouponByO2oMerchantIdAndName(o2oMerchantId, couponName);
		modelMap.put("result", result);
		return modelMap;
	}
	
	/**
	 * 新增卡券模板
	 * @return
	 */
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public String add(CouponTemplateFormBean couponTemplateFormBean){
		CouponTemplate couponTemplate = null;
		if(couponTemplateFormBean.getId()>0){
			couponTemplate = o2oCouponService.findCouponTemplateId(couponTemplateFormBean.getId());
		}else{
			couponTemplate = new CouponTemplate();
		}
		couponTemplate.setCategory(couponTemplateFormBean.getCategory());
		couponTemplate.setName(couponTemplateFormBean.getName());
		couponTemplate.setFaceValue(couponTemplateFormBean.getFaceValue());
		if(StringUtils.isNotBlank(couponTemplateFormBean.getOriginalPrice())){
			couponTemplate.setOriginalPrice(Integer.parseInt(couponTemplateFormBean.getOriginalPrice()));
		}
		if(StringUtils.isNotBlank(couponTemplateFormBean.getSettlementPrice())){
			couponTemplate.setSettlementPrice(Integer.parseInt(couponTemplateFormBean.getSettlementPrice()));
		}
		couponTemplate.setStartDate(DateUtils.convertStrToDate(couponTemplateFormBean.getStartDate(),"yyyy-MM-dd"));
		couponTemplate.setExpireDate(DateUtils.convertStrToDate(couponTemplateFormBean.getExpireDate(),"yyyy-MM-dd"));
		couponTemplate.setCount(couponTemplateFormBean.getCount());
		couponTemplate.setAddress(couponTemplateFormBean.getAddress());
		couponTemplate.setO2oMerchantId(couponTemplateFormBean.getO2oMerchantId());
		couponTemplate.setIsNeedCode(couponTemplateFormBean.getIsNeedCode());
		couponTemplate.setRemark(couponTemplateFormBean.getRemark());
		o2oCouponService.createCouponTemplate(couponTemplate);
		return "redirect:/o2o/coupon/"+couponTemplateFormBean.getO2oMerchantId()+"/query";
	}
	
	/**
	 * 进入卡券商家首页
	 * @return
	 */
	@RequestMapping(value = "enter/{id}/{o2oMerchantId}/edit", method = RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable("id") int id, @PathVariable("o2oMerchantId") int o2oMerchantId){
		CouponTemplate couponTemplate = o2oCouponService.findById(id);
		ModelAndView mv = new ModelAndView("/coupon/couponTemplate");
		mv.addObject("couponTemplate", couponTemplate);
		mv.addObject("id", id);
		mv.addObject("o2oMerchantId", o2oMerchantId);
		return mv;
	}
	
	@RequestMapping(value = "{id}/delete", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap delete(@PathVariable int id) {
		o2oCouponService.deleteCouponTemplateById(id);
		ModelMap modelMap = new ModelMap("retcode",0);
		return modelMap;
	}
	
	/**
	 * 进入卡券模板首页
	 * @return
	 */
	@RequestMapping(value = "{id}/query", method = RequestMethod.GET)
	public String query(@PathVariable("id") int id, RedirectAttributes attrs){
		attrs.addAttribute("merchantId", id);
		attrs.addAttribute("pageNumber", 0);
		attrs.addAttribute("pageSize", 20);
		return "redirect:/o2o/coupon/query/condition";
	}

	@RequestMapping(value = "query/condition", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView queryByConditon(@RequestParam(value="merchantId",required=true,defaultValue="0") int merchantId, 
			@RequestParam int pageNumber, @RequestParam int pageSize){
		List<CouponTemplate> couponTemplates = o2oCouponService.findCouponsByMerchantId(merchantId);
		ModelAndView mv = new ModelAndView("/coupon/coupon");
		mv.addObject("retcode",0);
		mv.addObject("coupons", couponTemplates);
		mv.addObject("merchantId", merchantId);
		return mv;
	}
}

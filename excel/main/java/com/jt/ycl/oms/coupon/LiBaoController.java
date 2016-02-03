/**
 * 
 */
package com.jt.ycl.oms.coupon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
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
import com.jt.core.model.LiBao;
import com.jt.utils.JSONSerializer;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="o2o/libao")
@OMSPermission(permission = Permission.O2O_MERCHANT_MGMT)
public class LiBaoController {
	
	@Autowired
	private LiBaoService liBaoService;
	
	@Autowired
	private O2OCouponService o2oCouponService;
	
	@RequestMapping(value = "enter/{o2oMerchantId}/add", method = RequestMethod.GET)
	public ModelAndView enterAdd(@PathVariable("o2oMerchantId") int o2oMerchantId){
		ModelAndView mv = new ModelAndView("/coupon/addLiBao");
		List<CouponTemplate> templates = o2oCouponService.findCouponsByMerchantId(o2oMerchantId);
		mv.addObject("couponTemplates", templates);
		mv.addObject("o2oMerchantId", o2oMerchantId);
		mv.addObject("id", 0); //为避免提交时为空，这里设置为0
		return mv;
	}
	
	/**
	 * 新增礼包
	 * @return
	 */
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public String add(LiBao liBaoFormBean){
		LiBao liBao = null;
		if(liBaoFormBean.getId()>0){
			liBao = liBaoService.findById(liBaoFormBean.getId());
		}else{
			liBao = new LiBao();
		}
		liBao.setName(liBaoFormBean.getName());
		liBao.setAmount(liBaoFormBean.getAmount());
		liBao.setO2oMerchantId(liBaoFormBean.getO2oMerchantId());
		String couponTemplates = liBaoFormBean.getCouponTemplates();
		if(StringUtils.isNotEmpty(couponTemplates)){
			String[] templates = couponTemplates.split(",");
			List<Integer> templateIds= new ArrayList<Integer>();
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for(String template : templates){
				String[] coupon = template.split(":");
				int templateId = Integer.parseInt(coupon[0]);
				templateIds.add(templateId);
				int count = Integer.parseInt(coupon[1]);
				map.put(templateId, count);
			}
			List<CouponTemplate> cts = o2oCouponService.findCouponTemplatesByIds(templateIds);
			if(CollectionUtils.isNotEmpty(cts)){
				List<CouponTemplate> jsonCouponTemplates = new ArrayList<CouponTemplate>();
				for(CouponTemplate couponTemplate : cts){
					if(map.get(couponTemplate.getId())>1){
						int count = map.get(couponTemplate.getId()) ;
						for(int j=0; j<count; j++){ //多个相同的模板
							jsonCouponTemplates.add(couponTemplate);
						}
					}else{
						jsonCouponTemplates.add(couponTemplate);
					}
				}
				liBao.setCouponTemplates(JSONSerializer.serialize(jsonCouponTemplates));
			}
		}
		liBaoService.createLiBao(liBao);
		return "redirect:/o2o/libao/"+liBaoFormBean.getO2oMerchantId()+"/query";
	}
	
	/**
	 * 进入礼包编辑页面
	 * @return
	 */
	@RequestMapping(value = "enter/{id}/{o2oMerchantId}/edit", method = RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable("id") int id, @PathVariable("o2oMerchantId") int o2oMerchantId){
		List<CouponTemplate> templates = o2oCouponService.findCouponsByMerchantId(o2oMerchantId);
		ModelAndView mv = new ModelAndView("/coupon/addLiBao");
		LiBao liBao = liBaoService.getLiBaoById(id);
		List<LiBaoCouponTemplate> liBaoCouponTemplates = liBaoService.getLiBaoCouponTemplatesById(id);
		mv.addObject("liBao", liBao);
		mv.addObject("liBaoCouponTemplates", liBaoCouponTemplates);
		mv.addObject("couponTemplates", templates);
		mv.addObject("id", id);
		mv.addObject("o2oMerchantId", o2oMerchantId);
		return mv;
	}
	
	@RequestMapping(value = "{id}/delete", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap delete(@PathVariable int id) {
		liBaoService.deleteLiBaoById(id);
		ModelMap modelMap = new ModelMap("retcode",0);
		return modelMap;
	}
	
	/**
	 * 进入商家的礼包界面
	 * @return
	 */
	@RequestMapping(value = "{id}/query", method = RequestMethod.GET)
	public String query(@PathVariable("id") int id, RedirectAttributes attrs){
		attrs.addAttribute("merchantId", id);
		return "redirect:/o2o/libao/query/condition";
	}
	
	@RequestMapping(value = "query/condition", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView queryByConditon(@RequestParam(value="merchantId",required=true,defaultValue="0") int merchantId){
		List<LiBao> libaos = liBaoService.findLiBaosByMerchantId(merchantId);
		ModelAndView mv = new ModelAndView("/coupon/liBao");
		mv.addObject("retcode",0);
		mv.addObject("libaos", libaos);
		mv.addObject("merchantId", merchantId);
		return mv;
	}
}

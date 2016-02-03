/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.O2OMerchant;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value="o2o/merchant")
@OMSPermission(permission = Permission.O2O_MERCHANT_MGMT)
public class O2OMerchantController {
	
	@Autowired
	private O2OMerchantService o2oMerchantService;
	
	@Autowired
	private VehicleMerchantService merchantService;

	/**
	 * 进入卡券商家首页
	 * @return
	 */
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public ModelAndView query(){
		ModelAndView mv = new ModelAndView("/merchant/o2oMerchant");
		return mv;
	}
	
	/**
	 * 进入卡券商家首页
	 * @return
	 */
	@RequestMapping(value = "enter/add", method = RequestMethod.GET)
	public ModelAndView enterAdd(){
		ModelAndView mv = new ModelAndView("/merchant/addO2OMerchant");
		mv.addObject("id", 0);
		return mv;
	}
	
	@RequestMapping(value="/openarea", method=RequestMethod.GET)
	@ResponseBody
	public String getOpenArea(){
		String areaJson = merchantService.findOpenedArea();
		return areaJson;
	}
	
	/**
	 * 进入卡券商家首页
	 * @return
	 */
	@RequestMapping(value = "add", method = RequestMethod.POST)
	public String add(MultipartHttpServletRequest request, O2OMerchant o2oMerchantFormBean){
		O2OMerchant o2oMerchant = null;
		if(o2oMerchantFormBean.getId()>0){
			o2oMerchant = o2oMerchantService.findById(o2oMerchantFormBean.getId());
		}else{
			o2oMerchant = new O2OMerchant();
		}
		o2oMerchant.setProvince(o2oMerchantFormBean.getProvince());
		o2oMerchant.setCityCode(o2oMerchantFormBean.getCityCode());
		o2oMerchant.setName(o2oMerchantFormBean.getName());
		o2oMerchant.setAlias(o2oMerchantFormBean.getAlias());
		o2oMerchant.setAddress(o2oMerchantFormBean.getAddress());
		o2oMerchant.setLatitude(o2oMerchantFormBean.getLatitude());
		o2oMerchant.setLongitude(o2oMerchantFormBean.getLongitude());
		o2oMerchant.setContact(o2oMerchantFormBean.getContact());
		o2oMerchant.setPhone(o2oMerchantFormBean.getPhone());
		o2oMerchant.setBankAccountName(o2oMerchantFormBean.getBankAccountName());
		o2oMerchant.setBank(o2oMerchantFormBean.getBank());
		o2oMerchant.setBankAccount(o2oMerchantFormBean.getBankAccount());
		if(o2oMerchantFormBean.getCoupon() && o2oMerchantFormBean.getLipei()){
			o2oMerchant.setServiceCategory(3); //提供卡券包和理赔服务
		}else if(o2oMerchantFormBean.getCoupon() && !o2oMerchantFormBean.getLipei()){
			o2oMerchant.setServiceCategory(1); //只提供卡券服务
		}else if(!o2oMerchantFormBean.getCoupon() && o2oMerchantFormBean.getLipei()){
			o2oMerchant.setServiceCategory(2); //只提供理赔服务
		}
		o2oMerchant.setContent(o2oMerchantFormBean.getContent());
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;  
		MultipartFile file = multipartRequest.getFile("showPicture");
		List<MultipartFile> files = multipartRequest.getFiles("morePicutre");
		o2oMerchantService.createO2OMerchant(o2oMerchant, file, files);
		return "redirect:/o2o/merchant/query";
	}
	
	/**
	 * 进入卡券商家首页
	 * @return
	 */
	@RequestMapping(value = "enter/{id}/edit", method = RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable("id") int id){
		O2OMerchant o2oMerchant = o2oMerchantService.findById(id);
		ModelAndView mv = new ModelAndView("/merchant/addO2OMerchant");
		mv.addObject("merchant", o2oMerchant);
		mv.addObject("id", id);
		return mv;
	}
	
	@RequestMapping(value = "{id}/delete", method = RequestMethod.GET)
	@ResponseBody
	@OMSPermission(permission = Permission.MERCHANT_DELETE)
	public ModelMap delete(@PathVariable int id) {
		o2oMerchantService.deleteO2OMerchantById(id);
		ModelMap modelMap = new ModelMap("retcode",0);
		return modelMap;
	}
	
	/**
	 * 商家按条件查询
	 * @return
	 */
	@RequestMapping(value = "/query/condition", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap queryByConditon(String province, int cityCode, String name, int serviceCategory, int pageNumber, int pageSize){
		Page<O2OMerchant> page = o2oMerchantService.findO2OMerchants(province, cityCode, name, serviceCategory, pageNumber, pageSize);
		ModelMap mv = new ModelMap("retcode",0);
		mv.addAttribute("merchants", page.getContent());
		mv.addAttribute("totalPages", page.getSize());
		mv.addAttribute("totalItems", page.getTotalElements());
		return mv;
	}
}

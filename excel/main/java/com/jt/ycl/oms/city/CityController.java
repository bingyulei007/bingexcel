package com.jt.ycl.oms.city;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.core.model.Region;
import com.jt.exception.CommonLogicException;

@Controller
@RequestMapping(value="city")
public class CityController {
	
	@Autowired
	private CityService cityService;
	
	@RequestMapping(value="province", method=RequestMethod.GET)
	@ResponseBody
	public Map<Integer, String> getAllProvince(){
		return cityService.getAllProvince();
	}
	
	@RequestMapping(value="{provinceCode}", method=RequestMethod.GET)
	@ResponseBody
	public Map<Integer, String> getCitiesByProvinceCode(@PathVariable("provinceCode") int provinceCode){
		return cityService.getCitiesByProvinceCode(provinceCode);
	}
	
	/**
	 * 查询保险公司在城市开办业务的区域
	 */
	@RequestMapping(value="/region/{cityCode}/{icCode}", method=RequestMethod.GET)
	@ResponseBody
	public List<String> getRegionList(@PathVariable int cityCode, @PathVariable int icCode) throws CommonLogicException, Exception {
		return cityService.getRegionList(cityCode, icCode);
	}
	
	/**
	 * 加载城市下的所有区域
	 * @param cityCode
	 * @return
	 */
	@RequestMapping(value={"{cityCode}/region"},method=RequestMethod.GET)
	@ResponseBody
	public ModelMap getAllRegionByCity(@PathVariable int cityCode){
		List<Region> regions = cityService.getAllRegionsByCity(cityCode);
		ModelMap modelMap = new ModelMap("retcode",0);
		modelMap.addAttribute("regions",regions);
		return modelMap;
	}
}

package com.jt.ycl.oms.city;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jt.core.model.ExpressCompany;

/**  
 * 创建时间：2015-12-8下午3:18:20  
 * 项目名称：ycl-oms  
 * @author shizhongtao  
 * @version 1.0   
 * @since JDK 1.7
 * 文件名称：ExpressCompanyController.java  
 * 类说明：  不同城市的配送公司管理
 */
@Controller
@RequestMapping(value="city/express")
public class ExpressCompanyController {
	@Autowired
	private ExpressCompanyService companyService;
	public ModelMap queryList(String provinceCode,String cityCode){
		ModelMap mm=new ModelMap();
		if(StringUtils.isBlank(provinceCode)||provinceCode.equals("0")){
			provinceCode=null;
		}
		if(StringUtils.isBlank(cityCode)||cityCode.equals("0")){
			cityCode=null;
		}
		mm.addAttribute("errcode", 0);
		List<ExpressCompany> list=companyService.queryList(provinceCode,cityCode);
		return mm;
	}
}

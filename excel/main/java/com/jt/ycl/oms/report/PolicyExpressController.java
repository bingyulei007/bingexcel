/**
 * 
 */
package com.jt.ycl.oms.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.report.bean.PolicyExpressBean;
import com.jt.ycl.oms.report.util.ExcelHanderUtil;

/**
 * @author wuqh
 * 
 */
@Controller
@RequestMapping(value = "report/express")
@OMSPermission(permission = Permission.REPORT_USER_MGMT)
public class PolicyExpressController {

	@Autowired
	private PolicyExpressService policyExpressService;

	@RequestMapping(value = "/enter", method = RequestMethod.GET)
	public ModelAndView enter() {
		ModelAndView mv = new ModelAndView("/report/insurance/policyExpress");
		return mv;
	}

	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap query(String licenseNo, int cityCode, int expressCompany, int status, String iccode, String startDate, String endDate) {
		ModelMap mm = new ModelMap("retcode", 0);
		List<PolicyExpressBean> policyExpress = policyExpressService.queryPolicyExpress(licenseNo, cityCode, expressCompany, status, iccode, startDate, endDate);
		mm.put("policyExpress", policyExpress);
		if (CollectionUtils.isNotEmpty(policyExpress)) {
			double totalExpressCost = 0d;
			double totalPosFee = 0d;
			double totalPremium = 0d;
			for (PolicyExpressBean policyExpressBean : policyExpress) {
				totalExpressCost += policyExpressBean.getExpressCost();
				totalPosFee += policyExpressBean.getPosFee();
				totalPremium += policyExpressBean.getPremium();
			}
			mm.put("policyCount", policyExpress.size());
			mm.put("totalExpressCost", totalExpressCost);
			mm.put("totalPosFee", totalPosFee);
			mm.put("totalPremium", totalPremium);
		} else {
			mm.put("totalItems", 0);
		}
		return mm;
	}
/**
 * 
 * <p>Title: 更新订单的配送状态<／p>
 * <p>Description: 从配送公司那边拿过来表格，根据id来查找对应的系统数据，然后更新状态和刷卡手续费<／p>
 * @param excel
 * @return
 * @throws Exception
 */
	@RequestMapping(value = "/excel/upload", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap upload(@RequestParam("file") MultipartFile excel) throws Exception {
		ModelMap mm = new ModelMap("errcode", 0);
		int re = policyExpressService.updatePolicyWuliuStatusFromSuzhouYGExcel(excel);
		mm.addAttribute("num", re);
		return mm;
	}

	@RequestMapping(value = "/excel/export", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<byte[]> upload(int cityCode, int expressCompany, int status, String iccode, String startDate, String endDate) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		// File file = securityGroupService.getModuleExcel();
		ByteArrayOutputStream os;
		try {
			os = (ByteArrayOutputStream) policyExpressService.getResultExcelStream(cityCode, expressCompany, status, iccode, startDate, endDate);
		} catch (Exception e) {

			e.printStackTrace();
			throw new IllegalStateException(e.getMessage());
		}

		byte[] byteArr = os.toByteArray();
		os.close();
		String name = "express";
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", name + ".xls");
		return new ResponseEntity<byte[]>(byteArr, headers, HttpStatus.CREATED);
	}
	@RequestMapping(value = "/change/acquire", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap  changeToAcquire(String  policyId){
		ModelMap mm = new ModelMap("errcode", 0);
		
		int re=policyExpressService.changeToAcquire(policyId);
		mm.addAttribute("body", re);
		if(re==0){
			mm.addAttribute("errcode", 1);
			mm.addAttribute("errmsg","数据已经更改");
		}
		return mm;
	}
	/**
	 * <p>Title: changeAlltoInTheWay</p>
	 * <p>Description: 更改所有已提到变为 inthe  way。表示已经交给配送公司。</p>
	 * @return
	 */
	@RequestMapping(value = "/change/way", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap  changeAlltoInTheWay(){
		ModelMap mm = new ModelMap("errcode", 0);
		return mm;
	}
}

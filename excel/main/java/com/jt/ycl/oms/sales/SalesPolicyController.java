/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.model.Comments;
import com.jt.core.model.PolicyFlag;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.ycl.oms.insurance.PolicyService;
import com.jt.ycl.oms.sales.bean.Price;

/**
 * @author Andy Cui
 */
@RequestMapping("/sales/policy")
@RestController
public class SalesPolicyController {

	@Autowired
	private MyPolicyService myPolicyService;
	
	@Autowired
	private PolicyService policyService;

	/**
	 * 进入按保单状态和标记页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/{channelCode}/enter", method = RequestMethod.GET)
	public ModelAndView enter(@PathVariable int channelCode) {
		ModelAndView mv = new ModelAndView("/sales/salesPolicy");
		mv.addObject("channelCode", channelCode);
		return mv;
	}

	/**
	 * 进入按保单状态和标记页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/supplement/{policyId}", method = RequestMethod.GET)
	public ModelAndView supplement(@PathVariable String policyId) {
		if(StringUtils.isEmpty(policyId)){
			
			throw new CommonLogicException(1, "没有保单id，请联系管理员");
		}
		ModelAndView mv = new ModelAndView("/sales/salesSupplement");
		List<String> list = policyService.getPolicyAttachmentUrls(policyId);
		if (list.size() > 0) {
			StringBuilder imgsB = new StringBuilder();
			for (String url : list) {
				imgsB.append(url).append(",");
			}
			String imgs = imgsB.substring(0, imgsB.length() - 1);
			mv.addObject("imgs", imgs);
		}
		mv.addObject("policyId", policyId);
		return mv;
	}

	/**
	 * 进入按list pic
	 * 
	 * @return
	 */
	@RequestMapping(value = "/supplement/{policyId}/{num}", method = RequestMethod.GET)
	public ModelAndView supplementListView(@PathVariable String policyId, @PathVariable int num) {
		ModelAndView mv = new ModelAndView("/sales/salesImgList");
		List<String> list = policyService.getPolicyAttachmentUrls(policyId);

		mv.addObject("imgs", list);

		mv.addObject("policyId", policyId);
		mv.addObject("num", num);
		return mv;
	}

	@RequestMapping(value = "/supplement/upload", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap submitSupplementMaterial(HttpServletRequest request, @RequestParam(value = "policyId") String policyId,
			@RequestParam(value = "img", required = false) String[] imageFile) throws CommonLogicException, Exception {
		ModelMap mm = new ModelMap();
		Enumeration<String> parameterNames = request.getParameterNames();

		if (null == imageFile || imageFile.length <= 0) {
			mm.put("errcode", 1);
			mm.put("errmsg", "请选择需要上传的图片");
			return mm;
		}
		for (String imgData : imageFile) {

			String regex = "/[a-zA-Z]+;";
			// policyService.savePic(file, inputStream);
			Pattern p = Pattern.compile(regex);
			Matcher matcher = p.matcher(imgData);
			if (matcher.find()) {
				String suffix = matcher.group();
				suffix = suffix.substring(1, suffix.length() - 1);
				StringBuilder builder = new StringBuilder();
				String file = builder.append(policyId).append("/").append(UUID.randomUUID().toString().replaceAll("-", "")).append(".").append(suffix)
						.toString();
				int start = imgData.indexOf(",");
				imgData = imgData.substring(start + 1);
				byte[] bytes = Base64.decodeBase64(imgData);
				ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentLength(inputStream.available());

				policyService.savePic(file, inputStream);

			} else {

				mm.put("errcode", 1);
				mm.put("errmsg", "上传文件有误");
				return mm;
			}

		}

		mm.put("errcode", 0);
		return mm;
	}

	/**
	 * 按状态查看我的保单或我的某个商户的保单，如果要查所有商户的保单，将channelCode赋值-1
	 */
	@RequestMapping("/{channelCode}/status")
	@ResponseBody
	public Map<Integer, Long> myPolicyByStatus(@CookieValue(required = false, value = "salesId") String salesId, @PathVariable long channelCode) {
		return myPolicyService.countMyPolicyByStatus(salesId, channelCode);
	}

	/**
	 * 按标记查看我的保单或我的某个商户的保单，如果要查所有商户的保单，将channelCode赋值-1
	 */
	@RequestMapping("/{channelCode}/flag")
	@ResponseBody
	public Map<Integer, Long> myPolicyByFlag(@CookieValue(required = false, value = "salesId") String salesId, @PathVariable long channelCode) {
		return myPolicyService.countMyPolicyByFlag(salesId, channelCode);
	}

	/**
	 * 进入我的保单列表
	 * 
	 * @return
	 */
	@RequestMapping(value = "/list/{type}/{channelCode}/{value}", method = RequestMethod.GET)
	public ModelAndView listPolicy(@PathVariable int type, @PathVariable long channelCode, @PathVariable int value) {
		ModelAndView mv = new ModelAndView("/sales/policy-list");
		if (type == 1) { // 按状态
			mv.addObject("status", value);
			mv.addObject("flag", -1);
		} else if (type == 2) { // 按标记
			mv.addObject("flag", value);
			mv.addObject("status", -1);
		}
		mv.addObject("channelCode", channelCode);
		return mv;
	}
	
	/**
	 * 通过搜索车牌号，进入我的保单列表
	 */
	@RequestMapping(value = "/list/search", method = RequestMethod.POST)
	public ModelAndView listPolicy(@RequestParam String licenseNo) {
		ModelAndView mv = new ModelAndView("/sales/policy-list");
		mv.addObject("status", -1);
		mv.addObject("flag", -1);
		mv.addObject("licenseNo", licenseNo);
		return mv;
	}

	/**
	 * 按状态查询保单，返回该状态下的保单列表 如果channelCode大于0，则查询范围限制在该商户名下，否则请将channelCode赋值-1
	 */
	@RequestMapping("/{channelCode}/status/{status}/{page}")
	public ModelMap listByStatus(@CookieValue(required = false, value = "salesId") String salesId, @PathVariable Long channelCode, @PathVariable int status,
			@PathVariable int page) {
		ModelMap modelMap = new ModelMap("title", PolicyStatus.getName(status));
		List<PolicyItem> policyList = myPolicyService.listByStatusOrFlag(salesId, channelCode, true, status, page);
		modelMap.addAttribute("policyList", policyList);
		modelMap.addAttribute("status", status);
		return modelMap;
	}

	/**
	 * 按标记查询保单，返回该标记下的保单列表 如果channelCode大于0，则查询范围限制在该商户名下，否则请将channelCode赋值-1
	 */
	@RequestMapping("/{channelCode}/flag/{flag}/{page}")
	public ModelMap listByFlag(@CookieValue(required = false, value = "salesId") String salesId, @PathVariable Long channelCode, @PathVariable int flag,
			@PathVariable int page) {
		ModelMap modelMap = new ModelMap("title", PolicyFlag.getName(flag));
		List<PolicyItem> policyList = myPolicyService.listByStatusOrFlag(salesId, channelCode, false, flag, page);
		modelMap.addAttribute("policyList", policyList);
		modelMap.addAttribute("flag", flag);
		return modelMap;
	}
	
	/**
	 * 根据车牌号搜索保单，不可以查询它人名下的保单
	 * 
	 * @param salesId
	 * @param licenseNo
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public ModelMap searchByLicenseNo(@CookieValue(required = false, value = "salesId") String salesId, @RequestParam(value = "licenseNo") String licenseNo) {
		licenseNo = licenseNo.toUpperCase();
		ModelMap modelMap = new ModelMap("title", licenseNo);
		List<PolicyItem> policyList = myPolicyService.searchPolicyByLicenseNo(licenseNo, salesId);
		modelMap.addAttribute("policyList", policyList);
		modelMap.addAttribute("status", 0);
		return modelMap;
	}

	@RequestMapping(value = "/{policyId}/comments", method = RequestMethod.POST)
	public Comments addComments(@PathVariable String policyId, @CookieValue String salesName, @RequestParam String content) throws Exception {
		salesName = URLDecoder.decode(salesName, "UTF-8");
		return myPolicyService.addComments(policyId, salesName, content);
	}

	@RequestMapping(value = "/{policyId}/detail", method = RequestMethod.GET)
	public List<Price> detail(@PathVariable String policyId) throws Exception {
		return myPolicyService.getPolicyPriceDetail(policyId);
	}

	@RequestMapping(value = "/{policyId}/action/{type}", method = RequestMethod.GET)
	public void action(@PathVariable String policyId, @CookieValue String salesName, @PathVariable int type) {
		myPolicyService.doAction(policyId, salesName, type);
	}

	/**
	 * 查看保单附件，返回的数组中保存着附件的完整URL（存储在阿里云OSS上）
	 */
	@RequestMapping(value = "/{policyId}/attachment", method = RequestMethod.GET)
	public List<String> viewAttachmentList(@PathVariable String policyId) {
		return policyService.getPolicyAttachmentUrls(policyId);
	}

	/**
	 * 删除一个保单附件
	 */
	@RequestMapping(value = "/supplement/delete/{policyId}", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap deleteAttachment(@PathVariable String policyId, @RequestParam String url) {
		String attachmentId = url.replace("http://auditimg.oss-cn-hangzhou.aliyuncs.com/", "");
		// http://auditimg.oss-cn-hangzhou.aliyuncs.com/JTK4f5074131c8947f6998c4d1d620c15ff/51f1699bf45146e7bfa0511080a7f84a.jpg
		// policyId 目前没有用，留作以后验证使用
		policyService.deleteAttachment(policyId, attachmentId);
		ModelMap mm = new ModelMap();
		mm.put("errcode", 0);
		return mm;
	}
}
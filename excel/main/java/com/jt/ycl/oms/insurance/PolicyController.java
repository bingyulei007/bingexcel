package com.jt.ycl.oms.insurance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.jt.core.ErrorCode;
import com.jt.core.SmsService;
import com.jt.core.SmsTemplateId;
import com.jt.core.insurance.BInsurance;
import com.jt.core.insurance.CInsurance;
import com.jt.core.insurance.CarShipTaxInfo;
import com.jt.core.insurance.ICCode;
import com.jt.core.insurance.InquiryResult;
import com.jt.core.insurance.InsuranceComparator;
import com.jt.core.insurance.StandardRiskKindCode;
import com.jt.core.insurance.SubmitPolicyFormBean;
import com.jt.core.model.BaojiaRecord;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.Comments;
import com.jt.core.model.Insurance;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantCommissionRate;
import com.jt.core.model.OmsUser;
import com.jt.core.model.PolicyFee;
import com.jt.core.model.PolicyFlag;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DWZUtils;
import com.jt.utils.DateUtils;
import com.jt.utils.InsuranceOrderNoUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.account.AccountService;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;
import com.jt.ycl.oms.car.CarService;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.merchant.VehicleMerchantService;

@Controller
@RequestMapping(value = "insurance")
@OMSPermission(permission = Permission.INSURANCE_POLICY_MGMT)
public class PolicyController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private PolicyService policyService;

	@Autowired
	private VehicleMerchantService vehicleMerchantService;

	@Autowired
	private CarService carService;

	@Autowired
	private CityService cityService;

	@Autowired
	private SmsService smsService;

	@Autowired
	private BaojiaRecordService baojiaService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private PolicyFeeService policyFeeService;
	
	@RequestMapping(value = "policy")
	public ModelAndView list(@RequestParam(value = "number", required = false, defaultValue = "") String number,
			@RequestParam(value = "provinceCode", required = false, defaultValue = "0") int provinceCode,
			@RequestParam(value = "cityCode", required = false, defaultValue = "0") int cityCode,
			@RequestParam(value = "companyCode", required = false, defaultValue = "0") int companyCode,
			@RequestParam(value = "startDate", required = false, defaultValue = "") String startDate,
			@RequestParam(value = "endDate", required = false, defaultValue = "") String endDate,
			@RequestParam(value = "status", required = false, defaultValue = "0") int status,
			@RequestParam(value = "priority", required = false, defaultValue = "0") int priority,
			@RequestParam(value = "flag", required = false, defaultValue = "0") int flag,
			@RequestParam(value = "customerservice", required = false, defaultValue = "0") int customerservice,
			@RequestParam(value = "salesMan", required = false, defaultValue = "") String salesId,
			@RequestParam(value = "channelCode", required = false, defaultValue = "0") long channelCode,
			@RequestParam(value = "policyType", required = false, defaultValue = "0") int policyType,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page, HttpSession session) {
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		if(cityCode == 0) {
			cityCode = accountInfo.getCityCode();
		}
		ModelMap mm = new ModelMap();
		mm.put("number", number);
		mm.put("cityCode", cityCode);
		mm.put("companyCode", companyCode);
		mm.put("startDate", startDate);
		mm.put("endDate", endDate);
		mm.put("provinceCode", provinceCode);
		mm.put("channelCode", channelCode);
		mm.put("policyType", policyType);

		// 第一次进入页面，如果是客服人员，默认查询当前登录客服人员的保单
		if (customerservice == 0 && StringUtils.equals(accountInfo.getRole().getName(), Role.OMS_HEBAO_USER)) {
			customerservice = accountInfo.getId();
		}
		if (customerservice == 0 && !StringUtils.equals(accountInfo.getRole().getName(), Role.OMS_HEBAO_USER)) {
			customerservice = -1;
		}
		if (customerservice == -2) {//过滤没有人认领的保单
			customerservice = 0;
		}
		mm.put("customerservice", customerservice);
		if (status == 0 && StringUtils.equals(accountInfo.getRole().getName(), Role.CUSTOMER_SERVICE)) {
			//如果是专门负责信息校对的员工登录，那么需要查询出所有状态下的保单
			status = -1;
		}
		mm.put("status", status);
		if (StringUtils.isNotEmpty(endDate)) {
			endDate += " 23:59:59";
		}

		if (StringUtils.equals(accountInfo.getRole().getName(), Role.BD_USER)) {
			salesId = accountInfo.getUserId();
		}
		mm.put("salesMan", salesId);

		Map<String, Object> map = policyService.queryInsurancePolicy(number, cityCode, companyCode, startDate, endDate, status, priority, flag,
				customerservice, salesId, channelCode, policyType, page);

		mm.put("recordCount", map.get("recordCount"));
		mm.put("totalPage", map.get("totalPage"));
		mm.put("currentPage", map.get("currentPage"));
		mm.put("result", map.get("result"));

		Map<String, Long> countMap = policyService.countGroupByStatus(customerservice);
		mm.put("countMap", countMap);

		List<OmsUser> bdUsers = accountService.findByRole(Role.BD_USER);
		List<OmsUser> hebaoUsers = accountService.findByRole(Role.OMS_HEBAO_USER);
		mm.put("bdUsers", bdUsers);
		mm.put("hebaoUsers", hebaoUsers);
		
		
		if (StringUtils.equals(accountInfo.getRole().getName(), Role.BD_USER)) {
			// BD用户进入另外一个页面
			return new ModelAndView("insurance/bdInsurancePolicy", mm);
		} else {
			return new ModelAndView("insurance/insurancePolicy", mm);
		}
	}

	@RequestMapping(value = "/{policyId}/{targetStatus}/status", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap changePolicyStatus(@PathVariable("policyId") String policyId, @PathVariable("targetStatus") int targetStatus, HttpServletRequest request,
			HttpSession session) {
		InsurancePolicy policy = policyService.getInsurancePolicyById(policyId);
		ModelMap mm = new ModelMap();
		if (policy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单不存在，可能已被删除，请刷新页面.");
			return mm;
		}
		if (policy.getCustomerservice() == 0) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单无处理人，请先分配处理人！");
			return mm;
		}
		String comments = request.getParameter("comments");
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		policy = policyService.changePolicyStatus(policy, targetStatus, comments, userName);
		if (policy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "非法操作，当前状态下的保单不允许执行该操作！");
		} else {
			mm.put("errcode", 0);
		}
		return mm;
	}

	@RequestMapping(value = "/{targetStatus}/status/batch", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap batchChangePolicyStatus(@RequestParam("policyIds[]") List<String> policyIds, @PathVariable("targetStatus") int targetStatus,
			HttpSession session) {
		ModelMap mm = new ModelMap();
		if (CollectionUtils.isEmpty(policyIds)) {
			mm.put("errcode", 1);
			mm.put("errmsg", "请选择需要操作的保单");
			return mm;
		}
		List<InsurancePolicy> policyList = policyService.getPolicyInPolicyIds(policyIds);
		List<PolicyFee> policyFees = policyFeeService.findByPolicyIdIn(policyIds);
		if (CollectionUtils.isEmpty(policyList)) {
			mm.put("errcode", 1);
			mm.put("errmsg", "未查询到需要操作的保单");
			return mm;
		}
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		int currentStatus = 0;
		for (int i = 0; i < policyList.size(); i++) {
			InsurancePolicy policy = policyList.get(i);
			if (policy.getCustomerservice() == 0) {
				mm.put("errcode", 1);
				mm.put("errmsg", policy.getCarNumber() + "尚未分配客服");
				return mm;
			}
			PolicyFee policyFee = policyFees.get(i);
			policy.setUpdateTime(new Date());
			if (i == 0) {
				currentStatus = policy.getStatus();
			} else {
				if (currentStatus != policy.getStatus()) {
					mm.put("errcode", 1);
					mm.put("errmsg", "保单状态不一致");
					return mm;
				}
			}
			String action = null;
			if ((currentStatus == PolicyStatus.UNDERWRITE_OK.value() || currentStatus == PolicyStatus.CHUDAN_AGREE.value())
					&& targetStatus == PolicyStatus.CHUDAN_FINISHED.value()) {
				// 只有处于核保通过状态的保单才能执行出单操作
				policy.setStatus(PolicyStatus.CHUDAN_FINISHED.value());
				action = "已完成缴费，出单完成";
			} else if (currentStatus == PolicyStatus.CHUDAN_FINISHED.value() && targetStatus == PolicyStatus.DELIVERING.value()) {
				// 处于已出单状态的保单，可以进入正在配送状态
				policy.setStatus(PolicyStatus.DELIVERING.value());
				action = "保单正在配送";
			} else if ((currentStatus == PolicyStatus.CHUDAN_FINISHED.value() || currentStatus == PolicyStatus.DELIVERING.value())
					&& targetStatus == PolicyStatus.DISPATCHED.value()) {
				// 处于已出单状态、正在配送状态的保单，可以进入已配送状态
				policy.setStatus(PolicyStatus.DISPATCHED.value());
				action = "保单配送完成";
			} else if (currentStatus == PolicyStatus.DISPATCHED.value() && targetStatus == PolicyStatus.PAID.value()) {
				// 处于已配送状态的保单，可以执行保费入账操作，保单状态进入保费已入账
				policy.setStatus(PolicyStatus.PAID.value());
				action = "我检查了一下， 保费已入公司帐户";
			} else if (currentStatus == PolicyStatus.PAID.value() && targetStatus == PolicyStatus.BROKERAGE_GRANTED.value()) {
				// 处于保费已入账的保单，可以执行发放佣金操作
				policy.setStatus(PolicyStatus.BROKERAGE_GRANTED.value());
				policy.setSettleStatus(1);
				action = "我检查了一下，佣金确实发了";
				policyFee.setReturnCashDate(new Date());
			} else if (currentStatus == PolicyStatus.PAID.value() && targetStatus == PolicyStatus.TRANSACTION_OK.value()) {
				// 处于保费已入账的保单，可以直接进入交易完成状态
				policy.setStatus(PolicyStatus.TRANSACTION_OK.value());
				action = "投保结束，交易完成";
			} else if (currentStatus == PolicyStatus.BROKERAGE_GRANTED.value() && targetStatus == PolicyStatus.TRANSACTION_OK.value()) {
				// 佣金已发放的保单，可以进入交易完成状态
				policy.setStatus(PolicyStatus.TRANSACTION_OK.value());
				action = "投保结束，交易完成";
			} else if (targetStatus == PolicyStatus.TRANSACTION_FAILED.value()
					&& (currentStatus == PolicyStatus.NEED_UNDERWRITE.value() || currentStatus == PolicyStatus.UNDERWRITE_OK.value())) {
				// 处于待核保、核保通过状态下的保单才能执行交易失败操作
				policy.setStatus(PolicyStatus.TRANSACTION_FAILED.value());
				action = "很可惜，交易失败";
			} else if (currentStatus == PolicyStatus.UNDERWRITE_OK.value() && targetStatus == PolicyStatus.CHUDAN_AGREE.value()) {
				policy.setStatus(PolicyStatus.CHUDAN_AGREE.value());
				action = "这张保单我同意出单，请缴费出单吧";
			} else if ((currentStatus == PolicyStatus.UNDERWRITE_OK.value() || currentStatus == PolicyStatus.NEED_UNDERWRITE.value()
					|| currentStatus == PolicyStatus.NO_QUERY_BAOJIA.value() || currentStatus == PolicyStatus.RESPONSE_BAOJIA.value())
					&& targetStatus == PolicyStatus.SUSPENDED.value()) {
				// 记录下挂起前的状态
				policy.setBeforeSuspendedStatus(policy.getStatus());
				policy.setStatus(PolicyStatus.SUSPENDED.value());
				action = "保单有问题，暂时挂起";
			} else {
				mm.put("errcode", 1);
				mm.put("errmsg", "非法操作，当前状态下的保单不允许执行该操作！");
				return mm;
			}
			policyService.addComments(policy.getOrderId(), action, userName);
		}

		if (targetStatus == PolicyStatus.CHUDAN_FINISHED.value()) {
			policyService.chudan(policyList);
		} else {
			policyService.updateInsurancePolicy(policyList);
		}
		if (targetStatus == PolicyStatus.BROKERAGE_GRANTED.value()) {
			policyFeeService.savePolicyFees(policyFees);
		}
		mm.put("errcode", 0);
		return mm;
	}

	@RequestMapping(value = "/{policyId}/remark", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap remark(@PathVariable("policyId") String policyId, @RequestParam("comments") String comment, HttpSession session) {
		ModelMap mm = new ModelMap();
		if (StringUtils.isEmpty(comment)) {
			mm.put("errcode", 0);
			return mm;
		}
		InsurancePolicy policy = policyService.getInsurancePolicyById(policyId);
		if (policy == null) {
			mm.put("errcode", ErrorCode.POLICY_NOT_FOUND);
			mm.put("errmsg", "保单不存在，可能已被删除，请刷新页面.");
			return mm;
		}

		policy.setReadFlag(0);// 标记为未读
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		Comments c = policyService.addComments(policyId, comment, accountInfo.getUserName());
		mm.put("errcode", 0);
		mm.put("comments", c.getName() + " " + DateUtils.convertDateToStr(c.getCreateDate(), "yyyy-MM-dd HH:mm:ss") + " " + c.getContent());
		return mm;
	}

	@RequestMapping(value = "{orderId}/grant", method = RequestMethod.GET)
	@ResponseBody
	@OMSPermission(permission = Permission.GRANT_COUPON)
	public ModelMap grantWashCarCoupons(@PathVariable String orderId, @RequestParam(value = "tickets", required = false, defaultValue = "0") int tickets) {
		ModelMap mm = new ModelMap();
		if (tickets <= 0 || tickets > 52) {
			mm.put("errcode", 1);
			mm.put("errmsg", "洗车券数量只能介于1到52之间！");
			return mm;
		}
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单不存在，请刷新页面后再试！");
			return mm;
		}
		if (policy.getStatus() != 9 && policy.getStatus() != 10) {
			mm.put("errcode", 1);
			mm.put("errmsg", "非法操作，当前状态不能发放洗车券！");
			return mm;
		}
		if (policy.getSumPremium() < 2500) {
			mm.put("errcode", 1);
			mm.put("errmsg", "商业险保费低于2500，不能发放洗车券！");
		} else {
			policyService.grantWashCarCoupon(policy, tickets);
			mm.put("errcode", 0);
		}
		return mm;
	}

	@RequestMapping(value = "{orderId}/detail", method = RequestMethod.GET)
	public ModelAndView policyDetail(@PathVariable("orderId") String orderId) throws Exception {
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		ModelMap mm = new ModelMap();
		if (policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "投保单不存在");
		}
		mm.put("policy", policy);

		String content = policy.getContent();
		ObjectMapper mapper = new ObjectMapper();
		InquiryResult inquiryResult = mapper.readValue(content, InquiryResult.class);
		List<Insurance> insuranceList = inquiryResult.getInsuranceList();
		Collections.sort(insuranceList, new InsuranceComparator());
		Iterator<Insurance> it = insuranceList.iterator();
		boolean md3md4 = false;
		boolean additional = false;
		while (it.hasNext()) {
			Insurance insurance = it.next();
			String riskKindCode = insurance.getRiskKindCode();
			// 太保
			if (policy.getCompanyCode() == ICCode.CPIC) {
				if (StringUtils.equals("MD3", riskKindCode) || StringUtils.equals("MD4", riskKindCode)) {
					if (md3md4) {
						it.remove();
					}
					insurance.setName("车上人员责任险不计免赔");
					md3md4 = true;
				} else if (StringUtils.equals("MZ", riskKindCode)) {
					insurance.setName("附加险不计免赔");
				}
			} else if (policy.getCompanyCode() == ICCode.PAIC || policy.getCompanyCode() == ICCode.TPIC) {
				// 平安 太平
				if (StringUtils.equals("MD3", riskKindCode) || StringUtils.equals("MD4", riskKindCode)) {
					if (md3md4) {
						it.remove();
					}
					insurance.setName("车上人员责任险不计免赔");
					md3md4 = true;
				} else if (StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode) || StringUtils.equals("MX1", riskKindCode)) {
					if (additional) {
						it.remove();
					}
					insurance.setName("附加险不计免赔");
					additional = true;
				}
			} else if (policy.getCompanyCode() == ICCode.YGBX) {
				if (StringUtils.equals("MA", riskKindCode)) {
					insurance.setName("不计免赔");
				}
			} else if (policy.getCompanyCode() == ICCode.GPIC) {
				if (StringUtils.equals("MA", riskKindCode) || StringUtils.equals("MB", riskKindCode) || StringUtils.equals("MD3", riskKindCode)
						|| StringUtils.equals("MD4", riskKindCode) || StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode)
						|| StringUtils.equals("MX1", riskKindCode) || StringUtils.equals("MG", riskKindCode)) {
					it.remove();
				}
				if (StringUtils.equals("M", riskKindCode)) {
					insurance.setName("不计免赔");
				}
			}

			// 如果不是人寿的，还有M险别，直接remove，确保展示出来的数据准确
			if (policy.getCompanyCode() != ICCode.GPIC && StringUtils.equals("M", riskKindCode)) {
				it.remove();
			}
		}
		mm.put("insurances", insuranceList);
		mm.put("inquiry", inquiryResult);

		Car car = carService.findCar(policy.getCarId());
		mm.put("car", car);

		List<String> attachmentUrlList = policyService.getPolicyAttachmentUrls(policy.getOrderId());
		List<String> carAttachmentUrls = carService.getCarAttachmentUrls(policy.getCarId());
		if(carAttachmentUrls.size()>0){
			attachmentUrlList.addAll(carAttachmentUrls);
		}
		mm.put("imgs", attachmentUrlList);
		// 获取保单对应的应收保费、佣金、配送、结算相关信息
		PolicyFee policyFee = policyFeeService.findByPolicyId(orderId);
		mm.put("policyFee", policyFee);
		return new ModelAndView("insurance/policyDetail", mm);
	}

	/**
	 * 人工核保
	 */
	@RequestMapping(value = "{policyId}/underwriting", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap underwritingPolicy(@PathVariable String policyId, HttpServletRequest request) throws Exception {
		ModelMap mm = new ModelMap();
		
		String a = request.getParameter("A");
		String ma = request.getParameter("MA");
		String b = request.getParameter("B");
		String mb = request.getParameter("MB");
		String g = request.getParameter("G");
		String mg = request.getParameter("MG");
		String d3 = request.getParameter("D3");
		String md3 = request.getParameter("MD3");
		String d4 = request.getParameter("D4");
		String md4 = request.getParameter("MD4");
		String f = request.getParameter("F");
		String l = request.getParameter("L");
		String ml = request.getParameter("ML");
		String z = request.getParameter("Z");
		String mz = request.getParameter("MZ");
		String x1 = request.getParameter("X1");
		String mx1 = request.getParameter("MX1");
		String bz = request.getParameter("BZ");//交强险
		String carShipTax = request.getParameter("carShipTax");//车船税
		// 下面3个是人寿专用参数
		String m = request.getParameter("M");
		String d11 = request.getParameter("D11");
		String d12 = request.getParameter("D12");
		
		InsurancePolicy policy = policyService.getInsurancePolicyById(policyId);
		if (policy == null) {
			mm.put("errcode", ErrorCode.POLICY_NOT_FOUND);
			mm.put("errmsg", "投保单不存在");
			return mm;
		}
		if (policy.getCustomerservice() == 0) {
			mm.put("errcode", ErrorCode.ILLEGAL_ACCESS);
			mm.put("errmsg", "没分配处理人怎么能核保呢？");
			return mm;
		}
		if (policy.getStatus() != PolicyStatus.NEED_UNDERWRITE.value()) {
			mm.put("errcode", ErrorCode.ILLEGAL_ARGUMENT);
			mm.put("errmsg", "不允许对处于非待核保状态的投保单进行核保操作！");
			return mm;
		}
		boolean buyBI = true;//是否购买商业险
		if(policy.getStartDate() == null) {
			buyBI = false;
		}
		String content = policy.getContent();
		ObjectMapper mapper = new ObjectMapper();
		
		InquiryResult inquiryResult = mapper.readValue(content, InquiryResult.class);
		List<Insurance> insurances = inquiryResult.getInsuranceList();
		Collections.sort(insurances, new InsuranceComparator());

		// 保费明细
		StringBuilder premiumDetail = new StringBuilder();

		float sumUnderwritePremium = 0;// 核保后的商业险总保费
		float sumUnmPremium = 0;// 不含不计免赔部分总保费
		if(buyBI) {
			for (Insurance insurance : insurances) {
				String riskKindCode = insurance.getRiskKindCode();
				if (StringUtils.equals(StandardRiskKindCode.A, riskKindCode)) {
					if(StringUtils.isNoneBlank(a)) {
						insurance.setUnderwritingPremium(Float.parseFloat(a));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车损险保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MA, riskKindCode)) {
					if(StringUtils.isNotBlank(ma)) {
						insurance.setUnderwritingPremium(Float.parseFloat(ma));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.B, riskKindCode)) {
					if(StringUtils.isNotBlank(b)) {
						insurance.setUnderwritingPremium(Float.parseFloat(b));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("三责险保额" + insurance.getAmount() / 10000 + "万元，保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MB, riskKindCode)) {
					if(StringUtils.isNotBlank(mb)) {
						insurance.setUnderwritingPremium(Float.parseFloat(mb));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.G, riskKindCode)) {
					if(StringUtils.isNotBlank(g)) {
						insurance.setUnderwritingPremium(Float.parseFloat(g));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("盗抢险保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MG, riskKindCode)) {
					if(StringUtils.isNotBlank(mg)) {
						insurance.setUnderwritingPremium(Float.parseFloat(mg));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.D3, riskKindCode)) {
					if(StringUtils.isNotBlank(d3)) {
						insurance.setUnderwritingPremium(Float.parseFloat(d3));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车上人员责任险（司机）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MD3, riskKindCode)) {
					if(StringUtils.isNotBlank(md3)) {
						insurance.setUnderwritingPremium(Float.parseFloat(md3));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.D4, riskKindCode)) {
					if(StringUtils.isNotBlank(d4)) {
						insurance.setUnderwritingPremium(Float.parseFloat(d4));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车上人员责任险（乘客）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MD4, riskKindCode)) {
					if(StringUtils.isNotBlank(md4)) {
						insurance.setUnderwritingPremium(Float.parseFloat(md4));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.F, riskKindCode)) {
					if(StringUtils.isNotBlank(f)) {
						insurance.setUnderwritingPremium(Float.parseFloat(f));
						insurance.setPremium(insurance.getUnderwritingPremium());
						String glassType = "国产玻璃";
						if (insurance.getGlassType() == 2) {
							glassType = "进口玻璃";
						}
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("玻璃单独破碎险【" + glassType + "】，保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.L, riskKindCode)) {
					if(StringUtils.isNotBlank(l)) {
						insurance.setUnderwritingPremium(Float.parseFloat(l));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车身划痕险保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.ML, riskKindCode)) {
					if(StringUtils.isNotBlank(ml)) {
						insurance.setUnderwritingPremium(Float.parseFloat(ml));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.Z, riskKindCode)) {
					if(StringUtils.isNotBlank(z)) {
						insurance.setUnderwritingPremium(Float.parseFloat(z));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("自燃损失险保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MZ, riskKindCode)) {
					if(StringUtils.isNotBlank(mz)) {
						insurance.setUnderwritingPremium(Float.parseFloat(mz));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.X1, riskKindCode)) {
					if(StringUtils.isNotBlank(x1)) {
						insurance.setUnderwritingPremium(Float.parseFloat(x1));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("涉水险保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals(StandardRiskKindCode.MX1, riskKindCode)) {
					if(StringUtils.isNotBlank(mx1)) {
						insurance.setUnderwritingPremium(Float.parseFloat(mx1));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals(StandardRiskKindCode.M, riskKindCode)) {
					if(StringUtils.isNotBlank(m)) {
						insurance.setUnderwritingPremium(Float.parseFloat(m));
						insurance.setPremium(insurance.getUnderwritingPremium());
					}
				} else if (StringUtils.equals("D11", riskKindCode)) {
					if(StringUtils.isNotBlank(d11)) {
						insurance.setUnderwritingPremium(Float.parseFloat(d11));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车上人员责任险（司机）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
					}
				} else if (StringUtils.equals("D12", riskKindCode)) {
					if(StringUtils.isNotBlank(d12)) {
						insurance.setUnderwritingPremium(Float.parseFloat(d12));
						insurance.setPremium(insurance.getUnderwritingPremium());
						sumUnmPremium += insurance.getPremium();
						premiumDetail.append("车上人员责任险（乘客）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
					}
				}
				sumUnderwritePremium += insurance.getUnderwritingPremium();
			}
			
			inquiryResult.getbInsurance().setSumPremium(sumUnderwritePremium);
			policy.setSumPremium(sumUnderwritePremium);
			
			float mTotal = new BigDecimal(sumUnderwritePremium - sumUnmPremium).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			premiumDetail.append("以上不计免赔合计" + mTotal + "元，");
		}
		// 计算总保费
		BigDecimal totalPremium = new BigDecimal(String.valueOf(inquiryResult.getbInsurance().getSumPremium()));
		
		if (StringUtils.isNotEmpty(bz)) {
			inquiryResult.getcInsurance().setUnderwritingPremium(Float.parseFloat(bz));
			inquiryResult.getcInsurance().setSumPremium(Float.parseFloat(bz));
			policy.setSumPremiumCI(Float.parseFloat(bz));

			premiumDetail.append("交强险" + bz + "元，");
			totalPremium = totalPremium.add(new BigDecimal(String.valueOf(inquiryResult.getcInsurance().getSumPremium())));
		}
		if (StringUtils.isNotEmpty(carShipTax)) {
			inquiryResult.getCarShipTaxInfo().setUnderwritingPremium(Float.parseFloat(carShipTax));
			inquiryResult.getCarShipTaxInfo().setCarShipTax(Float.parseFloat(carShipTax));
			policy.setCarShipTax(Float.parseFloat(carShipTax));

			premiumDetail.append("车船税" + carShipTax + "元，");
			totalPremium = totalPremium.add(new BigDecimal(String.valueOf(inquiryResult.getCarShipTaxInfo().getCarShipTax())));
		}
		inquiryResult.setTotalPremium(totalPremium.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
		premiumDetail.append("保费总计" + inquiryResult.getTotalPremium() + "元");
		
		String newContent = mapper.writeValueAsString(inquiryResult);
		policy.setContent(newContent);

		PolicyFee policyFee = policyFeeService.findByPolicyId(policyId);
		if (policy.getChannelCode() > 0) {
			MerchantCommissionRate rate = policyService.findCommissionRateByCompanyCode(policy.getCompanyCode());
			policyFee.setCommissionRate(rate.getRate() / 100f);// 返点的点数
			BigDecimal returndCash = new BigDecimal(policy.getSumPremium() * rate.getRate() / 100);
			policyFee.setReturnedCash(returndCash.setScale(2, RoundingMode.HALF_UP).floatValue());// 返现金额
			policyFeeService.savePolicyFee(policyFee);
		}

		AccountInfo accountInfo = (AccountInfo) request.getSession().getAttribute("user");
		String commentString = request.getParameter("comments");
		if (StringUtils.isEmpty(commentString)) {
			commentString = "核保通过，可以随时出单";
		}
		Comments c = policyService.addComments(policyId, commentString, accountInfo.getUserName());
		mm.put("comments", c.getName() + " " + DateUtils.convertDateToStr(c.getCreateDate(), "yyyy-MM-dd HH:mm:ss") + " " + c.getContent());

		policy.setStatus(PolicyStatus.UNDERWRITE_OK.value());
		policyFee.setPremiumDueAmount(policy.getTotalPremium());
		/*
		 * 核保通过的单子默认设置成商户需要和车主沟通、确认
		 */
		policy.setFlag(PolicyFlag.NEED_CHANNEL_COMMUNICATE_WITH_VEHICLE_OWNER.value());
		policy.setUpdateTime(new Date());
		policyService.updateInsurancePolicy(policy);

		if (SystemUtils.IS_OS_LINUX) {
			// 发短信通知
			smsService
					.send(SmsTemplateId.UNDERWRITE_OK_NOTIFY_CHANNEL,
							String.valueOf(policy.getPhone()),
							new String[] { policy.getCarNumber(), policy.getCompanyName(), premiumDetail.toString(), accountInfo.getPhone(),
									accountInfo.getUserName() });

			// 检索BD，给BD发送短信
			String salesId = policy.getSalesId();
			if (StringUtils.isNotEmpty(salesId)) {
				OmsUser account = accountService.findByName(salesId);
				if (account != null) {
					String customerserviceName = "周勇";
					OmsUser omsUser = accountService.findByEmployeeID(policy.getCustomerservice() + "");
					if (omsUser != null) {
						customerserviceName = omsUser.getName();
					}
					smsService.send(SmsTemplateId.UNDERWRITE_OK_NOTIFY_BD, account.getPhone(),
							new String[] { account.getName(), policy.getCarNumber(), policy.getCompanyName(), String.valueOf(inquiryResult.getTotalPremium()),
									policy.getChannelName(), policy.getChannelContact(), String.valueOf(policy.getPhone()), customerserviceName });
				}
			}
		} else {
			logger.info("{}核保通过，保费明细：{}", policy.getCarNumber(), premiumDetail.toString());
		}
		mm.put("errcode", 0);
		return mm;
	}

	/**
	 * 修改保单信息
	 */
	@RequestMapping(value = "/{policyId}", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap editPolicy(@PathVariable String policyId, InsurancePolicy request, HttpSession session) throws Exception {
		InsurancePolicy policy = policyService.getInsurancePolicyById(policyId);
		ModelMap mm = new ModelMap();
		if (policy == null) {
			mm.put("errcode", ErrorCode.POLICY_NOT_FOUND);
			mm.put("errmsg", "投保单不存在，可能已经被删除，请刷新页面后重试！");
			return mm;
		}
		if (request.getWashCarCoupons() < 0 || request.getWashCarCoupons() > 52) {
			mm.put("errcode", ErrorCode.POLICY_NOT_FOUND);
			mm.put("errmsg", "洗车券数量只能介于0到52之间！");
			return mm;
		}
		policy.setPhone(request.getPhone());
		policy.setOwner(request.getOwner());
		policy.setOwnerId(request.getOwnerId());
		policy.setApplicant(request.getApplicant());
		policy.setApplicantId(request.getApplicantId());
		policy.setInsurant(request.getInsurant());
		policy.setInsurantId(request.getInsurantId());

		policyService.updateInsurancePolicy(policy);

		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		logger.info("{}修改了{}的投保单，ID = {}", userName, policy.getCarNumber(), policy.getOrderId());

		mm.put("errcode", 0);
		return mm;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}

	@RequestMapping(value = "/policyfee/{policyId}", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap editPolicyFee(@PathVariable String policyId, PolicyFee request, HttpSession session) throws Exception {
		ModelMap mm = new ModelMap();
		// 计算每单的佣金比例
		InsurancePolicy policy = policyService.getInsurancePolicyById(policyId);
		if(policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "保单不存在，可能已被删除，请刷新页面重试！");
		}
		PolicyFee policyFee = policyFeeService.findByPolicyId(policyId);
		if(request.getCommissionRate() < 0) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "数据格式错误，佣金点数不能是负数！");
		}
		if(request.getCommissionRate() >= 1) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "数据格式错误，佣金点数不能大于1");
		}
		policyFee.setCommissionRate(request.getCommissionRate());
		BigDecimal returndCash = new BigDecimal(policy.getSumPremium() * policyFee.getCommissionRate());
		policyFee.setReturnedCash(returndCash.setScale(2, RoundingMode.HALF_UP).floatValue());// 返现金额
		
		policyFee.setExpressCompany(request.getExpressCompany());
		policyFee.setExpressSerialNo(request.getExpressSerialNo());
		policyFee.setExpressCost(request.getExpressCost());
		policyFee.setPosRate(request.getPosRate());//刷卡费率
		
		policyFee.setExpressAddress(request.getExpressAddress());
		policyFee.setRecipients(request.getRecipients());
		policyFee.setRecipientsPhone(request.getRecipientsPhone());
		
		policyFee.setPayMode(request.getPayMode());
		if(policyFee.getPayMode() == 10) {//苏州易高刷卡
			BigDecimal posFee = new BigDecimal(policy.getTotalPremium() * policyFee.getPosRate());
			policyFee.setPosFee(posFee.setScale(2, RoundingMode.HALF_UP).floatValue());
		} else if(policyFee.getPayMode() == 3) {//现金支付
			policyFee.setPosFee(0);
		}
		policyFeeService.savePolicyFee(policyFee);
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		logger.info("{}修改了{}的投保单的配送信息，保单ID：{}", userName, policy.getCarNumber(), policy.getOrderId());
		mm.put("errcode", 0);
		return mm;
	}

	@RequestMapping(value = "{policyId}/delete", method = RequestMethod.GET)
	@ResponseBody
	@OMSPermission(permission = Permission.INSURANCE_POLICY_DELETE_MGMT)
	public ModelMap deletePolicy(@PathVariable String policyId, HttpSession session) {
		ModelMap mm = new ModelMap();
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		policyService.deletePolicy(policyId, userName);
		mm.put("result", "success");
		return mm;
	}

	@RequestMapping(value = "{orderId}/update/settlestatus", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap updateSettleStatus(@PathVariable String orderId) {
		ModelMap mm = new ModelMap();
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy != null) {
			if (policy.getChannelCode() > 0) {
				policy.setSettleStatus(1);
				policy.setUpdateTime(new Date());
				policyService.updateInsurancePolicy(policy);
			}
			mm.put("result", "success");
		} else {
			mm.put("result", "failed");
		}
		return mm;
	}

	@RequestMapping(value = "commission", method = RequestMethod.GET)
	public ModelAndView commissionSettle() {
		ModelMap mm = new ModelMap();
		mm.put("currDate", DateUtils.convertDateToStr(new Date(), "yyyy-MM-dd"));
		return new ModelAndView("insurance/commission", mm);
	}

	/**
	 * 生成和渠道商结算佣金的excel
	 */
	@RequestMapping(value = "commission/task", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap doCommissionSettleTask(String createDate) {
		ModelMap mm = new ModelMap("errcode", 0);
		createDate += " 23:59:59";
		Date end = DateUtils.convertStrToDate(createDate, "yyyy-MM-dd HH:mm:ss");
		policyService.executeCommissionSettlementTask(end);
		return mm;
	}
	
	@RequestMapping(value = "settletask/start", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap startSettleTask(@RequestParam String startDate, @RequestParam String endDate) {
		ModelMap mm = new ModelMap("errcode", 0);
		startDate += " 00:00:00";
		endDate += " 23:59:59";
		policyService.startSettleTask(startDate, endDate);
		return mm;
	}

	@RequestMapping(value = "{orderId}/assign", method = RequestMethod.GET)
	@OMSPermission(permission = Permission.INSURANCE_POLICY_ASSIGN)
	public ModelAndView preAssign(@PathVariable String orderId) {
		ModelMap mm = new ModelMap();
		mm.put("orderId", orderId);
		List<OmsUser> hebaoUsers = accountService.findByRole(Role.OMS_HEBAO_USER);
		mm.put("hebaoUsers", hebaoUsers);
		return new ModelAndView("insurance/assign", mm);
	}

	@RequestMapping(value = "{orderId}/assign", method = RequestMethod.POST)
	@ResponseBody
	@OMSPermission(permission = Permission.INSURANCE_POLICY_ASSIGN)
	public ModelMap assign(@PathVariable String orderId, int customerservice, @RequestParam("comments") String comment, HttpSession session) {
		ModelMap mm = new ModelMap();
		InsurancePolicy insurancePolicy = policyService.getInsurancePolicyById(orderId);
		if (StringUtils.isNotEmpty(comment)) {
			insurancePolicy.setReadFlag(0);
		}
		if (insurancePolicy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单不存在，可能已被删除，请刷新页面.");
			return mm;
		}
		insurancePolicy.setCustomerservice(customerservice);
		policyService.updateInsurancePolicy(insurancePolicy);

		OmsUser omsUser = accountService.findByEmployeeID(String.valueOf(customerservice));
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		String userName = accountInfo.getUserName();
		policyService.addComments(orderId, "分配保单给" + omsUser.getName(), userName);
		if (StringUtils.isNotEmpty(comment)) {
			policyService.addComments(orderId, comment, userName);
		}
		mm.put("errcode", 0);
		return mm;
	}

	@RequestMapping(value = "/update/readflag/{orderId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap updateReadFlag(@PathVariable String orderId) {
		ModelMap mm = new ModelMap("retcode", 0);
		policyService.updateReadFlag(orderId, 1);
		return mm;
	}

	@RequestMapping(value = "/policy/flag/search", method = RequestMethod.GET)
	public ModelAndView flagStatistics() {
		ModelAndView mv = new ModelAndView("/insurance/flagPolicy");
		Map<Integer, Integer> results = policyService.staticsFlagPolicy();
		mv.addObject("results", results);

		Map<Integer, Long> statusMap = policyService.groupByStatus();
		mv.addObject("statusMap", statusMap);
		return mv;
	}

	/**
	 * 更新紧急度
	 * 
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value = "/update/priority/{orderId}/{priority}", method = RequestMethod.GET)
	public void updatePriority(@PathVariable String orderId, @PathVariable int priority) {
		policyService.updatePriority(orderId, priority);
	}

	/**
	 * 转换为真实保单
	 * @param carId
	 * @return
	 */
	@RequestMapping(value = "{carId}/policy/transfer", method = RequestMethod.GET)
	public ModelAndView policyTransfer(@PathVariable String carId) {
		ModelAndView mv = new ModelAndView("insurance/policyTransfer");

		BaojiaRecord baojia = baojiaService.getBaojiaRecordByCardId(carId);
		Map<Integer, String> companyMap = new HashMap<>();
		if (baojia != null) {
			if (StringUtils.isNotEmpty(baojia.getPICC())) {
				companyMap.put(ICCode.PICC, ICCode.getICNameByCode(ICCode.PICC));
			}
			if (StringUtils.isNotEmpty(baojia.getCPIC())) {
				companyMap.put(ICCode.CPIC, ICCode.getICNameByCode(ICCode.CPIC));
			}
			if (StringUtils.isNotEmpty(baojia.getPAIC())) {
				companyMap.put(ICCode.PAIC, ICCode.getICNameByCode(ICCode.PAIC));
			}
			if (StringUtils.isNotEmpty(baojia.getGPIC())) {
				companyMap.put(ICCode.GPIC, ICCode.getICNameByCode(ICCode.GPIC));
			}
			if (StringUtils.isNotEmpty(baojia.getZKIC())) {
				companyMap.put(ICCode.ZKIC, ICCode.getICNameByCode(ICCode.ZKIC));
			}
			if (StringUtils.isNotEmpty(baojia.getYGBX())) {
				companyMap.put(ICCode.YGBX, ICCode.getICNameByCode(ICCode.YGBX));
			}
			if (StringUtils.isNotEmpty(baojia.getTPIC())) {
				companyMap.put(ICCode.TPIC, ICCode.getICNameByCode(ICCode.TPIC));
			}
		}
		mv.addObject("companyMap", companyMap);
		Car car = carService.findCar(carId);
		Merchant merchant = vehicleMerchantService.findMerchantById(car.getMerchantCode());
		if (merchant != null) {
			mv.addObject("merchantAddress", merchant.getAddress());
		}
		mv.addObject("car", car);
		City city = cityService.getByCityCode(car.getCityCode());
		Map<Integer, String> provinces = cityService.getAllProvince();
		mv.addObject("provinces", provinces);
		if (city != null) {
			mv.addObject("cityName", city.getName());
			mv.addObject("provinceId", city.getProvinceCode());
			mv.addObject("cityCode", city.getCityCode());
		}

		return mv;
	}

	@RequestMapping(value = "/go/add/flag/{policyId}/{flag}", method = RequestMethod.GET)
	public ModelAndView addFlag(@PathVariable String policyId, @PathVariable int flag) {
		ModelAndView mv = new ModelAndView("/insurance/addFlag", "orderId", policyId);
		mv.addObject("flag", flag);
		return mv;
	}

	/**
	 * 更新标记
	 * 
	 * @param requestId
	 * @return
	 */
	@RequestMapping(value = "/update/flag/{orderId}/{flag}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap updateFlag(@PathVariable String orderId, @PathVariable int flag) {
		ModelMap mm = new ModelMap("retcode", 0);
		policyService.updateFlag(orderId, flag);
		return mm;
	}

	@RequestMapping(value = "/prority/count", method = RequestMethod.GET)
	@ResponseBody
	public int getProrityRequestCount() {
		int count = policyService.countProrityRequestCount();
		return count;
	}

	@RequestMapping(value = "/{orderId}/recover", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap recover(@PathVariable String orderId, HttpSession session) {
		ModelMap mm = new ModelMap();
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单不存在");
		} else {
			if (policy.getStatus() != PolicyStatus.SUSPENDED.value()) {
				mm.put("errcode", 2);
				mm.put("errmsg", "不能对非挂起状态下的保单进行解挂操作！");
			} else {
				AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
				String userName = accountInfo.getUserName();
				policyService.resume(policy, userName);
				mm.put("errcode", 0);
				mm.put("errmsg", "操作成功");
			}
		}
		return mm;
	}

	/**
	 * 发送补充资料短信
	 * 
	 * @param orderId
	 * @param datas
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/{orderId}/supplement/data", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendMessage(@PathVariable String orderId, @RequestParam("datas[]") List<String> datas) throws IOException {
		ModelMap mm = new ModelMap();
		if (CollectionUtils.isEmpty(datas)) {
			mm.put("result", "请选择需要补充的资料");
			return mm;
		}
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy == null) {
			mm.put("result", "保单不存在");
			return mm;
		}

		StringBuilder data = new StringBuilder();
		StringBuilder material = new StringBuilder();
		for (String str : datas) {
			material.append(str + ",");
			if (StringUtils.equals("1", str)) {
				data.append("行驶证、");
			} else if (StringUtils.equals("2", str)) {
				data.append("行驶证副本、");
			} else if (StringUtils.equals("3", str)) {
				data.append("行驶证副页反面、");
			} else if (StringUtils.equals("4", str)) {
				data.append("车主身份证、");
			} else if (StringUtils.equals("5", str)) {
				data.append("投保人身份证、");
			} else if (StringUtils.equals("6", str)) {
				data.append("被保险人身份证、");
			} else if (StringUtils.equals("7", str)) {
				data.append("全车前后左右四个角45°照车的全貌共4张以及有当天日期的报纸或者超市小票与车架号的合影、");
			} else if (StringUtils.equals("8", str)) {
				data.append("补充驾照、");
			} else if (StringUtils.equals("9", str)) {
				data.append("居住证、");
			} else if (StringUtils.equals("10", str)) {
				data.append("工作证明、");
			} else if (StringUtils.equals("11", str)) {
				data.append("授权书、");
			} else if (StringUtils.equals("12", str)) {
				data.append("新车合格证、");
			} else if (StringUtils.equals("13", str)) {
				data.append("新车发票、");
			} else if (StringUtils.equals("14", str)) {
				data.append("二手车交易发票、");
			} else if (StringUtils.equals("15", str)) {
				data.append("贷款合同、");
			} else if (StringUtils.equals("16", str)) {
				data.append("上年度保单、");
			} else if (StringUtils.equals("17", str)) {
				data.append("车辆登记证书、");
			}
		}
		data = data.replace(data.length() - 1, data.length(), "");

		String url = "http://api.ykcare.cn/wx/insurance/" + policy.getOrderId() + "/supplement/material?material=" + material.toString();
		String tinyUrl = DWZUtils.generate(url) + " ";

		if (policy.getCustomerservice() == 0) {
			mm.put("result", "尚未分配客服");
			return mm;
		}

		OmsUser omsUser = accountService.findByEmployeeID(policy.getCustomerservice() + "");
		if (omsUser == null) {
			mm.put("result", "客服不存在");
			return mm;
		}

		// 默认使用商家名称，如果商家名称为空，则使用车主姓名作为称谓
		String name = policy.getChannelName();
		if (StringUtils.isEmpty(name)) {
			name = policy.getOwner();
		}
		smsService.send(SmsTemplateId.SUPPLEMENT_MATERIAL, policy.getPhone() + "", new String[] { name, policy.getCarNumber(), data.toString(), tinyUrl,
				omsUser.getPhone(), omsUser.getName() });

		mm.put("result", "success");
		return mm;
	}

	/**
	 * 手动录单，支持2种方式： 1、基于现有保单，重新修改保单的保险公司、险种、保额、保费等信息，会覆盖原有保单
	 * 2、原来没有保单，基于现有车辆，为这辆车录入一张全新的保单
	 * 
	 * @param orderId
	 *            现有保单ID
	 * @param carId
	 *            车辆ID
	 */
	@RequestMapping(value = "manual/recording", method = RequestMethod.GET)
	public ModelAndView manualRecording(@RequestParam(required = false, defaultValue = "") String orderId,
			@RequestParam(required = false, defaultValue = "") String carId) {
		ModelMap mm = new ModelMap();
		if (StringUtils.isNotEmpty(orderId)) {
			InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
			if (policy == null) {
				throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "投保单不存在");
			}
			if (StringUtils.isEmpty(policy.getCarId())) {
				throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "尚未录入车辆信息");
			}
			carId = policy.getCarId();
			mm.put("policy", policy);
		}
		Car car = carService.findCar(carId);
		if (car == null) {
			throw new CommonLogicException(ErrorCode.CAR_NOT_FOUND, "车辆不存在，车辆ID错误，请刷新页面重试！");
		}
		City city = cityService.getByCityCode(car.getCityCode());
		Map<Integer, String> provinces = cityService.getAllProvince();
		mm.put("provinces", provinces);
		if (city != null) {
			mm.put("provinceId", city.getProvinceCode());
		}
		mm.put("car", car);
		return new ModelAndView("insurance/manualRecording", mm);
	}

	/**
	 * 手动录单
	 */
	@RequestMapping(value = "manual/recording", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap manualRecording(@RequestParam(required = false, defaultValue = "") String orderId, SubmitPolicyFormBean bean, HttpServletRequest request)
			throws Exception {
		ModelMap mm = new ModelMap();
		int companyCode = Integer.parseInt(request.getParameter("company"));
		boolean bInsuranceFlag = StringUtils.equals("on", request.getParameter("bInsurance"));
		boolean cInsuranceFlag = StringUtils.equals("on", request.getParameter("cInsurance"));
		ObjectMapper mapper = new ObjectMapper();
		InsurancePolicy policy = null;
		InquiryResult inquiryResult = null;
		// 保单重新手工录单
		if (StringUtils.isNotEmpty(orderId)) {
			policy = policyService.getInsurancePolicyById(orderId);
			if (policy == null) {
				mm.put("errcode", 1);
				mm.put("errmsg", "保单不存在");
				return mm;
			}
			String content = policy.getContent();
			if (StringUtils.isEmpty(content)) {
				inquiryResult = new InquiryResult();
				inquiryResult.setCarNumber(policy.getCarNumber());
			} else {
				inquiryResult = mapper.readValue(content, InquiryResult.class);
			}
		} else {
			InsurancePolicy duplicatePolicy = policyService.isDuplicatePolicy(bean.getCarId());
			if (duplicatePolicy != null) {
				mm.put("errcode", 1);
				mm.put("errmsg", "重复提交保单");
				return mm;
			}
			// 直接手工录单
			policy = new InsurancePolicy();
			policy.setOrderId(InsuranceOrderNoUtils.getOrderNo());
			inquiryResult = new InquiryResult();

			Car car = carService.findCar(bean.getCarId());

			Merchant merchant = vehicleMerchantService.getByMerchatId(Long.parseLong(car.getMerchantCode()));
			if (merchant != null) {
				policy.setChannelCode(merchant.getId());
				policy.setChannelName(merchant.getName());
				policy.setChannelContact(merchant.getManager());
				policy.setSalesId(merchant.getSalesId());
				policy.setSalesMan(merchant.getSalesman());
			}

			if (bInsuranceFlag) {
				policy.setProposalNo(RandomStringUtils.randomNumeric(20)); // 商业险投保单号
				policy.setPolicyNo(RandomStringUtils.randomNumeric(20));// 商业险保单号
			}
			if (cInsuranceFlag) {
				policy.setProposalNoCI(RandomStringUtils.randomNumeric(20)); // 交强险投保单号
				policy.setPolicyNoCI(RandomStringUtils.randomNumeric(20)); // 交强险保单号
			}
			policy.setOwner(bean.getOwner());
			policy.setUserId(car.getUserId());
			policy.setCarId(car.getId());
			policy.setCarNumber(car.getNumber());
			policy.setModelDescr(car.getModelDescr());

			policy.setCustomerservice(0);
			policy.setCreateDate(new Date());
			policy.setStatus(PolicyStatus.NEED_UNDERWRITE.value());

			inquiryResult.setCarNumber(car.getNumber());
		}
		policy.setCompanyCode(companyCode);

		// 商业险总保费
		float bInsuranceTotal = 0;
		// 不计免赔总保费
		float mInsuranceTotal = 0;
		// 司乘险不计免赔总保费
		float mdInsuranceTotal = 0;
		// 附加险不计免赔总保费
		float additionalInsuranceTotal = 0;

		inquiryResult.setCode(companyCode);
		inquiryResult.setCompany(ICCode.getICNameByCode(companyCode));

		// 保费明细
		StringBuilder premiumDetail = new StringBuilder();
		List<Insurance> insurances = new ArrayList<Insurance>();
		Insurance insurance = new Insurance();
		if (bInsuranceFlag) {
			insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.A)));
			insurance.setRiskKindCode(StandardRiskKindCode.A);
			insurance.setRiskCode("0505");
			insurance.setName("机动车损失保险");
			if (insurance.isBuy()) {
				insurance.setAmount(Float.parseFloat(request.getParameter("MAAmount")));
				insurance.setPremium(Float.parseFloat(request.getParameter("APremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("车损险保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MA);
			insurance.setRiskCode("0505");
			insurance.setName("车损不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MA)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MAPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.B)) > 0);
			insurance.setRiskKindCode(StandardRiskKindCode.B);
			insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.B)) * 10000);
			insurance.setRiskCode("0505");
			insurance.setName("第三者责任保险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("BPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("三责险保额" + insurance.getAmount() / 10000 + "万元，保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MB);
			insurance.setRiskCode("0505");
			insurance.setName("三者不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MB)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MBPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.G)));
			insurance.setRiskKindCode(StandardRiskKindCode.G);
			insurance.setRiskCode("0505");
			insurance.setName("机动车盗抢保险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("GPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("盗抢险保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MG);
			insurance.setRiskCode("0505");
			insurance.setName("全车盗抢不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MG)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MGPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.D3)) > 0);
			insurance.setRiskKindCode(StandardRiskKindCode.D3);
			insurance.setRiskCode("0505");
			insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.D3)) * 10000);
			insurance.setName("车上人员责任险司机座位");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("D3Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("车上人员责任险（司机）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MD3);
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任司机不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MD3)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MD3Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
				mdInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.D4)) > 0);
			insurance.setRiskKindCode(StandardRiskKindCode.D4);
			insurance.setRiskCode("0505");
			insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.D4)) * 10000);
			insurance.setName("车上人员责任险乘客座位");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("D4Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("车上人员责任险（乘客）保额" + insurance.getAmount() + "元/座，保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MD4);
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任乘客不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MD4)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MD4Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
				mdInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(Integer.parseInt(request.getParameter(StandardRiskKindCode.F)) > 0);
			insurance.setRiskKindCode(StandardRiskKindCode.F);
			insurance.setRiskCode("0505");
			insurance.setGlassType(Integer.parseInt(request.getParameter(StandardRiskKindCode.F)));
			insurance.setName("玻璃单独破碎险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("FPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				String glassType = "国产玻璃";
				if (insurance.getGlassType() == 2) {
					glassType = "进口玻璃";
				}
				premiumDetail.append("玻璃单独破碎险【" + glassType + "】，保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.L)) > 0);
			insurance.setRiskKindCode(StandardRiskKindCode.L);
			insurance.setRiskCode("0505");
			insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.L)));
			insurance.setName("车身划痕损失险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("LPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("车身划痕险保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.ML);
			insurance.setRiskCode("0505");
			insurance.setName("车身划痕不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.ML)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MLPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
				additionalInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.Z)));
			insurance.setRiskKindCode(StandardRiskKindCode.Z);
			insurance.setRiskCode("0505");
			insurance.setName("自燃损失险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("ZPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("自燃损失险保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MZ);
			insurance.setRiskCode("0505");
			insurance.setName("自燃损失不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MZ)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MZPremium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
				additionalInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.X1)));
			insurance.setRiskKindCode(StandardRiskKindCode.X1);
			insurance.setRiskCode("0505");
			insurance.setName("发动机特别损失险");
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("X1Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				premiumDetail.append("涉水险保费" + insurance.getPremium() + "元，");
			}
			insurances.add(insurance);

			insurance = new Insurance();
			insurance.setRiskKindCode(StandardRiskKindCode.MX1);
			insurance.setRiskCode("0505");
			insurance.setName("发动机特别损失不计免赔");
			insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MX1)));
			if (insurance.isBuy()) {
				insurance.setPremium(Float.parseFloat(request.getParameter("MX1Premium")));
				insurance.setUnderwritingPremium(insurance.getPremium());
				bInsuranceTotal += insurance.getPremium();
				mInsuranceTotal += insurance.getPremium();
				additionalInsuranceTotal += insurance.getPremium();
			}
			insurances.add(insurance);

			bInsuranceTotal = new BigDecimal(bInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			mInsuranceTotal = new BigDecimal(mInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			mdInsuranceTotal = new BigDecimal(mdInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			additionalInsuranceTotal = new BigDecimal(additionalInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

			premiumDetail.append("以上不计免赔合计" + mInsuranceTotal + "元，");

			// 人寿的增加M保存不计免赔合计
			if (mInsuranceTotal != 0 && companyCode == ICCode.GPIC) {
				insurance = new Insurance();
				insurance.setBuy(true);
				insurance.setPremium(mInsuranceTotal);
				insurance.setUnderwritingPremium(mInsuranceTotal);
				insurance.setRiskKindCode(StandardRiskKindCode.M);
				insurance.setRiskCode("0505");
				insurance.setName("不计免赔");

				insurances.add(insurance);
			}

			String startDate = request.getParameter("startDate");
			BInsurance bInsurance = new BInsurance();
			bInsurance.setDemandNo(RandomStringUtils.randomAlphanumeric(36).toLowerCase());
			bInsurance.setStartDate(startDate);
			bInsurance.setStartHour(0);
			bInsurance.setEndHour(24);

			// 计算商业险总保额
			BigDecimal sumAmount = new BigDecimal(0);
			for (Insurance insure : insurances) {
				if (insure.isBusinessInsurance() && insure.getAmount() > 0) {
					sumAmount = sumAmount.add(new BigDecimal(String.valueOf(insure.getAmount())));
				}
			}

			bInsurance.setSumAmount(sumAmount.floatValue());
			bInsurance.setSumPremium(bInsuranceTotal);

			inquiryResult.setbInsurance(bInsurance);
			policy.setSumAmount(bInsurance.getSumAmount());
			policy.setSumPremium(bInsuranceTotal);
			policy.setStartDate(DateUtils.convertStrToDate(startDate, "yyyy-MM-dd"));
			Date endDate = DateUtils.calculateEndDate(policy.getStartDate());
			policy.setEndDate(endDate);

			// 针对各家保险公司的险种需求做特殊处理
			Iterator<Insurance> it = insurances.iterator();

			while (it.hasNext()) {
				Insurance insure = it.next();
				String riskKindCode = insure.getRiskKindCode();
				if (companyCode == ICCode.CPIC || companyCode == ICCode.TPIC || companyCode == ICCode.PAIC) {
					// 太保、太平、平安
					if (mdInsuranceTotal != 0 && StringUtils.equals(riskKindCode, StandardRiskKindCode.MD3)) {
						insure.setBuy(true);
						insure.setPremium(mdInsuranceTotal);
						insure.setUnderwritingPremium(mdInsuranceTotal);
						insure.setName("车上人员责任险不计免赔");
					}
					// 附加险不计免赔包含了MX1 ML MZ，都存在MZ里面
					if (additionalInsuranceTotal != 0 && StringUtils.equals(riskKindCode, StandardRiskKindCode.MZ)) {
						insure.setBuy(true);
						insure.setPremium(additionalInsuranceTotal);
						insure.setUnderwritingPremium(additionalInsuranceTotal);
						insure.setName("附加险不计免赔");
					}
					// 移除掉合并的
					if (StringUtils.equals(riskKindCode, StandardRiskKindCode.MD4) || StringUtils.equals(riskKindCode, StandardRiskKindCode.ML)
							|| StringUtils.equals(riskKindCode, StandardRiskKindCode.MX1)) {
						it.remove();
					}
				} else if (companyCode == ICCode.YGBX) {
					// 阳光所有的不计免赔合计都存在MA里面，其余全部移除
					if (StringUtils.equals(riskKindCode, StandardRiskKindCode.MA)) {
						insure.setBuy(true);
						insure.setPremium(mInsuranceTotal);
						insure.setUnderwritingPremium(mInsuranceTotal);
						insure.setName("不计免赔");
					}

					// 移除掉合并的
					if (StringUtils.equals("MB", riskKindCode) || StringUtils.equals("MD3", riskKindCode) || StringUtils.equals("MD4", riskKindCode)
							|| StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode) || StringUtils.equals("MX1", riskKindCode)
							|| StringUtils.equals("MG", riskKindCode)) {
						it.remove();
					}
				} else if (companyCode == ICCode.GPIC) {
					// 人寿所有的不计免赔合计都存在M里面，原有的不计免赔全部移除掉
					if (StringUtils.equals("MA", riskKindCode) || StringUtils.equals("MB", riskKindCode) || StringUtils.equals("MD3", riskKindCode)
							|| StringUtils.equals("MD4", riskKindCode) || StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode)
							|| StringUtils.equals("MX1", riskKindCode) || StringUtils.equals("MG", riskKindCode)) {
						it.remove();
					}
				}

			}
		} else {
			inquiryResult.setbInsurance(new BInsurance());
			policy.setStartDate(null);
			policy.setEndDate(null);
			policy.setSumAmount(0);
			policy.setSumPremium(0);
		}

		if (cInsuranceFlag) {
			insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setRiskKindCode("BZ");
			insurance.setRiskCode("0594");
			insurance.setName("机动车交通事故责任强制保险");
			insurance.setPremium(Float.parseFloat(request.getParameter("BZPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			insurances.add(insurance);

			premiumDetail.append("交强险" + insurance.getPremium() + "元，");

			String startDateCI = request.getParameter("startDateCI");
			CarShipTaxInfo carShipTaxInfo = new CarShipTaxInfo();
			carShipTaxInfo.setCarShipTax(Float.parseFloat(request.getParameter("CSTPremium")));
			carShipTaxInfo.setUnderwritingPremium(carShipTaxInfo.getCarShipTax());
			inquiryResult.setCarShipTaxInfo(carShipTaxInfo);
			premiumDetail.append("车船税" + carShipTaxInfo.getCarShipTax() + "元，");
			CInsurance cInsurance = new CInsurance();
			cInsurance.setSumPremium(insurance.getPremium());
			cInsurance.setStartDate(startDateCI);
			cInsurance.setDemandNoCI(RandomStringUtils.randomAlphanumeric(36).toLowerCase());
			cInsurance.setUnderwritingPremium(cInsurance.getSumPremium());
			inquiryResult.setcInsurance(cInsurance);

			policy.setSumPremiumCI(insurance.getPremium());
			policy.setCarShipTax(carShipTaxInfo.getCarShipTax());
			policy.setStartDateCI(DateUtils.convertStrToDate(startDateCI, "yyyy-MM-dd"));
			Date endDateCI = DateUtils.calculateEndDate(policy.getStartDateCI());
			policy.setEndDateCI(endDateCI);
		} else {
			inquiryResult.setcInsurance(new CInsurance());
			inquiryResult.setCarShipTaxInfo(new CarShipTaxInfo());
			policy.setStartDateCI(null);
			policy.setEndDateCI(null);
			policy.setSumPremiumCI(0);
			policy.setCarShipTax(0);
		}

		inquiryResult.setInsuranceList(insurances);

		// 计算总保费
		BigDecimal totalPremium = new BigDecimal(String.valueOf(inquiryResult.getbInsurance().getSumPremium()));
		totalPremium = totalPremium.add(new BigDecimal(String.valueOf(inquiryResult.getcInsurance().getSumPremium())));
		totalPremium = totalPremium.add(new BigDecimal(String.valueOf(inquiryResult.getCarShipTaxInfo().getCarShipTax())));
		inquiryResult.setTotalPremium(totalPremium.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());

		premiumDetail.append("保费总计" + inquiryResult.getTotalPremium() + "元");

		policy.setContent(mapper.writeValueAsString(inquiryResult));
		policy.setCityCode(bean.getCityCode());
		City city = cityService.getByCityCode(bean.getCityCode());
		policy.setCityName(city.getName());
		policy.setRegion(bean.getRegion());
		policy.setPhone(Long.parseLong(bean.getPhone()));
		policy.setOwnerId(bean.getOwnerId());
		policy.setInsurant(bean.getInsurant());
		policy.setInsurantId(bean.getInsurantId());
		policy.setApplicant(bean.getApplicant());
		policy.setApplicantId(bean.getApplicantId());
		policy.setUpdateTime(new Date());
		policy.setVirtual(false);

		AccountInfo accountInfo = (AccountInfo) request.getSession().getAttribute("user");
		// 已反馈报价的和待核保的，手动录单后，直接修改为核保通过的
		if (policy.getStatus() == PolicyStatus.NO_QUERY_BAOJIA.value() || policy.getStatus() == PolicyStatus.NEED_UNDERWRITE.value()
				|| policy.getStatus() == PolicyStatus.RESPONSE_BAOJIA.value()) {
			// 核保通过的单子默认设置成商户需要和车主沟通、确认
			policy.setFlag(PolicyFlag.NEED_CHANNEL_COMMUNICATE_WITH_VEHICLE_OWNER.value());
			policy.setStatus(PolicyStatus.UNDERWRITE_OK.value());
		}
		policyService.manualRecording(policy, bean.getExpressAddress(), accountInfo.getUserName());

		mm.put("errcode", 0);
		return mm;
	}

	/**
	 * 查看苏州易高物流的物流信息
	 */
	@RequestMapping(value = "/{policyId}/wl")
	public ModelAndView showWL(@PathVariable String policyId) {
		InsurancePolicy policy = this.policyService.getInsurancePolicyById(policyId);
		if(policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "保单不存在，可能已经被删除，请刷新页面重试！");
		}
		String serialNo = policyService.getExpressSerialNo(policyId);
		ModelAndView mv = new ModelAndView("insurance/viewYiGaoWuliu", "serialNo", serialNo);
		mv.addObject("policy", policy);
		int days = DateUtils.difference(policy.getCreateDate(), new Date());
		if(days <=2) {
			mv.addObject("risk", "2天，正常范围内");
		} else if(days == 3) {
			mv.addObject("risk", "3天，异常未回款");
		} else {
			mv.addObject("risk", days + "天，高危订单！");
		}
		return mv;
	}

	@RequestMapping(value = "{orderId}/qrcode", method = RequestMethod.GET)
	public void uploadAttachment(@PathVariable String orderId, HttpServletResponse response) throws IOException, WriterException {
		String url = "http://api.ykcare.cn/wx/insurance/" + orderId + "/supplement/material?material=18";
		//String url = "http://172.16.1.65:6060/wx/insurance/" + orderId + "/supplement/material?material=18";
		String tinyUrl = DWZUtils.generate(url);

		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");
		int width = 400; // 图像宽度
		int height = 400; // 图像高度
		String format = "jpeg";// 图像类型
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix bitMatrix = new MultiFormatWriter().encode(tinyUrl, BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
		bitMatrix = deleteWhite(bitMatrix);
		MatrixToImageWriter.writeToStream(bitMatrix, format, response.getOutputStream());
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	private BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1]))
					resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	@RequestMapping(value = "{orderId}/endorsement", method = RequestMethod.GET)
	public ModelAndView endorsement(@PathVariable String orderId) {
		ModelMap mm = new ModelMap();
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "投保单不存在");
		}
		if (StringUtils.isEmpty(policy.getCarId())) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "尚未录入车辆信息");
		}
		mm.put("policy", policy);
		Car car = carService.findCar(policy.getCarId());
		mm.put("car", car);
		return new ModelAndView("insurance/endorsement", mm);
	}

	/**
	 * 手动录入一张批单
	 * @param orderId		原保单ID
	 * @param request
	 */
	@RequestMapping(value = "{orderId}/endorsement", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap endorsement(@PathVariable String orderId, HttpServletRequest request) throws JsonProcessingException {
		ModelMap mm = new ModelMap();
		InsurancePolicy policy = policyService.getInsurancePolicyById(orderId);
		if (policy == null) {
			mm.put("errcode", 1);
			mm.put("errmsg", "保单不存在");
			return mm;
		}
		int companyCode = policy.getCompanyCode();

		InquiryResult inquiryResult = new InquiryResult();
		inquiryResult.setCarNumber(policy.getCarNumber());
		inquiryResult.setCode(companyCode);
		inquiryResult.setCompany(ICCode.getICNameByCode(companyCode));

		// 商业险总保费
		float bInsuranceTotal = 0;
		// 不计免赔总保费
		float mInsuranceTotal = 0;
		// 司乘险不计免赔总保费
		float mdInsuranceTotal = 0;
		// 附加险不计免赔总保费
		float additionalInsuranceTotal = 0;

		List<Insurance> insurances = new ArrayList<Insurance>();
		Insurance insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.A)));
		insurance.setRiskKindCode(StandardRiskKindCode.A);
		insurance.setRiskCode("0505");
		insurance.setName("机动车损失保险");
		if (insurance.isBuy()) {
			insurance.setAmount(Float.parseFloat(request.getParameter("MAAmount")));
			insurance.setPremium(Float.parseFloat(request.getParameter("APremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MA);
		insurance.setRiskCode("0505");
		insurance.setName("车损不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MA)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MAPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.B)) > 0);
		insurance.setRiskKindCode(StandardRiskKindCode.B);
		insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.B)) * 10000);
		insurance.setRiskCode("0505");
		insurance.setName("第三者责任保险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("BPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MB);
		insurance.setRiskCode("0505");
		insurance.setName("三者不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MB)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MBPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.G)));
		insurance.setRiskKindCode(StandardRiskKindCode.G);
		insurance.setRiskCode("0505");
		insurance.setName("机动车盗抢保险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("GPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MG);
		insurance.setRiskCode("0505");
		insurance.setName("全车盗抢不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MG)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MGPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.D3)) > 0);
		insurance.setRiskKindCode(StandardRiskKindCode.D3);
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.D3)) * 10000);
		insurance.setName("车上人员责任险司机座位");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("D3Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MD3);
		insurance.setRiskCode("0505");
		insurance.setName("车上人员责任司机不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MD3)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MD3Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
			mdInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.D4)) > 0);
		insurance.setRiskKindCode(StandardRiskKindCode.D4);
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.D4)) * 10000);
		insurance.setName("车上人员责任险乘客座位");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("D4Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MD4);
		insurance.setRiskCode("0505");
		insurance.setName("车上人员责任乘客不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MD4)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MD4Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
			mdInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.F)));
		insurance.setRiskKindCode(StandardRiskKindCode.F);
		insurance.setRiskCode("0505");
		insurance.setGlassType(Integer.parseInt(request.getParameter(StandardRiskKindCode.F)));
		insurance.setName("玻璃单独破碎险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("FPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(Float.parseFloat(request.getParameter(StandardRiskKindCode.L)) > 0);
		insurance.setRiskKindCode(StandardRiskKindCode.L);
		insurance.setRiskCode("0505");
		insurance.setAmount(Float.parseFloat(request.getParameter(StandardRiskKindCode.L)));
		insurance.setName("车身划痕损失险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("LPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.ML);
		insurance.setRiskCode("0505");
		insurance.setName("车身划痕不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.ML)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MLPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
			additionalInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.Z)));
		insurance.setRiskKindCode(StandardRiskKindCode.Z);
		insurance.setRiskCode("0505");
		insurance.setName("自燃损失险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("ZPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MZ);
		insurance.setRiskCode("0505");
		insurance.setName("自燃损失不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MZ)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MZPremium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
			additionalInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setBuy(StringUtils.equals("1", request.getParameter(StandardRiskKindCode.X1)));
		insurance.setRiskKindCode(StandardRiskKindCode.X1);
		insurance.setRiskCode("0505");
		insurance.setName("发动机特别损失险");
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("X1Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		insurance = new Insurance();
		insurance.setRiskKindCode(StandardRiskKindCode.MX1);
		insurance.setRiskCode("0505");
		insurance.setName("发动机特别损失不计免赔");
		insurance.setBuy(StringUtils.equals("on", request.getParameter(StandardRiskKindCode.MX1)));
		if (insurance.isBuy()) {
			insurance.setPremium(Float.parseFloat(request.getParameter("MX1Premium")));
			insurance.setUnderwritingPremium(insurance.getPremium());
			bInsuranceTotal += insurance.getPremium();
			mInsuranceTotal += insurance.getPremium();
			additionalInsuranceTotal += insurance.getPremium();
		}
		insurances.add(insurance);

		bInsuranceTotal = new BigDecimal(bInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		mInsuranceTotal = new BigDecimal(mInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		mdInsuranceTotal = new BigDecimal(mdInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		additionalInsuranceTotal = new BigDecimal(additionalInsuranceTotal).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

		// 人寿的增加M保存不计免赔合计
		if (mInsuranceTotal != 0 && companyCode == ICCode.GPIC) {
			insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setPremium(mInsuranceTotal);
			insurance.setUnderwritingPremium(mInsuranceTotal);
			insurance.setRiskKindCode(StandardRiskKindCode.M);
			insurance.setRiskCode("0505");
			insurance.setName("不计免赔");

			insurances.add(insurance);
		}

		Date startDate = policy.getStartDate();
		BInsurance bInsurance = new BInsurance();
		bInsurance.setDemandNo(RandomStringUtils.randomAlphanumeric(36).toLowerCase());
		bInsurance.setStartDate(DateUtils.convertDateToStr(startDate, "yyyy-MM-dd"));
		bInsurance.setStartHour(0);
		bInsurance.setEndHour(24);

		// 计算商业险总保额
		BigDecimal sumAmount = new BigDecimal(0);
		for (Insurance insure : insurances) {
			if (insure.isBusinessInsurance() && insure.getAmount() > 0) {
				sumAmount = sumAmount.add(new BigDecimal(String.valueOf(insure.getAmount())));
			}
		}

		bInsurance.setSumAmount(sumAmount.floatValue());
		bInsurance.setSumPremium(bInsuranceTotal);
		inquiryResult.setbInsurance(bInsurance);

		// 针对各家保险公司的险种需求做特殊处理
		Iterator<Insurance> it = insurances.iterator();
		while (it.hasNext()) {
			Insurance insure = it.next();
			String riskKindCode = insure.getRiskKindCode();
			if (companyCode == ICCode.CPIC || companyCode == ICCode.TPIC || companyCode == ICCode.PAIC) {
				// 太保、太平、平安
				if (mdInsuranceTotal != 0 && StringUtils.equals(riskKindCode, StandardRiskKindCode.MD3)) {
					insure.setBuy(true);
					insure.setPremium(mdInsuranceTotal);
					insure.setUnderwritingPremium(mdInsuranceTotal);
					insure.setName("车上人员责任险不计免赔");
				}
				// 附加险不计免赔包含了MX1 ML MZ，都存在MZ里面
				if (additionalInsuranceTotal != 0 && StringUtils.equals(riskKindCode, StandardRiskKindCode.MZ)) {
					insure.setBuy(true);
					insure.setPremium(additionalInsuranceTotal);
					insure.setUnderwritingPremium(additionalInsuranceTotal);
					insure.setName("附加险不计免赔");
				}
				// 移除掉合并的
				if (StringUtils.equals(riskKindCode, StandardRiskKindCode.MD4) || StringUtils.equals(riskKindCode, StandardRiskKindCode.ML)
						|| StringUtils.equals(riskKindCode, StandardRiskKindCode.MX1)) {
					it.remove();
				}
			} else if (companyCode == ICCode.YGBX) {
				// 阳光所有的不计免赔合计都存在MA里面，其余全部移除
				if (StringUtils.equals(riskKindCode, StandardRiskKindCode.MA)) {
					insure.setBuy(true);
					insure.setPremium(mInsuranceTotal);
					insure.setUnderwritingPremium(mInsuranceTotal);
					insure.setName("不计免赔");
				}

				// 移除掉合并的
				if (StringUtils.equals("MB", riskKindCode) || StringUtils.equals("MD3", riskKindCode) || StringUtils.equals("MD4", riskKindCode)
						|| StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode) || StringUtils.equals("MX1", riskKindCode)
						|| StringUtils.equals("MG", riskKindCode)) {
					it.remove();
				}
			} else if (companyCode == ICCode.GPIC) {
				// 人寿所有的不计免赔合计都存在M里面，原有的不计免赔全部移除掉
				if (StringUtils.equals("MA", riskKindCode) || StringUtils.equals("MB", riskKindCode) || StringUtils.equals("MD3", riskKindCode)
						|| StringUtils.equals("MD4", riskKindCode) || StringUtils.equals("MZ", riskKindCode) || StringUtils.equals("ML", riskKindCode)
						|| StringUtils.equals("MX1", riskKindCode) || StringUtils.equals("MG", riskKindCode)) {
					it.remove();
				}
			}

		}

		inquiryResult.setInsuranceList(insurances);

		// 计算总保费
		BigDecimal totalPremium = new BigDecimal(String.valueOf(inquiryResult.getbInsurance().getSumPremium()));
		inquiryResult.setTotalPremium(totalPremium.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());

		// 创建批单
		InsurancePolicy endorsment = new InsurancePolicy();
		endorsment.setOrderId(InsuranceOrderNoUtils.getOrderNo());
		// 设置为批单
		endorsment.setPolicyType(2);
		endorsment.setAssociatedPolicyId(policy.getOrderId());
		endorsment.setChannelCode(policy.getChannelCode());
		endorsment.setChannelName(policy.getChannelName());
		endorsment.setChannelContact(policy.getChannelContact());
		endorsment.setSalesId(policy.getSalesId());
		endorsment.setSalesMan(policy.getSalesMan());

		endorsment.setProposalNo(RandomStringUtils.randomNumeric(20)); // 商业险投保单号
		endorsment.setPolicyNo(RandomStringUtils.randomNumeric(20));// 商业险保单号
		endorsment.setOwner(policy.getOwner());
		endorsment.setUserId(policy.getUserId());
		endorsment.setCarId(policy.getCarId());
		endorsment.setCarNumber(policy.getCarNumber());
		endorsment.setModelDescr(policy.getModelDescr());
		endorsment.setCompanyCode(companyCode);

		ObjectMapper mapper = new ObjectMapper();
		endorsment.setContent(mapper.writeValueAsString(inquiryResult));

		endorsment.setCityCode(policy.getCityCode());
		endorsment.setCityName(policy.getCityName());
		endorsment.setRegion(policy.getRegion());
		endorsment.setPhone(policy.getPhone());
		endorsment.setOwnerId(policy.getOwnerId());
		endorsment.setInsurant(policy.getInsurant());
		endorsment.setInsurantId(policy.getInsurantId());
		endorsment.setApplicant(policy.getApplicant());
		endorsment.setApplicantId(policy.getApplicantId());

		endorsment.setCreateDate(new Date());
		endorsment.setUpdateTime(new Date());
		endorsment.setCustomerservice(policy.getCustomerservice());
		endorsment.setStatus(PolicyStatus.CHUDAN_FINISHED.value());//批单状态应该设置为已出单

		endorsment.setSumAmount(bInsurance.getSumAmount());
		endorsment.setSumPremium(bInsuranceTotal);
		endorsment.setStartDate(startDate);
		endorsment.setEndDate(policy.getEndDate());
		endorsment.setVirtual(false);

		AccountInfo accountInfo = (AccountInfo) request.getSession().getAttribute("user");
		policyService.manualRecording(endorsment, "", accountInfo.getUserName());
		mm.put("errcode", 0);
		return mm;
	}
	/**
	 * 删除一个附件
	 */
	@RequestMapping(value = "/supplement/delete", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap deleteAttachment(@RequestParam String url) {
		String attachmentId = url.replace(
				"http://auditimg.oss-cn-hangzhou.aliyuncs.com/", "");
		// http://auditimg.oss-cn-hangzhou.aliyuncs.com/JTK4f5074131c8947f6998c4d1d620c15ff/51f1699bf45146e7bfa0511080a7f84a.jpg
		policyService.deleteAttachment(attachmentId);
		ModelMap mm = new ModelMap();
		logger.info("删除附件");
		mm.put("errcode", 0);
		return mm;
	}
	/**
	 * 导入佣金结算的excel
	 * @return
	 */
	 @RequestMapping(value = "/fee/excel/upload", method = RequestMethod.POST)
	 @ResponseBody
	public ModelMap  importExcelforFee(@RequestParam(required=false)double totleMoney,@RequestParam("file") MultipartFile fileUpload,HttpSession session){
		ModelMap mm = new ModelMap();
		mm.addAttribute("errcode", 1);
		mm.put("errmsg", "请选择正确的文件类型");
		if(fileUpload !=null){
			String filename = fileUpload.getOriginalFilename();
			Pattern pattern=Pattern.compile("^.*\\.xlsx?$");
			boolean matches = pattern.matcher(filename).matches();
			if(matches){
				AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
				int re=policyService.updatePolicyStatusFromBankTransactionExcel(fileUpload,accountInfo);
				mm.put("errcode", 0);
				mm.put("body", re);
			}
		}
	
		return mm;
	}
}
package com.jt.ycl.oms.insurance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.ErrorCode;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.CommentsDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.InsurancePolicyDaoImpl;
import com.jt.core.dao.MerchantCommissionRateDao;
import com.jt.core.dao.MerchantConfigDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.PolicyFeeDao;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.Comments;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantCommissionRate;
import com.jt.core.model.MerchantConfig;
import com.jt.core.model.PolicyFee;
import com.jt.core.model.PolicyFlag;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.account.UserService;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.coupon.OMSCouponService;
import com.jt.ycl.oms.insurance.vo.BrokerageBill;
import com.jt.ycl.oms.mail.EmailService;
import com.jt.ycl.oms.report.util.ExcelHanderUtil;
import com.jt.ycl.oms.task.PaymentRecord;
import com.jt.ycl.oms.wxmgt.WXMessageSendService;

@Service
@Transactional
public class PolicyService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private InsurancePolicyDao policyDao;

	@Autowired
	private PolicyFeeDao policyFeeDao;

	@Autowired
	private CarDao carDao;

	@Autowired
	private InsurancePolicyDaoImpl policyDaoImpl;

	@Autowired
	private OMSCouponService omsCouponService;

	@Autowired
	private WXMessageSendService wxMessageSendService;

	@Autowired
	private UserService userService;

	@Autowired
	private MerchantDao merchantDao;

	@Autowired
	private MerchantConfigDao merchantConfigDao;

	@Autowired
	private CommentsDao commentsDao;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CityService cityService;

	@Autowired
	private PolicyFeeService policyFeeService;

	@Autowired
	private MerchantCommissionRateDao merchantCommissionRateDao;

	private OSSClient client = new OSSClient("http://oss-cn-hangzhou.aliyuncs.com", "qlo4BoLGXaAXU7FA", "wHT8XCsYTnaj7utm5L0t1f1owmwSTy");

	@Autowired
	private PolicySettleTask policySettleTask;

	@Autowired
	private CMBPaymentGenerator cmbPaymentGenerator;

	@Autowired
	private JiaotongBankPaymentGenerator jiaotongBankPaymentGenerator;

	/**
	 * 保单查询
	 */
	public Map<String, Object> queryInsurancePolicy(String number, int cityCode, int companyCode, String startDate, String endDate, int status, int priority,
			int flag, int customerservice, String salesId, long channelCode, int policyType, int page) {
		int start = 0;
		int pageSize = 100;

		// 总记录数
		int recordCount = policyDaoImpl.countInsurancePolicy(number, cityCode, companyCode, channelCode, startDate, endDate, status, priority, flag,
				customerservice, salesId, policyType);
		// 总页数
		int totalPage = (recordCount + pageSize - 1) / pageSize;

		if (page >= totalPage) {
			page = totalPage;
		}

		if (page > 1) {
			start = pageSize * (page - 1);
		}

		List<InsurancePolicy> result = policyDaoImpl.queryInsurancePolicy(number, cityCode, companyCode, channelCode, startDate, endDate, status, priority,
				flag, customerservice, salesId, policyType, start, pageSize);

		/*
		 * for (InsurancePolicy insurancePolicy : result) { boolean hasAtt =
		 * hasAttchmentByPolicyId(insurancePolicy.getOrderId());
		 * insurancePolicy.setAttachmentFlag(hasAtt); }
		 */

		Map<String, Object> map = new HashMap<>();
		map.put("recordCount", recordCount);
		map.put("totalPage", totalPage);
		map.put("currentPage", page);
		map.put("result", result);
		return map;
	}

	/**
	 * 根据保单Id查询一条保单记录
	 * 
	 * @param orderId
	 * @return
	 */
	public InsurancePolicy getInsurancePolicyById(String orderId) {
		return policyDao.findOne(orderId);
	}

	/**
	 * 根据carIdId查询一条保单记录
	 * 
	 * @param carId
	 * @return
	 */
	public List<InsurancePolicy> getInsurancePolicyByCarId(String carId) {
		return policyDao.findPolicyByCarId(carId);
	}

	/**
	 * 发放洗车券
	 * 
	 * @param policy
	 * @param washCarCoupons
	 *            要发放的洗车券数量
	 */
	@Transactional
	public void grantWashCarCoupon(InsurancePolicy policy, int washCarCoupons) {
		omsCouponService.grantWashCarCoupons(policy.getUserId(), washCarCoupons);
		policy.setWashCarCoupons(washCarCoupons);
		policy.setSendDay(new Date());
		policyDao.save(policy);
	}

	/**
	 * 缴费完成，出单，如果这辆车是过户车，那么更新成非过户车，否则第二年还会按过户车报价造成报价不准确。
	 */
	@Transactional
	public InsurancePolicy chudan(InsurancePolicy policy) {
		Car car = carDao.findOne(policy.getCarId());
		if (car.isGuohu()) {
			car.setGuohu(false);
			car.setMakeDate(null);
			carDao.save(car);
		}
		return policyDao.save(policy);
	}

	/**
	 * 缴费完成，出单，如果这些辆车是过户车，那么更新成非过户车，否则第二年还会按过户车报价造成报价不准确。
	 */
	@Transactional
	public List<InsurancePolicy> chudan(List<InsurancePolicy> policyList) {
		if (CollectionUtils.isEmpty(policyList)) {
			return null;
		}
		Map<String, Integer> statusMap = new HashMap<>();
		List<String> carIds = new ArrayList<>();
		for (InsurancePolicy policy : policyList) {
			carIds.add(policy.getCarId());
			statusMap.put(policy.getOrderId(), policy.getStatus());
		}

		List<Car> cars = carDao.findByIdInAndGuohu(carIds, true);
		if (CollectionUtils.isNotEmpty(cars)) {
			for (Car car : cars) {
				car.setGuohu(false);
				car.setMakeDate(null);
			}
			carDao.save(cars);
		}
		return policyDao.save(policyList);
	}

	/**
	 * 更新保单
	 */
	@Transactional
	public InsurancePolicy updateInsurancePolicy(InsurancePolicy policy) {
		InsurancePolicy insurancePolicy = policyDao.save(policy);
		return insurancePolicy;
	}

	@Transactional
	public Comments addComments(String policyId, String content, String operator) {
		Comments c = new Comments();
		c.setContent(content);
		c.setCreateDate(new Date());
		c.setName(operator);
		c.setPolicyId(policyId);
		commentsDao.save(c);

		return c;
	}

	/**
	 * 批量更新保单
	 */
	@Transactional
	public List<InsurancePolicy> updateInsurancePolicy(List<InsurancePolicy> policyList) {
		if (CollectionUtils.isEmpty(policyList)) {
			return null;
		}
		List<InsurancePolicy> insurancePolicyList = policyDao.save(policyList);

		Map<String, Integer> statusMap = new HashMap<>();
		List<String> carIds = new ArrayList<>();
		for (InsurancePolicy policy : policyList) {
			carIds.add(policy.getCarId());
			statusMap.put(policy.getOrderId(), policy.getStatus());
		}
		return insurancePolicyList;
	}

	/**
	 * @param policyId
	 *            投保单ID
	 * @param userName
	 *            操作者
	 */
	@Transactional
	public void deletePolicy(String policyId, String userName) {
		InsurancePolicy policy = policyDao.findOne(policyId);
		if (policy == null) {
			return;
		}
		policyDao.delete(policy);
		PolicyFee fee = policyFeeDao.findOne(policyId);
		if (fee != null) {
			policyFeeDao.delete(fee);
		}

		// 删除批单
		List<InsurancePolicy> policyList = policyDao.findByAssociatedPolicyId(policyId);
		if (CollectionUtils.isNotEmpty(policyList)) {
			for (InsurancePolicy p : policyList) {
				policyDao.delete(policy);
				fee = policyFeeDao.findOne(p.getOrderId());
				if (fee != null) {
					policyFeeDao.delete(fee);
				}
			}
		}

		// 删除保存在阿里云上面的保单相关的附件
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest("auditimg");
		listObjectsRequest.setPrefix(policyId + "/");
		ObjectListing listing = client.listObjects(listObjectsRequest);

		List<String> imgs = new ArrayList<>();
		for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
			imgs.add(objectSummary.getKey());
		}
		// 当阿里云上面有这张保单相关的附件的时候，才去删除
		if (CollectionUtils.isNotEmpty(imgs)) {
			// 同时删除掉该文件夹
			imgs.add(policyId + "/");
			DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest("auditimg");
			deleteObjectsRequest.setKeys(imgs);
			client.deleteObjects(deleteObjectsRequest);
		}
		logger.info("{}删除了{}的投保单，ID = {}", userName, policy.getCarNumber(), policyId);
	}

	/**
	 * 执行渠道商佣金结算excel
	 */
	@Async
	public void executeCommissionSettlementTask(final Date createDate) {
		try {
			logger.info("开始执行渠道佣金结算任务...");
			List<Object[]> result = policyDao.findLeCreateDate(createDate);
			if (CollectionUtils.isEmpty(result)) {
				logger.info("结算周期内无保单，退出任务.");
				return;
			}
			int bdCount = 0;
			int pdCount = 0;
			for (Object[] obj : result) {
				int policyType = (boolean) obj[6] == true ? 1 : 2;
				if (policyType == 1) {
					bdCount++;
				} else {
					pdCount++;
				}
			}
			logger.info("结算周期内共：{}张单子，{}张保单，{}张批单", result.size(), bdCount, pdCount);
			Map<Long, BigDecimal> amountMap = new HashMap<>();
			Map<Long, String> numberMap = new HashMap<>();
			Map<Long, String> policyIdMap = new HashMap<>();
			for (Object[] obj : result) {
				long merchantCode = Long.parseLong(obj[0].toString());
				String carNumber = obj[1].toString();
				float returnedCash = Float.parseFloat(obj[2].toString());
				float sumPremium = Float.parseFloat(obj[3].toString());
				float commissionRate = Float.parseFloat(obj[4].toString());
				String policyId = obj[5].toString();
				BigDecimal temp = new BigDecimal(sumPremium * commissionRate);
				returnedCash = temp.setScale(2, RoundingMode.HALF_UP).floatValue();
				// 返现金额为0的，忽略掉
				if (returnedCash == 0) {
					logger.info("{}返现金额为0，跳过，此单无需结算");
					continue;
				}
				if (amountMap.containsKey(merchantCode)) {
					amountMap.put(merchantCode, amountMap.get(merchantCode).add(new BigDecimal(returnedCash)));
				} else {
					amountMap.put(merchantCode, new BigDecimal(returnedCash));
				}

				if (numberMap.containsKey(merchantCode)) {
					String nubmer = numberMap.get(merchantCode);
					numberMap.put(merchantCode, nubmer + "," + carNumber);
				} else {
					numberMap.put(merchantCode, carNumber);
				}

				if (policyIdMap.containsKey(merchantCode)) {
					String existing = policyIdMap.get(merchantCode);
					policyIdMap.put(merchantCode, existing + "," + policyId);
				} else {
					policyIdMap.put(merchantCode, policyId);
				}
			}

			List<PaymentRecord> records = new ArrayList<>();
			for (Map.Entry<Long, BigDecimal> entry : amountMap.entrySet()) {
				Long channelCode = entry.getKey();
				double amount = entry.getValue().setScale(2, RoundingMode.HALF_UP).doubleValue();
				Merchant merchant = merchantDao.findOne(channelCode);
				if (merchant == null)
					continue;
				PaymentRecord record = new PaymentRecord();
				record.setSerialNo(UUID.randomUUID().toString().replace("-", ""));// 结算序列号
				record.setBank(merchant.getBank());
				record.setBankAcountName(merchant.getBankAcountName());
				record.setBankCard(merchant.getBankCard());
				record.setProvince(merchant.getProvince());

				City city = cityService.getByCityCode(merchant.getCityCode());
				record.setCity(city.getName());
				record.setAmount(amount);
				record.setRemark(numberMap.get(channelCode));
				record.setChannelCode(String.valueOf(channelCode));
				record.setChannelName(merchant.getName());
				record.setPolicyId(policyIdMap.get(channelCode));
				records.add(record);
			}
			String excelFileName = DateUtils.convertDateToStr(new Date(), "yyyy-MM-dd") + ".xls";
			cmbPaymentGenerator.execute(records, excelFileName);
			// jiaotongBankPaymentGenerator.execute(records, excelFileName);
			logger.info("佣金结算任务执行成功.");
		} catch (Exception ex) {
			logger.error("执行佣金结算任务失败，", ex);
		}
	}

	public MerchantCommissionRate findCommissionRateByCompanyCode(int companyCode) {
		return merchantCommissionRateDao.findByCompanyCode(companyCode);
	}

	public List<InsurancePolicy> getPolicyInPolicyIds(List<String> policyIds) {
		return policyDao.findByOrderIdIn(policyIds);
	}

	// 更新保单紧急度
	@Transactional
	public void updatePriority(String orderId, int priority) {
		policyDao.updatePriority(orderId, priority);
	}

	// 更新保单标记
	@Transactional
	public void updateFlag(String orderId, int flag) {
		policyDao.updateFlag(orderId, flag);
	}

	/**
	 * 统计各种状态下的保单的数量 key是状态编码（见{@link PolicyStatus}），value是保单数量
	 */
	public Map<Integer, Long> groupByStatus() {
		List<Object[]> result = policyDao.countStatus();
		Map<Integer, Long> resultMap = new HashMap<>();
		for (Object[] obj : result) {
			int status = (int) obj[0];
			long count = (long) obj[1];

			resultMap.put(status, count);
		}
		return resultMap;
	}

	/**
	 * 按状态统计保单数量，比如待核保4个，核保通过10，已出单20个，其它200个
	 * 
	 * @param customerservice
	 *            客服ID，如果客服ID > 0，则统计范围限定在该客服名下的保单
	 */
	public Map<String, Long> countGroupByStatus(int customerservice) {
		List<Object[]> result = null;
		if (customerservice > 0) {
			result = policyDao.countGroupByStatus(customerservice);
		} else {
			result = policyDao.countStatus();
		}
		Map<String, Long> map = new HashMap<>();
		for (Object[] obj : result) {
			int status = (int) obj[0];
			long count = (long) obj[1];
			if (status == PolicyStatus.NEED_UNDERWRITE.value()) {
				map.put("need_underwrite", count);
			} else if (status == PolicyStatus.UNDERWRITE_OK.value()) {
				map.put("underwrite_ok", count);
			} else if (status == PolicyStatus.NO_QUERY_BAOJIA.value()) {
				map.put("need_query", count);
			} else if (status == PolicyStatus.RESPONSE_BAOJIA.value()) {
				map.put("query_ok", count);
			} else if (status == PolicyStatus.CHUDAN_FINISHED.value()) {
				map.put("chudan_finished", count);
			} else if (status == PolicyStatus.CHUDAN_AGREE.value()) {
				map.put("chudan_agree", count);
			} else if (status == PolicyStatus.SUSPENDED.value()) {
				map.put("suspended", count);
			} else {
				if (map.get("other") != null) {
					map.put("other", map.get("other") + count);
				} else {
					map.put("other", count);
				}
			}
		}
		return map;
	}

	public int countProrityRequestCount() {
		return policyDao.countProrityRequestCount();
	}

	@Transactional
	public void updateReadFlag(String orderId, int flag) {
		policyDao.updateReadFlag(orderId, flag);
	}

	public Map<Integer, Integer> staticsFlagPolicy() {
		Map<Integer, Integer> resultMap = new HashMap<Integer, Integer>();
		List<Object[]> results = policyDao.staticsFlagPolicy();
		if (CollectionUtils.isNotEmpty(results)) {
			for (Object[] objects : results) {
				int flag = Integer.parseInt(objects[0].toString());
				int count = Integer.parseInt(objects[1].toString());
				resultMap.put(flag, count);
			}
		}
		return resultMap;
	}

	/**
	 * 手动录单和创建批单都会调用该方法
	 * 
	 * @param policy
	 * @param expressAddress
	 */
	@Transactional
	public void manualRecording(InsurancePolicy policy, String expressAddress, String operator) {
		PolicyFee policyFee = policyFeeDao.findByPolicyId(policy.getOrderId());
		if (policyFee == null) {
			policyFee = new PolicyFee();
		}
		if (policy.getStatus() == PolicyStatus.CHUDAN_FINISHED.value()) {
			policyFee.setStatus(0);//
		}
		Merchant merchant = null;
		if (policy.getChannelCode() > 0) {// 填写商家推荐码标识该车属于某个商家
			merchant = merchantDao.findOne(policy.getChannelCode());
			policyFee.setReturnTarget(merchant.getName());

			MerchantConfig merchantConfig = merchantConfigDao.findOne(merchant.getId());
			if (merchantConfig != null) {
				policyFee.setExpressCompany(merchantConfig.getExpressCompany());
			}
		} else {
			policyFee.setReturnTarget(policy.getInsurant());
		}
		policyFee.setPolicyId(policy.getOrderId());
		// 计算每单的佣金比例
		MerchantCommissionRate rate = merchantCommissionRateDao.findByCompanyCode(policy.getCompanyCode());
		policyFee.setCommissionRate(rate.getRate() / 100f);// 返点的点数
		BigDecimal returndCash = new BigDecimal(policy.getSumPremium() * rate.getRate() / 100);
		policyFee.setReturnedCash(returndCash.setScale(2, RoundingMode.HALF_UP).floatValue());// 返现金额
		// 计算刷卡手续费，默认千分之3.8
		float total = policy.getTotalPremium();
		policyFee.setPremiumDueAmount(total);
		// 保费总金额大于0的时候，才计算刷卡手续费
		if (total > 0) {
			policyFee.setPosFee(new BigDecimal(total * 0.0038f).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
		}
		if (policy.getPolicyType() == 2) {
			// 如果是批单，使用原保单的配送地址
			PolicyFee associatedPolicyFee = policyFeeDao.findByPolicyId(policy.getAssociatedPolicyId());
			policyFee.setExpressAddress(associatedPolicyFee.getExpressAddress());
		} else {
			City city = cityService.getByCityCode(policy.getCityCode());
			policyFee.setExpressAddress(city.getProvince() + city.getName() + policy.getRegion() + expressAddress);
		}
		policyDao.save(policy);

		if (merchant != null) {
			// 渠道提交的保单，收件人默认是商家老板
			policyFee.setRecipients(merchant.getManager());
		} else {
			// 个人用户，收件人和联系电话，默认是被保险人、被保险人电话
			policyFee.setRecipients(policy.getInsurant());
		}
		policyFee.setRecipientsPhone(String.valueOf(policy.getPhone()));
		policyFeeDao.save(policyFee);

		if (policy.getPolicyType() == 1) {
			this.addComments(policy.getOrderId(), "手动录入了一张保单", operator);
		} else {
			this.addComments(policy.getOrderId(), "手动录入了一张批单", operator);
		}
	}

	/**
	 * 查看这张保单的快递单号
	 */
	public String getExpressSerialNo(String policyId) {
		PolicyFee fee = policyFeeDao.findOne(policyId);
		if (fee == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "保单配送信息不存在！");
		}
		if (StringUtils.isNotEmpty(fee.getExpressSerialNo())) {
			return fee.getExpressSerialNo();
		} else {
			return "亲，我们的师傅正在分拣，快递单号马上就来...";
		}
	}

	/**
	 * 修改保单状态
	 * 
	 * @param policy
	 *            保单
	 * @param targetStatus
	 *            准备修改成该状态
	 * @param comments
	 *            备注，可能为空
	 * @param userName
	 *            操作人
	 */
	@Transactional
	public InsurancePolicy changePolicyStatus(InsurancePolicy policy, int targetStatus, String comments, String userName) {
		String action = null;
		int currentStatus = policy.getStatus();
		if ((currentStatus == PolicyStatus.UNDERWRITE_OK.value() || currentStatus == PolicyStatus.CHUDAN_AGREE.value())
				&& targetStatus == PolicyStatus.CHUDAN_FINISHED.value()) {
			// 只有处于核保通过状态的保单才能执行出单操作
			policy.setStatus(PolicyStatus.CHUDAN_FINISHED.value());
			action = "已完成缴费，出单完成";
			policy.setCreateDate(new Date());// 将投保时间更新为当天
			policy.setFlag(PolicyFlag.NORMAL.value());
			policy.setPriority(1);

			PolicyFee fee = policyFeeDao.findOne(policy.getOrderId());
			fee.setStatus(0);// 将配送状态改成待配送
			policyFeeDao.save(fee);
		} else if (currentStatus == PolicyStatus.CHUDAN_FINISHED.value() && targetStatus == PolicyStatus.DELIVERING.value()) {
			// 处于已出单状态的保单，可以进入正在配送状态
			policy.setStatus(PolicyStatus.DELIVERING.value());
			action = "保单正在配送";

			PolicyFee fee = policyFeeDao.findOne(policy.getOrderId());
			fee.setStatus(2);// 配送中
			policyFeeDao.save(fee);
		} else if ((currentStatus == PolicyStatus.CHUDAN_FINISHED.value() || currentStatus == PolicyStatus.DELIVERING.value())
				&& targetStatus == PolicyStatus.DISPATCHED.value()) {
			// 处于已出单状态、正在配送状态的保单，可以进入已配送状态
			policy.setStatus(PolicyStatus.DISPATCHED.value());
			action = "保单配送完成";

			PolicyFee fee = policyFeeDao.findOne(policy.getOrderId());
			fee.setStatus(4);// 将配送状态改成完成
			policyFeeDao.save(fee);
		} else if (currentStatus == PolicyStatus.DISPATCHED.value() && targetStatus == PolicyStatus.PAID.value()) {
			// 处于已配送状态的保单，可以执行保费入账操作，保单状态进入保费已入账
			policy.setStatus(PolicyStatus.PAID.value());
			PolicyFee fee = policyFeeDao.findOne(policy.getOrderId());
			fee.setPremiumDueDate(new Date());
			policyFeeDao.save(fee);
			action = "我检查了一下， 保费已入公司帐户";
		} else if (currentStatus == PolicyStatus.PAID.value() && targetStatus == PolicyStatus.BROKERAGE_GRANTED.value()) {
			policy.setStatus(PolicyStatus.TRANSACTION_OK.value());// 处于保费已入账的保单，可以执行发放佣金操作，佣金发放完成后保单变成交易完成
			policy.setSettleStatus(1);
			action = "我检查了一下，佣金确实发了";
			PolicyFee policyFee = policyFeeService.findByPolicyId(policy.getOrderId());
			policyFee.setReturnCashDate(new Date());
			policyFeeService.savePolicyFee(policyFee);
		} else if (currentStatus == PolicyStatus.PAID.value() && targetStatus == PolicyStatus.TRANSACTION_OK.value()) {
			// 处于保费已入账的保单，可以直接进入交易完成状态
			policy.setStatus(PolicyStatus.TRANSACTION_OK.value());
			policy.setSettleStatus(1);
			action = "投保结束，交易完成";
		} else if (currentStatus == PolicyStatus.BROKERAGE_GRANTED.value() && targetStatus == PolicyStatus.TRANSACTION_OK.value()) {
			// 佣金已发放的保单，可以进入交易完成状态
			policy.setStatus(PolicyStatus.TRANSACTION_OK.value());
			policy.setSettleStatus(1);
			action = "投保结束，交易完成";
		} else if (targetStatus == PolicyStatus.TRANSACTION_FAILED.value()
				&& (currentStatus == PolicyStatus.NEED_UNDERWRITE.value() || currentStatus == PolicyStatus.UNDERWRITE_OK.value()
						|| currentStatus == PolicyStatus.RESPONSE_BAOJIA.value() || currentStatus == PolicyStatus.CHUDAN_AGREE.value())) {
			// 处于待核保、核保通过、已反馈报价、同意出单状态下的保单可以执行交易失败操作
			policy.setStatus(PolicyStatus.TRANSACTION_FAILED.value());
			action = "很可惜，交易失败";
		} else if (targetStatus == PolicyStatus.RESPONSE_BAOJIA.value()) {
			policy.setStatus(PolicyStatus.RESPONSE_BAOJIA.value());
			action = "把报价反馈给客户了";
		} else if (currentStatus == PolicyStatus.UNDERWRITE_OK.value() && targetStatus == PolicyStatus.CHUDAN_AGREE.value()) {
			policy.setStatus(PolicyStatus.CHUDAN_AGREE.value());
			policy.setFlag(PolicyFlag.NORMAL.value());
			policy.setPriority(1);
			action = "这张保单我同意出单，请缴费出单吧";
		} else if ((currentStatus == PolicyStatus.NEED_UNDERWRITE.value() || currentStatus == PolicyStatus.UNDERWRITE_OK.value()
				|| currentStatus == PolicyStatus.NO_QUERY_BAOJIA.value() || currentStatus == PolicyStatus.RESPONSE_BAOJIA.value())
				|| currentStatus == PolicyStatus.CHUDAN_AGREE.value() && targetStatus == PolicyStatus.SUSPENDED.value()) {
			// 记录下挂起前的状态
			policy.setBeforeSuspendedStatus(policy.getStatus());
			policy.setStatus(PolicyStatus.SUSPENDED.value());
			policy.setReadFlag(0);
			action = "保单有问题，暂时挂起";
		} else {
			return null;
		}

		Comments c = new Comments();
		c.setCreateDate(new Date());
		c.setName(userName);
		c.setPolicyId(policy.getOrderId());
		if (StringUtils.isNotEmpty(comments)) { // 新增1条备注
			c.setContent(comments);
		} else {
			c.setContent(action);
		}
		commentsDao.save(c);
		policy.setUpdateTime(new Date());
		if (targetStatus == PolicyStatus.CHUDAN_FINISHED.value()) {
			this.chudan(policy);
		} else {
			this.updateInsurancePolicy(policy);
		}
		return policy;
	}

	/**
	 * 保单挂起后，解挂
	 */
	@Transactional
	public void resume(InsurancePolicy policy, String operator) {
		if (policy.getStatus() != PolicyStatus.SUSPENDED.value()) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ACCESS, "不能对非挂起状态下的保单进行解挂操作！");
		}
		policy.setStatus(policy.getBeforeSuspendedStatus());
		policy.setBeforeSuspendedStatus(0);
		this.updateInsurancePolicy(policy);
		this.addComments(policy.getOrderId(), "恢复保单", operator);
	}

	/**
	 * 读取保单的附件url，附件都存储在阿里云oss上面
	 */
	public List<String> getPolicyAttachmentUrls(String orderId) {
		// 构造ListObjectsRequest请求
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest("auditimg");
		listObjectsRequest.setPrefix(orderId + "/");
		ObjectListing listing = client.listObjects(listObjectsRequest);

		List<String> urls = new ArrayList<>();
		for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
			urls.add("http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + objectSummary.getKey());
		}
		return urls;
	}

	public InsurancePolicy isDuplicatePolicy(String carId) {
		InsurancePolicy policy = policyDao.isDuplicatePolicy(carId);
		return policy;
	}

	public void savePic(String file, InputStream inputStream) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(inputStream.available());
		client.putObject("auditimg", file, inputStream, metadata);
		try {
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void deleteAttachment(String policyId, String attachmentId) {
		client.deleteObject("auditimg", attachmentId);
	}

	public void deleteAttachment(String attachmentId) {
		if (client.doesObjectExist("auditimg", attachmentId)) {
			client.deleteObject("auditimg", attachmentId);
		}
	}

	public boolean hasAttchmentByPolicyId(String policyId) {
		InsurancePolicy policy = policyDao.findOne(policyId);
		if (null != policy) {

			ListObjectsRequest listObjectsRequest = new ListObjectsRequest("auditimg");

			listObjectsRequest.setPrefix(policyId + "/");
			ObjectListing listing = client.listObjects(listObjectsRequest);
			if (listing.getObjectSummaries().size() > 0) {
				return true;
			} else {
				listObjectsRequest.setPrefix(policy.getCarId() + "/");
				listing = client.listObjects(listObjectsRequest);
				if (listing.getObjectSummaries().size() > 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 启动保险公司对账单任务，异步执行
	 * 
	 * @param startDate
	 *            统计开始时间
	 * @param endDate
	 *            统计结束时间
	 */
	@Async
	public void startSettleTask(String startDate, String endDate) {
		try {
			policySettleTask.doTask(DateUtils.convertStrToDate(startDate, "yyyy-MM-dd HH:mm:ss"), DateUtils.convertStrToDate(endDate, "yyyy-MM-dd HH:mm:ss"));
		} catch (Exception e) {
			logger.error("对账单任务执行失败.", e);
		}
	}

	/**
	 * 根据网银转账流水清单，自动更新系统中相应保单的状态
	 * 
	 * @param excel
	 *            网银转账交易流水单，excel格式
	 * @param moneyFromBank
	 *            财务人员根据网银中实际完成的交易金额填写，精确到小数点后两位
	 * @return 本次更新的保单数量
	 */
	public int updatePolicyStatusFromBankTransactionExcel(MultipartFile excel, AccountInfo accountInfo) {
		logger.info("开始导入渠道佣金结算流水单...");
		String[] fieldName = { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "fee", null, null, null,
				null, null, "policyIds" };
		String[] fieldTitle = { "业务参考号", "收款人编号", "收款人账号", "收款人名称", "收方开户支行", "收款人所在省", "收款人所在市", "收方邮件地址", "收方移动电话", "币种", "付款分行", "结算方式", "业务种类", "付方帐号",
				"期望日", "期望时间", "用途", "金额", "收方行号", "收方开户银行", "业务摘要", "渠道商户", "结算车辆", "保单ID" };
		int totleRow = 0;
		boolean b;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {
			logger.info("开始校验excel格式...");
			b = ExcelHanderUtil.validOrder(fieldTitle, bis);
		} catch (Exception e) {
			logger.error("校验表头格式失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		if (!b) {
			throw new CommonLogicException(1, "非标准数据表头");
		}
		logger.info("excel格式校验通过");
		List<InsurancePolicy> listNew = new ArrayList<InsurancePolicy>();
		List<BrokerageBill> list;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {
			list = ExcelHanderUtil.readInputStreamToEntity(bis, fieldName, BrokerageBill.class, 1);
		} catch (Exception e) {
			logger.error("excel数据读取失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		logger.info("本次需解析{}条记录", list.size());

		for (BrokerageBill item : list) {
			// 如果汇款成功
			String[] policyIds = org.apache.commons.lang3.StringUtils.split(item.getPolicyIds(), ",");
			for (String policyId : policyIds) {
				InsurancePolicy policy = policyDao.findOne(policyId);
				if (null == policy) {
					throw new CommonLogicException(1, "未能找到保单：" + policyId);
				} else if (policy.getStatus()== PolicyStatus.BROKERAGE_GRANTED.value()||policy.getStatus()== PolicyStatus.TRANSACTION_OK.value()) {
					continue;
				} 
				// 佣金已发放直接交易完成
				policy.setStatus(PolicyStatus.TRANSACTION_OK.value());
				logger.info("更新{}的保单状态为交易完成", policy.getCarNumber());
				listNew.add(policy);
				addComments(policyId, "导入数据，自动更新状态为交易完成", accountInfo.getUserName());
			}
		}

		if (listNew.size() > 0) {
			policyDao.save(listNew);
			logger.info("更新结束，本次共更新{}张保单，将状态置为交易完成。");
		} else {
			logger.info("更新结束，本次无需要更新状态的保单。");
		}

		totleRow = listNew.size();
		return totleRow;
	}
}
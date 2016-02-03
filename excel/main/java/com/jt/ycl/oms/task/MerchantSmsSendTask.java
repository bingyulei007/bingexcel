package com.jt.ycl.oms.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jt.core.SmsService;
import com.jt.core.SmsTemplateId;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.Merchant;
import com.jt.core.model.OmsUser;
import com.jt.core.model.PolicyStatus;
import com.jt.ycl.oms.account.AccountService;

/**
 * 每天统计出单商户、未出单商户，发送激励短信
 * 
 * @author xiaojiapeng
 */
@Component
public class MerchantSmsSendTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private SmsService smsService;

	@Autowired
	private MerchantDao merchantDao;

	@Autowired
	private InsurancePolicyDao insurancePolicyDao;

	@Autowired
	private AccountService accountService;

	@Scheduled(cron = "0 0 15 * * ?")
	// 每天15点运行一次
	public void doTask() {
		// 1.查询出所有的车险商家
		List<Merchant> merchants = merchantDao.findAllChexianMerchant();
		// 2.查询出所有的今天晚上8点以前所有商家出的保单
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date startDate = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MILLISECOND, -1);
		Date endDate = cal.getTime();

		List<Object[]> objects = insurancePolicyDao.getBrokerPoliciesByCreateDate(startDate, endDate);
		List<InsurancePolicy> policies = new ArrayList<InsurancePolicy>();
		Map<String, Float> policyFeeMap = new HashMap<String, Float>();
		if (CollectionUtils.isNotEmpty(objects)) {
			for (Object[] object : objects) {
				InsurancePolicy insurancePolicy = new InsurancePolicy();
				String orderId = object[0].toString();
				insurancePolicy.setOrderId(orderId);
				insurancePolicy.setChannelCode(Long.parseLong(object[1].toString()));
				insurancePolicy.setStatus(Integer.parseInt(object[2].toString()));
				policyFeeMap.put(orderId, Float.parseFloat(object[3].toString()));
				// p.orderId, p.merchantCode, p.status, f.returnedCash
			}
		}

		Map<Long, MerchantSmsInfo> merchantMap = new HashMap<>();

		for (Merchant merchant : merchants) {
			String managerPhone = merchant.getManagerPhone();
			// 过滤掉非法手机号码
			if (StringUtils.isEmpty(managerPhone) && managerPhone.length() != 11) {
				continue;
			}
			MerchantSmsInfo smsInfo = new MerchantSmsInfo();
			smsInfo.setName(merchant.getName());
			String merchantCode = merchant.getId() + "";
			smsInfo.setMerchantCode(merchantCode);
			smsInfo.setPhone(managerPhone);
			String salesMan = merchant.getSalesman();
			smsInfo.setSalesMan(salesMan);

			// 检索销售人员的手机号码
			if (StringUtils.isNotEmpty(salesMan)) {
				OmsUser omsUser = accountService.findByName(salesMan);
				if (omsUser != null) {
					smsInfo.setSalesManPhone(omsUser.getPhone());
				}
			}

			merchantMap.put(merchant.getId(), smsInfo);
		}

		for (InsurancePolicy policy : policies) {
			long channelCode = policy.getChannelCode();
			// 容错处理
			if (channelCode == 0) {
				continue;
			}
			MerchantSmsInfo merchantSmsInfo = merchantMap.get(channelCode);
			if (merchantSmsInfo == null) {
				if (logger.isWarnEnabled()) {
					logger.warn("渠道商编码" + channelCode + "的商家不存在");
				}
				continue;
			}

			int status = policy.getStatus();
			float returnedCash = policyFeeMap.get(policy.getOrderId());

			if (status != PolicyStatus.TRANSACTION_FAILED.value()) {
				// 交易失败的保单返现不计入总保费
				merchantSmsInfo.setTotalCommission(merchantSmsInfo.getTotalCommission() + returnedCash);
			}
			// 统计各种状态的保单数量
			merchantSmsInfo.setTotal(merchantSmsInfo.getTotal() + 1);
			if (status == PolicyStatus.CHUDAN_FINISHED.value()) {
				merchantSmsInfo.setChudan_finished(merchantSmsInfo.getChudan_finished() + 1);
				// 已出单的同时需要统计返现金额
				merchantSmsInfo.setExpectCommission(merchantSmsInfo.getExpectCommission() + returnedCash);
			} else if (status == PolicyStatus.NEED_UNDERWRITE.value()) {
				merchantSmsInfo.setNeed_underwrite(merchantSmsInfo.getNeed_underwrite() + 1);
			} else if (status == PolicyStatus.UNDERWRITE_OK.value()) {
				merchantSmsInfo.setUnderwrite_ok(merchantSmsInfo.getUnderwrite_ok() + 1);
			} else if (status == PolicyStatus.RESPONSE_BAOJIA.value()) {
				merchantSmsInfo.setResponse_baojia(merchantSmsInfo.getResponse_baojia() + 1);
			} else if (status == PolicyStatus.TRANSACTION_FAILED.value()) {
				merchantSmsInfo.setTransaction_failed(merchantSmsInfo.getTransaction_failed() + 1);
			}
		}

		List<MerchantSmsInfo> smsInfoList = new ArrayList<>(merchantMap.values());

		// 根据出的总保单数排序，创建渠道商成交量龙虎榜
		Collections.sort(smsInfoList, new Comparator<MerchantSmsInfo>() {
			@Override
			public int compare(MerchantSmsInfo o1, MerchantSmsInfo o2) {
				return o2.getTotal() - o1.getTotal();
			}

		});

		// 发送短信
		for (MerchantSmsInfo smsInfo : smsInfoList) {
			if (smsInfo.getTotal() != 0) {
				String[] datas = new String[] { smsInfo.getName(), smsInfo.getTotal() + "",
						smsInfo.getChudan_finished() + "", smsInfo.getNeed_underwrite() + "",
						smsInfo.getUnderwrite_ok() + "", smsInfo.getResponse_baojia() + "",
						smsInfo.getTransaction_failed() + "", smsInfo.getExpectCommission() + "",
						smsInfo.getTotalCommission() + "" };
				smsService.send(SmsTemplateId.CHUDAN_MERCHANT_NOTIFY, smsInfo.getPhone(), datas);
			} else {
				// TODO 未产生保单的商户暂且不发送短信
			}
		}
	}
}
package com.jt.ycl.oms.insurance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.ycl.oms.common.vo.YiGaoExpress;
import com.jt.ycl.oms.common.vo.YiGaoExpress.ExpressStatus;
import com.jt.ycl.oms.common.vo.YiGaoExpress.PayWay;
import com.jt.ycl.oms.insurance.vo.BrokerageBill;
import com.jt.ycl.oms.report.util.ExcelHanderUtil;

/**
 * 
 * @author bing
 * 
 */
@Service
@Transactional
public class AccountCheckingService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private InsurancePolicyDao policyDao;

	/**
	 * 
	 * @param startDate
	 * @param excel
	 * @param totleMoney
	 * @param verifed
	 *            true表示只是验证数据，不提交数据更新到系统；false表示更新数据
	 * @return
	 */
	public List<String> excelForYiGaoCheck(Date startDate, Date endDate, MultipartFile excel, double totleMoney) {
		List<String> listReturn = new ArrayList<String>();
		List<String> errListPolicyId = new ArrayList<String>();
		//重复保单提示
		List<String> repeatedPolicy = new ArrayList<String>();
		int selectedDateNum = 0;
		// 符合的总条数
		int totleRow = 0;
		// 更改的数目
		int changeRow = 0;
		// 刷卡的保单数目
		int posNum = 0;
		// 保费账面金额
		double premiumTotle = 0;
		// 误差金额
		double accumulatedError = 0;
		double expectAllMoney = 0d;
		String[] fieldTitle = { "保单ID", "配送状态", "提单时间", "完成日期", "问题件描述", "支付方式", "快递单号", "被保险人", "车牌", "保费合计", "送单地址", "联系电话", "保险公司" };
		String[] fieldName = { "policyId", "status", null, "finishedTime", null, "payWay", null, "insurant", "carNumber", "totlePremium", null, null, null };
		boolean b;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {

			b = ExcelHanderUtil.validOrder(fieldTitle, bis);
		} catch (Exception e) {
			logger.error("校验表头格式失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		if (!b) {
			throw new CommonLogicException(1, "非标准数据表头");
		}
		List<YiGaoExpress> list;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {
			list = ExcelHanderUtil.readInputStreamToEntity(bis, fieldName, YiGaoExpress.class, 1);

		} catch (Exception e) {
			logger.error("excel数据读取失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		logger.info("Excel记录总数：{}", list.size());

		List<YiGaoExpress> listUnFilter = new ArrayList<YiGaoExpress>();
		Set<String> setIds = new HashSet<>();
		for (YiGaoExpress yiGaoCheck : list) {

			Date finishedTime = yiGaoCheck.getFinishedTime();

			if ((String.format("%tF", startDate)).equals(String.format("%tF", finishedTime))
					|| (String.format("%tF", endDate)).equals(String.format("%tF", finishedTime))) {
				selectedDateNum++;
				
				if (ExpressStatus.FINISHED.equals(yiGaoCheck.getStatus())) {
					if (StringUtils.isBlank(yiGaoCheck.getPolicyId())) {
						listReturn.add("<font style='color:red'>所选日期内发现id为空值，状态为完成的保单</font>");
						continue;
					}
					double surfacePremium = yiGaoCheck.getTotlePremium();
					premiumTotle += surfacePremium;
					double realPremium = surfacePremium;
					if (PayWay.POS.equals(yiGaoCheck.getPayWay()) || PayWay.POS2.equals(yiGaoCheck.getPayWay())) {
						realPremium = (surfacePremium - surfacePremium * YiGaoExpress.posRate);
						posNum++;
					}
					expectAllMoney += realPremium;
					listUnFilter.add(yiGaoCheck);
					setIds.add(yiGaoCheck.getPolicyId());
					if (setIds.size() != listUnFilter.size()) {

						repeatedPolicy.add("<font style='color:red'>所选日期内发现重复保单" + yiGaoCheck.getPolicyId() + "</font>");
						setIds.add(UUID.randomUUID().toString());
					}
				}

			}
		}
		BigDecimal bgd = new BigDecimal(expectAllMoney);
		expectAllMoney = bgd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		accumulatedError = Math.abs(expectAllMoney - totleMoney);
		totleRow = listUnFilter.size();

		listReturn.add(String.format("<font style='font-size:16px;'>%tF到%tF共有%d条数据;其中配送完成%d条,使用POS付款的%d条。</font>", startDate, endDate, selectedDateNum,
				totleRow, posNum));
		listReturn.add(String.format("<font style='font-size:16px;'>扣除手续费%.2f元后，应收款项总额为%.2f元;实际收到账款为%.2f元,误差为%.2f元</font>", premiumTotle - expectAllMoney,
				expectAllMoney, totleMoney, accumulatedError));
		logger.debug("校验总金额通过,误差为{}元", accumulatedError);
		if (Math.abs(accumulatedError) > 0.01 * posNum) {
			String errMsg = String.format("<font style='color:red;'>本次金额误差%.2f元，差额较大，请认真核对</font>", accumulatedError);
			listReturn.add(errMsg);
		}
		listReturn.addAll(repeatedPolicy);
		for (YiGaoExpress yiGaoCheck : listUnFilter) {

			InsurancePolicy insurancePolicy = policyDao.findOne(yiGaoCheck.getPolicyId());
			if (insurancePolicy == null) {
				String errMsg = String.format("数据校验失败，系统中找不到对应保单%s", yiGaoCheck.getPolicyId());
				logger.debug(errMsg);
				listReturn.add(errMsg);
				continue;
			}

			double expectPremium = insurancePolicy.getCarShipTax() + insurancePolicy.getSumPremiumCI() + insurancePolicy.getSumPremium();
			// 此处比较的是易高与系统中的保费
			if (Math.abs(expectPremium - yiGaoCheck.getTotlePremium()) > 0.01) {
				logger.debug("校验表单数据{}", yiGaoCheck.getPolicyId());
				listReturn.add(String.format("<font style='color:red;'>数据校验失败，保单%s保费金额与系统金额相差%.2f元</font>",yiGaoCheck.getPolicyId(), (expectPremium - yiGaoCheck.getTotlePremium())));
				continue;
			}

			if (insurancePolicy.getStatus() == PolicyStatus.DISPATCHED.value()) {
				changeRow++;

			} else {
				errListPolicyId.add(insurancePolicy.getOrderId());
			}

		}
		if (changeRow == 0) {
			if (listReturn.size() == 2) {
				listReturn.add("未检测到有需要更新的保单");
			} else {

				listReturn.add("未检测到有需要更新的保单，但存在数据与系统不一致问题，请认真核对");

			}
		} else if (changeRow == totleRow) {
			if (listReturn.size() == 2) {
				listReturn.add("数据通过校验，数据可以导入");
			}
		} else {
			logger.debug("数据校验失败，状态不符合要求");
			listReturn.add("数据校验失败，以下保单状态存在问题");
			for (String id : errListPolicyId) {
				listReturn.add("&nbsp;&nbsp;&nbsp;" + id);
			}
		}

		return listReturn;
	}

	/**
	 * 
	 * @param startDate
	 * @param excel
	 * @param totleMoney
	 * @param verifed
	 *            true表示只是验证数据，不提交数据更新到系统；false表示更新数据
	 * @return
	 */
	public int excelForYiGaoUpgrade(Date startDate, Date endDate, MultipartFile excel, double totleMoney) {
		// 符合的总条数
		int totleRow = 0;
		// 更改的数目
		int changeRow = 0;
		// 刷卡的保单数目
		int posNum = 0;
		// 误差金额
		double accumulatedError;
		String[] fieldTitle = { "保单ID", "配送状态", "提单时间", "完成日期", "问题件描述", "支付方式", "快递单号", "被保险人", "车牌", "保费合计", "送单地址", "联系电话", "保险公司" };
		String[] fieldName = { "policyId", "status", null, "finishedTime", null, "payWay", null, "insurant", "carNumber", "totlePremium", null, null, null };
		boolean b;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {

			b = ExcelHanderUtil.validOrder(fieldTitle, bis);
		} catch (Exception e) {
			logger.error("校验表头格式失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		if (!b) {
			throw new CommonLogicException(1, "非标准数据表头");
		}
		List<YiGaoExpress> list;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(excel.getBytes());) {
			list = ExcelHanderUtil.readInputStreamToEntity(bis, fieldName, YiGaoExpress.class, 1);

		} catch (Exception e) {
			logger.error("excel数据读取失败", e);
			throw new CommonLogicException(1, e.getMessage());
		}
		logger.info("Excel记录总数：{}", list.size());
		double expectAllMoney = 0d;
		List<YiGaoExpress> listUnFilter = new ArrayList<YiGaoExpress>();
		Set<String> setIds = new HashSet<>();
		for (YiGaoExpress yiGaoCheck : list) {
			
			if (ExpressStatus.FINISHED.equals(yiGaoCheck.getStatus())) {
				Date finishedTime = yiGaoCheck.getFinishedTime();
				if ((String.format("%tF", startDate)).equals(String.format("%tF", finishedTime))
						|| (String.format("%tF", endDate)).equals(String.format("%tF", finishedTime))) {
					if (StringUtils.isBlank(yiGaoCheck.getPolicyId())) {
						throw new CommonLogicException(1, "所选日期内发现id为空值，状态为完成的保单");
					}
					double realPremium = yiGaoCheck.getTotlePremium();
					if (PayWay.POS.equals(yiGaoCheck.getPayWay()) || PayWay.POS2.equals(yiGaoCheck.getPayWay())) {
						realPremium = realPremium * YiGaoExpress.posRate;
						posNum++;
					}
					expectAllMoney += realPremium;
					listUnFilter.add(yiGaoCheck);
					setIds.add(yiGaoCheck.getPolicyId());
					if (setIds.size() != listUnFilter.size()) {
						throw new CommonLogicException(1, "所选日期内发现两条重复保单" + yiGaoCheck.getPolicyId());
					}
				}
			}
		}
		BigDecimal bgd = new BigDecimal(expectAllMoney);
		expectAllMoney = bgd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		accumulatedError = expectAllMoney - totleMoney;
		// 刷卡手续费，此处先容许每笔有一分钱的误差
		if (Math.abs(accumulatedError) > 0.01 * posNum) {
			String errMsg = String.format("导入失败，此次账目总金额误差%s元", accumulatedError);
			logger.error(errMsg);
			throw new CommonLogicException(1, errMsg);
		}
		logger.debug("校验总金额通过,误差为{}元", accumulatedError);
		totleRow = listUnFilter.size();
		// 定义待更新的保单
		List<InsurancePolicy> upgradeList = new ArrayList<InsurancePolicy>();
		for (YiGaoExpress yiGaoCheck : listUnFilter) {
			// 核对易高表与系统金额
			InsurancePolicy insurancePolicy = policyDao.findOne(yiGaoCheck.getPolicyId());
			if (insurancePolicy == null) {
				String errMsg = String.format("导入失败，系统中找不到对应保单", yiGaoCheck.getPolicyId());
				logger.error(errMsg);
				throw new CommonLogicException(1, errMsg);
			}

			double expectPremium = insurancePolicy.getCarShipTax() + insurancePolicy.getSumPremiumCI() + insurancePolicy.getSumPremium();
			// 此处比较的是易高与系统中的保费
			if (Math.abs(expectPremium - yiGaoCheck.getTotlePremium()) > 0.01) {
				logger.error("易高保单收费金额与本地库不一致,policyid{}", yiGaoCheck.getPolicyId());
				throw new CommonLogicException(1, "保单收费金额与系统不一致，保单" + yiGaoCheck.getPolicyId());
			}

			if (insurancePolicy.getStatus() == PolicyStatus.DISPATCHED.value()) {
				changeRow++;
				insurancePolicy.setStatus(PolicyStatus.PAID.value());
				upgradeList.add(insurancePolicy);
				logger.debug("更新保单{}状态是收到保费", insurancePolicy.getOrderId());
			}

			if (totleRow != changeRow) {
				throw new CommonLogicException(1, "请核对保单" + yiGaoCheck.getPolicyId() + "数据，状态或金额错误");
			}

		}
		// 更新数据

		policyDao.save(upgradeList);
		logger.debug("更新系统数据状态为已收到保费,共更新{}条数据", changeRow);

		return totleRow;
	}

}

/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.BaojiaRecordSearchCondition;
import com.jt.core.ErrorCode;
import com.jt.core.UnderwriteFailedException;
import com.jt.core.dao.BaojiaRecordDao;
import com.jt.core.dao.BaojiaRecordDaoImpl;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.PolicyFeeDao;
import com.jt.core.insurance.BInsurance;
import com.jt.core.insurance.CInsurance;
import com.jt.core.insurance.ForceSubmitPolicyFormBean;
import com.jt.core.insurance.ICCode;
import com.jt.core.insurance.InquiryResult;
import com.jt.core.model.BaojiaRecord;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.Insurance;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.Merchant;
import com.jt.core.model.PolicyFee;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.utils.ActualValueCalculator;
import com.jt.utils.DateUtils;
import com.jt.utils.InsuranceOrderNoUtils;
import com.jt.utils.JSONSerializer;
import com.jt.ycl.oms.car.CarService;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.merchant.VehicleMerchantService;

/**
 * @author wuqh
 * 
 */
@Service
@Transactional
public class BaojiaRecordService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BaojiaRecordDaoImpl baojiaRecordDaoImpl;

	@Autowired
	private BaojiaRecordDao baojiaRecordDao;

	@Autowired
	private CarService carService;

	@Autowired
	private InsurancePolicyDao policyDao;

	@Autowired
	private PolicyFeeDao policyFeeDao;

	@Autowired
	private CityService cityService;

	@Autowired
	private VehicleMerchantService merchantService;

	public Map<String, Object> pageBaojiaRecords(BaojiaRecordSearchCondition condition) {
		// 总记录数
		int pageSize = condition.getPageSize();
		int totalItems = baojiaRecordDaoImpl.countBaojiaRecords(condition);
		// 总页数
		int totalPages = (totalItems + pageSize - 1) / pageSize;
		List<BaojiaRecord> baojiaRecords = baojiaRecordDaoImpl.pageBaojiaRecords(condition);
		Map<String, Object> map = new HashMap<>();
		map.put("totalItems", totalItems);
		map.put("totalPages", totalPages);
		map.put("baojiaRecords", baojiaRecords);
		return map;
	}

	/**
	 * 报看报价详情
	 * 
	 * @param carId
	 * @return
	 */
	public BaojiaRecord getBaojiaRecordByCardId(String carId) {
		BaojiaRecord baojiaRecord = baojiaRecordDao.findByCarId(carId);
		return baojiaRecord;
	}

	/**
	 * 更新处理结果
	 * 
	 * @param carId
	 * @param result
	 * @param feedback
	 */
	public void updateHandleResult(String carId, String result, int feedback) {
		String newResult = "";
		if (StringUtils.isNotEmpty(result)) {
			newResult = DateUtils.convertDateToStr(new Date(), "[yy-MM-dd HH:mm]") + ":" + result;
		}
		baojiaRecordDaoImpl.updateHandleResult(carId, newResult, feedback);
	}

	public BaojiaRecord getHandleResult(String carId) {
		List<Object> baojiaRecords = baojiaRecordDaoImpl.getHandleResult(carId);
		BaojiaRecord baojiaRecord = new BaojiaRecord();
		if (CollectionUtils.isNotEmpty(baojiaRecords)) {
			Object[] object = (Object[]) baojiaRecords.get(0);
			baojiaRecord.setResult((String) object[0]);
			baojiaRecord.setFeedback(Integer.parseInt(String.valueOf(object[1])));
		}
		return baojiaRecord;
	}

	/**
	 * 强行提交保单
	 */
	public InsurancePolicy forceSumbitPolicy(ForceSubmitPolicyFormBean bean) {
		if (StringUtils.isBlank(bean.getOwnerId())) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "参数错误，身份证号码必填！");
		}
		if (StringUtils.isBlank(bean.getCarId())) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "参数错误，车辆ID必填！");
		}
		if (StringUtils.isBlank(bean.getExpressAddress())) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "参数错误，保单配送地址必填！");
		}
		if (bean.getiCCode() == 0) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "参数错误，保险公司必填！");
		}
		if (StringUtils.isBlank(bean.getStartDate()) && StringUtils.isBlank(bean.getStartDateCI())) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "参数错误，商业险起保日期和交强险起保日期至少填写一项！");
		}
		if (StringUtils.isNotBlank(bean.getStartDate())) {
			Date startDate = DateUtils.convertStrToDate(bean.getStartDate(), "yyyy-MM-dd");// 商业险起保日期
			if (startDate == null) {
				throw new UnderwriteFailedException("商业险起保日期格式错误！");
			}
			if (startDate.before(new Date())) {
				throw new UnderwriteFailedException("商业险起保日期不能早于今天！");
			}
		}

		if (StringUtils.isNotBlank(bean.getStartDateCI())) {
			Date startDateCI = DateUtils.convertStrToDate(bean.getStartDateCI(), "yyyy-MM-dd");// 交强险起保日期
			if (startDateCI == null) {
				throw new UnderwriteFailedException("交强险起保日期格式错误！");
			}
			if (startDateCI.before(new Date())) {
				throw new UnderwriteFailedException("交强险起保日期不能早于今天！");
			}
		}
		Car car = carService.findCar(bean.getCarId());
		if (car == null) {
			throw new CommonLogicException(ErrorCode.CAR_NOT_FOUND, "车辆ID错误，车辆不存在");
		}
		Map<String, Object> map = ActualValueCalculator.calcActualValue(car.getEnrollDate(), car.getPurchasePrice(), car.getSeatCount());
		if (StringUtils.isBlank(bean.getOwner())) {
			bean.setOwner(car.getOwner());
		}
		if (StringUtils.isEmpty(car.getOwnerId()) || !StringUtils.equals(bean.getOwner(), car.getOwner())) {
			car.setOwner(bean.getOwner());
			car.setOwnerId(bean.getOwnerId());
			carService.save(car);
		}

		InquiryResult result = new InquiryResult();
		List<Insurance> insurances = new ArrayList<Insurance>();
		if (bean.getA() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(car.getPurchasePrice());
			insurance.setPremium(0f);
			insurance.setRiskKindCode("A");
			insurance.setRiskCode("0505");
			insurance.setName("机动车损失保险");
			insurances.add(insurance);
		}
		if (bean.getB() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(bean.getB() * 10000);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("B");
			insurance.setRiskCode("0505");
			insurance.setName("第三者责任保险");
			insurances.add(insurance);
		}
		if (bean.getG() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount((float) map.get("actualValue"));
			insurance.setPremium(0f);
			insurance.setRiskKindCode("G");
			insurance.setRiskCode("0505");
			insurance.setName("机动车盗抢保险");
			insurances.add(insurance);
		}
		if (bean.getD3() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(bean.getD3() * 10000);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("D3");
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任险司机座位");
			insurances.add(insurance);
		}
		if (bean.getD4() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(bean.getD4() * 10000);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("D4");
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任险乘客座位");
			insurances.add(insurance);
		}
		if (bean.getF() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("F");
			insurance.setRiskCode("0505");
			insurance.setName("玻璃单独破碎险");
			insurance.setGlassType(bean.getF());
			insurances.add(insurance);
		}
		if (bean.getL() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(bean.getL());
			insurance.setPremium(0f);
			insurance.setRiskKindCode("L");
			insurance.setRiskCode("0505");
			insurance.setName("划痕险");
			insurances.add(insurance);
		}
		if (bean.getZ() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount((float) map.get("actualValue"));
			insurance.setPremium(0f);
			insurance.setRiskKindCode("Z");
			insurance.setRiskCode("0505");
			insurance.setName("自燃损失险");
			insurances.add(insurance);
		}
		if (bean.getX1() > 0) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount((float) map.get("actualValue"));
			insurance.setPremium(0f);
			insurance.setRiskKindCode("X1");
			insurance.setRiskCode("0505");
			insurance.setName("发动机涉水险");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMA())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MA");
			insurance.setRiskCode("0505");
			insurance.setName("车损不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMB())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MB");
			insurance.setRiskCode("0505");
			insurance.setName("三者不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMG())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MG");
			insurance.setRiskCode("0505");
			insurance.setName("全车盗抢不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMD3())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MD3");
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任司机不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMD4())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MD4");
			insurance.setRiskCode("0505");
			insurance.setName("车上人员责任乘客不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getML())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("ML");
			insurance.setRiskCode("0505");
			insurance.setName("车身划痕不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMZ())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MZ");
			insurance.setRiskCode("0505");
			insurance.setName("自燃不计免赔");
			insurances.add(insurance);
		}
		if ("on".equals(bean.getMX1())) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("MX1");
			insurance.setRiskCode("0505");
			insurance.setName("涉水不计免赔");
			insurances.add(insurance);
		}
		if (bean.isCinsurance()) {
			Insurance insurance = new Insurance();
			insurance.setBuy(true);
			insurance.setAmount(0);
			insurance.setPremium(0f);
			insurance.setRiskKindCode("BZ");
			insurance.setRiskCode("0505");
			insurance.setName("交强险");
			insurances.add(insurance);
		}

		InsurancePolicy policy = policyDao.isDuplicatePolicy(car.getId());
		if (policy != null) {
			throw new CommonLogicException(ErrorCode.DUPLICATE_POLICY, "重复提交保单。");
		} else {
			policy = new InsurancePolicy();
		}
		PolicyFee policyFee = new PolicyFee();
		result.setInsuranceList(insurances);
		result.setCarNumber(car.getNumber());
		result.setCode(bean.getiCCode());
		result.setCompany(ICCode.getICNameByCode(bean.getiCCode()));
		result.setLastYearEndDate(bean.getStartDate());
		result.setLastYearCIEndDate(bean.getStartDateCI());
		String orderId = InsuranceOrderNoUtils.getOrderNo();
		Merchant merchant = null;
		if (StringUtils.isNotEmpty(car.getMerchantCode())) {// 填写商家推荐码标识该车属于某个商家
			merchant = merchantService.findMerchantById(car.getMerchantCode());
			if (merchant == null) {
				throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "系统错误，渠道编码错误：" + car.getMerchantCode());
			}
			if (!merchant.isChexian()) {
				throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "对不起，您的帐户车险业务被冻结，请致电400-151-0056联系客服！");
			}
			// 检索是否有虚拟保单，虚拟保单中必然会有car相关的信息了，可以根据carId和渠道编码去查询虚拟保单，虚拟保单不会有有content内容
			policy = policyDao.findVirtualPolicyByCarId(car.getId());
			if (policy != null) {
				orderId = policy.getOrderId();
				// 虚拟保单都有附件
				policy.setAttachmentFlag(true);
				policy.setStatus(PolicyStatus.NEED_UNDERWRITE.value());// 将虚拟保单的状态改为待核保
				logger.info("找到{}车牌号为{}的虚拟保单，将其转换为真实保单。", merchant.getName(), car.getNumber());
			} else {
				policy = new InsurancePolicy();// 如果没有虚拟保单，则生成一张真实保单
			}
			policy.setChannelCode(merchant.getId());
			policy.setChannelName(merchant.getName());
			policyFee.setReturnTarget(merchant.getName());
			policy.setChannelContact(merchant.getManager());
			policy.setSalesId(merchant.getSalesId());
			policy.setSalesMan(merchant.getSalesman());
			long count = policyDao.countByChannelCode(Long.parseLong(car.getMerchantCode()));
			if (count == 0) {// 检查该保单是否是商家提交的首单
				policy.setFirstOrder(true);
			}
		} else {
			policyFee.setReturnTarget(bean.getInsurant());
		}

		City city = cityService.getByCityCode(bean.getCityCode());

		if (bean.isBinsurance()) {
			Date startDate = DateUtils.convertStrToDate(bean.getStartDate(), "yyyy-MM-dd");// 商业险起保日期
			policy.setStartDate(startDate);
			Date endDate = DateUtils.calculateEndDate(startDate);
			policy.setEndDate(endDate);
			result.setbInsurance(new BInsurance());
		}
		if (bean.isCinsurance()) {
			Date startDateCI = DateUtils.convertStrToDate(bean.getStartDateCI(), "yyyy-MM-dd");// 交强险起保日期
			policy.setStartDateCI(startDateCI);
			Date endDateCI = DateUtils.calculateEndDate(startDateCI);
			policy.setEndDateCI(endDateCI);
			result.setcInsurance(new CInsurance());
		}
		policy.setCompanyCode(bean.getiCCode());
		policy.setOrderId(orderId);
		policy.setContent(JSONSerializer.serialize(result)); // 序列化成JSON串
		policy.setOwner(bean.getOwner());
		policy.setInsurant(bean.getInsurant());
		policy.setApplicant(bean.getApplicant());
		policy.setUserId(car.getUserId());
		policy.setOwnerId(bean.getOwnerId());
		policy.setInsurantId(bean.getInsurantId());
		policy.setApplicantId(bean.getApplicantId());
		policy.setCarId(car.getId());

		policy.setCarNumber(car.getNumber());
		policy.setModelDescr(car.getModelDescr());
		policy.setCityCode(car.getCityCode());
		policy.setCityName(city.getName());
		policy.setRegion(bean.getRegion());
		policy.setPhone(Long.parseLong(bean.getPhone()));

		policy.setCustomerservice(bean.getCustomerservice());
		policy.setCreateDate(new Date());
		policy.setUpdateTime(new Date());
		policy.setVirtual(false);
		policyDao.save(policy);

		policyFee.setPolicyId(policy.getOrderId());
		
		if(merchant != null) {
			//渠道提交的保单，收件人默认是商家老板
			policyFee.setRecipients(merchant.getManager());
		} else {
			//个人用户，收件人和联系电话，默认是被保险人、被保险人电话
			policyFee.setRecipients(bean.getInsurant());
		}
		policyFee.setRecipientsPhone(bean.getPhone());
		policyFee.setExpressAddress(city.getProvince() + city.getName() + bean.getRegion() + bean.getExpressAddress());
		policyFeeDao.save(policyFee);
		return policy;
	}

}

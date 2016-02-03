package com.jt.ycl.oms.sales;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.ErrorCode;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.CommentsDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.InsurancePolicyDaoImpl;
import com.jt.core.dao.OmsUserDao;
import com.jt.core.insurance.ICCode;
import com.jt.core.insurance.InquiryResult;
import com.jt.core.insurance.StandardRiskKindCode;
import com.jt.core.model.Comments;
import com.jt.core.model.Insurance;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.OmsUser;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.insurance.PolicyService;
import com.jt.ycl.oms.sales.bean.Price;

@Service
@Transactional
public class MyPolicyService {

	@Autowired
	private InsurancePolicyDao policyDao;
	
	@Autowired
	private InsurancePolicyDaoImpl insurancePolicyDaoImpl;
	
	@Autowired
	private CommentsDao commentsDao;
	
	@Autowired
	private OmsUserDao omsUserDao;
	
	@Autowired
	private CarDao carDao;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private PolicyService policyService;
	
	public Map<Integer, Long> countMyPolicyByStatus(String salesId, Long channelCode) {
		Map<Integer, Long> resultMap = new HashMap<Integer, Long>();
		List<Object[]> resultList = null;
		if(channelCode > 0) {
			resultList = policyDao.countStatusByChannelCode(channelCode);
		} else {
			resultList = policyDao.countStatusBySalesId(salesId);
		}
		for(Object[] result : resultList) {
			int status = (int) result[0];
			Long nums = (Long) result[1];
			resultMap.put(status,  nums);
		}
		return resultMap;
	}

	public Map<Integer, Long> countMyPolicyByFlag(String salesId, Long channelCode) {
		Map<Integer, Long> resultMap = new HashMap<Integer, Long>();
		List<Object[]> resultList = null;
		if(channelCode > 0) {
			resultList = policyDao.countFlagByChannelCode(channelCode);
		} else {
			resultList = policyDao.countFlagBySalesId(salesId);
		}
		for(Object[] result : resultList) {
			int status = (int) result[0];
			long nums = (long) result[1];
			resultMap.put(status,  nums);
		}
		return resultMap;
	}

	public Comments addComments(String policyId, String salesName, String content) {
		Comments comments = new Comments();
		comments.setContent(content);
		comments.setCreateDate(new Date());
		comments.setName(salesName);
		comments.setPolicyId(policyId);
		
		commentsDao.save(comments);

		return comments;
	}

	public List<Price> getPolicyPriceDetail(String policyId) throws Exception {
		InsurancePolicy policy = policyDao.findOne(policyId);
		if(policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "保单不存在，请刷新页面重试.");
		}
		String content = policy.getContent();
		List<Price> priceList = new ArrayList<>();
		if(content == null) {
			return priceList;
		}
		InquiryResult inquiryResult = mapper.readValue(content, InquiryResult.class);
		Price bzPrice = new Price(StandardRiskKindCode.BZ, inquiryResult.getcInsurance().getSumPremium());
		Price tax = new Price("TAX", inquiryResult.getCarShipTaxInfo().getCarShipTax());
		Price BI_TOTAL = new Price("BI_TOTAL", inquiryResult.getbInsurance().getSumPremium());
		Price TOTAL = new Price("TOTAL", policy.getTotalPremium());
		priceList.add(bzPrice);
		priceList.add(tax);
		priceList.add(BI_TOTAL);
		priceList.add(TOTAL);
		
		BigDecimal temp = new BigDecimal(inquiryResult.getbInsurance().getSumPremium());
		List<Insurance> insurances = inquiryResult.getInsuranceList();
		for(Insurance insurance : insurances) {
			if(StandardRiskKindCode.A.equals(insurance.getRiskKindCode()) || 
			   StandardRiskKindCode.B.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.G.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.D3.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.D4.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.F.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.L.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.X1.equals(insurance.getRiskKindCode()) ||
			   StandardRiskKindCode.Z.equals(insurance.getRiskKindCode())
			) {
				priceList.add(new Price(insurance.getRiskKindCode(), insurance.getPremium()));
				temp = temp.subtract(new BigDecimal(insurance.getPremium()));
			}
		}
		Price bjmPrice = new Price("BJMP", temp.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
		priceList.add(bjmPrice);
		return priceList;
	}
	

	public void doAction(String policyId, String salesName, int type) {
		InsurancePolicy policy = policyDao.findOne(policyId);
		if(policy == null) {
			throw new CommonLogicException(ErrorCode.POLICY_NOT_FOUND, "保单不存在，请刷新页面重试."); 
		}
		if(type == 1) {//删单
			policyService.deletePolicy(policyId, salesName);
		} else if(type == 2) {//解挂
			policyService.resume(policy, salesName);
		} else if(type == 3) {//同意出单
			policy = policyService.changePolicyStatus(policy, PolicyStatus.CHUDAN_AGREE.value(), null, salesName);
			if(policy == null) {
				throw new CommonLogicException(ErrorCode.ILLEGAL_ACCESS, "非法操作，当前状态下的保单不允许执行该操作！");
			}
		} else {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ACCESS, "参数错误，只能是1,2,3");
		}
	}

	/**
	 * @param salesId			销售经理ID
	 * @param channelCode		渠道编码
	 * @param byStatus			true按保单状态查询，false按保单标记查询
	 * @param value				状态值or标记值
	 * @param page				翻页，页码
	 */
	public List<PolicyItem> listByStatusOrFlag(final String salesId, final Long channelCode, final boolean byStatus, final int value, int page) {
		PageRequest pageRequest = new PageRequest(page, 10);
		Page<InsurancePolicy> currentPage = policyDao.findAll(new Specification<InsurancePolicy>() {
			@Override
			public Predicate toPredicate(Root<InsurancePolicy> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(channelCode > 0) {
					predicateList.add(cb.equal(root.get("channelCode"), channelCode));	
				} else {
					predicateList.add(cb.equal(root.get("salesId"), salesId));
				}
				if(byStatus) {
					predicateList.add(cb.equal(root.get("status"), value));
				} else {
					predicateList.add(cb.equal(root.get("flag"), value));
				}
				predicateList.add(cb.equal(root.get("policyType"), 1));
				
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, pageRequest);
		
		List<InsurancePolicy> policyList = currentPage.getContent();
		List<PolicyItem> itemList = new ArrayList<>(policyList.size());
		for(InsurancePolicy policy : policyList) {
			PolicyItem item = new PolicyItem();
			
			if(policy.getStartDate() == null) {
				item.setBiDays("未知");
			} else {
				Date now = new Date();
				int diff = DateUtils.difference(now, policy.getStartDate());
				if(diff < 0) {
					item.setBiDays("已脱保");
				} else {
					item.setBiDays(diff + "天");
				}
			}
			
			if(policy.getStartDateCI() == null) {
				item.setCiDays("未知");
			} else {
				Date now = new Date();
				int diff = DateUtils.difference(now, policy.getStartDateCI());
				if(diff < 0) {
					item.setCiDays("已脱保");
				} else {
					item.setCiDays(diff + "天");
				}
			}
			item.setPolicyId(policy.getOrderId());
			item.setCarOwner(policy.getOwner());
			item.setChannelContactor(policy.getChannelContact());
			item.setChannelName(policy.getChannelName());
			item.setCreateDate(DateUtils.convertDateToStr(policy.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
			String icCompany = ICCode.getICNameByCode(policy.getCompanyCode());
			if(icCompany == null) {
				icCompany = "未知";
			}
			item.setIcCompany(icCompany);
			
			int customerService = policy.getCustomerservice();
			OmsUser omsUser = omsUserDao.findByEmployeeID(String.valueOf(customerService));
			if(omsUser != null) {
				item.setCustomerService(omsUser.getName());
			} else {
				item.setCustomerService("");
			}
			item.setIccode(policy.getCompanyCode());
			item.setLicenseNo(policy.getCarNumber());
			item.setTelephone(String.valueOf(policy.getPhone()));
			item.setTotalPremium(policy.getTotalPremium());
			item.setComments(policy.getComments2());
			itemList.add(item);
		}
		return itemList;
	}
	
	/**
	 * 统计销售人员业绩
	 * @param cityCode
	 * @param page
	 * @param pageSize
	 * @param startDate
	 * @param endDate
	 * @param salesId
	 * @return
	 */
	public List<BoardItem> staticsSalesPolicy(int cityCode, int page, int pageSize, String startDate, String endDate, String salesId){
		List<BoardItem> boardItems = new ArrayList<BoardItem>();
		List<Object[]> results = insurancePolicyDaoImpl.staticsSalesPolicy(cityCode, page, pageSize, startDate, endDate, salesId);
		if(CollectionUtils.isNotEmpty(results)){
			for(Object[] objects : results){
				BoardItem item = new BoardItem();
				if(objects[0] != null){
					item.setName(objects[0].toString());
				}
				if(objects[1] != null){
					item.setPcm(Integer.parseInt(objects[1].toString()));
				}
				if(objects[2] != null){
					item.setPmPolicyFee(Double.parseDouble(objects[2].toString()));
				}
				if(objects[3] != null){
					item.setTotal(Integer.parseInt(objects[3].toString()));
				}
				if(objects[4] != null){
					item.setTotalPolicyFee(Double.parseDouble(objects[4].toString()));
				}
				boardItems.add(item);
			}
		}
		return boardItems;
	}

	public List<PolicyItem> searchPolicyByLicenseNo(String licenseNo, String salesId) {
		List<InsurancePolicy> policyList = policyDao.findByCarNumber(licenseNo);
		List<PolicyItem> itemList = new ArrayList<>(policyList.size());
		for(InsurancePolicy policy : policyList) {
			if(!salesId.equals(policy.getSalesId())) {
				continue;
			}
			PolicyItem item = new PolicyItem();
			
			if(policy.getStartDate() == null) {
				item.setBiDays("未知");
			} else {
				Date now = new Date();
				int diff = DateUtils.difference(now, policy.getStartDate());
				if(diff < 0) {
					item.setBiDays("已脱保");
				} else {
					item.setBiDays(diff + "天");
				}
			}
			
			if(policy.getStartDateCI() == null) {
				item.setCiDays("未知");
			} else {
				Date now = new Date();
				int diff = DateUtils.difference(now, policy.getStartDateCI());
				if(diff < 0) {
					item.setCiDays("已脱保");
				} else {
					item.setCiDays(diff + "天");
				}
			}
			
			item.setPolicyId(policy.getOrderId());
			item.setCarOwner(policy.getOwner());
			item.setChannelContactor(policy.getChannelContact());
			item.setChannelName(policy.getChannelName());
			item.setCreateDate(DateUtils.convertDateToStr(policy.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
			String icCompany = ICCode.getICNameByCode(policy.getCompanyCode());
			if(icCompany == null) {
				icCompany = "未知";
			}
			item.setIcCompany(icCompany);
			
			int customerService = policy.getCustomerservice();
			OmsUser omsUser = omsUserDao.findByEmployeeID(String.valueOf(customerService));
			if(omsUser != null) {
				item.setCustomerService(omsUser.getName());
			} else {
				item.setCustomerService("");
			}
			item.setIccode(policy.getCompanyCode());
			item.setLicenseNo(policy.getCarNumber());
			item.setTelephone(String.valueOf(policy.getPhone()));
			item.setTotalPremium(policy.getTotalPremium());
			item.setComments(policy.getComments2());
			itemList.add(item);
		}
		return itemList;
	}
}
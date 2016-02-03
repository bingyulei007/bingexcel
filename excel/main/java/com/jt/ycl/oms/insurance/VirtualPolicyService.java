/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.net.URLDecoder;
import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.jt.core.ErrorCode;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.PolicyFeeDao;
import com.jt.core.model.Car;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.Merchant;
import com.jt.core.model.PolicyFee;
import com.jt.core.model.PolicyStatus;
import com.jt.core.model.VehicleLicense;
import com.jt.exception.CommonLogicException;
import com.jt.ycl.oms.car.CarService;

/**
 * @author wuqh
 */
@Service
@Transactional
public class VirtualPolicyService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private InsurancePolicyDao insurancePolicyDao;
	
	@Autowired
	private PolicyFeeDao policyFeeDao;
	
	@Autowired
	private MerchantDao merchantDao;
	
	@Autowired
	private CarService carService;
	
	private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";

	/**
	 * 创建一条客服通过新增报价请求生成的虚拟保单
	 */
	public void createVirtualPolicy(String policyId, long merchantId, String fangan, String iccode) throws Exception {
		Merchant merchant = merchantDao.findOne(merchantId);
		if(merchant == null) {
			throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "渠道商家不存在：" + merchantId);
		}
		fangan = URLDecoder.decode(fangan, "UTF-8");
		InsurancePolicy insurancePolicy = new InsurancePolicy();
		insurancePolicy.setCityCode(merchant.getCityCode());
		insurancePolicy.setOrderId(policyId);
		insurancePolicy.setChannelCode(merchantId);
		insurancePolicy.setBaojiaPackage(fangan);
		insurancePolicy.setChannelName(merchant.getName());
		insurancePolicy.setChannelContact(merchant.getManager());
		if(StringUtils.isNotEmpty(merchant.getManagerPhone())){
			insurancePolicy.setPhone(Long.parseLong(merchant.getManagerPhone()));
		}
		insurancePolicy.setRequestTime(new Date());
		insurancePolicy.setCreateDate(new Date());
		insurancePolicy.setIccode(iccode);
		insurancePolicy.setSalesId(merchant.getSalesId());
		insurancePolicy.setSalesMan(merchant.getSalesman());
		insurancePolicy.setPriority(1);
		insurancePolicy.setStatus(PolicyStatus.NO_QUERY_BAOJIA.value()); //待询价
		insurancePolicyDao.save(insurancePolicy);
	}
	
	public InsurancePolicy findById(String policyId){
		return insurancePolicyDao.findOne(policyId);
	}
	
	public Car findCarByCarNumberAndMerchantId(String number, String merchantCode){
		return carService.findCarByCarNumberAndMerchantId(number, merchantCode);
	}
	
	/**
	 * 添加商家超级上传的车辆
	 * @param car
	 * @return
	 */
	public Car addCar(Car car, String orderId, int customerservice){
		Car createCar =  carService.addCar(car, car.getMerchantCode());
		//将车辆相关信息更新到虚拟保单中
		InsurancePolicy insurancePolicy = insurancePolicyDao.findOne(orderId);
		if(insurancePolicy != null){
			insurancePolicy.setOwner(createCar.getOwner());
			insurancePolicy.setOwnerId(createCar.getOwnerId());
			insurancePolicy.setCarNumber(createCar.getNumber());
			insurancePolicy.setCarId(createCar.getId());
			insurancePolicy.setCustomerservice(customerservice);
			insurancePolicy.setCityCode(createCar.getCityCode());
			insurancePolicy.setUpdateTime(new Date());
			updateVirtualPolicy(insurancePolicy);
		}
		return createCar;
	}
	
	public InsurancePolicy isDuplicateVirtualPolicy(String carId) {
		return insurancePolicyDao.findVirtualPolicyByCarId(carId);
	}

	public void updateVirtualPolicy(String orderId, String baojiaPackage) {
		insurancePolicyDao.updateBaoJiaFangan(orderId, baojiaPackage);
	}
	
	public void updateVirtualPolicy(InsurancePolicy insurancePolicy){
		insurancePolicyDao.save(insurancePolicy);
	}

	//删除重复的虚拟保单
	public void deleteVirtualPolicy(String orderId) {
		InsurancePolicy policy = insurancePolicyDao.findOne(orderId);
		insurancePolicyDao.delete(orderId);
		logger.info("删除商家[{}]请求", policy.getChannelName());
		OSSClient client = null;
		try {
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
        	if(client.doesBucketExist(orderId)){
        		client.deleteBucket(orderId);
        		logger.info("删除商家报价请求上传的行驶证图片及身份证图片: {}", policy.getChannelName());
        	}
		}finally {
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}

	/**
	 * 将指定的保单迁移到目标渠道明下
	 * 
	 * @param orderId			保单ID
	 * @param targetChannelCode	目标渠道编码
	 */
	public void transferToChannel(String orderId, String targetChannelCode) {
		Merchant merchant = merchantDao.findOne(Long.parseLong(targetChannelCode));
		if(merchant == null){
			throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "渠道商编码错误：" + targetChannelCode);
		}
		String managerPhone = merchant.getManagerPhone();
		if(StringUtils.isBlank(managerPhone) || StringUtils.isBlank(merchant.getAddress())) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "目标渠道联系电话缺失，请先完善资料");
		}
		long phone = Long.parseLong(managerPhone);
		PolicyFee fee = policyFeeDao.findByPolicyId(orderId);
		if(fee == null) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "保单无配送信息");
		}
		fee.setExpressAddress(merchant.getAddress());
		fee.setRecipients(merchant.getManager());
		fee.setRecipientsPhone(managerPhone);
		insurancePolicyDao.transferPolicy(orderId, merchant.getId(), merchant.getName(), merchant.getManager(), phone);
		policyFeeDao.save(fee);
		logger.info("保单迁移完成，新的商户是{}，配送地址是{}", merchant.getName(), merchant.getAddress());
	}
	
	public VehicleLicense getVehicleLicenseByNumber(String carNumber){
		return carService.getVehicleLicenseByNumber(carNumber);
	}
	
	public int countNoHandleRequestCount(){
		return insurancePolicyDao.countNoHandleRequestCount();
	}
}

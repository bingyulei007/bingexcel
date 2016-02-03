package com.jt.ycl.oms.merchant;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.jt.core.ErrorCode;
import com.jt.core.dao.MerchantBaoJiaRequestDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.model.Car;
import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantBaoJiaRequest;
import com.jt.core.model.VehicleLicense;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.car.CarService;

/**
 * @author wuqh
 *
 */
@Service
@Transactional
public class MerchantBaoJiaRequestService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private MerchantBaoJiaRequestDao merchantBaoJiaRequestDao;
	
	@Autowired
	private CarService carService;
	
	@Autowired
	private MerchantDao merchantDao;
	
	private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private String bucketName = "auditimg";
	
	/**
	 * 查询商家提交的报价请求记录
	 */
	public Page<MerchantBaoJiaRequest> query(final String carNumber,final int state, final int policyState, final String salesman, 
			final String customerService, final String merchantName, final String startDate, final String endDate,
			int pageNumber, int pageSize){
		Page<MerchantBaoJiaRequest> pageResult = merchantBaoJiaRequestDao.findAll(new Specification<MerchantBaoJiaRequest>() {
			@Override
			public Predicate toPredicate(Root<MerchantBaoJiaRequest> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(state>0){
					predicateList.add(cb.equal(root.get("state"), state));
				}else{
					predicateList.add(cb.notEqual(root.get("state"), 4)); //默认不显示关闭的报价请求
				}
				if(policyState>-1){
					predicateList.add(cb.equal(root.get("policyStatus"), policyState));
				}
				if(StringUtils.isNotEmpty(carNumber)&& !"".equals(carNumber)) { //根据车牌号查询
					Path<String> carNumberPath = root.get("carNumber");
					Predicate p = cb.like(carNumberPath, "%"+carNumber+"%");
					predicateList.add(p);
				}
				if(!"0".equals(salesman)){
					predicateList.add(cb.equal(root.get("salesman"), salesman));
				}
				if(!"0".equals(customerService)){
					predicateList.add(cb.equal(root.get("customerService"), customerService));
				}
				if(StringUtils.isNotEmpty(merchantName)&& !"".equals(merchantName)) { //根据车牌号查询
					Path<String> merchantNamePath = root.get("merchantName");
					Predicate p = cb.like(merchantNamePath, "%"+merchantName+"%");
					predicateList.add(p);
				}
				if(StringUtils.isNotEmpty(startDate) && StringUtils.isEmpty(endDate)){
					Path<Date> requestTimePath = root.get("requestTime");
					Date date1 = DateUtils.convertStrToDate(startDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
					Date date2 = DateUtils.convertStrToDate(startDate+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
					predicateList.add(cb.greaterThanOrEqualTo(requestTimePath, date1));
					predicateList.add(cb.lessThanOrEqualTo(requestTimePath, date2));
				}
				if(StringUtils.isNotEmpty(startDate) && StringUtils.isNotEmpty(endDate)){
					Path<Date> requestTimePath = root.get("requestTime");
					Date date1 = DateUtils.convertStrToDate(startDate+" 00:00:00", "yyyy-MM-dd HH:mm:ss");
					Date date2 = DateUtils.convertStrToDate(endDate+" 23:59:59", "yyyy-MM-dd HH:mm:ss");
					predicateList.add(cb.greaterThanOrEqualTo(requestTimePath, date1));
					predicateList.add(cb.lessThanOrEqualTo(requestTimePath, date2));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.ASC,null));
		return pageResult;
	}
	
	/**
     * 创建分页请求和排序.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "requestTime");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

	public MerchantBaoJiaRequest getMerchantBaoJiaRequestById(int requestId) {
		return merchantBaoJiaRequestDao.findOne(requestId);
	}
	
	/**
	 * 添加商家超级上传的车辆
	 * @param car
	 * @return
	 */
	public Car addCar(Car car, int requestId){
		Car createCar =  carService.addCar(car, car.getMerchantCode());
		//将车牌号更新到请求记录信息中, 并将状态改为2：已分配
		merchantBaoJiaRequestDao.updateMerchantBaoJiaRequest(car.getNumber(), createCar.getId(), requestId, 2);
		return createCar;
	}

	/**
	 * 更新商家报价请求状态
	 * @param requestId
	 * @param state
	 */
	public void updateStateById(int requestId, String handler, int state) {
		merchantBaoJiaRequestDao.updateState(requestId, handler, state);
	}

	public void updateMerchantBaoJiaRequest(String number, String carId, int requestId, int state){
		merchantBaoJiaRequestDao.updateMerchantBaoJiaRequest(number, carId, requestId, state);
	}
	
	public VehicleLicense getVehicleLicenseByNumber(String carNumber){
		return carService.getVehicleLicenseByNumber(carNumber);
	}
	
	public Car findCarByCarNumberAndMerchantId(String number, String merchantCode){
		return carService.findCarByCarNumberAndMerchantId(number, merchantCode);
	}

	/**
	 * 删除商家的报价请求
	 * @param requestId
	 */
	public void deleteBaoJiaRequest(int requestId) {
		MerchantBaoJiaRequest request = merchantBaoJiaRequestDao.getOne(requestId);
		merchantBaoJiaRequestDao.delete(requestId);
		logger.info("删除商家[{}]请求", request.getMerchantName());
		OSSClient client = null;
		try {
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
			//http://auditimg.oss-cn-hangzhou.aliyuncs.com/
        	if(client.doesObjectExist(bucketName, request.getLicenseImageUrl().substring(45))){
        		client.deleteObject(bucketName, request.getLicenseImageUrl().substring(45));
        		logger.info("删除商家报价请求上传的行驶证图片: {}", request.getMerchantName());
        	}
        	if(client.doesObjectExist(bucketName, request.getIdImageUrl().substring(45))){
        		client.deleteObject(bucketName, request.getIdImageUrl().substring(45));
        		logger.info("删除商家报价请求上传的身份证图片: {}", request.getMerchantName());
        	}
		}finally {
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}

	public List<MerchantBaoJiaRequest> findByCarId(String carId) {
		return merchantBaoJiaRequestDao.findByCarId(carId);
	}
	
	public int countNoHandleRequestCount(){
		return merchantBaoJiaRequestDao.countNoHandleRequestCount();
	}

	/**
	 * 保存商户报价请求 
	 */
	public void saveMerchantUploadRecord(long merchantId, String fangan, String iccode, String vehicleLicenseUrl, String idCardUrl) throws Exception {
		Merchant merchant = merchantDao.findOne(merchantId);
		if(merchant == null) {
			throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "Merchant不存在：" + merchantId);
		}
		fangan = URLDecoder.decode(fangan, "UTF-8");
		MerchantBaoJiaRequest record = new MerchantBaoJiaRequest();
		record.setMerchantId((int) merchantId);
		record.setBaojiaPackage(fangan);
		record.setIdImageUrl(idCardUrl);
		record.setLicenseImageUrl(vehicleLicenseUrl);
		record.setManager(merchant.getManager());
		record.setMerchantName(merchant.getName());
		record.setPhone(merchant.getManagerPhone());
		record.setRequestTime(new Date());
		record.setIccode(iccode);
		record.setSalesman(merchant.getSalesman());
		record.setPriority(1);
		merchantBaoJiaRequestDao.save(record);
	}

	public MerchantBaoJiaRequest judgeDuplicateRequest(int merchantId, String carId) {
		return merchantBaoJiaRequestDao.judgeDuplicateRequest(merchantId,carId);
	}

	public void updatePriority(int requestId, int priority) {
		merchantBaoJiaRequestDao.updatePriority(requestId, priority);
	}
	
	public void updateFlag(int requestId, int flag) {
		merchantBaoJiaRequestDao.updateFlag(requestId, flag);
	}

	public void assign(int requestId, String customerService, String remark) {
		merchantBaoJiaRequestDao.assign(requestId, customerService, remark);
	}

	public MerchantBaoJiaRequest findByRequestId(int requestId) {
		return merchantBaoJiaRequestDao.findOne(requestId);
	}

	public void updateBaoJiaFangan(int requestId, String baojiaPackage) {
		merchantBaoJiaRequestDao.updateBaoJiaFangan(requestId, baojiaPackage);
	}
}

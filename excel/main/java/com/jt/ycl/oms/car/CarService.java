package com.jt.ycl.oms.car;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.jt.core.ErrorCode;
import com.jt.core.dao.BaojiaRecordDao;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.CarDaoImpl;
import com.jt.core.dao.ClaimHistoryDao;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.UserDao;
import com.jt.core.dao.VehicleConfigModelDao;
import com.jt.core.dao.VehicleLicenseDao;
import com.jt.core.model.Car;
import com.jt.core.model.City;
import com.jt.core.model.ClaimHistory;
import com.jt.core.model.Merchant;
import com.jt.core.model.User;
import com.jt.core.model.VehicleConfigModel;
import com.jt.core.model.VehicleLicense;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.city.CityService;

@Service
@Transactional
public class CarService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private OSSClient client = new OSSClient("http://oss-cn-hangzhou.aliyuncs.com", "qlo4BoLGXaAXU7FA", "wHT8XCsYTnaj7utm5L0t1f1owmwSTy");
	
	@Autowired
	private CarDao carDao;
	
	@Autowired
	private BaojiaRecordDao baojiaRecordDao;
	
	@Autowired
	private CarDaoImpl carDaoImpl;
	
	@Autowired
	private VehicleConfigModelDao vehicleConfigModelDao;
	
	@Autowired
	private ClaimHistoryDao claimHistoryDao;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private MerchantDao merchantDao;

	@Autowired
	private VehicleLicenseDao vehicleLicenseDao;
	
	@Autowired
	private InsurancePolicyDao insurancePolicyDao;
	
	public Car updateCar(String carId, String owner, String number, String vin, String engineNo, String modelName, String registerDate,
			String userId, boolean guohu, String makeDate, boolean companyCar){
		Car car = carDao.findOne(carId);
		if(car == null)
			return null;
		car.setNumber(number);
		car.setOwner(owner);
		car.setVin(vin);
		car.setEngineNo(engineNo);
		car.setModelName(modelName);
		car.setEnrollDate(DateUtils.convertStrToDate(registerDate, "yyyy-MM-dd"));
		car.setUserId(userId);
		car.setGuohu(guohu);
		if(guohu){
			car.setMakeDate(DateUtils.convertStrToDate(makeDate, "yyyy-MM-dd"));
		}
		car.setCompanyCar(companyCar);
		return carDao.save(car);
	}
	
	/**
	 * 编辑车辆时调用，因为添加车辆时，必然已经保存了相关的配置型号，所以编辑时只需要直接从数据库查询出来即可。添加车辆时，数据库中可能不存在，不能调用该方法
	 * @param modelName
	 * @return
	 */
	public List<VehicleConfigModel> findVehicleConfigModels(String modelName){
		return vehicleConfigModelDao.findByModelNameLike("%" + modelName + "%");
	}
	
	public Car findCar(String carId){
		return carDao.findOne(carId);
	}
	
	public Car findCarByCarNumberAndMerchantId(String number, String merchantCode){
		return carDao.findByNumberAndMerchantCode(number, merchantCode);
	}
	
	/**
	 * @param endMonth	根据车险到期月查询车辆
	 */
	public Object[] findByMonth(String vehicleNumber, int cityCode, int month, int pageNumber, int pageSize) {
		int offset = (pageNumber) * pageSize;
		long totalItems = carDaoImpl.countCarInMonth(vehicleNumber, cityCode, month, "");
		int totalPages = (int) (totalItems / 20) +  (totalItems % 20 == 0 ? 0 : 1);
		
		List<Car> carList = carDaoImpl.findCarInMonth(vehicleNumber, cityCode, month, "", offset, pageSize);
		Object[] resultObjects = new Object[3];
		resultObjects[0] = totalItems;
		resultObjects[1] = totalPages;
		resultObjects[2] = carList;
		return resultObjects;
	}
	
    /**
     * 删除车辆
     */
	public void deleteVehicle(String carId) {
		int nums = insurancePolicyDao.countByCarId(carId);
		if(nums > 0) {
			throw new CommonLogicException(ErrorCode.ILLEGAL_ACCESS, "车辆存在关联的保单，不能删除保单！");
		}
		carDao.delete(carId);
		baojiaRecordDao.deleteRecordByCarId(carId);
	}
	
	/**
	 * 将车辆添加到指定的商户名下
	 */
	public Car addCar(Car car, String merchantId) {
		Merchant merchant = merchantDao.findOne(Long.parseLong(merchantId));
		if(merchant == null) {
			throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "商户不存在：" + merchantId);
		}
		User user = userDao.findByPhone(merchant.getManagerPhone());
		if(user == null) {
			user = new User();
			user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
			user.setUsername(merchant.getManagerPhone());
			user.setPhone(merchant.getManagerPhone());
			user.setMerchantCode(String.valueOf(merchant.getId()));
			user.setCreateDate(new Date());
			user.setLastLoginDate(user.getCreateDate());
			userDao.save(user);
		}
		
		VehicleConfigModel cfgModel = vehicleConfigModelDao.findOne(car.getVehicleModelId());
		String brand = cfgModel.getBrand();
		car.setBrand(brand);
		
		car.setModelName(cfgModel.getModelName());
		car.setModelCode(cfgModel.getModelCode());
		car.setSeatCount(cfgModel.getSeatCount());
		car.setExhaustScale(cfgModel.getExhaustScale());
		car.setPurchasePrice(cfgModel.getPrice());
		car.setModelDescr(cfgModel.getDescription());
		
		//根据车牌号前缀来判断车辆是否是外地车
		boolean ecdemicCar = true;
		City city = cityService.getByCityCode(car.getCityCode());
		if(StringUtils.isBlank(city.getLicensePrefix())) {
			throw new CommonLogicException(ErrorCode.CITY_NOT_FOUND, "城市 " + car.getCityCode() + "没有配置车牌号前缀.");
		}
		String[] prefixList = StringUtils.split(city.getLicensePrefix(), "#");
		for(String prefix : prefixList) {
			if(car.getNumber().startsWith(prefix)) {
				ecdemicCar = false;
				break;
			}
		}
		car.setEcdemicVehicleFlag(ecdemicCar ? 1 : 0);// 1是，0否
		if(StringUtils.isBlank(brand)) {
			car.setImg(getImg(cfgModel.getModelName()));
		} else {
			car.setImg(getImg(brand));
		}
		car.setUserId(user.getId());
		car.setPhone(user.getPhone());
		
		if (logger.isDebugEnabled()) {
			logger.debug("添加新车辆：{}, 型号：{}", car.getNumber(), cfgModel.getDescription());
		}
		return carDao.save(car);
	}
	
	/**
	 * 修改车辆车辆
	 */
	public Car update(Car car) {
		if(car.getVehicleModelId() != 0) {
			VehicleConfigModel cfgModel = vehicleConfigModelDao.findOne(car.getVehicleModelId());
			String brand = cfgModel.getBrand();
			car.setBrand(brand);
			
			car.setModelName(cfgModel.getModelName());
			car.setModelCode(cfgModel.getModelCode());
			car.setSeatCount(cfgModel.getSeatCount());
			car.setExhaustScale(cfgModel.getExhaustScale());
			car.setPurchasePrice(cfgModel.getPrice());
			car.setModelDescr(cfgModel.getDescription());
			
			//根据车牌号前缀来判断车辆是否是外地车
			boolean ecdemicCar = true;
			City city = cityService.getByCityCode(car.getCityCode());
			if(StringUtils.isBlank(city.getLicensePrefix())) {
				throw new CommonLogicException(ErrorCode.CITY_NOT_FOUND, "城市 " + car.getCityCode() + "没有配置车牌号前缀.");
			}
			String[] prefixList = StringUtils.split(city.getLicensePrefix(), "#");
			for(String prefix : prefixList) {
				if(car.getNumber().startsWith(prefix)) {
					ecdemicCar = false;
					break;
				}
			}
			car.setEcdemicVehicleFlag(ecdemicCar ? 1 : 0);// 1是，0否
			if(StringUtils.isBlank(brand)) {
				car.setImg(getImg(cfgModel.getModelName()));
			} else {
				car.setImg(getImg(brand));
			}
		}
		if(StringUtils.isNotEmpty(car.getMerchantCode())) {
			Merchant merchant = merchantDao.findOne(Long.parseLong(car.getMerchantCode()));
			if(merchant == null) {
				throw new CommonLogicException(ErrorCode.MERCHANT_NOT_FOUND, "商户不存在：" + car.getMerchantCode());
			}
			User user = userDao.findByPhone(merchant.getManagerPhone());
			if(user == null) {
				user = new User();
				user.setId(UUID.randomUUID().toString().replaceAll("-", ""));
				user.setUsername(merchant.getManagerPhone());
				user.setPhone(merchant.getManagerPhone());
				user.setMerchantCode(String.valueOf(merchant.getId()));
				user.setCreateDate(new Date());
				user.setLastLoginDate(user.getCreateDate());
				userDao.save(user);
			}
			car.setUserId(user.getId());
		}
		return carDao.save(car);
	}
	
	private String getImg(String brand) {
		if(StringUtils.isBlank(brand)) {
			return "";
		}
		String img = "";
		if(brand.contains("DS")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/ds.jpg";
		} else if(brand.contains("MINI")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/mini.jpg";
		} else if(brand.contains("Smart")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/smart.jpg";
		} else if(brand.contains("奥迪")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/audi.jpg";
		} else if(brand.contains("宝骏")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/baojun.jpg";
		} else if(brand.contains("宝马")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/bmw.jpg";
		} else if(brand.contains("保时捷")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/baoshijie.jpg";
		} else if(brand.contains("北汽")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/beijingqiche.jpg";
		} else if(brand.contains("奔驰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/benchi.jpg";
		} else if(brand.contains("奔腾")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/benteng.jpg";
		} else if(brand.contains("本田")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/bentian.jpg";
		} else if(brand.contains("比亚迪")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/byd.jpg";
		} else if(brand.contains("标致")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/biaozhi.jpg";
		} else if(brand.contains("别克")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/buick.jpg";
		} else if(brand.contains("传祺")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/chuanqi.jpg";
		} else if(brand.contains("大众")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/dazhong.jpg";
		} else if(brand.contains("道奇")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/daoqi.jpg";
		} else if(brand.contains("帝豪")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/dihao.jpg";
		} else if(brand.contains("东风风神")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/dongfeng.jpg";
		} else if(brand.contains("东风")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/dongfeng.jpg";
		} else if(brand.contains("东南")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/dongnan.jpg";
		} else if(brand.contains("菲亚特")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/flat.jpg";
		} else if(brand.contains("丰田")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/fengtian.jpg";
		} else if(brand.contains("福特")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/ford.jpg";
		} else if(brand.contains("哈弗")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/changcheng.jpg";
		} else if(brand.contains("海马")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/haima.jpg";
		} else if(brand.contains("红旗")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/hongqi.jpg";
		} else if(brand.contains("华泰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/huatai.jpg";
		} else if(brand.contains("吉利")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/Geely.jpg";
		} else if(brand.contains("吉普")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/jeep.jpg";
		} else if(brand.contains("江淮")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/jianghuai.jpg";
		} else if(brand.contains("捷豹")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/jaguar.jpg";
		} else if(brand.contains("凯迪拉克")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/kaidilake.jpg";
		} else if(brand.contains("克莱斯勒")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/chrysler.jpg";
		} else if(brand.contains("雷克萨斯")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/leikesasi.jpg";
		} else if(brand.contains("雷诺")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/leinuo.jpg";
		} else if(brand.contains("理念")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/linian.jpg";
		} else if(brand.contains("力帆")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/lifan.jpg";
		} else if(brand.contains("莲花")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/lianhua.jpg";
		} else if(brand.contains("林肯")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/linken.jpg";
		} else if(brand.contains("铃木")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/lingmu.jpg";
		} else if(brand.contains("陆风")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/lufeng.jpg";
		} else if(brand.contains("路虎")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/landrover.jpg";
		} else if(brand.contains("马自达")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/mazida.jpg";
		} else if(brand.contains("名爵")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/minjue.jpg";
		} else if(brand.contains("奇瑞")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/qirui.jpg";
		} else if(brand.contains("启辰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/qicheng.jpg";
		} else if(brand.contains("起亚")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/qiya.jpg";
		} else if(brand.contains("全球鹰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/quanqiuying.jpg";
		} else if(brand.contains("日产")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/nissan.jpg";
		} else if(brand.contains("荣威")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/rongwei.jpg";
		} else if(brand.contains("瑞麒")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/ruiling.jpg";
		} else if(brand.contains("三菱")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/sanling.jpg";
		} else if(brand.contains("双环")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/shuanghuan.jpg";
		} else if(brand.contains("双龙")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/shuanglong.jpg";
		} else if(brand.contains("斯巴鲁")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/sibalu.jpg";
		} else if(brand.contains("斯柯达")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/skoda.jpg";
		} else if(brand.contains("沃尔沃")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/volvo.jpg";
		} else if(brand.contains("夏利")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/xiali.jpg";
		} else if(brand.contains("现代")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/xiandai.jpg";
		} else if(brand.contains("雪佛兰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/xuefulan.jpg";
		} else if(brand.contains("雪铁龙")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/xuetielong.jpg";
		} else if(brand.contains("一汽")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/yiqi.jpg";
		} else if(brand.contains("英菲尼迪")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/yinfeinidi.jpg";
		} else if(brand.contains("英伦")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/yinglun.jpg";
		} else if(brand.contains("长安")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/changan.jpg";
		} else if(brand.contains("长城")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/changcheng.jpg";
		} else if(brand.contains("长丰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/changfeng.jpg";
		} else if(brand.contains("中华")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/zhonghua.jpg";
		} else if(brand.contains("中兴")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/zhongxing.jpg";
		} else if(brand.contains("众泰")) {
			img = "http://carlogo.oss-cn-hangzhou.aliyuncs.com/zhongtai.jpg";
		}
		return img;
	}
	/**
	 * 读取car的附件url，附件都存储在阿里云oss上面
	 * @param carId  汽车id
	 */
	public List<String> getCarAttachmentUrls(String carid) {
		// 构造ListObjectsRequest请求
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest("auditimg");
		listObjectsRequest.setPrefix(carid + "/");
		ObjectListing listing = client.listObjects(listObjectsRequest);
		
		List<String> urls = new ArrayList<>();
		for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
			urls.add("http://auditimg.oss-cn-hangzhou.aliyuncs.com/" + objectSummary.getKey());
		}
		return urls;
	}
	/**
	 * 查询指定车辆的上年度车险的的结束日期
	 * @param carIds
	 * @return
	 */
	public Map<String, Date> getbInsuranceEndDateByIds(List<String> carIds){
		return carDaoImpl.getbInsuranceEndDateByIds(carIds);
	}

	public List<ClaimHistory> getClaimHistoryByCarId(String carId){
		return claimHistoryDao.findByCarIdOrderByClaimYearDesc(carId);
	}
	
	public List<Car> getCarByUserId(String userId){
		return carDao.findByUserId(userId);
	}

	public VehicleLicense getVehicleLicenseByNumber(String number) {
		return vehicleLicenseDao.findByNumber(number);
	}
	
	public void save(Car car) {
		carDao.save(car);
	}
}
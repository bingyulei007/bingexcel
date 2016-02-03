/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SystemUtils;
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
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.CacheHelper;
import com.jt.core.ErrorCode;
import com.jt.core.ServiceCategory;
import com.jt.core.dao.CityDao;
import com.jt.core.dao.GoodsDao;
import com.jt.core.dao.GoodsDaoImpl;
import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.OmsUserDao;
import com.jt.core.dao.RegionDao;
import com.jt.core.dao.impl.MerchantDaoImpl;
import com.jt.core.model.City;
import com.jt.core.model.Goods;
import com.jt.core.model.Merchant;
import com.jt.core.model.MerchantQueryCondition;
import com.jt.core.model.OmsUser;
import com.jt.core.model.Region;
import com.jt.exception.CommonLogicException;
import com.jt.utils.CipherUtil;
import com.jt.utils.DateUtils;
import com.jt.utils.JSONSerializer;

/**
 * @author Wuqh
 */
@Transactional
@Service
public class VehicleMerchantService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private GoodsDao goodsDao;
	
	@Autowired
	private MerchantDao merchantDao;
	
	@Autowired
	private RegionDao regionDao;
	
	@Autowired
	private CityDao cityDao;

	@Autowired
	private GoodsDaoImpl goodsDaoImpl;
	
	@Autowired
	private CacheHelper cacheHelper;
	
	@Autowired
	private MerchantDaoImpl merchantDaoImpl;
	
	@Autowired
	private InsurancePolicyDao insurancePolicyDao;
	
	@Autowired
	private OmsUserDao omsUserDao;
	
	@Autowired
	private BaiduAPIStore apiStore;
    
    private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private String bucketName = "merchantimg";
	
	public Merchant getByMerchatId(long merchantId){
		return merchantDao.findOne(merchantId);
	}
	
	public String findOpenedArea() {
		AreaJson areaJson = new AreaJson();
		List<String> provinces = cityDao.findAllOpenedProvinceNames();
		List<City> cities = cityDao.findAllOpenCities();
		List<Region> regions = regionDao.findAll();
		if(CollectionUtils.isNotEmpty(provinces)){
			List<AreaJson.Province> ps = new ArrayList<AreaJson.Province>();
			for(String province : provinces){
				AreaJson.Province p = new AreaJson.Province();
				p.setProvince(province);
				if(CollectionUtils.isNotEmpty(cities)){
					List<AreaJson.Province.City> cts = new ArrayList<AreaJson.Province.City>();
					for(City city : cities){
						if(province.equals(city.getProvince())){
							AreaJson.Province.City ct = new AreaJson.Province.City();
							ct.setCityCode(city.getCityCode());
							ct.setName(city.getName());
							if(CollectionUtils.isNotEmpty(regions)){
								List<AreaJson.Province.City.Region> rgs = new ArrayList<AreaJson.Province.City.Region>();
								for(Region region : regions){
									if(city.getCityCode() == region.getCityCode()){
										AreaJson.Province.City.Region rg = new AreaJson.Province.City.Region();
										rg.setId(region.getId());
										rg.setName(region.getName());
										rgs.add(rg);
									}
								}
								ct.setRegions(rgs);
							}
							cts.add(ct);
						}
						p.setCities(cts);
					}
				}
				ps.add(p);
			}
			areaJson.setProvinces(ps);
		}
		return JSONSerializer.serialize(areaJson);
	}
	
	public List<Region> getAllRegionsByCityCode(int cityCode){
		return regionDao.findByCityCode(cityCode);
	}
	
	/**
	 * 获取商家的洗车商品
	 * @param merchantId
	 * @return
	 */
	public List<Goods> getGoodsByMerchantId(long merchantId, boolean deleted, List<Integer> serviceCategoris){
		return goodsDao.findByMerchantIdAndDeletedNotAndServiceCategoryIn(merchantId, deleted, serviceCategoris);
	}
	
	public Merchant add(VehicleMerchantFormBean formBean, MultipartFile file) throws CommonLogicException {
		//1.保存注册商家信息
		Merchant merchant = new Merchant();
		merchant.setUsername(formBean.getUserName().trim());
		String encodPassword = CipherUtil.generatePassword("111111");
		merchant.setPassword(encodPassword);
		merchant.setName(formBean.getName());
		merchant.setLevel(formBean.getLevel());
		merchant.setAlias(formBean.getAlias());
		
		merchant.setProvince(formBean.getProvince());
		merchant.setCityCode(formBean.getCityCode());
		if(merchant.getCityCode() <= 0) {
			throw new CommonLogicException(ErrorCode.CITY_NOT_FOUND, "城市编码不能小于等于0");
		}
		merchant.setRegionId(formBean.getRegionId());
//		City city = cityDao.findByCityCode(formBean.getCityCode());
//		Region region = regionDao.findOne((int)formBean.getRegionId());
//		merchant.setAddress(formBean.getProvince() + city.getName() + region.getName() + formBean.getAddress());
		merchant.setAddress(formBean.getAddress());
		
		merchant.setLegalPerson(formBean.getLegalPerson());
		merchant.setManager(formBean.getManager());
		merchant.setManagerPhone(formBean.getManagerPhone());
		merchant.setWorkingTime(formBean.getWorkingTime());
		merchant.setHotline(formBean.getHotline());
		
		//TODO，经纬度改成必填
//		if(formBean.getLongitude() <= 0 || formBean.getLatitude() <= 0) {
//			throw new IllegalArgumentException("经纬度必填！");
//		}
		merchant.setLongitude(formBean.getLongitude());
		merchant.setLatitude(formBean.getLatitude());
		
//		boolean matched = SupportedBank.supported(formBean.getBank());
//		if(!matched) {
//			throw new CommonLogicException(ErrorCode.ILLEGAL_ARGUMENT, "对不起，开户行格式校验失败，请按正确格式填写！");
//		}
		merchant.setBank(formBean.getBank());
//		if(apiStore.queryBankCardInfo(merchantFormBean.getBankCard()) == null) {
//			throw new IllegalArgumentException("银行卡号校验失败");
//		}
		merchant.setBankCard(formBean.getBankCard());
		merchant.setBankAcountName(formBean.getBankAcountName());
		
		merchant.setInfoCompleted(Boolean.TRUE); //资料完整
		merchant.setApproved(1);//审核
		merchant.setCreateDate(new Date());
		merchant.setLastUpdateDate(new Date());
		merchant.setChexian(formBean.isChexian());
		merchant.setWashcar(formBean.isWashcar());
		merchant.setSalesId(formBean.getSalesId());
		OmsUser omsUser = omsUserDao.findOne(formBean.getSalesId());
		merchant.setSalesman(omsUser.getName());
		merchant.setSignedDate(DateUtils.convertStrToDate(formBean.getSignedDate(), "yyyy-MM-dd"));
		
		merchant = merchantDao.save(merchant);
		if(formBean.isWashcar()){
			//2. 初化洗车商品价格
			float normalPrice = formBean.getNormalPrice();
			float normalAccountPrice = formBean.getNormalAccountPrice();
			float finePrice = formBean.getFinePrice();
			float fineAccountPrice = formBean.getFineAccountPrice();
			boolean isContainTax = formBean.isContainTax();
			initWashCarData(merchant, normalPrice, normalAccountPrice, finePrice, fineAccountPrice, isContainTax);
			//3.上传附件
			try {
				upload(merchant.getId(), file);
			} catch (IOException e) {
			}
			//4. 更新缓存信息
			if(SystemUtils.IS_OS_LINUX) {
				cacheHelper.addMerchant(merchant);
			}
		}
		return merchant;
	}
	
	/**
	 * 更新商家信息
	 * @param merchant
	 * @param file
	 * @param normalPrice
	 * @param normalAccountPrice
	 * @param finePrice
	 * @param fineAccountPrice
	 */
	public void updatMerchant(Merchant merchant, MultipartFile file,
			float normalPrice, float normalAccountPrice, float finePrice,
			float fineAccountPrice, boolean isContainTax) throws IOException {
		OmsUser omsUser = omsUserDao.findOne(merchant.getSalesId());
		merchant.setSalesman(omsUser.getName());
		Merchant result = saveMerchant(merchant);
		//更新GOODS表中的洗车价格信息
		if(result != null){
			ArrayList<Integer> c = new ArrayList<>();
			c.add(ServiceCategory.NORMAL_WASH_CAR_5);
			c.add(ServiceCategory.CAREFULLY_WASH_CAR_5);
			List<Goods> goodss = goodsDao.findByMerchantIdAndDeletedNotAndServiceCategoryIn(result.getId(), true, c); 
			if(goodss != null && goodss.size()>0){
				List<Goods> newGoods = new ArrayList<Goods>();
				for(Goods goods : goodss){
					if(goods.getServiceCategory() == ServiceCategory.NORMAL_WASH_CAR_5){
						goods.setOriginalPrice(normalPrice);
						goods.setPrice(normalAccountPrice);
					}else if(goods.getServiceCategory() == ServiceCategory.CAREFULLY_WASH_CAR_5){
						goods.setOriginalPrice(finePrice);
						goods.setPrice(fineAccountPrice);
					}
					goods.setContainTax(isContainTax);
					//更新经纬度
					goods.setLatitude(merchant.getLatitude());
					goods.setLongitude(merchant.getLongitude());
					goods.setMerchantName(merchant.getAlias());
					newGoods.add(goods);
				}
				goodsDao.save(newGoods);
			}else{ //插入新的洗车商品数据
				initWashCarData(merchant, normalPrice, normalAccountPrice, finePrice,	fineAccountPrice, isContainTax);
			}
		}
		if(file != null && file.getSize()>0 && merchant.isWashcar()){
			upload(result.getId(),file);
		}
		if(SystemUtils.IS_OS_LINUX){
			cacheHelper.addMerchant(merchant);
		}
	}
	
	/**
	 * 多文件上传 
	 */
	public void upload(long merchantId, MultipartFile file) throws IOException{
		OSSClient client = null;
		String folderName = merchantId+"/";
		ObjectMetadata objectMeta = new ObjectMetadata();
		byte[] buffer = new byte[0];
		InputStream in = new ByteArrayInputStream(buffer);  
		objectMeta.setContentLength(0);
		try {
			//1. 创建文件夹
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
		    client.putObject(bucketName, folderName, in, objectMeta);
	        
	        //2. 上传图片缩略图，大小为148×148
	        String newFileName= "small.jpg";
        	BufferedImage bigBi = Thumbnails.of(file.getInputStream()).size(148, 148).keepAspectRatio(false).asBufferedImage();
        	bigBi.flush(); 
        	ByteArrayOutputStream bigBs = new ByteArrayOutputStream();  
        	ImageOutputStream BigImOut = ImageIO.createImageOutputStream(bigBs);  
        	ImageIO.write(bigBi, "jpg", BigImOut);  //所的上传的图片格式统一转换成JPG
        	in = new ByteArrayInputStream(bigBs.toByteArray());
        	ObjectMetadata meta = new ObjectMetadata();
        	// 必须设置ContentLength
        	meta.setContentLength(bigBs.toByteArray().length);
        	if(client.doesObjectExist(bucketName, folderName + newFileName)){
        		client.deleteObject(bucketName, folderName + newFileName);
        	}
        	client.putObject(bucketName, folderName + newFileName, in, meta);
        	
	        //3. 上传图片缩略图，大小为640×300
	        newFileName= "big.jpg";
        	BufferedImage smallBi = Thumbnails.of(file.getInputStream()).size(640,300).keepAspectRatio(false).asBufferedImage();
        	smallBi.flush(); 
        	ByteArrayOutputStream smallBs = new ByteArrayOutputStream();  
        	ImageOutputStream smallImOut = ImageIO.createImageOutputStream(smallBs);  
        	ImageIO.write(smallBi, "jpg", smallImOut); 
        	in = new ByteArrayInputStream(smallBs.toByteArray()); 
        	// 必须设置ContentLength
        	meta.setContentLength(smallBs.toByteArray().length);
        	if(client.doesObjectExist(bucketName, folderName + newFileName)){
        		client.deleteObject(bucketName, folderName + newFileName);
        	}
        	client.putObject(bucketName, folderName + newFileName, in, meta);
		}finally {
		    in.close();
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}
    
    @Transactional
    private void initWashCarData(Merchant merchant,float normalPrice, float normalAccountPrice, float finePrice, float fineAccountPrice, boolean isContainTax ){
		if(normalPrice>0 || normalAccountPrice>0){
			Goods result = goodsDao.findByMerchantIdAndServiceCategory(merchant.getId(), ServiceCategory.NORMAL_WASH_CAR_5);
			if(result != null){ //数据库中已存在
				result.setOriginalPrice(normalAccountPrice);
				result.setPrice(normalAccountPrice);
				result.setContainTax(isContainTax);
				goodsDao.save(result);
			}else{
		    	Goods goods  = new Goods();//普洗5座以下
		    	goods.setCreateDate(new Date());
		    	goods.setName("普洗");
		    	goods.setPrice(normalAccountPrice); //这里是商家洗车结算价
				goods.setDescr("包括：外观清洗、内饰清洗、脚垫清洗、后备箱清洗");
				goods.setMerchantId(merchant.getId());
				goods.setMerchantName(merchant.getAlias());
				goods.setCityCode(merchant.getCityCode());
				goods.setRegionId(merchant.getRegionId());
				goods.setBuyCount(0);
				goods.setScore(5);
				goods.setOriginalPrice(normalPrice); //这里是商家门市价
				goods.setLatitude(merchant.getLatitude());
				goods.setLongitude(merchant.getLongitude());
				goods.setCreateDate(new Date());
				goods.setServiceCategory(ServiceCategory.NORMAL_WASH_CAR_5);
				goods.setContainTax(isContainTax);
				goodsDao.save(goods);
			}
		}
		
		if(finePrice > 0 || fineAccountPrice > 0){//说明商家没有精洗，则不生成
			Goods result = goodsDao.findByMerchantIdAndServiceCategory(merchant.getId(), ServiceCategory.CAREFULLY_WASH_CAR_5);
			if(result != null){ //数据库中已存在
				result.setOriginalPrice(finePrice);
				result.setPrice(fineAccountPrice);
				result.setContainTax(isContainTax);
				goodsDao.save(result);
			}else{
				Goods goods = new Goods();
				goods.setCreateDate(new Date());
				goods.setName("精洗");
				goods.setPrice(fineAccountPrice);
				goods.setDescr("包括：外观清洗、内饰清洗、后备箱清洗、脚垫清洗、座椅清洗、仪表盘清洗、发动机舱清洗、打蜡");
				goods.setMerchantId(merchant.getId());
				goods.setMerchantName(merchant.getAlias());
				goods.setCityCode(merchant.getCityCode());
				goods.setRegionId(merchant.getRegionId());
				goods.setBuyCount(0);
				goods.setScore(5);
				goods.setOriginalPrice(finePrice);
				goods.setLatitude(merchant.getLatitude());
				goods.setLongitude(merchant.getLongitude());
				goods.setCreateDate(new Date());
				goods.setServiceCategory(ServiceCategory.CAREFULLY_WASH_CAR_5);
				goods.setContainTax(isContainTax);
				goodsDao.save(goods);
			}
		}
    }

	public Merchant saveMerchant(Merchant merchant){
		return merchantDao.save(merchant);
	}
	
    public void deleteMerchantById(long id){
    	long policies = insurancePolicyDao.countByChannelCode(id);
    	if(policies > 0) {
    		logger.info("由于编号为【{}】商家下有保单，不能删除 ", id);
    		throw new CommonLogicException(ErrorCode.DELETE_MERCHANT_FORBIDDEN, "商家名下有保单，禁止删除");
    	}
		merchantDao.delete(id);
		logger.info("删除商家: {}", id);
		int goods = goodsDaoImpl.deleteGoodsByMerchantId(id);
		logger.info("删除商家: {} 个商品", goods);
		OSSClient client = null;
		String folderName = id+"/";
		try {
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
	        //删除文件夹名为merchantId的文件夹以及文件夹下的缩略图small.jpg和big.jpg，
        	if(client.doesObjectExist(bucketName, folderName)){
        		client.deleteObject(bucketName, folderName);
        		logger.info("删除商家图片: {}", id);
        	}
		}finally {
		    if(client != null){
		    	client.shutdown();
		    }
		}
		if(SystemUtils.IS_OS_LINUX){
			cacheHelper.removeMerchant(id+"");
		}
	}
    
    /**
	 * 查询商家
	 */
	public Page<Merchant> findMerchants(final MerchantQueryCondition condition) {
		Page<Merchant> pageResult = merchantDao.findAll(new Specification<Merchant>() {
			@Override
			public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotEmpty(condition.getProvince())&&!"0".equals(condition.getProvince())) {
					predicateList.add(cb.equal(root.get("province"), condition.getProvince()));
				}
				if(condition.getCityCode() > 0) {
					predicateList.add(cb.equal(root.get("cityCode"), condition.getCityCode()));
				} 
				if(condition.getRegionId() > 0) {
					predicateList.add(cb.equal(root.get("regionId"), condition.getRegionId()));
				} 
				if(StringUtils.isNotEmpty(condition.getKeyName())){
					Path<String> namePath = root.get("name"); 
					Path<String> aliasPath = root.get("alias");
					Predicate p1 = cb.like(namePath, "%"+condition.getKeyName()+"%");
					Predicate p2 = cb.like(aliasPath, "%"+condition.getKeyName()+"%");
					predicateList.add(cb.or(cb.or(p2), p1));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(condition.getPageNumber(), condition.getPageSize(), Direction.DESC,null));
		return pageResult;
	}
	
	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "lastUpdateDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

    public List<Merchant> getAllMerchantForMap(){
    	return merchantDao.findAll();
    }
    
    public Map<String, Object> findVehicleMerchants(MerchantQueryCondition query){
    	List<Merchant> merchants = merchantDaoImpl.findVehicleMerchants(query);
		int pageSize = query.getPageSize();
		int totalItems = merchantDaoImpl.countVehicleMerchant(query);
		//总页数
		int totalPages = (totalItems+pageSize-1)/pageSize;
		Map<String, Object> map = new HashMap<>();
		map.put("totalItems", totalItems);
		map.put("totalPages", totalPages);
		map.put("merchants", merchants);
		return map;
    }

	public List<Merchant> queryMerchantsByName(final String merchantName) {
		List<Merchant> resultList = merchantDao.findAll(new Specification<Merchant> () {  
		public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {  
			ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
		    Path<String> namePath = root.get("name");  
		    Path<String> aliasPath = root.get("alias");  
		    Predicate p1 = cb.like(namePath, "%"+merchantName+"%");
			Predicate p2 = cb.like(aliasPath, "%"+merchantName+"%");
			predicateList.add(cb.or(cb.or(p2), p1));
			return cb.and(predicateList.toArray(new Predicate[0]));
		   }  
		  }); 
		return resultList;
	}

	public Merchant findMerchantById(String merchantCode) {
		return merchantDao.findOne(Long.parseLong(merchantCode));
	}

	public Merchant findByUsername(String userName) {
		return merchantDao.findByUsername(userName);
	}
}

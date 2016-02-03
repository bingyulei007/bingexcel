package com.jt.ycl.oms.city;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.CityDao;
import com.jt.core.dao.CityDaoImpl;
import com.jt.core.dao.RegionDao;
import com.jt.core.dao.ZKOrgCodeDao;
import com.jt.core.model.City;
import com.jt.core.model.Region;

@Service
public class CityService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CityDao cityDao;
	
	@Autowired
	private CityDaoImpl cityDaoImpl;
	
	@Autowired
	private ZKOrgCodeDao zkOrgCodeDao;
	
	@Autowired
	private RegionDao regionDao;
	
	/**
	 * 城市信息缓存
	 */
	private Map<Integer, City> cityMap = new ConcurrentHashMap<>();
	
	@PostConstruct
	public void init(){
		List<City> cityList = this.cityDao.findAll();
		for(City city : cityList) {
			this.cityMap.put(city.getCityCode(), city);
		}
		logger.info("城市数据已加载");
	}
	
	/**
	 * 获取所有的省份，以provinceCode为key，省份名称为value
	 * @return
	 */
	public Map<Integer, String> getAllProvince(){
		Map<Integer, String> result = cityDaoImpl.getAllProvince();
		return result;
	}
	
	public Map<Integer, String> getCitiesByProvinceCode(int provinceCode){
		return cityDaoImpl.getCitiesByProvinceCode(provinceCode);
	}
	
	public City getByCityCode(int cityCode){
		City city = cityMap.get(cityCode);
		if(city == null) {
			city = cityDao.findByCityCode(cityCode);
			if(city != null)
				cityMap.put(cityCode, city);
		}
		return city;
	}

	public Map<Integer, City> getCityMap(){
		return cityMap;
	}
	
	public List<String> getRegionList(int cityCode, int icCode) {
		return zkOrgCodeDao.findRegionByCityCode(cityCode);
	}
	
	/**
	 * 根据城市编码获取所有区域
	 * 
	 * @param cityCode
	 * @return
	 */
	public List<Region> getAllRegionsByCity(int cityCode){
		return regionDao.findByCityCode(cityCode); //城市开通即对应所有区县都是开通的
	}
}

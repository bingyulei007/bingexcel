/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.util.List;



/**
 * @author wuqh
 *
 */
public class AreaJson {
	
	private List<Province> provinces;

	public static class Province{
	
		private String province;
		
		private List<City> cities;
		
		public static class City{
			
			private String name;
			
			private int cityCode;
			
			private List<Region> regions;
			
			public static class Region{
				private int id;
				
				private String name;
	
				public String getName() {
					return name;
				}
	
				public void setName(String name) {
					this.name = name;
				}
	
				public int getId() {
					return id;
				}
	
				public void setId(int id) {
					this.id = id;
				}
			}
	
			public String getName() {
				return name;
			}
	
			public void setName(String name) {
				this.name = name;
			}
	
			public int getCityCode() {
				return cityCode;
			}
	
			public void setCityCode(int cityCode) {
				this.cityCode = cityCode;
			}
	
			public List<Region> getRegions() {
				return regions;
			}
	
			public void setRegions(List<Region> regions) {
				this.regions = regions;
			}
		}
		public String getProvince() {
			return province;
		}


		public void setProvince(String province) {
			this.province = province;
		}


		public List<City> getCities() {
			return cities;
		}


		public void setCities(List<City> cities) {
			this.cities = cities;
		}
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}
}

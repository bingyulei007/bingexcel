/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 百度API
 * 
 * @author Andy Cui
 */
@Component
public class BaiduAPIStore {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public static void main(String[] args) {
		BaiduAPIStore apiStore = new BaiduAPIStore();
		apiStore.queryBankCardInfo("4682030210746198");
	}

	/**
	 * 查询给定的银行卡号是否有效，如果有效返回银行卡信息，否则返回null
	 */
	@SuppressWarnings("unchecked")
	public String[] queryBankCardInfo(String cardNo) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL("http://apis.baidu.com/datatiny/cardinfo/cardinfo?cardnum=" + cardNo);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("apikey", "0f753722107f72a1b808d8737a429911");
			connection.connect();
			
			InputStream is = connection.getInputStream();
			ObjectMapper mapper = new ObjectMapper();
			
			/*
			 * {
			    "status": 1,
			    "data": {
			        "cardtype": "借记卡",
			        "cardlength": 19,
			        "cardprefixnum": "623058",
			        "cardname": "平安银行IC借记卡",
			        "bankname": "平安银行",
			        "banknum": "04100000"
			    }
			 * }
			 */
			Map<String, Object> jsonMap = mapper.readValue(is, Map.class);
			logger.info("验证银行卡：{}，返回结果：{}", cardNo, jsonMap.toString());
			int status = (int) jsonMap.get("status");
			if(status == -1) {
				return null;
			}
			Map<String, Object> dataMap = (Map<String, Object>) jsonMap.get("data");
			if(dataMap == null) {
				return null;
			} else {
				String[] resultStrings = new String[3];
				resultStrings[0] = (String) dataMap.get("cardtype");
				resultStrings[0] = (String) dataMap.get("cardname");
				resultStrings[0] = (String) dataMap.get("bankname");
				return resultStrings;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
		return null;
	}
}
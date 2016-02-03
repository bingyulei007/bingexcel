package com.jt.ycl.oms.wxmgt;

import java.io.IOException;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import net.sf.json.JSONObject;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WXAccessTokenCache {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	final String host = "78407f3388fd4c07.m.cnhzaliqshpub001.ocs.aliyuncs.com";//Memcache的访问地址
    final String port ="11211"; //默认端口 11211，不用改
    final String username = "78407f3388fd4c07";//访问Memcache的账号
    final String password = "ykycMemcache2015";//访问Memcache的密码
    
	private static String apiUrl = "https://api.weixin.qq.com/cgi-bin/token";
    
    private MemcachedClient client;
    
    private HashMap<String, Object> mappings = null;
    
    @Autowired
    private WXConfigInfo wxConfigInfo;
    
    @PostConstruct
    public void init() throws IOException {
    	if(SystemUtils.IS_OS_LINUX) {
    		AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"}, new PlainCallbackHandler(username, password));
    		client = new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(Protocol.BINARY).setAuthDescriptor(ad).build(), 
    				AddrUtil.getAddresses(host + ":" + port));
    		logger.info("连接阿里云Memcache服务成功.");
    	} else {
    		mappings = new HashMap<>();
    		logger.info("初始化JVM本地内存作为缓存服务.");
    	}
    }
    
    /**
	 * @param key
	 * @param exp
	 * @param value
	 */
	public void put(String key, Object value) {
		if(StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("key不能为空.");
		}
		int exp = 2 * 3600;//2小时
		
		if(value == null) {
			throw new IllegalArgumentException("value不能为空.");
		}
		if(SystemUtils.IS_OS_LINUX) {
			client.set(key, exp, value);
		} else {
			mappings.put(key, value);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("写入缓存：key = {}, value = {}", key, value);
		}
	}
	
	/**
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		if(StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("key不能为空.");
		}
		if(SystemUtils.IS_OS_LINUX) {
			return client.get(key);
		} else {
			return mappings.get(key);
		}
	}
	
	 /** 
     * 获取微信access_token， WX的JS调用会间接用到 
     * TOKEN的有效期只有7200秒
     * 如{"access_token":"w4NihBZd6R5tcTc37PJFEOO8cWy-E9slOaOBbC-svrhVsIaOUlSZGUbcqsjTZ1CKS72edvDR2a9-yFQ3-_AbpayHElvjJ4SgBGksBmaYYAk",
     * "expires_in":7200}
     * @param apiurl, 微信APIurl 
     * @param appid, 微信appid 
     * @param secret, 微信secret 
     * @return access_token 字符串 
     * @throws IOException 
     */  
	public String getAccessToken() throws IOException {
		Object accessToken = get(wxConfigInfo.getAppId().trim());
		if(accessToken != null){
			return (String)accessToken;
		}
		
		synchronized (apiUrl) {
			accessToken = get(wxConfigInfo.getAppId());
			if(accessToken == null){
				String tokenUrl = String.format("%s?grant_type=client_credential&appid=%s&secret=%s", apiUrl.trim(),
						wxConfigInfo.getAppId().trim(), wxConfigInfo.getAppSecret().trim());
				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet(tokenUrl);
				String access_token = null;
				CloseableHttpResponse httpResponse = null;
				try {
					httpResponse = httpClient.execute(httpGet);
					int statusCode = httpResponse.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						HttpEntity entity = httpResponse.getEntity();
						String entityString = EntityUtils.toString(entity);
						JSONObject fromObject = JSONObject.fromObject(entityString);
						access_token = fromObject.get("access_token").toString();
						
						//放入缓存
						put(wxConfigInfo.getAppId().trim(), access_token);
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (httpResponse != null) {
						httpResponse.close();
					}
					if (httpClient != null) {
						httpClient.close();
					}
				}
				return access_token;
			}else{
				return (String)accessToken;
			}
		}
		
	}  

}

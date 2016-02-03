/**
 * 
 */
package com.jt.ycl.oms.wxmgt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author wuqh
 *
 */
@Component("wxConfigInfo")
public class WXConfigInfo {

	@Value("${appId}")
    private String appId;

    @Value("${appSecret}")
    private String appSecret;
    
    @Value("${apiPort}")
    private String apiPort;
    
    @Value("${appUrl}")
    private String appUrl;
        
    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }  
    
    public String getApiPort() {
        return apiPort;
    }   
    
    public String getAppUrl(){
    	return appUrl;
    }
    
}
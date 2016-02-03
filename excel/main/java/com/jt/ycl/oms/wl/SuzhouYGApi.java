/**
 * 
 */
package com.jt.ycl.oms.wl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;

import com.jt.utils.HttpService;

/**
 * @author Andy Cui
 */
public class SuzhouYGApi {

	private HttpService httpService = HttpService.getInstance();
	
	/**
	 * 给苏州易高下发一条配送信息
	 * 
	 * @return
	 * @throws Exception 
	 */
	public String send() throws Exception {
		HttpPost sendPost = new HttpPost("");
		String xmlRequest = null;
		StringEntity entity = new StringEntity(xmlRequest, "UTF-8");
		entity.setContentType("application/xml");// 发送json数据需要设置contentType
		sendPost.setEntity(entity);
		
		CloseableHttpResponse response = httpService.execute(sendPost);
		
		return null;
		
	}
	
	
	/**
	 * 查询配送单的当前状态
	 * 
	 * @param orderNo		快递单号
	 */
	public String queryStatus(String orderNo) {
		return orderNo;
	}
}
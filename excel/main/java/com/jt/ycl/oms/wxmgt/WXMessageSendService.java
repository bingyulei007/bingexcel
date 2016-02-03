/**
 * 
 */
package com.jt.ycl.oms.wxmgt;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.utils.DateUtils;
import com.jt.utils.HttpService;

/**
 * @author wuqh
 *
 */
@Service
public class WXMessageSendService {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private WXAccessTokenCache cache;
	
	@Autowired
	private WXConfigInfo configInfo;
	
	public String libaoNotify(String openId, String orderId, String owner, String carNumber, int amount){
		Map<String, Object> param = new HashMap<>();
		param.put("touser", openId);
		if(configInfo.getAppUrl().contains("test")){
			param.put("template_id", "wJPFHWWA7li5aVdBjm9lDWaAv3KB1c4vMbIHW5gpbHM");
		}else{
			param.put("template_id", "dNx2lvWDqGTXisFhnrNYWtmfT189L5DRi_3CJAtmTfA");
		}
		String merchantUrl = "http://" + configInfo.getAppUrl() + "/wx/o2o/merchant/libao/" + orderId + "/go";
		param.put("url", merchantUrl);
		param.put("topcolor", "#eee");
		Map<String, Object> data = new HashMap<>();
		Map<String, String> m = new HashMap<String, String>();
		m.put("value", "尊敬的客户，您的投保单已成功处理完成，我们赠送你一套汽车保养大礼包！");
		m.put("color", "#173177");
		data.put("first", m);
		
		m = new HashMap<String, String>();
		m.put("value", owner);
		m.put("color", "#173177");
		data.put("keyword1", m);
		
		m = new HashMap<String, String>();
		m.put("value", carNumber);
		m.put("color", "#173177");
		data.put("keyword2", m);
		
		m = new HashMap<String, String>();
		m.put("value", amount+"元");
		m.put("color", "#173177");
		data.put("keyword3", m);
		
		m = new HashMap<String, String>();
		m.put("value", DateUtils.convertDateToStr(new Date(), "yyyy年MM月dd日 HH:mm:ss"));
		m.put("color", "#173177");
		data.put("keyword4", m);
		
		m = new HashMap<String, String>();
		m.put("value", "点击即可去您附近的汽车服务商家领取！");
		m.put("color", "#173177");
		data.put("remark", m);
		
		param.put("data", data);
		CloseableHttpClient client = HttpService.getInstance().getHttpClient();
		try {
			String accessToken = cache.getAccessToken();
			String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;
			
			HttpPost post = new HttpPost(url);
			post.setHeader("X-Requested-With", "XMLHttpRequest");
			
			ObjectMapper om = new ObjectMapper();
			StringEntity entity = new StringEntity(om.writeValueAsString(param), "UTF-8");
			entity.setContentType("application/json");// 发送json数据需要设置contentType
			post.setEntity(entity);
			CloseableHttpResponse response = client.execute(post);
			String result = EntityUtils.toString(response.getEntity());
			response.close();
			client.close();
			
			Map<String, Object> resultMap = om.readValue(result, Map.class);
			int errcode = (int) resultMap.get("errcode");
			if(errcode == 0){
				logger.info("发送投保通知消息成功，msgid：{}", resultMap.get("msgid").toString());
			}else{
				logger.info("给{}发送投保通知消息失败，错误代码：" + errcode + "，错误内容：" + resultMap.get("errmsg").toString(), openId);
			}
			return "success";
		} catch (IOException e) {
			logger.error("发送模版消息异常", e);
			return "failed";
		}
	}
}

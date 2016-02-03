package com.jt.ycl.oms.wxmgt;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

@Controller
@RequestMapping(value="wxmgt/menu")
@OMSPermission(permission = Permission.WX_MENU_MGMT)
public class WXMenuController {
	
	@Autowired
	private WXAccessTokenCache cache;
	
	/**
	 * 加载自定义菜单
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String initMenu(HttpServletRequest request, Model model) throws IOException{
		String access_token = cache.getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=" + access_token;
		HttpGet get = new HttpGet(url);
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		String result = "";
		try{
			response = client.execute(get);
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
			if(result.indexOf("errcode") > 0)
				result = "";
			if(StringUtils.isNotEmpty(result)){
				ObjectMapper om = new ObjectMapper();
				Map<String, Object> map = om.readValue(result, Map.class);
				Map<String, Object> menuMap = (Map<String, Object>) map.get("menu");
				if(MapUtils.isNotEmpty(menuMap)){
					result = om.writeValueAsString(menuMap);
				}
			}
			request.setAttribute("menu", result);
			return "wxmgt/wxmenu";
		}finally{
			if(response != null){
				response.close();
			}
			if(client != null){
				client.close();
			}
		}
	}
	
	/**
	 * 创建自定义菜单
	 * @param json
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="create", method = RequestMethod.POST)
	@ResponseBody
	public String createMenu(String json) throws IOException{
		if(StringUtils.isBlank(json)){
			return "{\"errcode\":500,\"errmsg\":\"格式不正确\"}";
		}
		String access_token = cache.getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + access_token;
		HttpPost post = new HttpPost(url);
		StringEntity entity = new StringEntity(json, "UTF-8");
		entity.setContentType("application/json");// 发送json数据需要设置contentType
		post.setEntity(entity);
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			response = client.execute(post);
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");
			return result;
		} finally {
			if(response != null){
				response.close();
			}
			if(client != null){
				client.close();
			}
		}
	}
}

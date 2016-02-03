package com.jt.ycl.oms.wxmgt;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.model.WXSubscribeReplyConfig;
import com.jt.utils.HttpService;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

@Controller
@RequestMapping(value="wxmgt/reply")
@OMSPermission(permission = Permission.WX_SUBSCRIBE_REPLY_MGMT)
public class WXSubscribeReplyController {
	
	private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private String bucketName = "szjt";
	private String floderName = "wximg";
	
	private HttpService httpService = HttpService.getInstance();
	
	@Autowired
	private WXSubscribeReplyService replyService;
	
	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView init(){
		List<WXSubscribeReplyConfig> configs = replyService.getAll();
		ModelMap mm = new ModelMap();
		if(CollectionUtils.isNotEmpty(configs)){
			for(int i = 0; i < configs.size(); i++){
				WXSubscribeReplyConfig config = configs.get(i);
				mm.put("title" + (i + 1), config.getTitle());
				mm.put("url" + (i + 1), config.getUrl());
				mm.put("picUrl" + (i + 1), config.getPicUrl());
			}
		}
		return new ModelAndView("wxmgt/replyConfig", mm);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public ModelAndView save(MultipartHttpServletRequest request) throws IOException{
		List<WXSubscribeReplyConfig> configs = new ArrayList<>();
		OSSClient client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
		for(int i = 1;i<=10;i++){
			String title = request.getParameter("title" + i);
			String url = request.getParameter("url" + i);
			MultipartFile multipartFile = request.getFile("picUrl" + i);
			//只要发现中间一个是空的，后面的都不保存了，这样可以保证顺序
			if(StringUtils.isBlank(title) || StringUtils.isBlank(url) || multipartFile == null){
				break;
			}
			WXSubscribeReplyConfig config = new WXSubscribeReplyConfig();
			config.setTitle(title);
			config.setUrl(url);
			String fileName = multipartFile.getOriginalFilename();
			String name = floderName + "/" + UUID.randomUUID().toString().replaceAll("-", "") + fileName.substring(fileName.lastIndexOf("."));
			InputStream inputStream = multipartFile.getInputStream();
			if(inputStream != null){
				ObjectMetadata meta = new ObjectMetadata();
	        	// 必须设置ContentLength
	        	meta.setContentLength(multipartFile.getSize());
	        	if(client.doesObjectExist(bucketName, name)){
	        		client.deleteObject(bucketName, name);
	        	}
	        	client.putObject(bucketName, name, inputStream, meta);
			}
			String picUrl = "http://" + bucketName + ".oss-cn-hangzhou.aliyuncs.com/" + name;
			config.setPicUrl(picUrl);
			configs.add(config);
		}
		
		replyService.updateAll(configs);
		
		//分别通知三台服务器
		HttpGet get1 = new HttpGet("http://120.55.72.24:6060/apigateway/api/v1/wx/event/callback/config/reload");
		try{
			CloseableHttpResponse response = httpService.execute(get1);
			if(response != null){
				response.close();
			}
		}catch(Exception ex){
			//do nothing
		}
		
		HttpGet get2 = new HttpGet("http://121.40.226.94:6060/apigateway/api/v1/wx/event/callback/config/reload");
		try{
			CloseableHttpResponse response = httpService.execute(get2);
			if(response != null){
				response.close();
			}
		}catch(Exception ex){
			//do nothing
		}
		
		HttpGet get3 = new HttpGet("http://121.43.111.95:6060/apigateway/api/v1/wx/event/callback/config/reload");
		try{
			CloseableHttpResponse response = httpService.execute(get3);
			if(response != null){
				response.close();
			}
		}catch(Exception ex){
			//do nothing
		}
		
		return new ModelAndView("redirect:/wxmgt/reply");
	}

}

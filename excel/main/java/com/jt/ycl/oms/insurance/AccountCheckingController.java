package com.jt.ycl.oms.insurance;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.auth.Role;

/**
 * 
 * @author bing
 * 
 */

@Controller
@RequestMapping(value = { "/account" })
public class AccountCheckingController {
	@Autowired
	private AccountCheckingService 		checkingService;
	/**
	 * 易高对账
	 * @param totleMoney
	 * @param fileUpload
	 * @return
	 */
	 @RequestMapping(value = "/yigao/check/upload", method = RequestMethod.POST)
	 @ResponseBody
	public ModelMap YiGaoCheck(@DateTimeFormat(pattern="yyyy-MM-dd") Date checkTimeStart,@DateTimeFormat(pattern="yyyy-MM-dd") Date checkTimeEnd,double totleMoney,@RequestParam("file") MultipartFile fileUpload,HttpSession session) {
		ModelMap mm=new ModelMap();
		mm.addAttribute("errcode", 1);
		mm.put("errmsg", "请选择正确的文件类型");
		if(checkTimeStart.getTime()>checkTimeEnd.getTime()){
			mm.put("errmsg", "日期范围错误！");
			return mm;
		}
		AccountInfo accountInfo = (AccountInfo) session.getAttribute("user");
		if(!(Role.OMS_MANAGER.equals(accountInfo.getRole().getName())||Role.OMS_HEBAO_USER.equals(accountInfo.getRole().getName()))){
			mm.put("errmsg", "你没有操作权限！");
			return mm;
		}
		
		
		if(fileUpload !=null){
			String filename = fileUpload.getOriginalFilename();
			Pattern pattern=Pattern.compile("^.*\\.xls(x)?$");
			boolean matches = pattern.matcher(filename).matches();
			if(matches){
				int re=checkingService.excelForYiGaoUpgrade(checkTimeStart,checkTimeEnd,fileUpload,totleMoney);
				
				
			
				mm.put("errcode", 0);
				mm.put("body", re);
			}
		}
		
		return mm;
	}
	 /**
	  * 易高对账校验
	  * @param totleMoney
	  * @param fileUpload
	  * @return
	  */
	 @RequestMapping(value = "/yigao/check/verify", method = RequestMethod.POST)
	 @ResponseBody
	 public ModelMap YiGaoVerify(@DateTimeFormat(pattern="yyyy-MM-dd") Date checkTimeStart,@DateTimeFormat(pattern="yyyy-MM-dd") Date checkTimeEnd,double totleMoney,@RequestParam("file") MultipartFile fileUpload) {
		 ModelMap mm=new ModelMap();
		 mm.addAttribute("errcode", 1);
		 mm.put("errmsg", "请选择正确的文件类型");
		 if(checkTimeStart.getTime()>checkTimeEnd.getTime()){
				mm.put("errmsg", "日期范围错误！");
				return mm;
			}
		 if(fileUpload !=null){
			 String filename = fileUpload.getOriginalFilename();
			 Pattern pattern=Pattern.compile("^.*\\.xls(x)?$");
			 boolean matches = pattern.matcher(filename).matches();
			 if(matches){
				 List<String> list=checkingService.excelForYiGaoCheck(checkTimeStart,checkTimeEnd,fileUpload,totleMoney);
				 
				 
				 mm.put("errcode", 0);
				 mm.put("body", list);
			 }
		 }
		 
		 return mm;
	 }
	
}

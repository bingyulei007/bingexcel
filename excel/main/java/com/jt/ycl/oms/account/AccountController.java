/**
 * 
 */
package com.jt.ycl.oms.account;


import java.util.Date;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.model.OmsUser;
import com.jt.utils.CipherUtil;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * @author wuqh
 *
 */
@Controller
@RequestMapping(value={"/omsuser"})
@OMSPermission(permission = Permission.USER_MGMT)
public class AccountController {
	
	@Autowired
	private AccountService accountService;

	@RequestMapping(value="list", method=RequestMethod.GET)
	public String preList(){
		return "user/omsUserList";
	}
	
	@RequestMapping(value="list", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap list(@RequestParam(required=false)String name, @RequestParam(required=false,defaultValue="0")int pageNumber, 
			@RequestParam(required=false,defaultValue="20")int pageSize){
		ModelMap mm = new ModelMap("retcode",0);
		Page<OmsUser> pageResult = accountService.findUsers(name, pageNumber, pageSize);
		mm.addAttribute("users", pageResult.getContent());
		mm.addAttribute("totalPages", pageResult.getSize());
		mm.addAttribute("totalItems", pageResult.getTotalElements());
		return mm;
	}
	
	@RequestMapping(value="enter/add", method=RequestMethod.GET)
	public ModelAndView enterAdd(){
		return new ModelAndView("user/addOmsUser");
	}
	
	@RequestMapping(value="enter/edit/{userId}", method=RequestMethod.GET)
	public ModelAndView enterEdit(@PathVariable String userId){
		OmsUser omsUser = accountService.findOmsUserByUserId(userId);
		return new ModelAndView("user/editOmsUser","user",omsUser);
	}
	
	@RequestMapping(value="enter/change/password/{userId}", method=RequestMethod.GET)
	public ModelAndView enterChangePassword(@PathVariable String userId){
		OmsUser omsUser = accountService.findOmsUserByUserId(userId);
		return new ModelAndView("user/changeOmsUserPassword","user",omsUser);
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap addOmsUser(OmsUser omsUserBean) throws Exception {
		ModelMap mm = new ModelMap();
		OmsUser user = new OmsUser();
		user.setUserId(UUID.randomUUID().toString().replace("-", ""));
		user.setUserName(omsUserBean.getUserName());
		user.setName(omsUserBean.getName());
		user.setPassword(CipherUtil.generatePassword(omsUserBean.getPassword()));
		user.setPhone(omsUserBean.getPhone());
		user.setState(1);
		user.setRole(omsUserBean.getRole());
		user.setEmail(omsUserBean.getEmail());
		user.setEmployeeID(omsUserBean.getEmployeeID());
		user.setCreateDate(new Date());
		accountService.save(user);
		return mm;
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap updateOmsUser(OmsUser omsUserBean) throws Exception {
		ModelMap mm = new ModelMap();
		OmsUser user = accountService.findOmsUserByUserId(omsUserBean.getUserId());
		user.setUserName(omsUserBean.getUserName());
		user.setName(omsUserBean.getName());
		user.setPhone(omsUserBean.getPhone());
		user.setRole(omsUserBean.getRole());
		user.setEmail(omsUserBean.getEmail());
		user.setEmployeeID(omsUserBean.getEmployeeID());
		user.setPhone(omsUserBean.getPhone());
		accountService.save(user);
		return mm;
	}
	
	@RequestMapping(value = "/change/password", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap changePassword(OmsUser omsUserBean) throws Exception {
		ModelMap mm = new ModelMap();
		OmsUser user = accountService.findOmsUserByUserId(omsUserBean.getUserId());
		user.setPassword(CipherUtil.generatePassword(omsUserBean.getPassword()));
		accountService.save(user);
		return mm;
	}
	
	@RequestMapping(value = "/delete/{userId}", method = RequestMethod.GET)
	@ResponseBody
	public ModelMap deleteOmsUser(@PathVariable String userId){
		ModelMap mm = new ModelMap("retcode",0);
		accountService.deleteOmsUser(userId);
		return mm;
	}
}

package com.jt.ycl.oms.account;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jt.core.model.OmsUser;
import com.jt.exception.CommonLogicException;
import com.jt.utils.CipherUtil;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;

@Controller
public class OmsLoginController {

	@Autowired
	private AccountService accountService;

	/**
	 * 进入登录界面
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(HttpSession session) throws Exception {
		return "login";
	}

	/**
	 * 用户登录
	 */
	@RequestMapping(value = { "login" }, method = RequestMethod.POST)
	public String login(String userName, String password, HttpSession session) throws CommonLogicException, Exception {
		AccountInfo accountInfo = new AccountInfo();
		OmsUser user = accountService.findByName(userName);
		if (user != null && user.getState() == 1 && StringUtils.equals(user.getPassword(), CipherUtil.encodeByMD5(password))) {
			List<Permission> permissions = Role.getPermissionsByRoleName(user.getRole());
			Role role = new Role();
			role.setName(user.getRole());
			role.setPermissions(permissions);
			
			accountInfo.setRole(role);
			accountInfo.setUserId(user.getUserId());
			accountInfo.setUserName(userName);
			accountInfo.setPhone(user.getPhone());
			accountInfo.setId(Integer.parseInt(user.getEmployeeID()));
			accountInfo.setCityCode(user.getCityCode());
			
			session.setAttribute("user", accountInfo);

			user.setLastLoginDate(new Date());
			accountService.save(user);

			String url = "";
			if (StringUtils.equals(accountInfo.getRole().getName(), Role.LICENSE_AUDITOR)) {
				url = "redirect:/wxmgt/audit";
			} else {
				url = "redirect:/insurance/policy";
			}
			return url;
		} else {
			return "redirect:/login";
		}
	}

	/**
	 * 退出登录
	 */
	@RequestMapping(value = { "logout" }, method = RequestMethod.GET)
	public String logout(String userId, HttpSession session) throws CommonLogicException, Exception {
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/login";
	}
}
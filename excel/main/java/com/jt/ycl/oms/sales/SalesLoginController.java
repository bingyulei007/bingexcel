/**
 * 
 */
package com.jt.ycl.oms.sales;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jt.core.model.OmsUser;
import com.jt.exception.CommonLogicException;
import com.jt.utils.CipherUtil;
import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.account.AccountService;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.auth.Role;

/**
 * @author wuqh
 * 
 */
@Controller
@RequestMapping("sales")
public class SalesLoginController {

	@Autowired
	private AccountService accountService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(HttpSession session) throws Exception {
		return "/sales/salesLogin";
	}

	/**
	 * 用户登录
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 * @throws CommonLogicException
	 */
	@RequestMapping(value = { "/login" }, method = RequestMethod.POST)
	public String login(String userName, String password, HttpServletResponse response, HttpSession session) throws CommonLogicException, Exception {
		AccountInfo accountInfo = new AccountInfo();
		OmsUser user = accountService.findByName(userName);
		if (user != null && user.getState() == 1 && CipherUtil.validatePassword(user.getPassword(), password)) {
			List<Permission> permissions = Role.getPermissionsByRoleName(user.getRole());
			Role role = new Role();
			role.setName(user.getRole());
			role.setPermissions(permissions);
			accountInfo.setRole(role);
			accountInfo.setUserName(userName);
			accountInfo.setPhone(user.getPhone());
			accountInfo.setId(Integer.parseInt(user.getEmployeeID()));
			accountInfo.setUserId(user.getUserId());
			accountInfo.setEmployeeID(user.getEmployeeID());
			user.setLastLoginDate(new Date());
			session.setAttribute("user", accountInfo);

			Cookie userCookie = new Cookie("salesId", accountInfo.getUserId());
			userCookie.setPath("/");
			userCookie.setMaxAge(-1);
			response.addCookie(userCookie);

			Cookie cookie2 = new Cookie("salesName", URLEncoder.encode(accountInfo.getUserName(), "UTF-8"));
			cookie2.setPath("/");
			cookie2.setMaxAge(-1);
			response.addCookie(cookie2);

			accountService.save(user);
			if (StringUtils.equals(accountInfo.getRole().getName(), Role.BD_USER) || StringUtils.equals(accountInfo.getRole().getName(), Role.OMS_MANAGER)) {
				return "redirect:/sales/index/enter";
			} else {
				return "redirect:/sales/login";
			}
		} else {
			return "redirect:/sales/login";
		}
	}

	/**
	 * 退出登录
	 * 
	 * @param userId
	 * @param session
	 * @return
	 * @throws CommonLogicException
	 * @throws Exception
	 */
	@RequestMapping(value = { "logout" }, method = RequestMethod.GET)
	public String logout(String userId, HttpSession session) throws CommonLogicException, Exception {
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/sales/salesLogin";
	}
}

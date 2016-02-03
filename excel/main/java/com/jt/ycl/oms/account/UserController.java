package com.jt.ycl.oms.account;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.core.model.Car;
import com.jt.core.model.User;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;
import com.jt.ycl.oms.car.CarService;
import com.jt.ycl.oms.coupon.OMSCouponService;

@Controller
@RequestMapping(value={"/user"})
@OMSPermission(permission = Permission.USER_MGMT)
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CarService carService;
	
	@Autowired
	private OMSCouponService omsCouponService;
	
	@RequestMapping(value="list", method=RequestMethod.GET)
	public String preList(){
		return "user/userList";
	}
	
	@RequestMapping(value="list", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap list(@RequestParam(required=false)String phone,@RequestParam(required=false)String startTime, 
			@RequestParam(required=false)String endTime, @RequestParam(required=false,defaultValue="0")int pageNumber, 
			@RequestParam(required=false,defaultValue="20")int pageSize){
		Date startDate = null;
		Date endDate = null;
		ModelMap mm = new ModelMap("retcode",0);
		if(StringUtils.isNotEmpty(startTime)){
			mm.put("startTime", startTime);
			startTime = startTime + " 00:00:00";
			startDate = DateUtils.convertStrToDate(startTime, "yyyy-MM-dd HH:mm:ss");
		}
		if(StringUtils.isNotEmpty(endTime)){
			mm.put("endTime", endTime);
			endTime = endTime + " 23:59:59";
			endDate = DateUtils.convertStrToDate(endTime, "yyyy-MM-dd HH:mm:ss");
		}
		
		Page<User> pageResult = userService.findUsers(phone, startDate, endDate, pageNumber, pageSize);
		
		mm.addAttribute("users", pageResult.getContent());
		mm.addAttribute("totalPages", pageResult.getSize());
		mm.addAttribute("totalItems", pageResult.getTotalElements());
		return mm;
	}
	
	@RequestMapping(value="{userId}/car", method=RequestMethod.GET)
	@ResponseBody
	public List<Car> getCarByUserId(@PathVariable String userId){
		return carService.getCarByUserId(userId);
	}
	
	@RequestMapping(value="{userId}/grant", method=RequestMethod.POST)
	@ResponseBody
	@OMSPermission(permission = Permission.GRANT_COUPON)
	public ModelMap grant(@PathVariable String userId, int number){
		ModelMap mm = new ModelMap();
		if(number > 0){
			omsCouponService.grantWashCarCoupons(userId, number);
			mm.put("result", "success");
		}else{
			mm.put("result", "failed");
		}
		return mm;
	}
	
	@RequestMapping(value="{userId}/checkticket", method=RequestMethod.GET)
	@ResponseBody
	@OMSPermission(permission = Permission.GRANT_COUPON)
	public ModelMap checkWashCarTicket(@PathVariable String userId){
		ModelMap mm = new ModelMap();
		int count = omsCouponService.checkValidWashCarTicket(userId);
		mm.put("count", count);
		return mm;
	}

}

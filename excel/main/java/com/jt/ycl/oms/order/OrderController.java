package com.jt.ycl.oms.order;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.core.model.Order;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * 用户订单管理
 * @author xiaojiapeng
 *
 */
@Controller
@RequestMapping(value="order")
@OMSPermission(permission = Permission.USER_ORDER_MGMT)
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@RequestMapping(value="list", method=RequestMethod.POST)
	@ResponseBody
	public ModelMap list(@RequestParam(required=false)String phone, @RequestParam(required=false)String merchantName, 
			@RequestParam(required=false,defaultValue="0")int cityCode, @RequestParam(required=false)String startTime, 
			@RequestParam(required=false)String endTime, @RequestParam(required=false,defaultValue="0")int pageNumber, 
			@RequestParam(required=false,defaultValue="20")int pageSize){
		OrderQueryCondition condition = new OrderQueryCondition();
		condition.setPhone(phone);
		condition.setCityCode(cityCode);
		condition.setMerchantName(merchantName);
		if(StringUtils.isNotEmpty(startTime)){
			condition.setStartTime(DateUtils.convertStrToDate(startTime + " 00:00:00", "yyyy-MM-dd HH:mm:ss"));
		}
		if(StringUtils.isNotEmpty(endTime)){
			condition.setEndTime(DateUtils.convertStrToDate(endTime + " 23:59:59", "yyyy-MM-dd HH:mm:ss"));
		}
		condition.setPageNumber(pageNumber);
		condition.setPageSize(pageSize);
		
		Page<Order> pageResult = orderService.findOrders(condition);
		
		ModelMap mm = new ModelMap("retcode",0);
		mm.addAttribute("orders", pageResult.getContent());
		mm.addAttribute("totalPages", pageResult.getSize());
		mm.addAttribute("totalItems", pageResult.getTotalElements());
		return mm;
	}
	
	@RequestMapping(value="list", method=RequestMethod.GET)
	public String preList(){
		return "order/orderList";
	}

}

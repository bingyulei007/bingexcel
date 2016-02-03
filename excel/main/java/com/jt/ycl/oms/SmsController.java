/**
 * 
 */
package com.jt.ycl.oms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.jt.core.SmsService;
import com.jt.core.dao.MerchantDao;
import com.jt.core.model.Merchant;

/**
 * @author Andy Cui
 */
@Controller
public class SmsController {

	private MerchantDao merchantDao;
	
	@Autowired
	private SmsService smsService;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@RequestMapping(value = "/sms/page", method = RequestMethod.GET)
	public ModelAndView sms() {
		return new ModelAndView("sms");
	}
	
	@RequestMapping(value = "/sms/1", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendSms1(String content) {
		ModelMap mm = new ModelMap();
		List<Merchant> merchants = merchantDao.findAll();
		int nums = 0;
		for(Merchant m : merchants) {
			if(m.isChexian()) {
				boolean result = smsService.send("42125", m.getManagerPhone(), new String[]{
					m.getManager(), content
				});
				if(result) {
					nums++;
					if (logger.isDebugEnabled()) {
						logger.debug("已发送短信给：{}", m.getManagerPhone());
					}
				}
			}
		}
		mm.addAttribute("nums", nums);
		return mm;
	}
	
	@RequestMapping(value = "/sms/2", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendSms2(String content) {
		ModelMap mm = new ModelMap();
		List<Merchant> merchants = merchantDao.findAll();
		int nums = 0;
		for(Merchant m : merchants) {
			if(m.isChexian()) {
				boolean result = smsService.send("42125", m.getManagerPhone(), new String[]{
					m.getManager(), content
				});
				if(result) {
					nums++;
					if (logger.isDebugEnabled()) {
						logger.debug("已发送短信给：{}", m.getManagerPhone());
					}
				}
			}
		}
		mm.addAttribute("nums", nums);
		return mm;
	}
	
	@RequestMapping(value = "/sms/3", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendSms3(String content) {
		ModelMap mm = new ModelMap();
		List<Merchant> merchants = merchantDao.findAll();
		int nums = 0;
		for(Merchant m : merchants) {
			if(m.isChexian()) {
				boolean result = smsService.send("42125", m.getManagerPhone(), new String[]{
					m.getManager(), content
				});
				if(result) {
					nums++;
					if (logger.isDebugEnabled()) {
						logger.debug("已发送短信给：{}", m.getManagerPhone());
					}
				}
			}
		}
		mm.addAttribute("nums", nums);
		return mm;
	}
	
	@RequestMapping(value = "/sms/4", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendSms4(String content) {
		ModelMap mm = new ModelMap();
		List<Merchant> merchants = merchantDao.findAll();
		int nums = 0;
		for(Merchant m : merchants) {
			if(m.isChexian()) {
				boolean result = smsService.send("42125", m.getManagerPhone(), new String[]{
					m.getManager(), content
				});
				if(result) {
					nums++;
					if (logger.isDebugEnabled()) {
						logger.debug("已发送短信给：{}", m.getManagerPhone());
					}
				}
			}
		}
		mm.addAttribute("nums", nums);
		return mm;
	}
	
	@RequestMapping(value = "/sms/5", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendSms5(String content) {
		ModelMap mm = new ModelMap();
		List<Merchant> merchants = merchantDao.findAll();
		int nums = 0;
		for(Merchant m : merchants) {
			if(m.isChexian()) {
				boolean result = smsService.send("42125", m.getManagerPhone(), new String[]{
					m.getManager(), content
				});
				if(result) {
					nums++;
					if (logger.isDebugEnabled()) {
						logger.debug("已发送短信给：{}", m.getManagerPhone());
					}
				}
			}
		}
		mm.addAttribute("nums", nums);
		return mm;
	}
	
	
	/**
	 * 发送通知卖车险的短信
	 * 
	 * @param content
	 */
	@RequestMapping(value = "/sms/test", method = RequestMethod.POST)
	@ResponseBody
	public ModelMap sendTestingSms(String phone, String content) {
		boolean result = smsService.send("16022", phone, new String[]{
			"测试短信", content
		});
		ModelMap mm = new ModelMap();
		if(result) {
			mm.addAttribute("nums", 1);
		} else {
			mm.addAttribute("nums", 0);
		}
		return mm;
	}
}

package com.jt.ycl.oms.coupon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.CouponCategory;
import com.jt.core.SmsService;
import com.jt.core.SmsTemplateId;
import com.jt.core.dao.CouponDao;
import com.jt.core.dao.UserDao;
import com.jt.core.model.Coupon;
import com.jt.core.model.User;
import com.jt.utils.DateUtils;

@Service
public class OMSCouponService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CouponDao couponDao;
	
	@Autowired
	private SmsService smsService;
	
	@Autowired
	private UserDao userDao;
	
	/**
	 * 专门给购买车险的用户发送洗车券
	 * @param userId
	 * @param num
	 */
	public void grantWashCarCoupons(String userId, int num){
		List<Coupon> coupons = new ArrayList<Coupon>();
		Date date = new Date();
		//1. 为投保本周也生成一张
		Date startDate = DateUtils.getFirstDayOfThisWeek(date);
		Date endDate = DateUtils.getLastDayOfWeek(startDate);
		String code = RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4);
		Coupon thisCoupon = new Coupon();
		thisCoupon.setCode(code);
		thisCoupon.setCategory(CouponCategory.WASH_CAR_COUPON);
		thisCoupon.setCreateDate(date);
		thisCoupon.setFaceValue(0);
		thisCoupon.setName("免费洗车券");
		thisCoupon.setType(1); //有尝赠送
		thisCoupon.setState(0); //未激活
		thisCoupon.setUserId(userId);
		thisCoupon.setStartDate(startDate);
		thisCoupon.setEndDate(endDate);
		thisCoupon.setRemark("全城通用");
		coupons.add(thisCoupon);
		
		Date newDate = new Date();
		//2. 生成除本周外剩余的洗车卷
		for (int i=1; i<num; i++) {
			newDate = DateUtils.getFirstDayOfNextWeek(newDate);
			endDate = DateUtils.getLastDayOfWeek(newDate);
			code = RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4) + "-" + RandomStringUtils.randomNumeric(4);
			Coupon coupon = new Coupon();
			coupon.setCode(code);
			coupon.setCategory(CouponCategory.WASH_CAR_COUPON);
			coupon.setCreateDate(date);
			coupon.setFaceValue(0);
			coupon.setName("免费洗车券");
			coupon.setType(1); //电子券
			coupon.setState(0); //未激活
			coupon.setUserId(userId);
			coupon.setStartDate(newDate);
			coupon.setEndDate(endDate);
			coupon.setRemark("全城通用");
			coupons.add(coupon);
		}
		couponDao.save(coupons);
		logger.info("发放{}张普通洗车券，用户ID = {}", num, userId);
		
		User user = userDao.findOne(userId);
		if(user != null){
			smsService.send(SmsTemplateId.SEND_WASH_CAR_TICKET, user.getPhone(), new String[]{"用户", num + ""});
		}
		
	}
	
	/**
	 * 检查有效的洗车券数量
	 * @param userId
	 * @return
	 */
	public int checkValidWashCarTicket(String userId){
		Date date = new Date();
		//1. 为投保本周也生成一张
		Date startDate = DateUtils.getFirstDayOfThisWeek(date);
		int count = couponDao.countValidCouponsByUserId(userId, startDate);
		return count;
	}

}

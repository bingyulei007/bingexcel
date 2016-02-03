package com.jt.ycl.oms.task;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jt.core.dao.SmsCodeDao;

/**
 * 自动删除前一天的短信验证记录
 * @author xiaojiapeng
 *
 */
@Component
@Transactional
public class DailyDeleteSmsCodeTask {

	@Autowired
	private SmsCodeDao smsCodeDao;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Scheduled(cron = "0 0 5 * * ?")//每天零晨5点运行
	public void doTask() {
		//删除昨天的所有短信验证码
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		
		logger.info("开始删除昨天短信...");
		int count = smsCodeDao.deleteYesterdayCode(c.getTime());
		logger.info("短信删除结束，删除：{} 条", count);
	}
}

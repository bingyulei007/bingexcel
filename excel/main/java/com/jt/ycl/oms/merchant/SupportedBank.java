/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Andy Cui
 */
public class SupportedBank {

	private static ArrayList<String> banks = new ArrayList<>();

	static {
		banks.add("中国农业银行");
		banks.add("中国工商银行");
		banks.add("中国建设银行");
		banks.add("中国邮政储蓄银行");
		banks.add("中国银行");
		banks.add("招商银行");
		banks.add("交通银行");
		banks.add("浦发银行");
		banks.add("中国光大银行");
		banks.add("中信银行");
		banks.add("平安银行");
		banks.add("中国民生银行");
		banks.add("华夏银行");
		banks.add("广发银行");
		banks.add("兴业银行");
		banks.add("上海银行");
		banks.add("北京银行");
		banks.add("苏州银行");
		banks.add("江苏银行");
		banks.add("南京银行");
		banks.add("宁波银行");
		banks.add("吴江农商行");
	}
	
	public static boolean supported(String name) {
		if(StringUtils.isBlank(name)) {
			return false;
		}
		for(String bankPrefix : banks) {
			if(name.startsWith(bankPrefix)) {
				return true;
			}
		}
		return false;
	}
}
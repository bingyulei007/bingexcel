package com.jt.ycl.oms.common.vo;

import com.jt.ycl.oms.common.util.excel.ExcelFieldConvertor;

public class ExpressFieldConvertor extends ExcelFieldConvertor {

	@Override
	public Object marshal(String fieldName, Object obj) {
		if (fieldName.equals("status")) {
			int status = Integer.parseInt(obj.toString());
			if (status == 0) {
				return "待提单";
			} else if (status == 1) {
				return "未提到";
			} else if (status == 2) {
				return "配送中";
			} else if (status == 3) {
				return "已退";
			} else if (status == 4) {
				return "配送完成";
			}
		} else if (fieldName.equals("payMode")) {
			int payMode = Integer.parseInt(obj.toString());
			if(payMode==1){
				return "保险公司POS机刷卡";
			}else if(payMode==2){
				return "网上支付";
			}else if(payMode==3){
				return "现金支付";
			}else if(payMode==4){
				return "支付宝";
			}else if(payMode==5){
				return "微信";
			}else if(payMode==6){
				return "拉卡拉";
			}else if(payMode==7){
				return "三维度";
			}else if(payMode==8){
				return "网银转账";
			}else if(payMode==9){
				return "其它";
			}else if(payMode==10){
				return "苏州易高刷卡";
			}
			
		} else if (fieldName.equals("companyCode")) {
			int companyCode = Integer.parseInt(obj.toString());
			if(companyCode==110){
				return "太平洋保险";
			}else if(companyCode==119){
				return "人保";
			}else if(companyCode==113){
				return "中国人寿财险";
			}else if(companyCode==127){
				return "紫金保险";
			}else if(companyCode==118){
				return "平安";
			}else if(companyCode==125){
				return "阳光保险";
			}else if(companyCode==123){
				return "太平保险";
			}
		}
		return obj;
	}

}

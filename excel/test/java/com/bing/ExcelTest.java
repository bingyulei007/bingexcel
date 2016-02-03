package com.bing;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.gson.Gson;
import com.jt.exception.CommonLogicException;
import com.jt.ycl.oms.insurance.vo.BrokerageBill;
import com.jt.ycl.oms.report.bean.ExpressVo;
import com.jt.ycl.oms.report.bean.PayWay;
import com.jt.ycl.oms.report.util.ExcelHanderUtil;

public class ExcelTest
{
	@Test
	public void  test4(){
		String arrStr="JTK0bce026abb31451294d44f1871b4040b,JTK9937717f27ba49dc8ff2a16c700c10a8";
		String[] split = StringUtils.split(arrStr, ",");
		for (String string : split) {
			System.out.println(string);
		}
		System.out.println(String.format("%tF",null));
		System.out.println(0.01*12);
	}
	@Test
	public void test3(){
		
		System.out.println(RandomStringUtils.randomNumeric(1));
		System.out.println(String.format("%tF", new Date()));
		System.out.println(new Date().getTime());
	}
	@Test
	public void test2(){
		String[] arr={"fee","payerCarNo","receiverbankName","receiverCarNo","receiverName","feeUse","receiverProvince","receiverCity","currency","channelCode","merchant","carNo","policyId","result"};
		File file=new File("F:/a.xls");
		List<BrokerageBill> list=null;
		try {
			 list = ExcelHanderUtil.readExcelToEntity(file, arr, BrokerageBill.class, 1);
			 Gson g=new Gson();
				System.out.println(g.toJson(list));
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | NoSuchFieldException | IllegalArgumentException
				| InvocationTargetException | ParseException | IOException e) {
			e.printStackTrace();
			
			
		}
	}
	@Test
	public void test1(){
		String[] colunmsName={"保单ID","配送状态","提单时间","完成日期","问题件描述","支付方式","快递单号","被保险人","车牌","保费合计","送单地址","联系电话","保险公司"};
		boolean b;
		
		File file=new File("F:/as.xlsx");
		
		
try {
			
			b = ExcelHanderUtil.validOrder(colunmsName, file);
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommonLogicException(1, e.getMessage());
		}
		if(!b){
			throw new CommonLogicException(1, "非标准数据表头");
		}
		System.out.println("表头验证通过");
		String[] colunms={"policyId","status","hasPolicyTime","finishedTime","desc","payWay","expressNo","insurant","carNumber","totlePremium","addr","phoneNum","insuranceCompany"};
		Class classType=ExpressVo.class;
		int fromRow=1;
		try {
			List list = ExcelHanderUtil.readExcelToEntity(file, colunms, classType, fromRow);
			System.out.println(list.size());
			Gson g=new Gson();
			System.out.println(g.toJson(list));
			
		} catch (InstantiationException | IllegalAccessException
				| NoSuchMethodException | SecurityException
				| NoSuchFieldException | IllegalArgumentException
				| InvocationTargetException | ParseException | IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		System.out.println(PayWay.class.isEnum());
	}
}

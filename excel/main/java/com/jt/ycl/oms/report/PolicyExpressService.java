package com.jt.ycl.oms.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.multipart.MultipartFile;

import com.jt.core.dao.InsurancePolicyDao;
import com.jt.core.dao.InsurancePolicyDaoImpl;
import com.jt.core.dao.PolicyFeeDao;
import com.jt.core.model.InsurancePolicy;
import com.jt.core.model.PolicyFee;
import com.jt.core.model.PolicyStatus;
import com.jt.exception.CommonLogicException;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.report.bean.ExpressStatus;
import com.jt.ycl.oms.report.bean.ExpressVo;
import com.jt.ycl.oms.report.bean.PayWay;
import com.jt.ycl.oms.report.bean.PolicyExpressBean;
import com.jt.ycl.oms.report.util.ExcelHanderUtil;
import com.jt.ycl.oms.report.util.ExpressFieldConvertor;
import com.jt.ycl.oms.report.util.FileUtil;

/**
 * @author wuqh
 */
@Service
@Transactional
public class PolicyExpressService {
	private String[] colunmsName = { "保单ID", "配送状态", "提单时间", "完成日期", "问题件描述", "支付方式", "快递单号", "被保险人", "车牌", "保费合计", "送单地址", "联系电话", "保险公司" };
	private String[] colunms = { "policyId", "status", "hasPolicyTime", "finishedTime", "desc", "payWay", "expressNo", "insurant", "carNumber", "totlePremium",
			"addr", "phoneNum", "insuranceCompany" };
	// 对应的是老吴写的<code>PolicyExpressBean</code>类
	private String[] outColunms = { "policyId", "status", "expressDate", "finishedDate", "desc", "payMode", "expressSerialNo", "insurant", "carNumber",
			"premium", "expressAddress", "phone", "companyCode" };

	@Autowired
	private InsurancePolicyDaoImpl insurancePolicyDaoImpl;
	
	@Autowired
	private PolicyFeeDao policyFeeDao;
	
	@Autowired
	private InsurancePolicyDao insurancePolicyDao;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 保单配送信息查询
	 * 
	 * @param licenseNo				车牌号
	 * @param cityCode				城市编码
	 * @param expressCompany		配送公司
	 * @param status				配送状态
	 * @param iccode				保险公司
	 * @param startDate				开始日期 
	 * @param endDate				结束日期
	 */
	public List<PolicyExpressBean> queryPolicyExpress(String licenseNo, int cityCode, int expressCompany, int status, String iccode, String startDate,
			String endDate) {
		List<PolicyExpressBean> policyExpressBeans = new ArrayList<PolicyExpressBean>();
		if (StringUtils.isEmpty(startDate) && StringUtils.isEmpty(endDate)) {
			endDate = DateUtils.convertDateToStr(new Date(), "yyyy-MM-dd");//如果不传日期，默认查询到当天23点59分59秒
		}
		List<Object[]> results = insurancePolicyDaoImpl.queryPolicyExpress(licenseNo, cityCode, expressCompany, status, iccode, startDate, endDate);
		for (Object[] objects : results) {
			PolicyExpressBean policyExpress = new PolicyExpressBean();
			policyExpress.setPolicyId(objects[0].toString());
			policyExpress.setCityName((String) objects[1]);
			policyExpress.setInsurant((String) objects[2]);
			policyExpress.setCarNumber(objects[3].toString());
			policyExpress.setPremium(Float.parseFloat(objects[4].toString()));// 应收保费
			policyExpress.setPhone((String) objects[5]);//收件人联系电话
			policyExpress.setCompanyCode(Integer.parseInt(objects[6].toString())); // 保险公司编码
			policyExpress.setStatus(Integer.parseInt(objects[8].toString())); // 状态
			policyExpress.setExpressDate(DateUtils.convertStrToDate(objects[9].toString(), "yyyy-MM-dd HH:mm:ss")); // 快递日期
			policyExpress.setExpressCompany(Integer.parseInt(objects[10].toString())); // 快递公司
			policyExpress.setExpressAddress((String) objects[11]);//配送地址
			policyExpress.setExpressSerialNo((String) objects[12]);//快递单号
			policyExpress.setPosFee(Float.parseFloat(objects[13].toString())); // 刷卡手续费
			policyExpress.setExpressCost(Float.parseFloat(objects[14].toString())); // 快递费
			policyExpress.setPayMode(Integer.parseInt(objects[15].toString())); // 支付方式
			policyExpress.setFinishedDate((Date) objects[16]);//配送完成日期
			policyExpress.setDesc((String) objects[17]);//问题件描述
			policyExpress.setRecipient((String) objects[18]);//收件人
			
			int days = DateUtils.difference(policyExpress.getExpressDate(), new Date());
			if(days <=2) {
				policyExpress.setRisk("正常");
			} else if(days == 3) {
				policyExpress.setRisk("3天，异常");
			} else {
				policyExpress.setRisk(days + "天，高危");
			}
			policyExpressBeans.add(policyExpress);
		}
		return policyExpressBeans;
	}

	/**
	 * 导入苏州易高的配送清单excel并解析，然后更新系统中保单的配送状态
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public int updatePolicyWuliuStatusFromSuzhouYGExcel(MultipartFile excel) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, InvocationTargetException, ParseException  {
		logger.info("开始导入苏州易高配送清单...");
		int totleRow = 0;
		Class classType = ExpressVo.class;
		String realPath = ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/");
		String path = realPath + "attachments" + File.separator + excel.getOriginalFilename();
		FileUtils.deleteDirectory(new File(realPath + "attachments"));
		File file = FileUtil.createFile(path);

		excel.transferTo(file);
		boolean b = ExcelHanderUtil.validOrder(colunmsName, file);
		if (!b) {
			throw new CommonLogicException(1, "Excel格式错误，非标准数据表头");
		}
		List<ExpressVo> list = ExcelHanderUtil.readExcelToEntity(file, colunms, classType, 1);
		FileUtils.deleteQuietly(file);
		logger.info("Excel记录总数：{}", list.size());
		
		List<PolicyFee> policyFeeList = policyFeeDao.findSuzhouYG();
		logger.info("待更新的保单（苏州易高负责）数量：{}", policyFeeList.size());
		for (PolicyFee policyFee : policyFeeList) {
			ExpressVo expressVo = queryFromList(policyFee.getPolicyId(), list);
			if (expressVo != null) {
				InsurancePolicy p = insurancePolicyDao.findOne(policyFee.getPolicyId());
				if(p == null) {
					throw new IllegalStateException("保单不存在，可能已经被删除，数据回滚，导入失败！保单ID：" + policyFee.getPolicyId());
				}
				if (expressVo.getStatus().equals(ExpressStatus.FINISHED) && (p.getStatus() == PolicyStatus.DELIVERING.value() || p.getStatus() == PolicyStatus.CHUDAN_FINISHED.value())) {
					// 如果配送完成，更改insurancePolicy状态为已配送
					p.setStatus(PolicyStatus.DISPATCHED.value());
					insurancePolicyDao.save(p);
				} else if(expressVo.getStatus().equals(ExpressStatus.IN_THE_WAY) || expressVo.getStatus().equals(ExpressStatus.NO_ACQUIRE)||expressVo.getStatus().equals(ExpressStatus.RETURNED)) {
					/*
					 * 如果物流状态为在途、未提到，更改InsurancePolicy状态为正在配送，未提到目前应该不会出现这种情况，都是从公司拿的
					 * 
					 */
					p.setStatus(PolicyStatus.DELIVERING.value());
					insurancePolicyDao.save(p);
				}else{
					throw new CommonLogicException(1, String.format("保单：%s配送状态非法",expressVo.getPolicyId()));
				}
				int status;
					status = convertorExpressStatus(expressVo.getStatus());
				
				int payMode=0;
				try {
					payMode = convertorPayWay(expressVo.getPayWay());
				} catch (Exception e) {
					throw new CommonLogicException(1, String.format("保单%s支付方式转换错误", expressVo.getPolicyId()));
				}
				if(status==4){
					if(payMode==0){
						throw new CommonLogicException(1, String.format("保单%s支付方式转换错误", expressVo.getPolicyId()));
					}
				}
				String _desc = policyFee.getDesc() == null ? "" : policyFee.getDesc();
				String _expressNo = policyFee.getExpressSerialNo() == null ? "" : policyFee.getExpressSerialNo();
				// 如果完全一样不更新
				if (policyFee.getStatus() == status && _desc.equals(expressVo.getDesc()) && policyFee.getPayMode() == payMode
						&& _expressNo.equals(expressVo.getExpressNo())) {
					if (policyFee.getFinishedTime() == null && expressVo.getFinishedTime() == null) {
						continue;
					}
					if (policyFee.getFinishedTime() != null) {
						if (policyFee.getFinishedTime().equals(expressVo.getFinishedTime())) {
							continue;
						}
					}
				}
				policyFee.setStatus(status);//配送状态
				policyFee.setDesc(expressVo.getDesc());//问题件描述
				policyFee.setFinishedTime(expressVo.getFinishedTime());//配送完成日期
				policyFee.setPayMode(payMode);//支付方式，现金or刷卡
				policyFee.setExpressSerialNo(expressVo.getExpressNo());//配送快递单号

				if (policyFee.getPayMode() == 10) {//10是苏州易高刷卡
					BigDecimal pos = new BigDecimal(p.getTotalPremium() * policyFee.getPosRate());
					policyFee.setPosFee(pos.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue());
					BigDecimal total = new BigDecimal(p.getTotalPremium());
					policyFee.setPremiumDueAmount(total.subtract(pos).floatValue());//实际收到的保费=总保费-刷卡手续费
				} else {
					policyFee.setPremiumDueAmount(p.getTotalPremium());
				}
				policyFeeDao.save(policyFee);
				logger.info("更新了{}的配送状态，当前物流状态：{}，问题件描述：{}，配送完成时间：{}，支付方式：{}，快递单号：{}", 
						p.getCarNumber(), expressVo.getStatus(), expressVo.getDesc(), 
						DateUtils.convertDateToStr(expressVo.getFinishedTime(), "yyyy-MM-dd"), expressVo.getPayWay(), expressVo.getExpressNo());
				totleRow++;
			}
		}
		if (totleRow > 0) {
			logger.info("本次更新{}条保单的配送状态", totleRow);
		} else {
			logger.info("数据无变化，本次无需更新.");
		}
		logger.info("苏州易高配送清单导入结束.");
		return totleRow;
	}

	private ExpressVo queryFromList(String id, List<ExpressVo> list) {
		for (ExpressVo vo : list) {
			if (vo.getPolicyId().equals(id)) {
				return vo;
			}
		}
		return null;
	}

	/**
	 * 配送状态 0待提单，默认； 1未提到； 2在途； 3已退； 4完成；
	 */
	private int convertorExpressStatus(ExpressStatus status) {
		if (status.equals(ExpressStatus.FINISHED)) {
			return 4;
		} else if (status.equals(ExpressStatus.IN_THE_WAY)) {
			return 2;
		} else if (status.equals(ExpressStatus.NO_ACQUIRE)) {
			return 1;
		} else if (status.equals(ExpressStatus.RETURNED)) {
			return 3;
		} else {
			throw new IllegalStateException("配送状态错误：" + status.toString());
		}
	}

	/**
	 * 保单支付方式
	 * 
	 * 1：保险公司POS机刷卡 2：网上支付，暂时用不到 3：现金支付，默认值 4：支付宝 5：微信 6：拉卡拉，手续费是千5 7：三维度 8：网银转账
	 * 9：其它 10：苏州易高刷卡，手续费是千3.8
	 * @throws IllegalAccessException 
	 */
	private int convertorPayWay(PayWay payWay) throws IllegalAccessException {
		if (payWay.equals(payWay.COD) || payWay.equals(payWay.MONEY)) {
			return 3;
		} else if (payWay.equals(payWay.PREPAY)) {
			return 8;
		} else if (payWay.equals(payWay.POS) || payWay.equals(payWay.POS2)) {
			return 10;
		} else {
			return 0;
		}
	}

	public OutputStream getResultExcelStream(int cityCode, int expressCompany, int status, String iccode, String startDate, String endDate) {
		List<PolicyExpressBean> list = queryPolicyExpress(null, cityCode, expressCompany, status, iccode, startDate, endDate);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = ExcelHanderUtil.createSheet(wb);
		sheet.setDefaultColumnWidth(20);
		ExcelHanderUtil.writeLineToSheet(wb, sheet, colunmsName, 0, true);

		try {
			ExcelHanderUtil.convertor = new ExpressFieldConvertor();
			ExcelHanderUtil.writeLinesToSheet(sheet, PolicyExpressBean.class, list, outColunms, 1);
		} catch (NoSuchFieldException | SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			logger.error("导出excel错误", e);
			throw new CommonLogicException(1, "服务器错误");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			wb.write(bos);
		} catch (IOException e) {
			logger.error("导出excel到输出流错误", e);
			throw new CommonLogicException(1, "服务器错误");
		}
		return bos;
	}

	public int changeToAcquire(String policyId) {
		
		PolicyFee policyFee = policyFeeDao.findOne(policyId);
		
		
		if(policyFee.getStatus()==0||policyFee.getStatus()==1){
			//表示保单拿到公司了
			policyFee.setStatus(5);
			policyFeeDao.save(policyFee);
			return 1;
		}if(policyFee.getStatus()==5){
			return 0;
		}else{
			throw new CommonLogicException(1, "更改前状态不合法");
		}
		
		
	}
}

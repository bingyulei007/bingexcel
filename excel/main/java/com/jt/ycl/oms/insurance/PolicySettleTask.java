package com.jt.ycl.oms.insurance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jt.core.dao.InsurancePolicyDaoImpl;
import com.jt.core.insurance.ICCode;
import com.jt.core.model.InsurancePolicy;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.mail.EmailService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * 保险公司对账单定时任务，按照城市、保险公司生成保单对账单，通过保单对账单和保险公司结算佣金
 * 
 * @author xiaojiapeng
 */
@Component
public class PolicySettleTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private EmailService emailService;

	@Autowired
	private InsurancePolicyDaoImpl policyDaoImpl;

	@Autowired
	private CityService cityService;

	private Configuration configuration = null;

	private Template t = null;

	public void doTask(Date startDate, Date endDate) throws Exception {
		Date now = new Date();
		String queryStartDate = DateUtils.convertDateToStr(startDate, "yyyy-MM-dd HH:mm:ss");
		String queryEndDate = DateUtils.convertDateToStr(endDate, "yyyy-MM-dd HH:mm:ss");
		logger.info("开始执行保险公司对账单任务，统计周期从{}到{}", queryStartDate, queryEndDate);

		// 生成的word文档保存的位置
		String dir = "D:\\account-statement";
		if (SystemUtils.IS_OS_LINUX) {
			dir = "/mnt/account-statement";
		}
		File path = new File(dir);
		if (!path.exists() || !path.isDirectory()) {
			path.mkdirs();
		}
		// 查询出所有的需要出对账单的保单
		List<InsurancePolicy> resultList = policyDaoImpl.getPolicyListForAccount(queryStartDate, queryEndDate);
		if (CollectionUtils.isEmpty(resultList)) {
			logger.warn("所选统计周期内无保单，任务退出！");
			return;
		} else {
			logger.info("统计周期内共：{} 张保单.", resultList.size());
		}

		// 将保单按照保险公司以及城市分组
		Map<Integer, Map<Integer, List<InsurancePolicy>>> groupMap = new HashMap<>();
		for (InsurancePolicy policy : resultList) {
			int companyCode = policy.getCompanyCode();
			int cityCode = policy.getCityCode();
			Map<Integer, List<InsurancePolicy>> companyMap = groupMap.get(companyCode);
			if (companyMap == null) {
				companyMap = new HashMap<>();
				List<InsurancePolicy> cityPolicyList = new ArrayList<>();
				cityPolicyList.add(policy);
				companyMap.put(cityCode, cityPolicyList);
				groupMap.put(companyCode, companyMap);
			} else {
				List<InsurancePolicy> cityPolicyList = companyMap.get(cityCode);
				if (cityPolicyList == null) {
					cityPolicyList = new ArrayList<>();
					cityPolicyList.add(policy);
					companyMap.put(cityCode, cityPolicyList);
				} else {
					cityPolicyList.add(policy);
				}
			}
		}

		if (MapUtils.isNotEmpty(groupMap)) {
			init();
			String generateDate = DateUtils.convertDateToStr(now, "yyyy年MM月dd日");
			String startDateStr = DateUtils.convertDateToStr(startDate, "yyyy-MM-dd");
			String endDateStr = DateUtils.convertDateToStr(endDate, "yyyy-MM-dd");
			String statisticalDate = startDateStr + "至" + endDateStr;
			String accountDate = DateUtils.convertDateToStr(startDate, "yyyy年MM月");
			// 生成的所有的文件
			List<File> files = new ArrayList<>();
			for (Map.Entry<Integer, Map<Integer, List<InsurancePolicy>>> entry : groupMap.entrySet()) {
				int companyCode = entry.getKey();
				String companyName = ICCode.getICNameByCode(companyCode);
				Map<Integer, List<InsurancePolicy>> companyMap = entry.getValue();
				for (Map.Entry<Integer, List<InsurancePolicy>> companyEntry : companyMap.entrySet()) {
					int cityCode = companyEntry.getKey();
					String cityName = cityService.getByCityCode(cityCode).getName();
					List<InsurancePolicy> cityPolicyList = companyEntry.getValue();
					String company = companyName + cityName + "分公司";
					String fileName = dir + File.separator + cityName + companyName + accountDate + "对账单.doc";
					// 商业险、交强险、总保费
					BigDecimal bPremiumTotal = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal cPremiumTotal = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal carShipTaxTotal = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal premiumTotal = new BigDecimal(0).setScale(2, BigDecimal.ROUND_HALF_UP);
					int count = cityPolicyList.size();
					List<Map<String, Object>> policyList = new ArrayList<>();
					for (InsurancePolicy policy : cityPolicyList) {
						BigDecimal premium = new BigDecimal(policy.getSumPremium()).setScale(2, BigDecimal.ROUND_HALF_UP);
						BigDecimal premiumCI = new BigDecimal(policy.getSumPremiumCI()).setScale(2, BigDecimal.ROUND_HALF_UP);
						BigDecimal carShipTax = new BigDecimal(policy.getCarShipTax()).setScale(2, BigDecimal.ROUND_HALF_UP);
						bPremiumTotal = bPremiumTotal.add(premium);
						cPremiumTotal = cPremiumTotal.add(premiumCI);
						carShipTaxTotal = carShipTaxTotal.add(carShipTax);
						Map<String, Object> policyMap = new HashMap<>();
						if (StringUtils.isEmpty(policy.getInsurant())) {
							policyMap.put("insurant", policy.getOwner());
						} else {
							policyMap.put("insurant", policy.getInsurant());
						}
						policyMap.put("carNumber", policy.getCarNumber());
						policyMap.put("createDate", DateUtils.convertDateToStr(policy.getCreateDate(), "yyyy-MM-dd"));
						policyMap.put("bTotal", premium.floatValue() == 0 ? "-" : premium.toString());
						policyMap.put("cTotal", premiumCI.floatValue() == 0 ? "-" : premiumCI.toString());
						policyMap.put("total", premium.add(premiumCI).add(carShipTax).toString());
						policyList.add(policyMap);

					}

					premiumTotal = premiumTotal.add(bPremiumTotal).add(cPremiumTotal).add(carShipTaxTotal);
					Map<String, Object> dataMap = new HashMap<>();
					dataMap.put("policyList", policyList);
					dataMap.put("generateDate", generateDate);
					dataMap.put("startDate", startDateStr);
					dataMap.put("endDate", endDateStr);
					dataMap.put("statisticalDate", statisticalDate);
					dataMap.put("count", count);
					dataMap.put("company", company);
					dataMap.put("bPremiumTotal", bPremiumTotal.toString());
					dataMap.put("cPremiumTotal", cPremiumTotal.toString());
					dataMap.put("premiumTotal", premiumTotal.toString());

					// 生成word文件
					File outFile = new File(fileName);
					Writer out = null;
					FileOutputStream fos = new FileOutputStream(outFile);
					OutputStreamWriter oWriter = new OutputStreamWriter(fos, "UTF-8");
					// 这个地方对流的编码不可或缺，使用main（）单独调用时，应该可以，但是如果是web请求导出时导出后word文档就会打不开，并且包XML文件错误。主要是编码格式不正确，无法解析。
					out = new BufferedWriter(oWriter);
					t.process(dataMap, out);
					out.close();
					fos.close();
					files.add(outFile);
				}
			}

			File file = doZip(dir, dir + File.separator + "policy_settlement.zip");
			// 发送邮件
			emailService.sendMimeMail(new String[] { "finance@ykcare.cn", "liuh@ykcare.cn", "cuiwj@ykcare.cn", "lubq@ykcare.cn" }, "保险公司" + accountDate
					+ "对账单", "保险公司对账单，请勿回复邮件！", new String[] { "policy_settlement.zip" }, file);
			file.delete();
			for (File f : files) {
				f.delete();
			}
		}
		logger.info("保险公司对账单任务执行结束...");
	}

	private void init() throws Exception {
		configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setDefaultEncoding("UTF-8");
		configuration.setClassForTemplateLoading(this.getClass(), "/");
		t = configuration.getTemplate("template.ftl"); // 文件名
	}

	private File doZip(String sourceDir, String zipFilePath) throws IOException {
		File file = new File(sourceDir);
		File zipFile = new File(zipFilePath);
		ZipOutputStream zos = null;
		try {
			// 创建写出流操作
			OutputStream os = new FileOutputStream(zipFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			zos = new ZipOutputStream(bos);

			String basePath = null;

			// 获取目录
			if (file.isDirectory()) {
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}

			zipFile(file, basePath, zos);
		} finally {
			if (zos != null) {
				zos.closeEntry();
				zos.close();
			}
		}
		return zipFile;
	}

	private void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException {
		File[] files = null;
		if (source.isDirectory()) {
			files = source.listFiles();
		} else {
			files = new File[1];
			files[0] = source;
		}
		InputStream is = null;
		String pathName;
		byte[] buf = new byte[1024];
		int length = 0;
		try {
			for (File file : files) {
				if (StringUtils.equals("policy_settlement.zip", file.getName())) {
					continue;
				}
				if (file.isDirectory()) {
					pathName = file.getPath().substring(basePath.length() + 1) + "/";
					zos.putNextEntry(new ZipEntry(pathName));
					zipFile(file, basePath, zos);
				} else {
					pathName = file.getPath().substring(basePath.length() + 1);
					is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					zos.putNextEntry(new ZipEntry(pathName));
					while ((length = bis.read(buf)) > 0) {
						zos.write(buf, 0, length);
					}
					bis.close();
				}
			}
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
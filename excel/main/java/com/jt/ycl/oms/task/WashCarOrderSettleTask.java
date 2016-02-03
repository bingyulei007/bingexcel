package com.jt.ycl.oms.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.dao.MerchantDao;
import com.jt.core.dao.OrderDao;
import com.jt.core.model.City;
import com.jt.core.model.Merchant;
import com.jt.core.model.Order;
import com.jt.core.model.OrderState;
import com.jt.utils.DateUtils;
import com.jt.ycl.oms.city.CityService;
import com.jt.ycl.oms.mail.EmailService;

/**
 * 生成洗车订单结算excel，根据结算单付款给商家。每个月5号零晨2点开始执行。
 */
@Component
public class WashCarOrderSettleTask {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private OrderDao orderDao;
	
	@Autowired
	private MerchantDao merchantDao;
	
	@Autowired
	private CityService cityService;
	
	private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private String bucketName = "washcar-order-excel";

	/**
	 * 按月统计，每月5号凌晨2点开始执行任务，生成上月的对账
	 */
//	@Scheduled(cron = "0 0 2 5 * ?")
	@Scheduled(cron = "0 0 2 * * ?")//每天零晨2点运行
	public void verifyOrdersByMonth() throws Exception {
		Date currentDate = new Date();
		String fromDate = DateUtils.convertDateToStr(DateUtils.getBeginTimeOfLastMonth(currentDate), "yyyy-MM-dd");
		String endDate = DateUtils.convertDateToStr(DateUtils.getEndTimeOfLastMonth(currentDate), "yyyy-MM-dd");
		logger.info("开始统计洗车订单：从 {} 到 {}", fromDate, endDate);
		//1. 读取所有商家，包括商家的开户行、银行帐户名、银行卡号、所在省份、所在城市
		List<Merchant> merchantList = merchantDao.findAll();
		if(logger.isInfoEnabled()) {
			logger.info("商家总数：" + merchantList.size());
		}
		/*
		 * 2. 对商家做一下归类，考虑到有些商家是连锁的，用的是同一个银行卡号，因此汇总这些商家的货款，作为一笔进行结算，
		 * 假如有1000家商家，那么结算记录小于等于1000 
		 */
		Map<Long, PaymentRecord> mapping = new HashMap<>();//key是商家ID，value是商家结算记录
		Map<String, PaymentRecord> tempMap = new HashMap<>();//key是银行卡号
		
		for(Merchant merchant : merchantList) {
			if(tempMap.get(merchant.getBankCard()) == null) {
				PaymentRecord record = new PaymentRecord();
				record.setBank(merchant.getBank());
				record.setBankAcountName(merchant.getBankAcountName());
				record.setBankCard(merchant.getBankCard());
				record.setProvince(merchant.getProvince());
				City city = cityService.getByCityCode(merchant.getCityCode());
				if(city != null){
					record.setCity(city.getName());
				}
				record.setSerialNo(UUID.randomUUID().toString().replace("-", ""));//结算序列号
				
				tempMap.put(merchant.getBankCard(), record);
			}
			mapping.put(merchant.getId(), tempMap.get(merchant.getBankCard()));
		}
		/*
		 * 3. 读取上个月所有的洗车订单，当数据量大了以后，这张表的查询效率（查询速度、内存占用）需要考虑，
		 * 假如1个月有50万洗车订单，那么就会查询出50万条记录。
		 */
		List<Order> orders = orderDao.findByFinishedDateBetween(DateUtils.getBeginTimeOfLastMonth(currentDate), 
										DateUtils.getEndTimeOfLastMonth(currentDate));
		if(logger.isInfoEnabled()) {
			logger.info("上月洗车订单总数：{}", orders.size());
		}
		for(Order order : orders) {
			if(order.getState() == OrderState.CONSUMED_AND_APPRAISED || order.getState() == OrderState.CONSUMED_NO_APPRAISED) {
				PaymentRecord record = mapping.get(order.getMerchantId());
				record.setAmount(record.getAmount() + order.getPrice());
			}
		}
		List<PaymentRecord> records = new ArrayList<>(mapping.values());
		createExcel(records, DateUtils.convertDateToStr(DateUtils.getBeginTimeOfLastMonth(currentDate), "yyyy年MM月"));
		/*
		 * 逐条更新这些订单的结算序列号，假如有50万条订单，如何快速更新?
		 */
		for(Order order : orders) {
			order.setSettlementState(2);
			order.setSettlementId(mapping.get(order.getMerchantId()).getSerialNo());
		}
		orderDao.save(orders);
		if(logger.isInfoEnabled()) {
			logger.info("订单状态、结算流水号更新完成.");
		}
		logger.info("洗车订单结算统计结束.");
	}

	private void createExcel(List<PaymentRecord> records, String fromDate) throws Exception {
		StringBuilder sb = new StringBuilder(fromDate);
		sb.append("_对账单");
		sb.append(".xls");
		String fileName = sb.toString();
		
		String dir = "D:\\merchant-payment";
		if (SystemUtils.IS_OS_LINUX) {
			dir = "/mnt/merchant-payment";
		}
		File path = new File(dir);
		if (!path.exists() || !path.isDirectory()) {
			path.mkdirs();
		}
		String filePath = dir + File.separator + fileName;
		File f = new File(filePath);
		if(f.exists()) {
			f.delete();
		}
		Workbook wb = new HSSFWorkbook();
		CreationHelper helper = wb.getCreationHelper();
		Sheet sheet1 = wb.createSheet("sheet1");
		Row row = null;
		Cell cell = null;

		row = sheet1.createRow(0);
		CellStyle cellStyle = createStyleCell(wb);
		cellStyle.setFont(createFonts(wb));
		for (int i = 0; i < 11; i++) {
			cell = row.createCell(i);
			cell.setCellStyle(cellStyle);
			if (i == 0) {
				cell.setCellStyle(cellStyle);
				cell.setCellValue("金额");
			}
			if (i == 1) {
				cell.setCellValue("付款人账号");
			}
			if (i == 2) {
				cell.setCellValue("收款人开户行名称");
			}
			if (i == 3) {
				cell.setCellValue("收款人账号");
			}
			if (i == 4) {
				cell.setCellValue("收款人名称");
			}
			if (i == 5) {
				cell.setCellValue("汇款用途");
			}
			if (i == 6) {
				cell.setCellValue("收款方所在省份");
			}
			if (i == 7) {
				cell.setCellValue("收款方所在城市");
			}
			if (i == 8) {
				cell.setCellValue("币种");
			}
			if (i == 9) {
				cell.setCellValue("ERP序号");
			}
			if (i == 10) {
				cell.setCellValue("预约时间");
			}
		}
		int rowIndex = 0;
		for (int index = 0, size = records.size(); index < size;index++) {
			PaymentRecord vo = records.get(index);
			if(vo.getAmount() <= 0) {
				continue;
			}
			row = sheet1.createRow(++rowIndex);
			for (int i = 0; i < 11; i++) {
				cell = row.createCell(i);
				cell.setCellStyle(cellStyle);
				if (i == 0) {
					cellStyle = setCellFormat(helper, cellStyle, "#,##0.00");
					cell.setCellStyle(cellStyle);
					cell.setCellValue(vo.getAmount());
				}
				if (i == 1) {
					cell.setCellValue("325605000018010344864");
				}
				if (i == 2) {
					cell.setCellValue(vo.getBank());
				}
				if (i == 3) {
					cell.setCellValue(vo.getBankCard());
				}
				if (i == 4) {
					cell.setCellValue(vo.getBankAcountName());
				}
				if (i == 5) {
					cell.setCellValue("货款");
				}
				if (i == 6) {
					cell.setCellValue(vo.getProvince());
				}
				if (i == 7) {
					cell.setCellValue(vo.getCity());
				}
				if (i == 8) {
					cell.setCellValue("人民币");
				}
			}
		}
		for (int i = 0; i < 11; i++) {
			sheet1.autoSizeColumn(i, true);
		}
		OutputStream os = new FileOutputStream(filePath);
		wb.write(os);
		os.close();
		File file = new File(filePath);
		emailService.sendMimeMail(new String[] { "finance@ykcare.cn", "liuh@ykcare.cn","cuiwj@ykcare.cn" }, "有空养车-洗车结算" + fileName, 
				"这是一封和渠道商进行洗车订单结算的自动通知邮件，请查收附件，请勿回复邮件！", new String[]{fileName}, file);
		try{
			upload(filePath);
			logger.info("洗车订单结算文件已上传至阿里云.");
			//上传到阿里云后，将服务器本地的文件删除掉
			if(file.exists()){
				file.delete();
			}
		}catch (Exception ex){
			logger.error("上传洗车订单结算文件到阿里云失败", ex);
		}
		logger.info("生成结算文件：" + filePath);
	}
	
	/**
	 * 上传到阿里云
	 * @param filePath
	 * @throws IOException
	 */
	private void upload(String filePath) throws IOException{
		OSSClient client = null;
		FileInputStream in = null;
		try {
			String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
			File file = new File(filePath);
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
			
			//判断是否存在，如果存在，则删除原有的
			if(client.doesObjectExist(bucketName, fileName)){
        		client.deleteObject(bucketName, fileName);
        	}
			
        	ObjectMetadata meta = new ObjectMetadata();
        	// 必须设置ContentLength
        	meta.setContentLength(file.length());
        	in = new FileInputStream(file);
        	client.putObject(bucketName, fileName, in, meta);
		}finally {
		    in.close();
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}
	
	public static CellStyle createStyleCell(Workbook wb) {
		CellStyle cellStyle = wb.createCellStyle();
		// 设置一个单元格边框颜色
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		// 设置一个单元格边框颜色
		cellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		cellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return cellStyle;
	}

	/**
	 * 设置文字在单元格里面的位置 CellStyle.ALIGN_CENTER CellStyle.VERTICAL_CENTER
	 */
	public static CellStyle setCellStyleAlignment(CellStyle cellStyle, short halign, short valign) {
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		return cellStyle;
	}

	/**
	 * 格式化单元格 如#,##0.00,m/d/yy去HSSFDataFormat或XSSFDataFormat里面找
	 * 
	 * @param cellStyle
	 * @param fmt
	 * @return
	 */
	public static CellStyle setCellFormat(CreationHelper helper, CellStyle cellStyle, String fmt) {
		cellStyle.setDataFormat(helper.createDataFormat().getFormat(fmt));
		return cellStyle;
	}

	/**
	 * 前景和背景填充的着色
	 */
	public static CellStyle setFillBackgroundColors(CellStyle cellStyle, short bg, short fg, short fp) {
		cellStyle.setFillForegroundColor(fg);
		cellStyle.setFillPattern(fp);
		return cellStyle;
	}

	/**
	 * 设置字体
	 */
	public static Font createFonts(Workbook wb) {
		Font font = wb.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);
		return font;
	}
}
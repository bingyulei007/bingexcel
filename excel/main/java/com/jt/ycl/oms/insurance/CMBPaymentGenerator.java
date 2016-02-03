/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jt.utils.DateUtils;
import com.jt.ycl.oms.mail.EmailService;
import com.jt.ycl.oms.task.PaymentRecord;

/**
 * @author Andy Cui
 */
@Component
public class CMBPaymentGenerator {
	
	@Autowired
	private EmailService emailService;
	
	public void execute(List<PaymentRecord> records, String fileName) throws Exception {
		String dir = "D:\\channel-commission-excel";
		if (SystemUtils.IS_OS_LINUX) {
			dir = "/mnt/channel-commission-excel";
		}
		File path = new File(dir);
		if (!path.exists() || !path.isDirectory()) {
			path.mkdirs();
		}
		String filePath = dir + File.separator + fileName;
		File f = new File(filePath);
		if (f.exists()) {
			f.delete();
		}
		Workbook wb = new HSSFWorkbook();
		CreationHelper helper = wb.getCreationHelper();
		Sheet sheet1 = wb.createSheet("佣金账单");
		Row row = null;
		Cell cell = null;

		row = sheet1.createRow(0);
		CellStyle cellStyle = createStyleCell(wb);
		cellStyle.setFont(createFonts(wb));
		for (int i = 0; i < 25; i++) {
			cell = row.createCell(i);
			cell.setCellStyle(cellStyle);
			if (i == 0) {
				cell.setCellStyle(cellStyle);
				cell.setCellValue("业务参考号");
			}
			if (i == 1) {
				cell.setCellValue("收款人编号");
			}
			if (i == 2) {
				cell.setCellValue("收款人账号");
			}
			if (i == 3) {
				cell.setCellValue("收款人名称");
			}
			if (i == 4) {
				cell.setCellValue("收方开户支行");
			}
			if (i == 5) {
				cell.setCellValue("收款人所在省");
			}
			if (i == 6) {
				cell.setCellValue("收款人所在市");
			}
			if (i == 7) {
				cell.setCellValue("收方邮件地址");
			}
			if (i == 8) {
				cell.setCellValue("收方移动电话");
			}
			if (i == 9) {
				cell.setCellValue("币种");
			}
			if (i == 10) {
				cell.setCellValue("付款分行");
			}
			if (i == 11) {
				cell.setCellValue("结算方式");
			}
			if(i == 12) {
				cell.setCellValue("业务种类");
			}
			if(i == 13) {
				cell.setCellValue("付方帐号");
			}
			if(i == 14) {
				cell.setCellValue("期望日");
			}
			if(i == 15) {
				cell.setCellValue("期望时间");
			}
			if(i == 16) {
				cell.setCellValue("用途");
			}
			if(i == 17) {
				cell.setCellValue("金额");
			}
			if(i == 18) {
				cell.setCellValue("收方行号");
			}
			if(i == 19) {
				cell.setCellValue("收方开户银行");
			}
			if(i == 20) {
				cell.setCellValue("业务摘要");
			}
			if(i == 21) {
				cell.setCellValue("渠道商户");
			}			
			if(i == 22) {
				cell.setCellValue("结算车辆");
			}			
			if(i == 23) {
				cell.setCellValue("保单ID");
			}
			if(i == 24) {
				cell.setCellValue("转账结果");
			}
		}
		for (int index = 0, size = records.size(); index < size;) {
			PaymentRecord vo = records.get(index);
			row = sheet1.createRow(++index);
			for (int i = 0; i < 25; i++) {
				cell = row.createCell(i);
				cell.setCellStyle(cellStyle);
				if (i == 0) {
					cell.setCellValue("");
				}
				if (i == 1) {
					cell.setCellValue(vo.getChannelCode());
				}
				if (i == 2) {
					cell.setCellValue(vo.getBankCard());
				}
				if (i == 3) {
					cell.setCellValue(vo.getBankAcountName());
				}
				if (i == 4) {
					cell.setCellValue(vo.getBank());
				}
				if (i == 5) {
					cell.setCellValue(vo.getProvince());
				}
				if (i == 6) {
					cell.setCellValue(vo.getCity());
				}
				if (i == 7) {
					cell.setCellValue("");//收方邮件地址
				}
				if (i == 8) {
					cell.setCellValue("");//收方移动电话
				}
				if (i == 9) {//币种
					cell.setCellValue("人民币");
				}
				if (i == 10) {
					cell.setCellValue("苏州");
				}
				if (i == 11) {
					cell.setCellValue("快速");
				}
				if (i == 12) {
					cell.setCellValue("普通汇兑");
				}
				if (i == 13) {
					cell.setCellValue("512905666410101");
				}
				if (i == 14) {
					cell.setCellValue(DateUtils.convertDateToStr(new Date(), "yyyyMMdd"));
				}
				if (i == 15) {
					cell.setCellValue("010000");
				}
				if (i == 16) {
					cell.setCellValue("业务佣金");
				}
				if (i == 17) {
//					CellStyle cellStyle2 = setCellFormat(helper, cellStyle, "#,##0.00");
//					cell.setCellStyle(cellStyle2);
					cell.setCellValue(vo.getAmount());
				}
				if (i == 18) {//收方行号
					cell.setCellValue("");
				}
				if (i == 19) {//收方开户银行
					cell.setCellValue("");
				}
				if (i == 20) {
					cell.setCellValue("有空养车佣金");
				}
				if (i == 21) {
					cell.setCellValue(vo.getChannelName());
				}
				if (i == 22) {
					cell.setCellValue(vo.getRemark());
				}
				if (i == 23) {
					cell.setCellValue(vo.getPolicyId());
				}
				if (i == 24) {
					cell.setCellValue("OK");
				}
			}
		}
		for (int i = 0; i < 25; i++) {
			sheet1.autoSizeColumn(i, true);
		}
		File file = new File(filePath);
		// 删除掉原有的
		if (file.exists()) {
			file.delete();
		}
		OutputStream os = new FileOutputStream(filePath);
		wb.write(os);
		os.close();

		emailService.sendMimeMail(new String[] { "cuiwj@ykcare.cn","liuh@ykcare.cn" }, "policy-settle-" + fileName,
				"这是一封和渠道商进行保费佣金结算的自动通知邮件，请查收附件，请勿回复邮件！", new String[] { fileName }, file);
	}

	private CellStyle createStyleCell(Workbook wb) {
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
	 * 格式化单元格 如#,##0.00,m/d/yy去HSSFDataFormat或XSSFDataFormat里面找
	 * 
	 * @param cellStyle
	 * @param fmt
	 * @return
	 */
	private CellStyle setCellFormat(CreationHelper helper, CellStyle cellStyle, String fmt) {
		cellStyle.setDataFormat(helper.createDataFormat().getFormat(fmt));
		return cellStyle;
	}

	/**
	 * 设置字体
	 */
	private Font createFonts(Workbook wb) {
		Font font = wb.createFont();
		font.setFontName("黑体");
		font.setFontHeightInPoints((short) 12);
		return font;
	}
}

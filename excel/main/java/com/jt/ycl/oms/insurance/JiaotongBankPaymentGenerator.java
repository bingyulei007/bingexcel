/**
 * 
 */
package com.jt.ycl.oms.insurance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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

import com.jt.ycl.oms.mail.EmailService;
import com.jt.ycl.oms.task.PaymentRecord;

/**
 * @author Andy Cui
 */
@Component
public class JiaotongBankPaymentGenerator {
	
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
		for (int i = 0; i < 14; i++) {
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
				cell.setCellValue("渠道商编码");
			}
			if (i == 10) {
				cell.setCellValue("渠道商");
			}
			if (i == 11) {
				cell.setCellValue("结算车辆");
			}
			if(i == 12) {
				cell.setCellValue("保单ID");
			}
			if(i == 13) {
				cell.setCellValue("转账结果");
			}
		}
		for (int index = 0, size = records.size(); index < size;) {
			PaymentRecord vo = records.get(index);
			row = sheet1.createRow(++index);
			for (int i = 0; i < 14; i++) {
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
				if (i == 9) {
					cell.setCellValue(vo.getChannelCode());
				}
				if (i == 10) {
					cell.setCellValue(vo.getChannelName());
				}
				if (i == 11) {
					cell.setCellValue(vo.getRemark());
				}
				if (i == 12) {
					cell.setCellValue(vo.getPolicyId());
				}
				if (i == 13) {
					cell.setCellValue("OK");
				}
			}
		}
		for (int i = 0; i < 14; i++) {
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

		emailService.sendMimeMail(new String[] {"cuiwj@ykcare.cn" }, "policy-settle-" + fileName,
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

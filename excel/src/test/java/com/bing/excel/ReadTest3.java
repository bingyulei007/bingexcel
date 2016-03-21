package com.bing.excel;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.bing.excel.annotation.BingConvertor;
import com.bing.excel.annotation.CellConfig;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.converter.base.BooleanFieldConverter;
import com.bing.excel.core.ExcelBing;
import com.bing.excel.core.ExcelBingBuilder;
import com.bing.excel.core.impl.ExcelBingImpl.SheetVo;
import com.bing.utils.StringParseUtil;
import com.google.common.base.MoreObjects;

public class ReadTest3 {

	@Test
	public void readExcelTest() throws URISyntaxException {
		// InputStream in = Person.class.getResourceAsStream("/person.xlsx");
		URL url = Salary.class.getResource("/salary.xlsx");
		File f = new File(url.toURI());

		ExcelBing bing = ExcelBingBuilder.toBuilder().builder();
		try {
			SheetVo<Salary> vo = bing.readFile(f, Salary.class, 1);
			System.out.println(vo.getSheetIndex());
			System.out.println(vo.getSheetName());
			List<Salary> objectList = vo.getObjectList();
			for (Salary salary : objectList) {
				System.out.println(salary);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	enum Department {
		develop, personnel, product;
	}

	public static class Salary {

		@CellConfig(index = 1)
		private String employNum;

		@CellConfig(index = 0)
		private String id;

		@CellConfig(index = 8)
		@BingConvertor(value = BooleanFieldConverter.class, strings = { "1","0" }, booleans = { false })
		private boolean allDay;

		@CellConfig(index=7)
		private Department department;//枚举类型
		
		
		@CellConfig(index = 13)
		@BingConvertor(DateTestConverter.class)
		// 自定义转换器
		private Date atypiaDate;
		@CellConfig(index = 15)
		@BingConvertor(DateTestConverter.class)
		// 自定义转换器
		private Date entryTime;

		// 其他变量可以这样定义。
		private transient String test;

		public String toString() {
			return MoreObjects.toStringHelper(this.getClass()).omitNullValues()
					.add("id", id).add("employNum", employNum)
					.add("allDay", allDay)
					.add("atypiaDate", atypiaDate)
					.add("department", department)
					.add("entryTime", entryTime).toString();
		}
	}

	public static class DateTestConverter implements FieldValueConverter {

		@Override
		public void toObject(Object source) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean canConvert(Class<?> clz) {
			return clz.equals(Date.class);
		}

		@Override
		public Object fromString(String cell) {

			if (StringUtils.isBlank(cell)) {
				return null;
			}
			try {
				return StringParseUtil.convertYMDT2Date(cell);
			} catch (ParseException e) {

				throw new RuntimeException(e);
			}
		}

	}
}

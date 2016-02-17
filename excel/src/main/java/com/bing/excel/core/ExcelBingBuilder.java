package com.bing.excel.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bing.common.Builder;
import com.bing.excel.core.impl.ExcelBingImpl;

/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <code>BingExcel</code>的构造类，可以添加自定义转换器等。<／p>
 * <p>
 * Company: bing<／p>
 * 
 * @author zhongtao.shi
 * @date 2015-12-8
 */
/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Company: bing<／p>
 * 
 * @author zhongtao.shi
 * @date 2015-12-8
 */
public class ExcelBingBuilder implements Builder<ExcelBing> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * bingExcel:对应的excel工具类。
	 */
	private ExcelBing bingExcel;

	/**
	 * <p>
	 * Title: <／p>
	 * <p>
	 * Description: 构造新的builder对象<／p>
	 */
	private ExcelBingBuilder() {
		
	}

	public static Builder<ExcelBing> toBuilder() {
		
		return new ExcelBingBuilder();
		
	}

	@Override
	public ExcelBing builder() {
		if(bingExcel==null){
			//bingExcel=new ExcelBingImpl();
		}
		return this.bingExcel;
	}

}

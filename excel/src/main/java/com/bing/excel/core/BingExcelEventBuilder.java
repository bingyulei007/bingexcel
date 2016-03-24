package com.bing.excel.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bing.common.Builder;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.handler.ConverterHandler;
import com.bing.excel.core.handler.LocalConverterHandler;
import com.bing.excel.core.impl.BingExcelEventImpl;
import com.bing.excel.core.impl.BingExcelImpl;
import com.bing.excel.exception.ConversionException;
import com.google.common.primitives.Primitives;

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
public class BingExcelEventBuilder implements Builder<BingExcelEvent> {
	private final ConverterHandler defaultLocalConverterHandler = new LocalConverterHandler();

	private BingExcelEvent bingExcelEvent;

	/**
	 * <p>
	 * Title: <／p>
	 * <p>
	 * Description:<／p>
	 */
	private BingExcelEventBuilder() {

	}

	public static Builder<BingExcelEvent> toBuilder() {

		return new BingExcelEventBuilder();

	}

	@Override
	public Builder<BingExcelEvent> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter) {
		
		defaultLocalConverterHandler.registerConverter(clazz, converter);
			
		return this;
	}

	@Override
	public BingExcelEvent builder() {
		if (bingExcelEvent == null) {
			bingExcelEvent = new BingExcelEventImpl(defaultLocalConverterHandler);
		}

		return this.bingExcelEvent;
	}

}

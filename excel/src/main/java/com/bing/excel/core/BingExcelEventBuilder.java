package com.bing.excel.core;


import com.bing.excel.core.handler.ConverterHandler;
import com.bing.common.ExcleBuilder;
import com.bing.excel.converter.FieldValueConverter;
import com.bing.excel.core.handler.LocalConverterHandler;
import com.bing.excel.core.impl.BingExcelEventImpl;

/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <code>BingExcel</code>的构造类，可以添加自定义转换器等。<／p>
 * <p>
 * Company: chinamobile<／p>
 * 
 * @author zhongtao.shi
 * date 2015-12-8
 * @Deprecated 不建议使用，如果要每次都输出，建议使用ExcelReadFactory
 */
/**
 * <p>
 * Title: BingExcelBuilder<／p>
 * <p>
 * Description: <／p>
 * <p>
 * Company: chinamobile<／p>
 * 
 * @author zhongtao.shi
 * date 2015-12-8
 */
@Deprecated
public class BingExcelEventBuilder implements ExcleBuilder<BingExcelEvent> {
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

	public static ExcleBuilder<BingExcelEvent> toBuilder() {

		return new BingExcelEventBuilder();

	}
	/**
	 * @return BingExcelEvent实例
	 */
	public static BingExcelEvent builderInstance(){
		return (new BingExcelEventBuilder()).builder();
	}
	@Override
	public ExcleBuilder<BingExcelEvent> registerFieldConverter(Class<?> clazz,
			FieldValueConverter converter) {
		
		defaultLocalConverterHandler.registerConverter(clazz, converter);
			
		return this;
	}

	@Deprecated
	@Override
	public BingExcelEvent builder() {
		if (bingExcelEvent == null) {
			bingExcelEvent = new BingExcelEventImpl(defaultLocalConverterHandler);
		}

		return this.bingExcelEvent;
	}
	@Override
	public BingExcelEvent build() {
		if (bingExcelEvent == null) {
			bingExcelEvent = new BingExcelEventImpl(defaultLocalConverterHandler);
		}

		return this.bingExcelEvent;
	}

	@Override
	public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz, String filedName,
			int index) {
		return null;
	}

	@Override
	public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz, String filedName,
			int index, String alias) {
		return null;
	}

	@Override
	public ExcleBuilder<BingExcel> addFieldConversionMapper(Class<?> clazz, String filedName,
			int index, String alias, FieldValueConverter converter) {
		return null;
	}

	@Override
	public ExcleBuilder<BingExcel> addClassNameAlias(Class<?> clazz, String alias) {
		return null;
	}
}

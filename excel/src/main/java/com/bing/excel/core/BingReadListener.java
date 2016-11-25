package com.bing.excel.core;

import com.bing.excel.core.impl.BingExcelEventImpl;

@Deprecated
public interface BingReadListener {

	void readModel(Object object, BingExcelEventImpl.ModelInfo modelInfo);

}

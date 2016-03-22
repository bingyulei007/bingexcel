package com.bing.excel.core;

import com.bing.excel.core.impl.BingExcelEventImpl.ModelInfo;

public interface BingReadListener {

	void writeModel(Object object, ModelInfo modelInfo);

}

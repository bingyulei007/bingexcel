package com.chinamobile.excel.core;

import com.chinamobile.excel.core.impl.BingExcelEventImpl.ModelInfo;
@Deprecated
public interface BingReadListener {

	void readModel(Object object, ModelInfo modelInfo);

}

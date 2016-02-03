package com.jt.ycl.oms.wxmgt;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.WXSubscribeReplyConfigDao;
import com.jt.core.model.WXSubscribeReplyConfig;

@Service
public class WXSubscribeReplyService {
	
	@Autowired
	private WXSubscribeReplyConfigDao configDao;
	
	public List<WXSubscribeReplyConfig> getAll(){
		return configDao.findAll();
	}
	
	public void updateAll(List<WXSubscribeReplyConfig> configs){
		if(CollectionUtils.isEmpty(configs)){
			return;
		}
		List<WXSubscribeReplyConfig> oldConfigs = configDao.findAll();
		//直接删除原来所有的
		if(CollectionUtils.isNotEmpty(oldConfigs)){
			configDao.delete(oldConfigs);
		}
		configDao.save(configs);
	}
}

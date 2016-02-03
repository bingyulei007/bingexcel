/**
 * 
 */
package com.jt.ycl.oms.coupon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.core.dao.LiBaoDao;
import com.jt.core.model.CouponTemplate;
import com.jt.core.model.LiBao;
import com.jt.utils.JSONSerializer;

/**
 * @author wuqh
 *
 */
@Service
@Transactional
public class LiBaoService {

	@Autowired
	private LiBaoDao liBaoDao;
	
	public List<LiBao> findLiBaosByMerchantId(int merchantId) {
		List<LiBao> result = liBaoDao.findByO2oMerchantId(merchantId);
		if(CollectionUtils.isNotEmpty(result)){
			for(LiBao liBao : result){
				List<CouponTemplate> couponTemplates = JSONSerializer.deserialize(liBao.getCouponTemplates(), CouponTemplate.class);
				liBao.setCount(couponTemplates.size());
			}
		}
		return result;
	}

	public LiBao findById(int id) {
		return liBaoDao.findOne(id);
	}

	public void createLiBao(LiBao liBao) {
		liBaoDao.save(liBao);
	}

	public void deleteLiBaoById(int id) {
		liBaoDao.delete(id);
	}
	
	public LiBao getLiBaoById(int id){
		LiBao liBao = liBaoDao.getOne(id);
		String couponTemplateString = liBao.getCouponTemplates();
		if(StringUtils.isNotEmpty(couponTemplateString)){
			List<CouponTemplate> couponTemplates = JSONSerializer.deserialize(couponTemplateString, CouponTemplate.class);
			if(CollectionUtils.isNotEmpty(couponTemplates)){
				int totalAmount = 0;
				for(CouponTemplate cTemplate : couponTemplates){
					totalAmount += cTemplate.getFaceValue();
				}
				liBao.setTotalAmount(totalAmount);
			}
		}
		return liBao;
	}

	public List<LiBaoCouponTemplate> getLiBaoCouponTemplatesById(int id) {
		LiBao liBao = liBaoDao.getOne(id);
		List<LiBaoCouponTemplate> result = new ArrayList<LiBaoCouponTemplate>();
		if(liBao != null){
			String couponTemplateString = liBao.getCouponTemplates();
			if(StringUtils.isNotEmpty(couponTemplateString)){
				List<CouponTemplate> couponTemplates = JSONSerializer.deserialize(couponTemplateString, CouponTemplate.class);
				if(CollectionUtils.isNotEmpty(couponTemplates)){
					Map<Integer, LiBaoCouponTemplate> map = new HashMap<Integer, LiBaoCouponTemplate>();
					Map<Integer, Integer> countMap = new HashMap<Integer, Integer>();
					for(CouponTemplate cTemplate : couponTemplates){
						if(map.get(cTemplate.getId()) !=null){ //已经存相同的卡券模板，需把金额及卡券数量相加
							LiBaoCouponTemplate liBaoCouponTemplate = map.get(cTemplate.getId());
							liBaoCouponTemplate.setCount(countMap.get(cTemplate.getId())+1);
							countMap.put(cTemplate.getId(), liBaoCouponTemplate.getCount());
							map.put(cTemplate.getId(), liBaoCouponTemplate);
						}else{
							LiBaoCouponTemplate liBaoCouponTemplate = new LiBaoCouponTemplate();
							liBaoCouponTemplate.setId(cTemplate.getId());
							liBaoCouponTemplate.setName(cTemplate.getName());
							liBaoCouponTemplate.setFaceValue(cTemplate.getFaceValue());
							liBaoCouponTemplate.setCount(1);
							map.put(cTemplate.getId(), liBaoCouponTemplate);
							countMap.put(cTemplate.getId(), 1);
						}
					}
					for(Integer idStr : map.keySet()){
						result.add(map.get(idStr));
					}
				}
			}
		}
		return result;
	}
}

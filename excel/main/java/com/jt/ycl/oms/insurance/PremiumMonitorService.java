package com.jt.ycl.oms.insurance;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.jpa.internal.QueryImpl;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Service;

import com.jt.core.model.InsurancePolicy;

@Service
public class PremiumMonitorService {
	@PersistenceContext
	private EntityManager entity;

	public List<?> queryFroPreminuDate(String salerName, String cityCode) {
		String sql = "SELECT t.salesMan saler, count(t.salesMan) dueAll, SUM( t.sumPremium + t.sumPremiumCI + t.carShipTax ) dueAllMoney, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY),'%Y-%m-%d'),' 23:59:59') THEN 1 ELSE 0 END ) t2, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 2 DAY),'%Y-%m-%d'),' 23:59:59') THEN t.sumPremium + t.sumPremiumCI + t.carShipTax ELSE 0 END ) t2Money, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY),'%Y-%m-%d'),' 23:59:59') THEN 1 ELSE 0 END ) t3, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 3 DAY),'%Y-%m-%d'),' 23:59:59') THEN t.sumPremium + t.sumPremiumCI + t.carShipTax ELSE 0 END ) t3Money, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY),'%Y-%m-%d'),' 23:59:59') THEN 1 ELSE 0 END ) t4, SUM( CASE WHEN t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 4 DAY),'%Y-%m-%d'),' 23:59:59') THEN t.sumPremium + t.sumPremiumCI + t.carShipTax ELSE 0 END ) t4Money, SUM( CASE WHEN t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY),'%Y-%m-%d'),' 23:59:59') THEN 1 ELSE 0 END ) t5Over, SUM( CASE WHEN t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY),'%Y-%m-%d'),' 23:59:59') THEN t.sumPremium + t.sumPremiumCI + t.carShipTax ELSE 0 END ) t5OverMoney FROM core_t_insurance_policy t WHERE t.`status` IN (4, 7, 11) ";
		StringBuilder appendSql = new StringBuilder();
		if (!"0".equals(salerName) && StringUtils.isNoneBlank(salerName)) {
			appendSql.append(" AND t.salesMan=:salerName");
		}
		if (!"0".equals(cityCode) && StringUtils.isNoneBlank(cityCode)) {
			appendSql.append("  AND t.cityCode=:cityCode");
		}
		appendSql.append("  GROUP BY t.salesMan");
		sql += appendSql.toString();
		Query query = entity.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		if (!"0".equals(salerName) && StringUtils.isNoneBlank(salerName)) {
			query.setParameter("salerName", salerName);
		}
		if (!"0".equals(cityCode) && StringUtils.isNoneBlank(cityCode)) {
			query.setParameter("cityCode", cityCode);
		}

		List<Map> list = query.getResultList();
		return list;
	}

	public List<?> querySalerPremium(String saler, String cityCode, int deadline) {
		String sql = "SELECT t.orderId ,t.merchantName,t.salesMan,t.insurant,t.carNumber,t.companyCode,CASE t.`status` WHEN '11' THEN '正在配送' WHEN '4' THEN '已出单' WHEN '7' THEN '已配送' END expressStatus,t.createDate FROM core_t_insurance_policy t WHERE    (t.`status` IN (4, 7, 11)) ";
		StringBuilder appendSql = new StringBuilder();
		if ( !"0".equals(cityCode)&&StringUtils.isNoneBlank(cityCode)) {
			appendSql.append("  AND t.cityCode=:cityCode");
		}
		if(StringUtils.isBlank(saler)){
			appendSql.append(" AND (t.salesMan IS NULL OR t.salesMan='')");
		}else{
			
			appendSql.append(" AND  t.salesMan =:salerName");
		}
		if(deadline!=0){
			if(deadline<5){
				appendSql.append("  AND  t.createDate >= CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL "+(deadline+1)+" DAY),'%Y-%m-%d'),' 23:59:59') AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL "+deadline+" DAY),'%Y-%m-%d'),' 23:59:59')");
			}else{
				appendSql.append(" AND t.createDate < CONCAT(DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 5 DAY),'%Y-%m-%d'),' 23:59:59')");
			}
		}
		
		sql += appendSql.toString();
		Query query = entity.createNativeQuery(sql);
		if(StringUtils.isNoneBlank(saler)){
		query.setParameter("salerName", saler);
		}
		if ( !"0".equals(cityCode)&&StringUtils.isNoneBlank(cityCode)) {
			query.setParameter("cityCode", cityCode);
		}
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		return query.getResultList();
	}
}

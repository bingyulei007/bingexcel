package com.jt.ycl.oms.wxmgt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CopyObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.core.SmsService;
import com.jt.core.SmsTemplateId;
import com.jt.core.dao.CarDao;
import com.jt.core.dao.UserDao;
import com.jt.core.dao.VehicleLicenseAuditDao;
import com.jt.core.model.Car;
import com.jt.core.model.User;
import com.jt.core.model.VehicleLicenseAudit;

@Service
public class VehicleLicenseAuditService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String accessKeyId = "qlo4BoLGXaAXU7FA";
	private final String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private final String bucketName = "auditimg";
	
	@Autowired
	private VehicleLicenseAuditDao auditDao;
	
	@Autowired
	private WXConfigInfo wxConfigInfo;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CarDao carDao;
	
	private OSSClient client = null;
	
	@Autowired
	private SmsService smsService;
	
	@PostConstruct
	public void init(){
		client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
	}
	
	public int countByStatus(int status){
		return auditDao.countByStatus(status);
	}
	
	/**
	 * 获取一条需要审核的记录
	 * @return
	 */
	public VehicleLicenseAudit getOneVehicleLicense(){
		List<VehicleLicenseAudit> audits = auditDao.findByStatusOrderByCreateDateAsc(1, new PageRequest(0,1));
		if(CollectionUtils.isNotEmpty(audits)){
			return audits.get(0);
		}
		return null;
	}
	
	/**
	 * 更新审核记录状态为审核通过
	 * @param id
	 */
	public void updateStatusSuccess(long id){
		VehicleLicenseAudit audit = auditDao.findOne(id);
		if(audit!=null && audit.getStatus() != 0){
			audit.setStatus(0);
			auditDao.save(audit);
			
			String srcKey = "unaudited/" + audit.getMediaId() + ".jpg";
			String destKey = "audited/" + audit.getMediaId() + ".jpg";
			
			if(client.doesObjectExist(bucketName, srcKey)){
				//审核通过，移动文件到已审核的文件夹中
				CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucketName, srcKey, bucketName, destKey);
				client.copyObject(copyObjectRequest);
				
        		client.deleteObject(bucketName, srcKey);
        	}
			String userId = audit.getUserId();
			User user = userDao.findOne(userId);
			Car car = carDao.findOne(audit.getCarId());
			if(user != null && StringUtils.isNotEmpty(user.getPhone())){
				smsService.send(SmsTemplateId.VEHICLE_LICENSE_AUDIT_SUCCESS, user.getPhone(), new String[]{car.getNumber()});
			}
		}
	}
	
	public void updateStatusFailed(long id, String errmsg){
		VehicleLicenseAudit audit = auditDao.findOne(id);
		if(audit!=null && audit.getStatus() != 2){
			audit.setStatus(2);
			audit.setErrmsg(errmsg);
			auditDao.save(audit);
			
			String key = "unaudited/" + audit.getMediaId() + ".jpg";
			if(client.doesObjectExist(bucketName, key)){
				client.deleteObject(bucketName, key);
			}
			
			String userId = audit.getUserId();
			User user = userDao.findOne(userId);
			Car car = carDao.findOne(audit.getCarId());
			if(user != null && StringUtils.isNotEmpty(user.getPhone()) && car != null){
				smsService.send(SmsTemplateId.VEHICLE_LICENSE_AUDIT_FAILED, user.getPhone(), new String[]{car.getNumber(), errmsg});
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void auditNotify(int status, String openId){
		try{
			CloseableHttpClient client = HttpClients.createDefault();
			String url = "http://127.0.0.1:" + wxConfigInfo.getApiPort()+"/apigateway/api/v1/wx/message/send/audit";
			HttpPost post = new HttpPost(url);
			List<NameValuePair> pairList = new ArrayList<>();
			NameValuePair pair = new BasicNameValuePair("status", status + "");
			pairList.add(pair);
			pair = new BasicNameValuePair("openId", openId);
			pairList.add(pair);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
			post.setEntity(entity);
			CloseableHttpResponse response = client.execute(post);
			if(logger.isDebugEnabled()){
				String result = EntityUtils.toString(response.getEntity());
				ObjectMapper om = new ObjectMapper();
				Map<String, String> map = om.readValue(result, Map.class);
				String res = map.get("result");
				if(StringUtils.equals("success", res)){
					logger.debug("调用微信客服通知成功");
				}else{
					logger.error("调用微信客服通知失败");
				}
			}
			response.close();
		}catch(Exception e){
			logger.error("调用微信客服通知失败",e);
		}
		
		
	}
	
	public Page<VehicleLicenseAudit> list(int page, int pageSize){
		return auditDao.findAll(new Specification<VehicleLicenseAudit>() {
			@Override
			public Predicate toPredicate(Root<VehicleLicenseAudit> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return null;
			}
		}, buildPageRequest(page, pageSize, Direction.DESC,null));
	}
	
	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "createDate");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }
}
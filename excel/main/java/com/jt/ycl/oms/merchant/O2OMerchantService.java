/**
 * 
 */
package com.jt.ycl.oms.merchant;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.jt.core.dao.CouponTemplateDao;
import com.jt.core.dao.LiBaoDao;
import com.jt.core.dao.O2OMerchantDao;
import com.jt.core.model.O2OMerchant;

/**
 * @author wuqh
 *
 */
@Service
@Transactional
public class O2OMerchantService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private O2OMerchantDao o2oMerchantDao;
	
	@Autowired
	private CouponTemplateDao couponTemplateDao;
	
	@Autowired
	private LiBaoDao liBaoDao;
	
	private String accessKeyId = "qlo4BoLGXaAXU7FA";
	private String accessKeySecret = "wHT8XCsYTnaj7utm5L0t1f1owmwSTy";
	private String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
	private String bucketName = "o2omerchantimg";

	/**
	 * 查询O2O商家
	 * @param merchant
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<O2OMerchant> findO2OMerchants(final String province,final int cityCode,final String merchant, final int serviceCategory,
			int pageNumber,	int pageSize) {
		Page<O2OMerchant> pageResult = o2oMerchantDao.findAll(new Specification<O2OMerchant>() {
			@Override
			public Predicate toPredicate(Root<O2OMerchant> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotEmpty(province)&&!"0".equals(province)) {
					predicateList.add(cb.equal(root.get("province"), province));
				}
				if(cityCode>0) {
					predicateList.add(cb.equal(root.get("cityCode"), cityCode));
				} 
				if(serviceCategory>0) {
					predicateList.add(cb.equal(root.get("serviceCategory"), serviceCategory));
				} 
				if(StringUtils.isNotEmpty(merchant)){
					Path<String> namePath = root.get("name"); 
					Path<String> aliasPath = root.get("alias");
					Predicate p1 = cb.like(namePath, "%"+merchant+"%");
					Predicate p2 = cb.like(aliasPath, "%"+merchant+"%");
					predicateList.add(cb.or(cb.or(p2), p1));
				}
				return cb.and(predicateList.toArray(new Predicate[0]));
			}
		}, buildPageRequest(pageNumber, pageSize, Direction.DESC,null));
		return pageResult;
	}

	/**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(int pageNumber, int pageSize, Direction sortType, String sortAttribute) {
    	Sort sort = null;
    	if(null == sortAttribute){
    		sort = new Sort(sortType, "id");
    	}else{
    		sort = new Sort(sortType, sortAttribute);
    	}
        return new PageRequest(pageNumber, pageSize, sort);
    }

	public void createO2OMerchant(O2OMerchant o2oMerchant, MultipartFile file, List<MultipartFile> files) {
		O2OMerchant result = saveO2OMerchant(o2oMerchant);
		if(file.getSize()>0 || files.size()>0){
			upload(result.getId(),file, files);
		}
	}

	private void upload(int o2oMerchantId, MultipartFile file, List<MultipartFile> files){
		OSSClient client = null;
		String folderName = o2oMerchantId+"/";
		ObjectMetadata objectMeta = new ObjectMetadata();
		byte[] buffer = new byte[0];
		InputStream in = new ByteArrayInputStream(buffer);  
		objectMeta.setContentLength(0);
		try {
			//1. 创建文件夹
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
		    client.putObject(bucketName, folderName, in, objectMeta);
			try {
				//2. 上传图片缩略图，大小为148×148
				if(file.getSize()>0){
					String logoFile= "small.jpg";
					BufferedImage bigBi = Thumbnails.of(file.getInputStream()).size(148, 148).keepAspectRatio(false).asBufferedImage();
					bigBi.flush(); 
		        	ByteArrayOutputStream bigBs = new ByteArrayOutputStream();  
		        	ImageOutputStream BigImOut = ImageIO.createImageOutputStream(bigBs);  
		        	ImageIO.write(bigBi, "jpg", BigImOut);  //所的上传的图片格式统一转换成JPG
		        	in = new ByteArrayInputStream(bigBs.toByteArray());
		        	ObjectMetadata meta = new ObjectMetadata();
		        	// 必须设置ContentLength
		        	meta.setContentLength(bigBs.toByteArray().length);
		        	if(client.doesObjectExist(bucketName, folderName + logoFile)){
		        		client.deleteObject(bucketName, folderName + logoFile);
		        	}
		        	client.putObject(bucketName, folderName + logoFile, in, meta);
		        	
		        	//3. 上传图片缩略图，大小为640×300
		        	String logoBigFile = "big.jpg";
		        	BufferedImage bi = Thumbnails.of(file.getInputStream()).size(640,300).keepAspectRatio(false).asBufferedImage();
		        	bi.flush(); 
		        	ByteArrayOutputStream bos = new ByteArrayOutputStream();  
		        	ImageOutputStream smallImOut = ImageIO.createImageOutputStream(bos);  
		        	ImageIO.write(bi, "jpg", smallImOut); 
		        	in = new ByteArrayInputStream(bos.toByteArray()); 
		        	// 必须设置ContentLength
		        	meta.setContentLength(bos.toByteArray().length);
		        	if(client.doesObjectExist(bucketName, folderName + logoBigFile)){
		        		client.deleteObject(bucketName, folderName + logoBigFile);
		        	}
		        	client.putObject(bucketName, folderName + logoBigFile, in, meta);
				}
	        	if(files != null && files.size()>0){//上传展示图
	        		int i=1;
	        		for(MultipartFile moreFile : files){
	        			if(moreFile.getSize()>0){
		        			//2. 上传图片缩略图，大小为大小为354×300
		    				String pictureName= i+".jpg";
		    				BufferedImage bufferedImage = Thumbnails.of(moreFile.getInputStream()).size(354, 300).keepAspectRatio(false).asBufferedImage();
		    				bufferedImage.flush(); 
		    	        	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
		    	        	ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);  
		    	        	ImageIO.write(bufferedImage, "jpg", imageOutputStream);  //所的上传的图片格式统一转换成JPG
		    	        	in = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		    	        	ObjectMetadata objectMetadata = new ObjectMetadata();
		    	        	// 必须设置ContentLength
		    	        	objectMetadata.setContentLength(byteArrayOutputStream.toByteArray().length);
		    	        	if(client.doesObjectExist(bucketName, folderName + pictureName)){
		    	        		client.deleteObject(bucketName, folderName + pictureName);
		    	        	}
		    	        	client.putObject(bucketName, folderName + pictureName, in, objectMetadata);
		    	        	i++;
	        			}
	        		}
	        	}
	        	
			} catch (IOException e) {
				e.printStackTrace();
			}
		}finally {
		    try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}

	public void deleteO2OMerchantById(int id){
		o2oMerchantDao.delete(id);
		logger.info("删除o2o商家: {}", id);
		//同时将该商家对应的卡券所有模板删除掉
		couponTemplateDao.deleteByO2oMerchantId(id);
		
		//删除所有该商家所有礼包
		liBaoDao.deleteByO2oMerchantId(id);
		
		//TODO, 是否需要删除用户的卡券
		
		OSSClient client = null;
		String folderName = id+"/";
		try {
			client = new OSSClient(endpoint,accessKeyId, accessKeySecret);
	        //删除文件夹名为merchantId的文件夹以及文件夹下的缩略图small.jpg和big.jpg，
        	if(client.doesObjectExist(bucketName, folderName)){
        		client.deleteObject(bucketName, folderName);
        		logger.info("删除商家图片: {}", id);
        	}
		}finally {
		    if(client != null){
		    	client.shutdown();
		    }
		}
	}
	
	private O2OMerchant saveO2OMerchant(O2OMerchant o2oMerchant) {
		return o2oMerchantDao.save(o2oMerchant);
	}

	public O2OMerchant findById(int id) {
		return o2oMerchantDao.findOne(id);
	}

}

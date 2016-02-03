package com.jt.ycl.oms.mail;

import java.io.File;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService extends JavaMailSenderImpl {
	
	private Properties javaMailProperties = new Properties();

	final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

	@PostConstruct
	public void init() throws Exception {
		setDefaultEncoding("UTF-8");
		setUsername("oss@ykcare.cn");
		setPassword("oss2015");
		javaMailProperties.setProperty("mail.smtp.port", "25");
		javaMailProperties.setProperty("mail.smtp.socketFactory.port", "25");
		javaMailProperties.setProperty("mail.smtp.host", "smtp.ym.163.com");
		javaMailProperties.setProperty("mail.smtp.auth", "true");
		javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
		javaMailProperties.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		setJavaMailProperties(javaMailProperties);
	}

	public void sendTextMail(String[] receviers, String replyTo, String subject, String content) {
		if (receviers == null || receviers.length < 1) {
			return;
		}
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom("oss@ykcare.cn");
		msg.setTo(receviers);
		msg.setSubject(subject);
		msg.setText(content);
		if (StringUtils.isNotBlank(replyTo)) {
			msg.setReplyTo(replyTo);
		}
		this.send(msg);
	}

	public void sendMimeMail(String[] receviers, String subject, String content, String[] fileNames, File... attachments) throws Exception {
		MimeMessage msg = this.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
		helper.setTo(receviers);
		helper.setFrom("oss@ykcare.cn", "有空养车运营管理系统");
		helper.setSubject(subject);
		helper.setText(content, true);

		if (attachments != null && attachments.length > 0) {
			for (int i = 0; i < attachments.length; i++) {
				File file = attachments[i];
				//解决附件名称过长，导致收到的邮件附件名称不是原有名称的问题
				String fileNameEnc;
				fileNameEnc="=?GBK?B?" + Base64.encodeBase64String(new String(fileNames[i].getBytes(),"GBK").getBytes()) + "?=";
				fileNameEnc=fileNameEnc.replaceAll("\r","").replaceAll("\n","");
				helper.addAttachment(fileNameEnc, file);
			}
		}
		this.send(msg);
	}
	
	public static void main(String[] args) throws Exception {
		String v = MimeUtility.encodeText("苏州佳途-商家结算单2015-05-01到2015-05-31.xls");
		System.out.println(v);
		v = v.replaceAll("\r", "").replaceAll("\n", "");
		System.out.println(v);
	}

}

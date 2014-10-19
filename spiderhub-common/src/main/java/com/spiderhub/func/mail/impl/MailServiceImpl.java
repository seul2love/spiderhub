package com.spiderhub.func.mail.impl;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.spiderhub.func.mail.IMailService;
import com.spiderhub.vo.MailInfo;

public class MailServiceImpl implements IMailService{
	private Logger logger = Logger.getLogger(MailServiceImpl.class);
	
	private Transport ts = null;
	
	private Message msg = null;
	
	private Session session = null;
	
	public MailServiceImpl() {
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.host", "smtp.sina.com");
		props.setProperty("mail.smtp.auth", "true");
		session = Session.getInstance(props);
		session.setDebug(true);
		msg = new MimeMessage(session);
	}


	public void sendMail(MailInfo mailInfo) {
		try {
			msg.setSubject(mailInfo.getSubject());
			msg.setText(mailInfo.getContent());
			msg.setFrom(new InternetAddress(mailInfo.getMailFrom()));
			ts = session.getTransport();
			ts.connect(mailInfo.getUsername(), mailInfo.getPassword());
			ts.sendMessage(msg, new InternetAddress[]{new InternetAddress(mailInfo.getMailTo()[0])});
			
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}


}

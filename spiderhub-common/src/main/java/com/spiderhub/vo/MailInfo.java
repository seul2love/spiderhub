package com.spiderhub.vo;

import java.util.Set;

public class MailInfo {
	private int id;
	private String username;
	private String password;
	private String mailFrom;
	private String[] mailTo;
	private String[] mailRecipients;
	private String subject;
	private String content;
	private String[] multipart;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMailFrom() {
		return mailFrom;
	}
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	public String[] getMailTo() {
		return mailTo;
	}
	public void setMailTo(String[] mailTo) {
		this.mailTo = mailTo;
	}
	public String[] getMailRecipients() {
		return mailRecipients;
	}
	public void setMailRecipients(String[] mailRecipients) {
		this.mailRecipients = mailRecipients;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String[] getMultipart() {
		return multipart;
	}
	public void setMultipart(String[] multipart) {
		this.multipart = multipart;
	}

	
	
	
}

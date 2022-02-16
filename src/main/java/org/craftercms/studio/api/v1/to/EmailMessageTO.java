/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.api.v1.to;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class EmailMessageTO implements Serializable {


    private static final long serialVersionUID = -509714280274105998L;
    protected String subject;
	protected String content;
	protected String to;
	protected String personalFromName;
	protected String replyTo;
	protected String previewBaseUrl;
	protected String liveBaseUrl;
    protected String authoringBaseUrl;
	protected String browserUrl;
	protected String adminEmail;
	protected String rejectReason;
	
	public String getBrowserUrl() {
		return browserUrl;
	}	

	protected String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		if(title != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_TITLE,title);
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
		if(adminEmail != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_ADMINEMAIL,adminEmail);
	}

	/**
	 * Starts from /site
	 * @param browserUrl preview url
	 */
	public void setBrowserUrl(String browserUrl) {
		this.browserUrl = browserUrl;		
		String previewUrl=previewBaseUrl+browserUrl;
		String liveUrl=liveBaseUrl+browserUrl;
		if(previewUrl != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_PREVIEWURL,previewUrl);
		
		if(liveUrl != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_LIVEURL,liveUrl);
	}
	
	public void setBrowserUrlForExternalDocument(String browserUrl) {
		this.browserUrl = browserUrl;
		if(browserUrl != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_PREVIEWURL,browserUrl);
		
		if(browserUrl != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_LIVEURL,browserUrl);
		
	}

	protected String getRelativeUrl(String fullUrl)
	{
		String relativeUrl="";
		return relativeUrl;
		
	}
	public String getPreviewBaseUrl() {
		return previewBaseUrl;
	}

	public void setPreviewBaseUrl(String previewBaseUrl) {
		this.previewBaseUrl = previewBaseUrl;
	}

	public String getLiveBaseUrl() {
		return liveBaseUrl;
	}

	public void setLiveBaseUrl(String liveBaseUrl) {
		this.liveBaseUrl = liveBaseUrl;
	}

    public String getAuthoringBaseUrl() { return authoringBaseUrl; }

    public void setAuthoringBaseUrl(String authoringBaseUrl) {
        this.authoringBaseUrl = authoringBaseUrl;
        if (StringUtils.isNotEmpty(authoringBaseUrl)) {
            setValue(EMAIL_TEMPLATE_KEYWORDS_AUTHORINGURL, authoringBaseUrl);
        }
    }

    public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
		if (rejectReason != null) 
			setValue(EMAIL_TEMPLATE_REJECT_REASON, rejectReason);
	}

	public String getRejectReason() {
		return rejectReason;
	}

	protected Map<String,String> keyValueMap;
	protected String[] emailTemplateKeywords= {
			"title",
			"preview-url",
			"live-url",
            "authoring-url",
			"user-name", 
			"admin-email",
			"reject-reason"
	};
	public static final String EMAIL_TEMPLATE_KEYWORDS_ADMINEMAIL="admin-email";
	public static final String EMAIL_TEMPLATE_KEYWORDS_USERNAME="user-name";
	public static final String EMAIL_TEMPLATE_KEYWORDS_PREVIEWURL="preview-url";
	public static final String EMAIL_TEMPLATE_KEYWORDS_LIVEURL="live-url";
    public static final String EMAIL_TEMPLATE_KEYWORDS_AUTHORINGURL="authoring-url";
	public static final String EMAIL_TEMPLATE_KEYWORDS_TITLE="title";
	public static final String EMAIL_TEMPLATE_REJECT_REASON="reject-reason";

	public String getValue(String key)
	{
		return keyValueMap.get(key);
	}
	
	public void setValue(String key,String value)
	{
		keyValueMap.put(key,value);
	}
	
	public String getPersonalFromName() {
		return personalFromName;
	}

	public void setPersonalFromName(String personalFromName) {
		this.personalFromName = personalFromName;
		if(personalFromName != null)
			setValue(EMAIL_TEMPLATE_KEYWORDS_USERNAME,personalFromName);
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public EmailMessageTO(String subject,String content,String to)
	{
		this.subject=subject;
		this.content=content;
		this.to=to;
		this.keyValueMap= new TreeMap<String,String>();
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
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public void preprocessEmail()
	{
		String newSubject= replaceKeywordsByValue(subject);
		subject=newSubject;
		
		String newContent=replaceKeywordsByValue(content);
		content=newContent;
	}
	
	protected String replaceKeywordsByValue(String input)
	{
		String output=input;
		for(int counter=0;counter<emailTemplateKeywords.length;counter++)
		{
			String keyword=emailTemplateKeywords[counter];
			String value=getValue(keyword);
			if(value != null)
				output=output.replace("$"+keyword, value);
			
		}
		return output;
	}

}

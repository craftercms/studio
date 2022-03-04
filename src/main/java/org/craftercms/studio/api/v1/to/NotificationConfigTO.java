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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * site notification configuration
 * 
 * @author hyanghee
 * 
 */
public class NotificationConfigTO implements TimeStamped, Serializable {

    private static final long serialVersionUID = 5995281689437086341L;
    /** site key **/
	protected String _site = null;
	/** canned messages **/
	protected Map<String, List<MessageTO>> _cannedMessages = null;
	/** email template messages **/
	protected Map<String, EmailMessageTemplateTO> _emailMessageTemplates = null;
	/** complete messages used for displaying complete pop-ups **/
	protected Map<String, String> _completeMessages = null;

	/** general messages **/
	protected Map<String, String> messages = null;

    /** send notice on action mapping **/
    protected Map<String, Boolean> _sendNoticeMapping = null;
    
    protected Map<String, String> errorMessages = null;
	/** configuration time stamp **/
	protected ZonedDateTime _lastUpdated = null;

    protected Map<String, String> submitNotificationsMapping = null;

    protected List<String> deploymentFailureNotifications = null;
	protected List<String> approverEmails = null;

	protected boolean isNewNotificationSystemEnable;

    protected List<String> repositoryMergeConflictNotifications = null;

	public NotificationConfigTO(final String _site) {
		this();
		this._site = _site;
	}

	public NotificationConfigTO() {
		_cannedMessages=new HashMap<>();
		_emailMessageTemplates=new HashMap<>();
		_completeMessages=new HashMap<>();
		messages=new HashMap<>();
		deploymentFailureNotifications=new ArrayList<>();
		isNewNotificationSystemEnable=false;//for now!
		approverEmails=new ArrayList<>();
		repositoryMergeConflictNotifications = new ArrayList<>();
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return _site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(String site) {
		this._site = site;
	}

	/**
	 * @return the canned messages
	 */
	public Map<String, List<MessageTO>> getCannedMessages() {
		return _cannedMessages;
	}

	/**
	 * @param cannedMessages
	 *            the canned messages to set
	 */
	public void setCannedMessages(Map<String, List<MessageTO>> cannedMessages) {
		this._cannedMessages = cannedMessages;
	}

	/**
	 * @return the email message templates
	 */
	public Map<String, EmailMessageTemplateTO> getEmailMessageTemplates() {
		return _emailMessageTemplates;
	}

	/**
	 * @param emailMessageTemplates
	 *            the email message templates to set
	 */
	public void setEmailMessageTemplates(Map<String, EmailMessageTemplateTO> emailMessageTemplates) {
		this._emailMessageTemplates = emailMessageTemplates;
	}

	
	/**
	 * @return the lastUpdated
	 */
	public ZonedDateTime getLastUpdated() {
		return _lastUpdated;
	}

	/**
	 * @param lastUpdated
	 *            the lastUpdated to set
	 */
	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @param completeMessages the completeMessages to set
	 */
	public void setCompleteMessages(Map<String, String> completeMessages) {
		this._completeMessages = completeMessages;
	}

	/**
	 * @return the completeMessages
	 */
	public Map<String, String> getCompleteMessages() {
		return _completeMessages;
	}

	/**
	 * @param messages the messages to set
	 */
	public void setMessages(Map<String, String> messages) {
		this.messages = messages;
	}

	/**
	 * @return the messages
	 */
	public Map<String, String> getMessages() {
		return messages;
	}

    public void setErrorMessages(Map<String, String> errorMessages) {
        this.errorMessages=errorMessages;
        //To change body of created methods use File | Settings | File Templates.
    }

    public Map<String, String> getErrorMessages() {
        return errorMessages;
    }

	/**
	 * @return the sendNoticeMapping
	 */
	public Map<String, Boolean> getSendNoticeMapping() {
		if (this._sendNoticeMapping == null) {
			this._sendNoticeMapping = new HashMap<String, Boolean>();
		}
		return _sendNoticeMapping;
	}

	public boolean isNewNotificationSystemEnable() {
		return isNewNotificationSystemEnable;
	}

	public void setNewNotificationSystemEnable(final boolean newNotificationSystemEnable) {
		isNewNotificationSystemEnable = newNotificationSystemEnable;
	}

	/**
	 * @param sendNoticeMapping the sendNoticeMapping to set
	 */
	public void setSendNoticeMapping(Map<String, Boolean> sendNoticeMapping) {
		this._sendNoticeMapping = sendNoticeMapping;
	}

    public Map<String, String> getSubmitNotificationsMapping() {
        return submitNotificationsMapping;
    }

    public void setSubmitNotificationsMapping(Map<String, String> submitNotificationsMapping) {
        this.submitNotificationsMapping = submitNotificationsMapping;
    }

    public List<String> getDeploymentFailureNotifications() {
        return deploymentFailureNotifications;
    }

    public void setDeploymentFailureNotifications(List<String> deploymentFailureNotifications) {
        this.deploymentFailureNotifications = deploymentFailureNotifications;
    }

	public List<String> getApproverEmails() {
		return approverEmails;
	}

	public void setApproverEmails(final List<String> approverEmails) {
		this.approverEmails = approverEmails;
	}

    public List<String> getRepositoryMergeConflictNotifications() {
        return repositoryMergeConflictNotifications;
    }

    public void setRepositoryMergeConflictNotifications(List<String> repositoryMergeConflictNotifications) {
        this.repositoryMergeConflictNotifications = repositoryMergeConflictNotifications;
    }
}

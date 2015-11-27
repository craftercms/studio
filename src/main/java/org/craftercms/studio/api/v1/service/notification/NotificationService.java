/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.api.v1.service.notification;

import org.craftercms.studio.api.v1.to.MessageTO;

import java.util.List;
import java.util.Date;
import java.util.Map;


/**
 * Provides services for sending and getting notification messages
 * 
 * @author hyanghee
 *
 */
public interface NotificationService {

	/** complete message keys **/
	public static final String COMPLETE_GO_LIVE = "go-live";
	public static final String COMPLETE_REJECT = "reject";
	public static final String COMPLETE_SCHEDULE_GO_LIVE = "schedule-to-go-live";	
	public static final String COMPLETE_SUBMIT_TO_GO_LIVE = "submit-to-go-live";
    public static final String COMPLETE_DELETE = "delete";
	
	/**
	 * get canned rejection reasons given a site name 
	 * 
	 * @param site
	 * @return messages
	 */
	public List<MessageTO> getCannedRejectionReasons(final String site);

	/**
	 * get a complete message by the given site and the key
	 * 
	 * @param site
	 * @param key
	 * @return complete message
	 */
	public String getCompleteMessage(final String site, final String key);

    public String getErrorMessage(final String site, final String key, Map<String,String> params);

	/**
	 * get a general message from the specified site by the given key
	 * 
	 * @param site
	 * @param key
	 * @return message
	 */
	public String getGeneralMessage(String site, String key);

    void sendDeploymentFailureNotification(String site, Throwable error);

    /**
	 * send rejection notification
	 * 
	 * @param site
	 * @param to
	 * @param url
	 * @param reason
	 * @param from
	 * @param isPreviewable
	 */
	public void sendRejectionNotification(final String site,final String to, final String url,final String reason,String from, boolean isPreviewable);
	
	/**
	 * send approval notification
	 * 
	 * @param site
	 * @param to
	 * @param url
	 * @param from
	 */
	public void sendApprovalNotification(final String site,final String to,final String url,String from);

    void sendContentSubmissionNotificationToApprovers(String site, String to, String browserUrl, String from, Date scheduledDate, boolean isPreviewable, boolean isDelete);

    /**
	 * send deletion notifcation 
	 * 
	 * @param site
	 * @param to
	 * @param url
	 * @param from
	 */
	public void sendDeleteApprovalNotification(String site,String to,final String url,String from);
	
	/**
	 * send content submission notification
	 * 
	 * @param site
	 * @param to
	 * @param url
	 * @param from
	 * @param scheduledDate
	 * @param isPreviewable
	 * @param isDelete
	 */
	public void sendContentSubmissionNotification(String site,String to,final String url,String from,Date scheduledDate,boolean isPreviewable,boolean isDelete);

	/**
	 * send notification upon the action given?
	 * 
	 * @param site
	 * @param action
	 * @return true if configured to send
	 */
	public boolean sendNotice(String site, String action);

	/**
	 * Send a generic notification.
	 * 
	 * @param site the site name of this notification.
	 * @param path the content path pertain to this notification.
	 * @param to the recipient of the email.
	 * @param from the sender of the email.
	 * @param key the email message template key in notification-config.xml.
	 * @param params the name/value pairs to be injected into the message.
	 */
	public void sendGenericNotification(String site, String path, String to, String from, String key, Map<String,String> params);

    void reloadConfiguration(String site);
}

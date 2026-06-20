/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * site notification configuration
 *
 * @author hyanghee
 */
public class NotificationConfigTO implements Serializable {

	private static final long serialVersionUID = 5995281689437086341L;
	/**
	 * email template messages
	 **/
	protected final Map<String, EmailMessageTemplateTO> _emailMessageTemplates;

	protected final List<String> deploymentFailureNotifications;
	protected final List<String> approverEmails;

	protected final List<String> repositoryMergeConflictNotifications;

	public NotificationConfigTO() {
		this._emailMessageTemplates = new HashMap<>();
		this.deploymentFailureNotifications = new ArrayList<>();
		this.approverEmails = new ArrayList<>();
		this.repositoryMergeConflictNotifications = new ArrayList<>();
	}

	/**
	 * @return the email message templates
	 */
	public Map<String, EmailMessageTemplateTO> getEmailMessageTemplates() {
		return _emailMessageTemplates;
	}

	public List<String> getDeploymentFailureNotifications() {
		return deploymentFailureNotifications;
	}

	public List<String> getApproverEmails() {
		return approverEmails;
	}

	public List<String> getRepositoryMergeConflictNotifications() {
		return repositoryMergeConflictNotifications;
	}

}

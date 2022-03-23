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
package org.craftercms.studio.api.v1.constant;

/**
 * CStudio Xml constants
 *
 * @author hyanghee
 *
 */
public final class StudioXmlConstants {

	/** xml document element names for cstudio **/
	public static final String DOCUMENT_ELM_INTERNAL_TITLE = "internal-name";
	public static final String DOCUMENT_ELM_CONTENT_TYPE = "content-type";
	public static final String DOCUMENT_ELM_FILE_NAME = "file-name";
	public static final String DOCUMENT_ELM_DISABLED = "disabled";

	/** xml document root and element names for roles-mapping and permissions-mapping xmls */
	public static final String DOCUMENT_ROLE_MAPPINGS = "role-mappings";
	public static final String DOCUMENT_PERMISSIONS = "permissions";
	public static final String DOCUMENT_ELM_ALLOWED_PERMISSIONS = "allowed-permissions/permission";
	public static final String DOCUMENT_ELM_GROUPS_NODE = "groups/group";
	public static final String DOCUMENT_ELM_USER_NODE = "users/user";
	public static final String DOCUMENT_ELM_PERMISSION_ROLE = "role";
	public static final String DOCUMENT_ELM_PERMISSION_RULE = "rule";
	public static final String DOCUMENT_ELM_SITE = "site";
	public static final String DOCUMENT_ATTR_REGEX= "@regex";
	public static final String DOCUMENT_ATTR_PERMISSIONS_NAME= "@name";

	// Notification config
	public static final String DOCUMENT_ELEMENT_GENERAL_MESSAGES = "//generalMessages";
	public static final String DOCUMENT_ELEMENT_COMPLETE_MESSAGES = "//completeMessages";
	public static final String DOCUMENT_ELEMENT_EMAIL_TEMPLATES = "//emailTemplates";
	public static final String DOCUMENT_ELEMENT_CANNED_MESSAGES = "//cannedMessages";
	public static final String DOCUMENT_ELEMENT_DEPLOYMENT_FAILURE_NOTIFICATION = "//deploymentFailureNotification";
	public static final String DOCUMENT_ELEMENT_APPROVER_EMAILS = "//approverEmails";
	public static final String DOCUMENT_ELEMENT_REPOSITORY_MERGE_CONFLICT_NOTIFICATION =
			"//repositoryMergeConflictNotification";

	private StudioXmlConstants() { }
}

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
package org.craftercms.cstudio.alfresco.constant;

import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * CStudio Content Model QNames
 * 
 * @author hyanghee
 *
 */
public interface CStudioContentModel {

	/** model namespaces **/
	public static final String NAMESPACE_ALFRESCO_URI = "http://www.alfresco.org/model";
	public static final String NAMESPACE_cstudio_CORE_URI = "http://cstudio/assets/core/1.0";	
	public static final String NAMESPACE_cstudio_CORE_WEB_URI = "http://cstudio.com/model/core-web/1.0";
	public static final String NAMESPACE_cstudio_WF_URI = "http://cstudio.com/model/cstudio-core-workflow/1.0";
	public static final String NAMESPACE_cstudio_WCM_WF_URI = "http://cstudio/model/wcmworkflow/1.0";
	
	/** model namespace prefixes **/
	public static final String NAMESPACE_ALFRESCO_PREFIX = "cm";
	public static final String NAMESPACE_cstudio_CORE_PREFIX = "cstudio-core";
	public static final String NAMESPACE_cstudio_CORE_WEB_PREFIX = "cstudio-core-web";
	public static final String NAMESPACE_cstudio_WF_PREFIX = "cstudio-core-workflow";
	public static final String NAMESPACE_cstudio_WCM_WF_PREFIX = "cstudiowcmwf";
	
	public static final String cstudio_NAMESPACE_PATTERN = "cstudio\\-.*";

	// core content models - aspects
	public static final QName ASPECT_ARTICLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "article");
	public static final QName ASPECT_IDENTIFIABLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "identifiable");
	public static final QName ASPECT_WCM_IDENTIFIABLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "wcmIdentifiable");
	public static final QName ASPECT_SEARCHABLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "searchable");
	public static final QName ASPECT_COMMON_METADATA = QName.createQName(NAMESPACE_cstudio_CORE_URI, "commonMetadata");
	public static final QName ASPECT_RENAMED = QName.createQName(NAMESPACE_cstudio_CORE_URI, "renamed");
	public static final QName ASPECT_IMAGE_METADATA = QName.createQName(NAMESPACE_cstudio_CORE_URI, "imageMetadata");
    public static final QName ASPECT_PREVIEWABLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "previewable");
    public static final QName ASPECT_PREVIEWABLE_DRAFT = QName.createQName(NAMESPACE_cstudio_CORE_URI, "previewableDraft");
    public static final QName ASPECT_CONFIGURATION_SPACE_EXPORT = QName.createQName(NAMESPACE_cstudio_CORE_URI,
        "configSpaceExport");

	
	// core web content models - aspects
	public static final QName ASPECT_COLLABORATIVE_SANDBOX = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "collaborativeSandbox");
	// web workflow models - aspects
	public static final QName ASPECT_WORKFLOW_SUBMITTED = QName.createQName(NAMESPACE_cstudio_WF_URI, "submitted");
	// core content models - properties

	/**************************************
	 * core content models - properties   *
	 **************************************/
	// common properties - searchable
	// public static final QName PROP_CONTNET_TITLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "contentTitle");
	// public static final QName PROP_INTERNAL_NAME = QName.createQName(NAMESPACE_cstudio_CORE_URI, "internalName");
	// public static final QName PROP_META_TITLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "metaTitle");
	// public static final QName PROP_META_KEYWORDS = QName.createQName(NAMESPACE_cstudio_CORE_URI, "metaKeywords");
	// public static final QName PROP_META_DESCRIPTION = QName.createQName(NAMESPACE_cstudio_CORE_URI, "metaDescription");
	// public static final QName PROP_SUMMARY = QName.createQName(NAMESPACE_cstudio_CORE_URI, "summary");
	// public static final QName PROP_PAGE_URL = QName.createQName(NAMESPACE_cstudio_CORE_URI, "pageUrl");
	// public static final QName PROP_CONTENT_TYPE= QName.createQName(NAMESPACE_cstudio_CORE_URI, "contentType");
	// public static final QName PROP_NAME = QName.createQName(NAMESPACE_cstudio_CORE_URI, "name");
	// public static final QName PROP_DESCRIPTION = QName.createQName(NAMESPACE_cstudio_CORE_URI, "description");	
	// public static final QName PROP_LABEL = QName.createQName(NAMESPACE_cstudio_CORE_URI, "label");
	// public static final QName PROP_FLOATING = QName.createQName(NAMESPACE_cstudio_CORE_URI, "floating");
	// public static final QName PROP_DISABLED = QName.createQName(NAMESPACE_cstudio_CORE_URI, "disabled");
	// public static final QName PROP_PLACEINNAV = QName.createQName(NAMESPACE_cstudio_CORE_URI, "placeInNav");
	// public static final QName PROP_ORDER_DEFAULT = QName.createQName(NAMESPACE_cstudio_CORE_URI, "orderDefault");
	// public static final QName PROP_TEMPLATE_VERSION = QName.createQName(NAMESPACE_cstudio_CORE_URI, "templateVersion");
	// // common properties
	// public static final QName PROP_ARTICLE_ID = QName.createQName(NAMESPACE_cstudio_CORE_URI, "articleId");
	// public static final QName PROP_IDENTIFIABLE_ID = QName.createQName(NAMESPACE_cstudio_CORE_URI, "id");
	// public static final QName PROP_IDENTIFIABLE_LABEL = QName.createQName(NAMESPACE_cstudio_CORE_URI, "label");
	// public static final QName PROP_IDENTIFIABLE_DESCRIPTION = QName.createQName(NAMESPACE_cstudio_CORE_URI, "label");
	// public static final QName PROP_IDENTIFIABLE_NAMESPACE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "namespace");
	// public static final QName PROP_IDENTIFIABLE_CURRENT = QName.createQName(NAMESPACE_cstudio_CORE_URI, "isCurrent");
	// public static final QName PROP_IDENTIFIABLE_NEW = QName.createQName(NAMESPACE_cstudio_CORE_URI, "new");
	// public static final QName PROP_IDENTIFIABLE_UPDATED = QName.createQName(NAMESPACE_cstudio_CORE_URI, "updated");
	// public static final QName PROP_IDENTIFIABLE_DELETED = QName.createQName(NAMESPACE_cstudio_CORE_URI, "isDeleted");
	// public static final QName PROP_IDENTIFIABLE_ORDER = QName.createQName(NAMESPACE_cstudio_CORE_URI, "order");
	// public static final QName PROP_IDENTIFIABLE_ICON_PATH = QName.createQName(NAMESPACE_cstudio_CORE_URI, "iconPath");
	// public static final QName PROP_WCM_IDENTIFIABLE_ID = QName.createQName(NAMESPACE_cstudio_CORE_URI, "wcmId");
	// public static final QName PROP_WCM_GROUP_IDENTIFIABLE_ID = QName.createQName(NAMESPACE_cstudio_CORE_URI, "wcmGroupId");
	// public static final QName PROP_IS_CURRENT = QName.createQName(NAMESPACE_cstudio_CORE_URI, "isCurrent");
	// public static final QName PROP_NAMESPACE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "namespace");
	// public static final QName PROP_SEQUENCE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "sequence");

	// // image properties 
	// public static final QName PROP_IMAGE_WIDTH = QName.createQName(NAMESPACE_cstudio_CORE_URI, "width");
	// public static final QName PROP_IMAGE_HEIGHT = QName.createQName(NAMESPACE_cstudio_CORE_URI, "height");

	
	// // readiness content models - properties
	// public static final QName PROP_AUTHOR = QName.createQName(NAMESPACE_cstudio_CORE_URI, "author");
	// public static final QName PROP_CREATE_DATE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "createDate");
	// public static final QName PROP_EXPIRATION_DATE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "expirationDate");
	// public static final QName PROP_LANGUAGE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "language");
	// public static final QName PROP_REPLACES_ID = QName.createQName(NAMESPACE_cstudio_CORE_URI, "replacesId");
	// public static final QName PROP_REVIEWER = QName.createQName(NAMESPACE_cstudio_CORE_URI, "reviewer");
	// public static final QName PROP_TITLE = QName.createQName(NAMESPACE_cstudio_CORE_URI, "title");
	// // core web content models - properties
	// public static final QName PROP_IS_LOCKED = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "isLocked");
	// public static final QName PROP_LOCKED_OWNER = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "lockedOwner");
	// public static final QName PROP_LAST_MODIFIED_BY = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "lastModifiedBy");
	// public static final QName PROP_CREATED_BY = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "createdBy");
	// public static final QName PROP_STATUS = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "status");
	// public static final QName PROP_REJECT_DATE = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "rejectDate");
	// public static final QName PROP_REJECTED_BY = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "rejectedBy");
	
	// //rename properties
	// public static final QName PROP_RENAMED_OLD_URL = QName.createQName(NAMESPACE_cstudio_CORE_URI, "oldUrl");
	// public static final QName PROP_RENAMED_DELETE_URL = QName.createQName(NAMESPACE_cstudio_CORE_URI, "renameDeleteUrl");

 //    // deployed properties
 //    public static final QName PROP_DEPLOYMENT_PATH = QName.createQName(NAMESPACE_cstudio_CORE_URI, "deploymentPath");
	
	// // content models - properties
	// public static final QName PROP_WEB_APPROVED_BY = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI, "approvedBy");
 //    public static final QName PROP_WEB_LAST_EDIT_DATE = QName.createQName(NAMESPACE_cstudio_CORE_WEB_URI,"lastEditDate");
	
	// // cstudio web workflow content models - properties
	// public static final QName PROP_WEB_WF_CHILDREN = QName.createQName(NAMESPACE_cstudio_WF_URI, "children");
	// public static final QName PROP_WEB_WF_ASSETS = QName.createQName(NAMESPACE_cstudio_WF_URI, "assets");
	// public static final QName PROP_WEB_WF_COMPONENTS = QName.createQName(NAMESPACE_cstudio_WF_URI, "components");
	// public static final QName PROP_WEB_WF_DOCUMENTS = QName.createQName(NAMESPACE_cstudio_WF_URI, "documents");
	// public static final QName PROP_WEB_WF_PARENT_URI = QName.createQName(NAMESPACE_cstudio_WF_URI, "parentUri");
	// public static final QName PROP_WEB_WF_SCHEDULED_DATE = QName.createQName(NAMESPACE_cstudio_WF_URI, "scheduledDate");
	// public static final QName PROP_WEB_WF_SEND_EMAIL = QName.createQName(NAMESPACE_cstudio_WF_URI, "sendEmail");
	// public static final QName PROP_WEB_WF_SUBMITTED_BY = QName.createQName(NAMESPACE_cstudio_WF_URI, "submittedBy");
	// public static final QName PROP_WEB_WF_SUBMITTEDFORDELETION = QName.createQName(NAMESPACE_cstudio_WF_URI, "submittedForDeletion");
	// public static final QName PROP_WEB_WF_CONTENT = QName.createQName(NAMESPACE_cstudio_WF_URI, "workflowFiles");
	// // cstudio wcm workflow content model - properties
	// public static final QName PROP_WCM_WF_SUBMITTED_BY = QName.createQName(NAMESPACE_cstudio_WCM_WF_URI, "submittedBy");

	// public static final RegexQNamePattern cstudio_PATTERN = new RegexQNamePattern("\\{http://cstudio/assets/.*");

	// public static final String MIMETYPE_XML = "@\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:text/xml";


}

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
 * General constants for WCM
 *
 * @author hyanghee
 *
 */
public class DmConstants {
	public final static String CONTENT_CHAIN_FORM = "formContent";
	public final static String CONTENT_CHAIN_ASSET = "assetContent";

    public final static String CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE = "simple";


	public final static String INDEX_FILE = "index.xml";
	public final static String XML_PATTERN = ".xml";
    public final static String CSS_PATTERN = ".css";
    public final static String JS_PATTERN = ".js";
	public final static String RENAME_WORKFLOW_PREFIX="Rename_Workflow";
	public final static String SCHEDULE_RENAME_WORKFLOW_PREFIX="Rename_Workflow_Scheduled";

	public final static String KEY_ACTIVITY_TYPE = "activityType";
	public final static String KEY_ALLOW_LESS_SIZE = "allowLessSize";
	public final static String KEY_WIDTH = "width";
	public final static String KEY_HEIGHT = "height";
	public final static String KEY_ALLOWED_WIDTH = "allowedWidth";
	public final static String KEY_ALLOWED_HEIGHT = "allowedHeight";
	public final static String KEY_CREATE_FOLDERS = "createFolders";
	public final static String KEY_CONTENT_TYPE = "contentType";
	public static final String KEY_EDIT = "edit";
	public final static String KEY_FILE_NAME = "fileName";
	public final static String KEY_FOLDER_PATH = "folderPath";
	public final static String KEY_FULL_PATH = "fullPath";
	public final static String KEY_IS_IMAGE = "isImage";
	public final static String KEY_IS_PREVIEW = "isPreview";
	public final static String KEY_NODE_REF = "nodeRef";
	public final static String KEY_OVERWRITE = "overwrite";
	public final static String KEY_PATH = "path";
	public final static String KEY_SITE = "site";
	public final static String KEY_SUB = "sub";
	public final static String KEY_USER = "user";
	public final static String KEY_SKIP_CLEAN_PREVIEW = "skipCleanPreview";
	public static final String KEY_COPIED_CONTENT = "copiedContent";
	public static final String KEY_PAGE_ID = "pageId";
	public static final String KEY_PAGE_GROUP_ID = "pageGroupId";
	public final static String KEY_UNLOCK = "unlock";
    public final static String KEY_SYSTEM_ASSET = "systemAsset";
	public final static String KEY_SKIP_AUDIT_LOG_INSERT = "skipAuditLogInsert";

	/** rename keys **/
	public final static String KEY_SOURCE_PATH = "sourcePath";
	public final static String KEY_TARGET_PATH = "targetPath";

	/* script object names */
	public final static String KEY_SCRIPT_DOCUMENT = "contentXml";
	public final static String KEY_CONTENT_LOADER = "contentLoader";

	/* TODO: move this to configuration */
	public static final String ROOT_PATTERN_PAGES = "/site/website";
	public static final String ROOT_PATTERN_COMPONENTS = "/site/(components|component-bindings|indexes|resources)";
	public static final String ROOT_PATTERN_ASSETS = "/static-assets";
	public static final String ROOT_PATTERN_DOCUMENTS = "/site/documents";
    public static final String ROOT_PATTERN_SYSTEM_COMPONENTS = "/site/system/page-components";

    public static final String DM_SCHEDULE_SUBMISSION_FLOW = "schedule_submission";

    public static final String CONTENT_LIFECYCLE_OPERATION ="contentLifecycleOperation" ;

    /* Workflow queries */
    public static final String JSON_KEY_ORDER_DEFAULT = "default";

    public static final String PUBLISHING_LOCK_KEY = "{SITE}_PUBLISHING_LOCK";

    public static final String KEY_APPLICATION_CONTEXT = "applicationContext";
}

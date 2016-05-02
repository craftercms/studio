/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import java.util.regex.Pattern;

/**
 * General constants for WCM
 * 
 * @author hyanghee
 * 
 */
public class DmConstants {
	public final static String CONTENT_CHAIN_FORM = "formContent";
	public final static String CONTENT_CHAIN_FORM_PREVIEW = "previewformContent";
	public final static String CONTENT_CHAIN_ASSET_PREVIEW = "previewAssetContent";
	public final static String CONTENT_CHAIN_PLAIN_PREVIEW = "previewPlainContent";
	public final static String CONTENT_CHAIN_IMPORT = "importContent";
	public final static String CONTENT_CHAIN_ASSET = "assetContent";
	public final static String CONTENT_CHAIN_ASSET_CLEAN_DRAFT = "assetContentCleanDraft";
	public final static String CONTENT_CHAIN_PLAIN = "plainContent";
	public final static String CONTENT_TYPE_PAGE= "pages";
	public final static String CONTENT_TYPE_ASSET= "assets";
	public final static String CONTENT_TYPE_COMPONENT= "components";
	public final static String CONTENT_TYPE_DOCUMENT= "documents";
    public final static String CONTENT_TYPE_RENDERING_TEMPLATE= "renderingTemplate";
    public final static String CONTENT_TYPE_FORM_DEFINITION= "formDefinition";
    public final static String CONTENT_TYPE_OTHER= "other";
    public final static String CONTENT_TYPE_ALL= "all";

    public final static String CONTENT_TYPE_CONFIG_FORM_PATH_SIMPLE = "simple";
	
	
	public final static String INDEX_FILE = "index.xml";
	public final static String XML_PATTERN = ".xml";
    public final static String CSS_PATTERN = ".css";
    public final static String JS_PATTERN = ".js";
	public final static String RENAME_WORKFLOW_PREFIX="Rename_Workflow";
	public final static String SCHEDULE_RENAME_WORKFLOW_PREFIX="Rename_Workflow_Scheduled";
	public final static String GOLIVE_WORKFLOW_PREFIX="Golive_Workflow";

	public final static String KEY_ACTIVITY_TYPE = "activityType";
	public final static String KEY_ALLOW_LESS_SIZE = "allowLessSize";
	public final static String KEY_WIDTH = "width";
	public final static String KEY_HEIGHT = "height";
	public final static String KEY_ALLOWED_WIDTH = "allowedWidth";
	public final static String KEY_ALLOWED_HEIGHT = "allowedHeight";
	public final static String KEY_ASSIGNEE = "assignee";
	public final static String KEY_AUTO_DEPLOY = "autoDeploy";
	public final static String KEY_CREATE_FOLDERS = "createFolders";
	public final static String KEY_CONTENT = "content";
	public final static String KEY_CONTENT_TYPE = "contentType";
	public final static String KEY_DEPENDENCY = "dependency";
	public final static String KEY_DESCRIPTION = "description";
	public final static String KEY_DOCUMENT = "document";
	public static final String KEY_EDIT = "edit";
	public final static String KEY_FILE_NAME = "fileName";
	public final static String KEY_FOLDER_PATH = "folderPath";
	public final static String KEY_FULL_PATH = "fullPath";
	public final static String KEY_ID = "id";
	public final static String KEY_IS_IMAGE = "isImage";
	public final static String KEY_IS_PREVIEW = "isPreview";
	public final static String KEY_LABEL = "label";
	public final static String KEY_LAUNCH_DATE = "launchDate";
	public final static String KEY_MIME_TYPE = "mimeType";
	public final static String KEY_NODE_REF = "nodeRef";
	public final static String KEY_OVERWRITE = "overwrite";
	public final static String KEY_PATH = "path";
	public final static String KEY_PATHS = "paths";
	public final static String KEY_PRIORITY = "priority";
	public final static String KEY_SEND_EMAIL = "sandEmail";
	public final static String KEY_SUBMIT_DIRECT = "submitDirect";
	public final static String KEY_SITE = "site";
	public final static String KEY_SUB = "sub";
	public final static String KEY_SANDBOX = "sandbox";
	public final static String KEY_USER = "user";
	public final static String KEY_SKIP_CLEAN_PREVIEW = "skipCleanPreview";
	public final static String KEY_WORKFLOW_NAME = "workflowName";
	public static final String KEY_COPIED_CONTENT = "copiedContent";
	public static final String KEY_PAGE_ID = "pageId";
	public static final String KEY_PAGE_GROUP_ID = "pageGroupId";
	public final static String KEY_UNLOCK = "unlock";
    public final static String KEY_SYSTEM_ASSET = "systemAsset";
	
	/** rename keys **/
	public final static String KEY_SOURCE_PATH = "sourcePath";
	public final static String KEY_SOURCE_FULL_PATH = "sourceFullPath";
	public final static String KEY_TARGET_PATH = "targetPath";
	public final static String KEY_TARGET_FULL_PATH = "targetFullPath";

	/* script object names **/
	public final static String KEY_SCRIPT_CONVERTER = "converter";
	public final static String KEY_SCRIPT_DOCUMENT = "contentXml";
	public final static String KEY_SCRIPT_NODE = "contentNode";
	public final static String KEY_SCRIPT_STORE = "store";
	public final static String KEY_CONTENT_LOADER = "contentLoader";

	/** TODO: move this to configuration **/
	public static final String ROOT_PATTERN_PAGES = "/site/website";
	public static final String ROOT_PATTERN_COMPONENTS = "/site/(components|component-bindings|indexes|resources)";
	public static final String ROOT_PATTERN_ASSETS = "/static-assets";
	public static final String ROOT_PATTERN_DOCUMENTS = "/site/documents";
    public static final String ROOT_PATTERN_SYSTEM_COMPONENTS = "/site/system/page-components";

	public static final String DM_STATUS_IN_PROGRESS = "In Progress";
	public static final String DM_STATUS_SCHEDULED = "Scheduled";
	public static final String DM_STATUS_SUBMITTED = "Submitted";
	public static final String DM_STATUS_LIVE = "Live";
	public static final String DM_STORE_CONTENT = "content";

    public static final String DM_STATUS_SCHEDULE_DELETE = "Schedule_Delete";
    public static final String DM_SCHEDULE_SUBMISSION_FLOW = "schedule_submission";

	//public final static String WEB_PROJECT_ROOT = JNDIConstants.DIR_DEFAULT_WWW + "/" + JNDIConstants.DIR_DEFAULT_APPBASE;
    public final static String DM_WEM_PROJECT_ROOT = "/app:company_home";
	//public final static Pattern WEBAPP_RELATIVE_PATH_PATTERN = Pattern.compile("([^:]+:/"
	//		+ WEB_PROJECT_ROOT + "/([^/]+))(.*)");
    public final static Pattern DM_WEBAPP_RELATIVE_PATH_PATTERN = Pattern.compile("(/wem-projects/[-\\w]*/[-\\w]*/work-area)(/.*)");
    public final static Pattern DM_SITE_PATH_PATTERN = Pattern.compile("(/wem-projects/[-\\w]*/[-\\w]*)([-\\w\\s]*)(/.*)");
    public final static Pattern DM_SITE_LIVE_PATH_PATTERN = Pattern.compile("(/wem-projects/[-\\w]*/)([-\\w]*)/live(/.*)");
    public static final String CONTENT_LIFECYCLE_OPERATION ="contentLifecycleOperation" ;
    
    /* Workflow queries */
    /*public static final String SUBMITTED_ITEMS_QUERY = (new StringBuilder("PATH:\"/app:company_home{site_root}//*\" AND"))
            .append(CStudioContentModel.PROP_STATUS.toString())
            .append(":\"")
            .append(DM_STATUS_SUBMITTED)
            .append("\"")
            .toString();
*/
    public static final String DM_WEM_PROJECTS_FOLDER = "wem-projects";
    public static final String DM_WORK_AREA_REPO_FOLDER = "work-area";
    public static final String DM_LIVE_REPO_FOLDER = "live";
    public static final String DM_DRAFT_REPO_FOLDER = "draft";
    public static final Pattern DM_REPO_TYPE_PATH_PATTERN = Pattern.compile("(/wem-projects/[-\\w]*/)([-\\w]*/)(work-area|live|draft)(/.*)");
    public static final Pattern DM_WORK_AREA_PATH_PATTERN = Pattern.compile("/wem-projects/[-\\w]*/[-\\w]*/work-area/.*");
    public static final Pattern DM_LIVE_PATH_PATTERN = Pattern.compile("/wem-projects/[-\\w]*/[-\\w]*/live/.*");
    public static final Pattern DM_DRAFT_PATH_PATTERN = Pattern.compile("/wem-projects/[-\\w]*/[-\\w]*/draft/.*");
    public static final String DM_REPO_PATH_PATTERN_STRING =
            (new StringBuilder()).append("/")
            .append("(").append(DM_WEM_PROJECTS_FOLDER).append(")")
            .append("/")
            .append("(").append("[-\\w]*").append(")")
            .append("/")
            .append("(").append("[-\\w]*").append(")")
            .append("/")
            .append("(").append(DM_WORK_AREA_REPO_FOLDER).append("|").append(DM_LIVE_REPO_FOLDER).append("|").append(DM_DRAFT_REPO_FOLDER).append(")")
            .append("(/.*)").toString();
    public static final String DM_MULTI_REPO_PATH_PATTERN_STRING =

            (new StringBuilder()).append("/")
                    .append("(").append(DM_WEM_PROJECTS_FOLDER).append(")")
                    .append("/")
                    .append("(").append("[-\\w]*").append(")")
                    .append("/")
                    .append("(").append("[-\\w]*").append(")")
                    .append("/")
                    .append("(").append("[-\\w\\s]*").append(")")
                    .append("(/.*)").toString();
    public static final Pattern DM_REPO_PATH_PATTERN = Pattern.compile(DM_REPO_PATH_PATTERN_STRING);

    public static final Pattern DM_MULTI_REPO_PATH_PATTERN = Pattern.compile(DM_MULTI_REPO_PATH_PATTERN_STRING);

    public static final String JSON_KEY_ORDER_DEFAULT = "default";

    public static final String CACHE_CSTUDIO_SITE_SCOPE = "CStudioSite_{site}";

    public static final String PUBLISHING_LOCK_KEY = "{SITE}_PUBLISHING_LOCK";

    public static final Pattern PATTERN_ACTIVITY_FEED_INTERNAL_NAME = Pattern.compile("^.*\"internalName\":\"(.*?)\".*$");
    public static final String KEY_APPLICATION_CONTEXT = "applicationContext";
/*
    public static QName[] SUBMITTED_PROPERTIES = new QName[]{
            CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY,
            CStudioContentModel.PROP_WEB_WF_SEND_EMAIL,
            CStudioContentModel.PROP_WEB_WF_SUBMITTEDFORDELETION//,
            //PORT Version2Model.PROP_QNAME_VERSION_DESCRIPTION,
            //PORTWCMWorkflowModel.PROP_LAUNCH_DATE
    };*/
}

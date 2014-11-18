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
package org.craftercms.cstudio.alfresco.dm.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.alfresco.config.JNDIConstants;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DM utility methods
 *
 * @author hyanghee
 * @author Dejan Brkic
 */
public class DmUtils {

    private static final Logger logger = LoggerFactory.getLogger(DmUtils.class);
    
    private static final String DRAFT_FOLDER = "draft";
    private static final String WORK_AREA_PATH = "/work-area";

    protected static final String LIST_CHANGES_QUERY = "PATH:\"/app:company_home{site_root}//*\" AND NOT @cm\\:versionLabel:*\\.0";
    /**
     * create a URI that is meaningful for the end user by removing the category root path, index.xml and changing .xml to .html
     * TODO: move this method to another config class that will support URL transformation in a better way
     *
     * @param uri
     * @param replacePattern
     * @param isPage
     * @return broswer URI
     */
    public static String getBrowserUri(String uri, String replacePattern, boolean isPage) {
        String browserUri = uri.replaceFirst(replacePattern, "");
        browserUri = browserUri.replaceFirst("/" + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = "/";
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
    }

    /**
     * get Store root path
     *
     * @param store
     * @return the Store root path
     */
    public static String getStoreRootPath(String store) {
        return store + ':' + JNDIConstants.DIR_DEFAULT_WWW_APPBASE;
    }

    /**
     * Returns the parent of give URL
     *
     * @param url
     * @return
     */
    public static String getParentUrl(String url) {
        int lastIndex = url.lastIndexOf("/");
        return url.substring(0, lastIndex);
    }

    /**
     * create store preview store name given the site name and the sandbox name
     *
     * @param site
     * @return store name
     */
    public static String createPreviewStoreName(String site) {
        return site + "--preview";
    }
    
    public static String getDraftFolder(String path) {
    	if (path.indexOf(DRAFT_FOLDER) != -1) {
    		return path;
    	} 
    	if (path != null && path.startsWith("/"))
    		return "/" + DRAFT_FOLDER  + path;
    	else 
    		return "/" + DRAFT_FOLDER + "/" + path;
    }
    
    public static String getAbsoluteDraftPath(String path, String parentPath) {
    	if (path.startsWith(parentPath)) {  // Managing new temp folder
    		path = path.substring(parentPath.length());
        }
    	parentPath = cleanRepositoryPath(parentPath);
    	
    	return parentPath + getDraftFolder(path);
    }

    /**
     * change the given full path to preview full path
     *
     * @param fullPath
     * @return preview path
     */
    public static String getPreviewPath(String fullPath) {
        return getDraftFolder(fullPath);
    }

    public static boolean isContentXML(String uri){
        if(StringUtils.isNotEmpty(uri) && uri.endsWith(DmConstants.XML_PATTERN)){
            return true;
        }
        return false;
    }

    /**
     * get a boolean value from the given string
     *
     * @param value
     * @param defaultToTrue
     * @return
     */
    public static boolean getBooleanValue(String value, boolean defaultToTrue) {
        if (StringUtils.isEmpty(value)) {
            return (defaultToTrue) ? true : false;
        } else if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else {
            return (defaultToTrue) ? true : false;
        }

    }

    /**
     * create store name given the site name
     *
     * @param site
     * @return store name
     */
    public static String createStoreName(String site) {
        return site;
    }

    /**
     * does the given path matches one of the path patterns?
     *
     * @param path
     * @param patterns
     * @return true if the path matches one of the patterns
     */
    public static boolean matchesPattern(String path, List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (path.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param path
     * @return
     */
    public static String getIndexFilePath(String path){
        if(!path.endsWith(DmConstants.XML_PATTERN)){
            path = path + "/" + DmConstants.INDEX_FILE;
        }
        return path;
    }

    public static String getAssignee(String site, String sub) {
        // TODO: find assignee from configuration
        return AuthenticationUtil.getAdminUserName();
    }


    /**
     * add the item into the submittedBy mapping given
     *
     * @param persistenceManagerService
     * @param contentService
     * @param searchService
     * @param site
     * @param relativePath
     * @param submittedBy
     * @param approver
     * 			submitted by becomes the approver name if it was not submitted by the regular user
     */
    public static void addToSubmittedByMapping(PersistenceManagerService persistenceManagerService, DmContentService contentService, SearchService searchService,
                                               String site, String relativePath, Map<String, String> submittedBy, String approver) {
        String fullPath = contentService.getContentFullPath(site, relativePath);
        String initiator = DmUtils.getStringProperty(persistenceManagerService, fullPath, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
        if (initiator != null) {
            submittedBy.put(relativePath, initiator);
        } else {
            submittedBy.put(relativePath, approver);
        }
    }


    /**
     * get string property
     *
     * @param persistenceManagerService
     * @param fullPath
     * @param propertyName
     * @return
     */
    public static String getStringProperty(PersistenceManagerService persistenceManagerService, String fullPath, QName propertyName) {
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        if (nodeRef == null) return null;
        Serializable value = persistenceManagerService.getProperty(nodeRef, propertyName);
        return (value != null) ? (String)value : null;
    }

    public static boolean isRenameWorkflow(String desc) {
        return desc.startsWith(DmConstants.RENAME_WORKFLOW_PREFIX);
    }

    public static boolean areEqual(Date oldDate, Date newDate) {
        if (oldDate == null && newDate == null) {
            return true;
        }
        if (oldDate != null && newDate != null) {
            return oldDate.equals(newDate);
        }
        return false;
    }

    /**
     * Returns the page name part (eg.index.xml) of a given URL
     *
     * @param url
     * @return
     */
    public static String getPageName(String url) {
        int lastIndex = url.lastIndexOf("/");
        return url.substring(lastIndex + 1);
    }

    /**
     * create content full path given the store root and the content path within
     * the sandbox
     *
     * @param site              site to find the content from
     * @param path              content path within the sandbox
     * @param servicesConfig
     * @return content full path
     */
    public static String getContentFullPath(String site, String path,
                                            ServicesConfig servicesConfig) {
        StringBuilder sbFullPath = new StringBuilder(servicesConfig.getRepositoryRootPath(site));
        sbFullPath.append(path);
        return sbFullPath.toString();
    }

    /**
     * get a relative path from the given avm path
     *
     * @param avmPath
     * @return relative path
     */
    public static String getRelativePath(String avmPath) {
        final Matcher m = DmConstants.WEBAPP_RELATIVE_PATH_PATTERN.matcher(avmPath);
        return m.matches() && m.group(3).length() != 0 ? m.group(3) : "/";
    }
    
    public static  String getNodePath(PersistenceManagerService persistenceManagerService, NodeRef node) {
        List<FileInfo> pathParts = null;
        try {
            pathParts = persistenceManagerService.getNamePath(persistenceManagerService.getCompanyHomeNodeRef(), node);
        } catch (FileNotFoundException e) {
            logger.error("ERROR: ", e);
        } catch (Exception e) {
            logger.error("ERROR: ", e);
        }
        String nodePath = "";
        if (pathParts != null) {
            for (FileInfo pathPart : pathParts) {
                nodePath = nodePath + "/" + pathPart.getName();
            }
        }
        return nodePath;
    }
       
    public static NodeRef getNodeRef(PersistenceManagerService persistenceManagerService, String path) {
    	return getNodeRef(persistenceManagerService, persistenceManagerService.getCompanyHomeNodeRef(), path);
    }
    
    public static NodeRef getNodeRef(PersistenceManagerService persistenceManagerService, NodeRef rootNode, String path) {
        return persistenceManagerService.getNodeRef(rootNode, path);
    }
    
    /**
	 * Return non-folder paths 
	 */
	public static List<String> getChildrenUri(PersistenceManagerService persistenceManagerService, SearchService searchService, NodeRef node, List<String> paths){
		FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
        if (nodeInfo.isFolder()) {
            List<FileInfo> children = persistenceManagerService.list(node);
            for (FileInfo child : children) {
            	getChildrenUri(persistenceManagerService, searchService, child.getNodeRef(), paths);
            }
        } else {
        	List<FileInfo> pathParts = null;
            try {
                pathParts = persistenceManagerService.getNamePath(persistenceManagerService.getCompanyHomeNodeRef(), node);
            } catch (FileNotFoundException e) {
                logger.error("ERROR: ", e);
            }
            String nodePath = "";
            for (FileInfo pathPart : pathParts) {
                nodePath = nodePath + "/" + pathPart.getName();
            }
	    	paths.add(nodePath);
        }
		return paths; 
    }
    
    public static String getMd5ForFile(InputStream input) {
        //PushbackInputStream helper = null;
        String result = null;
        MessageDigest md = null;
        try {
            //helper = new PushbackInputStream(input);
            //InputStreamReader reader = new InputStreamReader(input);
            md = MessageDigest.getInstance("MD5");

            md.reset();
            byte[] bytes = new byte[1024];
            int numBytes;
            //input.mark(input.available());
            input.mark(Integer.MAX_VALUE);
            while ((numBytes = input.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            result = new String(Hex.encodeHex(digest));
            input.reset();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while creating MD5 digest", e);
        } catch (IOException e) {
            logger.error("Error while reading input stream", e);
        } finally {

        }
        return result;
    }
    
    public static String getMd5ForFile(String data) {
    	InputStream is = null;
    	String fileName = null;
    
    	try {
    		is = new ByteArrayInputStream(data.getBytes("UTF-8"));
        
    		fileName = getMd5ForFile(is);
    	} catch(UnsupportedEncodingException e) {
    		logger.error("Error while creating MD5 digest", e);
    	}
    	return fileName;
    }

    /**
     * add the item into the submittedBy mapping given 
     *
     * @param nodeService
     * @param contentService
     * @param site
     * @param relativePath
     * @param submittedBy
     * @param approver
     * 			submitted by becomes the approver name if it was not submitted by the regular user
     */
    public static void addToSubmittedByMapping(PersistenceManagerService persistenceManagerService, DmContentService contentService,
                                               String site, String relativePath, Map<String, String> submittedBy, String approver) {
        String fullPath = contentService.getContentFullPath(site, relativePath);
        NodeRef nodeRef = getNodeRef(persistenceManagerService, fullPath);
        //String initiator = DmUtils.getStringProperty(avmService, fullPath, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
        Serializable initiatorValue = persistenceManagerService.getProperty(nodeRef, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
        String initiator = (initiatorValue == null ? null : (String)initiatorValue);
        if (initiator != null) {
            submittedBy.put(relativePath, initiator);
        } else {
            submittedBy.put(relativePath, approver);
        }
    }

    public static boolean isBootStrapWorkflow(String desc) {
        return desc.startsWith("build");
    }

    public static boolean isScheduleSubmission(String desc) {
        return desc.startsWith("schedule_workflow") || desc.startsWith(DmConstants.SCHEDULE_RENAME_WORKFLOW_PREFIX);
    }
    
    public static List<NodeRef> getChangeSet(SearchService searchService, ServicesConfig servicesConfig, String site) {
        return searchService.findNodes(CStudioConstants.STORE_REF, getListChangedQuery(servicesConfig, site));
    }

    public static String getListChangedQuery(ServicesConfig servicesConfig, String site) {
        String siteRootPath = servicesConfig.getRepositoryRootPath(site);
        String[] pathSegments = siteRootPath.split("//");
        StringBuilder siteRoot = new StringBuilder();
        for (String segment : pathSegments) {
            if (StringUtils.isNotEmpty(segment)) {
                siteRoot.append("/cm:").append(segment);
            }
        }
        return LIST_CHANGES_QUERY.replace("{site_root}", siteRoot.toString());
    }
    
    public static String getSiteFromFullPath(String fullPath) {
        String[] segments;
        if (fullPath.startsWith("/")) {
            segments = fullPath.substring(1).split("/");
        } else {
            segments = fullPath.split("/");
        }
        if (segments.length > 2) {
            return segments[2];
        } else {
            return null;
        }
    }
    
    public static String cleanRepositoryPath(String fullPath) {
    	int idx = -1;
    	if (fullPath.endsWith(WORK_AREA_PATH)) {
    		idx = fullPath.length() - WORK_AREA_PATH.length();
    	} else if (fullPath.endsWith(DRAFT_FOLDER)) {
    		idx = fullPath.length() - DRAFT_FOLDER.length();
    	}
    	
    	if (idx != -1) {
    		fullPath = fullPath.substring(0, idx);
    	}
    	return fullPath;
    	
    }
}

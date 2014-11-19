/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.content.pipeline.api.DmContentProcessor;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ActivityService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class is the same as FormDmContentProcess but without adding versionable aspect
 * 
 * @author hyanghee
 *
 */
public class ImportDmContentProcessor extends PathMatchProcessor implements DmContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ImportDmContentProcessor.class);

    public static final String NAME = "ImportDmContentProcessor";

    protected ServicesManager _servicesManager;
    public ServicesManager getServicesManager() {
        return _servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this._servicesManager = servicesManager;
    }

    /**
     * default constructor
     */
    public ImportDmContentProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ImportDmContentProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        try {
            writeContent(content);
        } catch (ServiceException e) {
            logger.error("Failed to write " + content.getId(),e);
            throw new ContentProcessException("Failed to write " + content.getId(), e);
        } finally {
            content.closeContentStream();
        }
    }

    /**
     * write content to the given path
     *
     * @param site
     *            site to write the content to
     * @param user
     * 			content owner
     * @param path
     *            path to write the content to
     * @param fileName
     *            content name
     * @param contentType
     * 				content type
     * @param input
     *            content input stream
     * @param createFolders
     * 				create missing folders?
     * @param overwrite
     * 				overwrite the existing content?
     * @param isPreview
     * @param unlock
     * 			unlock the content upon update?
     * @throws ServiceException
     */
    protected ActivityService.ActivityType writeContent(String site, String user, String path, String fileName, String contentType, InputStream input,
                                                        boolean createFolders, boolean overwrite, boolean isPreview, boolean unlock) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        String contentPath = getContentFullPath(site, path, isPreview);
        /* Disable DRAFT repo Dejan 29.03.2012 */
        //contentPath = (isPreview) ? DmUtils.getPreviewPath(contentPath) : contentPath;
        /***************************************/
        try {
            // look up the path content first
            NodeRef parentContent = persistenceManagerService.getNodeRef(contentPath);
            if (parentContent == null && createFolders) {
                parentContent = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentContent != null) {
                // if the parent content name is the same as the file name
                // update the content
                // look up the path content first
                FileInfo parentContentInfo = persistenceManagerService.getFileInfo(parentContent);
                if (parentContentInfo.getName().equals(fileName)) {
                    updateFile(site, parentContent, contentPath, input, user, isPreview, unlock);
                    return ActivityService.ActivityType.UPDATED;
                } else {
                    // otherwise, create new one
                    //contentPath = contentPath.replaceFirst(WcmConstants.INDEX_FILE, "");
                    String parentContentPath = persistenceManagerService.getNodePath(parentContent);
                    String fileFullPath =  parentContentPath + "/" + fileName;
                    NodeRef fileNode = persistenceManagerService.getNodeRef(fileFullPath);
                    if (fileNode != null && overwrite) {
                        updateFile(site, fileNode, fileFullPath, input, user, isPreview, unlock);
                        return ActivityService.ActivityType.UPDATED;
                    } else {
                        createNewFile(site, parentContent, fileName, contentType, input, user);
                        return ActivityService.ActivityType.CREATED;
                    }
                }
            } else {
                throw new ContentNotFoundException(path + " does not exist in site: " + site);
            }
        } catch (ContentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error: ", e);
            throw new ContentNotFoundException("Unexpected exception ", e);
        } finally {
            ContentUtils.release(input);
        }

    }

    protected void writeContent(PipelineContent content) throws ServiceException {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        String user = content.getProperty(DmConstants.KEY_USER);
        String site = content.getProperty(DmConstants.KEY_SITE);
        String path = content.getProperty(DmConstants.KEY_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String contentType = content.getProperty(DmConstants.KEY_CONTENT_TYPE);
        InputStream input = content.getContentStream();
        boolean isPreview = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_IS_PREVIEW));
        boolean createFolders = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_CREATE_FOLDERS));
        String unlockValue = content.getProperty(DmConstants.KEY_UNLOCK);
        boolean unlock = (!StringUtils.isEmpty(unlockValue) && unlockValue.equalsIgnoreCase("false")) ? false : true;
        boolean overwrite = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_OVERWRITE));

        String contentPath = getContentFullPath(site, path, isPreview);
        //contentPath = (isPreview) ? DmUtils.getPreviewPath(contentPath) : contentPath;
        try {
            // look up the path content first
            NodeRef parentContent = persistenceManagerService.getNodeRef(contentPath);
            if (parentContent == null && createFolders) {
                parentContent = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentContent != null) {
                // if the parent content name is the same as the file name
                // update the content
                // look up the path content first
                FileInfo parentContentInfo = persistenceManagerService.getFileInfo(parentContent);
                if (parentContentInfo.getName().equals(fileName)) {
                    InputStream existingContent = persistenceManagerService.getReader(parentContent).getContentInputStream();
                    String existingMd5 = DmUtils.getMd5ForFile(existingContent);
                    String newMd5 = DmUtils.getMd5ForFile(input);
                    if (!existingMd5.equals(newMd5)) {
                        updateFile(site, parentContent, contentPath, input, user, isPreview, unlock);
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.UPDATED.toString());
                    } else {
                        if (!isPreview) {
                            DmPathTO pathTO = new DmPathTO(contentPath);
                            if (cancelWorkflow(site, pathTO.getRelativePath())) {
                                DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
                                DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
                                dmWorkflowService.removeFromWorkflow(site, null, pathTO.getRelativePath(), true);
                                dmDependencyService.updateDependencies(site,pathTO.getRelativePath(), DmConstants.DM_STATUS_IN_PROGRESS);
                            } else {
                                if(updateWorkFlow(site,pathTO.getRelativePath())) {
                                    DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
                                    dmWorkflowService.updateWorkflowSandboxes(site,pathTO.getRelativePath());
                                }
                            }
                        }
                    }
                    if (unlock) {
                        persistenceManagerService.unlock(parentContent);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Unlocked the content " + contentPath);
                        }
                    }
                    return;
                } else {
                    // otherwise, create new one
                    //contentPath = contentPath.replaceFirst(WcmConstants.INDEX_FILE, "");
                    String parentContentPath = persistenceManagerService.getNodePath(parentContent);
                    if (parentContentPath.endsWith(DmConstants.XML_PATTERN) && !parentContentPath.endsWith(DmConstants.INDEX_FILE)){
                        parentContentPath = parentContentPath.substring(0, parentContentPath.lastIndexOf("/"));
                        parentContent = persistenceManagerService.getNodeRef(parentContentPath);
                    }
                    String fileFullPath =  parentContentPath + "/" + fileName;
                    NodeRef fileNode = persistenceManagerService.getNodeRef(fileFullPath);
                    if (fileNode != null && overwrite) {
                        InputStream existingContent = persistenceManagerService.getReader(fileNode).getContentInputStream();
                        String existingMd5 = DmUtils.getMd5ForFile(existingContent);
                        String newMd5 = DmUtils.getMd5ForFile(input);
                        if (!existingMd5.equals(newMd5)) {
                            updateFile(site, fileNode, fileFullPath, input, user, isPreview, unlock);
                            content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.UPDATED.toString());
                        } else {
                            if (!isPreview) {
                                DmPathTO pathTO = new DmPathTO(fileFullPath);
                                if (cancelWorkflow(site, pathTO.getRelativePath())) {
                                    DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
                                    DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
                                    dmWorkflowService.removeFromWorkflow(site, null, pathTO.getRelativePath(), true);
                                    dmDependencyService.updateDependencies(site,pathTO.getRelativePath(), DmConstants.DM_STATUS_IN_PROGRESS);
                                } else {
                                    if(updateWorkFlow(site,pathTO.getRelativePath())) {
                                        DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
                                        dmWorkflowService.updateWorkflowSandboxes(site,pathTO.getRelativePath());
                                    }
                                }
                            }
                        }
                        if (unlock) {
                            persistenceManagerService.unlock(fileNode);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Unlocked the content " + fileFullPath);
                            }
                        }
                        return;
                    } else {
                        NodeRef newFile = createNewFile(site, parentContent, fileName, contentType, input, user, unlock);
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.CREATED.toString());
                        content.setActionedNodeRef(newFile);
                        return;
                    }
                }
            } else {
                throw new ContentNotFoundException(path + " does not exist in site: " + site);
            }
        } catch (ContentNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error: ", e);
            throw new ContentNotFoundException("Unexpected exception ", e);
        } finally {
            ContentUtils.release(input);
        }

    }

    protected NodeRef createNewFile(String site, NodeRef parentNode, String fileName, String contentType, InputStream input, String user)
    	throws ContentNotFoundException {
    	return createNewFile(site, parentNode, fileName, contentType,  input, user, true);
    }

    /**
     * create new file to the given path. If the path is a file name, it will
     * create a new folder with the same name as the file name (without the
     * prefix) and move the existing file to the folder created. Then it creates
     * new file to the folder
     *
     * @param site
     *            Site name
     * @param parentNode
     *            Parent node
     * @param fileName
     *            new file name
     * @param contentType
     * 			content type
     * @param input
     *            file content
     * @param user
     *            current user
     * @throws ContentNotFoundException
     */
    protected NodeRef createNewFile(String site, NodeRef parentNode, String fileName, String contentType, InputStream input,
    		String user, boolean unlock)
            throws ContentNotFoundException {
        // if the given path is a file, create a folder as the same name
        // and move the file under the folder and change the name to be
        // index.xml
        //String folderPath = fullPath;
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        OutputStream output = null;
        NodeRef fileNode = null;

        if (parentNode != null) {
            // convert file to folder if target path is a file
            String folderPath = fileToFolder(parentNode);
            // create new content, apply group sandbox aspect, and apply new
            // aspect
            // input stream is closed by AvmService
            try {
                Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
                nodeProperties.put(CStudioContentModel.PROP_CONTENT_TYPE, contentType);
                nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
                nodeProperties.put(CStudioContentModel.PROP_CREATED_BY, user);
                nodeProperties.put(ContentModel.PROP_MODIFIER, user);
                nodeProperties.put(CStudioContentModel.PROP_STATUS, DmConstants.DM_STATUS_IN_PROGRESS);
                nodeProperties.put(CStudioContentModel.PROP_WEB_LAST_EDIT_DATE, new Date());
//                nodeProperties.put(ContentModel.PROP_AUTO_VERSION, false);
                fileNode = persistenceManagerService.createNewFile(parentNode, fileName, input, nodeProperties);
            } catch (Exception e) {
                logger.error("Error writing new file: " + fileName, e);
            } finally {
                if (output != null) ContentUtils.release(output);
                IOUtils.closeQuietly(input);
            }
            persistenceManagerService.addAspect(fileNode, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
            persistenceManagerService.lock(fileNode, LockType.WRITE_LOCK);

            if (!persistenceManagerService.hasAspect(fileNode, CStudioContentModel.ASPECT_PREVIEWABLE)) {
                persistenceManagerService.addAspect(fileNode, CStudioContentModel.ASPECT_PREVIEWABLE, new HashMap<QName, Serializable>());
                if (!persistenceManagerService.hasAspect(parentNode, CStudioContentModel.ASPECT_PREVIEWABLE)) {
                    persistenceManagerService.addAspect(parentNode, CStudioContentModel.ASPECT_PREVIEWABLE, new HashMap<QName, Serializable>());
                }
            }

            // unlock the content upon save
            if (unlock) {
                persistenceManagerService.unlock(fileNode);
            } else {
            }

            return fileNode;
        } else {
            throw new ContentNotFoundException(persistenceManagerService.getNodePath(parentNode) + " does not exist in site: " + site);
        }
    }

    /**
     * get content full path
     *
     * @param site
     * @param path
     * @return the content full path
     */
    protected String getContentFullPath(String site, String path, boolean isPreview) {
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        StringBuilder sbFullPath = new StringBuilder(servicesConfig.getRepositoryRootPath(site));
        /* Disable DRAFT repo Dejan 29.03.2012 */
        /*
        if (isPreview) {
        	sbFullPath = new StringBuilder(DmUtils.cleanRepositoryPath(sbFullPath.toString()));
        	path = DmUtils.getDraftFolder(path);
        }*/
        /**************************************/
        sbFullPath.append(path);
        return sbFullPath.toString();
    }

    protected String getContentFullPath(String site, String path) {
        return getContentFullPath(site, path, false);
    }


    /**
     * update the file at the given content node
     *
     * @param contentNode
     * @param fullPath
     * @param input
     * @param user
     * @param isPreview
     * @param unlock
     * 			unlock the content upon update?
     * @throws ServiceException
     */
    protected void updateFile(String site, NodeRef contentNode, String fullPath, InputStream input, String user, boolean isPreview, boolean unlock)
            throws ServiceException {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
        DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);

    	LockStatus lockStatus = persistenceManagerService.getLockStatus(contentNode);
        String nodeStatus = (String)persistenceManagerService.getProperty(contentNode, CStudioContentModel.PROP_STATUS);
        /**
         * added to handle issue with submitting locked content (Dejan 2012/04/12)
         */
        if (LockStatus.NO_LOCK.equals(lockStatus) || LockStatus.LOCK_OWNER.equals(lockStatus) ||
                (nodeStatus.equalsIgnoreCase(DmConstants.DM_STATUS_SUBMITTED))
        		)
        /****** end ******/
        {
            OutputStream output = null;
            try {
                output = persistenceManagerService.getWriter(contentNode).getContentOutputStream();
                IOUtils.copy(input, output);
            } catch (IOException e) {
                logger.error("Failed to write content to " + fullPath);
                throw new ServiceException(e);
            } finally {
                ContentUtils.release(output);
                ContentUtils.release(input);
            }

            if (!persistenceManagerService.hasAspect(contentNode, ContentModel.ASPECT_LOCKABLE)) {
                persistenceManagerService.addAspect(contentNode, ContentModel.ASPECT_LOCKABLE, null);
            }

            Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(contentNode);
            nodeProperties.put(ContentModel.PROP_MODIFIER, user);
            nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
            nodeProperties.put(CStudioContentModel.PROP_WEB_LAST_EDIT_DATE, new Date());
            persistenceManagerService.setProperties(contentNode, nodeProperties);
            // unlock the content upon save if the flag is true
            if (unlock) {
                persistenceManagerService.unlock(contentNode);
                if (logger.isDebugEnabled()) {
                    logger.debug("Unlocked the content " + fullPath);
                }
            } else {
                persistenceManagerService.lock(contentNode, LockType.WRITE_LOCK);
            }
            // if there is anything pending and this is not a preview update, cancel workflow
            if (!isPreview) {
                DmPathTO path = new DmPathTO(fullPath);
                if (cancelWorkflow(site, path.getRelativePath())) {
                    dmWorkflowService.removeFromWorkflow(site, null, path.getRelativePath(), true);
                    dmDependencyService.updateDependencies(site,path.getRelativePath(), DmConstants.DM_STATUS_IN_PROGRESS);
                } else {
                    if(updateWorkFlow(site,path.getRelativePath())) {
                        dmWorkflowService.updateWorkflowSandboxes(site,path.getRelativePath());
                    }
                }
            }
        } else {
            String owner = (String)persistenceManagerService.getProperty(contentNode, ContentModel.PROP_LOCK_OWNER);
            throw new ServiceException("The content is locked by another user: " + owner);
        }
    }

    /**
     * cancel the pending workflow upon editing the content at the given path?
     *
     * @param site
     * @param path
     * @return
     */
    protected boolean cancelWorkflow(String site, String path) {
        // don't cancel if the content is a level descriptor
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        if (path.endsWith(servicesConfig.getLevelDescriptorName(site))) {
            return true;
        } else {
            List<String> pagePatterns = servicesConfig.getPagePatterns(site);
            // cancel if the content is a page
            if (DmUtils.matchesPattern(path, pagePatterns)) {
                return true;
            }

            List<String> componentPatterns = servicesConfig.getComponentPatterns(site);
            if (DmUtils.matchesPattern(path, componentPatterns)) {
                return true;
            }

            // Checking for document also
            List<String> documentPatterns = servicesConfig.getDocumentPatterns(site);
            // cancel if the content is a document
            if (DmUtils.matchesPattern(path, documentPatterns)) {
                return true;
            }
        }
        return false;
    }

    protected boolean updateWorkFlow(String site,String path) {
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        List<String> assetPatterns = servicesConfig.getAssetPatterns(site);
        return  DmUtils.matchesPattern(path,assetPatterns);
    }

    @Override
    public NodeRef createMissingFoldersInPath(String site, String path, boolean isPreview) {
        // create parent folders if missing
        String [] levels = path.split("/");
        String parentPath = "";
        NodeRef lastNode = null;
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        for (String level : levels) {
            if (!StringUtils.isEmpty(level) && !level.endsWith(DmConstants.XML_PATTERN)) {
                String currentPath = parentPath + "/" + level;
                String fullPath = getContentFullPath(site, currentPath, isPreview);
                //fullPath = (isPreview) ? DmUtils.getPreviewPath(fullPath) : fullPath;
                lastNode = persistenceManagerService.getNodeRef(fullPath);
                if (lastNode == null) {
                    String parentFullPath = getContentFullPath(site, parentPath, isPreview);
                    //parentFullPath = (isPreview) ? DmUtils.getPreviewPath(parentFullPath) : parentFullPath;
                    NodeRef parentNode = persistenceManagerService.getNodeRef(parentFullPath);
                    Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
                    nodeProperties.put(ContentModel.PROP_NAME, level);
                    lastNode = persistenceManagerService.createNewFolder(parentNode, level, nodeProperties);
                }
                parentPath = currentPath;
            }
        }
        return lastNode;
    }


    @Override
    public String fileToFolder(NodeRef fileNode) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        FileInfo fileInfo = persistenceManagerService.getFileInfo(fileNode);
        if (fileInfo.isFolder()) {
            return persistenceManagerService.getNodePath(fileNode);
        }
        String path = persistenceManagerService.getNodePath(fileNode);
        int index = path.lastIndexOf("/");
        String folderPath = path.substring(0, index);
        String parentFileName = fileInfo.getName();
        String folderName = parentFileName.substring(0, parentFileName.indexOf("."));
        Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        nodeProperties.put(ContentModel.PROP_NAME, folderName);
        NodeRef newFolderNode = persistenceManagerService.createNewFolder(folderPath, folderName, nodeProperties);
        try {
            persistenceManagerService.move(fileNode, newFolderNode, DmConstants.INDEX_FILE);
        } catch (FileNotFoundException e) {
            logger.error("Unable to change file to folder from " + path + " to " + folderPath);
            return null;
        }
        folderPath = folderPath + "/" + folderName;
        if (logger.isDebugEnabled()) {
            logger.debug("Changed file to folder from " + path + " to " + folderPath);
        }
        return folderPath;
    }
}

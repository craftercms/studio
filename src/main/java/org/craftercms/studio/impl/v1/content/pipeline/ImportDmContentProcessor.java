/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.DmContentProcessor;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * this class is the same as FormDmContentProcess but without adding versionable aspect
 * 
 * @author hyanghee
 *
 */
public class ImportDmContentProcessor extends PathMatchProcessor implements DmContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ImportDmContentProcessor.class);

    public static final String NAME = "ImportDmContentProcessor";

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
        String contentPath = contentService.expandRelativeSitePath(site, path);

        try {
            // look up the path content first
            boolean contentExists = contentService.contentExists(site, path);
            ContentItemTO parentContent = contentService.getContentItem(site, path);
            if (contentExists && createFolders) {
                parentContent = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentContent != null) {
                // if the parent content name is the same as the file name
                // update the content
                // look up the path content first
                if (parentContent.getName().equals(fileName)) {
                    updateFile(site, parentContent, contentPath, input, user, unlock);
                    return ActivityService.ActivityType.UPDATED;
                } else {
                    // otherwise, create new one
                    //contentPath = contentPath.replaceFirst(WcmConstants.INDEX_FILE, "");
                    String fileFullPath =  contentService.expandRelativeSitePath(site, path + "/" + fileName);
                    ContentItemTO contentItem = contentService.getContentItem(site, path + "/" + fileName);
                    if (contentItem != null && overwrite) {
                        updateFile(site, contentItem, fileFullPath, input, user, unlock);
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

        String contentPath = contentService.expandRelativeSitePath(site, path);
        //contentPath = (isPreview) ? DmUtils.getPreviewPath(contentPath) : contentPath;
        try {
            // look up the path content first
            ContentItemTO parentContent = contentService.getContentItem(site, path);
            if (parentContent == null && createFolders) {
                parentContent = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentContent != null) {
                // if the parent content name is the same as the file name
                // update the content
                // look up the path content first
                if (parentContent.getName().equals(fileName)) {
                    InputStream existingContent = contentService.getContent(site, path);
                    String existingMd5 = ContentUtils.getMd5ForFile(existingContent);
                    String newMd5 = ContentUtils.getMd5ForFile(input);
                    if (!existingMd5.equals(newMd5)) {
                        updateFile(site, parentContent, contentPath, input, user, unlock);
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.UPDATED.toString());
                    } else {
                        if (!isPreview) {
                            if (cancelWorkflow(site, path)) {
                                workflowService.removeFromWorkflow(site, path, true);
                                dmDependencyService.updateDependencies(site,path, DmConstants.DM_STATUS_IN_PROGRESS);
                            } else {
                                if(updateWorkFlow(site, path)) {
                                    workflowService.updateWorkflowSandboxes(site, path);
                                }
                            }
                        }
                    }
                    if (unlock) {
                        //TODO: unlock content
                        //persistenceManagerService.unlock(parentContent);
                        logger.debug("Unlocked the content " + contentPath);
                    }
                    return;
                } else {
                    // otherwise, create new one
                    //contentPath = contentPath.replaceFirst(WcmConstants.INDEX_FILE, "");
                    String parentContentPath = contentService.expandRelativeSitePath(site, path);
                    if (parentContentPath.endsWith(DmConstants.XML_PATTERN) && !parentContentPath.endsWith(DmConstants.INDEX_FILE)){
                        parentContentPath = parentContentPath.substring(0, parentContentPath.lastIndexOf("/"));
                        String partentRelativePath = contentService.getRelativeSitePath(site, parentContentPath);
                        parentContent = contentService.getContentItem(site, partentRelativePath);
                    }
                    String fileFullPath =  parentContentPath + "/" + fileName;
                    String relativePath = contentService.getRelativeSitePath(site, fileFullPath);
                    ContentItemTO fileItem = contentService.getContentItem(site, relativePath);
                    if (fileItem != null && overwrite) {
                        InputStream existingContent = contentService.getContent(site, relativePath);
                        String existingMd5 = ContentUtils.getMd5ForFile(existingContent);
                        String newMd5 = ContentUtils.getMd5ForFile(input);
                        if (!existingMd5.equals(newMd5)) {
                            updateFile(site, fileItem, fileFullPath, input, user, unlock);
                            content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.UPDATED.toString());
                        } else {
                                if (cancelWorkflow(site, relativePath)) {
                                    workflowService.removeFromWorkflow(site, relativePath, true);
                                    dmDependencyService.updateDependencies(site, relativePath, DmConstants.DM_STATUS_IN_PROGRESS);
                                } else {
                                    if(updateWorkFlow(site, relativePath)) {
                                        workflowService.updateWorkflowSandboxes(site, relativePath);
                                    }
                                }

                        }
                        if (unlock) {
                            //TODO: unlock content
                            //persistenceManagerService.unlock(fileNode);
                            logger.debug("Unlocked the content " + fileFullPath);
                        }
                        return;
                    } else {
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, ActivityService.ActivityType.CREATED.toString());
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

    protected ContentItemTO createNewFile(String site, ContentItemTO parentItem, String fileName, String contentType, InputStream input, String user)
    	throws ContentNotFoundException {
    	return createNewFile(site, parentItem, fileName, contentType,  input, user, true);
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
    protected ContentItemTO createNewFile(String site, ContentItemTO parentItem, String fileName, String contentType, InputStream input,
    		String user, boolean unlock)
            throws ContentNotFoundException {
        // if the given path is a file, create a folder as the same name
        // and move the file under the folder and change the name to be
        // index.xml
        //String folderPath = fullPath;

        if (parentItem != null) {
            // convert file to folder if target path is a file
            String folderPath = fileToFolder(site, parentItem.getPath());
            // create new content, apply group sandbox aspect, and apply new
            // aspect
            // input stream is closed by AvmService
            try {
                /*
                Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
                nodeProperties.put(CStudioContentModel.PROP_CONTENT_TYPE, contentType);
                nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
                nodeProperties.put(CStudioContentModel.PROP_CREATED_BY, user);
                nodeProperties.put(ContentModel.PROP_MODIFIER, user);
                nodeProperties.put(CStudioContentModel.PROP_STATUS, DmConstants.DM_STATUS_IN_PROGRESS);
                nodeProperties.put(CStudioContentModel.PROP_WEB_LAST_EDIT_DATE, new Date());
//                nodeProperties.put(ContentModel.PROP_AUTO_VERSION, false);
*/
                contentService.writeContent(site, parentItem.getPath() + "/" + fileName, input);
            } catch (Exception e) {
                logger.error("Error writing new file: " + fileName, e);
            } finally {
                IOUtils.closeQuietly(input);
            }
            // TODO: lock content
            /*
            persistenceManagerService.addAspect(fileNode, ContentModel.ASPECT_LOCKABLE, new HashMap<QName, Serializable>());
            persistenceManagerService.lock(fileNode, LockType.WRITE_LOCK);
*/
            // TODO: make conent previewable
            /*
            if (!persistenceManagerService.hasAspect(fileNode, CStudioContentModel.ASPECT_PREVIEWABLE)) {
                persistenceManagerService.addAspect(fileNode, CStudioContentModel.ASPECT_PREVIEWABLE, new HashMap<QName, Serializable>());
                if (!persistenceManagerService.hasAspect(parentNode, CStudioContentModel.ASPECT_PREVIEWABLE)) {
                    persistenceManagerService.addAspect(parentNode, CStudioContentModel.ASPECT_PREVIEWABLE, new HashMap<QName, Serializable>());
                }
            }*/

            // unlock the content upon save
            if (unlock) {
                // TODO: unlock content
                //persistenceManagerService.unlock(fileNode);
            } else {
            }

            ContentItemTO fileItem = contentService.getContentItem(site, parentItem.getPath() + "/" + fileName);
            return fileItem;
        } else {
            String parentPath = contentService.expandRelativeSitePath(site, parentItem.getPath());
            throw new ContentNotFoundException(parentPath + " does not exist in site: " + site);
        }
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
    protected void updateFile(String site, ContentItemTO contentItem, String fullPath, InputStream input, String user, boolean unlock)
            throws ServiceException {

        // TODO: check lock status
        /*
    	LockStatus lockStatus = persistenceManagerService.getLockStatus(contentNode);
        String nodeStatus = (String)persistenceManagerService.getProperty(contentNode, CStudioContentModel.PROP_STATUS);
        /**
         * added to handle issue with submitting locked content (Dejan 2012/04/12)
         *//*
        if (LockStatus.NO_LOCK.equals(lockStatus) || LockStatus.LOCK_OWNER.equals(lockStatus) ||
                (nodeStatus.equalsIgnoreCase(DmConstants.DM_STATUS_SUBMITTED))
        		)
        /****** end ******/
        //{
            try {
                contentService.writeContent(fullPath, input);
            } finally {
                ContentUtils.release(input);
            }

        // TODO: make content lockable
        /*    if (!persistenceManagerService.hasAspect(contentNode, ContentModel.ASPECT_LOCKABLE)) {
                persistenceManagerService.addAspect(contentNode, ContentModel.ASPECT_LOCKABLE, null);
            }
*/
        // TODO: update content properties
        /*
            Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(contentNode);
            nodeProperties.put(ContentModel.PROP_MODIFIER, user);
            nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
            nodeProperties.put(CStudioContentModel.PROP_WEB_LAST_EDIT_DATE, new Date());
            persistenceManagerService.setProperties(contentNode, nodeProperties);
            */
            // unlock the content upon save if the flag is true
            if (unlock) {
                //persistenceManagerService.unlock(contentNode);
                logger.debug("Unlocked the content " + fullPath);

            } else {
                // TODO: lock content
                //persistenceManagerService.lock(contentNode, LockType.WRITE_LOCK);
            }
                String relativePath = contentService.getRelativeSitePath(site, fullPath);
                if (cancelWorkflow(site, relativePath)) {
                    workflowService.removeFromWorkflow(site, relativePath, true);
                    dmDependencyService.updateDependencies(site, relativePath, DmConstants.DM_STATUS_IN_PROGRESS);
                } else {
                    if(updateWorkFlow(site, relativePath)) {
                        workflowService.updateWorkflowSandboxes(site, relativePath);
                    }
                }

        /*} else {
            String owner = (String)persistenceManagerService.getProperty(contentNode, ContentModel.PROP_LOCK_OWNER);
            throw new ServiceException("The content is locked by another user: " + owner);
        }*/
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
        if (path.endsWith(servicesConfig.getLevelDescriptorName(site))) {
            return true;
        } else {
            List<String> pagePatterns = servicesConfig.getPagePatterns(site);
            // cancel if the content is a page
            if (ContentUtils.matchesPatterns(path, pagePatterns)) {
                return true;
            }

            List<String> componentPatterns = servicesConfig.getComponentPatterns(site);
            if (ContentUtils.matchesPatterns(path, componentPatterns)) {
                return true;
            }

            // Checking for document also
            List<String> documentPatterns = servicesConfig.getDocumentPatterns(site);
            // cancel if the content is a document
            if (ContentUtils.matchesPatterns(path, documentPatterns)) {
                return true;
            }
        }
        return false;
    }

    protected boolean updateWorkFlow(String site,String path) {
        List<String> assetPatterns = servicesConfig.getAssetPatterns(site);
        return  ContentUtils.matchesPatterns(path,assetPatterns);
    }

    @Override
    public ContentItemTO createMissingFoldersInPath(String site, String path, boolean isPreview) {
        // create parent folders if missing
        String [] levels = path.split("/");
        String parentPath = "";
        ContentItemTO lastItem = null;
        for (String level : levels) {
            if (!StringUtils.isEmpty(level) && !level.endsWith(DmConstants.XML_PATTERN)) {
                String currentPath = parentPath + "/" + level;
                String fullPath = contentService.expandRelativeSitePath(site, currentPath);
                lastItem = contentService.getContentItem(site, currentPath);
                if (lastItem == null) {
                    String parentFullPath = contentService.expandRelativeSitePath(site, parentPath);
                    ContentItemTO parentItem = contentService.getContentItem(site, parentPath);
                    contentService.createFolder(site, parentPath, level);
                    lastItem = contentService.getContentItem(site, currentPath);
                }
                parentPath = currentPath;
            }
        }
        return lastItem;
    }


    @Override
    public String fileToFolder(String site, String path) {
        // Check if it is already a folder
        /*
        if (fileInfo.isFolder()) {
            return persistenceManagerService.getNodePath(fileNode);
        }*/
        ContentItemTO itemTO = contentService.getContentItem(site, path);
        int index = path.lastIndexOf("/");
        String folderPath = path.substring(0, index);
        String parentFileName = itemTO.getName();
        String folderName = parentFileName.substring(0, parentFileName.indexOf("."));

        //Map<QName, Serializable> nodeProperties = new FastMap<QName, Serializable>();
        //nodeProperties.put(ContentModel.PROP_NAME, folderName);
        //NodeRef newFolderNode = persistenceManagerService.createNewFolder(folderPath, folderName, nodeProperties);
        contentService.createFolder(site, folderPath, folderName);
        folderPath = folderPath + "/" + folderName;
        contentService.moveContent(site, path, folderPath + "/" + DmConstants.INDEX_FILE);
        logger.debug("Changed file to folder from " + path + " to " + folderPath);

        return folderPath;
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public WorkflowService getWorkflowService() { return workflowService; }
    public void setWorkflowService(WorkflowService workflowService) { this.workflowService = workflowService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    protected ContentService contentService;
    protected DmDependencyService dmDependencyService;
    protected WorkflowService workflowService;
    protected ServicesConfig servicesConfig;
}

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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.DmContentProcessor;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.exception.RepositoryLockedException;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;

public class FormDmContentProcessor extends PathMatchProcessor implements DmContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FormDmContentProcessor.class);

    public static final String NAME = "WriteContentToDmProcessor";

    protected ContentService contentService;
    protected WorkflowService workflowService;
    protected ServicesConfig servicesConfig;
    protected ContentRepository contentRepository;
    protected ItemServiceInternal itemServiceInternal;
    protected SiteService siteService;
    protected UserServiceInternal userServiceInternal;
    protected org.craftercms.studio.api.v1.repository.ContentRepository contentRepositoryV1;
    protected org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2;

    /**
     * default constructor
     */
    public FormDmContentProcessor() {
        super(NAME); 
    }
 
    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public FormDmContentProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        try {
            writeContent(content, result);
        } catch (ServiceLayerException e) {
            logger.error("Failed to write " + content.getId(),e);
            throw new ContentProcessException("Failed to write " + content.getId(), e);
        } finally {
            content.closeContentStream();
        }
    }

    protected void writeContent(PipelineContent content, ResultTO result) throws ServiceLayerException {
        String user = content.getProperty(DmConstants.KEY_USER);
        String site = content.getProperty(DmConstants.KEY_SITE);
        String path = content.getProperty(DmConstants.KEY_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        InputStream input = content.getContentStream();
        boolean isPreview = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_IS_PREVIEW));
        boolean createFolders = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_CREATE_FOLDERS));
        String unlockValue = content.getProperty(DmConstants.KEY_UNLOCK);
        boolean unlock = (!StringUtils.isEmpty(unlockValue) && unlockValue.equalsIgnoreCase("false")) ? false : true;

        String parentContentPath = path;
        if (parentContentPath.endsWith(FILE_SEPARATOR + fileName)) {
            parentContentPath = parentContentPath.replace(FILE_SEPARATOR + fileName, "");
        } else {
            path = path + FILE_SEPARATOR + fileName;
        }
        try {
            // look up the path content first
            ContentItemTO parentItem = contentService.getContentItem(site, parentContentPath, 0);
            boolean parentContentExists = contentService.contentExists(site, parentContentPath);
            if (!parentContentExists && createFolders) {
                parentItem = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentItem != null) {
                // if the parent content name is the same as the file name
                // update the content
                // look up the path content first
                if (parentItem.getName().equals(fileName)) {
                    ContentItemTO item = contentService.getContentItem(site, path, 0);

                    updateFile(site, item, path, input, user, isPreview, unlock, result);
                    content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, OPERATION_UPDATE);
                    if (unlock) {
                        unlock(site, path);
                    }
                    return;
                } else {
                    // otherwise, create new one
                    if (path.endsWith(DmConstants.XML_PATTERN) && !path.endsWith(DmConstants.INDEX_FILE)){
                        parentContentPath = path.substring(0, path.lastIndexOf(FILE_SEPARATOR));
                        parentItem = contentService.getContentItem(site, parentContentPath, 0);
                    }

                    boolean fileExists = contentService.contentExists(site, path);
                    if (fileExists) {
                        ContentItemTO contentItem = contentService.getContentItem(site, path, 0);
                        updateFile(site, contentItem, path, input, user, isPreview, unlock, result);
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, OPERATION_UPDATE);
                        if (unlock) {
                            unlock(site, path);
                        }
                        return;
                    } else {
                        createNewFile(site, parentItem, fileName, input, user, unlock, result);
                        content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, OPERATION_CREATE);
                        return;
                    }
                }
            } else {
                throw new ContentNotFoundException(path + " does not exist in site: " + site);
            }
        } catch (ContentNotFoundException | RepositoryLockedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error: ", e);
            throw new ContentNotFoundException("Unexpected exception ", e);
        } finally {
            ContentUtils.release(input);
        }

    }

    // For backward compatibility ignore the exception
    protected void unlock(String siteId, String path) throws ContentNotFoundException {
        try {
            contentServiceV2.unlockContent(siteId, path);
            logger.debug("Unlocked the content site: {0} path: {1}", siteId, path);
        } catch (ContentAlreadyUnlockedException e) {
            logger.debug("Content already unlocked site: {0} path: {1}", siteId, path);
        }
    }

    /**
     * create new file to the given path. If the path is a file name, it will
     * create a new folder with the same name as the file name (without the
     * prefix) and move the existing file to the folder created. Then it creates
     * new file to the folder
     *
     * @param site
     *            Site name
     * @param fileName
     *            new file name
     * @param input
     *            file content
     * @param user
     *            current user
     * @throws ContentNotFoundException
     */
    protected ContentItemTO createNewFile(String site, ContentItemTO parentItem, String fileName, InputStream input,
                                          String user, boolean unlock, ResultTO result)
            throws ServiceLayerException {
        ContentItemTO fileItem = null;

        if (parentItem != null) {
            String itemPath = parentItem.getUri() + FILE_SEPARATOR + fileName;
            itemPath = itemPath.replaceAll(FILE_SEPARATOR + FILE_SEPARATOR, FILE_SEPARATOR);
            try {
                contentService.writeContent(site, itemPath, input);
                String commitId = contentRepository.getRepoLastCommitId(site);
                result.setCommitId(commitId);

                // Item
                // TODO: get local code with API 2
                String parentItemPath =
                        ContentUtils.getParentUrl(itemPath.replace(FILE_SEPARATOR + INDEX_FILE, ""));
                Item pItem = itemServiceInternal.getItem(site, parentItemPath, true);
                itemServiceInternal.persistItemAfterCreate(site, itemPath, user, commitId, Optional.of(unlock),
                        pItem.getId());
            } catch (Exception e) {
                logger.error("Error writing new file: " + fileName, e);
            } finally {
                IOUtils.closeQuietly(input);
            }

            // unlock the content upon save
            if (unlock) {
                contentRepositoryV1.unLockItem(site, itemPath);
            } else {
                contentRepository.lockItem(site, itemPath);
            }

            fileItem = contentService.getContentItem(site, itemPath, 0);
            return fileItem;
        } else {
            throw new ContentNotFoundException(parentItem.getUri() + " does not exist in site: " + site);
        }
    }


    /**
     * update the file at the given content node
     *
     * @param input
     * @param user
     * @param isPreview
     * @param unlock
     * 			unlock the content upon update?
     * @throws ServiceLayerException
     */
    protected void updateFile(String site, ContentItemTO contentItem, String path, InputStream input, String user,
                              boolean isPreview, boolean unlock, ResultTO result)
            throws ServiceLayerException, UserNotFoundException {

        boolean success = false;
        try {
            success = contentService.writeContent(site, path, input);
        } finally {
            ContentUtils.release(input);
        }

        if (success) {
            String commitId = contentRepository.getRepoLastCommitId(site);
            result.setCommitId(commitId);

            // if there is anything pending and this is not a preview update, cancel workflow
            if (!isPreview) {
                if (cancelWorkflow(site, path)) {
                    workflowService.removeFromWorkflow(site, path, true);
                } else {
                    if (updateWorkFlow(site, path)) {
                        workflowService.updateWorkflowSandboxes(site, path);
                    }
                }
            }

            // Item
            // TODO: get local code with API 2
            itemServiceInternal.persistItemAfterWrite(site, path, user, commitId, Optional.of(unlock));
        }

        // unlock the content upon save if the flag is true
        if (unlock) {
            contentRepositoryV1.unLockItem(site, path);
            logger.debug("Unlocked the content site: " + site + " path: " + path);
        } else {
            contentRepository.lockItem(site, path);
        }
    }

    /**
     * cancel the pending workflow upon editing the content at the given path?
     *
     * @param site
     * @param path
     * @return true if workflow needs to be canceled
     */
    protected boolean cancelWorkflow(String site, String path) {
        // don't cancel if the content is a level descriptor
        if (path.endsWith(servicesConfig.getLevelDescriptorName(site))) {
            return false;
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

            // Checking for display patterns also
            List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
            // cancel if the content is a document
            if (ContentUtils.matchesPatterns(path, displayPatterns)) {
                return true;
            }
        }
        return false;
    }

    protected boolean updateWorkFlow(String site,String path) {
        List<String> assetPatterns = servicesConfig.getAssetPatterns(site);
        return  ContentUtils.matchesPatterns(path, assetPatterns);
    }

    @Override
    public ContentItemTO createMissingFoldersInPath(String site, String path, boolean isPreview)
            throws ServiceLayerException, UserNotFoundException {
        // create parent folders if missing
        String [] levels = path.split(FILE_SEPARATOR);
        String parentPath = "";
        ContentItemTO lastItem = null;
        for (String level : levels) {
            if (!StringUtils.isEmpty(level) && !level.endsWith(DmConstants.XML_PATTERN)) {
                String currentPath = parentPath + FILE_SEPARATOR + level;
                if (!contentService.contentExists(site, currentPath)) {
                    contentService.createFolder(site, parentPath, level);
                }
                parentPath = currentPath;
            }
        }
        lastItem = contentService.getContentItem(site, parentPath, 0);
        return lastItem;
    }


    @Override
    public String fileToFolder(String site, String path) throws ServiceLayerException, UserNotFoundException {
        // Check if it is already a folder

        if (contentService.contentExists(site, path)) {
            ContentItemTO itemTO = contentService.getContentItem(site, path, 0);
            if (itemTO.isFolder() || itemTO.isDeleted()) {
                return  path;
            }
            int index = path.lastIndexOf(FILE_SEPARATOR);
            String folderPath = path.substring(0, index);
            String parentFileName = itemTO.getName();
            int dotIndex = parentFileName.indexOf(".");
            String folderName = (dotIndex > 0) ? parentFileName.substring(0, parentFileName.indexOf(".")) : parentFileName;
            contentService.createFolder(site, folderPath, folderName);
            folderPath = folderPath + FILE_SEPARATOR + folderName;
            contentService.moveContent(site, path, folderPath + FILE_SEPARATOR + DmConstants.INDEX_FILE);
            logger.debug("Changed file to folder from " + path + " to " + folderPath);

            return folderPath;
        } else {
            return path;
        }
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public WorkflowService getWorkflowService() {
        return workflowService;
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public org.craftercms.studio.api.v1.repository.ContentRepository getContentRepositoryV1() {
        return contentRepositoryV1;
    }

    public void setContentRepositoryV1(org.craftercms.studio.api.v1.repository.ContentRepository contentRepositoryV1) {
        this.contentRepositoryV1 = contentRepositoryV1;
    }

    public void setContentServiceV2(org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2) {
        this.contentServiceV2 = contentServiceV2;
    }

}

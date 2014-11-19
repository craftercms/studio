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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDependencyService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.api.DmVersionService;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentAssetInfoTO;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

public class AssetDmContentProcessor extends org.craftercms.cstudio.alfresco.dm.content.pipeline.impl.FormDmContentProcessor {

    public static final int READ_BUFFER_LENGTH = 32 * 1024;
    public static final String FILE_SIZE_MB = "MB";
    public static final String FILE_SIZE_KB = "KB";

    private static final Logger logger = LoggerFactory.getLogger(AssetDmContentProcessor.class);

    public static final String NAME = "WriteAssetToDmProcessor";
    
    protected String _assetsSystemPath;
    public String getAssetsSystemPath() {
        return _assetsSystemPath;
    }
    public void setAssetsSystemPath(String assetsSystemPath) {
        this._assetsSystemPath = assetsSystemPath;
    }

    /**
     * default constructor
     */
    public AssetDmContentProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public AssetDmContentProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String site = content.getProperty(DmConstants.KEY_SITE);
        String user = content.getProperty(DmConstants.KEY_USER);
        String path = content.getProperty(DmConstants.KEY_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String widthStr = content.getProperty(DmConstants.KEY_WIDTH);
        String heightStr = content.getProperty(DmConstants.KEY_HEIGHT);
        int width = (widthStr != null) ? Integer.parseInt(widthStr) : -1;
        int height = (heightStr != null) ? Integer.parseInt(heightStr) : -1;
        String unlockValue = content.getProperty(DmConstants.KEY_UNLOCK);
        // default is true for unlocking on save
        boolean unlock = (!StringUtils.isEmpty(unlockValue) && unlockValue.equalsIgnoreCase("false")) ? false : true;
        boolean isPreview = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_IS_PREVIEW));
        boolean isSystemAsset = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SYSTEM_ASSET));
        boolean createFolders = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_CREATE_FOLDERS));
        try {
            ContentAssetInfoTO oldAssetInfo = (ContentAssetInfoTO) result.getItem();
            ContentAssetInfoTO assetInfo = writeContentAsset(site, user, path, fileName,
                    content.getContentStream(), width, height, createFolders, isPreview, unlock, isSystemAsset);
            if (oldAssetInfo != null) {
                oldAssetInfo.setFileExtension(assetInfo.getFileExtension());
                oldAssetInfo.setFileName(assetInfo.getFileName());
                oldAssetInfo.setNodeRef(assetInfo.getNodeRef());
                oldAssetInfo.setSize(assetInfo.getSize());
                oldAssetInfo.setSizeUnit(assetInfo.getSizeUnit());
                result.setItem(oldAssetInfo);
            } else {
                result.setItem(assetInfo);
            }
        } catch (ServiceException e) {
            throw new ContentProcessException("Failed to write " + content.getId(), e);
        } finally {
            content.closeContentStream();
        }
    }

    /**
     * upload content asset to the given path
     *
     * @param site
     * @param path
     * @param assetName
     * @param in
     *            input stream to read the asset from
     * @param width
     * @param height
     * @param createFolders
     * 				create missing folders?
     * @param isPreview
     * @param unlock
     * 			unlock the content upon update?
     * @return asset information
     * @throws org.craftercms.cstudio.alfresco.service.exception.ContentNotAllowedException
     * @throws ServiceException
     */
    protected ContentAssetInfoTO writeContentAsset(String site, String user, String path, String assetName, InputStream in,
                                                   int width, int height, boolean createFolders, boolean isPreview, boolean unlock, boolean isSystemAsset)
            throws ServiceException {
        if (logger.isDebugEnabled()) {
            logger.debug("Writing content asset: [site: " + site + ", path: " + path + ", assetName: " + assetName + ", createFolders: " + createFolders);
        }
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
        String ext = null;
        int index = assetName.lastIndexOf(".");
        if (index > 0 && (index + 1) < assetName.length()) {
            ext = assetName.substring(index + 1).toUpperCase();
        }
        String folderPath = getContentFullPath(site, path, isPreview);

        if (isSystemAsset) {
            assetName = DmUtils.getMd5ForFile(in) + "." + ext;
        }
        String contentPath = folderPath + "/" + assetName;

        try {
            // look up the path content first
            NodeRef parentContent = persistenceManagerService.getNodeRef(folderPath);
            if (parentContent == null && createFolders) {
                parentContent = createMissingFoldersInPath(site, path, isPreview);
            }
            if (parentContent != null && persistenceManagerService.getFileInfo(parentContent).isFolder()) {
                NodeRef content = persistenceManagerService.getNodeRef(contentPath);
                if (content != null) {
                    updateFile(site, content, contentPath, in, user, isPreview, unlock);
                } else {
                    String contentType = namespaceService.getPrefixedTypeName(ContentModel.TYPE_CONTENT);
                    content = createNewFile(site, parentContent, assetName, contentType, in, user);
                    persistenceManagerService.insertNewObjectEntry(content);
                }
                persistenceManagerService.addAspect(content, ContentModel.ASPECT_LOCKABLE, null);
                if (!persistenceManagerService.hasAspect(content, CStudioContentModel.ASPECT_IMAGE_METADATA)) {
                    // if no metadata found, add the metdata
                    persistenceManagerService.addAspect(content, CStudioContentModel.ASPECT_IMAGE_METADATA, null);
                }
                // store the height and the width calculated
                Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(content);
                nodeProperties.put(CStudioContentModel.PROP_IMAGE_WIDTH, width);
                nodeProperties.put(CStudioContentModel.PROP_IMAGE_HEIGHT, height);
                persistenceManagerService.setProperties(content, nodeProperties);

                ContentAssetInfoTO assetInfo = new ContentAssetInfoTO();
                assetInfo.setFileName(assetName);
                ContentReader reader = persistenceManagerService.getReader(persistenceManagerService.getNodeRef(contentPath));
                ContentData data = reader.getContentData();
                long size = data.getSize();
                double convertedSize = size / new Double(READ_BUFFER_LENGTH * 32) * 1024;
                if(convertedSize >= 1024){
                    assetInfo.setSizeUnit(FILE_SIZE_MB);
                    assetInfo.setSize(convertedSize/1024);
                }else{
                    if(convertedSize > 0 && convertedSize < 1){
                        assetInfo.setSize(1);
                    }else{
                        assetInfo.setSize(Math.round(convertedSize));
                    }

                    assetInfo.setSizeUnit(FILE_SIZE_KB);
                }
                //assetInfo.setSize(size / new Double(CStudioWebScriptConstants.READ_BUFFER_LENGTH * 32) * 1024);
                assetInfo.setFileExtension(ext);
                return assetInfo;
            } else {
                throw new ServiceException(path + " does not exist or not a directory.");
            }
        } finally {
            ContentUtils.release(in);
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
    protected void updateFile(String site, NodeRef contentNode, String fullPath, InputStream input, String user, boolean isPreview, boolean unlock)
            throws ServiceException {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        DmDependencyService dmDependencyService = getServicesManager().getService(DmDependencyService.class);
        DmWorkflowService dmWorkflowService = getServicesManager().getService(DmWorkflowService.class);
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

        //TODO: Check if the content has changed to update VERSIONING or keep the same version.
        if (!persistenceManagerService.hasAspect(contentNode, ContentModel.ASPECT_LOCKABLE)) {
            persistenceManagerService.addAspect(contentNode, ContentModel.ASPECT_LOCKABLE, null);
        }
        Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(contentNode);
        nodeProperties.put(ContentModel.PROP_MODIFIER, user);
        nodeProperties.put(CStudioContentModel.PROP_LAST_MODIFIED_BY, user);
        persistenceManagerService.setProperties(contentNode, nodeProperties);

        DmVersionService dmVersionService = getServicesManager().getService(DmVersionService.class);
        dmVersionService.createNextMinorVersion(site, fullPath);

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
    }
}

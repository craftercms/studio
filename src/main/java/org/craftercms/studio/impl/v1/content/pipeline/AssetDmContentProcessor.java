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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.io.InputStream;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;

public class AssetDmContentProcessor extends FormDmContentProcessor {

    public static final String FILE_SIZE_MB = "MB";
    public static final String FILE_SIZE_KB = "KB";

    private static final Logger logger = LoggerFactory.getLogger(AssetDmContentProcessor.class);

    public static final String NAME = "WriteAssetToDmProcessor";

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
        boolean unlock = StringUtils.isEmpty(unlockValue) ||
                !unlockValue.equalsIgnoreCase("false");
        boolean isPreview = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_IS_PREVIEW));
        boolean isSystemAsset = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_SYSTEM_ASSET));
        boolean createFolders = ContentFormatUtils.getBooleanValue(content.getProperty(DmConstants.KEY_CREATE_FOLDERS));
        try {
            ContentAssetInfoTO oldAssetInfo = (ContentAssetInfoTO) result.getItem();
            ContentAssetInfoTO assetInfo = writeContentAsset(content, site, user, path, fileName,
                    content.getContentStream(), width, height, createFolders, isPreview, unlock, isSystemAsset,
                    result);
            if (oldAssetInfo != null) {
                oldAssetInfo.setFileExtension(assetInfo.getFileExtension());
                oldAssetInfo.setFileName(assetInfo.getFileName());
                oldAssetInfo.setSize(assetInfo.getSize());
                oldAssetInfo.setSizeUnit(assetInfo.getSizeUnit());
                result.setItem(oldAssetInfo);
            } else {
                result.setItem(assetInfo);
            }
        } catch (ServiceLayerException | UserNotFoundException e) {
            throw new ContentProcessException("Failed to write " + content.getId()+", "+e, e);
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
     * @throws ServiceLayerException
     */
    protected ContentAssetInfoTO writeContentAsset(PipelineContent content, String site, String user, String path,
                                                   String assetName, InputStream in, int width, int height,
                                                   boolean createFolders, boolean isPreview, boolean unlock,
                                                   boolean isSystemAsset, ResultTO result)
            throws ServiceLayerException, UserNotFoundException {
        logger.debug("Writing content asset: [site: " + site + ", path: " + path + ", assetName: "
                + assetName + ", createFolders: " + createFolders);

        String ext = null;
        int index = assetName.lastIndexOf(".");
        if (index > 0 && (index + 1) < assetName.length()) {
            ext = assetName.substring(index + 1).toUpperCase();
        }

        String contentPath = path + FILE_SEPARATOR + assetName;

        try {
            // look up the path content first
            ContentItemTO parentContentItem = contentService.getContentItem(site, path, 0);
            boolean parentExists = contentService.contentExists(site, path);
            if (!parentExists && createFolders) {
                parentContentItem = createMissingFoldersInPath(site, path, isPreview);
                parentExists = contentService.contentExists(site, path);
            }
            if (parentExists && parentContentItem.isFolder()) {
                boolean exists = contentService.contentExists(site, path + FILE_SEPARATOR + assetName);
                if (exists) {
                    updateFile(site, contentPath, in, user, isPreview, unlock, result);
                    content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, OPERATION_UPDATE);
                } else {
                    createNewFile(site, parentContentItem, assetName, in, user, unlock, result);
                    content.addProperty(DmConstants.KEY_ACTIVITY_TYPE, OPERATION_CREATE);
                }
                ContentAssetInfoTO assetInfo = new ContentAssetInfoTO();
                assetInfo.setFileName(assetName);
                long sizeInBytes = contentService.getContentSize(site, path + FILE_SEPARATOR + assetName);
                double convertedSize;
                if (sizeInBytes > 0) {
                    convertedSize = sizeInBytes / 1024d;
                    if (convertedSize >= 1024) {
                        assetInfo.setSizeUnit(FILE_SIZE_MB);
                        assetInfo.setSize(convertedSize / 1024d);
                    } else {
                        if (convertedSize > 0 && convertedSize < 1) {
                            assetInfo.setSize(1);
                        } else {
                            assetInfo.setSize(Math.round(convertedSize));
                        }

                        assetInfo.setSizeUnit(FILE_SIZE_KB);
                    }
                }
                // Item
                // TODO: get local code with API 2
                String commitId = result.getCommitId();
                if (StringUtils.isEmpty(commitId)) {
                    commitId = contentRepository.getRepoLastCommitId(site);
                }
                itemServiceInternal.persistItemAfterWrite(site, contentPath, user, commitId, unlock);
                assetInfo.setFileExtension(ext);
                return assetInfo;
            } else {
                throw new ServiceLayerException(path + " does not exist or not a directory.");
            }
        } finally {
            ContentUtils.release(in);
        }
    }

    /**
     * update the file at the given content node
     *
     * @param input
     * @param user
     * @param isPreview
     * @param unlock    unlock the content upon update?
     * @throws ServiceLayerException
     */
    protected void updateFile(String site, String relativePath, InputStream input,
                              String user, boolean isPreview, boolean unlock, ResultTO result)
            throws ServiceLayerException, UserNotFoundException {
        boolean success;
        try {
            success = contentService.writeContent(site, relativePath, input);
        } finally {
            ContentUtils.release(input);
        }

        if (success) {
            String commitId = contentRepository.getRepoLastCommitId(site);
            result.setCommitId(commitId);

            // if there is anything pending and this is not a preview update, cancel workflow
            if (!isPreview) {
                if (cancelWorkflow(site, relativePath)) {
                    workflowService.removeFromWorkflow(site, relativePath, true);
                }
            }

            // Item
            itemServiceInternal.persistItemAfterWrite(site, relativePath, user, commitId, unlock);
        }
        if (unlock) {
            contentRepositoryV1.unLockItem(site, relativePath);
            logger.debug("Unlocked the content site " + site + " path " + relativePath);
        } else {
            contentRepository.lockItem(site, relativePath);
        }
    }

}

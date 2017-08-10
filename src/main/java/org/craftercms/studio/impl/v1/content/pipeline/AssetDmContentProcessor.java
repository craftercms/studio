/*
 * Crafter Studio Web-content authoring solution
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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONTENT_PROCESSOR_ASSETS_SYSTEM_PATH;

public class AssetDmContentProcessor extends FormDmContentProcessor {

    public static final int READ_BUFFER_LENGTH = 32 * 1024;
    public static final String FILE_SIZE_MB = "MB";
    public static final String FILE_SIZE_KB = "KB";

    private static final Logger logger = LoggerFactory.getLogger(AssetDmContentProcessor.class);

    public static final String NAME = "WriteAssetToDmProcessor";

    public String getAssetsSystemPath() {
        return studioConfiguration.getProperty(CONTENT_PROCESSOR_ASSETS_SYSTEM_PATH);
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
                oldAssetInfo.setSize(assetInfo.getSize());
                oldAssetInfo.setSizeUnit(assetInfo.getSizeUnit());
                result.setItem(oldAssetInfo);
            } else {
                result.setItem(assetInfo);
            }
        } catch (ServiceException e) {
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
     * @throws ServiceException
     */
    protected ContentAssetInfoTO writeContentAsset(String site, String user, String path, String assetName, InputStream in,
                                                   int width, int height, boolean createFolders, boolean isPreview, boolean unlock, boolean isSystemAsset)
            throws ServiceException {
        logger.debug("Writing content asset: [site: " + site + ", path: " + path + ", assetName: " + assetName + ", createFolders: " + createFolders);

        String ext = null;
        int index = assetName.lastIndexOf(".");
        if (index > 0 && (index + 1) < assetName.length()) {
            ext = assetName.substring(index + 1).toUpperCase();
        }

        if (isSystemAsset) {
            assetName = ContentUtils.getMd5ForFile(in) + "." + ext;
        }
        String contentPath = path + "/" + assetName;

        try {
            // look up the path content first
            ContentItemTO parentContentItem = contentService.getContentItem(site, path, 0);
            boolean parentExists = contentService.contentExists(site, path);
            if (!parentExists && createFolders) {
                parentContentItem = createMissingFoldersInPath(site, path, isPreview);
                parentExists = contentService.contentExists(site, path);
            }
            if (parentExists && parentContentItem.isFolder()) {
                boolean exists = contentService.contentExists(site, path + "/" + assetName);
                ContentItemTO contentItem = null;
                if (exists) {
                    contentItem = contentService.getContentItem(site, path + "/" + assetName, 0);
                    updateFile(site, contentItem, contentPath, in, user, isPreview, unlock);
                } else {
                    // TODO: define content type
                    contentItem = createNewFile(site, parentContentItem, assetName, null, in, user, unlock);
                    objectStateService.insertNewEntry(site, contentItem);
                }
                ContentAssetInfoTO assetInfo = new ContentAssetInfoTO();
                assetInfo.setFileName(assetName);
                InputStream is = contentService.getContent(site, path + "/" + assetName);
                long sizeInBytes = 0;
                double convertedSize = 0;
                try {
                    sizeInBytes = IOUtils.toByteArray(is).length;
                } catch (Exception e) {
                    logger.error("Unable to determine size for content at site: " + site + " path " + contentPath);
                }
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
    protected void updateFile(String site, ContentItemTO contentItem, String relativePath, InputStream input, String user, boolean isPreview, boolean unlock)
            throws ServiceException {

        try {
            contentService.writeContent(site, relativePath, input);
        } finally {
            ContentUtils.release(input);
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put(ItemMetadata.PROP_MODIFIER, user);
        properties.put(ItemMetadata.PROP_MODIFIED, new Date());
        if (unlock) {
            properties.put(ItemMetadata.PROP_LOCK_OWNER, StringUtils.EMPTY);
        } else {
            properties.put(ItemMetadata.PROP_LOCK_OWNER, user);
        }
        if (!objectMetadataManager.metadataExist(site, relativePath)) {
            objectMetadataManager.insertNewObjectMetadata(site, relativePath);
        }
        objectMetadataManager.setObjectMetadata(site, relativePath, properties);

        if (unlock) {
            contentService.unLockContent(site, relativePath);
            logger.debug("Unlocked the content site " + site + " path " + relativePath);
        } else {
            contentService.lockContent(site, relativePath);
        }

        // if there is anything pending and this is not a preview update, cancel workflow
        if (!isPreview) {
            if (cancelWorkflow(site, relativePath)) {
                workflowService.removeFromWorkflow(site, relativePath, true);
            } else {
                if(updateWorkFlow(site,relativePath)) {
                    workflowService.updateWorkflowSandboxes(site,relativePath);
                }
            }
        }
    }

    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected StudioConfiguration studioConfiguration;

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}

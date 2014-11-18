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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotAllowedException;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ContentAssetInfoTO;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CheckImageSizeProcessor extends PathMatchProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckImageSizeProcessor.class);

    public static final String NAME = "CheckImageSizeProcessor";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * default constructor
     */
    public CheckImageSizeProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public CheckImageSizeProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String name = content.getProperty(DmConstants.KEY_FILE_NAME);
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        String mimetype = persistenceManagerService.guessMimetype(name);
        //String isImage = content.getProperty(WcmConstants.KEY_IS_IMAGE);
        boolean process = (StringUtils.isEmpty(mimetype)) ? false : mimetype.startsWith("image/") && !StringUtils.equalsIgnoreCase(mimetype, "image/svg+xml");
        if (process) {
            String allowLessSize = content.getProperty(DmConstants.KEY_ALLOW_LESS_SIZE);
            boolean lessSize = ContentFormatUtils.getBooleanValue(allowLessSize);
            String allowedWidth = content.getProperty(DmConstants.KEY_ALLOWED_WIDTH);
            String allowedHeight = content.getProperty(DmConstants.KEY_ALLOWED_HEIGHT);
            int width = (StringUtils.isEmpty(allowedWidth)) ? -1 : ContentFormatUtils.getIntValue(allowedWidth);
            int height = (StringUtils.isEmpty(allowedHeight)) ? -1 : ContentFormatUtils.getIntValue(allowedHeight);
            InputStream in = content.getContentStream();
            ContentAssetInfoTO assetInfo = (result.getItem() == null) ? new ContentAssetInfoTO() : (ContentAssetInfoTO) result.getItem();
            in = checkForImageSize(in, width, height, lessSize, assetInfo);
            content.getProperties().put(DmConstants.KEY_WIDTH, String.valueOf(assetInfo.getWidth()));
            content.getProperties().put(DmConstants.KEY_HEIGHT, String.valueOf(assetInfo.getHeight()));
            assetInfo.getWidth();
            result.setItem(assetInfo);
            content.setContentStream(in);
        }
    }

    /**
     * check the width and the height of the given image as an inputstream match the width and the height specified
     *
     * @param in
     * @param allowedWidth
     * @param allowedHeight
     * @param lessSize
     * @param assetInfo
     * @return
     * @throws org.craftercms.cstudio.alfresco.service.exception.ServiceException
     * @throws org.craftercms.cstudio.alfresco.service.exception.ContentNotAllowedException
     */
    protected InputStream checkForImageSize(InputStream in, int allowedWidth, int allowedHeight, boolean lessSize, ContentAssetInfoTO assetInfo) throws ContentNotAllowedException, ContentProcessException {
        ByteArrayOutputStream byteOutput = null;
        try {
            byteOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[CStudioWebScriptConstants.READ_BUFFER_LENGTH];
            int read = 0;
            while ((read = in.read(buffer)) > 0) {
                byteOutput.write(buffer, 0, read);
            }
            byte [] imageData = byteOutput.toByteArray();
            Image image = Toolkit.getDefaultToolkit().createImage(imageData);
            ImageIcon icon = new ImageIcon(image);
            int height = icon.getIconHeight();
            int width = icon.getIconWidth();
            validateImageSize(allowedWidth, allowedHeight, height, width, lessSize);
            assetInfo.setHeight(height);
            assetInfo.setWidth(width);
            return new ByteArrayInputStream(imageData);
        } catch (IOException e) {
            throw new ContentProcessException(e);
        } finally {
            // close the original inputstream
            ContentUtils.release(in);
            ContentUtils.release(byteOutput);
        }
    }

    /**
     * validate the image width and height against the allowed width and height
     *
     * @param allowedWidth
     * @param allowedHeight
     * @param height
     * @param width
     * @param lessSize
     * @throws ContentNotAllowedException
     */
    protected void validateImageSize(int allowedWidth, int allowedHeight, int height, int width, boolean lessSize) throws ContentNotAllowedException {
        if (height < 0 || width  < 0 ) {
            throw new ContentNotAllowedException("An image must be provided.");
        }
        boolean success = true;
        if (!lessSize) {
            if (allowedWidth > 0 && allowedWidth != width) {
                success = false;
            }
            if (allowedHeight > 0 && allowedHeight != height) {
                success = false;
            }
            if (!success) {
                throw new ContentNotAllowedException(
                        "The width and the height of the image must match to the specified width and height: "
                                + allowedWidth + "X" + allowedHeight
                                + ". The actual width and height: "+ width + "X" + height);
            }
        } else {
            if (allowedWidth > 0 && allowedWidth < width) {
                success = false;
            }
            if (allowedHeight > 0 && allowedHeight < height) {
                success = false;
            }
            if (!success) {
                throw new ContentNotAllowedException(
                        "The width and the height of the image must be less then or equal to the specified width and height: "
                                + allowedWidth + "X" + allowedHeight
                                + ". The actual width and height: "+ width + "X" + height);
            }
        }
    }
}

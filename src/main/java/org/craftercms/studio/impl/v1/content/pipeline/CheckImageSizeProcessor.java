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
import org.craftercms.studio.api.v1.exception.ContentNotAllowedException;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CheckImageSizeProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(CheckImageSizeProcessor.class);

    public static final String NAME = "CheckImageSizeProcessor";


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
        String mimetype = StudioUtils.getMimeType(name);
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
     * @return image as input stream
     */
    protected InputStream checkForImageSize(InputStream in, int allowedWidth, int allowedHeight, boolean lessSize, ContentAssetInfoTO assetInfo) throws ContentProcessException {
        ByteArrayOutputStream byteOutput = null;
        try {
            byteOutput = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];//PORT CStudioWebScriptConstants.READ_BUFFER_LENGTH];
            int read = 0;
            while ((read = in.read(buffer)) > 0) {
                byteOutput.write(buffer, 0, read);
            }
            byte [] imageData = byteOutput.toByteArray();
            Image image = Toolkit.getDefaultToolkit().createImage(imageData);
            ImageIcon icon = new ImageIcon(image);
            int height = icon.getIconHeight();
            int width = icon.getIconWidth();
            if (allowedHeight > 0 && allowedWidth > 0) {
                validateImageSize(allowedWidth, allowedHeight, height, width, lessSize);
            }
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

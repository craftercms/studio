/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.util.spring.mvc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.craftercms.engine.controller.rest.RestScriptsController;
import org.springframework.web.servlet.view.AbstractView;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;


public class BinaryView extends AbstractView {

    public static final String DEFAULT_CONTENT_STREAM_MODEL_ATTR_NAME = "contentStream";
    public static final String DEFAULT_CONTENT_PATH_MODEL_ATTR_NAME = "contentPath";

    public static final String DEFAULT_CONTENT_TYPE = "image/png";
    public static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    public static final String PRAGMA_HEADER_NAME = "Pragma";
    public static final String CACHE_CONTROL_HEADER_NAME = "Cache-Control";
    public static final String EXPIRES_HEADER_NAME = "Expires";

    public static final String DISABLED_CACHING_PRAGMA_HEADER_VALUE = "no-cache";
    public static final String DISABLED_CACHING_CACHE_CONTROL_HEADER_VALUE = "no-cache, no-store, max-age=0";
    public static final long DISABLED_CACHING_EXPIRES_HEADER_VALUE = 1L;


    private boolean disableCaching;

    public BinaryView() {
        setContentType(DEFAULT_CONTENT_TYPE);
    }



    /**
     * Tells the client to disable caching of the generated JSON. Default is false.
     */
    public void setDisableCaching(boolean disableCaching) {
        this.disableCaching = disableCaching;
    }


    @Override
    protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType(getContentType());
        response.setCharacterEncoding(DEFAULT_CHARACTER_ENCODING);
        if (disableCaching) {
            response.addHeader(PRAGMA_HEADER_NAME, DISABLED_CACHING_PRAGMA_HEADER_VALUE);
            response.addHeader(CACHE_CONTROL_HEADER_NAME, DISABLED_CACHING_CACHE_CONTROL_HEADER_VALUE);
            response.addDateHeader(EXPIRES_HEADER_NAME, DISABLED_CACHING_EXPIRES_HEADER_VALUE);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {

        OutputStream out = response.getOutputStream();
        Map<String, Object> responseModelMap = (Map<String, Object>)model.get(RestScriptsController.DEFAULT_RESPONSE_BODY_MODEL_ATTR_NAME);
        if (responseModelMap != null) {
            InputStream contentStream = (InputStream) responseModelMap.get(DEFAULT_CONTENT_STREAM_MODEL_ATTR_NAME);
            String contentPath = (String) responseModelMap.get(DEFAULT_CONTENT_PATH_MODEL_ATTR_NAME);

            MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
            String contentType = mimetypesFileTypeMap.getContentType(contentPath);
            response.setContentType(contentType);
            if (contentStream != null) {
                IOUtils.write(IOUtils.toByteArray(contentStream), out);
            }
            out.flush();
            IOUtils.closeQuietly(contentStream);
            IOUtils.closeQuietly(out);
        }
    }

}

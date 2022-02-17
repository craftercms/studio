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

package org.craftercms.studio.impl.v2.utils.spring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.core.io.AbstractResource;

/**
 * Implementation of {@link org.springframework.core.io.Resource} that wraps site content
 *
 * @author joseross
 * @since 3.1.1
 */
public class ContentResource extends AbstractResource {

    /**
     * The content service
     */
    protected ContentService contentService;

    /**
     * The site id
     */
    protected String site;

    /**
     * The relative path of the content
     */
    protected String path;

    public ContentResource(final ContentService contentService, final String site, final String path) {
        this.contentService = contentService;
        this.site = site;
        this.path = path;
    }

    @Override
    public boolean exists() {
        return contentService.contentExists(site, path);
    }

    @Override
    public long contentLength() {
        return contentService.getContentSize(site, path);
    }

    @Override
    public long lastModified() {
        //TODO: Fix when there is a way to get the real date for any file in the repo
        return System.currentTimeMillis();
    }

    @Override
    public String getFilename() {
        return FilenameUtils.getName(path);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return contentService.getContent(site, path);
        } catch (ContentNotFoundException e) {
            throw new FileNotFoundException("No content found for '" + path + "' in site: " + site);
        }
    }

}

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

package org.craftercms.studio.impl.v1.util.spring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.springframework.core.io.AbstractResource;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Implementation of {@link org.springframework.core.io.Resource} that wraps a site content item
 *
 * @author joseross
 * @since 3.1.1
 */
public class ContentItemResource extends AbstractResource {

    /**
     * The content service
     */
    protected ContentService contentService;

    /**
     * The site of the item
     */
    protected String site;

    /**
     * The relative path of the item
     */
    protected String path;

    /**
     * The content item
     */
    protected ContentItemTO item;

    public ContentItemResource(final ContentService contentService, final String site, final String path) {
        this.contentService = contentService;
        this.site = site;
        this.path = path;
        this.item = contentService.getContentItem(site, path);
    }

    @Override
    public boolean exists() {
        return contentService.contentExists(site, path);
    }

    @Override
    public boolean isOpen() {
        return isEmpty(item.getLockOwner());
    }

    @Override
    public long contentLength() {
        return contentService.getContentSize(site, path);
    }

    @Override
    public long lastModified() {
        //TODO: How to get last modified date?
        // item.getLastEditDate() is always coming as null...
        return System.currentTimeMillis();
    }

    @Override
    public String getFilename() {
        return item.getName();
    }

    @Override
    public String getDescription() {
        return item.getMetaDescription();
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

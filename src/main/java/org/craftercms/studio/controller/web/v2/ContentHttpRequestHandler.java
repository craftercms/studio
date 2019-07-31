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

package org.craftercms.studio.controller.web.v2;

import javax.servlet.http.HttpServletRequest;

import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.impl.v1.util.spring.ContentItemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link org.springframework.web.HttpRequestHandler} that serves site content as static resources
 *
 * @author joseross
 * @since 3.1.1
 */
public class ContentHttpRequestHandler extends ResourceHttpRequestHandler {

    /**
     * The content service
     */
    protected ContentService contentService;

    public ContentHttpRequestHandler(final ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Resource getResource(final HttpServletRequest request) {
        String site = request.getParameter("siteId");
        String path = request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        if (isNotEmpty(path)) {
            Resource resource = new ContentItemResource(contentService, site, path);
            if (resource.exists()) {
                return resource;
            }
        }
        return null;
    }

}

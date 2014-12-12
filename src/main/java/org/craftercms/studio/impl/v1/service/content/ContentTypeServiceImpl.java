/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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

package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Dejan Brkic
 */
public class ContentTypeServiceImpl implements ContentTypeService {
    @Override
    public ContentTypeConfigTO getContentTypeForContent(String site, String path) throws ServiceException {
        ContentItemTO itemTO = contentService.getContentItem(site, path);
        if (itemTO != null) {
            String type = itemTO.getContentType();
            if (!StringUtils.isEmpty(type)) {
                return servicesConfig.getContentTypeConfig(site, type);
                } else {
                    throw new ServiceException("No content type specified for " + path + " in site: " + site);
                }
            } else {
                throw new ContentNotFoundException(path + " is not found in site: " + site);
            }
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    protected ContentService contentService;
    protected ServicesConfig servicesConfig;
}

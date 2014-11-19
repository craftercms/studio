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
package org.craftercms.cstudio.alfresco.dm.util.impl;

import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioContentModel;
import org.craftercms.cstudio.alfresco.service.api.NamespaceService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.util.SearchUtils;
import org.craftercms.cstudio.alfresco.util.impl.SolrSearchQueryBuilder;

import java.util.List;

public class DmSolrSearchQueryBuilder extends SolrSearchQueryBuilder {
    /**
     * add content type query by its path and type
     *
     * @param buffer
     * @param site
     * @param contentTypes
     */
    protected void addContentTypeQuery(StringBuffer buffer, String site, List<String> contentTypes) {
        if (contentTypes != null && contentTypes.size() > 0) {
            buffer.append("(");
            boolean added = false;
            NamespaceService namespaceService = getServicesManager().getService(NamespaceService.class);
            for (String contentType : contentTypes) {
                if (!StringUtils.isEmpty(contentType)) {
                    if (added) {
                        buffer.append(" OR ");
                    }
                    // add type & path query for each content type
                    // TODO: disabled path query due to an issue in indexing sandbox
                    //String path = _servicesConfig.getWcmContentPath(site, contentType);
                    //buffer.append(SearchUtils.createPathQuery(path, false) + " AND ");

                    /**
                     * TODO: integrating document section with content-type.  current approach, not good.  checking, if starts with a slash
                     * but, good for the time being.
                     */
                    if (contentType.startsWith("/")) { // indicates content-type.
                        buffer.append(SearchUtils.createPropertyQuery(CStudioContentModel.PROP_CONTENT_TYPE,
                                String.class.getName(), contentType, null, null, false, SearchService.LANGUAGE_LUCENE, namespaceService));
                    } 
                    added = true;
                }
            }
            buffer.append(")");
        }
    }
}

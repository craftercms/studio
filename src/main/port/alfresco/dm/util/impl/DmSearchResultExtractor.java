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
package org.craftercms.cstudio.alfresco.dm.util.impl;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmSearchResultItemTO;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.to.SearchColumnTO;
import org.craftercms.cstudio.alfresco.to.SearchConfigTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.api.SearchResultExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DmSearchResultExtractor implements SearchResultExtractor {
    
    protected static final Logger logger = LoggerFactory.getLogger(DmSearchResultExtractor.class);
    
    protected int _defaultNumPerPage = 10;
    public int getDefaultNumPerPage() {
        return _defaultNumPerPage;
    }
    public void setDefaultNumPerPage(int defaultNumPerPage) {
        this._defaultNumPerPage = defaultNumPerPage;
    }

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public List extract(String site, List<NodeRef> nodeRefs, List<SearchColumnTO> columns, String sort, boolean ascending, int page, int pageSize) throws ServiceException {
        if (nodeRefs == null) {
            return new FastList<DmSearchResultItemTO>(0);
        } else {
            List<DmSearchResultItemTO> items = new FastList<DmSearchResultItemTO>(nodeRefs.size());
            page = (page <= 0) ? 1 : page;
            pageSize = (pageSize <= 0) ? _defaultNumPerPage : pageSize;
            int startIndex = (page - 1) * pageSize;
            int endIndex = page * pageSize;
            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
            for (int index = startIndex; index < nodeRefs.size() && index < endIndex; index++) {
                NodeRef nodeRef = nodeRefs.get(index);
                if (nodeRef != null) {
                    try {
                        DmSearchResultItemTO item = createResultItem(site, nodeRef);
                        items.add(item);
                    } catch (ServiceException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Failed to get DM content item: "  + persistenceManagerService.getNodePath(nodeRef), e);
                        }
                    }
                } else {
                    logger.error("Failed to get DM content item from "  + nodeRef);
                }
            }
            return items;
        }
    }

    /**
     * create a search result item given the content node
     *
     * @param site
     * @param nodeRef
     * @return search result item
     * @throws ServiceException
     */
    protected DmSearchResultItemTO createResultItem(String site, NodeRef nodeRef) throws ServiceException {
        DmSearchResultItemTO resultItem = new DmSearchResultItemTO();
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        DmContentItemTO item = persistenceManagerService.getContentItem(persistenceManagerService.getNodePath(nodeRef));
        resultItem.setItem(item);
        String contentType = item.getContentType();
        SearchConfigTO searchConfig = null;
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        if (!StringUtils.isEmpty(contentType)) {
            ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
            if (config != null) {
                searchConfig = config.getSearchConfig();
            }
        } else {
            searchConfig = servicesConfig.getDefaultSearchConfig(site);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_MODEL);
        dateFormat.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
        NodeService nodeService = getServicesManager().getService(NodeService.class);
        if (searchConfig != null && searchConfig.getExtractableMetadata() != null) {
            Map<QName, String> metadata = searchConfig.getExtractableMetadata();
            if (metadata != null) {
                Map<String, Serializable> properties = new FastMap<String, Serializable>();
                for (QName name : metadata.keySet()) {
                    String label = metadata.get(name);
                    Serializable propValue = nodeService.getProperty(nodeRef, name);
                    if (propValue instanceof Date) {
                        if (propValue != null) {
                            String convertedDate = ContentFormatUtils.formatDate(dateFormat, (Date) propValue);
                            properties.put(label, ContentFormatUtils.convertToFormDate(convertedDate));
                        } else {
                            properties.put(label, "");
                        }
                    } else {
                        properties.put(label, String.valueOf(propValue));
                    }
                }
                resultItem.setProperties(properties);
            }
        }
        return resultItem;
    }
}

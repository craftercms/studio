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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.util.api.ContentPropertyLoader;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SearchService;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * load properties from content xml by xpath
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class XPathPropertiesLoader implements ContentPropertyLoader {

    private static final Logger logger = LoggerFactory.getLogger(XPathPropertiesLoader.class);

    public static final String NAME = "XPathPropertiesLoader";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * path properties to load
     */
    protected Map<String, String> _pathProperties;
    public Map<String, String> getPathProperties() {
        return _pathProperties;
    }
    public void setPathProperties(Map<String, String> pathProperties) {
        this._pathProperties = pathProperties;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEligible(NodeRef node, DmContentItemTO item) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        return persistenceManagerService.getFileInfo(node).getName().endsWith(DmConstants.XML_PATTERN);
    }

    @Override
    public void loadProperties(NodeRef node, DmContentItemTO item) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        if (_pathProperties != null && _pathProperties.size() > 0) {
            InputStream is = null;
            try {
                is = persistenceManagerService.getReader(node).getContentInputStream();
                SAXReader saxReader = new SAXReader();
                Document content = saxReader.read(is);
                Element root = content.getRootElement();
                for (String key : _pathProperties.keySet()) {
                    String xpath = _pathProperties.get(key);
                    String value = root.valueOf(xpath);
                    item.getProperties().put(key, value);
                }
            } catch (DocumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Failed to load content properties from "
                            + getNodePath(node) + ". The content is not a valid xml content.");
                }
            } finally {
                ContentUtils.release(is);
            }
        }
    }

    protected String getNodePath(NodeRef node) {
        StringBuilder nodePath = new StringBuilder();
        try {
            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
            SearchService searchService = getServicesManager().getService(SearchService.class);
            List<FileInfo> paths = persistenceManagerService.getNamePath(searchService.findNode(node.getStoreRef(), DmConstants.DM_WEM_PROJECT_ROOT), node);
            for (FileInfo path : paths) {
                nodePath.append("/").append(path.getName());
            }
        } catch (FileNotFoundException e) {
            logger.error("Error while getting node path for node: " + node.getId(), e);
        }
        return nodePath.toString();
    }
}

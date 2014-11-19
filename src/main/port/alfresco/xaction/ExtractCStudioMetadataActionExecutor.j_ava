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
package org.craftercms.cstudio.alfresco.action;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmMetadataService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExtractCStudioMetadataActionExecutor extends ActionExecuterAbstractBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractCStudioMetadataActionExecutor.class);

    public static final String NAME = "extract-cstudio-metadata";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        extractMetadata(actionedUponNodeRef);
    }

    protected void extractMetadata(NodeRef nodeRef) {
        PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
        FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
        if(fileInfo.isFolder()){
            List<FileInfo> childNodes = persistenceManagerService.list(nodeRef);
            for (FileInfo childInfo : childNodes) {
                extractMetadata(childInfo.getNodeRef());
            }

        } else {
            String fullPath = persistenceManagerService.getNodePath(nodeRef);
            DmPathTO dmPathTO = new DmPathTO(fullPath);
            String site = dmPathTO.getSiteName();
            String relativePath = dmPathTO.getRelativePath();
            DmContentService dmContentService = getServicesManager().getService(DmContentService.class);
            DmMetadataService dmMetadataService = getServicesManager().getService(DmMetadataService.class);
            if (fullPath.endsWith(DmConstants.XML_PATTERN)) {
                try {
                    Document doc = dmContentService.getContentXml(site, null, relativePath);
                    dmMetadataService.extractMetadata(site, persistenceManagerService.getCurrentUserName(), null, relativePath, null, nodeRef, doc);
                } catch (ContentNotFoundException e) {
                    LOGGER.error("Failed to extract metadata for document: " + fullPath, e);
                } catch (ServiceException e) {
                    LOGGER.error("Failed to extract metadata for document: " + fullPath ,e);
                }
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

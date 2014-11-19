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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.util.List;


import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.craftercms.cstudio.alfresco.dm.constant.DmXmlConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmPageNavigationOrderService;
import org.craftercms.cstudio.alfresco.pagenavigationordersequence.PageNavigationOrderSequenceDaoService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.GeneralLockService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.to.PageNavigationOrderSequenceTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmPageNavigationOrderServiceImpl extends AbstractRegistrableService implements DmPageNavigationOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DmPageNavigationOrderServiceImpl.class);

    protected PageNavigationOrderSequenceDaoService _pageNavigationOrderSequenceDaoService;
    public PageNavigationOrderSequenceDaoService getPageNavigationOrderSequenceDaoService() {
        return _pageNavigationOrderSequenceDaoService;
    }
    public void setPageNavigationOrderSequenceDaoService(PageNavigationOrderSequenceDaoService pageNavigationOrderSequenceDaoService) {
        this._pageNavigationOrderSequenceDaoService = pageNavigationOrderSequenceDaoService;
    }

    @Override
    public void register() {
        this._servicesManager.registerService(DmPageNavigationOrderService.class, this);
    }

    @Override
    public float getNewNavOrder(String site, String path) {
        ServicesConfig servicesConfig = getServicesManager().getService(ServicesConfig.class);
        String siteRepo = servicesConfig.getRepositoryRootPath(site);
        String fullPath = siteRepo + path;
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        
        NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
        while (nodeRef == null) {
            fullPath = fullPath.substring(0, fullPath.lastIndexOf("/"));
            nodeRef = persistenceManagerService.getNodeRef(fullPath);
        }
        GeneralLockService sequenceLockService = getService(GeneralLockService.class);
        sequenceLockService.lock(nodeRef.getId());
        float lastNavOrder = 1000F;
        try {
            PageNavigationOrderSequenceTO sequenceTO = _pageNavigationOrderSequenceDaoService.getSequence(nodeRef.getId());
            if (sequenceTO == null) {
                
                List<FileInfo> children = getService(PersistenceManagerService.class).list(nodeRef);
                sequenceTO = new PageNavigationOrderSequenceTO(nodeRef.getId(), site, path, 1000F * children.size());
                _pageNavigationOrderSequenceDaoService.setSequence(sequenceTO);
            } else {
                sequenceTO = _pageNavigationOrderSequenceDaoService.increaseSequence(nodeRef.getId());
            }
            lastNavOrder = sequenceTO.getMaxCount();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        } finally {
            sequenceLockService.unlock(nodeRef.getId());
        }
        return lastNavOrder;

    }

    @Override
    public boolean addNavOrder(String site, String path, Document document) {
        boolean docUpdated =false;
        Element root = document.getRootElement();
        //Node navOrderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_ORDER_VALUE);
        Node navOrderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_ORDER_DEFAULT);

        //skip if order value element does not exist
        if(navOrderNode !=null){
            String newOrder = String.valueOf(getNewNavOrder(site, path));
             ((Element) navOrderNode).setText(newOrder);
            docUpdated=true;
        }
        return docUpdated;
    }

    @Override
    public boolean updateNavOrder(String site, String path, Document document) {
        boolean docUpdated =false;
        Element root = document.getRootElement();
        //Node navOrderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_ORDER_VALUE);
        Node navOrderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_ORDER_DEFAULT);

        //skip if order value element does not exist
        if(navOrderNode !=null){
            String value = ((Element) navOrderNode).getText();

            //skip if order value already exist
            if(StringUtils.isEmpty(value)){
                String newOrder = String.valueOf(getNewNavOrder(site, path));
                ((Element) navOrderNode).setText(newOrder);
                docUpdated=true;
            }else{
                if(logger.isDebugEnabled())
                    logger.debug("Nav Order value already exist: " +value);
            }
        }
        return docUpdated;
    }
}

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
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.PageNavigationOrder;
import org.craftercms.studio.api.v1.dal.PageNavigationOrderMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;


public class DmPageNavigationOrderServiceImpl extends AbstractRegistrableService implements DmPageNavigationOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DmPageNavigationOrderServiceImpl.class);

    @Override
    public void register() {
        this._servicesManager.registerService(DmPageNavigationOrderService.class, this);
    }

    @Override
    public float getNewNavOrder(String site, String path) {

        String lockId = site + ":" + path;
        generalLockService.lock(lockId);
        float lastNavOrder = 1000F;
        try {
            PageNavigationOrder pageNavigationOrder = pageNavigationOrderMapper.getPageNavigationOrderForSiteAndPath(site, path);
            if (pageNavigationOrder == null) {
                ContentItemTO itemTreeTO = contentService.getContentItemTree(site, path, 1);
                pageNavigationOrder = new PageNavigationOrder();
                pageNavigationOrder.setFolderId(itemTreeTO.getNodeRef());
                pageNavigationOrder.setSite(site);
                pageNavigationOrder.setPath(path);
                pageNavigationOrder.setMaxCount(1000F * itemTreeTO.getNumOfChildren());
                pageNavigationOrderMapper.insert(pageNavigationOrder);
            } else {
                float newMaxCount = pageNavigationOrder.getMaxCount() + pageNavigationOrderIncrement;
                pageNavigationOrder.setMaxCount(newMaxCount);
                pageNavigationOrderMapper.update(pageNavigationOrder);
            }
            lastNavOrder = pageNavigationOrder.getMaxCount();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        } finally {
            generalLockService.unlock(lockId);
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
                logger.debug("Nav Order value already exist: " +value);
            }
        }
        return docUpdated;
    }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public int getPageNavigationOrderIncrement() { return pageNavigationOrderIncrement; }
    public void setPageNavigationOrderIncrement(int pageNavigationOrderIncrement) { this.pageNavigationOrderIncrement = pageNavigationOrderIncrement; }

    protected GeneralLockService generalLockService;
    protected ContentService contentService;
    protected int pageNavigationOrderIncrement = 1000;

    @Autowired
    protected PageNavigationOrderMapper pageNavigationOrderMapper;
}

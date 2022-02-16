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
package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateDoubleParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequence;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequenceMapper;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PAGE_NAVIGATION_ORDER_INCREMENT;

public class DmPageNavigationOrderServiceImpl extends AbstractRegistrableService
        implements DmPageNavigationOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DmPageNavigationOrderServiceImpl.class);

    protected GeneralLockService generalLockService;
    protected ContentService contentService;
    protected StudioConfiguration studioConfiguration;
    protected NavigationOrderSequenceMapper navigationOrderSequenceMapper;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public void register() {
        this._servicesManager.registerService(DmPageNavigationOrderService.class, this);
    }

    @Override
    @ValidateParams
    public double getNewNavOrder(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path) {
        return getNewNavOrder(site, path, -1);
    }

    @Override
    @ValidateParams
    public double getNewNavOrder(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path,
                                 @ValidateDoubleParam(name = "currentMaxNavOrder") double currentMaxNavOrder) {

        String lockId = site + ":" + path;
        double lastNavOrder = 1000D;
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("path", path);
            NavigationOrderSequence navigationOrderSequence =
                    navigationOrderSequenceMapper.getPageNavigationOrderForSiteAndPath(params);
            if (navigationOrderSequence == null) {
                navigationOrderSequence = new NavigationOrderSequence();
                navigationOrderSequence.setSite(site);
                navigationOrderSequence.setPath(path);
                ContentItemTO itemTreeTO = contentService.getContentItemTree(site, path, 1);
                if (itemTreeTO == null) {
                    navigationOrderSequence.setMaxCount(0F);
                } else {
                    if (StringUtils.isEmpty(itemTreeTO.getNodeRef())) {
                        navigationOrderSequence.setFolderId(UUID.randomUUID().toString());
                    } else {
                        navigationOrderSequence.setFolderId(itemTreeTO.getNodeRef());
                    }
                    if (currentMaxNavOrder < 0) {
                        navigationOrderSequence.setMaxCount(1000F * itemTreeTO.getNumOfChildren());
                    } else {
                        double newMaxCount = currentMaxNavOrder + getPageNavigationOrderIncrement();
                        navigationOrderSequence.setMaxCount(newMaxCount);
                    }

                }
                retryingDatabaseOperationFacade.insertNavigationOrderSequence(navigationOrderSequence);
            } else {
                double newMaxCount = navigationOrderSequence.getMaxCount() + getPageNavigationOrderIncrement();
                navigationOrderSequence.setMaxCount(newMaxCount);
                retryingDatabaseOperationFacade.updateNavigationOrderSequence(navigationOrderSequence);
            }
            lastNavOrder = navigationOrderSequence.getMaxCount();
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
        }
        return lastNavOrder;

    }

    @Override
    @ValidateParams
    public boolean addNavOrder(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path, Document document) {
        boolean docUpdated =false;
        Element root = document.getRootElement();
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
    @ValidateParams
    public boolean updateNavOrder(@ValidateStringParam(name = "site") String site,
                                  @ValidateSecurePathParam(name = "path") String path, Document document) {
        boolean docUpdated =false;
        Element root = document.getRootElement();
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

    @Override
    @ValidateParams
    public void deleteSequencesForSite(@ValidateStringParam(name = "site") String site) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        retryingDatabaseOperationFacade.deleteNavigationOrderSequencesForSite(params);
    }

    @Override
    public int getPageNavigationOrderIncrement() {
        int toReturn = Integer.parseInt(studioConfiguration.getProperty(PAGE_NAVIGATION_ORDER_INCREMENT));
        return toReturn;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public NavigationOrderSequenceMapper getNavigationOrderSequenceMapper() {
        return navigationOrderSequenceMapper;
    }

    public void setNavigationOrderSequenceMapper(NavigationOrderSequenceMapper navigationOrderSequenceMapper) {
        this.navigationOrderSequenceMapper = navigationOrderSequenceMapper;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}

/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequence;
import org.craftercms.studio.api.v1.dal.NavigationOrderSequenceMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
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
    @Valid
    public double getNewNavOrder(@ValidateStringParam String site,
                                 @ValidateSecurePathParam String path) {
        return getNewNavOrder(site, path, -1);
    }

    @Override
    @Valid
    public double getNewNavOrder(@ValidateStringParam String site,
                                 @ValidateSecurePathParam String path,
                                 double currentMaxNavOrder) {
        double lastNavOrder = 1000D;
        try {
            Map<String, String> params = new HashMap<>();
            params.put("site", site);
            params.put("path", path);
            NavigationOrderSequence navigationOrderSequence =
                    navigationOrderSequenceMapper.getPageNavigationOrderForSiteAndPath(params);
            if (navigationOrderSequence == null) {
                navigationOrderSequence = getNewNavigationOrderSequence(site, path, currentMaxNavOrder);
                NavigationOrderSequence finalNavOrderSequence = navigationOrderSequence;
                retryingDatabaseOperationFacade.retry(() -> navigationOrderSequenceMapper.insert(finalNavOrderSequence));
            } else {
                double newMaxCount = navigationOrderSequence.getMaxCount() + getPageNavigationOrderIncrement();
                navigationOrderSequence.setMaxCount(newMaxCount);
                NavigationOrderSequence finalNavOrderSequence = navigationOrderSequence;
                retryingDatabaseOperationFacade.retry(() -> navigationOrderSequenceMapper.update(finalNavOrderSequence));
            }
            lastNavOrder = navigationOrderSequence.getMaxCount();
        } catch (Exception e) {
            logger.error("Failed to get the new NavOrder for site '{}' path '{}'", site, path, e);
        }
        return lastNavOrder;

    }

    private NavigationOrderSequence getNewNavigationOrderSequence(final String site, final String path, final double currentMaxNavOrder) {
        NavigationOrderSequence navigationOrderSequence;
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
        return navigationOrderSequence;
    }

    @Override
    @Valid
    public boolean addNavOrder(@ValidateStringParam String site,
                               @ValidateSecurePathParam String path, Document document) {
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
    @Valid
    public boolean updateNavOrder(@ValidateStringParam String site,
                                  @ValidateSecurePathParam String path, Document document) {
        boolean docUpdated =false;
        Element root = document.getRootElement();
        Node navOrderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_ORDER_DEFAULT);

        // Skip if order value element does not exist
        if (navOrderNode != null) {
            String value = ((Element) navOrderNode).getText();

            // Skip if order value already exist
            if (StringUtils.isEmpty(value)) {
                String newOrder = String.valueOf(getNewNavOrder(site, path));
                ((Element) navOrderNode).setText(newOrder);
                docUpdated = true;
            } else {
                logger.debug("NavOrder value already exist for site '{}' path '{}' value '{}'",
                        site, path, value);
            }
        }
        return docUpdated;
    }

    @Override
    @Valid
    public void deleteSequencesForSite(@ValidateStringParam String site) {
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        retryingDatabaseOperationFacade.retry(() -> navigationOrderSequenceMapper.deleteSequencesForSite(params));
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

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

package org.craftercms.studio.impl.v2.utils;

import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_BULK_OPERATIONS_BATCH_SIZE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.OBJECT_STATE_BULK_OPERATIONS_BATCH_SIZE;

public class StudioUtils {

    private ServicesConfig servicesConfig;
    private ContentService contentService;
    private StudioConfiguration studioConfiguration;

    public List<String> getEnvironmentNames(String siteId) {
        List<String> toRet = new ArrayList<String>();
        toRet.add(servicesConfig.getLiveEnvironment(siteId));
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            toRet.add(servicesConfig.getStagingEnvironment(siteId));
        }
        return toRet;
    }

    public ContentItemTO getContentItemForDashboard(String site, String path) {
        ContentItemTO item = null;
        if (!contentService.contentExists(site, path)) {
            item = contentService.createDummyDmContentItemForDeletedNode(site, path);
            item.setLockOwner("");
        } else {
            item = contentService.getContentItem(site, path, 0);
        }
        return item;
    }

    public int getBulkOperationsBatchSize() {
        return Integer.parseInt(studioConfiguration.getProperty(DB_BULK_OPERATIONS_BATCH_SIZE));
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
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
}

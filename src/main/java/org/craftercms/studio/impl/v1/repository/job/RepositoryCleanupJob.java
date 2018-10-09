/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.impl.v1.repository.job;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Triggers a cleanup for all repositories on all existing sites.
 * @author joseross
 */
public class RepositoryCleanupJob {

    protected static final Logger logger = LoggerFactory.getLogger(RepositoryCleanupJob.class);

    protected SiteService siteService;
    protected ContentRepository contentRepository;

    /**
     * Performs a cleanup for all repositories on all existing sites.
     */
    public void cleanupAllRepositories() {
        logger.info("Starting cleanup for all sites");
        siteService.getAllAvailableSites().forEach(contentRepository::cleanupRepositories);
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
    
}

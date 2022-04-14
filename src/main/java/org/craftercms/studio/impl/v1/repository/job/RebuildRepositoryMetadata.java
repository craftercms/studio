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

package org.craftercms.studio.impl.v1.repository.job;

import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.job.CronJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.core.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RebuildRepositoryMetadata {

    private final static Logger logger = LoggerFactory.getLogger(RebuildRepositoryMetadata.class);

    private static ReentrantLock taskLock = new ReentrantLock();

    protected PublishRequestMapper publishRequestMapper;
    protected DependencyService dependencyService;
    protected ContentService contentService;
    protected SecurityService securityService;
    protected TaskExecutor taskExecutor;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected ContentRepository contentRepository;
    protected ItemServiceInternal itemServiceInternal;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    public void execute(String site) {
        if (taskLock.tryLock()) {
            try {
                logger.debug("Starting the Rebuild Repository Metadata background task for site '{}'", site);
                CronJobContext securityContext = new CronJobContext(securityService.getCurrentUser());
                RebuildRepositoryMetadataTask task = new RebuildRepositoryMetadataTask(securityContext, site);
                taskExecutor.execute(task);
            } finally {
                taskLock.unlock();
            }
        }
    }

    class RebuildRepositoryMetadataTask implements Runnable {

        private String site;
        private CronJobContext securityContext;

        public RebuildRepositoryMetadataTask(CronJobContext securityContext, String site) {
            this.securityContext = securityContext;
            this.site = site;
        }

        @Override
        public void run() {
            logger.debug("Rebuild repository metadata for site '{}' started", site);
            CronJobContext.setCurrent(securityContext);
            logger.debug("Cleaning up existing repository metadata for site '{}'", site);
            try {
                cleanOldMetadata(site);
            } catch (SiteNotFoundException e) {
                logger.error("Error cleaning up old metadata for site '{}'", site, e);
            }
            logger.debug("Started the rebuild metadata process for site '{}'", site);
            try {
                rebuildMetadata(site);
            } catch (ServiceLayerException | UserNotFoundException e) {
                logger.error("Failed to rebuild the metadata for site '{}'", site, e);
            }
            CronJobContext.clear();
            logger.debug("Rebuild repository metadata for site '{}' ended", site);
        }
    }

    public boolean cleanOldMetadata(String site) throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(site);
        logger.debug("Clean the repository metadata in site '{}'", site);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);

        try {
            // Delete all dependencies
            logger.debug("Delete the dependencies in site '{}'", site);
            dependencyService.deleteSiteDependencies(site);
        } catch (Exception e) {
            logger.error("Failed to delete the dependencies in site '{}'", site, e);
        }

        try {
            // Delete deployment queue
            logger.debug("Delete the deployment queue in site '{}'", site);
            retryingDatabaseOperationFacade.deleteDeploymentDataForSite(params);
        } catch (Exception e) {
            logger.error("Failed to delete the deployment queue in site '{}'", site, e);
        }

        try {
            // Delete item table
            logger.debug("Delete the item table for site '{}'", site);
            itemServiceInternal.deleteItemsForSite(siteFeed.getId());
        } catch (Exception e) {
            logger.error("Failed to delete the item data for site '{}'", site, e);
        }

        try {
            logger.debug("Delete the git log data for site '{}'", site);
            contentRepository.deleteGitLogForSite(site);
        } catch (Exception e) {
            logger.error("Failed to delete the git log data for site '{}'", site, e);
        }

        return true;
    }


    protected boolean rebuildMetadata(String site) throws ServiceLayerException, UserNotFoundException {
        siteService.syncDatabaseWithRepo(site, null);
        return true;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public PublishRequestMapper getPublishRequestMapper() {
        return publishRequestMapper;
    }

    public void setPublishRequestMapper(PublishRequestMapper publishRequestMapper) {
        this.publishRequestMapper = publishRequestMapper;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}

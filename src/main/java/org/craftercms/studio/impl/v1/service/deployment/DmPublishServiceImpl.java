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
package org.craftercms.studio.impl.v1.service.deployment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.impl.v2.utils.DateUtils;

import static java.lang.String.format;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class DmPublishServiceImpl extends AbstractRegistrableService implements DmPublishService {

    private static final Logger logger = LoggerFactory.getLogger(DmPublishServiceImpl.class);

    protected DeploymentService deploymentService;
    protected SecurityService securityService;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
    protected DependencyService dependencyService;
    protected ItemServiceInternal itemServiceInternal;

    @Override
    public void register() {
        this._servicesManager.registerService(DmPublishService.class, this);
    }

    @Override
    @ValidateParams
    public void publish(@ValidateStringParam(name = "site") final String site, List<String> paths,
                        ZonedDateTime launchDate, final MultiChannelPublishingContext mcpContext) {
        boolean scheduledDateIsNow = false;
        if (launchDate == null) {
            scheduledDateIsNow=true;
            launchDate = DateUtils.getCurrentTime();
        }
        final String approver = securityService.getCurrentUser();
        final ZonedDateTime ld = launchDate;

        try {
            deploymentService.deploy(site, mcpContext.getPublishingChannelGroup(), paths, ld, approver,
                        mcpContext.getSubmissionComment(),scheduledDateIsNow );
        } catch (DeploymentException | ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to submit items for publishing in site '{}'", site, e);
        }
    }

    @Override
    @ValidateParams
    public void unpublish(@ValidateStringParam (name = "site") String site, List<String> paths, String approver) {
        unpublish(site, paths, approver, null);
    }

    @Override
    @ValidateParams
    public void unpublish(@ValidateStringParam(name = "site") String site, List<String> paths,
                          @ValidateStringParam(name = "approver") String approver, ZonedDateTime scheduleDate) {
        if (scheduleDate == null) {
            scheduleDate = DateUtils.getCurrentTime();
        }
        try {
            deploymentService.delete(site, paths, approver, scheduleDate, null);
        } catch (DeploymentException | ServiceLayerException | UserNotFoundException e) {
            logger.error("Failed to delete files during publishing site '{}'", site, e);
        }
    }

    @Override
    @ValidateParams
    public void cancelScheduledItem(@ValidateStringParam(name = "site") String site,
                                    @ValidateSecurePathParam(name = "path") String path) {
        try {
            deploymentService.cancelWorkflow(site, path);
        } catch (DeploymentException e) {
            logger.error("Failed to cancel workflow for site '{}' path '{}'", site, path, e);
        }
    }

    @Override
    @ValidateParams
    public void bulkGoLive(@ValidateStringParam(name = "site") String site,
                           @ValidateStringParam String environment,
                           @ValidateSecurePathParam(name = "path") String path,
                           String comment) throws ServiceLayerException {
        logger.info("Start Bulk Publish in site '{}' path '{}' to target '{}'", site, path, environment);

        String queryPath = path;
        if (queryPath.startsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
            queryPath = queryPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        }

        logger.debug("Get a change-set from site '{}' root path '{}'", site, queryPath);
        List<String> childrenPaths = new ArrayList<String>();

        childrenPaths = itemServiceInternal.getChangeSetForSubtree(site, queryPath);

        logger.debug("Collected '{}' items from site '{}' root path '{}'", childrenPaths.size(), site, queryPath);
        Set<String> processedPaths = new HashSet<String>();
        ZonedDateTime launchDate = DateUtils.getCurrentTime();
        for (String childPath : childrenPaths) {
            String childHash = DigestUtils.md2Hex(childPath);
            logger.debug("Process bulk publish dependencies in site '{}' path '{}'", site, childPath);
            if (processedPaths.add(childHash)) {
                List<String> pathsToPublish = new ArrayList<String>();
                List<String> candidatesToPublish = new ArrayList<String>();
                pathsToPublish.add(childPath);
                candidatesToPublish.addAll(itemServiceInternal.getSameCommitItems(site, childPath));
                candidatesToPublish.addAll(dependencyService.getPublishingDependencies(site, childPath));
                for (String pathToAdd : candidatesToPublish) {
                    String hash = DigestUtils.md2Hex(pathToAdd);
                    if (processedPaths.add(hash)) {
                        pathsToPublish.add(pathToAdd);
                    }
                }
                String aprover = securityService.getCurrentUser();
                if (StringUtils.isEmpty(comment)) {
                    comment = format("Bulk Publish invoked by '%s'", aprover);
                }
                logger.info("Publish a package of '{}' items in site '{}' path '{}' to target '{}'",
                        pathsToPublish.size(), site, childPath, environment);
                try {
                    deploymentService.deploy(site, environment, pathsToPublish, launchDate, aprover, comment, true);
                } catch (DeploymentException | UserNotFoundException e) {
                    logger.error("Failed to bulk publish site '{}' path '{}'", site, childPath, e);
                } finally {
                    logger.debug("Finished bulk publish processing of package in site '{}' path '{}'",
                            site, childPath);
                }
            }
        }
        logger.info("Finished Bulk Publish site '{}' path '{}' to target '{}'", site, path, environment);
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}

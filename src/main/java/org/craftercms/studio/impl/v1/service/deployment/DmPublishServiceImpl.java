/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.service.deployment;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmPathTO;
import org.craftercms.studio.api.v1.to.PublishingChannelConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;

public class DmPublishServiceImpl extends AbstractRegistrableService implements DmPublishService {

    private static final Logger logger = LoggerFactory.getLogger(DmPublishServiceImpl.class);

    @Override
    public void register() {
        this._servicesManager.registerService(DmPublishService.class, this);
    }

    @Override
    @ValidateParams
    public void publish(@ValidateStringParam(name = "site") final String site, List<String> paths, Date launchDate,
                        final MultiChannelPublishingContext mcpContext) {
        final List<String> pathsToPublish = new ArrayList<>();
        for (String p : paths) {
            DmPathTO dmPathTO = new DmPathTO(p);
            pathsToPublish.add(dmPathTO.getRelativePath());
        }
        boolean scheduledDateIsNow = false;
        if (launchDate == null) {
            scheduledDateIsNow=true;
            launchDate = new Date();
        }
        final String approver = securityService.getCurrentUser();
        final Date ld = launchDate;


        try {
            deploymentService.deploy(site, mcpContext.getPublishingChannelGroup(), pathsToPublish, ld, approver,
                        mcpContext.getSubmissionComment(),scheduledDateIsNow );
        } catch (DeploymentException e) {
            logger.error("Error while submitting paths to publish");
        }

    }

    @Override
    @ValidateParams
    public void unpublish(@ValidateStringParam(name = "site") String site, List<String> paths, @ValidateStringParam(name = "approver") String approver) {
        unpublish(site, paths, approver, null);
    }

    @Override
    @ValidateParams
    public void unpublish(@ValidateStringParam(name = "site") String site, List<String> paths, @ValidateStringParam(name = "approver") String approver, Date scheduleDate) {
        if (scheduleDate == null) {
            scheduleDate = new Date();
        }
        try {
            deploymentService.delete(site, paths, approver, scheduleDate);
        } catch (DeploymentException ex) {
            logger.error("Unable to delete files due a error ",ex);
        }
    }

    @Override
    @ValidateParams
    public void cancelScheduledItem(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        try {
            deploymentService.cancelWorkflow(site, path);
            /* TODO: remove property
            String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
            NodeRef nodeRef = persistenceManagerService.getNodeRef(fullPath);
            persistenceManagerService.removeProperty(nodeRef, WCMWorkflowModel.PROP_LAUNCH_DATE);
            */
        } catch (DeploymentException e) {
            logger.error(String.format("Error while canceling workflow for content at %s, site %s", path, site), e);
        }
    }
/*
    @Override
    public List<PublishingChannelTO> getAvailablePublishingChannelGroups(String site, String path) {
        List<PublishingChannelTO> channelTOs = new FastList<PublishingChannelTO>();
        List<String> channels = getPublishingChannels(site);
        for (String ch : channels) {
            PublishingChannelTO chTO = new PublishingChannelTO();
            chTO.setName(ch);
            chTO.setPublish(true);
            chTO.setUpdateStatus(false);
            channelTOs.add(chTO);
        }
        return channelTOs;
    }

    protected List<String> getPublishingChannels(String site) {
        SiteService siteService = getService(SiteService.class);
        List<String> channels = new FastList<String>();
        Map<String, PublishingChannelGroupConfigTO> channelGroupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        for (PublishingChannelGroupConfigTO configTO : channelGroupConfigTOs.values()) {
            channels.add(configTO.getName());
        }
        return channels;
    }
    
    /**
     * Checks if there are any publishing channels configure
     * @return true if there is at least one publishing channel config
     */
    @Override
    @ValidateParams
	public boolean hasChannelsConfigure(@ValidateStringParam(name = "site") String site, MultiChannelPublishingContext mcpContext) {
    	boolean toReturn = false;
        if (mcpContext != null) {
            Map<String, PublishingChannelGroupConfigTO> publishingChannelGroupConfigs = siteService.getPublishingChannelGroupConfigs(site);
            PublishingChannelGroupConfigTO configTO = publishingChannelGroupConfigs.get(mcpContext.getPublishingChannelGroup());
            if (configTO != null) {
                for (PublishingChannelConfigTO channelConfigTO : configTO.getChannels()) {
                    if (channelConfigTO != null) {
                        if (siteService.getDeploymentEndpoint(site, channelConfigTO.getName()) != null) {
                            toReturn = true;
                        }
                    }
                }
            }
        }
    	return toReturn;
    }

    @Override
    @ValidateParams
    public void bulkGoLive(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, @ValidateSecurePathParam(name = "path") String path) throws ServiceException {
        logger.info("Starting Bulk Go Live for path " + path + " site " + site);

        List<String> childrenPaths = new ArrayList<String>();
        ContentItemTO item = contentService.getContentItem(site, path, 2);
        logger.debug("Traversing subtree for site " + site + " and root path " + path);
        if (item != null) {
            if (!item.isFolder()) {
                childrenPaths.add(item.getUri());
            }
            if (item.getUri().endsWith("/" + DmConstants.INDEX_FILE) && objectMetadataManager.isRenamed(site, item.getUri())) {
                getAllMandatoryChildren(site, item, childrenPaths);
            } else {
                if (item.isFolder() || item.isContainer()) {
                    getAllMandatoryChildren(site, item, childrenPaths);
                }
            }
        }
        logger.debug("Collected " + childrenPaths.size() + " content items for site " + site + " and root path " + path);
        Set<String> processedPaths = new HashSet<String>();
        Date launchDate = new Date();
        for (String childPath : childrenPaths) {
            String childHash = DigestUtils.md2Hex(childPath);
            logger.debug("Processing dependencies for site " + site + " path " + childPath);
            if (processedPaths.add(childHash)) {
                List<String> pathsToPublish = new ArrayList<String>();
                List<String> candidatePathsToPublish = new ArrayList<String>();
                pathsToPublish.add(childPath);
                candidatePathsToPublish.add(childPath);
                getAllDependenciesRecursive(site, childPath, candidatePathsToPublish);
                for (String pathToAdd : candidatePathsToPublish) {
                    String hash = DigestUtils.md2Hex(pathToAdd);
                    if (processedPaths.add(hash)) {
                        pathsToPublish.add(pathToAdd);
                    }
                }
                String aprover = securityService.getCurrentUser();
                String comment = "Bulk Go Live invoked by " + aprover;
                logger.debug("Deploying package of " + pathsToPublish.size() + " items for site " + site + " path " +
                             childPath);
                try {
                    deploymentService.deploy(site, environment, pathsToPublish, launchDate, aprover, comment, true);
                } catch (DeploymentException e) {
                    logger.error("Error while running bulk Go Live operation", e);
                } finally {
                    logger.debug("Finished processing deployment package for path " + childPath + " site " + site);
                }
            }
        }
        logger.info("Finished Bulk Go Live for path " + path + " site " + site);
    }

    protected void getAllDependenciesRecursive(String site, String path, List<String> dependencyPaths) throws ServiceException {
        Set<String> depPaths = dependencyService.getPublishingDepenencies(site, path);
        dependencyPaths.addAll(depPaths);
    }

    protected void getAllMandatoryChildren(String site, ContentItemTO item, List<String> pathsToPublish) {
        if (item != null) {
            for (ContentItemTO child : item.getChildren()) {
                child = contentService.getContentItem(site, child.getUri(), 2);
                if (!child.isFolder()) {
                    pathsToPublish.add(child.getUri());
                }
                if (child.getChildren() != null && child.getChildren().size() > 0) {
                    getAllMandatoryChildren(site, child, pathsToPublish);
                }
            }
        }
    }

    @Override
    @ValidateParams
    public void bulkDelete(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        logger.debug("Starting Bulk Delete for path " + path + " site " + site);
        List<String> childrenPaths = new ArrayList<String>();
        childrenPaths.add(path);
        ContentItemTO item = contentService.getContentItem(site, path, 2);
        if (item != null) {
            if (!item.isFolder()) {
                childrenPaths.add(path);
            }
            if (path.endsWith("/" + DmConstants.INDEX_FILE) && objectMetadataManager.isRenamed(site, path)) {
                getAllMandatoryChildren(site, item, childrenPaths);
            } else {
                if (item.isFolder() || item.isContainer()) {
                    getAllMandatoryChildren(site, item, childrenPaths);
                }
            }
        }
        Date launchDate = new Date();
        String approver = securityService.getCurrentUser();
        logger.debug("Deleting " + childrenPaths.size() + " items");

        try {
            deploymentService.delete(site, childrenPaths, approver, launchDate);
        } catch (DeploymentException e) {
            logger.error("Error while running bulk Delete operation", e);
        } finally {
            logger.debug("Finished Bulk Delete for path " + path + " site " + site);
        }
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    protected DeploymentService deploymentService;
    protected SecurityService securityService;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
    protected ObjectMetadataManager objectMetadataManager;
    protected DependencyService dependencyService;
    protected ObjectStateService objectStateService;
}

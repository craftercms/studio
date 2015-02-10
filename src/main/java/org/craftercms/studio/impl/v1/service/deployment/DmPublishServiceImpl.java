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
package org.craftercms.studio.impl.v1.service.deployment;


import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.to.DmPathTO;
import org.craftercms.studio.api.v1.to.PublishingChannelConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DmPublishServiceImpl extends AbstractRegistrableService implements DmPublishService {

    private static final Logger logger = LoggerFactory.getLogger(DmPublishServiceImpl.class);

    //protected DmFilterWrapper dmFilterWrapper;


    //public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
//		this.dmFilterWrapper = dmFilterWrapper;
//	}


    @Override
    public void register() {
        this._servicesManager.registerService(DmPublishService.class, this);
    }

    @Override
    public void publish(final String site, List<String> paths, Date launchDate,
                        final MultiChannelPublishingContext mcpContext) {
        final List<String> pathsToPublish = new ArrayList<>();
        for (String p : paths) {
            DmPathTO dmPathTO = new DmPathTO(p);
            pathsToPublish.add(dmPathTO.getRelativePath());
        }
        if (launchDate == null) {
            launchDate = new Date();
        }
        final String approver = securityService.getCurrentUser();
        final Date ld = launchDate;


        try {
            deploymentService.deploy(site, mcpContext.getPublishingChannelGroup(), pathsToPublish, ld, approver,
                        mcpContext.getSubmissionComment());
        } catch (DeploymentException e) {
            logger.error("Error while submitting paths to publish");
        }

    }

    @Override
    public void unpublish(String site, List<String> paths, String approver) {
        unpublish(site, paths, approver, null);
    }

    @Override
    public void unpublish(String site, List<String> paths,  String approver, Date scheduleDate) {
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
    public void cancelScheduledItem(String site, String path) {
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
	public boolean hasChannelsConfigure(String site, MultiChannelPublishingContext mcpContext) {
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
/*
    @Override
    public void bulkGoLive(String site, String environment, String path) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Bulk Go Live for path " + path + " site " + site);
        }
        List<String> childrenPaths = new ArrayList<String>();
        childrenPaths.add(path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(servicesConfig.getRepositoryRootPath(site), path);
        if (nodeRef != null) {
            FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
            if (!fileInfo.isFolder()) {
                childrenPaths.add(path);
            }
            if (path.endsWith("/" + DmConstants.INDEX_FILE) && persistenceManagerService.hasAspect(nodeRef, CStudioContentModel.ASPECT_RENAMED)) {
                getAllMandatoryChildren(site, path, childrenPaths);
            } else {
                if (fileInfo.isFolder()) {
                    getAllMandatoryChildren(site, path, childrenPaths);
                }
            }
        }
        List<String> pathsToPublish = new ArrayList<String>();
        for (String childPath : childrenPaths) {
            pathsToPublish.add(childPath);
            getAllDependenciesRecursive(site, path, pathsToPublish);
        }
        Date launchDate = new Date();
        String approver = AuthenticationUtil.getFullyAuthenticatedUser();
        String comment = "Bulk Go Live invoked by " + approver;
        if (logger.isDebugEnabled()) {
            logger.debug("Deploying " + pathsToPublish.size() + " items");
        }
        try {
            deploymentService.deploy(site, environment, pathsToPublish, launchDate, approver, comment);
        } catch (DeploymentException e) {
            logger.error("Error while running bulk Go Live operation", e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Finished Bulk Go Live for path " + path + " site " + site);
            }
        }
    }

    protected void getAllDependenciesRecursive(String site, String path, List<String> dependencyPaths) {
        DmDependencyService dmDependencyService = _servicesManager.getService(DmDependencyService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String rootPath = servicesConfig.getRepositoryRootPath(site);
        List<String> depPaths = dmDependencyService.getDependencyPaths(site, path);
        for (String depPath : depPaths) {
            if (!dependencyPaths.contains(depPath)) {
                NodeRef nodeRef = persistenceManagerService.getNodeRef(rootPath, depPath);
                if (nodeRef != null) {
                    if (persistenceManagerService.isUpdatedOrNew(nodeRef)) {
                        dependencyPaths.add(depPath);
                        getAllDependenciesRecursive(site, depPath, dependencyPaths);
                    }
                }
            }
        }
    }

    protected void getAllMandatoryChildren(String site, String path, List<String> pathsToPublish) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String parentPath = path.replace("/" + DmConstants.INDEX_FILE, "");
        NodeRef nodeRef = persistenceManagerService.getNodeRef(servicesConfig.getRepositoryRootPath(site), parentPath);
        if (nodeRef != null) {
            List<FileInfo> children = persistenceManagerService.list(nodeRef);
            for (FileInfo childInfo : children) {
                NodeRef childNode = childInfo.getNodeRef();
                String fullChildPath = persistenceManagerService.getNodePath(childNode);
                DmPathTO dmPathTO = new DmPathTO(fullChildPath);
                if (childInfo.isFolder()) {
                    getAllMandatoryChildren(site, dmPathTO.getRelativePath(), pathsToPublish);
                } else {
                    if (!childInfo.getName().equalsIgnoreCase(DmConstants.INDEX_FILE) || !pathsToPublish.contains(dmPathTO.getRelativePath())) {
                        pathsToPublish.add(dmPathTO.getRelativePath());
                    }
                }
            }
        }
    }

    @Override
    public void bulkDelete(String site, String path) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting Bulk Delete for path " + path + " site " + site);
        }
        List<String> childrenPaths = new ArrayList<String>();
        childrenPaths.add(path);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        NodeRef nodeRef = persistenceManagerService.getNodeRef(servicesConfig.getRepositoryRootPath(site), path);
        if (nodeRef != null) {
            FileInfo fileInfo = persistenceManagerService.getFileInfo(nodeRef);
            if (!fileInfo.isFolder()) {
                childrenPaths.add(path);
            }
            if (path.endsWith("/" + DmConstants.INDEX_FILE) && persistenceManagerService.hasAspect(nodeRef, CStudioContentModel.ASPECT_RENAMED)) {
                getAllMandatoryChildren(site, path, childrenPaths);
            } else {
                if (fileInfo.isFolder()) {
                    getAllMandatoryChildren(site, path, childrenPaths);
                }
            }
        }
        Date launchDate = new Date();
        String approver = AuthenticationUtil.getFullyAuthenticatedUser();
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting " + childrenPaths.size() + " items");
        }
        try {
            deploymentService.delete(site, childrenPaths, approver, launchDate);
        } catch (DeploymentException e) {
            logger.error("Error while running bulk Delete operation", e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("Finished Bulk Delete for path " + path + " site " + site);
            }
        }
    }
    */


    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    protected DeploymentService deploymentService;
    protected SecurityService securityService;
    protected SiteService siteService;
}

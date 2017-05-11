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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.CopyToEnvironmentMapper;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.PUBLISHING_MANAGER_IMPORT_MODE_ENABLED;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PUBLISHING_MANAGER_INDEX_FILE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED;

public class PublishingManagerImpl implements PublishingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingManagerImpl.class);

    private final static String LIVE_ENVIRONMENT = "live";
    private final static String PRODUCTION_ENVIRONMENT = "Production";

    @Override
    public List<CopyToEnvironment> getItemsReadyForDeployment(String site, String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getItemsReadyForDeployment(params);
    }

    @Override
    public DeploymentItem processItem(CopyToEnvironment item) throws DeploymentException {

        if (item == null) {
            throw new DeploymentException("Cannot process item, item is null.");
        }

        DeploymentItem deploymentItem = new DeploymentItem();
        deploymentItem.setSite(item.getSite());
        deploymentItem.setPath(item.getPath());
        ObjectMetadata itemMetadata = objectMetadataManager.getProperties(item.getSite(), item.getPath());
        if (itemMetadata != null) {
            deploymentItem.setCommitId(itemMetadata.getCommitId());
        } else {
            deploymentItem.setCommitId(contentRepository.getRepoLastCommitId(item.getSite()));
        }

        String site = item.getSite();
        String path = item.getPath();
        String oldPath = item.getOldPath();
        String environment = item.getEnvironment();
        String action = item.getAction();

        String liveEnvironment = LIVE_ENVIRONMENT;
        boolean isLive = false;

        if (StringUtils.isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(environment)) {
                isLive = true;
            }
        }
        else if (LIVE_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment()) || PRODUCTION_ENVIRONMENT.equalsIgnoreCase(environment)) {
            isLive = true;
        }

        if (StringUtils.equals(action, CopyToEnvironment.Action.DELETE)) {
            if (oldPath != null && oldPath.length() > 0) {
                objectMetadataManager.clearRenamed(site, path);
            }
        } else {
            LOGGER.debug("Setting system processing for {0}:{1}", site, path);
            objectStateService.setSystemProcessing(site, path, true);

            if (isLive) {
                if (!isImportModeEnabled()) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    //contentRepository.createVersion(site, path, submissionComment, true);
                }
                else {
                    LOGGER.debug("Import mode is ON. Create new version is skipped for [{0}] site \"{1}\"", path, site);
                }
            }

            if (StringUtils.equals(action, CopyToEnvironment.Action.MOVE)) {
                if (oldPath != null && oldPath.length() > 0) {
                    if (isLive) {
                        objectMetadataManager.clearRenamed(site, path);
                    }
                }
            }

            ObjectMetadata objectMetadata = objectMetadataManager.getProperties(site, path);

            if (objectMetadata == null) {
                LOGGER.debug("No object state found for {0}:{1}, create it", site, path);
                objectMetadataManager.insertNewObjectMetadata(site, path);
                objectMetadata = objectMetadataManager.getProperties(site, path);
            }

            if (isLive) {
                // should consider what should be done if this does not work. Currently the method will bail and the item is stuck in processing.
                LOGGER.debug("Environment is live, transition item to LIVE state {0}:{1}", site, path);
                ContentItemTO contentItem = contentService.getContentItem(site, path);
                objectStateService.transition(site, contentItem, TransitionEvent.DEPLOYMENT);
                if (objectMetadata != null) {
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put(ObjectMetadata.PROP_SUBMITTED_BY, StringUtils.EMPTY);
                    props.put(ObjectMetadata.PROP_SEND_EMAIL, 0);
                    props.put(ObjectMetadata.PROP_SUBMITTED_FOR_DELETION, 0);
                    props.put(ObjectMetadata.PROP_SUBMISSION_COMMENT, StringUtils.EMPTY);
                    objectMetadataManager.setObjectMetadata(site, path, props);
                }
            }

            LOGGER.debug("Resetting system processing for {0}:{1}", site, path);
            objectStateService.setSystemProcessing(site, path, false);
        }
        return deploymentItem;
    }

    private void deleteFolder(String site, String path, String user, Deployer deployer) {
        if (contentService.contentExists(site, path)) {
            // TODO: SJ: This bypasses the Content Service, fix
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                contentService.deleteContent(site, path, false, user);
                deployer.deleteFile(site, path);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user, deployer);
            }
        }
    }

    @Override
    public void markItemsCompleted(String site, String environment, List<CopyToEnvironment> processedItems) throws DeploymentException {
        for (CopyToEnvironment item : processedItems) {
            item.setState(CopyToEnvironment.State.COMPLETED);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public void markItemsProcessing(String site, String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        for (CopyToEnvironment item : itemsToDeploy) {
            item.setState(CopyToEnvironment.State.PROCESSING);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public void markItemsReady(String site, String environment, List<CopyToEnvironment> copyToEnvironmentItems) throws DeploymentException {
        for (CopyToEnvironment item : copyToEnvironmentItems) {
            item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public List<DeploymentItem> processMandatoryDependencies(CopyToEnvironment item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException {
        List<DeploymentItem> mandatoryDependencies = new ArrayList<DeploymentItem>();
        String site = item.getSite();
        String path = item.getPath();

        if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.NEW) || StringUtils.equals(item.getAction(), CopyToEnvironment.Action.MOVE)) {
            if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
                String helpPath = path.replace("/" + getIndexFile(), "");
                int idx = helpPath.lastIndexOf("/");
                String parentPath = helpPath.substring(0, idx) + "/" + getIndexFile();
                if (objectStateService.isNew(site, parentPath) /* TODO: check renamed || objectStateService.isRenamed(site, parentPath) */) {
                    if (!missingDependenciesPaths.contains(parentPath) && !pathsToDeploy.contains(parentPath)) {
                        deploymentService.cancelWorkflow(site, parentPath);
                        missingDependenciesPaths.add(parentPath);
                        CopyToEnvironment parentItem = createMissingItem(site, parentPath, item);
                        DeploymentItem parentDeploymentItem = processItem(parentItem);
                        mandatoryDependencies.add(parentDeploymentItem);
                        mandatoryDependencies.addAll(processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }

            if (!isEnablePublishingWithoutDependencies()) {
                List<String> dependentPaths = dmDependencyService.getDependencyPaths(site, path);
                for (String dependentPath : dependentPaths) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    if (objectStateService.isNew(site, dependentPath) /* TODO: check renamed || contentRepository.isRenamed(site, dependentPath) */) {
                        if (!missingDependenciesPaths.contains(dependentPath) && !pathsToDeploy.contains(dependentPath)) {
                            deploymentService.cancelWorkflow(site, dependentPath);
                            missingDependenciesPaths.add(dependentPath);
                            CopyToEnvironment dependentItem = createMissingItem(site, dependentPath, item);
                            DeploymentItem dependentDeploymentItem = processItem(dependentItem);
                            mandatoryDependencies.add(dependentDeploymentItem);
                            mandatoryDependencies.addAll(processMandatoryDependencies(dependentItem, pathsToDeploy, missingDependenciesPaths));
                        }
                    }
                }
            }
        }

        return mandatoryDependencies;
    }

    public List<CopyToEnvironment> processMandatoryDependencies_old(CopyToEnvironment item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException {
        List<CopyToEnvironment> mandatoryDependencies = new ArrayList<CopyToEnvironment>();
        String site = item.getSite();
        String path = item.getPath();

        if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.NEW) || StringUtils.equals(item.getAction(), CopyToEnvironment.Action.MOVE)) {
            if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
                String helpPath = path.replace("/" + getIndexFile(), "");
                int idx = helpPath.lastIndexOf("/");
                String parentPath = helpPath.substring(0, idx) + "/" + getIndexFile();
                if (objectStateService.isNew(site, parentPath) /* TODO: check renamed || objectStateService.isRenamed(site, parentPath) */) {
                    if (!missingDependenciesPaths.contains(parentPath) && !pathsToDeploy.contains(parentPath)) {
                        deploymentService.cancelWorkflow(site, parentPath);
                        missingDependenciesPaths.add(parentPath);
                        CopyToEnvironment parentItem = createMissingItem(site, parentPath, item);
                        processItem(parentItem);
                        mandatoryDependencies.add(parentItem);
                        mandatoryDependencies.addAll(processMandatoryDependencies_old(parentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }

            if (!isEnablePublishingWithoutDependencies()) {
                List<String> dependentPaths = dmDependencyService.getDependencyPaths(site, path);
                for (String dependentPath : dependentPaths) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    if (objectStateService.isNew(site, dependentPath) /* TODO: check renamed || contentRepository.isRenamed(site, dependentPath) */) {
                        if (!missingDependenciesPaths.contains(dependentPath) && !pathsToDeploy.contains(dependentPath)) {
                            deploymentService.cancelWorkflow(site, dependentPath);
                            missingDependenciesPaths.add(dependentPath);
                            CopyToEnvironment dependentItem = createMissingItem(site, dependentPath, item);
                            processItem(dependentItem);
                            mandatoryDependencies.add(dependentItem);
                            mandatoryDependencies.addAll(processMandatoryDependencies_old(dependentItem, pathsToDeploy, missingDependenciesPaths));
                        }
                    }
                }
            }
        }

        return mandatoryDependencies;
    }

    private CopyToEnvironment createMissingItem(String site, String itemPath, CopyToEnvironment item) {
        CopyToEnvironment missingItem = new CopyToEnvironment();
        missingItem.setSite(site);
        missingItem.setEnvironment(item.getEnvironment());
        missingItem.setPath(itemPath);
        missingItem.setScheduledDate(item.getScheduledDate());
        missingItem.setState(item.getState());
        if (objectStateService.isNew(site, itemPath)) {
            missingItem.setAction(CopyToEnvironment.Action.NEW);
        }
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, itemPath);
        if ((metadata != null) && (metadata.getRenamed() != 0)) {
            String oldPath = metadata.getOldUrl();
            missingItem.setOldPath(oldPath);
            missingItem.setAction(CopyToEnvironment.Action.MOVE);
        }
        String contentTypeClass = contentService.getContentTypeClass(site, itemPath);
        missingItem.setContentTypeClass(contentTypeClass);
        missingItem.setUser(item.getUser());
        missingItem.setSubmissionComment(item.getSubmissionComment());
        return missingItem;
    }

    public String getIndexFile() {
        return studioConfiguration.getProperty(PUBLISHING_MANAGER_INDEX_FILE);
    }

    public boolean isImportModeEnabled() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(PUBLISHING_MANAGER_IMPORT_MODE_ENABLED));
        return toReturn;
    }

    public boolean isEnablePublishingWithoutDependencies() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED));
        return toReturn;
    }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public DeploymentService getDeploymentService() { return deploymentService; }
    public void setDeploymentService(DeploymentService deploymentService) { this.deploymentService = deploymentService; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ContentService contentService;
    protected DmDependencyService dmDependencyService;
    protected DeploymentService deploymentService;
    protected ContentRepository contentRepository;
    protected ObjectMetadataManager objectMetadataManager;
    protected ServicesConfig servicesConfig;
    protected SecurityProvider securityProvider;
    protected StudioConfiguration studioConfiguration;

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;
}

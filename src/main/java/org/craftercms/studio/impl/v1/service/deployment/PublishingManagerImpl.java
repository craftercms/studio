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
package org.craftercms.studio.impl.v1.service.deployment;


import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentHistoryProvider;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PUBLISHING_MANAGER_INDEX_FILE;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.
        PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED;

public class PublishingManagerImpl implements PublishingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingManagerImpl.class);

    private static final String LIVE_ENVIRONMENT = "live";
    private static final String PRODUCTION_ENVIRONMENT = "Production";

    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ContentService contentService;
    protected DeploymentService deploymentService;
    protected ContentRepository contentRepository;
    protected ObjectMetadataManager objectMetadataManager;
    protected ServicesConfig servicesConfig;
    protected SecurityProvider securityProvider;
    protected StudioConfiguration studioConfiguration;
    protected DependencyService dependencyService;
    protected DeploymentHistoryProvider deploymentHistoryProvider;

    @Autowired
    protected PublishRequestMapper publishRequestMapper;

    @Override
    @ValidateParams
    public List<PublishRequest> getItemsReadyForDeployment(@ValidateStringParam(name = "site") String site,
                                           @ValidateStringParam(name = "environment") String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", PublishRequest.State.READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        return publishRequestMapper.getItemsReadyForDeployment(params);
    }

    @Override
    public DeploymentItemTO processItem(PublishRequest item) throws DeploymentException {

        if (item == null) {
            throw new DeploymentException("Cannot process item, item is null.");
        }

        DeploymentItemTO deploymentItem = new DeploymentItemTO();
        deploymentItem.setSite(item.getSite());
        deploymentItem.setPath(item.getPath());
        deploymentItem.setCommitId(item.getCommitId());
        deploymentItem.setPackageId(item.getPackageId());

        String site = item.getSite();
        String path = item.getPath();
        String oldPath = item.getOldPath();
        String environment = item.getEnvironment();
        String action = item.getAction();
        String user = item.getUser();

        String liveEnvironment = LIVE_ENVIRONMENT;

        boolean siteEnvironmentConfigEnabled = Boolean.parseBoolean(
                studioConfiguration.getProperty(StudioConfiguration.CONFIGURATION_SITE_ENVIRONMENT_CONFIG_ENABLED));
        if (!siteEnvironmentConfigEnabled) {
            liveEnvironment = servicesConfig.getLiveEnvironment(site);
        }

        boolean isLive = false;

        if (StringUtils.isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(environment)) {
                isLive = true;
            }
        }
        else if (StringUtils.equalsIgnoreCase(LIVE_ENVIRONMENT, item.getEnvironment()) ||
                StringUtils.equalsIgnoreCase(PRODUCTION_ENVIRONMENT, environment)) {
            isLive = true;
        }

        if (StringUtils.equals(action, PublishRequest.Action.DELETE)) {
            if (oldPath != null && oldPath.length() > 0) {
                contentService.deleteContent(site, oldPath, user);
                boolean hasRenamedChildren = false;

                if (oldPath.endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                    if (contentService.contentExists(site,
                            oldPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""))) {
                        // TODO: SJ: This bypasses the Content Service, fix
                        RepositoryItem[] children = contentRepository.getContentChildren(
                                site, oldPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""));

                        if (children.length > 1) {
                            hasRenamedChildren = true;
                        }
                    }
                    if (!hasRenamedChildren) {
                        deleteFolder(site,
                                oldPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""), user);
                    }
                }
                deploymentItem.setMove(true);
                deploymentItem.setOldPath(oldPath);
                objectMetadataManager.clearRenamed(site, path);
            }


            boolean haschildren = false;

            if (item.getPath().endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                if (contentService.contentExists(site,
                        path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""))) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    RepositoryItem[] children = contentRepository.getContentChildren(site,
                            path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""));

                    if (children.length > 1) {
                        haschildren = true;
                    }
                }
            }

            if (contentService.contentExists(site, path)) {
                contentService.deleteContent(site, path, user);

                if (!haschildren) {
                    deleteFolder(site, path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""), user);
                }
            }
            deploymentItem.setDelete(true);
        } else {
            if (StringUtils.equals(action, PublishRequest.Action.MOVE)) {
                deploymentItem.setMove(true);
                deploymentItem.setOldPath(oldPath);
                if (oldPath != null && oldPath.length() > 0) {
                    if (isLive) {
                        objectMetadataManager.clearRenamed(site, path);
                    }
                }
            }

            ItemMetadata itemMetadata = objectMetadataManager.getProperties(site, path);

            if (itemMetadata == null) {
                throw new DeploymentException("No metadata found for " + site + ":" + path);
            }

            ContentItemTO contentItem = contentService.getContentItem(site, path);
            if (isLive) {
                // should consider what should be done if this does not work.
                // Currently the method will bail and the item is stuck in processing.
                LOGGER.debug("Environment is live, transition item to LIVE state {0}:{1}", site, path);

                // check if commit id from workflow and from object state match
                if (itemMetadata.getCommitId().equals(item.getCommitId())) {
                    objectStateService.transition(site, contentItem, TransitionEvent.DEPLOYMENT);
                }
            } else {
                    objectStateService.transition(site, contentItem, TransitionEvent.SAVE);
            }
            Map<String, Object> props = new HashMap<String, Object>();
            props.put(ItemMetadata.PROP_SUBMITTED_BY, StringUtils.EMPTY);
            props.put(ItemMetadata.PROP_SEND_EMAIL, 0);
            props.put(ItemMetadata.PROP_SUBMITTED_FOR_DELETION, 0);
            props.put(ItemMetadata.PROP_SUBMISSION_COMMENT, StringUtils.EMPTY);
            objectMetadataManager.setObjectMetadata(site, path, props);
        }
        return deploymentItem;
    }

    private void deleteFolder(String site, String path, String user) {
        String folderPath = path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        if (contentService.contentExists(site, path)) {
            // TODO: SJ: This bypasses the Content Service, fix
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                contentService.deleteContent(site, path, true, user);
                objectStateService.deleteObjectStatesForFolder(site, folderPath);
                objectMetadataManager.deleteObjectMetadataForFolder(site, folderPath);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user);
            }
        } else {
            objectStateService.deleteObjectStatesForFolder(site, folderPath);
            objectMetadataManager.deleteObjectMetadataForFolder(site, folderPath);
        }
    }

    @Override
    @ValidateParams
    public void markItemsCompleted(@ValidateStringParam(name = "site") String site,
                                   @ValidateStringParam(name = "environment") String environment,
                                   List<PublishRequest> processedItems) throws DeploymentException {
        for (PublishRequest item : processedItems) {
            item.setState(PublishRequest.State.COMPLETED);
            publishRequestMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsProcessing(@ValidateStringParam(name = "site") String site,
                                    @ValidateStringParam(name = "environment") String environment,
                                    List<PublishRequest> itemsToDeploy) throws DeploymentException {
        for (PublishRequest item : itemsToDeploy) {
            item.setState(PublishRequest.State.PROCESSING);
            publishRequestMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsReady(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "environment") String environment,
                               List<PublishRequest> copyToEnvironmentItems) throws DeploymentException {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(PublishRequest.State.READY_FOR_LIVE);
            publishRequestMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsBlocked(@ValidateStringParam(name = "site") String site,
                                 @ValidateStringParam(name = "environment") String environment,
                                 List<PublishRequest> copyToEnvironmentItems) throws DeploymentException {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(PublishRequest.State.BLOCKED);
            publishRequestMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public List<DeploymentItemTO> processMandatoryDependencies(PublishRequest item,
                                                               Set<String> pathsToDeploy,
                                                               Set<String> missingDependenciesPaths)
            throws DeploymentException, ServiceException {
        List<DeploymentItemTO> mandatoryDependencies = new ArrayList<DeploymentItemTO>();
        String site = item.getSite();
        String path = item.getPath();

        if (StringUtils.equals(item.getAction(), PublishRequest.Action.NEW) ||
                StringUtils.equals(item.getAction(), PublishRequest.Action.MOVE)) {
            if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
                String helpPath = path.replace(FILE_SEPARATOR + getIndexFile(), "");
                int idx = helpPath.lastIndexOf(FILE_SEPARATOR);
                String parentPath = helpPath.substring(0, idx) + FILE_SEPARATOR + getIndexFile();
                if (objectStateService.isNew(site, parentPath) || objectMetadataManager.isRenamed(site, parentPath)) {
                    if (!missingDependenciesPaths.contains(parentPath) && !pathsToDeploy.contains(parentPath)) {
                        deploymentService.cancelWorkflow(site, parentPath);
                        missingDependenciesPaths.add(parentPath);
                        PublishRequest parentItem = createMissingItem(site, parentPath, item);
                        DeploymentItemTO parentDeploymentItem = processItem(parentItem);
                        mandatoryDependencies.add(parentDeploymentItem);
                        mandatoryDependencies.addAll(
                                processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }

            if (!isEnablePublishingWithoutDependencies()) {
                Set<String> dependentPaths = dependencyService.getPublishingDependencies(site, path);
                for (String dependentPath : dependentPaths) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    if (objectStateService.isNew(site, dependentPath) ||
                            objectMetadataManager.isRenamed(site, dependentPath)) {
                        if (!missingDependenciesPaths.contains(dependentPath) &&
                                !pathsToDeploy.contains(dependentPath)) {
                            deploymentService.cancelWorkflow(site, dependentPath);
                            missingDependenciesPaths.add(dependentPath);
                            PublishRequest dependentItem = createMissingItem(site, dependentPath, item);
                            DeploymentItemTO dependentDeploymentItem = processItem(dependentItem);
                            mandatoryDependencies.add(dependentDeploymentItem);
                            mandatoryDependencies.addAll(
                                    processMandatoryDependencies(dependentItem, pathsToDeploy, missingDependenciesPaths));
                        }
                    }
                }
            }
        }

        return mandatoryDependencies;
    }

    private PublishRequest createMissingItem(String site, String itemPath, PublishRequest item) {
        PublishRequest missingItem = new PublishRequest();
        missingItem.setSite(site);
        missingItem.setEnvironment(item.getEnvironment());
        missingItem.setPath(itemPath);
        missingItem.setScheduledDate(item.getScheduledDate());
        missingItem.setState(item.getState());
        if (objectStateService.isNew(site, itemPath)) {
            missingItem.setAction(PublishRequest.Action.NEW);
        }
        ItemMetadata metadata = objectMetadataManager.getProperties(site, itemPath);
        if (metadata != null) {
            if (metadata.getRenamed() != 0) {
                String oldPath = metadata.getOldUrl();
                missingItem.setOldPath(oldPath);
                missingItem.setAction(PublishRequest.Action.MOVE);
            }
            missingItem.setCommitId(metadata.getCommitId());
        }
        String contentTypeClass = contentService.getContentTypeClass(site, itemPath);
        missingItem.setContentTypeClass(contentTypeClass);
        missingItem.setUser(item.getUser());
        missingItem.setSubmissionComment(item.getSubmissionComment());
        missingItem.setPackageId(item.getPackageId());
        return missingItem;
    }

    @Override
    @ValidateParams
    public boolean isPublishingBlocked(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        params.put("state", PublishRequest.State.BLOCKED);
        Integer result = publishRequestMapper.isPublishingBlocked(params);
        return result > 0;
    }

    @Override
    @ValidateParams
    public String getPublishingStatus(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        PublishRequest result = publishRequestMapper.checkPublishingStatus(params);
        return result.getState();
    }

    public String getIndexFile() {
        return studioConfiguration.getProperty(PUBLISHING_MANAGER_INDEX_FILE);
    }

    public boolean isEnablePublishingWithoutDependencies() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(
                PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED));
        return toReturn;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ObjectStateService getObjectStateService() {
        return objectStateService;
    }

    public void setObjectStateService(ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }

    public void setSecurityProvider(SecurityProvider securityProvider) {
        this.securityProvider = securityProvider;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public DeploymentHistoryProvider getDeploymentHistoryProvider() {
        return deploymentHistoryProvider;
    }

    public void setDeploymentHistoryProvider(DeploymentHistoryProvider deploymentHistoryProvider) {
        this.deploymentHistoryProvider = deploymentHistoryProvider;
    }
}

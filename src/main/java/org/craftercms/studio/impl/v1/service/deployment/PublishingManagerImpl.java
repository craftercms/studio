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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.dal.PublishRequest.State.PROCESSING;
import static org.craftercms.studio.api.v1.dal.PublishRequest.State.READY_FOR_LIVE;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_AND_LIVE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ENVIRONMENT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.PROCESSING_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.READY_STATE;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_PUBLISHING_BLACKLIST_REGEX;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED;

public class PublishingManagerImpl implements PublishingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingManagerImpl.class);

    public static final String LIVE_ENVIRONMENT = "live";
    private static final String PRODUCTION_ENVIRONMENT = "Production";

    protected ContentService contentService;
    protected DeploymentService deploymentService;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected StudioConfiguration studioConfiguration;
    protected DependencyService dependencyService;
    protected PublishRequestMapper publishRequestMapper;
    protected ItemServiceInternal itemServiceInternal;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    @ValidateParams
    public List<PublishRequest> getItemsReadyForDeployment(@ValidateStringParam(name = "site") String site,
                                           @ValidateStringParam(name = "environment") String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", DateUtils.getCurrentTime());
        return publishRequestMapper.getItemsReadyForDeployment(params);
    }

    @Override
    public DeploymentItemTO processItem(PublishRequest item)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {

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

        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            liveEnvironment = servicesConfig.getLiveEnvironment(site);
        }

        boolean isLive = false;

        if (isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(environment)) {
                isLive = true;
            }
        }
        else if (StringUtils.equalsIgnoreCase(LIVE_ENVIRONMENT, item.getEnvironment()) ||
                StringUtils.equalsIgnoreCase(PRODUCTION_ENVIRONMENT, environment)) {
            isLive = true;
        }

        if (StringUtils.equals(action, PublishRequest.Action.DELETE)) {
            // Only try to delete oldPath if it exists, this covers the case of move/rename & delete something new
            if (isNotEmpty(oldPath) && contentService.contentExists(site, oldPath)) {
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
                if (isLive) {
                    itemServiceInternal.clearPreviousPath(site, path);
                }
            }


            boolean hasChildren = false;

            if (item.getPath().endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                if (contentService.contentExists(site,
                        path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""))) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    RepositoryItem[] children = contentRepository.getContentChildren(site,
                            path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""));

                    if (children.length > 1) {
                        hasChildren = true;
                    }
                }
            }

            if (contentService.contentExists(site, path)) {
                contentService.deleteContent(site, path, user);

                if (!hasChildren) {
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
                        itemServiceInternal.clearPreviousPath(site, path);
                    }
                }
            }

            Workflow workflowEntry =
                    workflowServiceInternal.getWorkflowEntry(site, path, deploymentItem.getPackageId());

            if (workflowEntry == null) {
                if (contentService.contentExists(site, path)) {
                    Item it = itemServiceInternal.getItem(site, path, true);
                    if (Objects.nonNull(it)) {
                        if (isLive) {
                            itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK,
                                    PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
                            itemServiceInternal.clearPreviousPath(site, path);
                        } else {
                            itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_ON_MASK, PUBLISH_TO_STAGE_OFF_MASK);
                        }
                    } else {
                        LOGGER.warn("Content item: '" + site + "':'" + path + "' doesn't exists in " +
                                "the database, but does exist in git. This may cause problems " +
                                "in the environment: '" + environment + "'");
                    }
                } else {
                    LOGGER.warn("Content item: '" + site + "':'" + path + "' cannot be published. " +
                            "Content does not exist in git nor in the database. Skipping...");
                    return null;
                }
            } else {
                if (isLive) {
                    itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK,
                            PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
                    itemServiceInternal.clearPreviousPath(site, path);
                } else {
                    itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_ON_MASK, PUBLISH_TO_STAGE_OFF_MASK);
                }
            }
            String blacklistConfig = studioConfiguration.getProperty(CONFIGURATION_PUBLISHING_BLACKLIST_REGEX);
            if (isNotEmpty(blacklistConfig) &&
                    ContentUtils.matchesPatterns(item.getPath(), Arrays.asList(StringUtils.split(blacklistConfig, ",")))) {
                LOGGER.debug("File " + item.getPath() + " of the site " + site + " will not be published because it " +
                        "matches the configured publishing blacklist regex patterns.");
                markItemsCompleted(site, item.getEnvironment(), List.of(item));
                deploymentItem = null;
            }
        }
        return deploymentItem;
    }

    private void deleteFolder(String site, String path, String user) throws ServiceLayerException, UserNotFoundException {
        String folderPath = path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        if (contentService.contentExists(site, path)) {
            // TODO: SJ: This bypasses the Content Service, fix
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                contentService.deleteContent(site, path, true, user);
                itemServiceInternal.deleteItem(site, folderPath);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user);
            }
        } else {
            itemServiceInternal.deleteItem(site, folderPath);
        }
    }

    @Override
    @ValidateParams
    public void markItemsCompleted(@ValidateStringParam(name = "site") String site,
                                   @ValidateStringParam(name = "environment") String environment,
                                   List<PublishRequest> processedItems) throws DeploymentException {
        ZonedDateTime publishedOn = DateUtils.getCurrentTime();
        for (PublishRequest item : processedItems) {
            item.setState(PublishRequest.State.COMPLETED);
            item.setPublishedOn(publishedOn);
            retryingDatabaseOperationFacade.markPublishRequestItemCompleted(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsProcessing(@ValidateStringParam(name = "site") String site,
                                    @ValidateStringParam(name = "environment") String environment,
                                    List<PublishRequest> itemsToDeploy) throws DeploymentException {
        for (PublishRequest item : itemsToDeploy) {
            item.setState(PublishRequest.State.PROCESSING);
            retryingDatabaseOperationFacade.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsReady(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "environment") String environment,
                               List<PublishRequest> copyToEnvironmentItems) throws DeploymentException {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(READY_FOR_LIVE);
            retryingDatabaseOperationFacade.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsBlocked(@ValidateStringParam(name = "site") String site,
                                 @ValidateStringParam(name = "environment") String environment,
                                 List<PublishRequest> copyToEnvironmentItems) throws DeploymentException {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(PublishRequest.State.BLOCKED);
            retryingDatabaseOperationFacade.updateItemDeploymentState(item);
        }
    }

    @Override
    public List<DeploymentItemTO> processMandatoryDependencies(PublishRequest item,
                                                               Set<String> pathsToDeploy,
                                                               Set<String> missingDependenciesPaths)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {
        List<DeploymentItemTO> mandatoryDependencies = new ArrayList<>();
        String site = item.getSite();
        String path = item.getPath();

        if (StringUtils.equals(item.getAction(), PublishRequest.Action.NEW) ||
                StringUtils.equals(item.getAction(), PublishRequest.Action.MOVE)) {
            if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
                Path p = Paths.get(path);
                List<Path> parts = new LinkedList<>();
                if (Objects.nonNull(p.getParent())) {
                    p.getParent().iterator().forEachRemaining(parts::add);
                }
                List<String> ancestors = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(parts)) {
                    StringBuilder sbAncestor = new StringBuilder();
                    for (Path ancestor : parts) {
                        if (isNotEmpty(ancestor.toString())) {
                            sbAncestor.append(FILE_SEPARATOR).append(ancestor);
                            ancestors.add(0, sbAncestor.toString());
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(ancestors)) {
                    Iterator<String> ancestorIterator = ancestors.iterator();
                    boolean breakLoop = false;
                    while (!breakLoop && ancestorIterator.hasNext()) {
                        String anc = ancestorIterator.next();
                        Item it = itemServiceInternal.getItem(site, anc, true);
                        if (Objects.nonNull(it) && !StringUtils.equals(it.getSystemType(), CONTENT_TYPE_FOLDER) &&
                                (ItemState.isNew(it.getState()) || isNotEmpty(it.getPreviousPath())) &&
                                !missingDependenciesPaths.contains(it.getPath()) &&
                                !pathsToDeploy.contains(it.getPath())) {
                            deploymentService.cancelWorkflow(site, it.getPath());
                            missingDependenciesPaths.add(it.getPath());
                            PublishRequest parentItem = createMissingItem(site, it.getPath(), item);
                            DeploymentItemTO parentDeploymentItem = processItem(parentItem);
                            mandatoryDependencies.add(parentDeploymentItem);
                            mandatoryDependencies.addAll(
                                    processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                        }
                    }
                }
            }

            if (!isEnablePublishingWithoutDependencies()) {
                List<String> dependentPaths = dependencyService.getPublishingDependencies(site, path);
                for (String dependentPath : dependentPaths) {
                    // TODO: SJ: This bypasses the Content Service, fix
                    Item it = itemServiceInternal.getItem(site, dependentPath);
                    if (ItemState.isNew(it.getState()) || isNotEmpty(it.getPreviousPath())) {
                        if (!missingDependenciesPaths.contains(dependentPath) &&
                                !pathsToDeploy.contains(dependentPath)) {
                            deploymentService.cancelWorkflow(site, dependentPath);
                            missingDependenciesPaths.add(dependentPath);
                            PublishRequest dependentItem = createMissingItem(site, dependentPath, item);
                            DeploymentItemTO dependentDeploymentItem = processItem(dependentItem);
                            if (Objects.nonNull(dependentDeploymentItem)) {
                                mandatoryDependencies.add(dependentDeploymentItem);
                            }
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
        Item it = itemServiceInternal.getItem(site, itemPath);
        if (ItemState.isNew(it.getState())) {
            missingItem.setAction(PublishRequest.Action.NEW);
        }

        if (isNotEmpty(it.getPreviousPath())) {
            String oldPath = it.getPreviousPath();
            missingItem.setOldPath(oldPath);
            missingItem.setAction(PublishRequest.Action.MOVE);
        }
        String commitId = it.getCommitId();
        if (isNotEmpty(commitId)) {
            missingItem.setCommitId(commitId);
        } else {
            missingItem.setCommitId(contentRepository.getRepoLastCommitId(site));
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
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", PublishRequest.State.BLOCKED);
        Integer result = publishRequestMapper.isPublishingBlocked(params);
        return result > 0;
    }

    @Override
    @ValidateParams
    public boolean hasPublishingQueuePackagesReady(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", READY_FOR_LIVE);
        Integer result = publishRequestMapper.isPublishingBlocked(params);
        return result > 0;
    }

    @Override
    @ValidateParams
    public String getPublishingStatus(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        List<String> states = new ArrayList<>() {{
            add(READY_FOR_LIVE);
            add(PublishRequest.State.BLOCKED);
            add(PublishRequest.State.PROCESSING);
        }};

        params.put("states", states);
        PublishRequest result = publishRequestMapper.checkPublishingStatus(params);
        return result.getState();
    }

    @Override
    @ValidateParams
    public boolean isPublishingQueueEmpty(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", READY_FOR_LIVE);
        Integer result = publishRequestMapper.isPublishingQueueEmpty(params);
        return result < 1;
    }

    @Override
    @ValidateParams
    public void resetProcessingQueue(@ValidateStringParam(name = "site") String site,
                                        @ValidateStringParam(name = "environment") String environment) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, site);
        params.put(ENVIRONMENT, environment);
        params.put(PROCESSING_STATE, PROCESSING);
        params.put(READY_STATE, READY_FOR_LIVE);
        retryingDatabaseOperationFacade.resetPublishRequestProcessingQueue(params);
    }

    public boolean isEnablePublishingWithoutDependencies() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(
                PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED));
        return toReturn;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setPublishRequestMapper(PublishRequestMapper publishRequestMapper) {
        this.publishRequestMapper = publishRequestMapper;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}

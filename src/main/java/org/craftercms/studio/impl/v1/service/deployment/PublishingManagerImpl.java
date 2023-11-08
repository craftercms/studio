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
package org.craftercms.studio.impl.v1.service.deployment;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.to.DeploymentItemTO;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.dal.PublishRequest.State.PROCESSING;
import static org.craftercms.studio.api.v1.dal.PublishRequest.State.READY_FOR_LIVE;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_PUBLISHING_BLACKLIST_REGEX;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHING_MANAGER_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED;
import static org.craftercms.studio.impl.v1.util.ContentUtils.matchesPatterns;

public class PublishingManagerImpl implements PublishingManager {

    private static final Logger logger = LoggerFactory.getLogger(PublishingManagerImpl.class);

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
    @Valid
    public List<PublishRequest> getItemsReadyForDeployment(@ValidateStringParam String site,
                                           @ValidateStringParam String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", DateUtils.getCurrentTime());
        return publishRequestMapper.getItemsReadyForDeployment(params);
    }

    /**
     * Indicates if environment is the live/prod environment
     * for the given site
     * @param site the site id
     * @param env the publishing environment
     * @return true if env is the live environment for the site
     */
    private boolean isLiveEnv(final String site, final String env) {
        String liveEnvironment = LIVE_ENVIRONMENT;

        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            liveEnvironment = servicesConfig.getLiveEnvironment(site);
        }

        boolean isLive = false;

        if (isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(env)) {
                isLive = true;
            }
        } else if (equalsIgnoreCase(LIVE_ENVIRONMENT, env) ||
                equalsIgnoreCase(PRODUCTION_ENVIRONMENT, env)) {
            isLive = true;
        }

        return isLive;
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

        boolean isLive = isLiveEnv(site, environment);

        if (StringUtils.equals(action, PublishRequest.Action.DELETE)) {
            processDeleteItem(item, deploymentItem, site, path, oldPath, user, isLive);
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
            if (workflowEntry == null && !contentService.contentExists(site, path)) {
                logger.warn("Item in site '{}' path '{}' doesn't exist in the database nor the git repository. " +
                        "Skipping publishing of this item.", site, path);
                deploymentItem = null;
            }
            if (isPathBlackListed(item.getPath())) {
                logger.debug("The file in site '{}' path '{}' matches the publishing blacklist and will not be " +
                                "published", site, item.getPath());
                // TODO: JM: Should these be marked as CANCELLED instead of COMPLETED ?
                markItemsCompleted(site, item.getEnvironment(), List.of(item));
                deploymentItem = null;
            }
        }
        return deploymentItem;
    }

    @Override
    public void setPublishedState(String site, String environment, List<PublishRequest> items) {
        boolean isLive = isLiveEnv(site, environment);
        items.parallelStream().forEach(publishRequest -> {
            String path = publishRequest.getPath();
            Workflow workflowEntry =
                    workflowServiceInternal.getWorkflowEntry(site, path, publishRequest.getPackageId());
            if (workflowEntry != null) {
                setPublishedState(path, site, isLive);
                return;
            }
            if (!contentService.contentExists(site, path)) {
                logger.warn("Item in site '{}' path '{}' doesn't exist in the database nor the git repository. " +
                        "Skipping publishing of this item.", site, path);
                return;
            }
            Item it = itemServiceInternal.getItem(site, path, true);
            if (isNull(it)) {
                logger.warn("Item in site '{}' path '{}' doesn't exist in the database, but it does exist " +
                                "in git. This may cause problems in the publishing target '{}'",
                        site, path, environment);
            } else {
                setPublishedState(path, site, isLive);
            }
        });
    }

    private void setPublishedState(String path, String site, boolean isLive) {
        if (isLive) {
            itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK,
                    PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
            itemServiceInternal.clearPreviousPath(site, path);
        } else {
            itemServiceInternal.updateStateBits(site, path, PUBLISH_TO_STAGE_ON_MASK, PUBLISH_TO_STAGE_OFF_MASK);
        }
    }

    /**
     * Processes a deleted {@link PublishRequest}
     */
    private void processDeleteItem(PublishRequest item, DeploymentItemTO deploymentItem, String site, String path, String oldPath, String user, boolean isLive) throws ServiceLayerException, UserNotFoundException {
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
    }

    /**
     * Indicates if the given path matches the configured publishing blacklist regex.
     * Blacklisted paths should NOT be published
     * @param path path to be published
     * @return true if the path is blacklisted
     */
    private boolean isPathBlackListed(final String path) {
        String blacklistConfig = studioConfiguration.getProperty(CONFIGURATION_PUBLISHING_BLACKLIST_REGEX);
        return isNotEmpty(blacklistConfig) &&
                matchesPatterns(path, asList(split(blacklistConfig, ",")));
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
    @Valid
    public void markItemsCompleted(@ValidateStringParam String site,
                                   @ValidateStringParam String environment,
                                   List<PublishRequest> processedItems) {
        ZonedDateTime publishedOn = DateUtils.getCurrentTime();
        processedItems.parallelStream().forEach(item-> {
            item.setState(PublishRequest.State.COMPLETED);
            item.setPublishedOn(publishedOn);
            retryingDatabaseOperationFacade.retry(() -> publishRequestMapper.markItemCompleted(item));
        });
    }

    @Override
    @Valid
    public void markItemsProcessing(@ValidateStringParam String site,
                                    @ValidateStringParam String environment,
                                    List<PublishRequest> itemsToDeploy) {
        for (PublishRequest item : itemsToDeploy) {
            item.setState(PublishRequest.State.PROCESSING);
            retryingDatabaseOperationFacade.retry(() -> publishRequestMapper.updateItemDeploymentState(item));
        }
    }

    @Override
    @Valid
    public void markItemsReady(@ValidateStringParam String site,
                               @ValidateStringParam String environment,
                               List<PublishRequest> copyToEnvironmentItems) {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(READY_FOR_LIVE);
            retryingDatabaseOperationFacade.retry(() -> publishRequestMapper.updateItemDeploymentState(item));
        }
    }

    @Override
    @Valid
    public void markItemsBlocked(@ValidateStringParam String site,
                                 @ValidateStringParam String environment,
                                 List<PublishRequest> copyToEnvironmentItems) {
        for (PublishRequest item : copyToEnvironmentItems) {
            item.setState(PublishRequest.State.BLOCKED);
            retryingDatabaseOperationFacade.retry(() -> publishRequestMapper.updateItemDeploymentState(item));
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
            if (matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
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
                        if (!StringUtils.equals(it.getSystemType(), CONTENT_TYPE_FOLDER) &&
                                !missingDependenciesPaths.contains(dependentPath) &&
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
    @Valid
    public boolean isPublishingBlocked(@ValidateStringParam String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", PublishRequest.State.BLOCKED);
        int result = publishRequestMapper.isPublishingBlocked(params);
        return result > 0;
    }

    @Override
    @Valid
    public boolean hasPublishingQueuePackagesReady(@ValidateStringParam String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", READY_FOR_LIVE);
        int result = publishRequestMapper.isPublishingBlocked(params);
        return result > 0;
    }

    @Override
    @Valid
    public String getPublishingStatus(@ValidateStringParam String site) {
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
    @Valid
    public boolean isPublishingQueueEmpty(@ValidateStringParam String site) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("now", DateUtils.getCurrentTime());
        params.put("state", READY_FOR_LIVE);
        int result = publishRequestMapper.isPublishingQueueEmpty(params);
        return result < 1;
    }

    @Override
    @Valid
    public void resetProcessingQueue(@ValidateStringParam String site,
                                        @ValidateStringParam String environment) {
        Map<String, String> params = new HashMap<>();
        params.put(SITE_ID, site);
        params.put(ENVIRONMENT, environment);
        params.put(PROCESSING_STATE, PROCESSING);
        params.put(READY_STATE, READY_FOR_LIVE);
        retryingDatabaseOperationFacade.retry(() -> publishRequestMapper.resetProcessingQueue(params));
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

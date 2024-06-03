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

package org.craftercms.studio.impl.v2.service.publish.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.dal.publish.*;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageType;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.repository.LockedRepositoryException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.StudioUtils;
import org.craftercms.studio.model.publish.PublishingTarget;
import org.craftercms.studio.model.rest.dashboard.DashboardPublishingPackage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;

import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.tika.io.FilenameUtils.getName;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.ADD;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.SUBMITTED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageType.*;
import static org.craftercms.studio.api.v2.event.workflow.WorkflowEvent.WorkFlowEventType.APPROVE;
import static org.craftercms.studio.api.v2.event.workflow.WorkflowEvent.WorkFlowEventType.SUBMIT;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v2.utils.DateUtils.formatDateIso;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PublishServiceInternalImpl implements PublishService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(PublishServiceInternalImpl.class);

    private ContentRepository contentRepository;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private StudioUtils studioUtils;

    protected ItemServiceInternal itemServiceInternal;

    protected ApplicationContext applicationContext;
    private ServicesConfig servicesConfig;
    private UserServiceInternal userServiceInternal;
    private AuditServiceInternal auditServiceInternal;
    private DependencyService dependencyServiceInternal;
    private PublishDAO publishDao;
    private ItemTargetDAO itemTargetDao;

    private SitesService siteService;
    private GeneralLockService generalLockService;
    private ActivityStreamServiceInternal activityService;

    @Override
    public int getPublishingPackagesTotal(String siteId, String environment, String path, List<String> states) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getPublishingPackagesTotal(siteId, environment, path, states);
    }

    @Override
    public List<PublishingPackage> getPublishingPackages(String siteId, String environment, String path,
                                                         List<String> states, int offset, int limit) {
        // TODO: implement for new publishing system
        return emptyList();
//        return publishRequestDao.getPublishingPackages(siteId, environment, path, states, offset, limit);
    }

    @Override
    public PublishingPackageDetails getPublishingPackageDetails(String siteId, String packageId) {
        // TODO: implement for new publishing system
        return null;
//        List<PublishRequest> publishingRequests = publishRequestDao.getPublishingPackageDetails(siteId, packageId);
//        PublishingPackageDetails publishingPackageDetails = new PublishingPackageDetails();
//        List<PublishingPackageDetails.PublishingPackageItem> packageItems = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(publishingRequests)) {
//            PublishRequest pr = publishingRequests.get(0);
//            publishingPackageDetails.setSiteId(pr.getSite());
//            publishingPackageDetails.setPackageId(pr.getPackageId());
//            publishingPackageDetails.setEnvironment(pr.getEnvironment());
//            publishingPackageDetails.setState(pr.getState());
//            publishingPackageDetails.setScheduledDate(pr.getScheduledDate());
//            publishingPackageDetails.setUser(pr.getUser());
//            publishingPackageDetails.setComment(pr.getSubmissionComment());
//        }
//        for (PublishRequest publishRequest : publishingRequests) {
//            PublishingPackageDetails.PublishingPackageItem item = new PublishingPackageDetails.PublishingPackageItem();
//            item.setPath(publishRequest.getPath());
//            item.setContentTypeClass(publishRequest.getContentTypeClass());
//            packageItems.add(item);
//        }
//        publishingPackageDetails.setItems(packageItems);
//        return publishingPackageDetails;
    }

    @Override
    public void cancelPublishingPackages(String siteId, List<String> packageIds) {
        // TODO: implement for new publishing system
//        retryingDatabaseOperationFacade.retry(() -> publishRequestDao.cancelPackages(siteId, packageIds, CANCELLED));
    }

    @Override
    public int getPublishingHistoryDetailTotalItems(String siteId, String packageId) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getPublishingHistoryDetailTotalItems(siteId, packageId);
    }

    // TODO: implement for new publishing system
//    public List<PublishRequest> getPublishingHistoryDetail(String siteId, String packageId, int offset, int limit) {
//        return publishRequestDao.getPublishingHistoryDetail(siteId, packageId, offset, limit);
//    }

    public List<DeploymentHistoryItem> getDeploymentHistory(String siteId, List<String> environments,
                                                            ZonedDateTime fromDate, ZonedDateTime toDate,
                                                            String filterType, int numberOfItems) {
        // TODO: implement for new publishing system
        return emptyList();
    }

    @Override
    public List<DeploymentHistoryGroup> getDeploymentHistory(String siteId, int daysFromToday, int numberOfItems,
                                                             String filterType) throws ServiceLayerException, UserNotFoundException {
        ZonedDateTime toDate = DateUtils.getCurrentTime();
        ZonedDateTime fromDate = toDate.minusDays(daysFromToday);
        List<String> environments = studioUtils.getEnvironmentNames(siteId);
        List<DeploymentHistoryItem> deploymentHistoryItems = getDeploymentHistory(siteId,
                environments, fromDate, toDate, filterType, numberOfItems);
        List<DeploymentHistoryGroup> groups = new ArrayList<>();

        if (deploymentHistoryItems == null) {
            return groups;
        }
        int count = 0;
        Map<String, Set<String>> processedItems = new HashMap<>();
        for (int index = 0; index < deploymentHistoryItems.size() && count < numberOfItems; index++) {
            DeploymentHistoryItem entry = deploymentHistoryItems.get(index);
            String env = entry.getEnvironment();
            if (!processedItems.containsKey(env)) {
                processedItems.put(env, new HashSet<>());
            }
            if (!processedItems.get(env).contains(entry.getPath())) {
                ContentItemTO deployedItem = studioUtils.getContentItemForDashboard(entry.getSite(), entry.getPath());
                if (deployedItem != null) {
                    deployedItem.eventDate = entry.getDeploymentDate();
                    deployedItem.endpoint = entry.getTarget();
                    User user = userServiceInternal.getUserByIdOrUsername(-1, entry.getUser());
                    deployedItem.setUser(user.getUsername());
                    deployedItem.setUserFirstName(user.getFirstName());
                    deployedItem.setUserLastName(user.getLastName());
                    deployedItem.setEndpoint(entry.getEnvironment());
                    String deployedLabel = formatDateIso(entry.getDeploymentDate().truncatedTo(DAYS));
                    if (groups.size() > 0) {
                        DeploymentHistoryGroup group = groups.get(groups.size() - 1);
                        String lastDeployedLabel = group.getInternalName();
                        if (lastDeployedLabel.equals(deployedLabel)) {
                            // add to the last task if it is deployed on the same day
                            group.setNumOfChildren(group.getNumOfChildren() + 1);
                            group.getChildren().add(deployedItem);
                        } else {
                            groups.add(createDeploymentHistoryGroup(deployedLabel, deployedItem));
                        }
                    } else {
                        groups.add(createDeploymentHistoryGroup(deployedLabel, deployedItem));
                    }
                    processedItems.get(env).add(entry.getPath());
                }
            }
        }
        return groups;
    }

    private DeploymentHistoryGroup createDeploymentHistoryGroup(String deployedLabel, ContentItemTO item) {
        // otherwise just add as the last task
        DeploymentHistoryGroup group = new DeploymentHistoryGroup();
        group.setInternalName(deployedLabel);
        List<ContentItemTO> taskItems = group.getChildren();
        if (taskItems == null) {
            taskItems = new ArrayList<>();
            group.setChildren(taskItems);
        }
        taskItems.add(item);
        group.setNumOfChildren(taskItems.size());
        return group;
    }

    @Override
    public List<PublishingTarget> getAvailablePublishingTargets(@SiteId String siteId) {
        var availablePublishingTargets = new ArrayList<PublishingTarget>();
        var liveTarget = new PublishingTarget();
        liveTarget.setName(servicesConfig.getLiveEnvironment(siteId));
        availablePublishingTargets.add(liveTarget);
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            var stagingTarget = new PublishingTarget();
            stagingTarget.setName(servicesConfig.getStagingEnvironment(siteId));
            availablePublishingTargets.add(stagingTarget);
        }
        return availablePublishingTargets;
    }

    @Override
    public boolean isSitePublished(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId) {
        // Site is published if PUBLISHED repo exists
        return contentRepository.publishedRepositoryExists(siteId);
    }

    @Override
    public int getPublishingItemsScheduledTotal(String siteId, String publishingTarget, String approver,
                                                ZonedDateTime dateFrom, ZonedDateTime dateTo, List<String> systemTypes) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao
//                .getPublishingItemsScheduledTotal(siteId, publishingTarget, approver, READY_FOR_LIVE, dateFrom, dateTo, systemTypes)
//                .orElse(0);
    }

    // TODO: implement for new publishing system
//    @Override
//    public List<PublishRequest> getPublishingItemsScheduled(String siteId, String publishingTarget, String approver,
//                                                            ZonedDateTime dateFrom, ZonedDateTime dateTo,
//                                                            List<String> systemTypes, List<SortField> sortFields, int offset, int limit) {
//        return publishRequestDao.getPublishingItemsScheduled(siteId, publishingTarget, approver, READY_FOR_LIVE,
//                dateFrom, dateTo, systemTypes, DalUtils.mapSortFields(sortFields,PublishRequestDAO.SORT_FIELD_MAP), offset, limit);
//    }

    @Override
    public int getPublishingPackagesHistoryTotal(String siteId, String publishingTarget, String approver,
                                                 ZonedDateTime dateFrom, ZonedDateTime dateTo) {
        // TODO: implement for new publishing system
        return 0;
        // Need to check if null because of COUNT + GROUP BY
//        return publishRequestDao
//                .getPublishingPackagesHistoryTotal(siteId, publishingTarget, approver, COMPLETED, dateFrom, dateTo)
//                .orElse(0);
    }

    @Override
    public List<DashboardPublishingPackage> getPublishingPackagesHistory(String siteId, String publishingTarget,
                                                                         String approver, ZonedDateTime dateFrom,
                                                                         ZonedDateTime dateTo, int offset, int limit) {
        // TODO: implement for new publishing system
        return emptyList();
//        return publishRequestDao.getPublishingPackagesHistory(siteId, publishingTarget, approver, COMPLETED, dateFrom,
//                dateTo, offset, limit);
    }

    @Override
    public int getNumberOfPublishes(String siteId, int days) {
        // TODO: implement for new publishing system
        return 0;
//        return publishRequestDao.getNumberOfPublishes(siteId, days);
    }

    @Override
    public PublishDependenciesResult getPublishDependencies(final String siteId, final String publishingTarget,
                                                            final Collection<PublishRequestPath> publishRequestPaths,
                                                            final Collection<String> commitIds) throws ServiceLayerException, IOException {
        Site site = siteService.getSite(siteId);
        Set<String> corePackagePaths = new HashSet<>();
        corePackagePaths.addAll(publishRequestPaths.stream()
                .collect(teeing(
                        flatMapping(requestPath -> expandPublishRequestPath(site, requestPath).stream(), toSet()),
                        flatMapping(requestPath -> (requestPath.includeSoftDeps() ?
                                dependencyServiceInternal.getSoftDependencies(site.getSiteId(), Set.of(requestPath.path()))
                                : SetUtils.<String>emptySet()).stream(), toSet()),
                        SetUtils::union
                )));

        Map<Boolean, List<String>> commitOperations = contentRepository.validatePublishCommits(site.getSiteId(), commitIds).stream()
                .map(commitId -> contentRepository.getOperationsFromFirstParentDiff(site.getSiteId(), commitId))
                .flatMap(List::stream)
                .filter(getCommitRepoOperationsFilter(site))
                .collect(partitioningBy(op -> op.getAction() == RepoOperation.Action.DELETE,
                        mapping(RepoOperation::getPath, toList())));

        // Add non-delete operations
        corePackagePaths.addAll(commitOperations.get(false));

        Collection<String> deletedPaths = commitOperations.get(true);

        Collection<String> softDependencies = dependencyServiceInternal.getSoftDependencies(siteId, corePackagePaths);
        // Get hard deps of them all
        Collection<String> hardDependencies = dependencyServiceInternal.getHardDependencies(siteId, publishingTarget, union(corePackagePaths, softDependencies));
        return new PublishDependenciesResult(corePackagePaths, deletedPaths, hardDependencies, softDependencies);
    }

    /**
     * Get common filter for commit repo operations to be included in publish dependencies
     * or actual publishing package submission
     */
    private Predicate<RepoOperation> getCommitRepoOperationsFilter(final Site site) {
        return op -> !contains(IGNORE_FILES, getName(op.getMoveToPath()))
                && !contains(IGNORE_FILES, getName(op.getPath()))
                // Ignore deletes if the path exists in current version
                && (op.getAction() != RepoOperation.Action.DELETE || !contentRepository.contentExists(site.getSiteId(), op.getPath()));
    }

    /**
     * Audit publish submission
     *
     * @param p         the publish package
     * @param operation the audit operation
     */
    private void auditPublishSubmission(final PublishPackage p, final String operation) {
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setActorId(String.valueOf(p.getSubmitterId()));
        auditLog.setSiteId(p.getSiteId());
        auditLog.setPrimaryTargetId(String.valueOf(p.getSiteId()));
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(String.valueOf(p.getSiteId()));

        AuditLogParameter commentParam = new AuditLogParameter();
        commentParam.setTargetId(TARGET_TYPE_SUBMISSION_COMMENT);
        commentParam.setTargetType(TARGET_TYPE_SUBMISSION_COMMENT);
        commentParam.setTargetValue(defaultIfEmpty(p.getComment(), ""));

        AuditLogParameter packageParam = new AuditLogParameter();
        packageParam.setTargetId(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetType(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetValue(String.valueOf(p.getId()));

        auditLog.setParameters(List.of(commentParam, packageParam));
        auditServiceInternal.insertAuditLog(auditLog);
        activityService.insertActivity(p.getSiteId(), p.getSubmitterId(), TARGET_TYPE_PUBLISHING_PACKAGE, now(), null, null);
    }

    /**
     * Create publish items from commit ids.
     * Extract operations from commit ids and create publish items.
     * Notice that delete operations will not be processed if new version of the path exists.
     */
    private void createPublishItemsFromCommitIds(final Site site, final Collection<String> commitIds,
                                                 final Map<String, PublishItem> publishItemsByPath)
            throws ServiceLayerException, IOException {
        if (isEmpty(commitIds)) {
            return;
        }
        // Validate and sort commits
        List<String> sortedCommits = contentRepository.validatePublishCommits(site.getSiteId(), commitIds);
        publishItemsByPath.putAll(
                sortedCommits.stream()
                        .map(commitId -> contentRepository.getOperationsFromFirstParentDiff(site.getSiteId(), commitId))
                        .flatMap(List::stream)
                        .filter(getCommitRepoOperationsFilter(site))
                        .map(op -> createPublishItem(site, op.getPath(),
                                op.getAction() == RepoOperation.Action.DELETE ? PublishItem.Action.DELETE : ADD, true))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }

    private PublishItem createPublishItem(final Site site, final String path,
                                          final PublishItem.Action action, final boolean userRequested) {
        PublishItem publishItem = new PublishItem();
        Collection<ItemTarget> itemTargets = itemTargetDao.getByItemPath(site.getId(), path);
        publishItem.setAction(action);
        publishItem.setPath(path);
        publishItem.setUserRequested(userRequested);
        if (!CollectionUtils.isNotEmpty(itemTargets)) {
            return publishItem;
        }
        String liveTarget = servicesConfig.getLiveEnvironment(site.getSiteId());
        String stagingTarget = servicesConfig.getStagingEnvironment(site.getSiteId());
        itemTargets.forEach(itemTarget -> {
            if (isNotEmpty(itemTarget.getOldPath())) {
                if (itemTarget.getTarget().equals(stagingTarget)) {
                    publishItem.setStagingOldPath(itemTarget.getOldPath());
                } else if (itemTarget.getTarget().equals(liveTarget)) {
                    publishItem.setLiveOldPath(itemTarget.getOldPath());
                }
            }
        });
        return publishItem;
    }

    /**
     * Create publish items from list of {@link PublishRequestPath}.
     * For each PublishRequest path:
     * - Add the path if it is not a folder
     * - Include soft deps if requested
     * - Include children if requested
     */
    private void createPublishItemsFromPaths(final Site site, final Collection<PublishRequestPath> publishRequestPaths,
                                             final Map<String, PublishItem> publishItemsByPath) {
        if (isEmpty(publishRequestPaths)) {
            return;
        }

        Set<String> allPaths = new HashSet<>();
        Set<String> softDepsPaths = new HashSet<>();
        for (PublishRequestPath publishRequestPath : publishRequestPaths) {
            Set<String> expandedPathList = expandPublishRequestPath(site, publishRequestPath);
            allPaths.addAll(expandedPathList);
            if (publishRequestPath.includeSoftDeps()) {
                softDepsPaths.addAll(expandedPathList);
            }
        }
        if(!softDepsPaths.isEmpty()) {
            allPaths.addAll(dependencyServiceInternal.getSoftDependencies(site.getSiteId(), softDepsPaths));
        }

        publishItemsByPath.putAll(
                allPaths.stream()
                        .filter(path -> !publishItemsByPath.containsKey(path))
                        .map(path -> createPublishItem(site, path, ADD, true))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }

    /**
     * Expand publish request path.
     * - Include the path itself if not a folder
     * - Include non-folder children if requested, recursively
     */
    private Set<String> expandPublishRequestPath(final Site site, final PublishRequestPath publishPath) {
        Set<String> paths = new HashSet<>();
        if (!contentRepository.isFolder(site.getSiteId(), publishPath.path())) {
            paths.add(publishPath.path());
        }
        if (publishPath.includeChildren()) {
            // Notice that we are publishing regardless of the item's state, consistent
            // with current behavior when publishing a live item directly
            paths.addAll(itemServiceInternal.getChildrenPaths(site.getId(), publishPath.path()));
        }
        return paths;
    }

    /**
     * Create publish items for hard dependencies.
     * For each non-delete PublishItem, get hard dependencies and add them to the publishItemsByPath map.
     */
    private void createPublishItemsForHardDeps(Site site, Map<String, PublishItem> publishItemsByPath) {
        Collection<String> paths = publishItemsByPath.keySet().stream()
                .filter(p -> publishItemsByPath.get(p).getAction() != PublishItem.Action.DELETE)
                .collect(toList());
        if (isEmpty(paths)) {
            return;
        }
        publishItemsByPath.putAll(
                dependencyServiceInternal.getHardDependencies(site.getSiteId(), paths).stream()
                        .filter(dep -> !publishItemsByPath.containsKey(dep))
                        .map(dep -> createPublishItem(site, dep, ADD, false))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }


    @Override
    public long publish(final String siteId, final String publishingTarget, final List<PublishRequestPath> paths,
                        final List<String> commitIds, final Instant schedule, final String comment, final boolean publishAll)
            throws ServiceLayerException, AuthenticationException {
        return routePackageSubmission(siteId, publishingTarget, paths, commitIds, schedule, comment, false, publishAll);
    }

    @Override
    public long requestPublish(final String siteId, final String publishingTarget, final List<PublishRequestPath> paths,
                               final List<String> commitIds, final Instant schedule, final String comment, final boolean publishAll)
            throws AuthenticationException, ServiceLayerException {
        return routePackageSubmission(siteId, publishingTarget, paths, commitIds, schedule, comment, true, publishAll);
    }

    /**
     * Routes the request to the appropriate method based on the site's publishing repo status.
     */
    private long routePackageSubmission(final String siteId, final String publishingTarget, final List<PublishRequestPath> paths,
                                        final List<String> commitIds, final Instant schedule, final String comment,
                                        final boolean requestApproval, final boolean publishAll)
            throws ServiceLayerException, AuthenticationException {
        Site site = siteService.getSite(siteId);
        if (!site.isSitePublishedRepoCreated()) {
            return new InitialPublishPackageBuilder(site, publishingTarget, requestApproval, comment).build();
        }

        if (publishAll) {
            if (schedule != null) {
                throw new InvalidParametersException("Failed to submit publishing package: Cannot schedule a publish all operation");
            }
            return new PublishAllPackageBuilder(site, publishingTarget, requestApproval, comment).build();
        }

        return new ItemListPublishPackageBuilder(site, publishingTarget,
                paths, commitIds, schedule, comment, requestApproval).build();
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setRetryingDatabaseOperationFacade(final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }

    public void setItemServiceInternal(final ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setStudioUtils(final StudioUtils studioUtils) {
        this.studioUtils = studioUtils;
    }

    public void setUserServiceInternal(final UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setPublishDao(final PublishDAO publishDao) {
        this.publishDao = publishDao;
    }

    public void setSiteService(final SitesService siteService) {
        this.siteService = siteService;
    }

    public void setGeneralLockService(final GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setActivityService(final ActivityStreamServiceInternal activityService) {
        this.activityService = activityService;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyService dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public void setItemTargetDao(ItemTargetDAO itemTargetDao) {
        this.itemTargetDao = itemTargetDao;
    }

    /**
     * Publish package builder.
     * Encapsulates the logic to build a publish package and its items according to the different parameters
     */
    private abstract class PublishPackageBuilder {

        protected final Site site;
        protected final String target;
        protected final String lockKey;
        protected final boolean requestApproval;
        protected final String comment;

        private PublishPackageBuilder(final Site site, final String target, final boolean requestApproval, final String comment) {
            this.site = site;
            this.target = target;
            this.requestApproval = requestApproval;
            this.comment = comment;
            lockKey = org.craftercms.studio.api.v2.utils.StudioUtils.getPullOrSubmitPublishingLockKey(site.getSiteId());
        }

        /**
         * Create a new publish package and populate it with the necessary information.
         *
         * @return the newly created publish package
         * @throws AuthenticationException if unable to find the current user
         */
        protected PublishPackage createPackage() throws AuthenticationException {
            PublishPackage publishPackage = new PublishPackage();
            publishPackage.setPackageType(getPackageType());
            publishPackage.setSiteId(site.getId());
            publishPackage.setTarget(target);
            publishPackage.setComment(comment);
            publishPackage.setSubmitterId(userServiceInternal.getCurrentUser().getId());
            publishPackage.setCommitId(site.getLastCommitId());
            publishPackage.setApprovalState(requestApproval ? SUBMITTED : APPROVED);
            return publishPackage;
        }

        /**
         * Get the package type for this builder.
         */
        protected abstract PackageType getPackageType();

        /**
         * Get items to be published.
         */
        @NonNull
        protected Collection<PublishItem> getItems() throws ServiceLayerException, IOException {
            return emptyList();
        }

        /**
         * Template method to build a publish package and its items.
         *
         * @return the id of the created package
         */
        public long build()
                throws ServiceLayerException, AuthenticationException {
            acquireLock();

            try {
                // Combine list of paths and list of commit changes
                Collection<PublishItem> publishItems = getItems();

                // Create package
                PublishPackage publishPackage = createPackage();
                retryingDatabaseOperationFacade.retry(() -> publishDao.insertPackageAndItems(publishPackage, publishItems));
                auditPublishSubmission(publishPackage, requestApproval ? OPERATION_REQUEST_PUBLISH : OPERATION_PUBLISH);

                applicationContext.publishEvent(new WorkflowEvent(site.getSiteId(), publishPackage.getId(), requestApproval ? SUBMIT : APPROVE));
                if (!requestApproval) {
                    notifyPublisher(publishPackage);
                }
                return publishPackage.getId();
            } catch (IOException e) {
                logger.error("Failed to submit publish package: ", e);
                throw new ServiceLayerException("Failed to submit publish package", e);
            } finally {
                generalLockService.unlock(lockKey);
            }
        }

        /**
         * Notify (if needed) the publisher to process the publish package.
         *
         * @param publishPackage the newly created publish package
         */
        protected void notifyPublisher(final PublishPackage publishPackage) {
            applicationContext.publishEvent(new RequestPublishEvent(site.getSiteId(), publishPackage.getId()));
        }

        /**
         * Acquire the lock to prevent concurrent publish requests, or throw an exception if the lock cannot be acquired.
         *
         * @throws ServiceLayerException if the lock cannot be acquired
         */
        private void acquireLock() throws ServiceLayerException {
            boolean lockAcquired = generalLockService.tryLock(lockKey);
            if (!lockAcquired) {
                throw new LockedRepositoryException("Failed to submit publish all request: The repository is already locked for publishing or processing.");
            }
        }
    }

    /**
     * Publish all package builder.
     * {@link PublishPackageBuilder} implementation that will create {@link PublishItem}s for each unpublished
     * path in the site without caring about dependencies.
     */
    private class PublishAllPackageBuilder extends PublishPackageBuilder {

        public PublishAllPackageBuilder(Site site, String publishingTarget, boolean requestApproval, String comment) throws SiteNotFoundException {
            super(site, publishingTarget, requestApproval, comment);
        }

        @Override
        protected PackageType getPackageType() {
            return PUBLISH_ALL;
        }

        @NotNull
        @Override
        protected Collection<PublishItem> getItems() {
            return itemServiceInternal.getUnpublishedPaths(site.getId()).stream()
                    .map(path -> createPublishItem(site, path, ADD, true))
                    .toList();
        }
    }

    /**
     * Initial publish package builder.
     */
    private class InitialPublishPackageBuilder extends PublishPackageBuilder {
        public InitialPublishPackageBuilder(Site site, String publishingTarget, boolean requestApproval, String comment) throws SiteNotFoundException {
            super(site, publishingTarget, requestApproval, comment);
        }

        @Override
        protected PackageType getPackageType() {
            return INITIAL_PUBLISH;
        }
    }

    /**
     * Item list publish package builder.
     * This builder will create {@link PublishItem}s for each path in the list of {@link PublishRequestPath}s and
     * commit ids, including their hard dependencies.
     */
    private class ItemListPublishPackageBuilder extends PublishPackageBuilder {
        private final Collection<PublishRequestPath> paths;
        private final Collection<String> commitIds;
        private final Instant schedule;

        public ItemListPublishPackageBuilder(Site site, String target, List<PublishRequestPath> paths,
                                             List<String> commitIds, Instant schedule,
                                             String comment, boolean requestApproval) throws SiteNotFoundException {
            super(site, target,requestApproval, comment);
            this.paths = paths;
            this.commitIds = commitIds;
            this.schedule = schedule;
        }

        @Override
        protected PublishPackage createPackage() throws AuthenticationException {
            PublishPackage publishPackage = super.createPackage();
            publishPackage.setSchedule(schedule);
            return publishPackage;
        }

        @Override
        protected PackageType getPackageType() {
            return ITEM_LIST;
        }

        @NotNull
        @Override
        protected Collection<PublishItem> getItems() throws ServiceLayerException, IOException {
            // Combine list of paths and list of commit changes
            Map<String, PublishItem> publishItemsByPath = new HashMap<>();
            createPublishItemsFromCommitIds(site, commitIds, publishItemsByPath);
            createPublishItemsFromPaths(site, paths, publishItemsByPath);
            createPublishItemsForHardDeps(site, publishItemsByPath);

            if (publishItemsByPath.isEmpty()) {
                throw new InvalidParametersException("Failed to submit publish package: No items to publish");
            }
            return publishItemsByPath.values();
        }

        @Override
        protected void notifyPublisher(PublishPackage publishPackage) {
            if (schedule == null) {
                super.notifyPublisher(publishPackage);
            }
        }
    }
}

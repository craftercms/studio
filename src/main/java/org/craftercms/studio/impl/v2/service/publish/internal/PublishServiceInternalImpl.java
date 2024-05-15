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

import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.repository.LockedRepositoryException;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
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

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.tika.io.FilenameUtils.getName;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.ADD;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.DELETE;
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
    private DependencyServiceInternal dependencyServiceInternal;
    private PublishDAO publishDao;

    private SitesService siteService;
    private GeneralLockService generalLockService;

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

//    @Override
    public void initialPublish(String siteId) throws SiteNotFoundException {
        contentRepository.initialPublish(siteId);
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
    public long publishAll(final String siteId, final String publishingTarget, final boolean requestApproval, final boolean notifySubmitter,final  String comment)
            throws ServiceLayerException, AuthenticationException {
        String lockKey = org.craftercms.studio.api.v2.utils.StudioUtils.getPublishingOrProcessingLockKey(siteId);
        boolean lockAcquired = generalLockService.tryLock(lockKey);
        if (!lockAcquired) {
            throw new LockedRepositoryException("Failed to submit publish all request: The repository is already locked for publishing or processing.");
        }
        try {
            Site site = siteService.getSite(siteId);
            long submitterId = userServiceInternal.getCurrentUser().getId();
            PublishPackage publishPackage = PublishPackage.publishAll()
                    .withSiteId(site.getId())
                    .withTarget(publishingTarget)
                    .withComment(comment)
                    .withSubmitterId(submitterId)
                    .withRequestApproval(requestApproval)
                    .withNotifySubmitter(notifySubmitter)
                    .withCommitId(contentRepository.getRepoLastCommitId(siteId))
                    .build();
            retryingDatabaseOperationFacade.retry(() -> publishDao.insertPackage(publishPackage));
            auditPublishAll(publishPackage, requestApproval);
            applicationContext.publishEvent(new RequestPublishEvent(siteId));
            return publishPackage.getId();
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    /**
     * Audit publish_all operation
     *
     * @param p               the publish package
     * @param requestApproval if approval is requested for the package
     */
    private void auditPublishAll(final PublishPackage p, final boolean requestApproval) {
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(requestApproval ? OPERATION_REQUEST_PUBLISH_ALL : OPERATION_PUBLISH_ALL);
        auditLog.setActorId(String.valueOf(p.getSubmitterId()));
        auditLog.setSiteId(p.getSiteId());
        auditLog.setPrimaryTargetId(String.valueOf(p.getSiteId()));
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(String.valueOf(p.getSiteId()));

        AuditLogParameter commentParam = new AuditLogParameter();
        commentParam.setTargetId(TARGET_TYPE_SUBMISSION_COMMENT);
        commentParam.setTargetType(TARGET_TYPE_SUBMISSION_COMMENT);
        commentParam.setTargetValue(p.getComment());

        AuditLogParameter packageParam = new AuditLogParameter();
        packageParam.setTargetId(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetType(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetValue(String.valueOf(p.getId()));

        auditLog.setParameters(List.of(commentParam, packageParam));
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void createPublishItemsFromCommitIds(final String siteId, final String publishingTarget,
                                                 final List<String> commitIds, final Map<String, PublishItem> publishItemsByPath)
            throws ServiceLayerException, IOException {
        if (isEmpty(commitIds)) {
            return;
        }
        // Validate and sort commits
        List<String> sortedCommits = contentRepository.validatePublishCommits(siteId, commitIds);
        publishItemsByPath.putAll(
                sortedCommits.stream()
                        .map(commitId -> contentRepository.getOperationsFromDelta(siteId, commitId, commitId + "~1"))
                        .flatMap(List::stream)
                        .filter(op -> !contains(IGNORE_FILES, getName(op.getMoveToPath())))
                        .filter(op -> !contains(IGNORE_FILES, getName(op.getPath())))
                        // Ignore deletes if the path exists in current version
                        .filter(op -> op.getAction() != RepoOperation.Action.DELETE || contentRepository.contentExists(siteId, op.getPath()))
                        .map(op -> createPublishItem(siteId, publishingTarget, op.getPath(),
                                op.getAction() == RepoOperation.Action.DELETE ? DELETE : ADD, true))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }

    private PublishItem createPublishItem(final String siteId, final String publishingTarget,
                                          final String path, final PublishItem.Action action, final boolean userRequested) {
        PublishItem publishItem = new PublishItem();
        if (action != DELETE) {
            // TODO: retrieve item and check if there is an old path for the target ??
        }
        publishItem.setAction(action);
        publishItem.setPath(path);
        publishItem.setUserRequested(userRequested);
        return publishItem;
    }

    private void createPublishItemsFromPaths(final String siteId, final String publishingTarget,
                                             final List<PublishRequestPath> publishRequestPaths, final Map<String, PublishItem> publishItemsByPath)
            throws ServiceLayerException {
        if (isEmpty(publishRequestPaths)) {
            return;
        }

        Set<String> allPaths = new HashSet<>();
        Set<String> softDepsPaths = new HashSet<>();
        for (PublishRequestPath publishRequestPath : publishRequestPaths) {
            allPaths.addAll(expandPublishRequestPath(siteId, publishRequestPath, publishingTarget));
            if (publishRequestPath.includeSoftDeps()) {
                softDepsPaths.add(publishRequestPath.path());
            }
        }
        allPaths.addAll(dependencyServiceInternal.getSoftDependencies(siteId, softDepsPaths));

        publishItemsByPath.putAll(
                allPaths.stream()
                        .filter(path -> !publishItemsByPath.containsKey(path))
                        .map(path -> createPublishItem(siteId, publishingTarget, path, ADD, true))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }

    private void createPublishItemsForHardDeps(String siteId, String publishingTarget, Map<String, PublishItem> publishItemsByPath) throws ServiceLayerException {
        publishItemsByPath.putAll(
                dependencyServiceInternal.getHardDependencies(siteId, publishItemsByPath.keySet()).stream()
                        .filter(dep -> !publishItemsByPath.containsKey(dep))
                        .map(dep -> createPublishItem(siteId, publishingTarget, dep, ADD, false))
                        .collect(toMap(PublishItem::getPath, item -> item)));
    }

    private Set<String> expandPublishRequestPath(final String siteId, final PublishRequestPath publishPath, final String publishingTarget)
            throws ServiceLayerException {
        Set<String> paths = new HashSet<>();
        // TODO: replace this with a new method that retrieves systemType only
        Item item = itemServiceInternal.getItem(siteId, publishPath.path(), true);
        if (item == null) {
            throw new ContentNotFoundException("Content not found for path: " + publishPath.path());
        }
        if (!CONTENT_TYPE_FOLDER.equals(item.getSystemType())) {
            paths.add(publishPath.path());
        }
        if (publishPath.includeChildren()) {
            // TODO: Get children if include children flag is true
            // Write db query to recursively get children paths only - include non-published-for-target items only
        }
        return paths;
    }

    @Override
    public long publish(String siteId, String publishingTarget, List<PublishRequestPath> paths, List<String> commitIds, Instant schedule, String comment)
            throws ServiceLayerException, AuthenticationException {
        String lockKey = org.craftercms.studio.api.v2.utils.StudioUtils.getPublishingOrProcessingLockKey(siteId);
        boolean lockAcquired = generalLockService.tryLock(lockKey);
        if (!lockAcquired) {
            throw new LockedRepositoryException("Failed to submit publish all request: The repository is already locked for publishing or processing.");
        }
        try {
            Site site = siteService.getSite(siteId);
            // TODO: implement new publishing system
            // TODO: make this a db transaction

            // Combine list of paths and list of commit changes
            Map<String, PublishItem> publishItemsByPath = new HashMap<>();
            createPublishItemsFromCommitIds(siteId, publishingTarget, commitIds, publishItemsByPath);
            createPublishItemsFromPaths(siteId, publishingTarget, paths, publishItemsByPath);
            createPublishItemsForHardDeps(siteId, publishingTarget, publishItemsByPath);

            if (publishItemsByPath.isEmpty()) {
                throw new InvalidParametersException("Failed to submit publish package: No items to publish");
            }

            // Create package
            PublishPackage publishPackage = PublishPackage.builder()
                    .withSiteId(site.getId())
                    .withTarget(publishingTarget)
                    .withComment(comment)
                    .withSubmitterId(userServiceInternal.getCurrentUser().getId())
                    .withCommitId(contentRepository.getRepoLastCommitId(siteId))
                    .withSchedule(schedule)
                    .build();
            retryingDatabaseOperationFacade.retry(() -> publishDao.insertPackage(publishPackage));
            retryingDatabaseOperationFacade.retry(() -> publishDao.insertItems(publishPackage.getId(), publishItemsByPath.values()));

            // Create and insert publish items
            applicationContext.publishEvent(new RequestPublishEvent(siteId));
            return publishPackage.getId();
        } catch (IOException e) {
            logger.error("Failed to publish items: ", e);
            throw new ServiceLayerException("Failed to publish items", e);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public long requestPublish(String siteId, String publishingTarget, List<PublishRequestPath> paths,
                               List<String> commitIds, Instant schedule, String comment, boolean notifySubmitter) {
        // TODO: implement new publishing system
        return 0;
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

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }
}

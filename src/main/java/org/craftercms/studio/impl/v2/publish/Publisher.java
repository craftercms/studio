/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.publish;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.dal.publish.ItemTargetDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState;
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.GitContentRepository.GitPublishChangeSet;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v2.utils.PublishUtils;
import org.craftercms.studio.impl.v2.utils.db.DBUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.time.Instant.now;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.DELETE;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.*;
import static org.springframework.data.util.Predicates.negate;

/**
 * Listen for {@link RequestPublishEvent} and handle accordingly.
 */
public class Publisher implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    // Format for transaction name: PUBLISH-{siteId}-{packageId}-{target}
    private static final String PUBLISH_TRANSACTION_NAME_FORMAT = "PUBLISH-%s-%d-%s";

    private final SiteDAO siteDao;
    private final PublishDAO publishDao;
    private ApplicationEventPublisher eventPublisher;
    private final ItemServiceInternal itemServiceInternal;
    private final AuditServiceInternal auditServiceInternal;
    private final GitContentRepository contentRepository;
    private final GeneralLockService generalLockService;
    private final ServicesConfig servicesConfig;
    private final ItemTargetDAO itemTargetDAO;
    private final PlatformTransactionManager transactionManager;

    @ConstructorProperties({"siteDao", "publishDao", "itemServiceInternal", "auditServiceInternal",
            "contentRepository", "generalLockService", "servicesConfig", "itemTargetDAO", "transactionManager"})
    public Publisher(final SiteDAO siteDao, final PublishDAO publishDao,
                     final ItemServiceInternal itemServiceInternal,
                     final AuditServiceInternal auditServiceInternal,
                     final GitContentRepository contentRepository,
                     final GeneralLockService generalLockService,
                     final ServicesConfig servicesConfig,
                     final ItemTargetDAO itemTargetDAO,
                     final PlatformTransactionManager transactionManager) {
        this.siteDao = siteDao;
        this.publishDao = publishDao;
        this.itemServiceInternal = itemServiceInternal;
        this.auditServiceInternal = auditServiceInternal;
        this.contentRepository = contentRepository;
        this.generalLockService = generalLockService;
        this.servicesConfig = servicesConfig;
        this.itemTargetDAO = itemTargetDAO;
        this.transactionManager = transactionManager;
    }

    @Async
    @EventListener
    @LogExecutionTime
    public void handleRequestPublishEvent(final RequestPublishEvent event) throws ServiceLayerException {
        long packageId = event.getPackageId();
        String siteId = event.getSiteId();
        logger.debug("Received request to publish package '{}' for site: '{}'", packageId, siteId);
        Site site = siteDao.getSite(siteId);
        if (!site.getPublishingEnabled()) {
            logger.warn("Site '{}' is not enabled for publishing. Ignoring request to publish package '{}'", siteId, packageId);
            return;
        }
        if (!Site.State.READY.equals(site.getState())) {
            logger.warn("Site '{}' is not in a ready state. Ignoring request to publish package '{}'", siteId, packageId);
            return;
        }
        String lockKey = StudioUtils.getPublishingLockKey(siteId);
        boolean lockAcquired = generalLockService.tryLock(lockKey);
        if (!lockAcquired) {
            logger.warn("Failed to acquire lock for publishing package '{}' for site '{}'", packageId, siteId);
            return;
        }

        try {
            PublishPackage publishPackage = publishDao.getById(packageId);
            doPublish(publishPackage);
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    /*
     * Process a publish package
     */
    private void doPublish(final PublishPackage publishPackage) throws ServiceLayerException {
        long packageId = publishPackage.getId();
        String siteId = publishPackage.getSite().getSiteId();
        publishDao.updatePackageState(packageId, PROCESSING.value, READY.value);
        publishDao.updatePublishItemState(packageId, PublishItem.PublishState.PROCESSING.value, PublishItem.PublishState.PENDING.value);
        try {
            Collection<PublishItem> publishItems = publishDao.getPublishItems(packageId);
            // Set all affected items to system processing
            publishDao.updateItemStateBits(packageId, SYSTEM_PROCESSING.value, 0);
            auditPublishOperation(publishPackage, OPERATION_PUBLISH_START);

            // TODO: Initiate package progress reporting
            switch (publishPackage.getPackageType()) {
                case INITIAL_PUBLISH -> {
                    logger.debug("Processing initial publish package '{}' for site '{}'", packageId, siteId);
                    doInitialPublish(publishPackage);
                }
                case PUBLISH_ALL -> {
                    logger.debug("Processing publish-all package '{}' for site '{}'", packageId, siteId);
                    doPublishItemList(publishPackage, publishItems, runInTransaction(this::doPublishAllTarget));
                }
                case ITEM_LIST -> {
                    logger.debug("Processing publish package '{}' for site '{}'", packageId, siteId);
                    doPublishItemList(publishPackage, publishItems, runInTransaction(this::doPublishItemListTarget));
                }
                default ->
                        throw new ServiceLayerException(format("Unknown package type '%s' for package '%d' for site '%s'",
                                publishPackage.getPackageType(), packageId, siteId));
            }
            auditPublishOperation(publishPackage, OPERATION_PUBLISHED);
            eventPublisher.publishEvent(new PublishEvent(siteId));
            // TODO: Complete package progress
        } catch (Exception e) {
            logger.error("Failed to publish package '{}' for site '{}'", packageId, siteId, e);
            int errorCode = PublishUtils.translatePackageException(e);
            publishDao.updateFailedPackage(packageId, PackageState.LIVE_FAILED.value + STAGING_FAILED.value, 0, errorCode, errorCode);
            String exceptionMessage = format("Failed to publish package '%d' for site '%s'", packageId, siteId);
            throw new ServiceLayerException(exceptionMessage, e);
        } finally {
            publishPackage.setPublishedOn(now());
            publishDao.updatePackage(publishPackage);
            publishDao.updatePackageState(packageId, 0, PROCESSING.value);
            // Clear system processing bit for all affected items
            publishDao.updateItemStateBits(packageId, 0, SYSTEM_PROCESSING.value);
            publishDao.updatePublishItemState(packageId, 0, PublishItem.PublishState.PROCESSING.value);
        }
    }

    /**
     * Process a package that contains a list of items to publish (i.e. a package with type equal to either PUBLISH_ALL or ITEM_LIST)
     */
    private void doPublishItemList(final PublishPackage publishPackage,
                                   final Collection<PublishItem> publishItems,
                                   final TargetPublisherFunction targetPublisher) {
        String siteId = publishPackage.getSite().getSiteId();
        String target = publishPackage.getTarget();

        boolean isLiveTarget = StringUtils.equals(servicesConfig.getLiveEnvironment(siteId), target);

        if (isLiveTarget && servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            String stagingEnvironment = servicesConfig.getStagingEnvironment(siteId);
            runTargetPublisher(publishPackage, publishItems, targetPublisher, false, stagingEnvironment);
        }
        runTargetPublisher(publishPackage, publishItems, targetPublisher, isLiveTarget, target);
    }

    /**
     * Helper function to run the "target publisher" and handle exception.
     * On exception, it will update the package and its PublishItems with the target-aware failing state
     */
    private void runTargetPublisher(final PublishPackage publishPackage,
                                    final Collection<PublishItem> publishItems,
                                    final TargetPublisherFunction targetPublisher,
                                    final boolean isLiveTarget,
                                    final String target) {
        PublishPackageTO packageTO = getPublishPackageTO(publishPackage, isLiveTarget);
        try {
            targetPublisher.run(packageTO, target, publishItems);
        } catch (Exception e) {
            logger.error("Failed to publish package '{}' to target '{}' for site '{}'", publishPackage.getId(), target, publishPackage.getSite().getSiteId(), e);
            int errorCode = PublishUtils.translatePackageException(e);
            int liveErrorCode = isLiveTarget ? errorCode : 0;
            int stagingErrorCode = isLiveTarget ? 0 : errorCode;
            publishDao.updateFailedPackage(publishPackage.getId(), packageTO.getFailedOnBits(),
                    0, stagingErrorCode, liveErrorCode);
            long onMask = isLiveTarget ? PublishItem.PublishState.LIVE_FAILED.value : PublishItem.PublishState.STAGING_FAILED.value;
            publishDao.updatePublishItemState(publishPackage.getId(), onMask, 0);
            itemServiceInternal.updateForCompletePackage(packageTO.getId(),
                    packageTO.getItemSuccessOnMask(),
                    packageTO.getItemSuccessOffMask(),
                    0,
                    packageTO.getItemFailureOffMask(),
                    packageTO.getItemSuccessState());
        }
    }

    private PublishPackageTO getPublishPackageTO(final PublishPackage publishPackage, final boolean isLiveTarget) {
        return new PublishPackageTO(publishPackage, isLiveTarget);
    }

    /**
     * Convenience method to call doPublishTarget using ContentRepository publish function
     *
     * @param publishPackage the package to publish
     * @param target         the target to publish to
     * @param publishItems   the list of items to publish
     */
    @NonNull
    private void doPublishItemListTarget(final PublishPackageTO publishPackage,
                                         final String target, final Collection<PublishItem> publishItems) throws ServiceLayerException, IOException {
        doPublishTarget(publishPackage, target, publishItems, contentRepository::publish);
    }

    private List<PublishItemTOImpl> expandPublishItem(final PublishItem pi, final String target, final boolean isLiveTarget) {
        List<PublishItemTOImpl> items = new ArrayList<>();
        items.add(new PublishItemTOImpl(pi, pi.getPath(), pi.getAction(), isLiveTarget));
        String previousPath = pi.getPreviousPath(target);
        if (previousPath != null) {
            items.add(new PublishItemTOImpl(pi, previousPath, DELETE, isLiveTarget));
        }
        return items;
    }

    /**
     * Process a list of items to publish for a specific target
     * Notice that for packages with target 'live', this method should
     * be called twice, once for the staging target and once for the live target
     */
    @NonNull
    private void doPublishTarget(final PublishPackageTO packageTO,
                                 final String target,
                                 final Collection<PublishItem> publishItems,
                                 final RepoPublishFunction repoPublishFunction) throws ServiceLayerException, IOException {
        String siteId = packageTO.getSite().getSiteId();
        long packageId = packageTO.getId();

        String liveTarget = servicesConfig.getLiveEnvironment(siteId);
        boolean isLiveTarget = StringUtils.equals(liveTarget, target);
        if (!isLiveTarget && !contentRepository.isTargetPublished(siteId, target)) {
            itemTargetDAO.initStaging(packageTO.getSite().getId(), target, liveTarget);
        }

        List<PublishItemTOImpl> publishItemTOs = publishItems.stream()
                .peek(pi->logger.debug("Processing publish item '{}' for package '{}' to target '{}', site '{}'", pi.getPath(), packageId, target, siteId))
                .map(pi -> expandPublishItem(pi, target, isLiveTarget))
                .flatMap(List::stream)
                .toList();

        GitPublishChangeSet<PublishItemTOImpl> publishChangeSet = repoPublishFunction.run(packageTO.getPackage(), target, publishItemTOs);

        Set<PublishItem> failedItems = publishChangeSet.failedItems().stream()
                .peek(pi -> logger.error("Failed to publish item '{}' for package '{}' to target '{}', site '{}'", pi.getPath(), packageId, target, siteId))
                .map(PublishItemTOImpl::getPublishItem)
                .collect(Collectors.toSet());

        Collection<PublishItem> successfulItems = publishChangeSet.successfulItems().stream()
                .peek(pi -> logger.debug("Successfully published item '{}' for package '{}' to target '{}', site '{}'", pi.getPath(), packageId, target, siteId))
                .map(PublishItemTOImpl::getPublishItem)
                .filter(negate(failedItems::contains))
                .toList();

        if (failedItems.isEmpty()) {
            publishDao.updatePublishItemState(packageId, packageTO.getItemSuccessState(), 0);
            if (packageTO.getPackageType() == PublishPackage.PackageType.PUBLISH_ALL) {
                cancelOutstandingTargetPackages(packageTO.getSite().getId(), target);
            }
        } else {
            publishDao.updatePublishItemListState(union(successfulItems, failedItems));
        }
        itemServiceInternal.updateForCompletePackage(packageId,
                packageTO.getItemSuccessOnMask(),
                packageTO.getItemSuccessOffMask(),
                0,
                packageTO.getItemFailureOffMask(),
                packageTO.getItemSuccessState());
        long packageStateOnBits;
        if (publishChangeSet.completed()) {
            itemTargetDAO.updateForCompletePackage(packageId, publishChangeSet.commitId(), target, packageTO.getItemSuccessState());
            packageTO.setPublishedCommitId(publishChangeSet.commitId());
            publishDao.updatePackage(packageTO.getPackage());
            if (publishChangeSet.hasFailedItems()) {
                packageStateOnBits = packageTO.getCompletedWithErrorsOnBits();
            } else {
                packageStateOnBits = packageTO.getSuccessOnBits();
            }
        } else {
            packageStateOnBits = packageTO.getFailedOnBits();
        }
        publishDao.updatePackageState(packageId, packageStateOnBits, 0);

        if (publishChangeSet.completed()) {
            contentRepository.updateRef(siteId, packageId, publishChangeSet.commitId(), target);
        }
    }

    /**
     * Convenience method to call doPublishTarget using ContentRepository publishAll function
     *
     * @param publishPackage the package to publish
     * @param target         the target to publish to
     * @param publishItems   the list of items to publish
     */
    @NonNull
    private void doPublishAllTarget(final PublishPackageTO publishPackage,
                                    final String target,
                                    final Collection<PublishItem> publishItems) throws ServiceLayerException, IOException {
        doPublishTarget(publishPackage, target, publishItems, contentRepository::publishAll);
    }

    private TargetPublisherFunction runInTransaction(TargetPublisherFunction publisher) {
        return (publishPackage,
                publishingTarget,
                publishItems) ->
                DBUtils.runInTransaction(transactionManager,
                        format(PUBLISH_TRANSACTION_NAME_FORMAT, publishPackage.getSite().getSiteId(), publishPackage.getId(), publishingTarget),
                        () -> publisher.run(publishPackage, publishingTarget, publishItems));
    }

    /**
     * Convenience functional interface for a method that publishes a package to a target
     */
    @FunctionalInterface
    private interface TargetPublisherFunction {
        void run(final PublishPackageTO publishPackage,
                 final String target,
                 final Collection<PublishItem> publishItems)
                throws Exception;
    }

    /**
     * Convenience functional interface for a {@link ContentRepository} publish method
     */
    @FunctionalInterface
    private interface RepoPublishFunction {
        GitPublishChangeSet<PublishItemTOImpl> run(PublishPackage publishPackage,
                                                   String publishingTarget,
                                                   Collection<PublishItemTOImpl> publishItems) throws ServiceLayerException, IOException;
    }

    /**
     * Process an initial publish package
     */
    private void doInitialPublish(final PublishPackage publishPackage) throws Exception {
        String siteId = publishPackage.getSite().getSiteId();
        String commitId = contentRepository.initialPublish(siteId);
        boolean stagingEnabled = servicesConfig.isStagingEnvironmentEnabled(siteId);
        Instant now = now();
        DBUtils.runInTransaction(transactionManager,
                format(PUBLISH_TRANSACTION_NAME_FORMAT, publishPackage.getSite().getSiteId(), publishPackage.getId(), OPERATION_INITIAL_PUBLISH), () -> {
                    cancelAllOutstandingPackages(publishPackage.getSiteId());
                    itemServiceInternal.updateStatesForSite(siteId, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK, PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);

                    Collection<String> targets = new ArrayList<>();
                    targets.add(servicesConfig.getLiveEnvironment(siteId));
                    if (stagingEnabled) {
                        targets.add(servicesConfig.getStagingEnvironment(siteId));
                    }
                    itemTargetDAO.insertForInitialPublish(publishPackage.getSite().getId(), targets, commitId, now);

                    publishPackage.setPublishedLiveCommitId(commitId);
                    publishPackage.setPublishedStagingCommitId(commitId);
                    publishPackage.setPublishedOn(now);
                    publishDao.updatePackageState(publishPackage.getId(), PackageState.LIVE_SUCCESS.value + PackageState.STAGING_SUCCESS.value, 0);
                    publishDao.updatePackage(publishPackage);
                });
    }

    private void cancelAllOutstandingPackages(final long siteId) {
        try {
            publishDao.cancelAllOutstandingPackages(siteId);
        } catch (Exception e) {
            logger.error("Failed to cancel outstanding packages for site '{}'", siteId, e);
        }
    }

    private void cancelOutstandingTargetPackages(final long siteId, final String target) {
        try {
            publishDao.cancelOutstandingPackages(siteId, target);
        } catch (Exception e) {
            logger.error("Failed to cancel outstanding packages for site '{}', target '{}'", siteId, target, e);
        }
    }

    /**
     * Audit a publish operation
     *
     * @param p         the publish package
     * @param operation the operation
     */
    private void auditPublishOperation(final PublishPackage p, final String operation) {
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setActorId(String.valueOf(p.getSubmitterId()));
        auditLog.setSiteId(p.getSiteId());
        auditLog.setPrimaryTargetId(String.valueOf(p.getSiteId()));
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(String.valueOf(p.getSiteId()));

        AuditLogParameter packageParam = new AuditLogParameter();
        packageParam.setTargetId(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetType(TARGET_TYPE_PUBLISHING_PACKAGE);
        packageParam.setTargetValue(String.valueOf(p.getId()));

        auditLog.setParameters(List.of(packageParam));
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}

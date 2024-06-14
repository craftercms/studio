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
import org.craftercms.studio.api.v2.event.publish.PublishEvent;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.GitContentRepository.GitPublishChangeSet;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;

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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.union;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.Action.DELETE;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.State.FAILED;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.State.PUBLISHED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.*;

/**
 * Listen for {@link RequestPublishEvent} and handle accordingly.
 */
public class Publisher implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    private final SiteDAO siteDao;
    private final PublishDAO publishDao;
    private ApplicationEventPublisher eventPublisher;
    private final ItemServiceInternal itemServiceInternal;
    private final AuditServiceInternal auditServiceInternal;
    private final GitContentRepository contentRepository;
    private final GeneralLockService generalLockService;
    private final ServicesConfig servicesConfig;
    private final ItemTargetDAO itemTargetDAO;

    @ConstructorProperties({"siteDao", "publishDao", "itemServiceInternal", "auditServiceInternal",
            "contentRepository", "generalLockService", "servicesConfig", "itemTargetDAO"})
    public Publisher(final SiteDAO siteDao, final PublishDAO publishDao,
                     final ItemServiceInternal itemServiceInternal,
                     final AuditServiceInternal auditServiceInternal,
                     final GitContentRepository contentRepository,
                     final GeneralLockService generalLockService,
                     final ServicesConfig servicesConfig,
                     final ItemTargetDAO itemTargetDAO) {
        this.siteDao = siteDao;
        this.publishDao = publishDao;
        this.itemServiceInternal = itemServiceInternal;
        this.auditServiceInternal = auditServiceInternal;
        this.contentRepository = contentRepository;
        this.generalLockService = generalLockService;
        this.servicesConfig = servicesConfig;
        this.itemTargetDAO = itemTargetDAO;
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
        publishDao.updatePackageState(packageId, PROCESSING);
        List<Long> affectedItemIds = null;
        try {
            Collection<PublishItem> publishItems = publishDao.getPublishItems(packageId);
            affectedItemIds = publishItems.stream().map(PublishItem::getItemId).toList();
            // Set all affected items to system processing
            itemServiceInternal.updateStateBitsByIds(affectedItemIds, SYSTEM_PROCESSING.value, 0);

            // TODO: Make transactional
            // Begin transaction
            auditPublishOperation(publishPackage, OPERATION_PUBLISH_START);

            // TODO: Initiate package progress reporting
            switch (publishPackage.getPackageType()) {
                case INITIAL_PUBLISH -> {
                    logger.debug("Processing initial publish package '{}' for site '{}'", packageId, siteId);
                    doInitialPublish(publishPackage);
                }
                case PUBLISH_ALL -> {
                    logger.debug("Processing publish-all package '{}' for site '{}'", packageId, siteId);
                    doPublishItemList(publishPackage, publishItems, this::doPublishAllTarget);
                }
                case ITEM_LIST -> {
                    logger.debug("Processing publish package '{}' for site '{}'", packageId, siteId);
                    doPublishItemList(publishPackage, publishItems, this::doPublishItemListTarget);
                }
                default ->
                        throw new ServiceLayerException(format("Unknown package type '%s' for package '%d' for site '%s'",
                                publishPackage.getPackageType(), packageId, siteId));
            }
            // Clear system processing bit for all affected items
            itemServiceInternal.updateStateBitsByIds(affectedItemIds, 0, SYSTEM_PROCESSING.value);
            // Commit transaction
            auditPublishOperation(publishPackage, OPERATION_PUBLISHED);
            eventPublisher.publishEvent(new PublishEvent(siteId));
            // TODO: Complete package progress
        } catch (Exception e) {
            logger.error("Failed to publish package '{}' for site '{}'", packageId, siteId, e);
            publishDao.updateFailedPackage(packageId, PublishPackage.PackageState.FAILED, e.getMessage());
            String exceptionMessage = format("Failed to publish package '%d' for site '%s'", packageId, siteId);
            throw new ServiceLayerException(exceptionMessage, e);
        } finally {
            if (affectedItemIds != null) {
                // Clear system processing bit for all affected items
                itemServiceInternal.updateStateBitsByIds(affectedItemIds, 0, SYSTEM_PROCESSING.value);
            }
        }
    }

    /**
     * Process a package that contains a list of items to publish (i.e. a package with type equal to either PUBLISH_ALL or ITEM_LIST)
     */
    private void doPublishItemList(final PublishPackage publishPackage,
                                   final Collection<PublishItem> publishItems,
                                   final TargetPublisherFunction targetPublisher)
            throws ServiceLayerException, IOException {
        String siteId = publishPackage.getSite().getSiteId();
        String target = publishPackage.getTarget();

        boolean isLiveTarget = StringUtils.equals(servicesConfig.getLiveEnvironment(siteId), target);

        boolean errors = false;
        if (isLiveTarget && servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            GitPublishChangeSet<PublishItemTOImpl> stagingChangeset = targetPublisher.run(publishPackage,
                    servicesConfig.getStagingEnvironment(siteId), publishItems);
            publishPackage.setPublishedStagingCommitId(stagingChangeset.commitId());
            errors |= isNotEmpty(stagingChangeset.failedItems());
        }
        GitPublishChangeSet<PublishItemTOImpl> targetChangeset = targetPublisher.run(publishPackage, target, publishItems);
        errors |= isNotEmpty(targetChangeset.failedItems());

        if (isLiveTarget) {
            publishPackage.setPublishedLiveCommitId(targetChangeset.commitId());
        } else {
            publishPackage.setPublishedStagingCommitId(targetChangeset.commitId());
        }

        Instant now = now();
        // TODO: handle the case where ALL items failed
        publishPackage.setPackageState(errors ? COMPLETED_WITH_ERRORS : COMPLETED);
        publishPackage.setPublishedOn(now);
        publishDao.updatePackage(publishPackage);
    }

    /**
     * Convenience method to call doPublishTarget using ContentRepository publish function
     *
     * @param publishPackage the package to publish
     * @param target         the target to publish to
     * @param publishItems   the list of items to publish
     * @return the change set resulting from the publish operation
     */
    @NonNull
    private GitPublishChangeSet<PublishItemTOImpl> doPublishItemListTarget(final PublishPackage publishPackage,
                                                                        final String target, final Collection<PublishItem> publishItems) throws ServiceLayerException, IOException {
        return doPublishTarget(publishPackage, target, publishItems, contentRepository::publish);
    }

    /**
     * Process a list of items to publish for a specific target
     * Notice that for packages with target 'live', this method should
     * be called twice, once for the staging target and once for the live target
     */
    @NonNull
    private GitPublishChangeSet<PublishItemTOImpl> doPublishTarget(final PublishPackage publishPackage,
                                                                final String target,
                                                                final Collection<PublishItem> publishItems,
                                                                final RepoPublishFunction repoPublishFunction)
            throws ServiceLayerException, IOException {
        String siteId = publishPackage.getSite().getSiteId();
        long packageId = publishPackage.getId();

        boolean isLiveTarget = StringUtils.equals(servicesConfig.getLiveEnvironment(siteId), target);
        boolean isStagingTarget = !isLiveTarget;
        List<PublishItemTOImpl> publishItemTOs = publishItems.stream()
                .map(pi -> {
                    List<PublishItemTOImpl> items = new ArrayList<>();
                    items.add(new PublishItemTOImpl(pi, pi.getPath(), pi.getAction()));
                    if (pi.getLiveOldPath() != null && isLiveTarget) {
                        items.add(new PublishItemTOImpl(pi, pi.getLiveOldPath(), DELETE));
                    } else if (pi.getStagingOldPath() != null && isStagingTarget) {
                        items.add(new PublishItemTOImpl(pi, pi.getStagingOldPath(), DELETE));
                    }
                    return items;
                }).flatMap(List::stream)
                .toList();

        GitPublishChangeSet<PublishItemTOImpl> publishChangeSet = repoPublishFunction.run(publishPackage, target, publishItemTOs);

        Set<PublishItem> failedItems = publishChangeSet.failedItems().stream()
                .map(PublishItemTOImpl::getPublishItem)
                .peek(pi -> pi.setState(FAILED))
                .collect(Collectors.toSet());

        Collection<PublishItem> successfulItems =
                publishItems.stream()
                        .filter(pi -> !failedItems.contains(pi))
                        .peek(pi -> pi.setState(PUBLISHED))
                        .toList();

        long onMask = isLiveTarget ? PUBLISH_TO_STAGE_AND_LIVE_ON_MASK : PUBLISH_TO_STAGE_ON_MASK;
        long offMask = isLiveTarget ? PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK : PUBLISH_TO_STAGE_OFF_MASK;
        if (failedItems.isEmpty()) {
            publishDao.updatePublishItemState(packageId, PUBLISHED);
            if (publishPackage.getPackageType() == PublishPackage.PackageType.PUBLISH_ALL) {
                cancelOutstandingTargetPackages(siteId, target);
            }
        } else {
            publishDao.updatePublishItemListState(union(successfulItems, failedItems));
        }
        if (!successfulItems.isEmpty()) {
            itemServiceInternal.updateForCompletePackage(packageId, onMask, offMask, now());
            itemTargetDAO.updateForCompletePackage(packageId, publishChangeSet.commitId(), target, now());
        }

        contentRepository.updateRef(siteId, packageId, publishChangeSet.commitId(), target);
        return publishChangeSet;
    }

    /**
     * Convenience method to call doPublishTarget using ContentRepository publishAll function
     *
     * @param publishPackage the package to publish
     * @param target         the target to publish to
     * @param publishItems   the list of items to publish
     * @return the change set resulting from the publish operation
     */
    @NonNull
    private GitPublishChangeSet<PublishItemTOImpl> doPublishAllTarget(final PublishPackage publishPackage,
                                                                   final String target, final Collection<PublishItem> publishItems) throws ServiceLayerException, IOException {
        return doPublishTarget(publishPackage, target, publishItems, contentRepository::publishAll);
    }

    /**
     * Convenience functional interface for a method that publishes a package to a target
     */
    @FunctionalInterface
    private interface TargetPublisherFunction {
        GitPublishChangeSet<PublishItemTOImpl> run(final PublishPackage publishPackage,
                                                final String target,
                                                final Collection<PublishItem> publishItems)
                throws ServiceLayerException, IOException;
    }

    /**
     * Convenience functional interface for a {@link ContentRepository} publish method
     */
    @FunctionalInterface
    private interface RepoPublishFunction {
        GitPublishChangeSet<PublishItemTOImpl> run(PublishPackage publishPackage,
                                                String publishingTarget,
                                                Collection<PublishItemTOImpl> publishItems) throws ServiceLayerException;
    }

    /**
     * Process an initial publish package
     */
    private void doInitialPublish(final PublishPackage publishPackage) throws ServiceLayerException {
        String siteId = publishPackage.getSite().getSiteId();
        String commitId = contentRepository.initialPublish(siteId);
        boolean stagingEnabled = servicesConfig.isStagingEnvironmentEnabled(siteId);
        Instant now = now();
        cancelAllOutstandingPackages(siteId);
        itemServiceInternal.updateStatesForSite(siteId, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK, PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
        itemServiceInternal.updateSiteLastPublishedOn(siteId, now);

        Collection<String> targets = new ArrayList<>();
        targets.add(servicesConfig.getLiveEnvironment(siteId));
        if (stagingEnabled) {
            targets.add(servicesConfig.getStagingEnvironment(siteId));
        }
        itemTargetDAO.insertForInitialPublish(publishPackage.getSite().getId(), targets, commitId, now);

        publishPackage.setPublishedLiveCommitId(commitId);
        publishPackage.setPublishedStagingCommitId(commitId);
        publishPackage.setPublishedOn(now);
        publishPackage.setPackageState(COMPLETED);
        publishDao.updatePackage(publishPackage);
    }

    private void cancelAllOutstandingPackages(final String siteId) {
        try {
            publishDao.cancelAllOutstandingPackages(siteId);
        } catch (Exception e) {
            logger.error("Failed to cancel outstanding packages for site '{}'", siteId, e);
        }
    }

    private void cancelOutstandingTargetPackages(final String siteId, final String target) {
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

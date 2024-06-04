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
import org.craftercms.studio.api.v2.repository.ContentRepository.PublishChangeSet;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.State.FAILED;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.State.PUBLISHED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.*;
import static org.springframework.util.CollectionUtils.isEmpty;

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
    private final ContentRepository contentRepository;
    private final GeneralLockService generalLockService;
    private final ServicesConfig servicesConfig;
    private final ItemTargetDAO itemTargetDAO;

    @ConstructorProperties({"siteDao", "publishDao", "itemServiceInternal", "auditServiceInternal",
            "contentRepository", "generalLockService", "servicesConfig", "itemTargetDAO"})
    public Publisher(final SiteDAO siteDao, final PublishDAO publishDao,
                     final ItemServiceInternal itemServiceInternal,
                     final AuditServiceInternal auditServiceInternal,
                     final ContentRepository contentRepository,
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

        Collection<PublishItem> publishItems = publishDao.getPublishItems(packageId);
        List<Long> affectedItemIds = publishItems.stream().map(PublishItem::getItemId).toList();
        // Set all affected items to system processing
        itemServiceInternal.updateStateBitsByIds(affectedItemIds, SYSTEM_PROCESSING.value, 0);

        // TODO: Make transactional
        // Begin transaction
        auditPublishOperation(publishPackage, OPERATION_PUBLISH_START);
        try {
            // TODO: Initiate package progress reporting
            switch (publishPackage.getPackageType()) {
                case INITIAL_PUBLISH -> {
                    logger.debug("Processing initial publish package '{}' for site '{}'", packageId, siteId);
                    doInitialPublish(publishPackage);
                }
                case PUBLISH_ALL -> {
                    logger.debug("Processing publish-all package '{}' for site '{}'", packageId, siteId);
                    doPublishAll(publishPackage, publishItems);
                }
                case ITEM_LIST -> {
                    logger.debug("Processing publish package '{}' for site '{}'", packageId, siteId);
                    doPublishItemList(publishPackage, publishItems);
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
        }
    }

    /**
     * Process a package that contains a list of items to publish (i.e. neither an initial publish nor a publish-all)
     */
    private void doPublishItemList(final PublishPackage publishPackage, Collection<PublishItem> publishItems) {
        // TODO: Implement
    }

    /**
     * Process a publish-all publish package
     */
    private void doPublishAll(final PublishPackage publishPackage, Collection<PublishItem> publishItems) throws ServiceLayerException {
        String commitId = publishPackage.getCommitId();
        String siteId = publishPackage.getSite().getSiteId();

        PublishChangeSet publishChangeSet = contentRepository.publishAll(siteId,
                commitId, publishPackage.getTarget(), publishPackage.getComment());

        publishPackage.setPublishedLiveCommitId(publishChangeSet.liveCommitId());
        publishPackage.setPublishedStagingCommitId(publishChangeSet.stagingCommitId());
        publishPackage.setPackageState(isEmpty(publishChangeSet.failedItems()) ? COMPLETED : COMPLETED_WITH_ERRORS);
        publishDao.updatePackage(publishPackage);

        boolean isLiveTarget = StringUtils.equals(servicesConfig.getLiveEnvironment(siteId), publishPackage.getTarget());
        long onMask = isLiveTarget ? PUBLISH_TO_STAGE_AND_LIVE_ON_MASK : PUBLISH_TO_STAGE_ON_MASK;
        long offMask = isLiveTarget ? PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK : PUBLISH_TO_STAGE_OFF_MASK;

        List<Long> successfulItemIds = publishChangeSet.successfulItems().stream().map(PublishItem::getItemId).toList();
        List<Long> failedItemIds = publishChangeSet.failedItems().stream().map(PublishItem::getItemId).toList();

        if (failedItemIds.isEmpty()) {
            // If nothing failed then we can just update all paths in the site
            itemServiceInternal.updateStatesForSite(siteId, onMask, offMask);
            publishDao.updatePublishItemState(publishPackage.getId(), PUBLISHED);
            cancelOutstandingPackages(publishPackage.getId(), siteId);
            itemTargetDAO.clearForSiteAndTarget(publishPackage.getSiteId(), publishPackage.getTarget());
        } else {
            itemServiceInternal.updateStateBitsByIds(successfulItemIds, onMask, offMask);
            publishDao.updatePublishItemListState(successfulItemIds, PUBLISHED);
            publishDao.updatePublishItemListState(failedItemIds, FAILED);

            itemTargetDAO.clearForItemIds(successfulItemIds);
        }
    }

    /**
     * Process an initial publish package
     */
    private void doInitialPublish(final PublishPackage publishPackage) throws ServiceLayerException {
        String siteId = publishPackage.getSite().getSiteId();
        String commitId = contentRepository.initialPublish(siteId);
        Instant now = Instant.now();
        cancelOutstandingPackages(publishPackage.getId(), siteId);
        itemServiceInternal.updateStatesForSite(siteId, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK, PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
        itemServiceInternal.updateSiteLastPublishedOn(siteId, now);

        publishPackage.setPublishedLiveCommitId(commitId);
        publishPackage.setPublishedStagingCommitId(commitId);
        publishPackage.setPublishedOn(now);
        publishPackage.setPackageState(COMPLETED);
        publishDao.updatePackage(publishPackage);
    }

    private void cancelOutstandingPackages(final long packageId, final String siteId) {
        try {
            publishDao.cancelOutstandingPackages(siteId);
        } catch (Exception e) {
            logger.error("Failed to cancel outstanding packages for site '{}' after initial publish, package '{}'",
                    siteId, packageId, e);
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

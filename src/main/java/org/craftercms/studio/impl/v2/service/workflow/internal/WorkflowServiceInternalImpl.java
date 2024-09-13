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

package org.craftercms.studio.impl.v2.service.workflow.internal;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.exception.publish.InvalidPackageStateException;
import org.craftercms.studio.api.v2.exception.publish.PackageAlreadyApprovedException;
import org.craftercms.studio.api.v2.exception.publish.PublishPackageNotFoundException;
import org.craftercms.studio.api.v2.service.audit.internal.ActivityStreamServiceInternal;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.time.Instant;
import java.util.List;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.APPROVED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.ApprovalState.REJECTED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.CANCELLED;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getPublishPackageLockKey;

public class WorkflowServiceInternalImpl implements WorkflowService, ApplicationEventPublisherAware {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowServiceInternalImpl.class);

    private ItemServiceInternal itemServiceInternal;
    private SitesService siteService;
    private GeneralLockService generalLockService;
    private ActivityStreamServiceInternal activityStreamServiceInternal;
    private AuditServiceInternal auditServiceInternal;
    private PublishDAO publishDao;
    private UserServiceInternal userServiceInternal;
    private ServicesConfig servicesConfig;
    private ApplicationEventPublisher eventPublisher;

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemServiceInternal.getItemByStatesTotal(siteId, path, states, null);
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) throws SiteNotFoundException {
        return itemServiceInternal.getItemByStates(siteId, path, states, null, null, offset, limit).stream()
                .map(SandboxItem::getInstance)
                .collect(toList());
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing, boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged, isNew, modified);
    }

    @Override
    public void approvePackage(final String siteId, final long packageId,
                               final Instant schedule, final boolean updateSchedule, final String comment)
            throws AuthenticationException, ServiceLayerException {
        doReviewPackage(siteId, packageId, p -> {
            if (APPROVED == p.getApprovalState()) {
                throw new PackageAlreadyApprovedException(siteId, packageId);
            }
            if (updateSchedule) {
                p.setSchedule(schedule);
            }
            p.setApprovalState(APPROVED);
            p.setReviewerComment(comment);
        }, OPERATION_APPROVE, WorkflowEvent.WorkFlowEventType.APPROVE);
    }

    @Override
    public void cancelPackage(final String siteId, final long packageId, String comment)
            throws ServiceLayerException, AuthenticationException {
        doReviewPackage(siteId, packageId, p -> {
            p.setPackageState(CANCELLED.value);
            p.setReviewerComment(comment);
        }, OPERATION_CANCEL_PUBLISHING_PACKAGE, WorkflowEvent.WorkFlowEventType.CANCEL);
    }

    @Override
    public void rejectPackage(final String siteId, final long packageId, final String comment)
            throws ServiceLayerException, AuthenticationException {
        doReviewPackage(siteId, packageId, p -> {
            p.setApprovalState(REJECTED);
            p.setPackageState(CANCELLED.value);
            p.setReviewerComment(comment);
        }, OPERATION_REJECT_PUBLISHING_PACKAGE, WorkflowEvent.WorkFlowEventType.REJECT);
    }

    /**
     * Update a packageState and/or approvalState of a package
     *
     * @param siteId        the site id
     * @param packageId     the package id
     * @param packageReview the package review operation
     * @param operation     the operation being performed (e.g. cancel, reject, approve)
     * @param eventType     the workflow event type to be triggered if the update is completed
     * @throws ServiceLayerException   if the package is not found or is not in a valid state
     * @throws AuthenticationException if there is an error trying to retrieve the current user
     */
    private void doReviewPackage(final String siteId, final long packageId,
                                 final PackageReview packageReview,
                                 final String operation, final WorkflowEvent.WorkFlowEventType eventType)
            throws ServiceLayerException, AuthenticationException {
        Site site = siteService.getSite(siteId);
        User user = userServiceInternal.getCurrentUser();

        PublishPackage publishPackage = publishDao.getById(site.getId(), packageId);
        if (publishPackage == null) {
            throw new PublishPackageNotFoundException(siteId, packageId);
        }

        String packageLockKey = getPublishPackageLockKey(packageId);
        generalLockService.lock(packageLockKey);
        try {
            publishPackage = publishDao.getById(site.getId(), packageId);
            if (publishPackage.getPackageState() != PublishPackage.PackageState.READY.value) {
                throw new InvalidPackageStateException("Unable to review package because it is not in READY state", siteId, packageId);
            }

            packageReview.reviewPackage(publishPackage);
            publishPackage.setReviewedOn(now());
            publishPackage.setReviewerId(user.getId());
            publishDao.cancelPackage(publishPackage, servicesConfig.getLiveEnvironment(siteId));

            createUpdateStatePackageAuditLogEntry(publishPackage, user.getUsername(), operation);

            activityStreamServiceInternal.insertActivity(site.getId(), user.getId(),
                    operation, DateUtils.getCurrentTime(), null, String.valueOf(packageId));
            eventPublisher.publishEvent(new WorkflowEvent(siteId, packageId, eventType));
            // TODO: implement notifications
        } finally {
            generalLockService.unlock(packageLockKey);
        }
    }

    /**
     * Audit package state update: cancellation/rejection/approval
     *
     * @param publishPackage the package being cancelled
     * @param username       the username of the user who cancelled the package
     * @param operation      the operation being performed
     */
    private void createUpdateStatePackageAuditLogEntry(final PublishPackage publishPackage,
                                                       final String username, final String operation) {
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOrigin(ORIGIN_API);
        auditLog.setOperation(operation);
        auditLog.setActorId(username);
        auditLog.setSiteId(publishPackage.getSiteId());
        auditLog.setPrimaryTargetId(String.valueOf(publishPackage.getId()));
        auditLog.setPrimaryTargetType(TARGET_TYPE_PUBLISHING_PACKAGE);
        auditLog.setPrimaryTargetValue(String.valueOf(publishPackage.getId()));
        auditServiceInternal.insertAuditLog(auditLog);
    }

    public void setItemServiceInternal(final ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    @SuppressWarnings("unused")
    public void setActivityStreamServiceInternal(final ActivityStreamServiceInternal activityStreamServiceInternal) {
        this.activityStreamServiceInternal = activityStreamServiceInternal;
    }

    public void setAuditServiceInternal(final AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public void setGeneralLockService(final GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    @SuppressWarnings("unused")
    public void setPublishDao(final PublishDAO publishDao) {
        this.publishDao = publishDao;
    }

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setSiteService(final SitesService siteService) {
        this.siteService = siteService;
    }

    public void setUserServiceInternal(final UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    private interface PackageReview {
        void reviewPackage(PublishPackage publishPackage) throws ServiceLayerException;
    }
}

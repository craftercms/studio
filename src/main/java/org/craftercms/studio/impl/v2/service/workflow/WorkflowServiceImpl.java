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

package org.craftercms.studio.impl.v2.service.workflow;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.AuditLogParameter;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v2.dal.WorkflowItem;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.publish.internal.PublishServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.WorkflowService;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.craftercms.studio.permissions.CompositePermission;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_INITIAL_PUBLISH;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUBLISH;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REJECT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REQUEST_PUBLISH;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REJECTION_COMMENT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SUBMISSION_COMMENT;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_WORKFLOW_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.CANCEL_WORKFLOW_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.PUBLISH_TO_STAGE_AND_LIVE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_LIVE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_LIVE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_SCHEDULED_LIVE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_SCHEDULED_LIVE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_SCHEDULED_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.SUBMIT_TO_WORKFLOW_SCHEDULED_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.isInWorkflowOrScheduled;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;
import static org.craftercms.studio.api.v2.dal.Workflow.STATE_OPENED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_LIVE;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.PATH_RESOURCE_ID;
import static org.craftercms.studio.permissions.PermissionResolverImpl.SITE_ID_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_DELETE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_READ;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_PUBLISH;

public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    private ItemServiceInternal itemServiceInternal;
    private ContentServiceInternal contentServiceInternal;
    private DependencyServiceInternal dependencyServiceInternal;
    private SiteService siteService;
    private AuditServiceInternal auditServiceInternal;
    private SecurityService securityService;
    private WorkflowServiceInternal workflowServiceInternal;
    private UserServiceInternal userServiceInternal;
    private DeploymentService deploymentService;
    private NotificationService notificationService;
    private DependencyService dependencyService;
    private PublishServiceInternal publishServiceInternal;
    private ServicesConfig servicesConfig;
    private StudioConfiguration studioConfiguration;

    @Override
    public int getItemStatesTotal(String siteId, String path, Long states) {
        return itemServiceInternal.getItemStatesTotal(siteId, path, states);
    }

    @Override
    public List<SandboxItem> getItemStates(String siteId, String path, Long states, int offset, int limit) {
        return itemServiceInternal.getItemStates(siteId, path, states, offset, limit)
                .stream().map(item -> SandboxItem.getInstance(item)).collect(Collectors.toList());
    }

    @Override
    public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing,
                                 boolean clearUserLocked, Boolean live, Boolean staged) {
        itemServiceInternal.updateItemStates(siteId, paths, clearSystemProcessing, clearUserLocked, live, staged);
    }

    @Override
    public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
                                        boolean clearUserLocked, Boolean live, Boolean staged) {
        itemServiceInternal.updateItemStatesByQuery(siteId, path, states, clearSystemProcessing, clearUserLocked,
                live, staged);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = PERMISSION_CONTENT_READ)
    public List<SandboxItem> getWorkflowAffectedPaths(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                                                      @ProtectedResourceId(PATH_RESOURCE_ID)
                                                      String path) throws UserNotFoundException, ServiceLayerException {
        List<String> affectedPaths = new ArrayList<String>();
        List<SandboxItem> result = new ArrayList<>();
        Item item = itemServiceInternal.getItem(siteId, path);
        if (Objects.isNull(item)) {
            throw new ContentNotFoundException(path, siteId,
                    "Content not found for site " + siteId + " and path " + path);
        }
        if (isInWorkflowOrScheduled(item.getState())) {
            affectedPaths.add(path);
            boolean isNew = isNew(item.getState());
            boolean isRenamed = StringUtils.isNotEmpty(item.getPreviousPath());
            if (isNew || isRenamed) {
                affectedPaths.addAll(getMandatoryDescendents(siteId, path));
            }
            List<String> dependencyPaths = new ArrayList<String>();
            dependencyPaths.addAll(dependencyServiceInternal.getHardDependencies(siteId, affectedPaths));
            affectedPaths.addAll(dependencyPaths);
            List<String> candidates = new ArrayList<String>();
            for (String p : affectedPaths) {
                if (!candidates.contains(p)) {
                    candidates.add(p);
                }
            }

            List<SandboxItem> candidateItems = contentServiceInternal.getSandboxItemsByPath(siteId, candidates, true);
            result = candidateItems.stream().filter(i -> isInWorkflowOrScheduled(i.getState())).collect(Collectors.toList());
        }
        return result;
    }

    private List<String> getMandatoryDescendents(String site, String path)
            throws UserNotFoundException, ServiceLayerException {
        List<String> descendents = new ArrayList<String>();
        GetChildrenResult result = contentServiceInternal.getChildrenByPath(site, path, null, null, null, null,
                null, 0, Integer.MAX_VALUE);
        if (result != null) {
            if (Objects.nonNull(result.getLevelDescriptor())) {
                descendents.add(result.getLevelDescriptor().getPath());
            }
            if (CollectionUtils.isNotEmpty(result.getChildren())) {
                for (SandboxItem item : result.getChildren()) {
                    descendents.add(item.getPath());
                    descendents.addAll(getMandatoryDescendents(site, item.getPath()));
                }
            }
        }
        return descendents;
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_READ)
    public void requestPublish(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                               @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                               List<String> optionalDependencies, String publishingTarget, ZonedDateTime schedule,
                               String comment, boolean sendEmailNotifications)
            throws ServiceLayerException, UserNotFoundException, DeploymentException {
        // Create submission package
        List<String> pathsToAddToWorkflow = calculateSubmissionPackage(siteId, paths, optionalDependencies);
        try {
            String submittedBy = securityService.getCurrentUser();
            // set system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToAddToWorkflow, true);
            // cancel existing workflow
            cancelExistingWorkflowEntries(siteId, pathsToAddToWorkflow);
            // create new workflow entries
            createWorkflowEntries(siteId, pathsToAddToWorkflow, submittedBy, publishingTarget, schedule, comment,
                    sendEmailNotifications);
            // notify approvers
            notificationService.notifyApprovesContentSubmission(
                    siteId, null, pathsToAddToWorkflow, submittedBy, schedule, false, comment);
            // create audit log entries
            createPublishRequestAuditLogEntry(siteId, pathsToAddToWorkflow, submittedBy, comment);
        } finally {
            // clear system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToAddToWorkflow, false);
        }
    }

    private List<String> calculateSubmissionPackage(String siteId, List<String> paths,
                                                    List<String> optionalDependencies) throws ServiceLayerException {
        var submissionPackage = new ArrayList<String>();
        submissionPackage.addAll(paths);
        if (CollectionUtils.isNotEmpty(optionalDependencies)) {
            submissionPackage.addAll(optionalDependencies);
        }
        List<String> dependencies = dependencyServiceInternal.getHardDependencies(siteId, submissionPackage);
        submissionPackage.addAll(dependencies);
        return submissionPackage;
    }

    private void cancelExistingWorkflowEntries(String siteId, List<String> paths) throws DeploymentException {
        if (CollectionUtils.isNotEmpty(paths)) {
            deploymentService.cancelWorkflowBulk(siteId, Set.copyOf(paths));
            workflowServiceInternal.deleteWorkflowEntries(siteId, paths);
            itemServiceInternal.updateStateBitsBulk(siteId, paths, CANCEL_WORKFLOW_ON_MASK, CANCEL_WORKFLOW_OFF_MASK);
        }
    }

    private void createWorkflowEntries(String siteId, List<String> paths, String submittedBy, String publishingTarget,
                                       ZonedDateTime scheduledDate, String submissionComment,
                                       boolean sendEmailNotifications)
            throws UserNotFoundException, ServiceLayerException {
        User userObj = userServiceInternal.getUserByIdOrUsername(-1, submittedBy);
        var workflowEntries = new ArrayList<Workflow>();
        paths.forEach(path -> {
            Item it = itemServiceInternal.getItem(siteId, path);

            Workflow workflow = new Workflow();
            workflow.setItemId(it.getId());
            workflow.setSubmitterId(userObj.getId());
            workflow.setNotifySubmitter(sendEmailNotifications ? 1: 0);
            workflow.setSubmitterComment(submissionComment);
            workflow.setTargetEnvironment(publishingTarget);
            if (Objects.nonNull(scheduledDate)) {
                workflow.setSchedule(scheduledDate);
            }
            workflow.setState(STATE_OPENED);
            workflow.setTargetEnvironment(publishingTarget);
            workflowEntries.add(workflow);
        });
        workflowServiceInternal.insertWorkflowEntries(workflowEntries);

        // Item
        String liveEnvironment = StringUtils.EMPTY;
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            liveEnvironment = servicesConfig.getLiveEnvironment(siteId);
        }
        boolean isLive = false;
        if (StringUtils.isEmpty(liveEnvironment)) {
            liveEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_LIVE);
        }
        if (liveEnvironment.equals(publishingTarget)) {
            isLive = true;
        }

        if (Objects.nonNull(scheduledDate)) {
            if (isLive) {
                itemServiceInternal.updateStateBitsBulk(siteId, paths,
                        SUBMIT_TO_WORKFLOW_SCHEDULED_LIVE_ON_MASK,
                        SUBMIT_TO_WORKFLOW_SCHEDULED_LIVE_OFF_MASK);
            } else {
                itemServiceInternal.updateStateBitsBulk(siteId, paths, SUBMIT_TO_WORKFLOW_SCHEDULED_ON_MASK,
                        SUBMIT_TO_WORKFLOW_SCHEDULED_OFF_MASK);
            }
        } else {
            if (isLive) {
                itemServiceInternal.updateStateBitsBulk(siteId, paths, SUBMIT_TO_WORKFLOW_LIVE_ON_MASK,
                        SUBMIT_TO_WORKFLOW_LIVE_OFF_MASK);
            } else {
                itemServiceInternal.updateStateBitsBulk(siteId, paths, SUBMIT_TO_WORKFLOW_ON_MASK,
                        SUBMIT_TO_WORKFLOW_OFF_MASK);
            }
        }
    }

    private void createPublishRequestAuditLogEntry(String siteId, List<String> submittedPaths, String submittedBy,
                                                   String comment)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_REQUEST_PUBLISH);
        auditLog.setActorId(submittedBy);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
        auditLog.setPrimaryTargetValue(siteId);
        var auditLogParameters = new ArrayList<AuditLogParameter>();
        submittedPaths.forEach(path -> {
            var auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + path);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(path);
            auditLogParameters.add(auditLogParameter);
        });
        if (StringUtils.isNotEmpty(comment)) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":submissionComment");
            auditLogParameter.setTargetType(TARGET_TYPE_SUBMISSION_COMMENT);
            auditLogParameter.setTargetValue(comment);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_PUBLISH)
    public void publish(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                        @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                        List<String> optionalDependencies, String publishingTarget, ZonedDateTime schedule,
                        String comment) throws ServiceLayerException, UserNotFoundException, DeploymentException {
        if (!publishServiceInternal.isSitePublished(siteId)) {
            publishServiceInternal.initialPublish(siteId);
            itemServiceInternal.updateStatesForSite(siteId, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK,
                    PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
            createInitialPublishAuditLog(siteId);
        } else {
            // Create publish package
            List<String> pathsToPublish = calculatePublishPackage(siteId, paths, optionalDependencies);
            try {
                // Set system processing
                itemServiceInternal.setSystemProcessingBulk(siteId, pathsToPublish, true);
                // Cancel scheduled items from publishing queue
                publishServiceInternal.cancelScheduledQueueItems(siteId, pathsToPublish);
                // Add to publishing queue
                String publishedBy = securityService.getCurrentUser();
                boolean scheduledDateIsNow = false;
                if (schedule == null) {
                    scheduledDateIsNow = true;
                    schedule = ZonedDateTime.now(ZoneOffset.UTC);
                }
                deploymentService.deploy(siteId, publishingTarget, paths, schedule, publishedBy, comment, scheduledDateIsNow);
                // Insert audit log
                createPublishAuditLogEntry(siteId, pathsToPublish, publishedBy);
            } finally {
                // Reset system processing
                itemServiceInternal.setSystemProcessingBulk(siteId, pathsToPublish, false);
            }
        }
    }

    private List<String> calculatePublishPackage(String siteId, List<String> paths,
                                                    List<String> optionalDependencies)
            throws ServiceLayerException, UserNotFoundException {
        var submissionPackage = new ArrayList<String>();
        var publishPackage = new ArrayList<String>();
        submissionPackage.addAll(paths);
        if (CollectionUtils.isNotEmpty(optionalDependencies)) {
            submissionPackage.addAll(optionalDependencies);
        }
        List<String> dependencies = dependencyServiceInternal.getHardDependencies(siteId, submissionPackage);
        submissionPackage.addAll(dependencies);
        publishPackage.addAll(submissionPackage);
        // Calculate renamed items and add renamed children
        List<Item> items = itemServiceInternal.getItems(siteId, submissionPackage, false);
        items.forEach(item ->{
            if (StringUtils.isNotEmpty(item.getPreviousPath())) {
                publishPackage.addAll(itemServiceInternal.getSubtreeForDelete(siteId, item.getPath()));
            }
        });
        return publishPackage;
    }

    private void createPublishAuditLogEntry(String siteId, List<String> pathsToPublish, String publishedBy)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_PUBLISH);
        auditLog.setActorId(publishedBy);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        var auditLogParameters = new ArrayList<AuditLogParameter>();
        pathsToPublish.forEach(path -> {
            var auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + path);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(path);
            auditLogParameters.add(auditLogParameter);
        });
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_PUBLISH)
    public void approve(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                        @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                        List<String> optionalDependencies, String publishingTarget, ZonedDateTime schedule,
                        String comment) throws UserNotFoundException, ServiceLayerException, DeploymentException {
        if (!publishServiceInternal.isSitePublished(siteId)) {
            publishServiceInternal.initialPublish(siteId);
            itemServiceInternal.updateStatesForSite(siteId, PUBLISH_TO_STAGE_AND_LIVE_ON_MASK,
                    PUBLISH_TO_STAGE_AND_LIVE_OFF_MASK);
            createInitialPublishAuditLog(siteId);
        } else {
            // Create publish package
            List<String> pathsToPublish = calculatePublishPackage(siteId, paths, optionalDependencies);
            try {
                // Set system processing
                itemServiceInternal.setSystemProcessingBulk(siteId, pathsToPublish, true);
                // Cancel scheduled items from publishing queue
                publishServiceInternal.cancelScheduledQueueItems(siteId, pathsToPublish);
                // Add to publishing queue
                String publishedBy = securityService.getCurrentUser();
                boolean scheduledDateIsNow = false;
                if (schedule == null) {
                    scheduledDateIsNow = true;
                    schedule = ZonedDateTime.now(ZoneOffset.UTC);
                }
                deploymentService.deploy(siteId, publishingTarget, paths, schedule, publishedBy, comment, scheduledDateIsNow);
                // Insert audit log
                createApproveAuditLogEntry(siteId, pathsToPublish, publishedBy, comment);
            } finally {
                // Reset system processing
                itemServiceInternal.setSystemProcessingBulk(siteId, pathsToPublish, false);
            }
        }
    }

    private void createApproveAuditLogEntry(String siteId, List<String> pathsToPublish, String publishedBy,
                                            String comment)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_PUBLISH);
        auditLog.setActorId(publishedBy);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        var auditLogParameters = new ArrayList<AuditLogParameter>();
        pathsToPublish.forEach(path -> {
            var auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + path);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(path);
            auditLogParameters.add(auditLogParameter);
        });
        if (StringUtils.isNotEmpty(comment)) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":submissionComment");
            auditLogParameter.setTargetType(TARGET_TYPE_SUBMISSION_COMMENT);
            auditLogParameter.setTargetValue(comment);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void createInitialPublishAuditLog(String siteId) throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_INITIAL_PUBLISH);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_PUBLISH)
    public void reject(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                       @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                       String comment) throws ServiceLayerException, DeploymentException {
        // Create submission package
        List<String> pathsToCancelWorkflow = calculateSubmissionPackage(siteId, paths, null);
        try {
            boolean shouldNotify = false;

            String rejectedBy = securityService.getCurrentUser();
            // set system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToCancelWorkflow, true);

            // get submitters list
            Set<String> submitterList = new HashSet<>();
            for (String path : pathsToCancelWorkflow) {
                WorkflowItem workflowItem = workflowServiceInternal.getWorkflowEntry(siteId, path);
                if (Objects.nonNull(workflowItem)) {
                    shouldNotify = shouldNotify || workflowItem.getNotifySubmitter() == 1;
                    try {
                        User submitter = userServiceInternal
                                .getUserByIdOrUsername(workflowItem.getSubmitterId(), StringUtils.EMPTY);
                        if (Objects.nonNull(submitter)) {
                            submitterList.add(submitter.getUsername());
                        }
                    } catch (UserNotFoundException | ServiceLayerException e) {
                        logger.debug("Didn't find submitter user for path {0}. Notification will not be sent.", e,
                                path);
                    }
                }
            }

            // cancel workflow
            cancelExistingWorkflowEntries(siteId, pathsToCancelWorkflow);
            // create audit log entries
            createRejectAuditLogEntry(siteId, pathsToCancelWorkflow, rejectedBy, comment);
            // notify rejection
            if (shouldNotify) {
                notifyRejection(siteId, pathsToCancelWorkflow, rejectedBy, comment, List.copyOf(submitterList));
            }
        } finally {
            // clear system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToCancelWorkflow, false);
        }
    }

    private void createRejectAuditLogEntry(String siteId, List<String> submittedPaths, String rejectedBy,
                                           String comment)
            throws SiteNotFoundException {
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_REJECT);
        auditLog.setActorId(rejectedBy);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteId);
        var auditLogParameters = new ArrayList<AuditLogParameter>();
        submittedPaths.forEach(path -> {
            var auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":" + path);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(path);
            auditLogParameters.add(auditLogParameter);
        });
        if (StringUtils.isNotEmpty(comment)) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(siteId + ":rejectionComment");
            auditLogParameter.setTargetType(TARGET_TYPE_REJECTION_COMMENT);
            auditLogParameter.setTargetValue(comment);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void notifyRejection(String siteId, List<String> pathsToCancelWorkflow, String rejectedBy, String reason,
                                 List<String> submitterList) {
        notificationService.notifyContentRejection(siteId, submitterList, pathsToCancelWorkflow, reason, rejectedBy);
    }

    @Override
    @HasPermission(type = CompositePermission.class, action = PERMISSION_CONTENT_DELETE)
    public void delete(@ProtectedResourceId(SITE_ID_RESOURCE_ID) String siteId,
                       @ProtectedResourceId(PATH_LIST_RESOURCE_ID) List<String> paths,
                       List<String> optionalDependencies, String comment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {
        // create submission package (aad folders and children if pages)
        List<String> pathsToDelete = calculateDeleteSubmissionPackage(siteId, paths, optionalDependencies);
        String deletedBy = securityService.getCurrentUser();
        try {
            // set system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToDelete, true);
            // cancel existing workflow
            cancelExistingWorkflowEntries(siteId, pathsToDelete);
            // add to publishing queue
            deploymentService.delete(siteId, pathsToDelete, deletedBy, ZonedDateTime.now(ZoneOffset.UTC), comment);
            // send notification email
            // TODO: We don't have notifications on delete now. Fix this ???
        } finally {
            // clear system processing
            itemServiceInternal.setSystemProcessingBulk(siteId, pathsToDelete, false);
        }
    }

    private List<String> calculateDeleteSubmissionPackage(String siteId, List<String> paths,
                                                          List<String> optionalDependencies)
            throws UserNotFoundException, ServiceLayerException {
        var deletePackage = new ArrayList<String>();
        deletePackage.addAll(paths);
        if (CollectionUtils.isNotEmpty(optionalDependencies)) {
            deletePackage.addAll(optionalDependencies);
        }
        List<SandboxItem> items = contentServiceInternal.getSandboxItemsByPath(siteId, paths, false);
        items.forEach(item -> {
            if (StringUtils.equals(item.getSystemType(), StudioConstants.CONTENT_TYPE_FOLDER)) {
                deletePackage.addAll(itemServiceInternal.getSubtreeForDelete(siteId, item.getPath()));
            } else if (StringUtils.equals(item.getSystemType(), StudioConstants.CONTENT_TYPE_PAGE)) {
                deletePackage.addAll(itemServiceInternal.getSubtreeForDelete(siteId,
                        item.getPath().replace(FILE_SEPARATOR + INDEX_FILE, "")));
            }
        });
        Set<String> dependencies = dependencyService.getDeleteDependencies(siteId, deletePackage);
        deletePackage.addAll(dependencies);
        return deletePackage;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public ContentServiceInternal getContentServiceInternal() {
        return contentServiceInternal;
    }

    public void setContentServiceInternal(ContentServiceInternal contentServiceInternal) {
        this.contentServiceInternal = contentServiceInternal;
    }

    public DependencyServiceInternal getDependencyServiceInternal() {
        return dependencyServiceInternal;
    }

    public void setDependencyServiceInternal(DependencyServiceInternal dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public WorkflowServiceInternal getWorkflowServiceInternal() {
        return workflowServiceInternal;
    }

    public void setWorkflowServiceInternal(WorkflowServiceInternal workflowServiceInternal) {
        this.workflowServiceInternal = workflowServiceInternal;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public DeploymentService getDeploymentService() {
        return deploymentService;
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public PublishServiceInternal getPublishServiceInternal() {
        return publishServiceInternal;
    }

    public void setPublishServiceInternal(PublishServiceInternal publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}

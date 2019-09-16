/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
 */
package org.craftercms.studio.impl.v1.service.deployment;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.CommitNotFoundException;
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentHistoryProvider;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDeploymentTaskTO;
import org.craftercms.studio.api.v1.to.PublishStatus;
import org.craftercms.studio.api.v1.to.PublishingChannelTO;
import org.craftercms.studio.api.v1.to.PublishingTargetTO;
import org.craftercms.studio.api.v1.to.RepoOperationTO;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.service.deployment.job.DeployContentToEnvironmentStore;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.craftercms.studio.api.v1.constant.StudioConstants.DATE_FORMAT_DEPLOYED;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v1.service.objectstate.State.NEW_DELETED;
import static org.craftercms.studio.api.v1.service.objectstate.State.NEW_SUBMITTED_NO_WF_SCHEDULED;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.DELETE;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SUBMIT_WITHOUT_WORKFLOW_SCHEDULED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_START_PUBLISHER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_STOP_PUBLISHER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.PREVIOUS_COMMIT_SUFFIX;

/**
 */
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    private static int CTED_AUTOINCREMENT = 0;

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected DependencyService dependencyService;
    protected DmFilterWrapper dmFilterWrapper;
    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ObjectMetadataManager objectMetadataManager;
    protected ContentRepository contentRepository;
    protected DmPublishService dmPublishService;
    protected SecurityService securityService;
    protected EventService eventService;
    protected DeployContentToEnvironmentStore deployContentToEnvironmentStoreJob;
    protected NotificationService notificationService;
    protected DeploymentHistoryProvider deploymentHistoryProvider;
    protected StudioConfiguration studioConfiguration;
    protected PublishRequestMapper publishRequestMapper;
    protected AuditServiceInternal auditServiceInternal;

    @Override
    @ValidateParams
    public void deploy(@ValidateStringParam(name = "site") String site,
                       @ValidateStringParam(name = "environment") String environment, List<String> paths,
                       ZonedDateTime scheduledDate, @ValidateStringParam(name = "approver") String approver,
                       @ValidateStringParam(name = "submissionComment") String submissionComment,
                       final boolean scheduleDateNow) throws DeploymentException {

        if (scheduledDate != null && scheduledDate.isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
            objectStateService.transitionBulk(site, paths, SUBMIT_WITHOUT_WORKFLOW_SCHEDULED,
                    NEW_SUBMITTED_NO_WF_SCHEDULED);
        }
        List<String> newPaths = new ArrayList<String>();
        List<String> updatedPaths = new ArrayList<String>();
        List<String> movedPaths = new ArrayList<String>();

        Map<String, List<String>> groupedPaths = new HashMap<String, List<String>>();

        for (String p : paths) {
            boolean isFolder = contentRepository.isFolder(site, p);
            if (isFolder) {
                logger.debug("Content item at path " + p + " for site " + site +
                        " is folder and will not be added to publishing queue.");
            } else {
                if (objectStateService.isNew(site, p)) {
                    newPaths.add(p);
                } else if (objectMetadataManager.isRenamed(site, p)) {
                    movedPaths.add(p);
                } else {
                    updatedPaths.add(p);
                }
            }
        }

        groupedPaths.put(PublishRequest.Action.NEW, newPaths);
        groupedPaths.put(PublishRequest.Action.MOVE, movedPaths);
        groupedPaths.put(PublishRequest.Action.UPDATE, updatedPaths);

        environment = resolveEnvironment(site, environment);

        List<PublishRequest> items = createItems(site, environment, groupedPaths, scheduledDate, approver,
                submissionComment);
        for (PublishRequest item : items) {
            publishRequestMapper.insertItemForDeployment(item);
        }
        objectStateService.setSystemProcessingBulk(site, paths, false);
        // We need to pick up this on Inserting , not on execution!
        try {
            sendContentApprovalEmail(items, scheduleDateNow);
        } catch(Exception errNotify) {
            logger.error("Error sending approval notification ", errNotify);
        }
        String statusMessage = studioConfiguration
                .getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED);
        try {
            siteService.updatePublishingStatusMessage(site, statusMessage);
        } catch (SiteNotFoundException e) {
            logger.error("Error updating publishing status for site " + site);
        }
    }

    private String resolveEnvironment(String site, String environment) {
        String toRet = environment;
        List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);
        for (PublishingTargetTO target : publishingTargets) {
            if (target.getDisplayLabel().equals(environment)) {
                toRet = target.getRepoBranchName();
                break;
            }
        }
        return toRet;
    }

    protected void sendContentApprovalEmail(List<PublishRequest> itemList, boolean scheduleDateNow) {
        for (PublishRequest listItem : itemList) {
            ItemMetadata itemMetadata = objectMetadataManager.getProperties(listItem.getSite(), listItem.getPath());
            if (itemMetadata != null) {
                if (itemMetadata.getSendEmail() == 1) {
                    // found the first item that needs to be sent
                    notificationService.notifyContentApproval(listItem.getSite(),
                        itemMetadata.getSubmittedBy(),
                        getPathRelativeToSite(itemList),
                        listItem.getUser(),
                        // Null == now, anything else is scheduled
                        scheduleDateNow?null:listItem.getScheduledDate(),
                        Locale.ENGLISH);
                    // no point in looking further, quit looping
                    break;
                }
            }
        }
    }

    private List<String> getPathRelativeToSite(final List<PublishRequest> itemList) {
        List<String> paths = new ArrayList<String>(itemList.size());
        for (PublishRequest copyToEnvironment : itemList) {
            paths.add(copyToEnvironment.getPath());
        }
        return paths;
    }

    private List<PublishRequest> createItems(String site, String environment, Map<String, List<String>> paths,
                                             ZonedDateTime scheduledDate, String approver, String submissionComment) {
        List<PublishRequest> newItems = new ArrayList<PublishRequest>();

        String packageId = UUID.randomUUID().toString();

        Map<String, Object> params = null;
        for (String action : paths.keySet()) {
            for (String path : paths.get(action)) {
                PublishRequest item = new PublishRequest();
                ItemMetadata metadata = objectMetadataManager.getProperties(site, path);
                if (metadata != null) {
                    params = new HashMap<String, Object>();
                    params.put("site_id", site);
                    params.put("environment", environment);
                    params.put("state", PublishRequest.State.READY_FOR_LIVE);
                    params.put("path", path);
                    params.put("commitId", metadata.getCommitId());
                    if (publishRequestMapper.checkItemQueued(params) > 0) {
                        logger.info("Path " + path + " with commit ID " + metadata.getCommitId() +
                                " already has queued publishing request for environment " + environment + " of site " +
                                site + ". Adding another publishing request is skipped.");
                    } else {
                        item.setId(++CTED_AUTOINCREMENT);
                        item.setSite(site);
                        item.setEnvironment(environment);
                        item.setPath(path);
                        item.setScheduledDate(scheduledDate);
                        item.setState(PublishRequest.State.READY_FOR_LIVE);
                        item.setAction(action);
                        if (metadata.getRenamed() > 0) {
                            String oldPath = metadata.getOldUrl();
                            item.setOldPath(oldPath);
                        }
                        String commitId = metadata.getCommitId();
                        if (StringUtils.isNotEmpty(commitId)) {
                            item.setCommitId(commitId);
                        } else {
                            item.setCommitId(contentRepository.getRepoLastCommitId(site));
                        }

                        String contentTypeClass = contentService.getContentTypeClass(site, path);
                        item.setContentTypeClass(contentTypeClass);
                        item.setUser(approver);
                        item.setSubmissionComment(submissionComment);
                        item.setPackageId(packageId);
                        newItems.add(item);
                    }

                    if (scheduledDate != null && scheduledDate.isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
                        Map<String, Object> properties = new HashMap<>();
                        properties.put(ItemMetadata.PROP_SUBMISSION_COMMENT, submissionComment);
                        properties.put(ItemMetadata.PROP_SUBMITTED_TO_ENVIRONMENT, environment);
                        properties.put(ItemMetadata.PROP_LAUNCH_DATE, scheduledDate);
                        objectMetadataManager.setObjectMetadata(site, path, properties);
                    }
                }
            }
        }
        return newItems;
    }

    @Override
    @ValidateParams
    public void delete(@ValidateStringParam(name = "site") String site, List<String> paths,
                       @ValidateStringParam(name = "approver") String approver, ZonedDateTime scheduledDate)
            throws DeploymentException, SiteNotFoundException {
        if (scheduledDate != null && scheduledDate.isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
            objectStateService.transitionBulk(site, paths, DELETE, NEW_DELETED);

        }
        Set<String> environments = getAllPublishingEnvironments(site);
        for (String environment : environments) {
            List<PublishRequest> items = createDeleteItems(site, environment, paths, approver, scheduledDate);
            for (PublishRequest item : items) {
                publishRequestMapper.insertItemForDeployment(item);
            }
        }
        objectStateService.setSystemProcessingBulk(site, paths, false);
        String statusMessage = studioConfiguration
                .getProperty(JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_QUEUED);
        try {
            siteService.updatePublishingStatusMessage(site, statusMessage);
        } catch (SiteNotFoundException e) {
            logger.error("Error updating publishing status for site " + site);
        }
    }

    protected Set<String> getAllPublishingEnvironments(String site) {
        List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);
        Set<String> environments = new HashSet<String>();
        if (publishingTargets != null && publishingTargets.size() > 0) {
            for (PublishingTargetTO target : publishingTargets) {
                if (StringUtils.isNotEmpty(target.getRepoBranchName())) {
                    environments.add(target.getRepoBranchName());
                }
            }
        }
        return environments;
    }

    private List<PublishRequest> createDeleteItems(String site, String environment, List<String> paths,
                                                   String approver, ZonedDateTime scheduledDate)
            throws SiteNotFoundException {
        List<PublishRequest> newItems = new ArrayList<PublishRequest>(paths.size());
        String packageId = UUID.randomUUID().toString();
        for (String path : paths) {
            if (contentService.contentExists(site, path)) {
                ContentItemTO contentItem = contentService.getContentItem(site, path, 0);
                if (!contentItem.isFolder()) {
                    PublishRequest item = new PublishRequest();
                    ItemMetadata metadata = objectMetadataManager.getProperties(site, path);
                    item.setId(++CTED_AUTOINCREMENT);
                    item.setSite(site);
                    item.setEnvironment(environment);
                    item.setPath(path);
                    item.setScheduledDate(scheduledDate);
                    item.setState(PublishRequest.State.READY_FOR_LIVE);
                    item.setAction(PublishRequest.Action.DELETE);
                    if (metadata != null) {
                        if (metadata.getRenamed() > 0) {
                            String oldPath = metadata.getOldUrl();
                            item.setOldPath(oldPath);
                        }
                        String commitId = metadata.getCommitId();
                        if (StringUtils.isNotEmpty(commitId)) {
                            item.setCommitId(commitId);
                        } else {
                            item.setCommitId(contentRepository.getRepoLastCommitId(site));
                        }
                    }
                    String contentTypeClass = contentService.getContentTypeClass(site, path);
                    item.setContentTypeClass(contentTypeClass);
                    item.setUser(approver);
                    item.setPackageId(packageId);
                    newItems.add(item);

                    if (contentService.contentExists(site, path)) {
                        contentService.deleteContent(site, path, approver);
                        if (path.endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                            deleteFolder(site, path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE,
                                    ""), approver);
                        }
                    }
                    String lastRepoCommitId = contentRepository.getRepoLastCommitId(site);
                    if (StringUtils.isNotEmpty(lastRepoCommitId)) {
                        item.setCommitId(lastRepoCommitId);
                    }
                } else {
                    RepositoryItem[] children = contentRepository.getContentChildren(site, path);
                    List<String> childPaths = new ArrayList<String>();
                    for (RepositoryItem child : children) {
                        childPaths.add(child.path + FILE_SEPARATOR + child.name);
                    }
                    newItems.addAll(createDeleteItems(site, environment, childPaths, approver, scheduledDate));
                    deleteFolder(site, path, approver);
                }
            }
        }
        return newItems;
    }

    private void deleteFolder(String site, String path, String user) throws SiteNotFoundException {
        String folderPath = path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        if (contentService.contentExists(site, path)) {
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                if (path.endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                    contentService.deleteContent(site, path, true, user);
                    objectStateService.deleteObjectStatesForFolder(site, folderPath);
                    objectMetadataManager.deleteObjectMetadataForFolder(site, folderPath);
                    String parentPath = ContentUtils.getParentUrl(path);
                    deleteFolder(site, parentPath, user);
                } else {
                    contentService.deleteContent(site, path, true, user);
                    objectStateService.deleteObjectStatesForFolder(site, folderPath);
                    objectMetadataManager.deleteObjectMetadataForFolder(site, folderPath);
                }
            }
        } else {
            objectStateService.deleteObjectStatesForFolder(site, folderPath);
            objectMetadataManager.deleteObjectMetadataForFolder(site, folderPath);
        }
    }

    @Override
    @ValidateParams
    public void deleteDeploymentDataForSite(@ValidateStringParam(name = "site") final String site) {
        signalWorkersToStop();
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        publishRequestMapper.deleteDeploymentDataForSite(params);
        signalWorkersToContinue();
    }


    private void signalWorkersToContinue() {
        DeployContentToEnvironmentStore.signalToStop(false);
    }

    private void signalWorkersToStop() {
        DeployContentToEnvironmentStore.signalToStop(true);
        while (DeployContentToEnvironmentStore.isRunning()) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                logger.info("Interrupted while waiting to stop workers", e);
            }
        }
    }

    @Override
    @ValidateParams
    public List<PublishRequest> getScheduledItems(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("state", PublishRequest.State.READY_FOR_LIVE);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        return publishRequestMapper.getScheduledItems(params);
    }

    @Override
    @ValidateParams
    public void cancelWorkflow(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path) throws DeploymentException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        publishRequestMapper.cancelWorkflow(params);
    }

    @Override
    @ValidateParams
    public void cancelWorkflowBulk(@ValidateStringParam(name = "site") String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("paths", paths);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", ZonedDateTime.now(ZoneOffset.UTC));
        publishRequestMapper.cancelWorkflowBulk(params);
    }

    @Override
    @ValidateParams
    public List<DmDeploymentTaskTO> getDeploymentHistory(@ValidateStringParam(name = "site") String site,
                                 @ValidateIntegerParam(name = "daysFromToday") int daysFromToday,
                                 @ValidateIntegerParam(name = "numberOfItems") int numberOfItems,
                                 @ValidateStringParam(name = "sort") String sort, boolean ascending,
                                 @ValidateStringParam(name = "filterType") String filterType) throws SiteNotFoundException {
        // get the filtered list of attempts in a specific date range
        ZonedDateTime toDate = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime fromDate = toDate.minusDays(daysFromToday);
        List<DeploymentSyncHistory> deployReports = deploymentHistoryProvider.getDeploymentHistory(site,
                getEnvironmentNames(site), fromDate, toDate, dmFilterWrapper, filterType, numberOfItems);
        List<DmDeploymentTaskTO> tasks = new ArrayList<DmDeploymentTaskTO>();

        if (deployReports != null) {
            int count = 0;
            String timezone = servicesConfig.getDefaultTimezone(site);
            //Set<String> processedItems = new HashSet<String>();
            Map<String, Set<String>> processedItems = new HashMap<String, Set<String>>();
            for (int index = 0; index < deployReports.size() && count < numberOfItems; index++) {
                DeploymentSyncHistory entry = deployReports.get(index);
                String env = entry.getEnvironment();
                if (!processedItems.containsKey(env)) {
                    processedItems.put(env, new HashSet<String>());
                }
                if (!processedItems.get(env).contains(entry.getPath())) {
                    ContentItemTO deployedItem = getDeployedItem(entry.getSite(), entry.getPath());
                    if (deployedItem != null) {
                        deployedItem.eventDate = entry.getSyncDate();
                        deployedItem.endpoint = entry.getTarget();
                        deployedItem.setUser(entry.getUser());
                        deployedItem.setEndpoint(entry.getEnvironment());
                        String deployedLabel =
                                entry.getSyncDate().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DEPLOYED));
                        if (tasks.size() > 0) {
                            DmDeploymentTaskTO lastTask = tasks.get(tasks.size() - 1);
                            String lastDeployedLabel = lastTask.getInternalName();
                            if (lastDeployedLabel.equals(deployedLabel)) {
                                // add to the last task if it is deployed on the same day
                                lastTask.setNumOfChildren(lastTask.getNumOfChildren() + 1);
                                lastTask.getChildren().add(deployedItem);
                            } else {
                                tasks.add(createDeploymentTask(deployedLabel, deployedItem));
                            }
                        } else {
                            tasks.add(createDeploymentTask(deployedLabel, deployedItem));
                        }
                        processedItems.get(env).add(entry.getPath());
                    }
                }
            }
        }
        return tasks;
    }

    private List<String> getEnvironmentNames(String siteId) {
        List<String> toRet = new ArrayList<String>();
        if (servicesConfig.isStagingEnvironmentEnabled(siteId)) {
            List<PublishingChannelTO> channelsTO = getPublishedEnvironments(siteId);
            if (CollectionUtils.isNotEmpty(channelsTO)) {
                channelsTO.forEach(channel -> {
                    toRet.add(channel.getName());
                });
            }
        } else {
            List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(siteId);
            if (CollectionUtils.isNotEmpty(publishingTargets)) {
                publishingTargets.forEach(target -> {
                    toRet.add(target.getRepoBranchName());
                });
            }
        }


        return toRet;
    }

    /**
     * create WcmDeploymentTask
     *
     * @param deployedLabel
     * @param item
     * @return deployment task
     */
    protected DmDeploymentTaskTO createDeploymentTask(String deployedLabel, ContentItemTO item) {
        // otherwise just add as the last task
        DmDeploymentTaskTO task = new DmDeploymentTaskTO();
        task.setInternalName(deployedLabel);
        List<ContentItemTO> taskItems = task.getChildren();
        if (taskItems == null) {
            taskItems = new ArrayList<ContentItemTO>();
            task.setChildren(taskItems);
        }
        taskItems.add(item);
        task.setNumOfChildren(taskItems.size());
        return task;
    }

    /**
     * get a deployed item by the given path. If the item is new, it will be added to the itemsMap
     *
     * @param site
     * @param path
     * @return deployed item
     */
    protected ContentItemTO getDeployedItem(String site, String path) {

        ContentItemTO item = null;
        if (!contentService.contentExists(site, path)) {
            item = contentService.createDummyDmContentItemForDeletedNode(site, path);
            item.setLockOwner("");
        } else {
            item = contentService.getContentItem(site, path, 0);
        }
        return item;

    }

    @Override
    @ValidateParams
    public List<ContentItemTO> getScheduledItems(@ValidateStringParam(name = "site") String site,
                                                 @ValidateStringParam(name = "sort") String sort,
                                                 boolean ascending,
                                                 @ValidateStringParam(name = "subSort") String subSort,
                                                 boolean subAscending,
                                                 @ValidateStringParam(name = "filterType") String filterType)
            throws ServiceLayerException {
        if (StringUtils.isEmpty(sort)) {
            sort = DmContentItemComparator.SORT_EVENT_DATE;
        }
        DmContentItemComparator comparator =
                new DmContentItemComparator(sort, ascending, true, true);
        DmContentItemComparator subComparator =
                new DmContentItemComparator(subSort, subAscending, true, true);
        List<ContentItemTO> items = null;
        items = getScheduledItems(site, comparator, subComparator, filterType);
        return items;
    }

    @SuppressWarnings("unchecked")
    protected List<ContentItemTO> getScheduledItems(String site, DmContentItemComparator comparator,
                                                    DmContentItemComparator subComparator, String filterType) {
        List<ContentItemTO> results = new FastArrayList();
        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        List<PublishRequest> deploying = getScheduledItems(site);
        SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_FORMAT_SCHEDULED);
        List<ContentItemTO> scheduledItems = new ArrayList<ContentItemTO>();
        for (PublishRequest deploymentItem : deploying) {
            Set<String> permissions = securityService.getUserPermissions(site, deploymentItem.getPath(),
                    securityService.getCurrentUser(), Collections.<String>emptyList());
            if (permissions.contains(StudioConstants.PERMISSION_VALUE_PUBLISH)) {
                addScheduledItem(site, deploymentItem.getEnvironment(), deploymentItem.getScheduledDate(), format,
                        deploymentItem.getPath(), deploymentItem.getPackageId(), results, comparator, subComparator,
                        displayPatterns, filterType);
            }
        }
        return results;
    }

    /**
     * add a scheduled item created from the given node to the scheduled items
     * list if the item is not a component or a static asset
     *
     * @param site
     * @param launchDate
     * @param scheduledItems
     * @param comparator
     * @param displayPatterns
     */
    protected void addScheduledItem(String site, String environment, ZonedDateTime launchDate, SimpleDateFormat format,
                                    String path, String packageId, List<ContentItemTO> scheduledItems,
                                    DmContentItemComparator comparator, DmContentItemComparator subComparator,
                                    List<String> displayPatterns, String filterType) {
        try {
            addToScheduledDateList(site, environment, launchDate, format, path, packageId, scheduledItems, comparator,
                    subComparator, displayPatterns, filterType);
            if(!(path.endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE) || path.endsWith(DmConstants.XML_PATTERN))) {
                path = path + FILE_SEPARATOR + DmConstants.INDEX_FILE;
            }
        } catch (ServiceLayerException e) {
            logger.error("failed to read site " + site + " path " + path + ". " + e.getMessage());
        }
    }

    /**
     * add the given node to the scheduled items list
     *
     * @param site
     * @param launchDate
     * @param format
     * @param scheduledItems
     * @param comparator
     * @param subComparator
     * @param displayPatterns
     * @throws ServiceLayerException
     */
    protected void addToScheduledDateList(String site, String environment, ZonedDateTime launchDate,
                                          SimpleDateFormat format, String path, String packageId,
                                          List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                          DmContentItemComparator subComparator, List<String> displayPatterns,
                                          String filterType) throws ServiceLayerException {
        String timeZone = servicesConfig.getDefaultTimezone(site);
        String dateLabel = launchDate.format(DateTimeFormatter.ofPattern(format.toPattern()));
        // add only if the current node is a file (directories are
        // deployed with index.xml)
        // display only if the path matches one of display patterns
        if (ContentUtils.matchesPatterns(path, displayPatterns)) {
            ContentItemTO itemToAdd = contentService.getContentItem(site, path, 0);
            if (dmFilterWrapper.accept(site, itemToAdd, filterType)) {
                itemToAdd.scheduledDate = launchDate;
                itemToAdd.environment = environment;
                itemToAdd.packageId = packageId;
                boolean found = false;
                for (int index = 0; index < scheduledItems.size(); index++) {
                    ContentItemTO currDateItem = scheduledItems.get(index);
                    // if the same date label found, add the content item to
                    // it non-recursively
                    if (currDateItem.name.equals(dateLabel)) {
                        currDateItem.addChild(itemToAdd, subComparator, false);
                        found = true;
                        break;
                        // if the date is after the current date, add a new
                        // date item before it
                        // and add the content item to the new date item
                    } else if (itemToAdd.scheduledDate.compareTo(currDateItem.scheduledDate) < 0) {
                        ContentItemTO dateItem = createDateItem(dateLabel, itemToAdd, comparator, timeZone);
                        scheduledItems.add(index, dateItem);
                        found = true;
                        break;
                    }
                }
                // if not found, add to the end of list
                if (!found) {
                    ContentItemTO dateItem = createDateItem(dateLabel, itemToAdd, comparator, timeZone);
                    scheduledItems.add(dateItem);
                }
            }
        }
    }

    protected ContentItemTO createDateItem(String name, ContentItemTO itemToAdd, DmContentItemComparator comparator,
                                           String timeZone) {
        ContentItemTO dateItem = new ContentItemTO();
        dateItem.name = name;
        dateItem.internalName = name;
        dateItem.eventDate = itemToAdd.scheduledDate;
        dateItem.scheduledDate = itemToAdd.scheduledDate;
        dateItem.timezone = timeZone;
        dateItem.addChild(itemToAdd, comparator, false);
        return dateItem;
    }

    @Override
    @ValidateParams
    public Map<String, List<PublishingChannelTO>> getAvailablePublishingChannelGroups(
            @ValidateStringParam(name = "site") String site,
            @ValidateSecurePathParam(name = "path") String path) {
        List<PublishingChannelTO> channelsTO = !servicesConfig.isStagingEnvironmentEnabled(site) ?
                getAvailablePublishingChannelGroupsForSite(site) : getPublishedEnvironments(site);
        List<PublishingChannelTO> publishChannels = new ArrayList<PublishingChannelTO>();
        List<PublishingChannelTO> updateStatusChannels = new ArrayList<PublishingChannelTO>();
        for (PublishingChannelTO channelTO : channelsTO) {
            if (channelTO.isPublish()) {
                publishChannels.add(channelTO);
            }
            if (channelTO.isUpdateStatus()) {
                updateStatusChannels.add(channelTO);
            }
        }
        Map<String, List<PublishingChannelTO>> result = new HashMap<>();
        result.put("availablePublishChannels", publishChannels);
        result.put("availableUpdateStatusChannels", updateStatusChannels);
        return result;
    }

    protected List<PublishingChannelTO> getPublishedEnvironments(String site) {
        List<PublishingChannelTO> channelTOs = new ArrayList<PublishingChannelTO>();
        Set<String> environments = getAllPublishedEnvironments(site);
        for (String ch : environments) {
            PublishingChannelTO chTO = new PublishingChannelTO();
            chTO.setName(ch);
            chTO.setPublish(true);
            chTO.setUpdateStatus(false);
            channelTOs.add(chTO);
        }
        return channelTOs;
    }

    protected Set<String> getAllPublishedEnvironments(String site) {
        Set<String> publishedEnvironments = new HashSet<String>();
        publishedEnvironments.add(servicesConfig.getLiveEnvironment(site));
        publishedEnvironments.add(servicesConfig.getStagingEnvironment(site));
        return publishedEnvironments;
    }

    protected List<PublishingChannelTO> getAvailablePublishingChannelGroupsForSite(String site) {
        List<PublishingChannelTO> channelTOs = new ArrayList<PublishingChannelTO>();
        List<String> channels = getPublishingChannels(site);
        for (String ch : channels) {
            PublishingChannelTO chTO = new PublishingChannelTO();
            chTO.setName(ch);
            chTO.setPublish(true);
            chTO.setUpdateStatus(false);
            channelTOs.add(chTO);
        }
        return channelTOs;
    }

    protected List<String> getPublishingChannels(String site) {
        List<String> channels = new ArrayList<String>();
        List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);
        Collections.sort(publishingTargets, new Comparator<PublishingTargetTO>() {
            @Override
            public int compare(PublishingTargetTO o1, PublishingTargetTO o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        for (PublishingTargetTO target : publishingTargets) {
            channels.add(target.getDisplayLabel());
        }
        return channels;
    }

    @Override
    @ValidateParams
    public void syncAllContentToPreview(@ValidateStringParam(name = "site") String site, boolean waitTillDone)
            throws ServiceLayerException {
        PreviewEventContext context = new PreviewEventContext(waitTillDone);
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);
    }

    @Override
    @ValidateParams
    public void bulkGoLive(@ValidateStringParam(name = "site") String site,
                           @ValidateStringParam(name = "environment") String environment,
                           @ValidateSecurePathParam(name = "path") String path) throws ServiceLayerException {
        dmPublishService.bulkGoLive(site, environment, path);
    }

    @Override
    @ValidateParams
    public PublishStatus getPublishStatus(@ValidateStringParam(name = "site") String site)
            throws SiteNotFoundException {
        return siteService.getPublishStatus(site);
    }

    @Override
    @ValidateParams
    public ZonedDateTime getLastDeploymentDate(@ValidateStringParam(name = "site") String site,
                                               @ValidateSecurePathParam(name = "path") String path) {
        return deploymentHistoryProvider.getLastDeploymentDate(site, path);
    }

    @Override
    @ValidateParams
    public boolean enablePublishing(@ValidateStringParam(name = "site") String site, boolean enabled)
            throws SiteNotFoundException, AuthenticationException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        if (!securityService.isSiteAdmin(securityService.getCurrentUser(), site)) {
            throw new AuthenticationException();
        }

        boolean toRet = siteService.enablePublishing(site, enabled);
        String message = StringUtils.EMPTY;
        if (enabled) {
            message = studioConfiguration.getProperty(
                    StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STARTED_USER);
        } else {
            message = studioConfiguration.getProperty(
                    StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_USER);
        }
        message = message.replace("{username}", securityService.getCurrentUser()).replace("{datetime}",
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(DATE_PATTERN_WORKFLOW_WITH_TZ)));
        siteService.updatePublishingStatusMessage(site, message);

        SiteFeed siteFeed = siteService.getSite(site);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setSiteId(siteFeed.getId());
        if (enabled) {
            auditLog.setOperation(OPERATION_START_PUBLISHER);
        } else {
            auditLog.setOperation(OPERATION_STOP_PUBLISHER);
        }
        auditLog.setActorId(securityService.getCurrentUser());
        auditLog.setPrimaryTargetId(siteFeed.getSiteId());
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteFeed.getName());
        auditServiceInternal.insertAuditLog(auditLog);

        return toRet;
    }

    @Override
    @ValidateParams
    public void publishCommits(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "environment") String environment,
                               List<String> commitIds, @ValidateStringParam(name = "comment") String comment)
            throws SiteNotFoundException, EnvironmentNotFoundException, CommitNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        environment = resolveEnvironment(site, environment);
        Set<String> environments = servicesConfig.isStagingEnvironmentEnabled(site) ?
                getAllPublishedEnvironments(site) : getAllPublishingEnvironments(site);
        if (!environments.contains(environment)) {
            throw new EnvironmentNotFoundException();
        }
        if (!checkCommitIds(site, commitIds)) {
            throw new CommitNotFoundException();
        }
        logger.debug("Creating publish request items for queue for site " + site + " environment " + environment);
        List<PublishRequest> publishRequests = createCommitItems(site, environment, commitIds,
                ZonedDateTime.now(ZoneOffset.UTC), securityService.getCurrentUser(), comment);
        logger.debug("Insert publish request items to the queue");
        for (PublishRequest request : publishRequests) {
            publishRequestMapper.insertItemForDeployment(request);
        }
        logger.debug("Completed adding commits to publishing queue");
    }

    private boolean checkCommitIds(String site, List<String> commitIds) {
        boolean toRet = true;
        for (String commitId : commitIds) {
            toRet = toRet && contentRepository.commitIdExists(site, commitId);
        }
        return toRet;
    }

    private List<PublishRequest> createCommitItems(String site, String environment, List<String> commitIds,
                                                   ZonedDateTime scheduledDate, String approver, String comment) {
        List<PublishRequest> newItems = new ArrayList<PublishRequest>(commitIds.size());
        String packageId = UUID.randomUUID().toString();
        logger.debug("Get repository operations for each commit id and create publish request items");
        for (String commitId : commitIds) {
            logger.debug("Get repository operations for commit " + commitId);
            List<RepoOperationTO> operations =
                    contentRepository.getOperations(site, commitId + PREVIOUS_COMMIT_SUFFIX, commitId);

            for (RepoOperationTO op : operations) {
                logger.debug("Creating publish request item: ");
                PublishRequest item = new PublishRequest();
                item.setId(++CTED_AUTOINCREMENT);
                item.setSite(site);
                item.setEnvironment(environment);
                item.setScheduledDate(scheduledDate);
                item.setState(PublishRequest.State.READY_FOR_LIVE);
                item.setCommitId(commitId);
                item.setUser(approver);
                item.setPackageId(packageId);
                item.setSubmissionComment(comment);

                switch (op.getOperation()) {
                    case CREATE:
                    case COPY:
                        item.setPath(op.getPath());
                        item.setAction(PublishRequest.Action.NEW);
                        item.setContentTypeClass(contentService.getContentTypeClass(site, op.getPath()));
                        break;

                    case UPDATE:
                        item.setPath(op.getPath());
                        item.setAction(PublishRequest.Action.UPDATE);
                        item.setContentTypeClass(contentService.getContentTypeClass(site, op.getPath()));
                        break;

                    case DELETE:
                        item.setPath(op.getPath());
                        item.setAction(PublishRequest.Action.DELETE);
                        item.setContentTypeClass(contentService.getContentTypeClass(site, op.getPath()));
                        break;

                    case MOVE:
                        item.setPath(op.getMoveToPath());
                        item.setOldPath(op.getPath());
                        item.setAction(PublishRequest.Action.MOVE);
                        item.setContentTypeClass(contentService.getContentTypeClass(site, op.getPath()));
                        break;

                    default:
                        logger.error("Error: Unknown repo operation for site " + site + " operation: " +
                                op.getOperation());
                        continue;
                }
                logger.debug("\tPath: " + item.getPath() + " operation: " + item.getAction());
                newItems.add(item);
            }
        }
        logger.debug("Created " + newItems.size() + " publish request items for queue");
        return newItems;
    }

    @Override
    public void publishItems(String site, String environment, ZonedDateTime schedule, List<String> paths,
                             String submissionComment)
            throws ServiceLayerException, DeploymentException {

        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        Set<String> environements = getAllPublishingEnvironments(site);
        if (!environements.contains(environment)) {
            throw new EnvironmentNotFoundException();
        }
        // get all publishing dependencies
        Set<String> dependencies = dependencyService.calculateDependenciesPaths(site, paths);
        Set<String> allPaths = new HashSet<String>();
        allPaths.addAll(paths);
        allPaths.addAll(dependencies);

        // remove all items from existing workflows
        cancelWorkflowBulk(site, allPaths);

        // send to deployment queue
        List<String> asList = new ArrayList<String>();
        asList.addAll(allPaths);
        String approver = securityService.getCurrentUser();
        boolean scheduledDateIsNow = false;
        if (schedule == null) {
            scheduledDateIsNow = true;
            schedule = ZonedDateTime.now(ZoneOffset.UTC);
        }
        deploy(site, environment, asList, schedule, approver, submissionComment, scheduledDateIsNow);
    }

    @Override
    public void resetStagingEnvironment(String siteId) throws ServiceLayerException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        contentRepository.resetStagingRepository(siteId);
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
        this.dmFilterWrapper = dmFilterWrapper;
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

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public DmPublishService getDmPublishService() {
        return dmPublishService;
    }

    public void setDmPublishService(DmPublishService dmPublishService) {
        this.dmPublishService = dmPublishService;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public DeployContentToEnvironmentStore getDeployContentToEnvironmentStoreJob() {
        return deployContentToEnvironmentStoreJob;
    }

    public void setDeployContentToEnvironmentStoreJob(
            DeployContentToEnvironmentStore deployContentToEnvironmentStoreJob) {
        this.deployContentToEnvironmentStoreJob = deployContentToEnvironmentStoreJob;
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public DeploymentHistoryProvider getDeploymentHistoryProvider() {
        return deploymentHistoryProvider;
    }

    public void setDeploymentHistoryProvider(DeploymentHistoryProvider deploymentHistoryProvider) {
        this.deploymentHistoryProvider = deploymentHistoryProvider;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public PublishRequestMapper getPublishRequestMapper() {
        return publishRequestMapper;
    }

    public void setPublishRequestMapper(PublishRequestMapper publishRequestMapper) {
        this.publishRequestMapper = publishRequestMapper;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }
}

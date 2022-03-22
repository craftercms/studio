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

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.FilenameUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.PublishRequest;
import org.craftercms.studio.api.v1.dal.PublishRequestMapper;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.CommitNotFoundException;
import org.craftercms.studio.api.v1.exception.EnvironmentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.PublishRequestDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.Workflow;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.event.workflow.WorkflowEvent;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.workflow.internal.WorkflowServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_ASSET;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_COMPONENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_START_PUBLISHER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_STOP_PUBLISHER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.dal.ItemState.DELETE_OFF_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.DELETE_ON_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.DESTINATION;
import static org.craftercms.studio.api.v2.dal.ItemState.IN_WORKFLOW;
import static org.craftercms.studio.api.v2.dal.ItemState.SCHEDULED;
import static org.craftercms.studio.api.v2.dal.ItemState.isNew;
import static org.craftercms.studio.api.v2.dal.PublishStatus.QUEUED;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.dal.PublishStatus.STOPPED;
import static org.craftercms.studio.api.v2.dal.Workflow.STATE_APPROVED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_PUBLISHED_LIVE;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.PREVIOUS_COMMIT_SUFFIX;

/**
 */
public class DeploymentServiceImpl implements DeploymentService, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    private static int CTED_AUTOINCREMENT = 0;

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected DependencyService dependencyService;
    protected DmFilterWrapper dmFilterWrapper;
    protected SiteService siteService;
    protected ContentRepository contentRepository;
    protected DmPublishService dmPublishService;
    protected SecurityService securityService;
    protected NotificationService notificationService;
    protected StudioConfiguration studioConfiguration;
    protected PublishRequestMapper publishRequestMapper;
    protected AuditServiceInternal auditServiceInternal;
    protected org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2;
    protected ItemServiceInternal itemServiceInternal;
    protected WorkflowServiceInternal workflowServiceInternal;
    protected UserServiceInternal userServiceInternal;
    protected PublishingManager publishingManager;
    protected PublishRequestDAO publishRequestDAO;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    protected ApplicationContext applicationContext;

    @Override
    @ValidateParams
    public void deploy(@ValidateStringParam(name = "site") String site,
                       @ValidateStringParam(name = "environment") String environment, List<String> paths,
                       ZonedDateTime scheduledDate, @ValidateStringParam(name = "approver") String approver,
                       @ValidateStringParam(name = "submissionComment") String submissionComment,
                       final boolean scheduleDateNow)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {

        if (scheduledDate != null && scheduledDate.isAfter(DateUtils.getCurrentTime())) {
            itemServiceInternal.updateStateBitsBulk(site, paths, SCHEDULED.value, 0);
        }
        itemServiceInternal.updateStateBitsBulk(site, paths, 0, IN_WORKFLOW.value);
        String liveEnvironment = StringUtils.EMPTY;
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            liveEnvironment = servicesConfig.getLiveEnvironment(site);
        }
        boolean isLive = false;
        if (StringUtils.isEmpty(liveEnvironment)) {
            liveEnvironment = studioConfiguration.getProperty(REPO_PUBLISHED_LIVE);
        }
        if (liveEnvironment.equals(environment)) {
            isLive = true;
        }
        if (isLive) {
            itemServiceInternal.updateStateBitsBulk(site, paths, DESTINATION.value, 0);
        } else {
            itemServiceInternal.updateStateBitsBulk(site, paths, 0, DESTINATION.value);
        }

        List<String> newPaths = new ArrayList<String>();
        List<String> updatedPaths = new ArrayList<String>();
        List<String> movedPaths = new ArrayList<String>();

        Map<String, List<String>> groupedPaths = new HashMap<String, List<String>>();

        for (String p : paths) {
            Item item = itemServiceInternal.getItem(site, p);
            boolean isFolder = StringUtils.equals(item.getSystemType(), CONTENT_TYPE_FOLDER);
            if (isFolder) {
                logger.debug("Content item at path " + p + " for site " + site +
                        " is folder and will not be added to publishing queue.");
            } else {
                if (isNew(item.getState())) {
                    newPaths.add(p);
                } else if (StringUtils.isNotEmpty(item.getPreviousPath())) {
                    movedPaths.add(p);
                } else {
                    updatedPaths.add(p);
                }
            }
        }

        groupedPaths.put(PublishRequest.Action.NEW, newPaths);
        groupedPaths.put(PublishRequest.Action.MOVE, movedPaths);
        groupedPaths.put(PublishRequest.Action.UPDATE, updatedPaths);

        List<PublishRequest> items = createItems(site, environment, groupedPaths, scheduledDate, approver,
                submissionComment);
        for (PublishRequest item : items) {
            retryingDatabaseOperationFacade.insertItemForDeployment(item);
        }
        itemServiceInternal.setSystemProcessingBulk(site, paths, false);

        // We need to pick up this on Inserting , not on execution!
        try {
            sendContentApprovalEmail(items, scheduleDateNow);
        } catch(Exception errNotify) {
            logger.error("Error sending approval notification ", errNotify);
        }
        try {
            siteService.updatePublishingStatus(site, QUEUED);
        } catch (SiteNotFoundException e) {
            logger.error("Error updating publishing status for site " + site);
        }
        applicationContext.publishEvent(new WorkflowEvent(securityService.getAuthentication(), site));
    }

    protected void sendContentApprovalEmail(List<PublishRequest> itemList, boolean scheduleDateNow)
            throws ServiceLayerException, UserNotFoundException {
        for (PublishRequest listItem : itemList) {
            Workflow workflow = workflowServiceInternal.getWorkflowEntry(listItem.getSite(), listItem.getPath(),
                    listItem.getPackageId());
            if (workflow != null) {
                if (workflow.getNotifySubmitter() == 1) {
                    // found the first item that needs to be sent
                    User submitter = userServiceInternal.getUserByIdOrUsername(workflow.getSubmitterId(), null);
                    notificationService.notifyContentApproval(listItem.getSite(), submitter.getUsername(),
                            getPathRelativeToSite(itemList), listItem.getUser(),
                            // Null == now, anything else is scheduled
                            scheduleDateNow ? null : listItem.getScheduledDate());
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
                                             ZonedDateTime scheduledDate, String approver, String submissionComment)
            throws ServiceLayerException, UserNotFoundException {
        List<PublishRequest> newItems = new ArrayList<PublishRequest>();

        String packageId = UUID.randomUUID().toString();

        Map<String, Object> params = null;
        for (String action : paths.keySet()) {
            for (String path : paths.get(action)) {
                PublishRequest item = new PublishRequest();
                Item it = itemServiceInternal.getItem(site, path);
                if (it != null) {
                    params = new HashMap<String, Object>();
                    params.put("site_id", site);
                    params.put("environment", environment);
                    params.put("state", PublishRequest.State.READY_FOR_LIVE);
                    params.put("path", path);
                    params.put("commitId", it.getCommitId());
                    if (publishRequestMapper.checkItemQueued(params) > 0) {
                        logger.info("Path " + path + " with commit ID " + it.getCommitId() +
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
                        if (StringUtils.isNotEmpty(it.getPreviousPath())) {
                            String oldPath = it.getPreviousPath();
                            item.setOldPath(oldPath);
                        }
                        String commitId = it.getCommitId();
                        if (StringUtils.isNotEmpty(commitId) && contentRepositoryV2.commitIdExists(site, commitId)) {
                            item.setCommitId(commitId);
                        } else {
                            if (StringUtils.isNotEmpty(commitId)) {
                                logger.warn("Commit ID is NULL for content " + path +
                                        ". Was the git repo reset at some point?" );
                            } else {
                                logger.warn("Commit ID " + commitId + " does not exist for content " + path +
                                        ". Was the git repo reset at some point?" );
                            }
                            logger.info("Publishing content from HEAD for " + path);
                            item.setCommitId(contentRepository.getRepoLastCommitId(site));
                        }

                        String contentTypeClass = contentService.getContentTypeClass(site, path);
                        item.setContentTypeClass(contentTypeClass);
                        item.setUser(approver);
                        item.setSubmissionComment(submissionComment);
                        item.setPackageId(packageId);
                        newItems.add(item);
                    }


                    User reviewer = userServiceInternal.getUserByIdOrUsername(-1, securityService.getCurrentUser());
                    Workflow workflow = workflowServiceInternal.getWorkflowEntryForApproval(it.getId());
                    boolean insert = false;
                    if (Objects.isNull(workflow)) {
                        workflow = new Workflow();
                        workflow.setItemId(it.getId());
                        insert = true;
                    }
                    workflow.setState(STATE_APPROVED);
                    workflow.setTargetEnvironment(environment);
                    if (scheduledDate != null && scheduledDate.isAfter(DateUtils.getCurrentTime())) {
                        workflow.setSchedule(scheduledDate);
                    }
                    workflow.setReviewerComment(submissionComment);
                    workflow.setReviewerId(reviewer.getId());
                    workflow.setPublishingPackageId(packageId);
                    if (insert) {
                        workflowServiceInternal.insertWorkflow(workflow);
                    } else {
                        workflowServiceInternal.updateWorkflow(workflow);
                    }
                }
            }
        }
        return newItems;
    }

    @Override
    @ValidateParams
    public void delete(@ValidateStringParam(name = "site") String site, List<String> paths,
                       @ValidateStringParam(name = "approver") String approver, ZonedDateTime scheduledDate,
                       String submissionComment)
            throws DeploymentException, ServiceLayerException, UserNotFoundException {
        if (scheduledDate != null && scheduledDate.isAfter(DateUtils.getCurrentTime())) {
            itemServiceInternal.updateStateBitsBulk(site, paths, DELETE_ON_MASK, DELETE_OFF_MASK);
        }
        Set<String> environments = getAllPublishedEnvironments(site);
        for (String environment : environments) {
            List<PublishRequest> items =
                    createDeleteItems(site, environment, paths, approver, scheduledDate, submissionComment);
            for (PublishRequest item : items) {
                retryingDatabaseOperationFacade.insertItemForDeployment(item);
            }
        }
        itemServiceInternal.setSystemProcessingBulk(site, paths, false);
        try {
            siteService.updatePublishingStatus(site, QUEUED);
        } catch (SiteNotFoundException e) {
            logger.error("Error updating publishing status for site " + site);
        }
    }

    private List<PublishRequest> createDeleteItems(String site, String environment, List<String> paths,
                                                   String approver, ZonedDateTime scheduledDate,
                                                   String submissionComment)
            throws ServiceLayerException, UserNotFoundException {
        List<PublishRequest> newItems = new ArrayList<PublishRequest>(paths.size());
        String packageId = UUID.randomUUID().toString();
        for (String path : paths) {
            if (contentService.contentExists(site, path)) {
                ContentItemTO contentItem = contentService.getContentItem(site, path, 0);
                if (!contentItem.isFolder()) {
                    PublishRequest item = new PublishRequest();
                    Item it = itemServiceInternal.getItem(site, path);
                    item.setId(++CTED_AUTOINCREMENT);
                    item.setSite(site);
                    item.setEnvironment(environment);
                    item.setPath(path);
                    item.setScheduledDate(scheduledDate);
                    item.setState(PublishRequest.State.READY_FOR_LIVE);
                    item.setAction(PublishRequest.Action.DELETE);
                    if (it != null) {
                        if (StringUtils.isNotEmpty(it.getPreviousPath())) {
                            String oldPath = it.getPreviousPath();
                            item.setOldPath(oldPath);
                        }
                        String commitId = it.getCommitId();
                        if (StringUtils.isNotEmpty(commitId) && contentRepositoryV2.commitIdExists(site, commitId)) {
                            item.setCommitId(commitId);
                        } else {
                            if (StringUtils.isNotEmpty(commitId)) {
                                logger.warn("Commit ID is NULL for content " + path +
                                        ". Was the git repo reset at some point?" );
                            } else {
                                logger.warn("Commit ID " + commitId + " does not exist for content " + path +
                                        ". Was the git repo reset at some point?" );
                            }
                            logger.info("Publishing content from HEAD for " + path);
                            item.setCommitId(contentRepository.getRepoLastCommitId(site));
                        }
                    }
                    String contentTypeClass = contentService.getContentTypeClass(site, path);
                    item.setContentTypeClass(contentTypeClass);
                    item.setUser(approver);
                    item.setPackageId(packageId);
                    item.setSubmissionComment(submissionComment);
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
                    newItems.addAll(createDeleteItems(site, environment, childPaths, approver, scheduledDate,
                            submissionComment));
                    deleteFolder(site, path, approver);
                }
            }
        }
        return newItems;
    }

    private void deleteFolder(String site, String path, String user)
            throws ServiceLayerException, UserNotFoundException {
        String folderPath = path.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        SiteFeed siteFeed = siteService.getSite(site);
        if (contentService.contentExists(site, path)) {
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                if (path.endsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                    contentService.deleteContent(site, path, true, user);
                    itemServiceInternal.deleteItemForFolder(siteFeed.getId(), folderPath);
                    String parentPath = ContentUtils.getParentUrl(path);
                    deleteFolder(site, parentPath, user);
                } else {
                    contentService.deleteContent(site, path, true, user);
                    itemServiceInternal.deleteItemForFolder(siteFeed.getId(), folderPath);
                }
            }
        } else {
            itemServiceInternal.deleteItemForFolder(siteFeed.getId(), folderPath);
        }
    }

    @Override
    @ValidateParams
    public void deleteDeploymentDataForSite(@ValidateStringParam(name = "site") final String site) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        retryingDatabaseOperationFacade.deleteDeploymentDataForSite(params);
    }

    @Override
    @ValidateParams
    public List<org.craftercms.studio.api.v2.dal.PublishRequest> getScheduledItems(
            @ValidateStringParam(name = "site") String site, String filterType) {
        String contentTypeClass = null;
        switch (filterType) {
            case CONTENT_TYPE_PAGE:
            case CONTENT_TYPE_COMPONENT:
            case CONTENT_TYPE_ASSET:
                contentTypeClass = filterType;
                break;
            default:
                contentTypeClass = null;
                break;
        }
        return publishRequestDAO.getScheduledItems(site, PublishRequest.State.READY_FOR_LIVE, contentTypeClass,
                DateUtils.getCurrentTime());
    }

    @Override
    @ValidateParams
    public void cancelWorkflow(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "path") String path) throws DeploymentException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELLED);
        params.put("now", DateUtils.getCurrentTime());
        retryingDatabaseOperationFacade.cancelWorkflow(params);
    }

    @Override
    @ValidateParams
    public void cancelWorkflowBulk(@ValidateStringParam(name = "site") String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("paths", paths);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELLED);
        params.put("now", DateUtils.getCurrentTime());
        retryingDatabaseOperationFacade.cancelWorkflowBulk(params);
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
        List<org.craftercms.studio.api.v2.dal.PublishRequest> deploying = getScheduledItems(site, filterType);
        for (org.craftercms.studio.api.v2.dal.PublishRequest deploymentItem : deploying) {
            Set<String> permissions = securityService.getUserPermissions(site, deploymentItem.getPath(),
                    securityService.getCurrentUser(), Collections.<String>emptyList());
            if (permissions.contains(StudioConstants.PERMISSION_VALUE_PUBLISH)) {
                addScheduledItem(site, deploymentItem.getEnvironment(), deploymentItem.getScheduledDate(),
                        deploymentItem.getPath(), deploymentItem.getPackageId(), results, comparator, subComparator,
                        displayPatterns);
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
    protected void addScheduledItem(String site, String environment, ZonedDateTime launchDate,
                                    String path, String packageId, List<ContentItemTO> scheduledItems,
                                    DmContentItemComparator comparator, DmContentItemComparator subComparator,
                                    List<String> displayPatterns) {
        try {
            addToScheduledDateList(site, environment, launchDate, path, packageId, scheduledItems, comparator,
                    subComparator, displayPatterns);
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
     * @param scheduledItems
     * @param comparator
     * @param subComparator
     * @param displayPatterns
     * @throws ServiceLayerException
     */
    protected void addToScheduledDateList(String site, String environment, ZonedDateTime launchDate, String path,
                                          String packageId, List<ContentItemTO> scheduledItems,
                                          DmContentItemComparator comparator, DmContentItemComparator subComparator,
                                          List<String> displayPatterns)
            throws ServiceLayerException {
        String timeZone = servicesConfig.getDefaultTimezone(site);
        String dateLabel =
                launchDate.withZoneSameInstant(ZoneId.of(timeZone)).format(ISO_OFFSET_DATE_TIME);
        // add only if the current node is a file (directories are
        // deployed with index.xml)
        // display only if the path matches one of display patterns
        if (ContentUtils.matchesPatterns(path, displayPatterns)) {
            ContentItemTO itemToAdd = contentService.getContentItem(site, path, 0);
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

    protected Set<String> getAllPublishedEnvironments(String site) {
        Set<String> publishedEnvironments = new LinkedHashSet<String>();
        publishedEnvironments.add(servicesConfig.getLiveEnvironment(site));
        if (servicesConfig.isStagingEnvironmentEnabled(site)) {
            publishedEnvironments.add(servicesConfig.getStagingEnvironment(site));
        }
        return publishedEnvironments;
    }

    @Override
    @ValidateParams
    public void bulkGoLive(@ValidateStringParam(name = "site") String site,
                           @ValidateStringParam(name = "environment") String environment,
                           @ValidateSecurePathParam(name = "path") String path,
                           String comment) throws ServiceLayerException {
        dmPublishService.bulkGoLive(site, environment, path, comment);
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
        String status;
        if (enabled) {
            logger.info("Publishing started for site {0}", site);
            if (publishingManager.isPublishingQueueEmpty(site)) {
                status = READY;
            } else {
                status = QUEUED;
            }

        } else {
            logger.info("Publishing stopped for site {0}", site);
            status = STOPPED;
        }
        siteService.updatePublishingStatus(site, status);

        SiteFeed siteFeed = siteService.getSite(site);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setSiteId(siteFeed.getId());
        if (enabled) {
            logger.info("Publishing started for site {0}", site);
            auditLog.setOperation(OPERATION_START_PUBLISHER);
        } else {
            logger.info("Publishing stopped for site {0}", site);
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
        Set<String> environments = getAllPublishedEnvironments(site);
        if (!environments.contains(environment)) {
            throw new EnvironmentNotFoundException();
        }
        if (!checkCommitIds(site, commitIds)) {
            throw new CommitNotFoundException();
        }
        logger.debug("Creating publish request items for queue for site " + site + " environment " + environment);
        List<PublishRequest> publishRequests = createCommitItems(site, environment, commitIds,
                DateUtils.getCurrentTime(), securityService.getCurrentUser(), comment);
        logger.debug("Insert publish request items to the queue");
        for (PublishRequest request : publishRequests) {
            retryingDatabaseOperationFacade.insertItemForDeployment(request);
        }
        logger.debug("Completed adding commits to publishing queue");
    }

    private boolean checkCommitIds(String site, List<String> commitIds) {
        boolean toRet = true;
        for (String commitId : commitIds) {
            if (StringUtils.isNotEmpty(commitId)) {
                toRet = toRet && contentRepositoryV2.commitIdExists(site, commitId);
            }
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
            List<RepoOperation> operations =
                    contentRepositoryV2.getOperations(site, commitId + PREVIOUS_COMMIT_SUFFIX, commitId);

            for (RepoOperation op : operations) {
                if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(op.getMoveToPath())) ||
                        ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(op.getPath()))) {
                    continue;
                }
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

                switch (op.getAction()) {
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
                                op.getAction());
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
            throws ServiceLayerException, DeploymentException, UserNotFoundException {

        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        Set<String> environements = getAllPublishedEnvironments(site);
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
        List<String> asList = new ArrayList<>(allPaths);
        String approver = securityService.getCurrentUser();
        boolean scheduledDateIsNow = false;
        if (schedule == null) {
            scheduledDateIsNow = true;
            schedule = DateUtils.getCurrentTime();
        }
        deploy(site, environment, asList, schedule, approver, submissionComment, scheduledDateIsNow);
    }

    @Override
    public void resetStagingEnvironment(String siteId) throws ServiceLayerException, CryptoException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        contentRepository.resetStagingRepository(siteId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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


    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
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

    public org.craftercms.studio.api.v2.repository.ContentRepository getContentRepositoryV2() {
        return contentRepositoryV2;
    }

    public void setContentRepositoryV2(org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2) {
        this.contentRepositoryV2 = contentRepositoryV2;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
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

    public PublishingManager getPublishingManager() {
        return publishingManager;
    }

    public void setPublishingManager(PublishingManager publishingManager) {
        this.publishingManager = publishingManager;
    }

    public PublishRequestDAO getPublishRequestDAO() {
        return publishRequestDAO;
    }

    public void setPublishRequestDAO(PublishRequestDAO publishRequestDAO) {
        this.publishRequestDAO = publishRequestDAO;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}

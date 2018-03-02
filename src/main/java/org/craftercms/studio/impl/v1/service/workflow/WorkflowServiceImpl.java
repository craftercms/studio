/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.service.workflow;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyRules;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.service.workflow.context.RequestContext;
import org.craftercms.studio.api.v1.service.workflow.context.RequestContextBuilder;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.DmError;
import org.craftercms.studio.api.v1.to.DmFolderConfigTO;
import org.craftercms.studio.api.v1.to.GoLiveDeleteCandidates;
import org.craftercms.studio.api.v1.to.GoLiveQueue;
import org.craftercms.studio.api.v1.to.GoLiveQueueChildFilter;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreGoLiveOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreScheduleDeleteOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreScheduleOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.PreSubmitDeleteOperation;
import org.craftercms.studio.impl.v1.service.workflow.operation.SubmitLifeCycleOperation;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.GoLiveQueueOrganizer;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.WORKFLOW_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED;

/**
 * workflow service implementation
 */
public class WorkflowServiceImpl implements WorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

    protected enum Operation {
        GO_LIVE, DELETE,
        SUBMIT_TO_GO_LIVE,
        REJECT,
    }

    protected String JSON_KEY_ITEMS = "items";
    protected String JSON_KEY_SCHEDULED_DATE = "scheduledDate";
    protected String JSON_KEY_IS_NOW = "now";
    protected String JSON_KEY_PUBLISH_CHANNEL = "publishChannel";
    protected String JSON_KEY_STATUS_SET = "status";
    protected String JSON_KEY_STATUS_MESSAGE = "message";
    protected String JSON_KEY_SUBMISSION_COMMENT = "submissionComment";
    protected String JSON_KEY_URI = "uri";
    protected String JSON_KEY_DELETED = "deleted";
    protected String JSON_KEY_SUBMITTED_FOR_DELETION = "submittedForDeletion";
    protected String JSON_KEY_SUBMITTED = "submitted";
    protected String JSON_KEY_IN_PROGRESS = "inProgress";
    protected String JSON_KEY_IN_REFERENCE = "reference";
    protected String JSON_KEY_COMPONENTS = "components";
    protected String JSON_KEY_DOCUMENTS = "documents";
    protected String JSON_KEY_ASSETS = "assets";
    protected String JSON_KEY_RENDERING_TEMPLATES = "renderingTemplates";
    protected String JSON_KEY_DELETED_ITEMS = "deletedItems";
    protected String JSON_KEY_CHILDREN = "children";
    protected String JSON_KEY_SEND_EMAIL = "sendEmail";
    protected String JSON_KEY_USER = "user";
    protected String JSON_KEY_REASON = "reason";
    public static final String COMPLETE_SUBMIT_TO_GO_LIVE_MSG = "submitToGoLive";

    @Override
    @ValidateParams
    public ResultTO submitToGoLive(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "username") String username, String request) throws ServiceException {
        return submitForApproval(site, username, request, false);
    }

    @SuppressWarnings("unchecked")
    protected ResultTO submitForApproval(final String site, String submittedBy, final String request, final boolean delete) throws ServiceException {
        RequestContext requestContext = RequestContextBuilder.buildSubmitContext(site, submittedBy);
        ResultTO result = new ResultTO();
        try {
            SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            JSONObject requestObject = JSONObject.fromObject(request);
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            int length = items.size();
            if (length > 0) {
                for (int index = 0; index < length; index++) {
                    objectStateService.setSystemProcessing(site, items.optString(index), true);
                }
            }
            boolean isNow = (requestObject.containsKey(JSON_KEY_IS_NOW)) ? requestObject.getBoolean(JSON_KEY_IS_NOW) : false;
            ZonedDateTime scheduledDate = null;
            if (!isNow) {
                scheduledDate = (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) ? getScheduledDate(site, format, requestObject.getString(JSON_KEY_SCHEDULED_DATE)) : null;
            }
            boolean sendEmail = (requestObject.containsKey(JSON_KEY_SEND_EMAIL)) ? requestObject.getBoolean(JSON_KEY_SEND_EMAIL) : false;

            String submissionComment = (requestObject != null && requestObject.containsKey(JSON_KEY_SUBMISSION_COMMENT)) ? requestObject.getString(JSON_KEY_SUBMISSION_COMMENT) : null;
            // TODO: check scheduled date to make sure it is not null when isNow
            // = true and also it is not past


            String schDate = null;
            if (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                schDate = requestObject.getString(JSON_KEY_SCHEDULED_DATE);
            }
            if (length > 0) {
                List<DmDependencyTO> submittedItems = new ArrayList<DmDependencyTO>();
                for (int index = 0; index < length; index++) {
                    String stringItem = items.optString(index);
                    DmDependencyTO submittedItem = getSubmittedItem(site, stringItem, format, schDate, null);
                    String user = submittedBy;
                    submittedItems.add(submittedItem);
                    if (delete) {
                        submittedItem.setSubmittedForDeletion(true);
                    }
                }
                submittedItems.addAll(addDependenciesForSubmitForApproval(site, submittedItems, format, schDate));
                List<String> submittedPaths = new ArrayList<String>();
                for (DmDependencyTO goLiveItem : submittedItems) {
                    submittedPaths.add(goLiveItem.getUri());
                    objectStateService.setSystemProcessing(site, goLiveItem.getUri(), true);
                    DependencyRules rule = new DependencyRules(site);
                    rule.setObjectStateService(objectStateService);
                    rule.setContentService(contentService);
                    Set<DmDependencyTO> depSet = rule.applySubmitRule(goLiveItem);
                    for (DmDependencyTO dep : depSet) {
                        submittedPaths.add(dep.getUri());
                        objectStateService.setSystemProcessing(site, dep.getUri(), true);
                    }
                }
                List<DmError> errors = submitToGoLive(submittedItems, scheduledDate, sendEmail, delete, requestContext, submissionComment);
                result.setSuccess(true);
                result.setStatus(200);
                result.setMessage(notificationService.getNotificationMessage(site, NotificationMessageType
                            .CompleteMessages,COMPLETE_SUBMIT_TO_GO_LIVE_MSG,Locale.ENGLISH));
                for (String relativePath : submittedPaths) {
                    objectStateService.setSystemProcessing(site, relativePath, false);
                }
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            logger.error("Error while submitting content for approval.", e);
        }
        return result;

    }

    protected List<DmError> submitToGoLive(List<DmDependencyTO> submittedItems, ZonedDateTime scheduledDate, boolean sendEmail, boolean submitForDeletion, RequestContext requestContext, String submissionComment) throws ServiceException {
        List<DmError> errors = new ArrayList<DmError>();
        String site = requestContext.getSite();
        String submittedBy = requestContext.getUser();
        for (DmDependencyTO submittedItem : submittedItems) {
            try {
                DependencyRules rule = new DependencyRules(site);
                rule.setContentService(contentService);
                rule.setObjectStateService(objectStateService);
                submitThisAndReferredComponents(submittedItem, site, scheduledDate, sendEmail, submitForDeletion, submittedBy, rule, submissionComment);
                List<DmDependencyTO> children = submittedItem.getChildren();
                if (children != null && !submitForDeletion) {
                    for (DmDependencyTO child : children) {
                        if (!child.isReference()) {
                            submitThisAndReferredComponents(child, site, scheduledDate, sendEmail, submitForDeletion, submittedBy, rule, submissionComment);
                        }
                    }
                }
            } catch (ContentNotFoundException e) {
                errors.add(new DmError(site, submittedItem.getUri(), e));
            }
        }
        notificationService.notifyApprovesContentSubmission(site,null,getDeploymentPaths(submittedItems),submittedBy,scheduledDate,
                submitForDeletion,submissionComment,Locale.ENGLISH);
        return errors;
    }

    private List<String> getDeploymentPaths(final List<DmDependencyTO> submittedItems) {
        List<String> paths=new ArrayList<>(submittedItems.size());
        for (DmDependencyTO submittedItem : submittedItems) {
            paths.add(submittedItem.getUri());
        }
        return paths;
    }

    protected void submitThisAndReferredComponents(DmDependencyTO submittedItem, String site, ZonedDateTime scheduledDate,
                                                   boolean sendEmail, boolean submitForDeletion, String submittedBy,
                                                   DependencyRules rule, String submissionComment) throws
                                                   ServiceException {
        doSubmit(site, submittedItem, scheduledDate, sendEmail, submitForDeletion, submittedBy, true,
            submissionComment);
        Set<DmDependencyTO> stringSet;

        if (submitForDeletion) {
            stringSet = rule.applyDeleteDependencyRule(submittedItem);
        } else {
            stringSet = rule.applySubmitRule(submittedItem);
        }

        for (DmDependencyTO s : stringSet) {
            ContentItemTO contentItem = contentService.getContentItem(site, s.getUri());
            boolean lsendEmail = true;
            boolean lnotifyAdmin = true;
            lsendEmail = sendEmail && ((!contentItem.isDocument() && !contentItem.isComponent() && !contentItem
                .isAsset()));
            lnotifyAdmin = (!contentItem.isDocument() && !contentItem.isComponent() && !contentItem.isAsset());
            // notify admin will always be true, unless for dependent document/banner/other-files
            doSubmit(site, s, scheduledDate, lsendEmail, submitForDeletion, submittedBy, lnotifyAdmin, submissionComment);
        }
    }

    protected void doSubmit(final String site, final DmDependencyTO dependencyTO, final ZonedDateTime scheduledDate, final boolean sendEmail, final boolean submitForDeletion, final String user, final boolean notifyAdmin, final String submissionComment) throws ServiceException {
        //first remove from workflow
        removeFromWorkflow(site, dependencyTO.getUri(), true);
        ContentItemTO item = contentService.getContentItem(site, dependencyTO.getUri());

        Map<String, Object> properties = new HashMap<>();
        properties.put(ItemMetadata.PROP_SUBMITTED_BY, user);
        properties.put(ItemMetadata.PROP_SEND_EMAIL, sendEmail? 1 : 0);
        properties.put(ItemMetadata.PROP_SUBMITTED_FOR_DELETION, submitForDeletion? 1 : 0);
        properties.put(ItemMetadata.PROP_SUBMISSION_COMMENT, submissionComment);

        if (null == scheduledDate) {
            properties.put(ItemMetadata.PROP_LAUNCH_DATE, null);
        } else {
            properties.put(ItemMetadata.PROP_LAUNCH_DATE, scheduledDate);
        }
        if (!objectMetadataManager.metadataExist(site, dependencyTO.getUri())) {
            objectMetadataManager.insertNewObjectMetadata(site, dependencyTO.getUri());
        }
        objectMetadataManager.setObjectMetadata(site, dependencyTO.getUri(), properties);
        if (scheduledDate != null) {
            objectStateService.transition(site, item, TransitionEvent.SUBMIT_WITH_WORKFLOW_SCHEDULED);
        } else {
            objectStateService.transition(site, item, TransitionEvent.SUBMIT_WITH_WORKFLOW_UNSCHEDULED);
        }
    }

    @Override
    @ValidateParams
    public void submitToGoLive(@ValidateStringParam(name = "site") String site, List<String> paths, ZonedDateTime scheduledDate, boolean sendApprovedNotice, @ValidateStringParam(name = "submitter") String submitter) {
		/*
		// this needs to be gutted an re-written as workflow handlers that rely on services like dependency, state, content repository
		// that use the appropriate DAL objects.  Now is not the time to pull the thread on that sweater :-/
		*/
    }

    @Override
    @ValidateParams
    public Map<String, Object> getGoLiveItems(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "sort") String sort, boolean ascending) throws ServiceException {
        DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, false, false);
        List<ContentItemTO> items = getGoLiveItems(site, comparator);

        int total = 0;
        if (items != null) {
            for (ContentItemTO item : items) {
                total += item.getNumOfChildren();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put(StudioConstants.PROPERTY_TOTAL, total);
        result.put(StudioConstants.PROPERTY_SORTED_BY, sort);
        result.put(StudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
        result.put(StudioConstants.PROPERTY_DOCUMENTS, items);
        return result;
    }

    protected List<ContentItemTO> getGoLiveItems(final String site, final DmContentItemComparator comparator) throws ServiceException {
        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        List<ContentItemTO> categoryItems = getCategoryItems(site);
        GoLiveQueue queue = new GoLiveQueue();
        fillQueue(site, queue, null);

        Set<ContentItemTO> queueItems = queue.getQueue();
        ContentItemTO.ChildFilter childFilter = new GoLiveQueueChildFilter(queue);
        GoLiveQueueOrganizer goLiveQueueOrganizer = new GoLiveQueueOrganizer(contentService, childFilter);
        for (ContentItemTO queueItem : queueItems) {
            if (queueItem.getLastEditDate() != null) {
                queueItem.setEventDate(queueItem.getLastEditDate());
            }
            goLiveQueueOrganizer.addToGoLiveItems(site, queueItem, categoryItems, comparator, false, displayPatterns);
        }
        return categoryItems;
    }

    /**
     * get the top category items that to be displayed in UI
     *
     * @param site
     */
    protected List<ContentItemTO> getCategoryItems(final String site) {
        String siteRootPrefix = servicesConfig.getRootPrefix(site);
        List<ContentItemTO> categories = new ArrayList<>();
        List<DmFolderConfigTO> folders = servicesConfig.getFolders(site);

        for (DmFolderConfigTO folder : folders) {
            String uri = (folder.isAttachRootPrefix()) ? siteRootPrefix + folder.getPath() : folder.getPath();
            // if the flag to read direct children is set to true, get direct
            // child folders and add them as categories
            if (folder.isReadDirectChildren()) {
                ContentItemTO rootItem = contentService.getContentItemTree(site, siteRootPrefix + folder.getPath(), 1);
                if (rootItem != null) {
                    if (rootItem.children != null) {
                        for (ContentItemTO childItem : rootItem.children) {
                            categories.add(childItem);
                        }
                    }
                    categories.add(rootItem);
                }
            } else {
                ContentItemTO categoryItem = new ContentItemTO();
                String timeZone = servicesConfig.getDefaultTimezone(site);
                categoryItem.setTimezone(timeZone);
                categoryItem.setName(folder.getName());
                categoryItem.setInternalName(folder.getName());
                categoryItem.setUri(uri);
                categoryItem.setPath(uri);
                categoryItem.setCategoryRoot(uri);
                categories.add(categoryItem);
            }
        }
        return categories;
    }

    @Override
    @ValidateParams
    public void fillQueue(@ValidateStringParam(name = "site") String site, GoLiveQueue goLiveQueue, GoLiveQueue inProcessQueue) throws ServiceException {
        List<ItemState> changeSet = objectStateService.getSubmittedItems(site);
        // TODO: implement list changed all

        // the category item to add all other items that do not belong to
        // regular categories specified in the configuration
        if (changeSet != null) {
            // add all content items from each task if task is the review task
            for (ItemState state : changeSet) {
                try {
                    if (contentService.contentExists( state.getSite(), state.getPath())) {
                        ContentItemTO item = contentService.getContentItem(state.getSite(), state.getPath(), 0);
                        Set<String> permissions = securityService.getUserPermissions(site, item.getUri(), securityService.getCurrentUser(), Collections.<String>emptyList());
                        if (permissions.contains(StudioConstants.PERMISSION_VALUE_PUBLISH)) {
                            addToQueue(site, goLiveQueue, inProcessQueue, item, state);
                        }
                    } else {
                        _cancelWorkflow(site, state.getPath());
                        objectStateService.deleteObjectStateForPath(site, state.getPath());
                        objectMetadataManager.deleteObjectMetadata(site, state.getPath());
                    }
                } catch (Exception e) {
                    logger.error("Could not warm cache for [" + state.getSite() + " : " + state.getPath() + "] " + e.getMessage());
                }
            }
        }
    }

    protected void addToQueue(String site, GoLiveQueue queue, GoLiveQueue inProcessQueue, ContentItemTO item, ItemState itemState) throws ServiceException {
        if (item != null) {
            State state = State.valueOf(itemState.getState());
            //add only submitted items to go live Q.
            if (State.isSubmitted(state)) {
                queue.add(item);
            }

            if (inProcessQueue != null) {
                if (!State.isLive(state)) {
                    inProcessQueue.add(item);
                    inProcessQueue.add(item.getPath(), item);
                }
            }
        } else {
            objectStateService.deleteObjectState(itemState.getObjectId());
        }
    }

    @Override
    @ValidateParams
    public Map<String, Object> getInProgressItems(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "sort") String sort, boolean ascending, boolean inProgressOnly) throws ServiceException {
        DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, true, true);
        comparator.setSecondLevelCompareRequired(true);
        comparator.setSecondLevelSortBy(DmContentItemComparator.SORT_PATH);
        List<ContentItemTO> items = getInProgressItems(site, comparator, inProgressOnly);
        JSONObject jsonObject = new JSONObject();
        int total = 0;
        if (items != null) {
            for (ContentItemTO item : items) {
                total += item.getNumOfChildren();
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put(StudioConstants.PROPERTY_TOTAL, total);
        result.put(StudioConstants.PROPERTY_SORTED_BY, sort);
        result.put(StudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
        result.put(StudioConstants.PROPERTY_DOCUMENTS, items);
        return result;
    }

    protected List<ContentItemTO> getInProgressItems(final String site, final DmContentItemComparator comparator, final boolean inProgressOnly) throws ServiceException {
        final List<ContentItemTO> categoryItems = new ArrayList<>();

        List<ContentItemTO>categoryItems1 = getCategoryItems(site);
        categoryItems.addAll(categoryItems1);


        long st = System.currentTimeMillis();
        List<ItemState> changeSet = objectStateService.getChangeSet(site);

        logger.debug("Time taken listChangedAll()  " + (System.currentTimeMillis() - st));

        // the category item to add all other items that do not belong to
        // regular categories specified in the configuration
        st = System.currentTimeMillis();

        if (changeSet != null) {
            List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
            //List<String> inProgressItems = new FastList<String>();
            for (ItemState state : changeSet) {
                if (contentService.contentExists(state.getSite(), state.getPath())) {
                    if (ContentUtils.matchesPatterns(state.getPath(), displayPatterns)) {
                        ContentItemTO item = contentService.getContentItem(state.getSite(), state.getPath(), 0);
                        addInProgressItems(site, item, categoryItems, comparator, inProgressOnly);
                    }
                }
            }
        }

        logger.debug("Time taken after listChangedAll() : " + (System.currentTimeMillis() - st));
        return categoryItems;
    }

    protected void addInProgressItems(String site, ContentItemTO item, List<ContentItemTO> categoryItems, DmContentItemComparator comparator, boolean inProgressOnly) {
        if (addToQueue(false, inProgressOnly, true)) {
            if (!(item.isSubmitted() || item.isInProgress())) {
                return;
            }

            item.setDeleted(false);
            ContentItemTO found = null;
            String uri = item.getUri();
            for (ContentItemTO categoryItem : categoryItems) {
                String categoryPath = categoryItem.getPath() + FILE_SEPARATOR;
                if (uri.startsWith(categoryPath)) {
                    found = categoryItem;
                    break;
                }
            }
            if (found != null && !found.getUri().equals(item.getUri())) {
                found.addChild(item, comparator, true);
            }
        }
    }

    /**
     * add the current item to the queue?
     *
     * @param inProgressOnly
     * @param submitted
     * @param includeInProgress
     * @return true if added to queue
     */
    protected boolean addToQueue(boolean submitted, boolean inProgressOnly, boolean includeInProgress) {
        // excluded approved or scheduled items if in-progress items are
        // included. go-live queue case the node is in review
        if (inProgressOnly && submitted) {
            return false;
        }
        // add items in following cases
        // 1) if the item is submitted, add if the flag is not in-progress only
        // 2) if the item is in progress, add if the flag is either in-progress
        // only or include in progress
        if (submitted && !inProgressOnly) {
            return true;
        } else if (!submitted && (inProgressOnly || includeInProgress)) {
            return true;
        }
        return false;
    }

    @Override
    @ValidateParams
    public boolean removeFromWorkflow(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, boolean cancelWorkflow) throws ServiceException {
        Set<String> processedPaths = new HashSet<>();
        return removeFromWorkflow(site, path, processedPaths, cancelWorkflow);
    }

    protected boolean removeFromWorkflow(String site,  String path, Set<String> processedPaths, boolean cancelWorkflow) throws ServiceException {
        // remove submitted aspects from all dependent items
        if (!processedPaths.contains(path)) {
            processedPaths.add(path);
            // cancel workflow if anything is pending
            long startTime = System.currentTimeMillis();
            if (cancelWorkflow) {
                _cancelWorkflow(site, path);
            }
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("_cancelWorkflow Duration 111: {0}", duration);
        }
        return false;
    }

    protected void _cancelWorkflow(String site, String path) throws ServiceException {
        List<String> allItemsToCancel = getWorkflowAffectedPathsInternal(site, path);
        List<String> paths = new ArrayList<String>();
        for (String affectedItem : allItemsToCancel) {
            try {
                deploymentService.cancelWorkflow(site, affectedItem);
                ItemMetadata itemMetadata = objectMetadataManager.getProperties(site, affectedItem);
                if (itemMetadata != null) {
                    itemMetadata.setSubmittedBy(StringUtils.EMPTY);
                    itemMetadata.setSendEmail(0);
                    itemMetadata.setSubmittedForDeletion(0);
                    itemMetadata.setSubmissionComment(StringUtils.EMPTY);
                    itemMetadata.setLaunchDate(null);
                    objectMetadataManager.updateObjectMetadata(itemMetadata);
                }
                paths.add(affectedItem);
            } catch (DeploymentException e) {
                logger.error("Error occurred while trying to cancel workflow for path [" + affectedItem + "], site " + site, e);
            }
        }
        objectStateService.transitionBulk(site, paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.REJECT, State.NEW_UNPUBLISHED_UNLOCKED);
    }

    protected List<String> getWorkflowAffectedPathsInternal(String site, String path) throws ServiceException {
        List<String> affectedPaths = new ArrayList<String>();
        List<String> filteredPaths = new ArrayList<String>();
        if (objectStateService.isInWorkflow(site, path)) {
            affectedPaths.add(path);
            boolean isNew = objectStateService.isNew(site, path);
            boolean isRenamed = objectMetadataManager.isRenamed(site, path);
            if (isNew || isRenamed) {
                getMandatoryChildren(site, path, affectedPaths);
            }

            List<String> dependencyPaths = new ArrayList<String>();
            dependencyPaths.addAll(dependencyService.getPublishingDependencies(site, affectedPaths));
            affectedPaths.addAll(dependencyPaths);
            List<String> candidates = new ArrayList<String>();
            for (String p : affectedPaths) {
                if (!candidates.contains(p)) {
                    candidates.add(p);
                }
            }

            for (String cp : candidates) {
                if (objectStateService.isInWorkflow(site, cp)) {
                    filteredPaths.add(cp);
                }
            }
        }

        return filteredPaths;
    }

    @Override
    @ValidateParams
    public List<ContentItemTO> getWorkflowAffectedPaths(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws ServiceException {
        List<String> affectedPaths = getWorkflowAffectedPathsInternal(site, path);
        return getWorkflowAffectedItems(site, affectedPaths);
    }

    private void getMandatoryChildren(String site, String path, List<String> affectedPaths) {
        ContentItemTO item = contentService.getContentItem(site, path);
        for (ContentItemTO child : item.getChildren()) {
            if (!affectedPaths.contains(child.getUri())) {
                affectedPaths.add(child.getUri());
                getMandatoryChildren(site, child.getUri(), affectedPaths);
            }
        }
    }

    protected List<ContentItemTO> getWorkflowAffectedItems(String site, List<String> paths) {
        List<ContentItemTO> items = new ArrayList<>();

        for (String path : paths) {
            ContentItemTO item = contentService.getContentItem(site, path);
            items.add(item);
        }
        return items;
    }

    @Override
    public void updateWorkflowSandboxes(String site, String path) {

    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @Override
    @ValidateParams
    public ResultTO goDelete(@ValidateStringParam(name = "site") String site, String request, @ValidateStringParam(name = "user") String user) {
        return approve(site, request, Operation.DELETE);
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected ResultTO approve(String site, String request, Operation operation) {
        String approver = securityService.getCurrentUser();
        ResultTO result = new ResultTO();
        try {
            JSONObject requestObject = JSONObject.fromObject(request);
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            String scheduledDate = null;
            if (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                scheduledDate = requestObject.getString(JSON_KEY_SCHEDULED_DATE);
            }
            boolean isNow = (requestObject.containsKey(JSON_KEY_IS_NOW)) ? requestObject.getBoolean(JSON_KEY_IS_NOW) : false;

            String publishChannelGroupName = (requestObject.containsKey(JSON_KEY_PUBLISH_CHANNEL)) ? requestObject.getString(JSON_KEY_PUBLISH_CHANNEL) : null;
            JSONObject jsonObjectStatus = requestObject.getJSONObject(JSON_KEY_STATUS_SET);
            String statusMessage = (jsonObjectStatus != null && jsonObjectStatus.containsKey(JSON_KEY_STATUS_MESSAGE)) ? jsonObjectStatus.getString(JSON_KEY_STATUS_MESSAGE) : null;
            String submissionComment = (requestObject != null && requestObject.containsKey(JSON_KEY_SUBMISSION_COMMENT)) ? requestObject.getString(JSON_KEY_SUBMISSION_COMMENT) : "Test Go Live";
            MultiChannelPublishingContext mcpContext = new MultiChannelPublishingContext(publishChannelGroupName, statusMessage, submissionComment);

            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);
                DmDependencyTO submittedItem = null;

                submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate, null);
                List<DmDependencyTO> submitForDeleteChildren = removeSubmitToDeleteChildrenForGoLive(submittedItem, operation);
                if (submittedItem.isReference()) {
                    submittedItem.setReference(false);
                }
                submittedItems.add(submittedItem);
                submittedItems.addAll(submitForDeleteChildren);
            }
            switch (operation) {
                case GO_LIVE:
                    if (scheduledDate != null && isNow == false) {
                        responseMessageKey = NotificationService.COMPLETE_SCHEDULE_GO_LIVE;
                    } else {
                        responseMessageKey = NotificationService.COMPLETE_GO_LIVE;
                    }
                    List<DmDependencyTO> submitToDeleteItems = new ArrayList<>();
                    List<DmDependencyTO> goLiveItems = new ArrayList<>();
                    List<DmDependencyTO> renameItems = new ArrayList<>();
                    for (DmDependencyTO item : submittedItems) {
                        if (item.isSubmittedForDeletion()) {
                            submitToDeleteItems.add(item);
                        } else {
                            if (!isItemRenamed(site, item)) {
                                goLiveItems.add(item);
                            } else {
                                renameItems.add(item);
                            }
                        }
                    }

                    if (!submitToDeleteItems.isEmpty()) {
                        doDelete(site, submitToDeleteItems, approver);
                    }

                    if (!goLiveItems.isEmpty()) {
                        List<DmDependencyTO> references = getRefAndChildOfDiffDateFromParent(site, goLiveItems, true);
                        List<DmDependencyTO> children = getRefAndChildOfDiffDateFromParent(site, goLiveItems, false);
                        goLiveItems.addAll(references);
                        goLiveItems.addAll(children);
                        List<String> goLivePaths = new ArrayList<>();
                        Set<String> processedPaths = new HashSet<String>();
                        for (DmDependencyTO goLiveItem : goLiveItems) {
                            resolveSubmittedPaths(site, goLiveItem, goLivePaths, processedPaths);
                        }
                        List<String> nodeRefs = new ArrayList<>();
                        goLive(site, goLiveItems, approver, mcpContext);
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            renamePaths.add(renameItem.getUri());
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            renamePaths.add(renamedChild.getUri());
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }

                        goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        deletePaths.add(deletedItem.getUri());
                        ContentItemTO contentItem = contentService.getContentItem(site, deletedItem.getUri());
                        if (contentItem != null) {
                            //nodeRefs.add(nodeRef.getId());
                        }
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
            result.setMessage(notificationService.getNotificationMessage(site,
                    NotificationMessageType.CompleteMessages, responseMessageKey, Locale.ENGLISH));
        } catch (JSONException e) {
            logger.error("error performing operation " + operation + " " + e);

            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } catch (ServiceException e) {
            logger.error("error performing operation " + operation + " " + e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected ResultTO approve_new(String site, String request, Operation operation) {
        String approver = securityService.getCurrentUser();
        ResultTO result = new ResultTO();
        try {
            JSONObject requestObject = JSONObject.fromObject(request);
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            String scheduledDate = null;
            if (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                scheduledDate = requestObject.getString(JSON_KEY_SCHEDULED_DATE);
            }
            boolean isNow = (requestObject.containsKey(JSON_KEY_IS_NOW)) ? requestObject.getBoolean(JSON_KEY_IS_NOW) : false;

            String publishChannelGroupName = (requestObject.containsKey(JSON_KEY_PUBLISH_CHANNEL)) ? requestObject.getString(JSON_KEY_PUBLISH_CHANNEL) : null;
            JSONObject jsonObjectStatus = requestObject.getJSONObject(JSON_KEY_STATUS_SET);
            String statusMessage = (jsonObjectStatus != null && jsonObjectStatus.containsKey(JSON_KEY_STATUS_MESSAGE)) ? jsonObjectStatus.getString(JSON_KEY_STATUS_MESSAGE) : null;
            String submissionComment = (requestObject != null && requestObject.containsKey(JSON_KEY_SUBMISSION_COMMENT)) ? requestObject.getString(JSON_KEY_SUBMISSION_COMMENT) : "Test Go Live";
            MultiChannelPublishingContext mcpContext = new MultiChannelPublishingContext(publishChannelGroupName, statusMessage, submissionComment);

            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            List<String> submittedPaths = new ArrayList<String>();
            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);

                submittedPaths.add(stringItem);
                DmDependencyTO submittedItem = null;

                submittedItem = getSubmittedItem_new(site, stringItem, format, scheduledDate);
                List<DmDependencyTO> submitForDeleteChildren = removeSubmitToDeleteChildrenForGoLive(submittedItem, operation);
                if (submittedItem.isReference()) {
                    submittedItem.setReference(false);
                }
                submittedItems.add(submittedItem);
                submittedItems.addAll(submitForDeleteChildren);
            }
            switch (operation) {
                case GO_LIVE:
                    if (scheduledDate != null && !isNow) {
                        responseMessageKey = NotificationService.COMPLETE_SCHEDULE_GO_LIVE;
                    } else {
                        responseMessageKey = NotificationService.COMPLETE_GO_LIVE;
                    }
                    List<DmDependencyTO> submitToDeleteItems = new ArrayList<>();
                    List<DmDependencyTO> goLiveItems = new ArrayList<>();
                    List<DmDependencyTO> renameItems = new ArrayList<>();
                    for (DmDependencyTO item : submittedItems) {
                        if (item.isSubmittedForDeletion()) {
                            submitToDeleteItems.add(item);
                        } else {
                            if (!isItemRenamed(site, item)) {
                                goLiveItems.add(item);
                            } else {
                                renameItems.add(item);
                            }
                        }
                    }
                    if (!submitToDeleteItems.isEmpty()) {
                        doDelete(site, submitToDeleteItems, approver);
                    }

                    if (!goLiveItems.isEmpty()) {
                        List<DmDependencyTO> references = getRefAndChildOfDiffDateFromParent_new(site, goLiveItems, true);
                        List<DmDependencyTO> children = getRefAndChildOfDiffDateFromParent_new(site, goLiveItems, false);
                        goLiveItems.addAll(references);
                        goLiveItems.addAll(children);
                        List<DmDependencyTO> dependencies = addDependenciesForSubmittedItems(site, submittedItems, format, scheduledDate);
                        goLiveItems.addAll(dependencies);
                        List<String> goLivePaths = new ArrayList<>();
                        for (DmDependencyTO goLiveItem : goLiveItems) {
                            goLivePaths.add(goLiveItem.getUri());
                        }
                        goLive(site, goLiveItems, approver, mcpContext);
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            renamePaths.add(renameItem.getUri());
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            renamePaths.add(renamedChild.getUri());
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }
                        goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        //deletedItem.setScheduledDate(getScheduledDate(site, format, scheduledDate));
                        deletePaths.add(deletedItem.getUri());
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
            result.setMessage(notificationService.getNotificationMessage(site, NotificationMessageType
                    .CompleteMessages, responseMessageKey, Locale.ENGLISH));

        } catch (JSONException e) {
            logger.error("error performing operation " + operation + " " + e);

            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } catch (ServiceException e) {
            logger.error("error performing operation " + operation + " " + e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected ResultTO approveWithoutDependencies(String site, String request, Operation operation) {
        String approver = securityService.getCurrentUser();
        ResultTO result = new ResultTO();
        try {
            JSONObject requestObject = JSONObject.fromObject(request);
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            String scheduledDate = null;
            if (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                scheduledDate = requestObject.getString(JSON_KEY_SCHEDULED_DATE);
            }
            boolean isNow = (requestObject.containsKey(JSON_KEY_IS_NOW)) ? requestObject.getBoolean(JSON_KEY_IS_NOW) : false;

            String publishChannelGroupName = (requestObject.containsKey(JSON_KEY_PUBLISH_CHANNEL)) ? requestObject.getString(JSON_KEY_PUBLISH_CHANNEL) : null;
            JSONObject jsonObjectStatus = requestObject.getJSONObject(JSON_KEY_STATUS_SET);
            String statusMessage = (jsonObjectStatus != null && jsonObjectStatus.containsKey(JSON_KEY_STATUS_MESSAGE)) ? jsonObjectStatus.getString(JSON_KEY_STATUS_MESSAGE) : null;
            String submissionComment = (requestObject != null && requestObject.containsKey(JSON_KEY_SUBMISSION_COMMENT)) ? requestObject.getString(JSON_KEY_SUBMISSION_COMMENT) : "Test Go Live";
            MultiChannelPublishingContext mcpContext = new MultiChannelPublishingContext(publishChannelGroupName, statusMessage, submissionComment);

            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            List<String> submittedPaths = new ArrayList<String>();
            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);

                submittedPaths.add(stringItem);
                DmDependencyTO submittedItem = null;

                submittedItem = getSubmittedItemApproveWithoutDependencies(site, stringItem, format, scheduledDate);
                List<DmDependencyTO> submitForDeleteChildren = removeSubmitToDeleteChildrenForGoLive(submittedItem, operation);
                if (submittedItem.isReference()) {
                    submittedItem.setReference(false);
                }
                submittedItems.add(submittedItem);
                submittedItems.addAll(submitForDeleteChildren);
            }
            switch (operation) {
                case GO_LIVE:
                    if (scheduledDate != null && isNow == false) {
                        responseMessageKey = NotificationService.COMPLETE_SCHEDULE_GO_LIVE;
                    } else {
                        responseMessageKey = NotificationService.COMPLETE_GO_LIVE;
                    }
                    List<DmDependencyTO> submitToDeleteItems = new ArrayList<>();
                    List<DmDependencyTO> goLiveItems = new ArrayList<>();
                    List<DmDependencyTO> renameItems = new ArrayList<>();
                    for (DmDependencyTO item : submittedItems) {
                        if (item.isSubmittedForDeletion()) {
                            submitToDeleteItems.add(item);
                        } else {
                            if (!isItemRenamed(site, item)) {
                                goLiveItems.add(item);
                            } else {
                                renameItems.add(item);
                            }
                        }
                    }

                    if (!submitToDeleteItems.isEmpty()) {
                        doDelete(site, submitToDeleteItems, approver);
                    }

                    if (!goLiveItems.isEmpty()) {
                        //List<DmDependencyTO> references = getRefAndChildOfDiffDateFromParent_new(site, goLiveItems, true);
                        //List<DmDependencyTO> children = getRefAndChildOfDiffDateFromParent_new(site, goLiveItems, false);
                        //goLiveItems.addAll(references);
                        //goLiveItems.addAll(children);
                        //List<DmDependencyTO> dependencies = addDependenciesForSubmittedItems(site, submittedItems, format, scheduledDate);
                        //goLiveItems.addAll(dependencies);
                        List<String> goLivePaths = new ArrayList<>();
                        Set<String> processedPaths = new HashSet<String>();
                        for (DmDependencyTO goLiveItem : goLiveItems) {
                            resolveSubmittedPaths(site, goLiveItem, goLivePaths, processedPaths);
                        }
                        goLive(site, goLiveItems, approver, mcpContext);
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            renamePaths.add(renameItem.getUri());
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            renamePaths.add(renamedChild.getUri());
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().isAfter(ZonedDateTime.now(ZoneOffset.UTC))) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }

                        goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        deletePaths.add(deletedItem.getUri());
                        ContentItemTO contentItem = contentService.getContentItem(site, deletedItem.getUri());
                        if (contentItem != null) {
                            //nodeRefs.add(nodeRef.getId());
                        }
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
            result.setMessage(notificationService.getNotificationMessage(site,NotificationMessageType
                    .CompleteMessages,responseMessageKey,Locale.ENGLISH));
        } catch (JSONException e) {
            logger.error("error performing operation " + operation + " " + e);

            result.setSuccess(false);
            result.setMessage(e.getMessage());
        } catch (ServiceException e) {
            logger.error("error performing operation " + operation + " " + e);
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    /**
     * get a submitted item from a JSON item
     *
     * @param site
     * @param item
     * @param format
     * @return submitted item
     * @throws net.sf.json.JSONException
     */
    protected DmDependencyTO getSubmittedItem(String site, JSONObject item, SimpleDateFormat format, String globalSchDate) throws JSONException, ServiceException {
        DmDependencyTO submittedItem = new DmDependencyTO();
        String uri = item.getString(JSON_KEY_URI);
        submittedItem.setUri(uri);
        boolean deleted = (item.containsKey(JSON_KEY_DELETED)) ? item.getBoolean(JSON_KEY_DELETED) : false;
        submittedItem.setDeleted(deleted);
        boolean isNow = (item.containsKey(JSON_KEY_IS_NOW)) ? item.getBoolean(JSON_KEY_IS_NOW) : false;
        submittedItem.setNow(isNow);
        boolean submittedForDeletion = (item.containsKey(JSON_KEY_SUBMITTED_FOR_DELETION)) ? item.getBoolean(JSON_KEY_SUBMITTED_FOR_DELETION) : false;
        boolean submitted = (item.containsKey(JSON_KEY_SUBMITTED)) ? item.getBoolean(JSON_KEY_SUBMITTED) : false;
        boolean inProgress = (item.containsKey(JSON_KEY_IN_PROGRESS)) ? item.getBoolean(JSON_KEY_IN_PROGRESS) : false;
        boolean isReference = (item.containsKey(JSON_KEY_IN_REFERENCE)) ? item.getBoolean(JSON_KEY_IN_REFERENCE) : false;
        submittedItem.setReference(isReference);
        // boolean submittedForDeletion =
        // (item.containsKey(JSON_KEY_SUBMITTED_FOR_DELETION)) ?
        // item.getBoolean(JSON_KEY_SUBMITTED_FOR_DELETION) : false;
        submittedItem.setSubmittedForDeletion(submittedForDeletion);
        submittedItem.setSubmitted(submitted);
        submittedItem.setInProgress(inProgress);
        // TODO: check scheduled date to make sure it is not null when isNow =
        // true and also it is not past
        ZonedDateTime scheduledDate = null;
        if (globalSchDate != null && !StringUtils.isEmpty(globalSchDate)) {
            scheduledDate = getScheduledDate(site, format, globalSchDate);
        } else {
            if (item.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                String dateStr = item.getString(JSON_KEY_SCHEDULED_DATE);
                if (!StringUtils.isEmpty(dateStr)) {
                    scheduledDate = getScheduledDate(site, format, dateStr);
                }
            }
        }
        if (scheduledDate == null && isNow == false) {
            submittedItem.setNow(true);
        }
        submittedItem.setScheduledDate(scheduledDate);
        JSONArray components = (item.containsKey(JSON_KEY_COMPONENTS) && !item.getJSONObject(JSON_KEY_COMPONENTS).isNullObject()) ? item.getJSONArray(JSON_KEY_COMPONENTS) : null;
        List<DmDependencyTO> submittedComponents = getSubmittedItems(site, components, format, globalSchDate);
        submittedItem.setComponents(submittedComponents);

        JSONArray documents = (item.containsKey(JSON_KEY_DOCUMENTS) && !item.getJSONObject(JSON_KEY_DOCUMENTS).isNullObject()) ? item.getJSONArray(JSON_KEY_DOCUMENTS) : null;
        List<DmDependencyTO> submittedDocuments = getSubmittedItems(site, documents, format, globalSchDate);

        submittedItem.setDocuments(submittedDocuments);
        JSONArray assets = (item.containsKey(JSON_KEY_ASSETS) && !item.getJSONObject(JSON_KEY_ASSETS).isNullObject()) ? item.getJSONArray(JSON_KEY_ASSETS) : null;
        List<DmDependencyTO> submittedAssets = getSubmittedItems(site, assets, format, globalSchDate);
        submittedItem.setAssets(submittedAssets);

        JSONArray templates = (item.containsKey(JSON_KEY_RENDERING_TEMPLATES) && !item.getJSONObject(JSON_KEY_RENDERING_TEMPLATES).isNullObject()) ? item.getJSONArray(JSON_KEY_RENDERING_TEMPLATES) : null;
        List<DmDependencyTO> submittedTemplates = getSubmittedItems(site, templates, format, globalSchDate);
        submittedItem.setRenderingTemplates(submittedTemplates);

        JSONArray deletedItems = (item.containsKey(JSON_KEY_DELETED_ITEMS) && !item.getJSONObject(JSON_KEY_DELETED_ITEMS).isNullObject()) ? item.getJSONArray(JSON_KEY_DELETED_ITEMS) : null;
        List<DmDependencyTO> deletes = getSubmittedItems(site, deletedItems, format, globalSchDate);
        submittedItem.setDeletedItems(deletes);

        JSONArray children = (item.containsKey(JSON_KEY_CHILDREN)) ? item.getJSONArray(JSON_KEY_CHILDREN) : null;
        List<DmDependencyTO> submittedChidren = getSubmittedItems(site, children, format, globalSchDate);
        submittedItem.setChildren(submittedChidren);

        if (uri.endsWith(DmConstants.XML_PATTERN)) {
            /**
             * Get dependent pages
             */
            Set<String> deps = dependencyService.getItemDependencies(site, uri, 1);
            List<String> pagePatterns = servicesConfig.getPagePatterns(site);
            List<String> documentPatterns = servicesConfig.getDocumentPatterns(site);
            List<DmDependencyTO> dependentPages = new ArrayList<>();
            List<DmDependencyTO> dependentDocuments = new ArrayList<>();
            for (String dep : deps) {
                if (ContentUtils.matchesPatterns(dep, pagePatterns)) {
                    DmDependencyTO dmDependencyTO = new DmDependencyTO();
                    dmDependencyTO.setUri(dep);
                    dependentPages.add(dmDependencyTO);
                } else if (ContentUtils.matchesPatterns(dep, documentPatterns)) {
                    DmDependencyTO dmDependencyTO = new DmDependencyTO();
                    dmDependencyTO.setUri(dep);
                    dependentDocuments.add(dmDependencyTO);
                }
            }
            submittedItem.setPages(dependentPages);
            submittedItem.setDocuments(dependentDocuments);
        }

        return submittedItem;
    }

    protected DmDependencyTO getSubmittedItem(String site, String itemPath, SimpleDateFormat format, String globalSchDate, Set<String> processedDependencies) throws JSONException {
        DmDependencyTO submittedItem = new DmDependencyTO();
        submittedItem.setUri(itemPath);
        // true and also it is not past
        ZonedDateTime scheduledDate = null;
        if (globalSchDate != null && !StringUtils.isEmpty(globalSchDate)) {
            scheduledDate = getScheduledDate(site, format, globalSchDate);
        } else {
            if (submittedItem.getScheduledDate() != null) {
                scheduledDate = getScheduledDate(site, format, submittedItem.getScheduledDate().format(DateTimeFormatter.ofPattern(format.toPattern())));
            }
        }
        if (scheduledDate == null) {
            submittedItem.setNow(true);
        }
        submittedItem.setScheduledDate(scheduledDate);
        if (processedDependencies == null) {
            processedDependencies = new HashSet<String>();
        }
        if (CollectionUtils.isNotEmpty(submittedItem.getComponents())) {
            for (DmDependencyTO component : submittedItem.getComponents()) {
                if (!processedDependencies.contains(component.getUri())) {
                    component = getSubmittedItem(site, component.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(submittedItem.getDocuments())) {
            for (DmDependencyTO document : submittedItem.getDocuments()) {
                if (!processedDependencies.contains(document.getUri())) {
                    document = getSubmittedItem(site, document.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(submittedItem.getAssets())) {
            for (DmDependencyTO asset : submittedItem.getAssets()) {
                if (!processedDependencies.contains(asset.getUri())) {
                    asset = getSubmittedItem(site, asset.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(submittedItem.getRenderingTemplates())) {
            for (DmDependencyTO template : submittedItem.getRenderingTemplates()) {
                if (!processedDependencies.contains(template.getUri())) {
                    template = getSubmittedItem(site, template.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(submittedItem.getDeletedItems())) {
            for (DmDependencyTO deletedItem : submittedItem.getDeletedItems()) {
                if (!processedDependencies.contains(deletedItem.getUri())) {
                    deletedItem = getSubmittedItem(site, deletedItem.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(submittedItem.getChildren())) {
            for (DmDependencyTO child : submittedItem.getChildren()) {
                if (!processedDependencies.contains(child.getUri())) {
                    child = getSubmittedItem(site, child.getUri(), format, globalSchDate, processedDependencies);
                }
            }
        }

        return submittedItem;
    }

    protected DmDependencyTO getSubmittedItem_new(String site, String itemPath, SimpleDateFormat format, String globalSchDate) throws JSONException {
        DmDependencyTO submittedItem = new DmDependencyTO();
        submittedItem.setUri(itemPath);
        // TODO: check scheduled date to make sure it is not null when isNow =
        // true and also it is not past
        ZonedDateTime scheduledDate = null;
        if (globalSchDate != null && !StringUtils.isEmpty(globalSchDate)) {
            scheduledDate = getScheduledDate(site, format, globalSchDate);
        } else {
            if (submittedItem.getScheduledDate() != null) {
                scheduledDate = getScheduledDate(site, format, submittedItem.getScheduledDate().format(DateTimeFormatter.ofPattern(format.toPattern())));
            }
        }
        if (scheduledDate == null) {
            submittedItem.setNow(true);
        }
        submittedItem.setScheduledDate(scheduledDate);

        return submittedItem;
    }

    protected DmDependencyTO getSubmittedItemApproveWithoutDependencies(String site, String itemPath, SimpleDateFormat format, String globalSchDate) throws JSONException {
        DmDependencyTO submittedItem = new DmDependencyTO();
        submittedItem.setUri(itemPath);
        // TODO: check scheduled date to make sure it is not null when isNow =
        // true and also it is not past
        ZonedDateTime scheduledDate = null;
        if (globalSchDate != null && !StringUtils.isEmpty(globalSchDate)) {
            scheduledDate = getScheduledDate(site, format, globalSchDate);
        } else {
            if (submittedItem.getScheduledDate() != null) {
                scheduledDate = getScheduledDate(site, format, submittedItem.getScheduledDate().format(DateTimeFormatter.ofPattern(format.toPattern())));
            }
        }
        if (scheduledDate == null) {
            submittedItem.setNow(true);
        }
        submittedItem.setScheduledDate(scheduledDate);

        return submittedItem;
    }

    /**
     * get submitted items from JSON request
     *
     * @param site
     * @param items
     * @param format
     * @return submitted items
     * @throws JSONException
     */
    protected List<DmDependencyTO> getSubmittedItems(String site, JSONArray items, SimpleDateFormat format, String schDate) throws JSONException, ServiceException {
        if (items != null) {
            int length = items.size();
            if (length > 0) {
                List<DmDependencyTO> submittedItems = new ArrayList<>();
                for (int index = 0; index < length; index++) {
                    JSONObject item = items.getJSONObject(index);
                    DmDependencyTO submittedItem = getSubmittedItem(site, item, format, schDate);
                    submittedItems.add(submittedItem);
                }
                return submittedItems;
            }
        }
        return null;
    }

    /**
     * removes the child items which are in submit to delete state from
     * submitted items as these have to be routed for deletion. it applies to
     * GoLive operation.
     *
     * @param dependencyTO
     * @param operation
     */
    protected List<DmDependencyTO> removeSubmitToDeleteChildrenForGoLive(DmDependencyTO dependencyTO, Operation operation) {
        List<DmDependencyTO> submitForDeleteChilds = new ArrayList<>();
        if (operation == Operation.GO_LIVE && !dependencyTO.isSubmittedForDeletion()) {
            List<DmDependencyTO> children = dependencyTO.getChildren();
            if (children != null) {
                for (DmDependencyTO child : children) {
                    if (child.isSubmittedForDeletion()) {
                        submitForDeleteChilds.add(child);
                    }
                }
                for (DmDependencyTO submitForDeleteChild : submitForDeleteChilds) {
                    children.remove(submitForDeleteChild);
                }
            }
        }
        return submitForDeleteChilds;
    }

    protected void doDelete(String site, List<DmDependencyTO> submittedItems, String approver) throws ServiceException {
        long start = System.currentTimeMillis();
        String user = securityService.getCurrentUser();
        // get web project information
        //String assignee = getAssignee(site, sub);
        // Don't make go live an item if it is new and to be deleted
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        List<String> itemsToDelete = new ArrayList<>();
        List<DmDependencyTO> deleteItems = new ArrayList<>();
        List<DmDependencyTO> scheItems = new ArrayList<>();
        for (DmDependencyTO submittedItem : submittedItems) {
            String uri = submittedItem.getUri();
            ZonedDateTime schDate = submittedItem.getScheduledDate();
            boolean isItemForSchedule = false;
            if (schDate == null || schDate.isBefore(now)) {
                // Sending Notification
                if (StringUtils.isNotEmpty(approver)) {
                    // immediate delete
                    if (submittedItem.isSendEmail()) {
                        sendDeleteApprovalNotification(site, submittedItem, approver);//TODO move it after delete actually happens
                    }
                }
                if (submittedItem.getUri().endsWith(DmConstants.INDEX_FILE)) {
                    submittedItem.setUri(submittedItem.getUri().replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""));
                }
                itemsToDelete.add(uri);
            } else {
                scheItems.add(submittedItem);
                isItemForSchedule = true;
            }
            submittedItem.setDeleted(true);
            // replace with the folder name
            boolean isNew = objectStateService.isNew(site, uri);
            if (!isNew || isItemForSchedule) {
                deleteItems.add(submittedItem);
            }
        }
        GoLiveContext context = new GoLiveContext(approver, site);
        final String pathPrefix = FILE_SEPARATOR + "wem-projects" + FILE_SEPARATOR + site + FILE_SEPARATOR + site + FILE_SEPARATOR + "work-area";
        Map<ZonedDateTime, List<DmDependencyTO>> groupedPackages = groupByDate(deleteItems, now);
        if (groupedPackages.isEmpty()) {
            groupedPackages.put(now, Collections.<DmDependencyTO>emptyList());
        }
        for (ZonedDateTime scheduledDate : groupedPackages.keySet()) {
            List<DmDependencyTO> deletePackage = groupedPackages.get(scheduledDate);
            SubmitPackage submitpackage = new SubmitPackage(pathPrefix);
            Set<String> rescheduledUris = new HashSet<String>();
            if (deletePackage != null) {
                ZonedDateTime launchDate = scheduledDate.equals(now) ? null : scheduledDate;
                Set<String> processedUris = new HashSet<String>();
                for (DmDependencyTO dmDependencyTO : deletePackage) {
                    if (launchDate != null) {
                        handleReferences(site, submitpackage, dmDependencyTO, true, null, "", rescheduledUris, processedUris);
                    } else {
                        applyDeleteDependencyRule(site, submitpackage, dmDependencyTO);
                    }
                }
                String label = submitpackage.getLabel();

                SubmitLifeCycleOperation deleteOperation = null;
                Set<String> liveDependencyItems = new HashSet<String>();
                Set<String> allItems = new HashSet<String>();
                for (String uri : itemsToDelete) {//$ToDO $ remove this case and keep the item in go live queue
                    GoLiveDeleteCandidates deleteCandidate = contentService.getDeleteCandidates(context.getSite(), uri);
                    allItems.addAll(deleteCandidate.getAllItems());
                    //get all dependencies that has to be removed as well
                    liveDependencyItems.addAll(deleteCandidate.getLiveDependencyItems());
                }

                List<String> submitPackPaths = submitpackage.getPaths();
                if (launchDate != null) {
                    deleteOperation = new PreScheduleDeleteOperation(this, submitpackage.getUris(), launchDate, context, rescheduledUris);
                    label = DmConstants.DM_SCHEDULE_SUBMISSION_FLOW + ":" + label;
                } else {
                    //add dependencies to submitPackage
                    for (String liveDependency : liveDependencyItems) {
                        submitpackage.addToPackage(liveDependency);
                    }
                    submitPackPaths = submitpackage.getPaths();

                    deleteOperation = new PreSubmitDeleteOperation(this, new HashSet<String>(allItems), context, rescheduledUris);
                    removeChildFromSubmitPackForDelete(submitPackPaths);
                }
                Map<String, String> submittedBy = new HashMap<>();

                workflowProcessor.addToWorkflow(site, new ArrayList<String>(), launchDate, label, deleteOperation, approver, null);
            }
        }
        long end = System.currentTimeMillis();
        logger.debug("Submitted deleted items to queue time = " + (end - start));
    }

    @Override
    public Map<ZonedDateTime, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, ZonedDateTime now) {
        Map<ZonedDateTime, List<DmDependencyTO>> groupedPackages = new HashMap<>();
        for (DmDependencyTO submittedItem : submittedItems) {

            ZonedDateTime scheduledDate = (submittedItem.isNow()) ? null : submittedItem.getScheduledDate();
            if (scheduledDate == null || scheduledDate.isBefore(now)) {
                scheduledDate = now;
            }
            List<DmDependencyTO> goLivePackage = groupedPackages.get(scheduledDate);
            if (goLivePackage == null)
                goLivePackage = new ArrayList<>();
            goLivePackage.add(submittedItem);
            groupedPackages.put(scheduledDate, goLivePackage);
        }

        return groupedPackages;
    }

    protected void handleReferences(String site, SubmitPackage submitpackage, DmDependencyTO dmDependencyTO, boolean isNotScheduled, SubmitPackage dependencyPackage, String approver, Set<String> rescheduledUris, Set<String> processedUris) {//,boolean isReferencePage) {
        if (!processedUris.contains(dmDependencyTO.getUri())) {
            ItemMetadata properties = objectMetadataManager.getProperties(site, dmDependencyTO.getUri());
            ZonedDateTime scheduledDate = null;
            if (properties != null) {
                scheduledDate = properties.getLaunchDate();
            }
            ItemState state = objectStateService.getObjectState(site, dmDependencyTO.getUri());
            if (state != null) {
                if (!State.isSubmitted(State.valueOf(state.getState())) && scheduledDate != null && scheduledDate.equals(dmDependencyTO.getScheduledDate())) {
                    if (objectStateService.isScheduled(site, dmDependencyTO.getUri())) {
                        return;
                    } else {
                        submitpackage.addToPackage(dmDependencyTO);
                    }
                }
            }
            if (!dmDependencyTO.isReference()) {
                submitpackage.addToPackage(dmDependencyTO);
            }
            if (isRescheduleRequest(dmDependencyTO, site)) {
                rescheduledUris.add(dmDependencyTO.getUri());
            }
            processedUris.add(dmDependencyTO.getUri());
        }
    }

    protected boolean areEqual(ZonedDateTime oldDate, ZonedDateTime newDate) {
        if (oldDate == null && newDate == null) {
            return true;
        }
        if (oldDate != null && newDate != null) {
            return oldDate.equals(newDate);
        }
        return false;
    }

    protected void applyDeleteDependencyRule(String site, SubmitPackage pack, DmDependencyTO dmDependencyTO) {
        pack.addToPackage(dmDependencyTO);
        DependencyRules rule = new DependencyRules(site);
        rule.setObjectStateService(objectStateService);
        rule.setContentService(contentService);
        Set<DmDependencyTO> dependencyTOSet = rule.applyDeleteDependencyRule(dmDependencyTO);
        for (DmDependencyTO dependencyTO : dependencyTOSet) {
            pack.addToPackage(dependencyTO);
        }
    }

    /**
     * parse the given date
     *
     * @param site
     * @param format
     * @param dateStr
     * @return date
     */
    protected ZonedDateTime getScheduledDate(String site, SimpleDateFormat format, String dateStr) {
        return ContentFormatUtils.parseDate(format, dateStr, servicesConfig.getDefaultTimezone(site));
    }

    protected void removeChildFromSubmitPackForDelete(List<String> paths) {
        Iterator<String> itr = paths.iterator();
        while (itr.hasNext()) {
            String path = itr.next();
            if (checkParentExistsInSubmitPackForDelete(paths, path)) {
                itr.remove();
            }
        }
    }

    protected boolean checkParentExistsInSubmitPackForDelete(List<String> paths, String path) {
        String split[] = path.split(FILE_SEPARATOR);
        for (int i = split.length - 1; i >= 0; i--) {
            int lastIndex = path.lastIndexOf(split[i]) - 1;
            if (lastIndex > 0) {
                path = path.substring(0, lastIndex);
                if (paths.contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void sendDeleteApprovalNotification(String site, DmDependencyTO submittedItem, String approver) {
        try {

            if (submittedItem.isSendEmail()) {
                String uri = submittedItem.getUri();
                ContentItemTO contentItem = contentService.getContentItem(site, uri);
                if (contentItem != null) {
                    //Prepare to send notification
/*
                    Serializable submittedByValue = persistenceManagerService.getProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY);
                    String submittedBy = "";
                    if (submittedByValue != null) {
                        submittedBy = (String) submittedByValue;
                        notificationService.sendDeleteApprovalNotification(site, submittedBy, uri, approver);
                    }*/
                }
            }
        } catch (Exception e) {
            logger.error("Could not send delete approval notification for newly created item", e);
        }
    }

    protected List<DmDependencyTO> getRefAndChildOfDiffDateFromParent(String site, List<DmDependencyTO> submittedItems, boolean removeInPages) throws ServiceException {
        List<DmDependencyTO> childAndReferences = new ArrayList<>();
        for (DmDependencyTO submittedItem : submittedItems) {
            List<DmDependencyTO> children = submittedItem.getChildren();
            ZonedDateTime date = submittedItem.getScheduledDate();
            if (children != null) {
                Iterator<DmDependencyTO> childItr = children.iterator();
                while (childItr.hasNext()) {
                    DmDependencyTO child = childItr.next();
                    ZonedDateTime pageDate = child.getScheduledDate();
                    if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                        if (!submittedItem.isNow()) {
                            child.setNow(false);
                            if (date != null && (pageDate != null && pageDate.isBefore(date))) {
                                child.setScheduledDate(date);
                            }
                        }
                        childAndReferences.add(child);
                        List<DmDependencyTO> childDeps = child.flattenChildren();
                        for (DmDependencyTO childDep : childDeps) {
                            if (objectStateService.isUpdatedOrNew(site, childDep.getUri())) {
                                childAndReferences.add(childDep);
                            }
                        }
                        child.setReference(false);
                        childItr.remove();
                        if (removeInPages) {
                            String uri = child.getUri();
                            List<DmDependencyTO> pages = submittedItem.getPages();
                            if (pages != null) {
                                Iterator<DmDependencyTO> pagesIter = pages.iterator();
                                while (pagesIter.hasNext()) {
                                    DmDependencyTO page = pagesIter.next();
                                    if (page.getUri().equals(uri)) {
                                        pagesIter.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Set<String> dependenciesPaths = dependencyService.getPublishingDependencies(site, submittedItem.getUri());
            for (String depPath : dependenciesPaths) {
                DmDependencyTO dmDependencyTO = new DmDependencyTO();
                dmDependencyTO.setUri(depPath);
                childAndReferences.add(dmDependencyTO);
            }
        }
        return childAndReferences;
    }

    protected List<DmDependencyTO> getRefAndChildOfDiffDateFromParent_new(String site, List<DmDependencyTO> submittedItems, boolean removeInPages) {
        List<DmDependencyTO> childAndReferences = new ArrayList<>();
        for (DmDependencyTO submittedItem : submittedItems) {
            List<DmDependencyTO> children = submittedItem.getChildren();
            ZonedDateTime date = submittedItem.getScheduledDate();
            if (children != null) {
                Iterator<DmDependencyTO> childItr = children.iterator();
                while (childItr.hasNext()) {
                    DmDependencyTO child = childItr.next();
                    ZonedDateTime pageDate = child.getScheduledDate();
                    if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                        if (!submittedItem.isNow()) {
                            child.setNow(false);
                            if (date != null && (pageDate != null && pageDate.isBefore(date))) {
                                child.setScheduledDate(date);
                            }
                        }
                        childAndReferences.add(child);
                        List<DmDependencyTO> childDeps = child.flattenChildren();
                        for (DmDependencyTO childDep : childDeps) {
                            if (objectStateService.isUpdatedOrNew(site, childDep.getUri())) {
                                childAndReferences.add(childDep);
                            }
                        }
                        child.setReference(false);
                        childItr.remove();
                        if (removeInPages) {
                            String uri = child.getUri();
                            List<DmDependencyTO> pages = submittedItem.getPages();
                            if (pages != null) {
                                Iterator<DmDependencyTO> pagesIter = pages.iterator();
                                while (pagesIter.hasNext()) {
                                    DmDependencyTO page = pagesIter.next();
                                    if (page.getUri().equals(uri)) {
                                        pagesIter.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return childAndReferences;
    }

    protected List<DmDependencyTO> addDependenciesForSubmittedItems(String site, List<DmDependencyTO> submittedItems, SimpleDateFormat format, String globalScheduledDate) throws ServiceException {
        List<DmDependencyTO> dependencies = new ArrayList<DmDependencyTO>();
        Set<String> dependenciesPaths = new HashSet<String>();
        for (DmDependencyTO submittedItem : submittedItems) {
            if (!dependenciesPaths.contains(submittedItem.getUri())) {
                dependenciesPaths.addAll(dependencyService.getPublishingDependencies(site, submittedItem.getUri()));
            }
        }
        for (String depPath : dependenciesPaths) {
            dependencies.add(getSubmittedItem_new(site, depPath, format, globalScheduledDate));
        }
        return dependencies;
    }

    protected List<DmDependencyTO> addDependenciesForSubmitForApproval(String site, List<DmDependencyTO> submittedItems, SimpleDateFormat format, String globalScheduledDate) throws ServiceException {
        List<DmDependencyTO> dependencies = new ArrayList<DmDependencyTO>();
        Set<String> dependenciesPaths = new HashSet<String>();
        for (DmDependencyTO submittedItem : submittedItems) {
            dependenciesPaths.addAll(dependencyService.getPublishingDependencies(site, submittedItem.getUri()));
        }
        for (String depPath : dependenciesPaths) {
            dependencies.add(getSubmittedItem(site, depPath, format, globalScheduledDate, null));
        }
        return dependencies;
    }

    protected void resolveSubmittedPaths(String site, DmDependencyTO item, List<String> submittedPaths, Set<String> processedPaths) throws ServiceException {
        if (!processedPaths.contains(item.getUri())) {
            if (!submittedPaths.contains(item.getUri())) {
                submittedPaths.add(item.getUri());
            }
            List<DmDependencyTO> children = item.getChildren();
            if (children != null) {
                for (DmDependencyTO child : children) {
                    if (objectStateService.isUpdatedOrNew(site, child.getUri())) {
                        if (!submittedPaths.contains(child.getUri())) {
                            submittedPaths.add(child.getUri());
                        }
                        resolveSubmittedPaths(site, child, submittedPaths, processedPaths);
                    }
                }
            }
            Set<String> dependencyPaths = dependencyService.getPublishingDependencies(site, item.getUri());
            submittedPaths.addAll(dependencyPaths);
            processedPaths.addAll(dependencyPaths);
            processedPaths.add(item.getUri());
        }
    }

    protected List<DmDependencyTO> getChildrenForRenamedItem(String site, DmDependencyTO renameItem) {
        List<DmDependencyTO> toRet = new ArrayList<>();
        List<DmDependencyTO> children = renameItem.getChildren();
        ZonedDateTime date = renameItem.getScheduledDate();
        if (children != null) {
            Iterator<DmDependencyTO> childItr = children.iterator();
            while (childItr.hasNext()) {
                DmDependencyTO child = childItr.next();
                ZonedDateTime pageDate = child.getScheduledDate();
                if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                    if (!renameItem.isNow()) {
                        child.setNow(false);
                        if (date != null && (pageDate != null && pageDate.isBefore(date))) {
                            child.setScheduledDate(date);
                        }
                    }
                    toRet.add(child);
                    List<DmDependencyTO> childDeps = child.flattenChildren();
                    for (DmDependencyTO childDep : childDeps) {
                        if (objectStateService.isUpdatedOrNew(site, childDep.getUri())) {
                            toRet.add(childDep);
                        }
                    }
                    child.setReference(false);
                    childItr.remove();
                }
            }
        }
        return toRet;
    }

    @Override
    public void preScheduleDelete(Set<String> urisToDelete, final ZonedDateTime scheduleDate, final GoLiveContext context, Set rescheduledUris)
            throws ServiceException {
        final String site = context.getSite();
        final List<String> itemsToDelete = new ArrayList<String>(urisToDelete);
        dmPublishService.unpublish(site, itemsToDelete, context.getApprover(), scheduleDate);
    }

    @Override
    public List<String> preDelete(Set<String> urisToDelete, GoLiveContext context, Set<String> rescheduledUris) throws ServiceException {
        cleanUrisFromWorkflow(urisToDelete, context.getSite());
        cleanUrisFromWorkflow(rescheduledUris, context.getSite());
        List<String> deletedItems = deleteInTransaction(context.getSite(), new ArrayList<String>(urisToDelete), true, context.getApprover());
        return deletedItems;
    }

    protected List<String> deleteInTransaction(final String site, final List<String> itemsToDelete, final boolean generateActivity, final String approver) throws ServiceException {
        dmPublishService.unpublish(site, itemsToDelete, approver);
        return null;
        //return contentService.deleteContents(site, itemsToDelete, generateActivity, approver);
    }

    protected void cleanUrisFromWorkflow(final Set<String> uris, final String site) throws ServiceException {
        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                cleanWorkflow(uri, site, Collections.<DmDependencyTO>emptySet());
            }
        }
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @Override
    @ValidateParams
    public ResultTO goLive(@ValidateStringParam(name = "site") final String site, final String request) throws ServiceException {
        String lockKey = DmConstants.PUBLISHING_LOCK_KEY.replace("{SITE}", site.toUpperCase());
        try {
            if (isEnablePublishingWithoutDependencies()) {
                return approveWithoutDependencies(site, request, Operation.GO_LIVE);
            } else {
                return approve_new(site, request, Operation.GO_LIVE);
            }
        } catch (RuntimeException e) {
            logger.error("error making go live", e);
            throw e;
        }
    }

    @Override
    @ValidateParams
    public boolean cleanWorkflow(@ValidateSecurePathParam(name = "url") final String url, @ValidateStringParam(name = "site") final String site, final Set<DmDependencyTO> dependents) throws ServiceException {
        _cancelWorkflow(site, url);
        return true;
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @throws ServiceException
     */
    protected void goLive(final String site, final List<DmDependencyTO> submittedItems, String approver)
            throws ServiceException {
        goLive(site, submittedItems, approver, null);
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @throws ServiceException
     */
    protected void goLive(final String site, final List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext)
            throws ServiceException {
        // get web project information
        final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        if (submittedItems != null) {
            // group submitted items into packages by their scheduled date
            Map<ZonedDateTime, List<DmDependencyTO>> groupedPackages = groupByDate(submittedItems, now);

            for (ZonedDateTime scheduledDate : groupedPackages.keySet()) {
                List<DmDependencyTO> goLivePackage = groupedPackages.get(scheduledDate);
                if (goLivePackage != null) {
                    ZonedDateTime launchDate = scheduledDate.equals(now) ? null : scheduledDate;

                    final boolean isNotScheduled = (launchDate == null);
                    // for submit direct, package them together and submit them
                    // together as direct submit
                    final SubmitPackage submitpackage = new SubmitPackage("");
                    /*
                        dependencyPackage holds references of page.
                     */
                    final Set<String> rescheduledUris = new HashSet<String>();
                    final SubmitPackage dependencyPackage = new SubmitPackage("");
                    Set<String> processedUris = new HashSet<String>();
                    for (final DmDependencyTO dmDependencyTO : goLivePackage) {
                        goLivepackage(site, submitpackage, dmDependencyTO, isNotScheduled, dependencyPackage, approver, rescheduledUris, processedUris);
                    }
                    List<String> stringList = submitpackage.getPaths();
                    String label = submitpackage.getLabel();
                    SubmitLifeCycleOperation operation = null;
                    GoLiveContext context = new GoLiveContext(approver, site);
                    if (!isNotScheduled) {
                        Set<String> uris = new HashSet<String>();
                        uris.addAll(dependencyPackage.getUris());
                        uris.addAll(submitpackage.getUris());
                        label = getScheduleLabel(submitpackage, dependencyPackage);
                        operation = new PreScheduleOperation(this, uris, launchDate, context, rescheduledUris);
                    } else {
                        operation = new PreGoLiveOperation(this, submitpackage.getUris(), context, rescheduledUris);
                    }
                    if (!stringList.isEmpty()) {
                        // get the workflow initiator mapping
                        Map<String, String> submittedBy = new HashMap<String, String>();
                        for (String uri : stringList) {
                            dmPublishService.cancelScheduledItem(site, uri);
                        }
                        workflowProcessor.addToWorkflow(site, stringList, launchDate, label, operation, approver, mcpContext);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
    }

    protected void goLivepackage(String site, SubmitPackage submitpackage, DmDependencyTO dmDependencyTO, boolean isNotScheduled, SubmitPackage dependencyPackage, String approver, Set<String> rescheduledUris, Set<String> processedUris) {
        if (!processedUris.contains(dmDependencyTO.getUri())) {
            handleReferences(site, submitpackage, dmDependencyTO, isNotScheduled, dependencyPackage, approver, rescheduledUris, processedUris);
            List<DmDependencyTO> children = dmDependencyTO.getChildren();
            if (children != null) {
                for (DmDependencyTO child : children) {
                    handleReferences(site, submitpackage, child, isNotScheduled, dependencyPackage, approver, rescheduledUris, processedUris);
                    goLivepackage(site, submitpackage, child, isNotScheduled, dependencyPackage, approver, rescheduledUris, processedUris);
                }
            }
            processedUris.add(dmDependencyTO.getUri());
        }
    }

    protected String getScheduleLabel(SubmitPackage submitPackage, SubmitPackage dependencyPack) {
        StringBuilder builder = new StringBuilder("schedule_workflow:");
        builder.append(submitPackage.getLabel()).
                append(",").
                append(dependencyPack.getLabel());
        String label = builder.toString();
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }
        return label;

    }

    /**
     * submit the given list of paths to workflow
     *
     * @param site
     * @param launchDate
     * @param label
     * @param paths
     */
    @SuppressWarnings("deprecation")
    protected void submitToWorkflow(final String site, final ZonedDateTime launchDate, final String label, final List<String> paths) throws ServiceException {
        submitToWorkflow(site, launchDate, label, paths, null);
    }

    /**
     * submit the given list of paths to workflow
     *
     * @param site
     * @param launchDate
     * @param label
     * @param paths
     */
    @SuppressWarnings("deprecation")
    protected void submitToWorkflow(final String site, final ZonedDateTime launchDate, final String label, final List<String> paths, final MultiChannelPublishingContext mcpContext) throws ServiceException {
        _submit(site, launchDate, label, paths, mcpContext);
    }

    protected void _submit(String site, ZonedDateTime launchDate, String label, List<String> paths, MultiChannelPublishingContext mcpContext) {
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }

        // submit to workflow

        logger.debug("[WORKFLOW] w1,publish for " + label + ",start," + System.currentTimeMillis());

        dmPublishService.publish(site, paths, launchDate, mcpContext);

    }

    @Override
    @ValidateParams
    public boolean isRescheduleRequest(DmDependencyTO dependencyTO, @ValidateStringParam(name = "site") String site) {
        if ((dependencyTO.isDeleted() || (!dependencyTO.isSubmitted() && !dependencyTO.isInProgress()))) {
            ContentItemTO to = contentService.getContentItem(site, dependencyTO.getUri());
            ZonedDateTime newDate = dependencyTO.getScheduledDate();
            ZonedDateTime oldDate = to.getScheduledDate();
            return !areEqual(oldDate, newDate);
        }
        return false;
    }

    @Override
    public void preGoLive(Set<String> uris, GoLiveContext context, Set<String> rescheduledUris) {
        /* TODO: do we need this ?
        String approver = context.getApprover();
        String site = context.getSite();

        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        for (String uri : uris) {

            if (ContentUtils.matchesPatterns(uri, displayPatterns) || customContentTypeNotification) {
                String path = dmContentService.getContentFullPath(site, uri);
                final NodeRef node = persistenceManagerService.getNodeRef(path);
                if (node != null && StringUtils.isNotEmpty(approver)) {
                    persistenceManagerService.disableBehaviour(node, ContentModel.ASPECT_LOCKABLE);
                    persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_APPROVED_BY, approver);
                    persistenceManagerService.enableBehaviour(node, ContentModel.ASPECT_LOCKABLE);
                }
            }
        }
        */
    }

    @Override
    public void preSchedule(Set<String> uris, final ZonedDateTime date, final GoLiveContext context, Set<String> rescheduledUris) {
        /* TODO: do we need this?
        preGoLive(uris, context, rescheduledUris);
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        for (String path : uris) {
            String fullPath = dmContentService.getContentFullPath(context.getSite(), path);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                //dmStateManager.markScheduled(node, date, context.getSite());
                Map<QName, Serializable> nodeProperties = persistenceManagerService.getProperties(node);
                nodeProperties.put(WCMWorkflowModel.PROP_LAUNCH_DATE, date);
                persistenceManagerService.setProperties(node, nodeProperties);
            }
        }*/
    }

    @Override
    @SuppressWarnings("unchecked")
    @ValidateParams
    public ResultTO reject(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, String request) throws ServiceException {
        ResultTO result = new ResultTO();
        try {
            String approver = user;
            if (StringUtils.isEmpty(approver)) {
                approver = securityService.getCurrentUser();
            }
            JSONObject requestObject = JSONObject.fromObject(request);
            String reason = (requestObject.containsKey(JSON_KEY_REASON)) ? requestObject.getString(JSON_KEY_REASON) : "";
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            String scheduledDate = null;
            if (requestObject.containsKey(JSON_KEY_SCHEDULED_DATE)) {
                scheduledDate = requestObject.getString(JSON_KEY_SCHEDULED_DATE);
            }
            int length = items.size();
            if (length > 0) {
                SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
                List<DmDependencyTO> submittedItems = new ArrayList<DmDependencyTO>();
                for (int index = 0; index < length; index++) {
                    String stringItem = items.optString(index);
                    //JSONObject item = items.getJSONObject(index);
                    DmDependencyTO submittedItem = null; //getSubmittedItem(site, item, format, scheduledDate);
                    submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate, null);
                    submittedItems.add(submittedItem);
                }
                List<String> paths = new ArrayList<String>();
                for (DmDependencyTO goLiveItem : submittedItems) {
                    if (contentService.contentExists(site, goLiveItem.getUri())) {
                        paths.add(goLiveItem.getUri());
                    }
                }
                objectStateService.setSystemProcessingBulk(site, paths, true);
                reject(site, submittedItems, reason, approver);
                objectStateService.setSystemProcessingBulk(site, paths, false);
                result.setSuccess(true);
                result.setStatus(200);
                result.setMessage(notificationService.getNotificationMessage(site, NotificationMessageType
                        .CompleteMessages, NotificationService.COMPLETE_REJECT, Locale.ENGLISH));
            } else {
                result.setSuccess(false);
                result.setMessage("No items provided for preparation.");
            }
        } catch (JSONException e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    protected void reject(String site, List<DmDependencyTO> submittedItems, String reason, String approver) {
        if (submittedItems != null) {
            // for each top level items submitted
            // add its children and dependencies that must go with the top level
            // item to the submitted aspect
            // and only submit the top level items to workflow
            for (DmDependencyTO dmDependencyTO : submittedItems) {
                DependencyRules rule = new DependencyRules(site);
                rule.setContentService(contentService);
                rule.setObjectStateService(objectStateService);
                rejectThisAndReferences(site, dmDependencyTO, rule, approver, reason);
                List<DmDependencyTO> children = dmDependencyTO.getChildren();
                if (children != null) {
                    for (DmDependencyTO child : children) {
                        rejectThisAndReferences(site, child, rule, approver, reason);
                    }
                }
            }
            if(!submittedItems.isEmpty()) {
                // for some reason ,  submittedItems.get(0).getSubmittedBy() returns empty and
                // metadata for the same value is also empty , using last modify to blame the rejection.
                final ItemMetadata metaData = objectMetadataManager.getProperties(site, submittedItems.get(0).getUri
                        ());
                String whoToBlame = "admin"; //worst case, we need someone to blame.
                if(metaData!=null && StringUtils.isNotBlank(metaData.getModifier())){
                    whoToBlame=metaData.getModifier();
                }
                notificationService.notifyContentRejection(site, whoToBlame, getDeploymentPaths(submittedItems),
                    reason, approver, Locale.ENGLISH);
            }
        }

        // TODO: send the reason to the user
    }

    protected void rejectThisAndReferences(String site, DmDependencyTO dmDependencyTO, DependencyRules rule, String approver, String reason) {
        _reject(site, dmDependencyTO, approver, true, reason);
        Set<DmDependencyTO> dependencyTOSet = rule.applyRejectRule(dmDependencyTO);
        for (DmDependencyTO dependencyTO : dependencyTOSet) {
            boolean lsendEmail = true;
            try {
                ContentItemTO contentItem = contentService.getContentItem(site, dependencyTO.getUri());
                lsendEmail = !contentItem.isDocument() && !contentItem.isComponent() && !contentItem.isAsset();
            } catch (Exception e) {
                logger.error("during rejection, content retrieve failed");
                lsendEmail = false;
            }
            _reject(site, dependencyTO, approver, lsendEmail, reason);
        }
    }

    protected void _reject(String site, DmDependencyTO dmDependencyTO, String approver, boolean sendEmail, String reason) {
        boolean contentExists = contentService.contentExists(site, dmDependencyTO.getUri());
        if (contentExists) {
            if (!objectMetadataManager.metadataExist(site, dmDependencyTO.getUri())) {
                objectMetadataManager.insertNewObjectMetadata(site, dmDependencyTO.getUri());
            }

            Map<String, Object> newProps = new HashMap<String, Object>();
            newProps.put(ItemMetadata.PROP_SUBMITTED_BY, "");
            newProps.put(ItemMetadata.PROP_SEND_EMAIL, 0);
            newProps.put(ItemMetadata.PROP_SUBMITTED_FOR_DELETION, 0);
            newProps.put(ItemMetadata.PROP_LAUNCH_DATE, null);
            objectMetadataManager.setObjectMetadata(site, dmDependencyTO.getUri(), newProps);
            ContentItemTO item = contentService.getContentItem(site, dmDependencyTO.getUri());
            objectStateService.transition(site, item, TransitionEvent.REJECT);
        }
    }

    /* ================= */
    protected boolean isItemRenamed(String site, DmDependencyTO item) {
        if (item.getUri().endsWith(DmConstants.XML_PATTERN) || !item.getUri().contains(".")) {
            return objectMetadataManager.isRenamed(site, item.getUri());
        } else {
            // if not xml or a folder, skip checking if renamed
            return false;
        }
    }

    // End Rename Service Methods
     /* ================ */


    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) { this.dmFilterWrapper = dmFilterWrapper; }

    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public void setDeploymentService(DeploymentService deploymentService) { this.deploymentService = deploymentService; }

    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public WorkflowProcessor getWorkflowProcessor() { return workflowProcessor; }
    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) { this.workflowProcessor = workflowProcessor; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public NotificationService getNotificationService() { return notificationService; }
    public void setNotificationService(final org.craftercms.studio.api.v2.service.notification.NotificationService notificationService) { this.notificationService = notificationService; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public boolean isEnablePublishingWithoutDependencies() {
        boolean toReturn = Boolean.parseBoolean(studioConfiguration.getProperty(WORKFLOW_PUBLISHING_WITHOUT_DEPENDENCIES_ENABLED));
        return toReturn;
    }

    protected ServicesConfig servicesConfig;
    protected DeploymentService deploymentService;
    protected ContentService contentService;
    protected DmFilterWrapper dmFilterWrapper;
    protected DependencyService dependencyService;
    protected ObjectStateService objectStateService;
    protected DmPublishService dmPublishService;
    protected GeneralLockService generalLockService;
    protected SecurityService securityService;
    protected SiteService siteService;
    protected WorkflowProcessor workflowProcessor;
    protected ObjectMetadataManager objectMetadataManager;
    protected NotificationService notificationService;
    protected StudioConfiguration studioConfiguration;

    public static class SubmitPackage {
        protected String pathPrefix;
        protected Set<String> paths = new HashSet<String>();
        protected Set<DmDependencyTO> items = new HashSet<DmDependencyTO>();
        protected Set<String> uris = new HashSet<String>();

        protected StringBuilder builder = new StringBuilder();

        public SubmitPackage(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        public void addToPackage(String relativePath) {
            paths.add(pathPrefix + relativePath);
            builder.append(relativePath).append(", ");
            uris.add(relativePath);
        }

        public void addToPackage(DmDependencyTO item) {
            paths.add(pathPrefix + item.getUri());
            builder.append(item).append(", ");
            items.add(item);
            uris.add(item.getUri());
        }

        public Set<String> getUris() {
            return uris;
        }

        public List<String> getPaths() {
            return new ArrayList<String>(paths);
        }

        public Set<DmDependencyTO> getItems() {
            return items;
        }

        public String getLabel() {
            String label = builder.toString();
            if (label.length() > 255) {
                label = label.substring(0, 252) + "..";
            }
            return label;
        }
    }
}

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
package org.craftercms.studio.impl.v1.service.deployment;

import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.*;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.*;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.DeploymentEndpointConfig;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.deployment.DeployerFactory;
import org.craftercms.studio.impl.v1.service.deployment.job.DeployContentToEnvironmentStore;
import org.craftercms.studio.impl.v1.service.deployment.job.PublishContentToDeploymentTarget;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.Reactor;
import reactor.event.Event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    private static final int HISTORY_ALL_LIMIT = 9999999;
    private final static String CONTENT_TYPE_ALL= "all";

    private static int CTED_AUTOINCREMENT = 0;
    private static int PSD_AUTOINCREMENT = 0;

    @ValidateParams
    public void deploy(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<String> paths, Date scheduledDate, @ValidateStringParam(name = "approver") String approver, @ValidateStringParam(name = "submissionComment") String submissionComment, final boolean scheduleDateNow) throws DeploymentException {

        if (scheduledDate != null && scheduledDate.after(new Date())) {
            objectStateService.transitionBulk(site, paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SUBMIT_WITHOUT_WORKFLOW_SCHEDULED, org.craftercms.studio.api.v1.service.objectstate.State.NEW_SUBMITTED_NO_WF_SCHEDULED);
            objectStateService.setSystemProcessingBulk(site, paths, false);
        } else {
            objectStateService.setSystemProcessingBulk(site, paths, true);
        }

        List<String> newPaths = new ArrayList<String>();
        List<String> updatedPaths = new ArrayList<String>();
        List<String> movedPaths = new ArrayList<String>();

        Map<String, List<String>> groupedPaths = new HashMap<String, List<String>>();

        for (String p : paths) {
            ContentItemTO item = contentService.getContentItem(site, p, 0);
            if (item.isFolder()) {
                logger.debug("Content item at path " + p + " for site " + site + " is folder and will not be added to publishing queue.");
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

        groupedPaths.put(CopyToEnvironment.Action.NEW, newPaths);
        groupedPaths.put(CopyToEnvironment.Action.MOVE, movedPaths);
        groupedPaths.put(CopyToEnvironment.Action.UPDATE, updatedPaths);

        List<CopyToEnvironment> items = createItems(site, environment, groupedPaths, scheduledDate, approver, submissionComment);
        for (CopyToEnvironment item : items) {
            copyToEnvironmentMapper.insertItemForDeployment(item);
        }
        // We need to pick up this on Inserting , not on execution!
        try {
            sendContentApprovalEmail(items, scheduleDateNow);
        }catch(Exception errNotify) {
            logger.error("Error sending approval notification ",errNotify);
        }
}

    protected void sendContentApprovalEmail(List<CopyToEnvironment> itemList,boolean scheduleDateNow) {
        if(notificationService.isEnable()) {
            for (CopyToEnvironment listItem : itemList) {
                ObjectMetadata objectMetadata = objectMetadataManager.getProperties(listItem.getSite(), listItem.getPath());
                if (objectMetadata != null) {
                    if (objectMetadata.getSendEmail() == 1) {
                        // found the first item that needs to be sent
                        notificationService.notifyContentApproval(listItem.getSite(),
                            objectMetadata.getSubmittedBy(),
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
    }

    private List<String> getPathRelativeToSite(final List<CopyToEnvironment> itemList) {
        List<String> paths = new ArrayList<String>(itemList.size());
        for (CopyToEnvironment copyToEnvironment : itemList) {
            paths.add(copyToEnvironment.getPath());
        }
        return paths;
    }

    private List<CopyToEnvironment> createItems(String site, String environment, Map<String, List<String>> paths, Date scheduledDate, String approver, String submissionComment) {
        List<CopyToEnvironment> newItems = new ArrayList<CopyToEnvironment>(paths.size());
        for (String action : paths.keySet()) {
            for (String path : paths.get(action)) {
                CopyToEnvironment item = new CopyToEnvironment();
                item.setId(++CTED_AUTOINCREMENT);
                item.setSite(site);
                item.setEnvironment(environment);
                item.setPath(path);
                item.setScheduledDate(scheduledDate);
                item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
                item.setAction(action);
                if (objectMetadataManager.isRenamed(site, path)) {
                    String oldPath = objectMetadataManager.getOldPath(site, item.getPath());
                    item.setOldPath(oldPath);
                }
                String contentTypeClass = contentService.getContentTypeClass(site, path);
                item.setContentTypeClass(contentTypeClass);
                item.setUser(approver);
                item.setSubmissionComment(submissionComment);
                newItems.add(item);
            }
        }
        return newItems;
    }

    @Override
    @ValidateParams
    public void delete(@ValidateStringParam(name = "site") String site, List<String> paths, @ValidateStringParam(name = "approver") String approver, Date scheduledDate) throws DeploymentException {
        if (scheduledDate != null && scheduledDate.after(new Date())) {
            objectStateService.transitionBulk(site, paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.DELETE, org.craftercms.studio.api.v1.service.objectstate.State.NEW_DELETED);
            objectStateService.setSystemProcessingBulk(site, paths, false);
        } else {
            objectStateService.setSystemProcessingBulk(site, paths, true);
        }
        Set<String> environments = getAllPublishingEnvironments(site);
        for (String environment : environments) {
            List<CopyToEnvironment> items = createDeleteItems(site, environment, paths, approver, scheduledDate);
            for (CopyToEnvironment item : items) {
                copyToEnvironmentMapper.insertItemForDeployment(item);
            }
        }
    }

    protected Set<String> getAllPublishingEnvironments(String site) {
        Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        Set<String> environments = new HashSet<String>();
        if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
            for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
                if (StringUtils.isNotEmpty(groupConfigTO.getName())) {
                    environments.add(groupConfigTO.getName());
                }
            }
        }
        return environments;
    }

    private List<CopyToEnvironment> createDeleteItems(String site, String environment, List<String> paths, String approver, Date scheduledDate) {
        List<CopyToEnvironment> newItems = new ArrayList<CopyToEnvironment>(paths.size());
        for (String path : paths) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("site", site);
            params.put("environment", environment);
            params.put("path", path);
            params.put("state", CopyToEnvironment.State.COMPLETED);
            boolean renamed = objectMetadataManager.isRenamed(site, path);
            String oldPath = null;
            if (renamed) {
                oldPath = objectMetadataManager.getOldPath(site, path);
                params.put("path", oldPath);
            }

            int numDeployments = copyToEnvironmentMapper.checkIfItemWasPublishedForEnvironment(params);
            if (numDeployments > 0) {
                CopyToEnvironment item = new CopyToEnvironment();
                item.setId(++CTED_AUTOINCREMENT);
                item.setSite(site);
                item.setEnvironment(environment);
                item.setPath(path);
                if (renamed) {
                    item.setOldPath(oldPath);
                }
                item.setScheduledDate(scheduledDate);
                item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
                item.setAction(CopyToEnvironment.Action.DELETE);
                String contentTypeClass = contentService.getContentTypeClass(site, path);
                item.setContentTypeClass(contentTypeClass);
                item.setUser(approver);
                newItems.add(item);
            } else {
                params = new HashMap<String, String>();
                params.put("site", site);
                params.put("path", path);
                params.put("state", CopyToEnvironment.State.COMPLETED);
                numDeployments = copyToEnvironmentMapper.checkIfItemWasPublishedForEnvironment(params);
                if (numDeployments < 1) {
                    boolean haschildren = false;
                    if (path.endsWith("/" + DmConstants.INDEX_FILE)) {
                        String fullPath = contentService.expandRelativeSitePath(site, path.replace("/" + DmConstants.INDEX_FILE, ""));
                        if (contentService.contentExists(fullPath)) {
                            RepositoryItem[] children = contentRepository.getContentChildren(fullPath);

                            if (children.length > 1) {
                                haschildren = true;
                            }
                        }
                    }

                    if (contentService.contentExists(site, path)) {
                        contentService.deleteContent(site, path, approver);

                        if (!haschildren) {
                            deleteFolder(site, path.replace("/" + DmConstants.INDEX_FILE, ""), approver);
                        }
                    }
                }
            }
        }
        return newItems;
    }

    private void deleteFolder(String site, String path, String user) {
        String fullPath = contentService.expandRelativeSitePath(site, path);
        if (contentService.contentExists(fullPath)) {
            RepositoryItem[] children = contentRepository.getContentChildren(fullPath);

            if (children.length < 1) {
                contentService.deleteContent(site, path, false, user);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user);
            }
        }
    }

    @Override
    @ValidateParams
    public void deleteDeploymentDataForSite(@ValidateStringParam(name = "site") final String site) {
        signalWorkersToStop();
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        copyToEnvironmentMapper.deleteDeploymentDataForSite(params);
        publishToTargetMapper.deleteDeploymentDataForSite(params);
        deploymentSyncHistoryMapper.deleteDeploymentDataForSite(params);
        signalWorkersToContinue();
    }


    private void signalWorkersToContinue() {
        DeployContentToEnvironmentStore.signalToStop(false);
        PublishContentToDeploymentTarget.signalToStop(false);
    }

    private void signalWorkersToStop() {
        DeployContentToEnvironmentStore.signalToStop(true);
        PublishContentToDeploymentTarget.signalToStop(true);
        while (DeployContentToEnvironmentStore.isRunning() && PublishContentToDeploymentTarget.isRunning()) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                logger.info("Interrupted while waiting to stop workers", e);
            }
        }
    }

    @Override
    @ValidateParams
    public List<CopyToEnvironment> getScheduledItems(@ValidateStringParam(name = "site") String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getScheduledItems(params);
    }

    @Override
    @ValidateParams
    public void cancelWorkflow(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws DeploymentException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", new Date());
        copyToEnvironmentMapper.cancelWorkflow(params);
    }

    @Override
    @ValidateParams
    public void cancelWorkflowBulk(@ValidateStringParam(name = "site") String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("paths", paths);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", new Date());
        copyToEnvironmentMapper.cancelWorkflowBulk(params);
    }

    @Override
    @ValidateParams
    public List<DmDeploymentTaskTO> getDeploymentHistory(@ValidateStringParam(name = "site") String site, @ValidateIntegerParam(name = "daysFromToday") int daysFromToday, @ValidateIntegerParam(name = "numberOfItems") int numberOfItems, @ValidateStringParam(name = "sort") String sort, boolean ascending, @ValidateStringParam(name = "filterType") String filterType) {
        // get the filtered list of attempts in a specific date range
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - (1000L * 60L * 60L * 24L * daysFromToday));
        List<DeploymentSyncHistory> deployReports = getDeploymentHistory(site, fromDate, toDate, filterType, numberOfItems); //findDeploymentReports(site, fromDate, toDate);
        List<DmDeploymentTaskTO> tasks = new ArrayList<DmDeploymentTaskTO>();

        if (deployReports != null) {
            int count = 0;
            SimpleDateFormat deployedFormat = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_DEPLOYED);
            deployedFormat.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
            String timezone = servicesConfig.getDefaultTimezone(site);
            for (int index = 0; index < deployReports.size() && count < numberOfItems; index++) {
                DeploymentSyncHistory entry = deployReports.get(index);
                ContentItemTO deployedItem = getDeployedItem(entry.getSite(), entry.getPath());
                if (deployedItem != null) {
                    Set<String> permissions = securityService.getUserPermissions(site, deployedItem.getUri(), securityService.getCurrentUser(), Collections.<String>emptyList());
                    if (permissions.contains(CStudioConstants.PERMISSION_VALUE_PUBLISH)) {
                        deployedItem.eventDate = entry.getSyncDate();
                        deployedItem.endpoint = entry.getTarget();
                        String deployedLabel = ContentFormatUtils.formatDate(deployedFormat, entry.getSyncDate(), timezone);
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
                        count++;
                    }
                }
            }
        }
        return tasks;
    }

    protected List<DeploymentSyncHistory> getDeploymentHistory(String site, Date fromDate, Date toDate, String filterType, int numberOfItems) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("from_date", fromDate);
        params.put("to_date", toDate);
        if (numberOfItems <= 0) {
            params.put("limit", HISTORY_ALL_LIMIT);
        } else {
            params.put("limit", numberOfItems);
        }
        if (!filterType.equalsIgnoreCase(CONTENT_TYPE_ALL)) {
            params.put("filter", filterType);
        }
        return deploymentSyncHistoryMapper.getDeploymentHistory(params);
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
            ActivityFeed activity = activityService.getDeletedActivity(site, path);
            if (activity != null) {
                JSONObject summaryObject = JSONObject.fromObject(activity.getSummary());
                if (summaryObject.containsKey(CStudioConstants.CONTENT_TYPE)) {
                    String contentType = (String)summaryObject.get(CStudioConstants.CONTENT_TYPE);
                    item.contentType = contentType;
                }
                if(summaryObject.containsKey(CStudioConstants.INTERNAL_NAME)) {
                    String internalName = (String)summaryObject.get(CStudioConstants.INTERNAL_NAME);
                    item.internalName = internalName;
                }
                if(summaryObject.containsKey(CStudioConstants.BROWSER_URI)) {
                    String browserUri = (String)summaryObject.get(CStudioConstants.BROWSER_URI);
                    item.browserUri = browserUri;
                }
            }
            item.setLockOwner("");
        } else {
            item = contentService.getContentItem(site, path, 0);
        }
        return item;

    }

    @Override
    @ValidateParams
    public List<ContentItemTO> getScheduledItems(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "sort") String sort, boolean ascending, @ValidateStringParam(name = "subSort") String subSort, boolean subAscending, @ValidateStringParam(name = "filterType") String filterType) throws ServiceException {
        if (StringUtils.isEmpty(sort)) {
            sort = DmContentItemComparator.SORT_EVENT_DATE;
        }
        DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, true, true);
        DmContentItemComparator subComparator = new DmContentItemComparator(subSort, subAscending, true, true);
        List<ContentItemTO> items = null;
        items = getScheduledItems(site, comparator, subComparator, filterType);
        return items;
    }

    protected List<ContentItemTO> getScheduledItems(String site, DmContentItemComparator comparator, DmContentItemComparator subComparator, String filterType) {
        List<ContentItemTO> results = new FastArrayList();
        List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
        List<CopyToEnvironment> deploying = getScheduledItems(site);
        SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_SCHEDULED);
        List<ContentItemTO> scheduledItems = new ArrayList<ContentItemTO>();
        for (CopyToEnvironment deploymentItem : deploying) {
            String fullPath = contentService.expandRelativeSitePath(site, deploymentItem.getPath());
            Set<String> permissions = securityService.getUserPermissions(site, deploymentItem.getPath(), securityService.getCurrentUser(), Collections.<String>emptyList());
            if (permissions.contains(CStudioConstants.PERMISSION_VALUE_PUBLISH)) {
                addScheduledItem(site, deploymentItem.getScheduledDate(), format, fullPath, results, comparator, subComparator, displayPatterns, filterType);
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
    protected void addScheduledItem(String site, Date launchDate, SimpleDateFormat format, String fullPath,
                                    List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                    DmContentItemComparator subComparator, List<String> displayPatterns, String filterType) {
        try {
            addToScheduledDateList(site, launchDate, format, fullPath,
                scheduledItems, comparator, subComparator, displayPatterns, filterType);
            String relativePath = contentService.getRelativeSitePath(site, fullPath);
            if(!(relativePath.endsWith("/" + DmConstants.INDEX_FILE) || relativePath.endsWith(DmConstants.XML_PATTERN))) {
                relativePath = relativePath + "/" + DmConstants.INDEX_FILE;
            }
            addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,displayPatterns,filterType,relativePath);
        } catch (ServiceException e) {
            logger.error("failed to read " + fullPath + ". " + e.getMessage());
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
     * @throws ServiceException
     */
    protected void addToScheduledDateList(String site, Date launchDate, SimpleDateFormat format, String fullPath,
                                          List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                          DmContentItemComparator subComparator, List<String> displayPatterns, String filterType) throws ServiceException {
        String timeZone = servicesConfig.getDefaultTimezone(site);
        String dateLabel = ContentFormatUtils.formatDate(format, launchDate, timeZone);
        DmPathTO path = new DmPathTO(fullPath);
        // add only if the current node is a file (directories are
        // deployed with index.xml)
        // display only if the path matches one of display patterns
        if (ContentUtils.matchesPatterns(path.getRelativePath(), displayPatterns)) {
            ContentItemTO itemToAdd = contentService.getContentItem(site, path.getRelativePath(), 0);
            if (dmFilterWrapper.accept(site, itemToAdd, filterType)) {
                itemToAdd.scheduledDate = launchDate;
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

    protected void addDependendenciesToSchdeuleList(String site,
                                                    Date launchDate,
                                                    SimpleDateFormat format,
                                                    List<ContentItemTO>scheduledItems,
                                                    DmContentItemComparator comparator,
                                                    DmContentItemComparator subComparator,
                                                    List<String> displayPatterns,
                                                    String filterType,
                                                    String relativePath) throws ServiceException {

        Set<String> dependencyPaths = dependencyService.getItemDependencies(site, relativePath,1);
        _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, displayPatterns, filterType, dependencyPaths);
    }

    protected ContentItemTO createDateItem(String name, ContentItemTO itemToAdd, DmContentItemComparator comparator, String timeZone) {
        ContentItemTO dateItem = new ContentItemTO();
        dateItem.name = name;
        dateItem.internalName = name;
        dateItem.eventDate = itemToAdd.scheduledDate;
        dateItem.scheduledDate = itemToAdd.scheduledDate;
        dateItem.timezone = timeZone;
        dateItem.addChild(itemToAdd, comparator, false);
        return dateItem;
    }

    protected void _addDependendenciesToSchdeuleList(String site,
                                                     Date launchDate,
                                                     SimpleDateFormat format,
                                                     List<ContentItemTO>scheduledItems,
                                                     DmContentItemComparator comparator,
                                                     DmContentItemComparator subComparator,
                                                     List<String> displayPatterns,
                                                     String filterType,
                                                     Set<String> dependencies) throws ServiceException {
        if(dependencies != null) {
            for(String dependency : dependencies) {
                if (objectStateService.isNew(site, dependency)) {
                    String fullPath = contentService.expandRelativeSitePath(site, dependency);
                    if(objectStateService.isScheduled(site, dependency)) {
                        addScheduledItem(site,launchDate,format,fullPath,scheduledItems,comparator,subComparator,displayPatterns,filterType);
                        if(dependency.endsWith(DmConstants.XML_PATTERN)) {
                            addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,displayPatterns,filterType,dependency);
                        }
                    }
                }
            }
        }
    }

    @Override
    @ValidateParams
    public Map<String, List<PublishingChannelTO>> getAvailablePublishingChannelGroups(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        List<PublishingChannelTO> channelsTO = getAvailablePublishingChannelGroupsForSite(site, path);
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

    protected List<PublishingChannelTO> getAvailablePublishingChannelGroupsForSite(String site, String path) {
        List<PublishingChannelTO> channelTOs = new ArrayList<PublishingChannelTO>();
        List<PublishingChannelGroupConfigTO> channels = getPublishingChannels(site);
        for (PublishingChannelGroupConfigTO ch : channels) {
            PublishingChannelTO chTO = new PublishingChannelTO();
            chTO.setName(ch.getName());
            chTO.setPublish(true);
            chTO.setUpdateStatus(false);
            chTO.setOrder(ch.getOrder());
            channelTOs.add(chTO);
        }
        Collections.sort(channelTOs, new Comparator<PublishingChannelTO>() {
            @Override
            public int compare(PublishingChannelTO o1, PublishingChannelTO o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        return channelTOs;
    }

    protected List<PublishingChannelGroupConfigTO> getPublishingChannels(String site) {
        List<PublishingChannelGroupConfigTO> channels = new ArrayList<PublishingChannelGroupConfigTO>();
        Map<String, PublishingChannelGroupConfigTO> channelGroupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        List<PublishingChannelGroupConfigTO> channelGroupConfigs = new ArrayList<>(channelGroupConfigTOs.values());
        Collections.sort(channelGroupConfigs, new Comparator<PublishingChannelGroupConfigTO>() {
            @Override
            public int compare(PublishingChannelGroupConfigTO o1, PublishingChannelGroupConfigTO o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        String user = securityService.getCurrentUser();
        Set<String> userRoles = new HashSet<>();
        if (StringUtils.isNotEmpty(user)) {
            userRoles = securityService.getUserRoles(site, user);
        }
        for (PublishingChannelGroupConfigTO configTO : channelGroupConfigs) {
            if (CollectionUtils.isEmpty(configTO.getRoles())) {
                channels.add(configTO);
            } else {
                if (CollectionUtils.containsAny(configTO.getRoles(), userRoles)) {
                    channels.add(configTO);
                }
            }
        }
        return channels;
    }

    @Override
    @ValidateParams
    public void setupItemsForPublishingSync(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        List<PublishToTarget> items = createItems(site, environment, itemsToDeploy);
        for (PublishToTarget item : items) {
            publishToTargetMapper.insertItemForTargetSync(item);
        }
    }

    private List<PublishToTarget> createItems(String site, String environment, List<CopyToEnvironment> itemsToDeploy) {
        Calendar cal = Calendar.getInstance();
        long currentTimestamp = cal.getTimeInMillis();
        List<PublishToTarget> newItems = new ArrayList<PublishToTarget>(itemsToDeploy.size());
        for (CopyToEnvironment itemToDeploy : itemsToDeploy) {
            PublishToTarget item = new PublishToTarget();
            item.setId(++PSD_AUTOINCREMENT);
            item.setSite(site);
            item.setEnvironment(itemToDeploy.getEnvironment());
            item.setPath(itemToDeploy.getPath());
            item.setUsername(itemToDeploy.getUser());
            item.setVersion(currentTimestamp);
            if (StringUtils.equals(itemToDeploy.getAction(), CopyToEnvironment.Action.NEW)) {
                item.setAction(PublishToTarget.Action.NEW);
            } else if (StringUtils.equals(itemToDeploy.getAction(), CopyToEnvironment.Action.MOVE)) {
                item.setAction(PublishToTarget.Action.MOVE);
                item.setOldPath(itemToDeploy.getOldPath());
            } else if (StringUtils.equals(itemToDeploy.getAction(), CopyToEnvironment.Action.DELETE)) {
                item.setAction(PublishToTarget.Action.DELETE);
                item.setOldPath(itemToDeploy.getOldPath());
            } else {
                item.setAction(PublishToTarget.Action.UPDATE);
            }
            item.setContentTypeClass(itemToDeploy.getContentTypeClass());
            newItems.add(item);
        }
        return newItems;
    }

    @Override
    @ValidateParams
    public List<PublishToTarget> getItemsToSync(@ValidateStringParam(name = "site") String site, @ValidateLongParam(name = "targetVersion") long targetVersion, List<String> environments) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("version", targetVersion);
        params.put("environments", environments);
        List<PublishToTarget> queue = publishToTargetMapper.getItemsReadyForTargetSync(params);
        return queue;
    }

    @Override
    public void insertDeploymentHistory(DeploymentEndpointConfigTO target, List<PublishToTarget> publishedItems, Date publishingDate) {
        List<DeploymentSyncHistory> items = createItems(target, publishedItems, publishingDate);
        for (DeploymentSyncHistory item : items) {
            deploymentSyncHistoryMapper.insertDeploymentSyncHistoryItem(item);
        }
    }

    private List<DeploymentSyncHistory> createItems(DeploymentEndpointConfigTO target, List<PublishToTarget> publishedItems, Date publishingDate) {
        List<DeploymentSyncHistory> items = new ArrayList<DeploymentSyncHistory>(publishedItems.size());

        for (PublishToTarget item : publishedItems) {
            DeploymentSyncHistory historyItem = new DeploymentSyncHistory();
            historyItem.setSite(item.getSite());
            historyItem.setPath(item.getPath());
            historyItem.setEnvironment(item.getEnvironment());
            historyItem.setSyncDate(publishingDate);
            historyItem.setTarget(target.getName());
            historyItem.setUser(item.getUsername());
            historyItem.setContentTypeClass(item.getContentTypeClass());
            items.add(historyItem);
        }

        return items;
    }

    @Override
    @ValidateParams
    public void syncAllContentToPreview(@ValidateStringParam(name = "site") String site) throws ServiceException {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);

        String sessionTicket = securityService.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);
        repositoryReactor.notify(EBusConstants.REPOSITORY_PREVIEW_SYNC_EVENT, Event.wrap(message));
    }

    protected void syncFolder(String site, String path, Deployer deployer) {
        RepositoryItem[] children = contentRepository.getContentChildren(path);

        for (RepositoryItem item : children) {
            if (item.isFolder) {
                syncFolder(site, item.path + "/" + item.name, deployer);
            } else {
                try {
                    deployer.deployFile(site, contentService.getRelativeSitePath(site, item.path + "/" + item.name));
                } catch (DeploymentException e) {
                    e.printStackTrace();logger.error("Error while saving content to preview [site: {0}] [path: {1}]", e, site, item.path + "/" + item.name);
                }
            }
        }
    }

    @Override
    @ValidateParams
    public List<CopyToEnvironment> getDeploymentQueue(@ValidateStringParam(name = "site") String site) throws ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        List<String> states = new ArrayList<String>();
        states.add(CopyToEnvironment.State.READY_FOR_LIVE);
        states.add(CopyToEnvironment.State.PROCESSING);
        params.put("states", states);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getItemsBySiteAndStates(params);
    }

    @Override
    @ValidateParams
    public List<PublishToTarget> getSyncTargetQueue(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "endpoint") String endpoint, @ValidateLongParam(name = "targetVersion") long targetVersion) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("version", targetVersion);
        params.put("environments", getEndpontEnvironments(site, endpoint));
        return publishToTargetMapper.getItemsReadyForTargetSync(params);
    }

    protected Set<String> getEndpontEnvironments(String site, String endpoint) {
        Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        Set<String> environments = new HashSet<String>();
        Map<String, DeploymentEndpointConfigTO> targetMap = new HashMap<String, DeploymentEndpointConfigTO>();
        if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
            for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
                List<PublishingChannelConfigTO> channelConfigTOs = groupConfigTO.getChannels();
                if (channelConfigTOs != null && channelConfigTOs.size() > 0) {
                    for (PublishingChannelConfigTO channelConfigTO : channelConfigTOs) {
                        DeploymentEndpointConfigTO endpointTO = siteService.getDeploymentEndpoint(site, channelConfigTO.getName());
                        if (endpointTO != null && StringUtils.equals(endpoint, endpointTO.getName())) {
                            environments.add(groupConfigTO.getName());
                        }
                    }
                }
            }
        }
        return environments;
    }

    @Override
    @ValidateParams
    public List<DeploymentEndpointConfigTO> getDeploymentEndpoints(@ValidateStringParam(name = "site") String site) {
        DeploymentConfigTO config = deploymentEndpointConfig.getSiteDeploymentConfig(site);
        return new ArrayList<>(config.getEndpointMapping().values());
    }

    @Override
    @ValidateParams
    public boolean cancelDeployment(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateLongParam(name = "deploymentId") long deploymentId) throws ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("id", deploymentId);
        params.put("canceledState", CopyToEnvironment.State.CANCELED);
        copyToEnvironmentMapper.cancelDeployment(params);
        return true;
    }

    @Override
    @ValidateParams
    public void bulkGoLive(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, @ValidateSecurePathParam(name = "path") String path) throws ServiceException {
        dmPublishService.bulkGoLive(site, environment, path);
    }

    @Override
    public List<DeploymentJobTO> getDeploymentJobs() {
        List<DeploymentJobTO> jobList = new ArrayList<DeploymentJobTO>();

        DeploymentJobTO copyToEnvStoreJob = new DeploymentJobTO();
        copyToEnvStoreJob.setId(deployContentToEnvironmentStoreJob.getClass().getCanonicalName());
        copyToEnvStoreJob.setName(deployContentToEnvironmentStoreJob.getClass().getSimpleName());
        copyToEnvStoreJob.setEnabled(deployContentToEnvironmentStoreJob.isMasterPublishingNode());
        copyToEnvStoreJob.setRunning(false);
        try {
            copyToEnvStoreJob.setHost(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            logger.debug("Error while getting host information");
        }
        jobList.add(copyToEnvStoreJob);

        DeploymentJobTO publishToTargetJob = new DeploymentJobTO();
        publishToTargetJob.setId(publishContentToDeploymentTargetJob.getClass().getCanonicalName());
        publishToTargetJob.setName(publishContentToDeploymentTargetJob.getClass().getSimpleName());
        publishToTargetJob.setEnabled(publishContentToDeploymentTargetJob.isMasterPublishingNode());
        publishToTargetJob.setRunning(false);
        try {
            publishToTargetJob.setHost(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            logger.debug("Error while getting host information");
        }
        jobList.add(publishToTargetJob);

        return jobList;
    }

    @Override
    @ValidateParams
    public void bulkDelete(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        dmPublishService.bulkDelete(site, path);
    }

    @Override
    @ValidateParams
    public void publishItems(@ValidateStringParam(name = "site") String site,
                             @ValidateStringParam(name = "environment") String environment,
                             Date schedule, List<String> paths,
                             @ValidateStringParam(name = "submissionComment") String submissionComment)
            throws ServiceException, DeploymentException {

        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
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
            schedule = new Date();
        }
        deploy(site, environment, asList, schedule, approver, submissionComment, scheduledDateIsNow);
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setActivityService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
        this.dmFilterWrapper = dmFilterWrapper;
    }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public DeployerFactory getDeployerFactory() { return deployerFactory; }
    public void setDeployerFactory(DeployerFactory deployerFactory) { this.deployerFactory = deployerFactory; }

    public Reactor getRepositoryReactor() { return repositoryReactor; }
    public void setRepositoryReactor(Reactor repositoryReactor) { this.repositoryReactor = repositoryReactor; }

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }

    public DeploymentEndpointConfig getDeploymentEndpointConfig() { return deploymentEndpointConfig; }
    public void setDeploymentEndpointConfig(DeploymentEndpointConfig deploymentEndpointConfig) { this.deploymentEndpointConfig = deploymentEndpointConfig; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public DeployContentToEnvironmentStore getDeployContentToEnvironmentStoreJob() { return deployContentToEnvironmentStoreJob; }
    public void setDeployContentToEnvironmentStoreJob(DeployContentToEnvironmentStore deployContentToEnvironmentStoreJob) { this.deployContentToEnvironmentStoreJob = deployContentToEnvironmentStoreJob; }

    public PublishContentToDeploymentTarget getPublishContentToDeploymentTargetJob() { return publishContentToDeploymentTargetJob; }
    public void setPublishContentToDeploymentTargetJob(PublishContentToDeploymentTarget publishContentToDeploymentTargetJob) { this.publishContentToDeploymentTargetJob = publishContentToDeploymentTargetJob; }


    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ActivityService activityService;
    protected DependencyService dependencyService;
    protected DmFilterWrapper dmFilterWrapper;
    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ObjectMetadataManager objectMetadataManager;
    protected ContentRepository contentRepository;
    protected DeployerFactory deployerFactory;
    protected Reactor repositoryReactor;
    protected DmPublishService dmPublishService;
    protected DeploymentEndpointConfig deploymentEndpointConfig;
    protected SecurityService securityService;

    protected DeployContentToEnvironmentStore deployContentToEnvironmentStoreJob;
    protected PublishContentToDeploymentTarget publishContentToDeploymentTargetJob;
    protected NotificationService notificationService;
    @Autowired
    protected DeploymentSyncHistoryMapper deploymentSyncHistoryMapper;

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;

    @Autowired
    protected PublishToTargetMapper publishToTargetMapper;
}

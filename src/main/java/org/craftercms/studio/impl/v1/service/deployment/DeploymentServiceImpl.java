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
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.*;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.*;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.impl.v1.service.deployment.job.DeployContentToEnvironmentStore;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;

/**
 */
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    private static int CTED_AUTOINCREMENT = 0;

    public void deploy(String site, String environment, List<String> paths, Date scheduledDate, String approver, String submissionComment, final boolean scheduleDateNow) throws DeploymentException {

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
            if (objectStateService.isNew(site, p)) {
                newPaths.add(p);
            } else if (objectMetadataManager.isRenamed(site, p)) {
                movedPaths.add(p);
            } else {
                updatedPaths.add(p);
            }
        }

        groupedPaths.put(CopyToEnvironment.Action.NEW, newPaths);
        groupedPaths.put(CopyToEnvironment.Action.MOVE, movedPaths);
        groupedPaths.put(CopyToEnvironment.Action.UPDATE, updatedPaths);

        environment = resolveEnvironment(site, environment);

        List<CopyToEnvironment> items = createItems(site, environment, groupedPaths, scheduledDate, approver, submissionComment);
        for (CopyToEnvironment item : items) {
            copyToEnvironmentMapper.insertItemForDeployment(item);
        }
        // We need to pick up this on Inserting , not on execution!
        try {
            sendContentApprovalEmail(items, scheduleDateNow);
        } catch(Exception errNotify) {
            logger.error("Error sending approval notification ", errNotify);
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

    protected void sendContentApprovalEmail(List<CopyToEnvironment> itemList, boolean scheduleDateNow) {
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
                ObjectMetadata metadata = objectMetadataManager.getProperties(site, path);
                item.setId(++CTED_AUTOINCREMENT);
                item.setSite(site);
                item.setEnvironment(environment);
                item.setPath(path);
                item.setScheduledDate(scheduledDate);
                item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
                item.setAction(action);
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
                item.setSubmissionComment(submissionComment);
                newItems.add(item);
            }
        }
        return newItems;
    }

    @Override
    public void delete(String site, List<String> paths, String approver, Date scheduledDate) throws DeploymentException {
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

    private List<CopyToEnvironment> createDeleteItems(String site, String environment, List<String> paths, String approver, Date scheduledDate) {
        List<CopyToEnvironment> newItems = new ArrayList<CopyToEnvironment>(paths.size());
        for (String path : paths) {
            CopyToEnvironment item = new CopyToEnvironment();
            item.setId(++CTED_AUTOINCREMENT);
            item.setSite(site);
            item.setEnvironment(environment);
            item.setPath(path);
            item.setScheduledDate(scheduledDate);
            item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
            item.setAction(CopyToEnvironment.Action.DELETE);
            if (objectMetadataManager.isRenamed(site, path)) {
                String oldPath = objectMetadataManager.getOldPath(site, item.getPath());
                item.setOldPath(oldPath);
            }
            String contentTypeClass = contentService.getContentTypeClass(site, path);
            item.setContentTypeClass(contentTypeClass);
            item.setUser(approver);
            newItems.add(item);


            boolean haschildren = false;
            if (path.endsWith("/" + DmConstants.INDEX_FILE)) {
                String folderPath = path.replace("/" + DmConstants.INDEX_FILE, "");
                if (contentService.contentExists(site, folderPath)) {
                    RepositoryItem[] children = contentRepository.getContentChildren(site, folderPath);

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
        return newItems;
    }

    private void deleteFolder(String site, String path, String user) {
        if (contentService.contentExists(site, path)) {
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);

            if (children.length < 1) {
                contentService.deleteContent(site, path, false, user);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user);
            }
        }
    }

    @Override
    public void deleteDeploymentDataForSite(final String site) {
        signalWorkersToStop();
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        copyToEnvironmentMapper.deleteDeploymentDataForSite(params);
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
    public List<CopyToEnvironment> getScheduledItems(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getScheduledItems(params);
    }

    @Override
    public void cancelWorkflow(String site, String path) throws DeploymentException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", new Date());
        copyToEnvironmentMapper.cancelWorkflow(params);
    }

    @Override
    public List<DmDeploymentTaskTO> getDeploymentHistory(String site, int daysFromToday, int numberOfItems, String sort, boolean ascending, String filterType) {
        // get the filtered list of attempts in a specific date range
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - (1000L * 60L * 60L * 24L * daysFromToday));
        List<DeploymentSyncHistory> deployReports = deploymentHistoryProvider.getDeploymentHistory(site, fromDate, toDate, dmFilterWrapper, filterType, numberOfItems); //findDeploymentReports(site, fromDate, toDate);
        List<DmDeploymentTaskTO> tasks = new ArrayList<DmDeploymentTaskTO>();

        if (deployReports != null) {
            int count = 0;
            SimpleDateFormat deployedFormat = new SimpleDateFormat(StudioConstants.DATE_FORMAT_DEPLOYED);
            deployedFormat.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
            String timezone = servicesConfig.getDefaultTimezone(site);
            Set<String> processedItems = new HashSet<String>();
            for (int index = 0; index < deployReports.size() && count < numberOfItems; index++) {
                DeploymentSyncHistory entry = deployReports.get(index);
                if (!processedItems.contains(entry.getPath())) {
                    ContentItemTO deployedItem = getDeployedItem(entry.getSite(), entry.getPath());
                    if (deployedItem != null) {
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
                        processedItems.add(entry.getPath());
                    }
                }
            }
        }
        return tasks;
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
                if (summaryObject.containsKey(StudioConstants.CONTENT_TYPE)) {
                    String contentType = (String)summaryObject.get(StudioConstants.CONTENT_TYPE);
                    item.contentType = contentType;
                }
                if(summaryObject.containsKey(StudioConstants.INTERNAL_NAME)) {
                    String internalName = (String)summaryObject.get(StudioConstants.INTERNAL_NAME);
                    item.internalName = internalName;
                }
                if(summaryObject.containsKey(StudioConstants.BROWSER_URI)) {
                    String browserUri = (String)summaryObject.get(StudioConstants.BROWSER_URI);
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
    public List<ContentItemTO> getScheduledItems(String site, String sort, boolean ascending, String subSort, boolean subAscending, String filterType) throws ServiceException {
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
        SimpleDateFormat format = new SimpleDateFormat(StudioConstants.DATE_FORMAT_SCHEDULED);
        List<ContentItemTO> scheduledItems = new ArrayList<ContentItemTO>();
        for (CopyToEnvironment deploymentItem : deploying) {
            Set<String> permissions = securityService.getUserPermissions(site, deploymentItem.getPath(), securityService.getCurrentUser(), Collections.<String>emptyList());
            if (permissions.contains(StudioConstants.PERMISSION_VALUE_PUBLISH)) {
                addScheduledItem(site, deploymentItem.getScheduledDate(), format, deploymentItem.getPath(), results, comparator, subComparator, displayPatterns, filterType);
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
    protected void addScheduledItem(String site, Date launchDate, SimpleDateFormat format, String path,
                                    List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                    DmContentItemComparator subComparator, List<String> displayPatterns, String filterType) {
        try {
            addToScheduledDateList(site, launchDate, format, path,
                scheduledItems, comparator, subComparator, displayPatterns, filterType);
            if(!(path.endsWith("/" + DmConstants.INDEX_FILE) || path.endsWith(DmConstants.XML_PATTERN))) {
                path = path + "/" + DmConstants.INDEX_FILE;
            }
            addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,displayPatterns,filterType,path);
        } catch (ServiceException e) {
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
     * @throws ServiceException
     */
    protected void addToScheduledDateList(String site, Date launchDate, SimpleDateFormat format, String path,
                                          List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
                                          DmContentItemComparator subComparator, List<String> displayPatterns, String filterType) throws ServiceException {
        String timeZone = servicesConfig.getDefaultTimezone(site);
        String dateLabel = ContentFormatUtils.formatDate(format, launchDate, timeZone);
        // add only if the current node is a file (directories are
        // deployed with index.xml)
        // display only if the path matches one of display patterns
        if (ContentUtils.matchesPatterns(path, displayPatterns)) {
            ContentItemTO itemToAdd = contentService.getContentItem(site, path, 0);
            if (dmFilterWrapper.accept(site, itemToAdd, filterType)) {
                //itemToAdd.submitted = false;
                itemToAdd.scheduledDate = launchDate;
                //itemToAdd.inProgress = false;
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
                                                    String relativePath) {

        DmDependencyTO dmDependencyTo = dmDependencyService.getDependencies(site, relativePath, false, true);

        if (dmDependencyTo != null) {

            List<DmDependencyTO> pages = dmDependencyTo.getPages();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, displayPatterns, filterType, pages);

            List<DmDependencyTO> components = dmDependencyTo.getComponents();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, displayPatterns, filterType, components);

            List<DmDependencyTO> documents = dmDependencyTo.getDocuments();
            _addDependendenciesToSchdeuleList(site, launchDate, format, scheduledItems, comparator, subComparator, displayPatterns, filterType, documents);
        }

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
                                                     List<DmDependencyTO>dependencies) {
        if(dependencies != null) {
            for(DmDependencyTO dependencyTo:dependencies) {
                if (objectStateService.isNew(site, dependencyTo.getUri())) {
                    String uri = dependencyTo.getUri();
                    if(objectStateService.isScheduled(site, uri)) {
                        addScheduledItem(site,launchDate,format,uri,scheduledItems,comparator,subComparator,displayPatterns,filterType);
                        if(dependencyTo.getUri().endsWith(DmConstants.XML_PATTERN)) {
                            addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,displayPatterns,filterType,uri);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Map<String, List<PublishingChannelTO>> getAvailablePublishingChannelGroups(String site, String path) {
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
    public void syncAllContentToPreview(String site) throws ServiceException {
        PreviewEventContext context = new PreviewEventContext();
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);
    }

    protected void syncFolder(String site, String path, Deployer deployer) {
        RepositoryItem[] children = contentRepository.getContentChildren(site, path);

        for (RepositoryItem item : children) {
            if (item.isFolder) {
                syncFolder(site, item.path + "/" + item.name, deployer);
            } else {
                deployer.deployFile(site, item.path + "/" + item.name);
            }
        }
    }

    @Override
    public List<CopyToEnvironment> getDeploymentQueue(String site) throws ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        List<String> states = new ArrayList<String>();
        states.add(CopyToEnvironment.State.READY_FOR_LIVE);
        states.add(CopyToEnvironment.State.PROCESSING);
        params.put("states", states);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getItemsBySiteAndStates(params);
    }

    protected Set<String> getEndpontEnvironments(String site, String endpoint) {
        List<PublishingTargetTO> publishingTargets = siteService.getPublishingTargetsForSite(site);
        Set<String> environments = new HashSet<String>();
        if (publishingTargets != null && publishingTargets.size() > 0) {
            for (PublishingTargetTO target : publishingTargets) {
                environments.add(target.getRepoBranchName());
            }
        }
        return environments;
    }

    @Override
    public boolean cancelDeployment(String site, String path, long deploymentId) throws ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("id", deploymentId);
        params.put("canceledState", CopyToEnvironment.State.CANCELLED);
        copyToEnvironmentMapper.cancelDeployment(params);
        return true;
    }

    @Override
    public void bulkGoLive(String site, String environment, String path) {
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

        return jobList;
    }

    @Override
    public PublishStatus getPublishStatus(String site) throws SiteNotFoundException {
        return siteService.getPublishStatus(site);
    }

    @Override
    public Date getLastDeploymentDate(String site, String path) {
        return deploymentHistoryProvider.getLastDeploymentDate(site, path);
    }

    @Override
    public boolean enablePublishing(String site, boolean enabled) throws SiteNotFoundException {
        boolean toRet = siteService.enablePublishing(site, enabled);
        String message = StringUtils.EMPTY;
        SimpleDateFormat sdf = new SimpleDateFormat(StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
        if (enabled) {
            message = studioConfiguration.getProperty(StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STARTED_USER);
        } else {
            message = studioConfiguration.getProperty(StudioConfiguration.JOB_DEPLOY_CONTENT_TO_ENVIRONMENT_STATUS_MESSAGE_STOPPED_USER);
        }
        message = message.replace("{username}", securityService.getCurrentUser()).replace("{datetime}",sdf.format(new Date()));
        siteService.updatePublishingStatusMessage(site, message);
        return toRet;
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

    public void setDmDependencyService(DmDependencyService dmDependencyService) {
        this.dmDependencyService = dmDependencyService;
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

    public DmPublishService getDmPublishService() { return dmPublishService; }
    public void setDmPublishService(DmPublishService dmPublishService) { this.dmPublishService = dmPublishService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public DeployContentToEnvironmentStore getDeployContentToEnvironmentStoreJob() { return deployContentToEnvironmentStoreJob; }
    public void setDeployContentToEnvironmentStoreJob(DeployContentToEnvironmentStore deployContentToEnvironmentStoreJob) { this.deployContentToEnvironmentStoreJob = deployContentToEnvironmentStoreJob; }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public DeploymentHistoryProvider getDeploymentHistoryProvider() { return deploymentHistoryProvider; }
    public void setDeploymentHistoryProvider(DeploymentHistoryProvider deploymentHistoryProvider) { this.deploymentHistoryProvider = deploymentHistoryProvider; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ActivityService activityService;
    protected DmDependencyService dmDependencyService;
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

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;

}

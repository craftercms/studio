/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
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

import javolution.util.FastList;
import net.sf.json.JSONObject;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.dal.*;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.CopyToEnvironmentItem;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.fsm.TransitionEvent;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDeploymentTaskTO;
import org.craftercms.studio.impl.v1.deployment.dal.DeploymentDAL;
import org.craftercms.studio.impl.v1.deployment.dal.DeploymentDALException;
import org.craftercms.studio.impl.v1.service.deployment.job.DeployContentToEnvironmentStore;
import org.craftercms.studio.impl.v1.service.deployment.job.PublishContentToDeploymentTarget;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class DeploymentServiceImpl implements DeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentServiceImpl.class);

    private static final int HISTORY_ALL_LIMIT = 9999999;
    private final static String CONTENT_TYPE_ALL= "all";

    public void deploy(String site, String environment, List<String> paths, Date scheduledDate, String approver, String submissionComment) throws DeploymentException {

        if (scheduledDate != null && scheduledDate.after(new Date())) {
            _contentRepository.stateTransition(site, paths, TransitionEvent.SCHEDULED_DEPLOYMENT);
            _contentRepository.setSystemProcessing(site, paths, false);
        } else {
            _contentRepository.setSystemProcessing(site, paths, true);
        }

        List<String> newPaths = new ArrayList<String>();
        List<String> updatedPaths = new ArrayList<String>();
        List<String> movedPaths = new ArrayList<String>();

        Map<CopyToEnvironmentItem.Action, List<String>> groupedPaths = new HashMap<CopyToEnvironmentItem.Action, List<String>>();

        for (String p : paths) {
            if (_contentRepository.isNew(site, p)) {
                newPaths.add(p);
            } else if (_contentRepository.isRenamed(site, p)) {
                movedPaths.add(p);
            } else {
                updatedPaths.add(p);
            }
        }

        groupedPaths.put(CopyToEnvironmentItem.Action.NEW, newPaths);
        groupedPaths.put(CopyToEnvironmentItem.Action.MOVE, movedPaths);
        groupedPaths.put(CopyToEnvironmentItem.Action.UPDATE, updatedPaths);

        // use dal to setup deploy to environment log
        if (!_contentRepository.environmentRepoExists(site, environment)) {
            _contentRepository.createEnvironmentRepo(site, environment);
        }
        _deploymentDAL.setupItemsToDeploy(site, environment, groupedPaths, scheduledDate, approver, submissionComment);
    }

    @Override
    public void delete(String site, List<String> paths, String approver, Date scheduledDate) throws DeploymentException {
        if (scheduledDate != null && scheduledDate.after(new Date())) {
            _contentRepository.stateTransition(site, paths, TransitionEvent.DELETE);
            _contentRepository.setSystemProcessing(site, paths, false);
        } else {
            _contentRepository.setSystemProcessing(site, paths, true);
        }
        Set<String> environments = _contentRepository.getAllPublishingEnvironments(site);
        for (String environment : environments) {
            _deploymentDAL.setupItemsToDelete(site, environment, paths, approver, scheduledDate);
        }
    }

    @Override
    public void deleteDeploymentDataForSite(final String site) {
        try {
            signalWorkersToStop();
            _deploymentDAL.deleteDeploymentDataForSite(site);
        } catch (DeploymentDALException e) {
            logger.error("Error while deleting deployment data for site {0}", e, site);
        } finally {
            signalWorkersToContinue();
        }
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
    public List<CopyToEnvironment> getScheduledItems(String site) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getScheduledItems(params);
    }

    @Override
    public void cancelWorkflow(String site, String path) throws DeploymentException {
        _deploymentDAL.cancelWorkflow(site, path);
    }

    @Override
    public List<DmDeploymentTaskTO> getDeploymentHistory(String site, int daysFromToday, int numberOfItems, String sort, boolean ascending, String filterType) {
        // get the filtered list of attempts in a specific date range
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - (1000L * 60L * 60L * 24L * daysFromToday));
        List<DeploymentSyncHistory> deployReports = getDeploymentHistory(site, fromDate, toDate, filterType, numberOfItems); //findDeploymentReports(site, fromDate, toDate);
        List<DmDeploymentTaskTO> tasks = new FastList<DmDeploymentTaskTO>();

        if (deployReports != null) {
            int count = 0;
            SimpleDateFormat deployedFormat = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_DEPLOYED);
            deployedFormat.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
            String timezone = servicesConfig.getDefaultTimezone(site);
            for (int index = 0; index < deployReports.size() && count < numberOfItems; index++) {
                DeploymentSyncHistory entry = deployReports.get(index);
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
                    count++;
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
        return _deploymentSyncHistoryMapper.getDeploymentHistory(params);
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
            taskItems = new FastList<ContentItemTO>();
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

        ContentItemTO item;
        //ServicesConfig servicesConfig = getService(ServicesConfig.class);
        //String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
        item = contentService.getContentItem(site, path);
        if (item == null) {
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
        }
        return item;

    }

    public void setDeploymentDAL(DeploymentDAL deploymentDAL) {
        this._deploymentDAL = deploymentDAL;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this._contentRepository = contentRepository;
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

    protected DeploymentDAL _deploymentDAL;
    protected ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected ActivityService activityService;

    @Autowired
    protected DeploymentSyncHistoryMapper _deploymentSyncHistoryMapper;
    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;
}

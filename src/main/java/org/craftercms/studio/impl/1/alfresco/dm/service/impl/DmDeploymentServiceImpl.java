/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import javolution.util.FastList;
//import net.sf.json.JSONObject;
//import org.craftercms.cstudio.alfresco.activityfeed.CStudioActivityFeedDAO;
//import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
//import org.craftercms.cstudio.alfresco.dm.filter.DmFilterWrapper;
//import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmDeploymentService;
//import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmDeploymentTaskTO;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
//import org.craftercms.cstudio.alfresco.service.api.CStudioActivityService;
//import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
//import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
//import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
//import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
//import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.api.service.deployment.DeploymentService;
import org.craftercms.studio.api.domain.DeploymentSyncHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
//import java.util.TimeZone;

public class DmDeploymentServiceImpl extends AbstractRegistrableService implements DmDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(DmDeploymentServiceImpl.class);

    //protected DmFilterWrapper _dmFilterWrapper;
    //public DmFilterWrapper getDmFilterWrapper() {
    //    return _dmFilterWrapper;
    //}
    //public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
    //    this._dmFilterWrapper = dmFilterWrapper;
    //}

    protected DeploymentService _deploymentService;
    public DeploymentService getDeploymentService() {
        return _deploymentService;
    }
    public void setDeploymentService(DeploymentService deploymentService) {
        this._deploymentService = deploymentService;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmDeploymentService.class, this);
    }

    /**
     * get a deployed item by the given path. If the item is new, it will be added to the itemsMap
     *
     * @param site
     * @param path
     * @return deployed item
     */
    /*
    protected DmContentItemTO getDeployedItem(String site, String path) {
        DmContentService dmContentService = getService(DmContentService.class);
        DmContentItemTO item;
        try {
            ServicesConfig servicesConfig = getService(ServicesConfig.class);
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
            item = persistenceManagerService.getContentItem(fullPath, false);
            if (item == null) {
                item = dmContentService.createDummyDmContentItemForDeletedNode(site, path);
                CStudioActivityService cStudioActivityService = getService(CStudioActivityService.class);
                CStudioActivityFeedDAO activity = cStudioActivityService.getDeletedActivity(site, path);
                if (activity != null) {
                    JSONObject summaryObject = JSONObject.fromObject(activity.getSummary());
                    if (summaryObject.containsKey(CStudioConstants.CONTENT_TYPE)) {
                        String contentType = (String)summaryObject.get(CStudioConstants.CONTENT_TYPE);
                        item.setContentType(contentType);
                    }
                    if(summaryObject.containsKey(CStudioConstants.INTERNAL_NAME)) {
                        String internalName = (String)summaryObject.get(CStudioConstants.INTERNAL_NAME);
                        item.setInternalName(internalName);
                    }
                    if(summaryObject.containsKey(CStudioConstants.BROWSER_URI)) {
                        String browserUri = (String)summaryObject.get(CStudioConstants.BROWSER_URI);
                        item.setBrowserUri(browserUri);
                    }
                }
            }
            return item;
        } catch (ContentNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ignoring : " + path + ". content could be deleted in " + site + ".", e);
            }
            item = dmContentService.createDummyDmContentItemForDeletedNode(site, path);
            CStudioActivityService cStudioActivityService = getService(CStudioActivityService.class);
            CStudioActivityFeedDAO activity = cStudioActivityService.getDeletedActivity(site, path);
            if (activity != null) {
                JSONObject summaryObject = JSONObject.fromObject(activity.getSummary());
                if (summaryObject.containsKey(CStudioConstants.CONTENT_TYPE)) {
                    String contentType = (String)summaryObject.get(CStudioConstants.CONTENT_TYPE);
                    item.setContentType(contentType);
                }
                if(summaryObject.containsKey(CStudioConstants.INTERNAL_NAME)) {
                    String internalName = (String)summaryObject.get(CStudioConstants.INTERNAL_NAME);
                    item.setInternalName(internalName);
                }
                if(summaryObject.containsKey(CStudioConstants.BROWSER_URI)) {
                    String browserUri = (String)summaryObject.get(CStudioConstants.BROWSER_URI);
                    item.setBrowserUri(browserUri);
                }
            }
            return item;
        } catch (ServiceException e) {
            logger.error("Error in retrieving : " + path + " in " + site + ".", e);
        }
        return null;

    }*/


    /**
     * create WcmDeploymentTask
     *
     * @param deployedLabel
     * @param item
     * @return deployment task
     */
    /*
    protected DmDeploymentTaskTO createDeploymentTask(String deployedLabel, DmContentItemTO item) {
        // otherwise just add as the last task
        DmDeploymentTaskTO task = new DmDeploymentTaskTO();
        task.setInternalName(deployedLabel);
        List<DmContentItemTO> taskItems = task.getChildren();
        if (taskItems == null) {
            taskItems = new FastList<DmContentItemTO>();
            task.setChildren(taskItems);
        }
        taskItems.add(item);
        task.setNumOfChildren(taskItems.size());
        return task;
    }*/
    
	@Override
	public List<DeploymentSyncHistory> getDeploymentHistory(String site, int daysFromToday, int numberOfItems, String sort, boolean ascending, String filterType) {
		 // get the filtered list of attempts in a specific date range
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - (1000L * 60L * 60L * 24L * daysFromToday));
        List<DeploymentSyncHistory> deployReports = _deploymentService.getDeploymentHistory(site,fromDate,toDate,filterType,numberOfItems); //findDeploymentReports(site, fromDate, toDate);
        List<DmDeploymentTaskTO> tasks = new FastList<DmDeploymentTaskTO>();
        /*
        if (deployReports != null) {
            int count = 0;
            SimpleDateFormat deployedFormat = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_DEPLOYED);
            ServicesConfig servicesConfig = getService(ServicesConfig.class);
            deployedFormat.setTimeZone(TimeZone.getTimeZone(servicesConfig.getDefaultTimezone(site)));
            String timezone = servicesConfig.getDefaultTimezone(site);
            for (int index = 0; index < deployReports.size() && count < numberOfItems; index++) {
                DeploymentSyncHistoryItem entry = deployReports.get(index);
                    DmContentItemTO deployedItem = getDeployedItem(entry.getSite(), entry.getPath());
                    if (deployedItem != null) {
                        deployedItem.setEventDate(entry.getSyncDate());
                        deployedItem.setEndpoint(entry.getTarget());
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
        }*/
        return deployReports;
	}
    
}

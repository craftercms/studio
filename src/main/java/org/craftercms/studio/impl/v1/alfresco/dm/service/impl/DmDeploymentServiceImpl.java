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
package org.craftercms.studio.impl.v1.alfresco.dm.service.impl;

import javolution.util.FastList;
import net.sf.json.JSONObject;
import org.craftercms.studio.api.domain.ActivityFeed;
import org.craftercms.studio.api.domain.DeploymentSyncHistory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.impl.v1.alfresco.constant.CStudioConstants;
import org.craftercms.studio.impl.v1.alfresco.dm.constant.DmConstants;
import org.craftercms.studio.impl.v1.alfresco.dm.service.api.DmDeploymentService;
import org.craftercms.studio.impl.v1.alfresco.dm.to.DmDeploymentTaskTO;
import org.craftercms.studio.impl.v1.alfresco.dm.to.DmPathTO;
import org.craftercms.studio.impl.v1.alfresco.service.AbstractRegistrableService;
import org.craftercms.studio.impl.v1.alfresco.service.api.CStudioActivityService;
import org.craftercms.studio.impl.v1.alfresco.service.api.ServicesConfig;
import org.craftercms.studio.impl.v1.alfresco.util.ContentFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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

    protected ContentService contentService;

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
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
    protected ContentItemTO getDeployedItem(String site, String path) {

        ContentItemTO item;
            //ServicesConfig servicesConfig = getService(ServicesConfig.class);
            //String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
            item = contentService.getContentItem(site, path);
            if (item == null) {
                item = createDummyDmContentItemForDeletedNode(site, path);
                CStudioActivityService cStudioActivityService = getService(CStudioActivityService.class);
                ActivityFeed activity = cStudioActivityService.getDeletedActivity(site, path);
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

    private ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath){
        String absolutePath = expandRelativeSitePath(site, relativePath);
        DmPathTO path = new DmPathTO(absolutePath);
        ContentItemTO item = new ContentItemTO();
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = path.getName();
        //String relativePath = path.getRelativePath();
        String fullPath = path.toString();
        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativePath.replace("/" + name, "") : relativePath;
        item.path = folderPath;
        /**
         * Internal name should be just folder name
         */
        String internalName = folderPath;
        int index = folderPath.lastIndexOf('/');
        if (index != -1)
        internalName = folderPath.substring(index + 1);

        item.internalName = internalName;
        //item.title = internalName;
        item.isDisabled = false;
        item.isNavigation = false;
        item.name = name;
        item.uri = relativePath;

        //item.defaultWebApp = path.getDmSitePath();
        //set content type based on the relative Path
        String contentType = getContentType(site, relativePath);
        item.contentType = contentType;
        if (contentType.equals(DmConstants.CONTENT_TYPE_COMPONENT)) {
            item.component = true;
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_DOCUMENT)) {
            item.document = true;
        }
        // set if the content is new
        item.isDeleted = true;
        item.isContainer = false;
        //item.isNewFile = false;
        item.isNew = false;
        item.isInProgress = false;
        item.timezone = servicesConfig.getDefaultTimezone(site);
        item.isPreviewable = false;
        item.browserUri = getBrowserUri(item);

        return item;
    }

    protected String expandRelativeSitePath(String site, String relativePath) {
        return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
    }

    protected String getBrowserUri(ContentItemTO item) {
        String replacePattern = "";
        //if (item.isLevelDescriptor) {
        //    replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        //} else if (item.isComponent()) {
        if (item.isComponent) {
            replacePattern = DmConstants.ROOT_PATTERN_COMPONENTS;
        } else if (item.isAsset) {
            replacePattern = DmConstants.ROOT_PATTERN_ASSETS;
        } else if (item.isDocument) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        }
        boolean isPage = !(item.isComponent || item.isAsset || item.isDocument);
        return getBrowserUri(item.uri, replacePattern, isPage);
    }

    protected static String getBrowserUri(String uri, String replacePattern, boolean isPage) {
        String browserUri = uri.replaceFirst(replacePattern, "");
        browserUri = browserUri.replaceFirst("/" + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = "/";
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
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

    protected String getContentType(String site, String uri) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site)) || uri.endsWith("/" + servicesConfig.getLevelDescriptorName(site))) {
            return DmConstants.CONTENT_TYPE_COMPONENT;
        } else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
            return DmConstants.CONTENT_TYPE_DOCUMENT;
        } else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
            return DmConstants.CONTENT_TYPE_ASSET;

        } else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
            return DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
        }
        return DmConstants.CONTENT_TYPE_PAGE;
    }

    protected boolean matchesPatterns(String uri, List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (uri.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }
    
	@Override
	public List<DmDeploymentTaskTO> getDeploymentHistory(String site, int daysFromToday, int numberOfItems, String sort, boolean ascending, String filterType) {
		 // get the filtered list of attempts in a specific date range
        Date toDate = new Date();
        Date fromDate = new Date(toDate.getTime() - (1000L * 60L * 60L * 24L * daysFromToday));
        List<DeploymentSyncHistory> deployReports = _deploymentService.getDeploymentHistory(site,fromDate,toDate,filterType,numberOfItems); //findDeploymentReports(site, fromDate, toDate);
        List<DmDeploymentTaskTO> tasks = new FastList<DmDeploymentTaskTO>();

        if (deployReports != null) {
            int count = 0;
            SimpleDateFormat deployedFormat = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_DEPLOYED);
            ServicesConfig servicesConfig = getService(ServicesConfig.class);
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
    
}

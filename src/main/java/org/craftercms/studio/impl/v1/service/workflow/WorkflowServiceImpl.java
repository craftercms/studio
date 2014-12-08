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
package org.craftercms.studio.impl.v1.service.workflow;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import javolution.util.FastList;
import javolution.util.FastTable;
import net.sf.json.JSONObject;
import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.impl.v1.service.workflow.dal.WorkflowJobDAL;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.GoLiveQueueOrganizer;

/**
 * workflow service implementation
 */
public class WorkflowServiceImpl implements WorkflowService {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

	public WorkflowJob createJob(String site, List<String> srcPaths,  String processName, Map<String, String> properties) {
		WorkflowJob job = _workflowJobDAL.createJob(site, srcPaths,  processName, properties);
		job.setCurrentStatus(WorkflowService.STATE_STARTED);
		job = _workflowJobDAL.updateJob(job);
		
		return job;
	}
	
	public List<WorkflowJob> getActiveJobs() {
		List<WorkflowJob> allJobs = _workflowJobDAL.getJobsByState(null);
		// Reverse scan and delete STATE_ENDED jobs from list
		for (int i = allJobs.size(); i > 0;) {
			WorkflowJob job = allJobs.get(--i);
			if (job.getCurrentStatus().equals(WorkflowService.STATE_ENDED))
				allJobs.remove(i);
		}
		return allJobs;
	}
	
	public List<WorkflowJob> getJobsInState(Set<String> states) {
		return _workflowJobDAL.getJobsByState(states);
	}

	public WorkflowJob getJob(String jobId) {
		return _workflowJobDAL.getJob(jobId);
	}
	
	public WorkflowJob updateJob(WorkflowJob job) {
		return _workflowJobDAL.updateJob(job);
	}
	
	public boolean deleteJob(String jobId) {
		return _workflowJobDAL.deleteJob(jobId);
	}
	
	public boolean startJob(String jobId) {
		return false;
	}

	public boolean transitionJobState(String jobId, String state) {
		return false;
	}
	
	public boolean endJob(String jobId) {
		return false;
	}

	@Override
	public void submitToGoLive(String site, List<String> paths, Date scheduledDate, boolean sendApprovedNotice, String submitter) {
		/*
		// this needs to be gutted an re-written as workflow handlers that rely on services like dependency, state, content repository
		// that use the appropriate DAL objects.  Now is not the time to pull the thread on that sweater :-/ 
		String submissionComment = "";
		//{"items":[{"asset":false,"assets":[],"browserUri":"/","categoryRoot":"","children":[{"asset":false,"assets":[],"browserUri":"/crafter-level-descriptor.level.xml","categoryRoot":"","children":[],"component":true,"components":[],"container":false,"contentType":"/component/level-descriptor","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-13T21:45:05","eventDateAsDate":{"date":13,"day":6,"hours":21,"minutes":45,"month":3,"seconds":5,"time":1365903905242,"timezoneOffset":240,"year":113},"floating":false,"form":"/component/level-descriptor","formPagePath":"simple","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"crafter-level-descriptor.level.xml","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":true,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"/site/website/index.xml","metaDescription":"","name":"crafter-level-descriptor.level.xml","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/552af5fc-c93e-4032-9e24-66c4f5609cf9","now":false,"numOfChildren":0,"orders":[],"pages":[],"parentPath":"/","path":"/site/website","previewable":false,"properties":{},"reference":false,"renderingTemplate":false,"renderingTemplates":[],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/site/website/crafter-level-descriptor.level.xml","user":"","userFirstName":"","userLastName":"","width":0,"workflowId":""},{"asset":false,"assets":[],"browserUri":"/about","categoryRoot":"","children":[],"component":false,"components":[],"container":true,"contentType":"/page/entry","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-18T22:35:22","eventDateAsDate":{"date":18,"day":4,"hours":22,"minutes":35,"month":3,"seconds":22,"time":1366338922741,"timezoneOffset":240,"year":113},"floating":false,"form":"/page/entry","formPagePath":"simple","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"About","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":false,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"/site/website/index.xml","metaDescription":"","name":"index.xml","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/ef1d4242-c03a-4eb0-b294-d90ae7246ec9","now":false,"numOfChildren":1,"orders":[{"disabled":"","id":"default","name":"","order":1000,"placeInNav":""}],"pages":[],"parentPath":"/","path":"/site/website/about","previewable":true,"properties":{},"reference":false,"renderingTemplate":false,"renderingTemplates":[{"asset":false,"assets":[],"browserUri":"/templates/web/entry.ftl","categoryRoot":"","children":[],"component":true,"components":[],"container":false,"contentType":"","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-13T21:56:03","eventDateAsDate":{"date":13,"day":6,"hours":21,"minutes":56,"month":3,"seconds":3,"time":1365904563607,"timezoneOffset":240,"year":113},"floating":false,"form":"","formPagePath":"","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"entry.ftl","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":false,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"/site/website/about/index.xml","metaDescription":"","name":"entry.ftl","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/5a234a22-8f4e-48d5-9da1-7797886e6776","now":false,"numOfChildren":0,"orders":[],"pages":[],"parentPath":"","path":"/templates/web","previewable":false,"properties":{},"reference":true,"renderingTemplate":false,"renderingTemplates":[],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/templates/web/entry.ftl","user":"","userFirstName":"","userLastName":"","width":0,"workflowId":""}],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/site/website/about/index.xml","user":"author","userFirstName":"author","userLastName":"author","width":0,"workflowId":""},{"asset":false,"assets":[],"browserUri":"/about/crafter-level-descriptor.level.xml","categoryRoot":"","children":[],"component":true,"components":[],"container":false,"contentType":"/component/level-descriptor","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-18T01:11:04","eventDateAsDate":{"date":18,"day":4,"hours":1,"minutes":11,"month":3,"seconds":4,"time":1366261864668,"timezoneOffset":240,"year":113},"floating":false,"form":"/component/level-descriptor","formPagePath":"simple","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"crafter-level-descriptor.level.xml","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":true,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"/site/website/about/index.xml","metaDescription":"","name":"crafter-level-descriptor.level.xml","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/873a99c8-109d-47ac-a245-7c2194b17fdf","now":false,"numOfChildren":0,"orders":[],"pages":[],"parentPath":"/about","path":"/site/website/about","previewable":false,"properties":{},"reference":false,"renderingTemplate":false,"renderingTemplates":[],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/site/website/about/crafter-level-descriptor.level.xml","user":"","userFirstName":"","userLastName":"","width":0,"workflowId":""}],"component":false,"components":[],"container":true,"contentType":"/page/entry","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-18T03:25:40","eventDateAsDate":{"date":18,"day":4,"hours":3,"minutes":25,"month":3,"seconds":40,"time":1366269940177,"timezoneOffset":240,"year":113},"floating":false,"form":"/page/entry","formPagePath":"simple","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"Home","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":false,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"","metaDescription":"","name":"index.xml","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/47a6796a-f6d0-46bc-a563-f9d08d941b01","now":true,"numOfChildren":3,"orders":[{"disabled":"","id":"default","name":"","order":-1,"placeInNav":""}],"pages":[],"parentPath":"","path":"/site/website","previewable":true,"properties":{},"reference":false,"renderingTemplate":false,"renderingTemplates":[{"asset":false,"assets":[],"browserUri":"/templates/web/entry.ftl","categoryRoot":"","children":[],"component":true,"components":[],"container":false,"contentType":"","defaultWebApp":"/wem-projects/test-fr/test-fr/work-area","deleted":false,"deletedItems":[],"directory":false,"disabled":false,"document":false,"documents":[],"endpoint":"","eventDate":"2013-04-13T21:56:03","eventDateAsDate":{"date":13,"day":6,"hours":21,"minutes":56,"month":3,"seconds":3,"time":1365904563607,"timezoneOffset":240,"year":113},"floating":false,"form":"","formPagePath":"","height":0,"hideInAuthoring":false,"inFlight":false,"inProgress":true,"internalName":"entry.ftl","lastEditDate":{},"lastEditDateAsString":"","levelDescriptor":false,"levelDescriptors":[],"live":false,"lockOwner":"","mandatoryParent":"/site/website/index.xml","metaDescription":"","name":"entry.ftl","navigation":false,"new":true,"newFile":true,"nodeRef":"workspace://SpacesStore/5a234a22-8f4e-48d5-9da1-7797886e6776","now":false,"numOfChildren":0,"orders":[],"pages":[],"parentPath":"","path":"/templates/web","previewable":false,"properties":{},"reference":true,"renderingTemplate":false,"renderingTemplates":[],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/templates/web/entry.ftl","user":"","userFirstName":"","userLastName":"","width":0,"workflowId":""}],"scheduled":false,"scheduledDate":"","scheduledDateAsDate":{},"submissionComment":"","submitted":false,"submittedByFirstName":"","submittedByLastName":"","submittedForDeletion":false,"timezone":"EST5EDT","title":"","uri":"/site/website/index.xml","user":"admin","userFirstName":"Administrator","userLastName":"","width":0,"workflowId":""}],"submissionComment":"","now":"true","scheduledDate":"2010-02-26T15:00:00","sendEmail":"true"}

		List<DmDependencyTO> submittedItems = new ArrayList<DmDependencyTO>();
		RequestContext requestContext = new RequestContext(site, submitter);

		for(String path : paths) {
			DmDependencyTO depTO = new DmDependencyTO();
			depTO.setUri(path);
			submittedItems.add(depTO);
		}
		
		try{
			List<DmError> errors = _dmSimpleWfService.submitToGoLive(
					submittedItems, 
					scheduledDate, 
					sendApprovedNotice, 
					false, //submit for delete,  
					requestContext, //request context, 
					submissionComment);
		}
		catch(Exception err) {
			logger.error("submitToGoLive", err);
		}
		*/
	}

	@Override
	public String getScheduledItems(String site, String sort, boolean ascending, String subSort, boolean subAscending, String filterType) throws ServiceException {
		if (StringUtils.isEmpty(sort)) {
			sort = DmContentItemComparator.SORT_EVENT_DATE;
		}
		DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, true, true);
		DmContentItemComparator subComparator = new DmContentItemComparator(subSort, subAscending, true, true);
		List<ContentItemTO> items = null;
		items = getScheduledItems(site, comparator, subComparator, filterType);
		JSONObject jsonObject = new JSONObject();
		int total = 0;
		if (items != null) {
			for (ContentItemTO item : items) {
				total += item.numOfChildren;
			}
		}
		jsonObject.put(CStudioConstants.PROPERTY_TOTAL, total);
		jsonObject.put(CStudioConstants.PROPERTY_SORTED_BY, sort);
		jsonObject.put(CStudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
		jsonObject.put(CStudioConstants.PROPERTY_DOCUMENTS, items);
		return jsonObject.toString();
	}

	protected List<ContentItemTO> getScheduledItems(String site, DmContentItemComparator comparator, DmContentItemComparator subComparator, String filterType) {
		List<ContentItemTO> results = new FastArrayList();
		List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
		List<CopyToEnvironment> deploying = deploymentService.getScheduledItems(site);
		SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_FORMAT_SCHEDULED);
		List<ContentItemTO> scheduledItems = new ArrayList<ContentItemTO>();
		for (CopyToEnvironment deploymentItem : deploying) {
			String fullPath = expandRelativeSitePath(site, deploymentItem.getPath());
			addScheduledItem(site, deploymentItem.getScheduledDate(), format, fullPath, results, comparator, subComparator, displayPatterns, filterType);
		}
		return results;
	}

	/**
	 * add a scheduled item created from the given node to the scheduled items
	 * list if the item is not a component or a static asset
	 *
	 * @param site
	 * @param launchDate
	 * @param node
	 * @param scheduledItems
	 * @param comparator
	 * @param displayPatterns
	 */
	protected void addScheduledItem(String site, Date launchDate, SimpleDateFormat format, String fullPath,
									List<ContentItemTO> scheduledItems, DmContentItemComparator comparator,
									DmContentItemComparator subComparator, List<String> displayPatterns, String filterType) {
		try {
			DmPathTO path = new DmPathTO(fullPath);
			addToScheduledDateList(site, launchDate, format, fullPath,
					scheduledItems, comparator, subComparator, displayPatterns, filterType);
			String relativePath = getRelativeSitePath(site, fullPath);
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
	 * @param node
	 * @param scheduledItems
	 * @param comparator
	 * @param subComparator
	 * @param taskId
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
			ContentItemTO itemToAdd = contentService.getContentItem(site, path.getRelativePath());
			if (dmFilterWrapper.accept(site, itemToAdd, filterType)) {
				itemToAdd.submitted = false;
				itemToAdd.scheduledDate = launchDate;
				itemToAdd.inProgress = false;
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

		DmDependencyTO dmDependencyTo = dmDependencyService.getDependencies(site, null, relativePath, false, true);

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
				if (true/*contentService.isNew(site, dependencyTo.getUri())*/) {
					String uri = dependencyTo.getUri();
					String fullPath = expandRelativeSitePath(site, uri);
					if(isNodeInScheduledStatus(fullPath)) {
						addScheduledItem(site,launchDate,format,fullPath,scheduledItems,comparator,subComparator,displayPatterns,filterType);
						if(dependencyTo.getUri().endsWith(DmConstants.XML_PATTERN)) {
							addDependendenciesToSchdeuleList(site,launchDate,format,scheduledItems,comparator,subComparator,displayPatterns,filterType,uri);
						}
					}
				}
			}
		}
	}

	protected boolean isNodeInScheduledStatus(String fullPath) {
		/*try {
			Serializable propValue = getService(PersistenceManagerService.class).getProperty(fullPath, CStudioContentModel.PROP_STATUS);
			if (propValue != null) {
				String status = (String)propValue;
				return DmConstants.DM_STATUS_SCHEDULED.equals(status);
			}
		} catch (Exception e) {
		}*/
		return false;
	}

	protected String expandRelativeSitePath(String site, String relativePath) {
		return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
	}

	protected String getRelativeSitePath(String site, String fullPath) {
		return fullPath.replace("/wem-projects/" + site + "/" + site + "/work-area", "");
	}

	@Override
	public String getGoLiveItems(String site, String sort, boolean ascending) throws ServiceException {
		DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, false, false);
		List<ContentItemTO> items = getGoLiveItems(site, comparator);
		JSONObject jsonObject = new JSONObject();
		int total = 0;
		if (items != null) {
			for (ContentItemTO item : items) {
				total += item.getNumOfChildren();
			}
		}
		jsonObject.put(CStudioConstants.PROPERTY_TOTAL, total);
		jsonObject.put(CStudioConstants.PROPERTY_SORTED_BY, sort);
		jsonObject.put(CStudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
		jsonObject.put(CStudioConstants.PROPERTY_DOCUMENTS, items);
		return jsonObject.toString();
	}

	protected List<ContentItemTO> getGoLiveItems(final String site, final DmContentItemComparator comparator) throws ServiceException {
		List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
		List<ContentItemTO> categoryItems = getCategoryItems(site);
		GoLiveQueue queue = new GoLiveQueue();
		fillQueue(site, queue, null);

		//}

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
		List<ContentItemTO> categories = new FastTable<ContentItemTO>();
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

	public void fillQueue(String site, GoLiveQueue goLiveQueue, GoLiveQueue inProcessQueue) throws ServiceException {
		//List<NodeRef> changeSet = searchService.findNodes(CStudioConstants.STORE_REF, getSubmittedItemsQuery(site));
		List<ObjectState> changeSet = objectStateService.getSubmittedItems(site);
		// TODO: implement list changed all

		// the category item to add all other items that do not belong to
		// regular categories specified in the configuration
		if (changeSet != null) {
			// add all content items from each task if task is the review task
			for (ObjectState state : changeSet) {
				try {
					ContentItemTO item = contentService.getContentItem(state.getSite(), state.getPath());
					addToQueue(site, goLiveQueue, inProcessQueue, item, state);
				} catch (Exception e) {
					logger.error("Could not warm cache for [" + state.getSite() + " : " + state.getPath() + "] " + e.getMessage());
				}
			}
		}
	}

	protected void addToQueue(String site, GoLiveQueue queue, GoLiveQueue inProcessQueue, ContentItemTO item, ObjectState itemState) throws ServiceException {
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

	public String getInProgressItems(String site, String sort, boolean ascending, boolean inProgressOnly) throws ServiceException {
		DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, true, true);
		List<ContentItemTO> items = getInProgressItems(site, comparator, inProgressOnly);
		JSONObject jsonObject = new JSONObject();
		int total = 0;
		if (items != null) {
			for (ContentItemTO item : items) {
				total += item.getNumOfChildren();
			}
		}
		jsonObject.put(CStudioConstants.PROPERTY_TOTAL, total);
		jsonObject.put(CStudioConstants.PROPERTY_SORTED_BY, sort);
		jsonObject.put(CStudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
		jsonObject.put(CStudioConstants.PROPERTY_DOCUMENTS, items);
		return jsonObject.toString();
	}

	protected List<ContentItemTO> getInProgressItems(final String site, final DmContentItemComparator comparator, final boolean inProgressOnly) throws ServiceException {
		final List<ContentItemTO> categoryItems = new FastList<ContentItemTO>();

		List<ContentItemTO>categoryItems1 = getCategoryItems(site);
		categoryItems.addAll(categoryItems1);


		long st = System.currentTimeMillis();
		//List<NodeRef> changeSet = persistenceManagerService.getChangeSet(site);
		List<ObjectState> changeSet = objectStateService.getChangeSet(site);

		logger.debug("Time taken listChangedAll()  " + (System.currentTimeMillis() - st));

		// the category item to add all other items that do not belong to
		// regular categories specified in the configuration
		st = System.currentTimeMillis();

		if (changeSet != null) {
			List<String> displayPatterns = servicesConfig.getDisplayInWidgetPathPatterns(site);
			//List<String> inProgressItems = new FastList<String>();
			for (ObjectState state : changeSet) {
				if (contentService.contentExists(state.getSite(), state.getPath())) {
					if (ContentUtils.matchesPatterns(state.getPath(), displayPatterns)) {
						ContentItemTO item = contentService.getContentItem(state.getSite(), state.getPath());
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
				String categoryPath = categoryItem.getPath() + "/";
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
	 * @param includeInProgress
	 * @param includeInProgress
	 * @return
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

	public void setWorkflowJobDAL(WorkflowJobDAL dal) { _workflowJobDAL = dal; }

//	public void setDmWorkflowService(DmWorkflowService service) { _dmSimpleWfService = service; }

	// // @Override
	public NotificationService getNotificationService() { return _notificationService; }
	public void setNotificationService(NotificationService service) { _notificationService = service; }

	public ServicesConfig getServicesConfig() { return servicesConfig; }
	public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

	public void setDmDependencyService(DmDependencyService dmDependencyService) {
		this.dmDependencyService = dmDependencyService;
	}

	public void setDmFilterWrapper(DmFilterWrapper dmFilterWrapper) {
		this.dmFilterWrapper = dmFilterWrapper;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setDeploymentService(DeploymentService deploymentService) {
		this.deploymentService = deploymentService;
	}

	public void setObjectStateService(ObjectStateService objectStateService) {
		this.objectStateService = objectStateService;
	}

	private WorkflowJobDAL _workflowJobDAL;
//	private DmWorkflowService _dmSimpleWfService;
	private NotificationService _notificationService;
	protected ServicesConfig servicesConfig;
	protected DeploymentService deploymentService;
	protected ContentService contentService;
	protected DmFilterWrapper dmFilterWrapper;
	protected DmDependencyService dmDependencyService;
	protected ObjectStateService objectStateService;
}
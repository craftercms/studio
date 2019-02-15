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
import java.util.*;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.listener.DmWorkflowListener;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmRenameService;
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
import org.craftercms.studio.api.v1.service.workflow.WorkflowJob;
import org.craftercms.studio.api.v1.service.workflow.WorkflowService;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;
import org.craftercms.studio.api.v1.service.workflow.context.RequestContext;
import org.craftercms.studio.api.v1.service.workflow.context.RequestContextBuilder;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DmContentItemComparator;
import org.craftercms.studio.api.v1.util.filter.DmFilterWrapper;
import org.craftercms.studio.api.v2.service.notification.NotificationMessageType;
import org.craftercms.studio.impl.v1.service.workflow.dal.WorkflowJobDAL;
import org.craftercms.studio.impl.v1.service.workflow.operation.*;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.GoLiveQueueOrganizer;

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
	public WorkflowJob createJob(@ValidateStringParam(name = "site") String site, List<String> srcPaths, @ValidateStringParam(name = "processName") String processName, Map<String, String> properties) {
		WorkflowJob job = _workflowJobDAL.createJob(site, srcPaths, processName, properties);
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

	@Override
	@ValidateParams
	public WorkflowJob getJob(@ValidateStringParam(name = "jobId") String jobId) {
		return _workflowJobDAL.getJob(jobId);
	}
	
	public WorkflowJob updateJob(WorkflowJob job) {
		return _workflowJobDAL.updateJob(job);
	}

	@Override
	@ValidateParams
	public boolean deleteJob(@ValidateStringParam(name = "jobId") String jobId) {
		return _workflowJobDAL.deleteJob(jobId);
	}

	@Override
	@ValidateParams
	public boolean startJob(@ValidateStringParam(name = "jobId") String jobId) {
		return false;
	}

	@Override
	@ValidateParams
	public boolean transitionJobState(@ValidateStringParam(name = "jobId") String jobId, @ValidateStringParam(name = "state") String state) {
		return false;
	}

	@Override
	@ValidateParams
	public boolean endJob(@ValidateStringParam(name = "jobId") String jobId) {
		return false;
	}

	@Override
    @ValidateParams
	public ResultTO submitToGoLive(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "username") String username, @ValidateStringParam(name = "request") String request) throws ServiceException {
		return submitForApproval(site, username, request, false);
	}

	protected ResultTO submitForApproval(final String site, String submittedBy, final String request, final boolean delete) throws ServiceException {
        long start = System.currentTimeMillis();
        RequestContext requestContext = RequestContextBuilder.buildSubmitContext(site, submittedBy);
        ResultTO result = new ResultTO();
        try {
            SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            JSONObject requestObject = JSONObject.fromObject(request);
            JSONArray items = requestObject.getJSONArray(JSON_KEY_ITEMS);
            int length = items.size();
            if (length > 0) {
                for (int index = 0; index < length; index++) {
                    objectStateService.setSystemProcessing(site, items.optString(index), true);
                }
            }
            boolean isNow = (requestObject.containsKey(JSON_KEY_IS_NOW)) ? requestObject.getBoolean(JSON_KEY_IS_NOW) : false;
            Date scheduledDate = null;
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
                List<String> submittedItemsPaths = new ArrayList<String>();
                for (int index = 0; index < length; index++) {
					String stringItem = items.optString(index);
                    submittedItemsPaths.add(stringItem);
                }
                List<String> submittedPaths = new ArrayList<String>();
                submittedPaths.addAll(submittedItemsPaths);
                submittedPaths.addAll(dependencyService.getPublishingDepenencies(site, submittedItemsPaths));
                objectStateService.setSystemProcessingBulk(site, submittedPaths, true);
                List<DmError> errors = submitToGoLive(submittedPaths, scheduledDate, sendEmail, delete, requestContext, submissionComment);
                result.setSuccess(true);
				result.setStatus(200);
				if(notificationService2.isEnable()){
					result.setMessage(notificationService2.getNotificationMessage(site, NotificationMessageType
						.CompleteMessages,COMPLETE_SUBMIT_TO_GO_LIVE_MSG,Locale.ENGLISH));
				}else {
					result.setMessage(notificationService.getCompleteMessage(site, NotificationService.COMPLETE_SUBMIT_TO_GO_LIVE));
				}
                objectStateService.setSystemProcessingBulk(site, submittedPaths, false);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            logger.error("Error while submitting content for approval.", e);
        }
        return result;

	}

    protected List<DmError> submitToGoLive(List<String> submittedPaths, Date scheduledDate, boolean sendEmail, boolean submitForDeletion, RequestContext requestContext, String submissionComment) throws ServiceException {
        List<DmError> errors = new ArrayList<DmError>();
        String site = requestContext.getSite();
        String submittedBy = requestContext.getUser();
        for (String submittedPath : submittedPaths) {
            try {
                doSubmit(site, submittedPath, scheduledDate, sendEmail, submitForDeletion, submittedBy, true, submissionComment);

            } catch (ContentNotFoundException e) {
                errors.add(new DmError(site, submittedPath, e));
            }
        }
		notificationService2.notifyApprovesContentSubmission(site,null,submittedPaths,submittedBy,scheduledDate,
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

	protected void submitThisAndReferredComponents(DmDependencyTO submittedItem, String site, Date scheduledDate, boolean sendEmail, boolean submitForDeletion, String submittedBy, DependencyRules rule, String submissionComment) throws ServiceException {
        doSubmit(site, submittedItem.getUri(), scheduledDate, sendEmail, submitForDeletion, submittedBy, true, submissionComment);
        Set<DmDependencyTO> stringSet;

        if (submitForDeletion) {
            stringSet = rule.applyDeleteDependencyRule(submittedItem);
        } else {
            stringSet = rule.applySubmitRule(submittedItem);
        }
        for (DmDependencyTO s : stringSet) {
            String fullPath = contentService.expandRelativeSitePath(site, s.getUri());
            ContentItemTO contentItem = contentService.getContentItem(site, s.getUri());
            boolean lsendEmail = true;
            boolean lnotifyAdmin = true;
            lsendEmail = sendEmail && ((!contentItem.isDocument() && !contentItem.isComponent() && !contentItem.isAsset()) || customContentTypeNotification);
            lnotifyAdmin = (!contentItem.isDocument() && !contentItem.isComponent() && !contentItem.isAsset());
            // notify admin will always be true, unless for dependent document/banner/other-files
            doSubmit(site, s.getUri(), scheduledDate, lsendEmail, submitForDeletion, submittedBy, lnotifyAdmin, submissionComment);
        }
    }

    protected void doSubmit(final String site, final String submittedPath, final Date scheduledDate, final boolean sendEmail, final boolean submitForDeletion, final String user, final boolean notifyAdmin, final String submissionComment) throws ServiceException {
        //first remove from workflow
        removeFromWorkflow(site, submittedPath, true);
        ContentItemTO item = contentService.getContentItem(site, submittedPath);

		Map<String, Object> properties = new HashMap<>();
		properties.put(ObjectMetadata.PROP_SUBMITTED_BY, user);
		properties.put(ObjectMetadata.PROP_SEND_EMAIL, sendEmail ? 1 : 0);
		properties.put(ObjectMetadata.PROP_SUBMITTED_FOR_DELETION, submitForDeletion ? 1 : 0);
		properties.put(ObjectMetadata.PROP_SUBMISSION_COMMENT, submissionComment);

        if (null == scheduledDate) {
			properties.put(ObjectMetadata.PROP_LAUNCH_DATE, null);
        } else {
			properties.put(ObjectMetadata.PROP_LAUNCH_DATE, scheduledDate);
        }
		if (!objectMetadataManager.metadataExist(site, submittedPath)) {
			objectMetadataManager.insertNewObjectMetadata(site, submittedPath);
		}
		objectMetadataManager.setObjectMetadata(site, submittedPath, properties);
        if (scheduledDate != null) {
            objectStateService.transition(site, item, TransitionEvent.SUBMIT_WITH_WORKFLOW_SCHEDULED);
        } else {
			objectStateService.transition(site, item, TransitionEvent.SUBMIT_WITH_WORKFLOW_UNSCHEDULED);
        }
        if (notifyAdmin) {
            boolean isPreviewable = item.isPreviewable();

            notificationService.sendContentSubmissionNotificationToApprovers(site, "admin", submittedPath, user, scheduledDate, isPreviewable, submitForDeletion);
        }

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
		result.put(CStudioConstants.PROPERTY_TOTAL, total);
		result.put(CStudioConstants.PROPERTY_SORTED_BY, sort);
		result.put(CStudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
		result.put(CStudioConstants.PROPERTY_DOCUMENTS, items);
		return result;
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

	public void fillQueue(String site, GoLiveQueue goLiveQueue, GoLiveQueue inProcessQueue) throws ServiceException {
		List<ObjectState> changeSet = objectStateService.getSubmittedItems(site);
		// TODO: implement list changed all

		// the category item to add all other items that do not belong to
		// regular categories specified in the configuration
		if (changeSet != null) {
			// add all content items from each task if task is the review task
			for (ObjectState state : changeSet) {
				try {
                    if (contentService.contentExists( state.getSite(), state.getPath())) {
                        ContentItemTO item = contentService.getContentItem(state.getSite(), state.getPath(), 0);
                        Set<String> permissions = securityService.getUserPermissions(site, item.getUri(), securityService.getCurrentUser(), Collections.<String>emptyList());
                        if (permissions.contains(CStudioConstants.PERMISSION_VALUE_PUBLISH)) {
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

	@Override
	@ValidateParams
	public Map<String, Object> getInProgressItems(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "sort") String sort, boolean ascending, boolean inProgressOnly) throws ServiceException {
		DmContentItemComparator comparator = new DmContentItemComparator(sort, ascending, true, true);
		List<ContentItemTO> items = getInProgressItems(site, comparator, inProgressOnly);
		JSONObject jsonObject = new JSONObject();
		int total = 0;
		if (items != null) {
			for (ContentItemTO item : items) {
				total += item.getNumOfChildren();
			}
		}
		Map<String, Object> result = new HashMap<>();
		result.put(CStudioConstants.PROPERTY_TOTAL, total);
		result.put(CStudioConstants.PROPERTY_SORTED_BY, sort);
		result.put(CStudioConstants.PROPERTY_SORT_ASCENDING, String.valueOf(ascending));
		result.put(CStudioConstants.PROPERTY_DOCUMENTS, items);
		return result;
	}

	protected List<ContentItemTO> getInProgressItems(final String site, final DmContentItemComparator comparator, final boolean inProgressOnly) throws ServiceException {
		final List<ContentItemTO> categoryItems = new ArrayList<>();

		List<ContentItemTO>categoryItems1 = getCategoryItems(site);
		categoryItems.addAll(categoryItems1);


		long st = System.currentTimeMillis();
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

	@Override
    @ValidateParams
	public boolean removeFromWorkflow(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "[ath") String path, boolean cancelWorkflow) throws ServiceException {
		Set<String> processedPaths = new HashSet<>();
		return removeFromWorkflow(site, path, processedPaths, cancelWorkflow);
	}

	protected boolean removeFromWorkflow(String site,  String path, Set<String> processedPaths, boolean cancelWorkflow) throws ServiceException {
		// remove submitted aspects from all dependent items
		if (!processedPaths.contains(path)) {
			processedPaths.add(path);
			//if (contentService.contentExists(site, path)) {
				//removeSubmittedAspect(site, fullPath, null, false, DmConstants.DM_STATUS_IN_PROGRESS);
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
                if (objectStateService.isSubmitted(site, affectedItem) || objectStateService.isScheduled(site, affectedItem)) {
                    deploymentService.cancelWorkflow(site, affectedItem);
                    ObjectMetadata objectMetadata = objectMetadataManager.getProperties(site, affectedItem);
                    if (objectMetadata != null) {
                        objectMetadata.setSubmittedBy(StringUtils.EMPTY);
                        objectMetadata.setSendEmail(0);
                        objectMetadata.setSubmittedForDeletion(0);
                        objectMetadata.setSubmissionComment(StringUtils.EMPTY);
                        objectMetadata.setLaunchDate(null);
                        objectMetadataManager.updateObjectMetadata(objectMetadata);
                    }
                    paths.add(affectedItem);
                }
            } catch (DeploymentException e) {
                logger.error("Error occurred while trying to cancel workflow for path [" + affectedItem + "], site " + site, e);
            }
        }
        objectStateService.transitionBulk(site, paths, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.REJECT, State.NEW_UNPUBLISHED_UNLOCKED);
	}

	/*
	    TODO: Method needs cleaning up and refactoring.
	 */
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

            Set<String> dependencyPaths = dependencyService.getPublishingDepenencies(site, affectedPaths);
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

    /*
        TODO: Method name is not meaningful.
     */
	@Override
    @ValidateParams
	public List<ContentItemTO> getWorkflowAffectedPaths(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws ServiceException {
		List<String> affectedPaths = getWorkflowAffectedPathsInternal(site, path);
        return getWorkflowAffectedItems(site, affectedPaths);
	}

	private void getMandatoryChildren(String site, String path, List<String> affectedPaths) {
		// TODO: check folders and list children
		/*
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		String parentPath = fullPath.replace("/" + DmConstants.INDEX_FILE, "");
		List<FileInfo> children = persistenceManagerService.list(parentPath);
		for (FileInfo child : children) {
			NodeRef childRef = child.getNodeRef();
			String childPath = persistenceManagerService.getNodePath(childRef);
			DmPathTO dmPathTO = new DmPathTO(childPath);
			if (!affectedPaths.contains(dmPathTO.getRelativePath())) {
				affectedPaths.add(dmPathTO.getRelativePath());
				getMandatoryChildren(childPath, affectedPaths);
			}
		}*/
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
		// TODO: copy to live repo node
		/*
		PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
		NodeRef node = persistenceManagerService.getNodeRef(fullPath);
		if (node != null) {
			NodeRef liveRepoNode = persistenceManagerService.getNodeRef(getPathFromLiveRepo(fullPath));
			if (liveRepoNode != null) {
				persistenceManagerService.copy(node, liveRepoNode);
			}
		}*/
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
    public ResultTO goDelete(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "request") String request, @ValidateStringParam(name = "user") String user) {
        String md5 = ContentUtils.getMd5ForFile(request);
        String id = site + ":" + user + ":" + md5;
        if (!generalLockService.tryLock(id)) {
            generalLockService.lock(id);
            generalLockService.unlock(id);
            return new ResultTO();
        }
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put(CStudioConstants.USER, user);
            //ThreadLocalContainer.set(map);
            return approve(site, request, Operation.DELETE);
        } finally {
            //ThreadLocalContainer.remove();
            generalLockService.unlock(id);
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
            if(operation!=Operation.DELETE && !dmPublishService.hasChannelsConfigure(site, mcpContext)){
                ResultTO toReturn = new ResultTO();
                List<PublishingChannelConfigTO> channelsList = siteService.getPublishingChannelGroupConfigs(site).get(mcpContext.getPublishingChannelGroup()).getChannels();
                String channels = StringUtils.join(channelsList, " ");
                toReturn.setMessage(" Specified target '"+channels+"' was not found. Please check if an endpoint or channel with name '"+channels+"' exists in site configuration");
                toReturn.setSuccess(false);
                toReturn.setInvalidateCache(false);
                return toReturn;
            }



            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);
                DmDependencyTO submittedItem = null;

				submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate);
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
                            if (!dmRenameService.isItemRenamed(site, item)) {
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
                        for (DmDependencyTO goLiveItem : goLiveItems) {
                            resolveSubmittedPaths(site, goLiveItem, goLivePaths);
                        }
                        List<String> nodeRefs = new ArrayList<>();
                        for (String fullPath : goLivePaths) {
                            String path = contentService.getRelativeSitePath(site, fullPath);
                            String lockId = site + ":" + path;
                            generalLockService.lock(lockId);

                        }
                        try {
                            goLive(site, goLiveItems, approver, mcpContext);
                        } finally {
                            for (String fullPath : goLivePaths) {
                                String path = contentService.getRelativeSitePath(site, fullPath);
                                String lockId = site + ":" + path;
                                generalLockService.unlock(lockId);
                            }
                        }
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            String fullPath = contentService.expandRelativeSitePath(site, renameItem.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            String fullPath = contentService.expandRelativeSitePath(site, renamedChild.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().after(new Date())) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }

                        dmRenameService.goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        String fullPath = contentService.expandRelativeSitePath(site, deletedItem.getUri());
                        //deletedItem.setScheduledDate(getScheduledDate(site, format, scheduledDate));
                        deletePaths.add(fullPath);
                        ContentItemTO contentItem = contentService.getContentItem(site, deletedItem.getUri());
                        if (contentItem != null) {
                            //nodeRefs.add(nodeRef.getId());
                        }
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
			if(notificationService2.isEnable()){
				result.setMessage(notificationService2.getNotificationMessage(site,
					NotificationMessageType.CompleteMessages,responseMessageKey,Locale.ENGLISH));
			}else{
				result.setMessage(notificationService.getCompleteMessage(site, responseMessageKey));
			}
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
            if(operation != Operation.DELETE && !dmPublishService.hasChannelsConfigure(site, mcpContext)){
                ResultTO toReturn = new ResultTO();
                List<PublishingChannelConfigTO> channelsList = siteService.getPublishingChannelGroupConfigs(site).get(mcpContext.getPublishingChannelGroup()).getChannels();
                String channels = StringUtils.join(channelsList, " ");
                toReturn.setMessage(" Specified target '"+channels+"' was not found. Please check if an endpoint or channel with name '"+channels+"' exists in site configuration");
                toReturn.setSuccess(false);
                toReturn.setInvalidateCache(false);
                return toReturn;
            }

            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            List<String> submittedPaths = new ArrayList<String>();
            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);

                submittedPaths.add(stringItem);
                DmDependencyTO submittedItem = null;

                submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate);
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
                            if (!dmRenameService.isItemRenamed(site, item)) {
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
                            resolveSubmittedPaths(site, goLiveItem, goLivePaths);
                        }
                        for (String fullPath : goLivePaths) {
                            String path = contentService.getRelativeSitePath(site, fullPath);
                            String lockId = site + ":" + path;
                            generalLockService.lock(lockId);

                        }
                        try {
                            goLive(site, goLiveItems, approver, mcpContext);
                        } finally {
                            for (String fullPath : goLivePaths) {
                                String path = contentService.getRelativeSitePath(site, fullPath);
                                String lockId = site + ":" + path;
                                generalLockService.unlock(lockId);
                            }
                        }
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            String fullPath = contentService.expandRelativeSitePath(site, renameItem.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            String fullPath = contentService.expandRelativeSitePath(site, renamedChild.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().after(new Date())) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }

                        dmRenameService.goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        String fullPath = contentService.expandRelativeSitePath(site, deletedItem.getUri());
                        //deletedItem.setScheduledDate(getScheduledDate(site, format, scheduledDate));
                        deletePaths.add(fullPath);
                        ContentItemTO contentItem = contentService.getContentItem(site, deletedItem.getUri());
                        if (contentItem != null) {
                            //nodeRefs.add(nodeRef.getId());
                        }
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
			if(notificationService2.isEnable()) {
				result.setMessage(notificationService2.getNotificationMessage(site,NotificationMessageType
					.CompleteMessages,responseMessageKey,Locale.ENGLISH));
			}else {
				result.setMessage(notificationService.getCompleteMessage(site, responseMessageKey));
			}

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
            if(operation != Operation.DELETE && !dmPublishService.hasChannelsConfigure(site, mcpContext)){
                ResultTO toReturn = new ResultTO();
                List<PublishingChannelConfigTO> channelsList = siteService.getPublishingChannelGroupConfigs(site).get(mcpContext.getPublishingChannelGroup()).getChannels();
                String channels = StringUtils.join(channelsList, " ");
                toReturn.setMessage(" Specified target '"+channels+"' was not found. Please check if an endpoint or channel with name '"+channels+"' exists in site configuration");
                toReturn.setSuccess(false);
                toReturn.setInvalidateCache(false);
                return toReturn;
            }

            int length = items.size();
            if (length == 0) {
                throw new ServiceException("No items provided to go live.");
            }

            List<String> submittedPaths = new ArrayList<String>();
            String responseMessageKey = null;
            SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
            List<DmDependencyTO> submittedItems = new ArrayList<>();
            for (int index = 0; index < length; index++) {
                String stringItem = items.optString(index);

                submittedPaths.add(stringItem);
                DmDependencyTO submittedItem = null;

                submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate);
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
                            if (!dmRenameService.isItemRenamed(site, item)) {
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
                        for (DmDependencyTO goLiveItem : goLiveItems) {
                            resolveSubmittedPaths(site, goLiveItem, goLivePaths);
                        }
                        for (String fullPath : goLivePaths) {
                            String path = contentService.getRelativeSitePath(site, fullPath);
                            String lockId = site + ":" + path;
                            generalLockService.lock(lockId);

                        }
                        try {
                            goLive(site, goLiveItems, approver, mcpContext);
                        } finally {
                            for (String fullPath : goLivePaths) {
                                String path = contentService.getRelativeSitePath(site, fullPath);
                                String lockId = site + ":" + path;
                                generalLockService.unlock(lockId);
                            }
                        }
                    }

                    if (!renameItems.isEmpty()) {
                        List<String> renamePaths = new ArrayList<>();
                        List<DmDependencyTO> renamedChildren = new ArrayList<>();
                        for (DmDependencyTO renameItem : renameItems) {
                            renamedChildren.addAll(getChildrenForRenamedItem(site, renameItem));
                            String fullPath = contentService.expandRelativeSitePath(site, renameItem.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renameItem.getUri(), true);
                        }
                        for (DmDependencyTO renamedChild : renamedChildren) {
                            String fullPath = contentService.expandRelativeSitePath(site, renamedChild.getUri());
                            renamePaths.add(fullPath);
                            objectStateService.setSystemProcessing(site, renamedChild.getUri(), true);
                        }
                        renameItems.addAll(renamedChildren);
                        //Set proper information of all renameItems before send them to GoLive
                        for(int i=0;i<renameItems.size();i++){
                            DmDependencyTO renamedItem = renameItems.get(i);
                            if (renamedItem.getScheduledDate() != null && renamedItem.getScheduledDate().after(new Date())) {
                                renamedItem.setNow(false);
                            } else {
                                renamedItem.setNow(true);
                            }
                            renameItems.set(i, renamedItem);
                        }

                        dmRenameService.goLive(site, renameItems, approver, mcpContext);
                    }

                    break;
                case DELETE:
                    responseMessageKey = NotificationService.COMPLETE_DELETE;
                    List<String> deletePaths = new ArrayList<>();
                    List<String> nodeRefs = new ArrayList<String>();
                    for (DmDependencyTO deletedItem : submittedItems) {
                        String fullPath = contentService.expandRelativeSitePath(site, deletedItem.getUri());
                        //deletedItem.setScheduledDate(getScheduledDate(site, format, scheduledDate));
                        deletePaths.add(fullPath);
                        ContentItemTO contentItem = contentService.getContentItem(site, deletedItem.getUri());
                        if (contentItem != null) {
                            //nodeRefs.add(nodeRef.getId());
                        }
                    }
                    doDelete(site, submittedItems, approver);
            }
            result.setSuccess(true);
            result.setStatus(200);
            if(notificationService2.isEnable()) {
                result.setMessage(notificationService2.getNotificationMessage(site,NotificationMessageType
                        .CompleteMessages,responseMessageKey,Locale.ENGLISH));
            }else {
                result.setMessage(notificationService.getCompleteMessage(site, responseMessageKey));
            }

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
     * @return
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
        Date scheduledDate = null;
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

    protected DmDependencyTO getSubmittedItem(String site, String itemPath, SimpleDateFormat format, String globalSchDate) throws JSONException {
        DmDependencyTO submittedItem = new DmDependencyTO();
        submittedItem.setUri(itemPath);
        // TODO: check scheduled date to make sure it is not null when isNow =
        // true and also it is not past
        Date scheduledDate = null;
        if (globalSchDate != null && !StringUtils.isEmpty(globalSchDate)) {
            scheduledDate = getScheduledDate(site, format, globalSchDate);
        } else {
            if (submittedItem.getScheduledDate() != null) {
                scheduledDate = getScheduledDate(site, format, format.format(submittedItem.getScheduledDate()));
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
     * @return
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
        final Date now = new Date();
        List<String> itemsToDelete = new ArrayList<>();
        List<DmDependencyTO> deleteItems = new ArrayList<>();
        List<DmDependencyTO> scheItems = new ArrayList<>();
        for (DmDependencyTO submittedItem : submittedItems) {
            String uri = submittedItem.getUri();
            Date schDate = submittedItem.getScheduledDate();
            boolean isItemForSchedule = false;
            if (schDate == null || schDate.before(now)) {
                // Sending Notification
                if (StringUtils.isNotEmpty(approver)) {
                    // immediate delete
                    if (submittedItem.isSendEmail()) {
                        sendDeleteApprovalNotification(site, submittedItem, approver);//TODO move it after delete actually happens
                    }
                }
                if (submittedItem.getUri().endsWith(DmConstants.INDEX_FILE)) {
                    submittedItem.setUri(submittedItem.getUri().replace("/" + DmConstants.INDEX_FILE, ""));
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
            ContentItemTO itemToDelete = contentService.getContentItem(site,uri);
            /* TODO: if item is renamed
            if(persistenceManagerService.hasAspect(itemToDelete, CStudioContentModel.ASPECT_RENAMED)){
                String oldPath = (String) persistenceManagerService.getProperty(itemToDelete, CStudioContentModel.PROP_RENAMED_OLD_URL);
                if(oldPath!=null){
                    itemsToDelete.add(oldPath);//Make sure old path is going to be deleted
                }
            }*/
        }
        //List<String> deletedItems = deleteInTransaction(site, itemsToDelete);
        GoLiveContext context = new GoLiveContext(approver, site);
        //final String pathPrefix = getSiteRoot(site, null);
        final String pathPrefix = "/wem-projects/" + site + "/" + site + "/work-area";
        Map<Date, List<DmDependencyTO>> groupedPackages = groupByDate(deleteItems, now);
        if (groupedPackages.isEmpty()) {
            groupedPackages.put(now, Collections.<DmDependencyTO>emptyList());
        }
        for (Date scheduledDate : groupedPackages.keySet()) {
            List<DmDependencyTO> deletePackage = groupedPackages.get(scheduledDate);
            SubmitPackage submitpackage = new SubmitPackage(pathPrefix);
            Set<String> rescheduledUris = new HashSet<String>();
            if (deletePackage != null) {
                Date launchDate = scheduledDate.equals(now) ? null : scheduledDate;
                for (DmDependencyTO dmDependencyTO : deletePackage) {
                    if (launchDate != null) {
                        handleReferencesDelete(site, submitpackage, dmDependencyTO, true, null, "", rescheduledUris);
                    } else {
                        applyDeleteDependencyRule(site, submitpackage, dmDependencyTO);
                    }
                }
                String label = submitpackage.getLabel();
                //String workFlowName = _submitDirectWorkflowName;

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
                    //workFlowName = _reviewWorkflowName;
                    /*
                    for (String submitPackPath : submitpackage.getUris()) {
                        String fullpath = dmContentService.getContentFullPath(site, submitPackPath);
                        _cacheManager.invalidateAndRemoveFromQueue(fullpath, site);
                    }*/
                } else {
                    //add dependencies to submitPackage
                    for (String liveDependency : liveDependencyItems) {
                        DmPathTO pathTO = new DmPathTO(liveDependency);
                        submitpackage.addToPackage(pathTO.getRelativePath());
                    }
                    submitPackPaths = submitpackage.getPaths();

                    deleteOperation = new PreSubmitDeleteOperation(this, new HashSet<String>(allItems), context, rescheduledUris);
                    removeChildFromSubmitPackForDelete(submitPackPaths);
                    for (String deleteCandidate : allItems) {
                        //_cacheManager.invalidateAndRemoveFromQueue(deleteCandidate, site);
                    }
                }
                Map<String, String> submittedBy = new HashMap<>();

                /* TODO: add to submitted by mapping
                for (String longPath : submitPackPaths) {
                    String uri = longPath.substring(pathPrefix.length());
                    //DmUtils.addToSubmittedByMapping(persistenceManagerService, dmContentService, searchService, site, uri, submittedBy, approver);
                }*/

                workflowProcessor.addToWorkflow(site, new ArrayList<String>(), launchDate, label, deleteOperation, approver, null);
            }
        }
        long end = System.currentTimeMillis();
        logger.debug("Submitted deleted items to queue time = " + (end - start));
    }

    @Override
    public Map<Date, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, Date now) {
        Map<Date, List<DmDependencyTO>> groupedPackages = new HashMap<>();
        for (DmDependencyTO submittedItem : submittedItems) {

            Date scheduledDate = (submittedItem.isNow()) ? null : submittedItem.getScheduledDate();
            if (scheduledDate == null || scheduledDate.before(now)) {
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

    protected void handleReferences(String site, SubmitPackage submitpackage, DmDependencyTO dmDependencyTO, boolean isNotScheduled, SubmitPackage dependencyPackage, String approver, Set<String> rescheduledUris) throws ServiceException {//,boolean isReferencePage) {
        String path = contentService.expandRelativeSitePath(site, dmDependencyTO.getUri());
		ObjectMetadata properties = objectMetadataManager.getProperties(site, dmDependencyTO.getUri());
		Date scheduledDate = null;
        if (properties != null) {
            scheduledDate = properties.getLaunchDate();
        }
		ObjectState state = objectStateService.getObjectState(site, dmDependencyTO.getUri());
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

        Set<String> dependencySet = dependencyService.getPublishingDepenencies(site, dmDependencyTO.getUri());
        for (String dependency : dependencySet) {
            submitpackage.addToPackage(dependency);
            if (!isNotScheduled) {
                dependencyPackage.addToPackage(dependency);
            }
        }

        if (isRescheduleRequest(dmDependencyTO, site)) {
            rescheduledUris.add(dmDependencyTO.getUri());
        }
    }

    protected void handleReferencesDelete(String site, SubmitPackage submitpackage, DmDependencyTO dmDependencyTO, boolean isNotScheduled, SubmitPackage dependencyPackage, String approver, Set<String> rescheduledUris) throws ServiceException {//,boolean isReferencePage) {
        String path = contentService.expandRelativeSitePath(site, dmDependencyTO.getUri());
        ObjectMetadata properties = objectMetadataManager.getProperties(site, dmDependencyTO.getUri());
        Date scheduledDate = null;
        if (properties != null) {
            scheduledDate = properties.getLaunchDate();
        }
        ObjectState state = objectStateService.getObjectState(site, dmDependencyTO.getUri());
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

        Set<String> dependencySet = dependencyService.getDeleteDepenencies(site, dmDependencyTO.getUri());
        for (String dependency : dependencySet) {
            submitpackage.addToPackage(dependency);
            if (!isNotScheduled) {
                dependencyPackage.addToPackage(dependency);
            }
        }

        if (isRescheduleRequest(dmDependencyTO, site)) {
            rescheduledUris.add(dmDependencyTO.getUri());
        }
    }

    protected boolean areEqual(Date oldDate, Date newDate) {
        if (oldDate == null && newDate == null) {
            return true;
        }
        if (oldDate != null && newDate != null) {
            return oldDate.equals(newDate);
        }
        return false;
    }

    protected void applyDeleteDependencyRule(String site, SubmitPackage pack, DmDependencyTO dmDependencyTO) throws ServiceException {
        pack.addToPackage(dmDependencyTO);
        Set<String> deps = dependencyService.getDeleteDepenencies(site, dmDependencyTO.getUri());
        for (String dependency : deps) {
            pack.addToPackage(dependency);
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
    protected Date getScheduledDate(String site, SimpleDateFormat format, String dateStr) {
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
        String split[] = path.split("/");
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
            Date date = submittedItem.getScheduledDate();
            if (children != null) {
                Iterator<DmDependencyTO> childItr = children.iterator();
                while (childItr.hasNext()) {
                    DmDependencyTO child = childItr.next();
                    Date pageDate = child.getScheduledDate();
                    if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                        if (!submittedItem.isNow()) {
                            child.setNow(false);
                            if (date != null && (pageDate != null && pageDate.before(date))) {
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

            Set<String> dependenciesPaths = dependencyService.getPublishingDepenencies(site, submittedItem.getUri());
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
            Date date = submittedItem.getScheduledDate();
            if (children != null) {
                Iterator<DmDependencyTO> childItr = children.iterator();
                while (childItr.hasNext()) {
                    DmDependencyTO child = childItr.next();
                    Date pageDate = child.getScheduledDate();
                    if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                        if (!submittedItem.isNow()) {
                            child.setNow(false);
                            if (date != null && (pageDate != null && pageDate.before(date))) {
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
        List<String> submittedItemsPaths = new ArrayList<String>();
        for (DmDependencyTO submittedItem : submittedItems) {
            submittedItemsPaths.add(submittedItem.getUri());
        }
        dependenciesPaths.addAll(dependencyService.getPublishingDepenencies(site, submittedItemsPaths));
        for (String depPath : dependenciesPaths) {
            dependencies.add(getSubmittedItem(site, depPath, format, globalScheduledDate));
        }
        return dependencies;
    }

    protected List<DmDependencyTO> addDependenciesForSubmitForApproval(String site, List<DmDependencyTO> submittedItems, SimpleDateFormat format, String globalScheduledDate) throws ServiceException {
        List<DmDependencyTO> dependencies = new ArrayList<DmDependencyTO>();
        Set<String> dependenciesPaths = new HashSet<String>();
        List<String> submittedItemPaths = new ArrayList<String>();
        for (DmDependencyTO submittedItem : submittedItems) {
            submittedItemPaths.add(submittedItem.getUri());
        }
        dependenciesPaths.addAll(dependencyService.getPublishingDepenencies(site, submittedItemPaths));
        for (String depPath : dependenciesPaths) {
            dependencies.add(getSubmittedItem(site, depPath, format, globalScheduledDate));
        }
        return dependencies;
    }

    protected void resolveSubmittedPaths(String site, DmDependencyTO item, List<String> submittedPaths) throws ServiceException {
        String fullPath = contentService.expandRelativeSitePath(site, item.getUri());
        if (!submittedPaths.contains(fullPath)) {
            submittedPaths.add(fullPath);
        }
        List<DmDependencyTO> children = item.getChildren();
        if (children != null) {
            for (DmDependencyTO child : children) {
                String childPath = contentService.expandRelativeSitePath(site, child.getUri());
                if (objectStateService.isUpdatedOrNew(site, child.getUri())) {
                    if (!submittedPaths.contains(childPath)) {
                        submittedPaths.add(childPath);
                    }
                    resolveSubmittedPaths(site, child, submittedPaths);
                }
            }
        }

        Set<String> dependencyPaths = dependencyService.getPublishingDepenencies(site, item.getUri());
        submittedPaths.addAll(dependencyPaths);

    }

    protected List<DmDependencyTO> getChildrenForRenamedItem(String site, DmDependencyTO renameItem) {
        List<DmDependencyTO> toRet = new ArrayList<>();
        List<DmDependencyTO> children = renameItem.getChildren();
        Date date = renameItem.getScheduledDate();
        if (children != null) {
            Iterator<DmDependencyTO> childItr = children.iterator();
            while (childItr.hasNext()) {
                DmDependencyTO child = childItr.next();
                Date pageDate = child.getScheduledDate();
                if ((date == null && pageDate != null) || (date != null && !date.equals(pageDate))) {
                    if (!renameItem.isNow()) {
                        child.setNow(false);
                        if (date != null && (pageDate != null && pageDate.before(date))) {
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
    public void preScheduleDelete(Set<String> urisToDelete, final Date scheduleDate, final GoLiveContext context, Set rescheduledUris)
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
     * @param request
     * @return call result
     * @throws ServiceException
     */
    @Override
    @ValidateParams
    public ResultTO goLive(@ValidateStringParam(name = "site") final String site, @ValidateStringParam(name = "request") final String request) throws ServiceException {
        String lockKey = DmConstants.PUBLISHING_LOCK_KEY.replace("{SITE}", site.toUpperCase());
        generalLockService.lock(lockKey);
        try {
            try {
                if (enablePublishingWithoutDependencies) {
                    return approveWithoutDependencies(site, request, Operation.GO_LIVE);
                } else {
                    return approve_new(site, request, Operation.GO_LIVE);
                }
            } catch (RuntimeException e) {
                logger.error("error making go live", e);
                throw e;
            }
        } catch (RuntimeException e) {
            throw e;
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @return call result
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
     * @return call result
     * @throws ServiceException
     */
    protected void goLive(final String site, final List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext)
            throws ServiceException {
        long start = System.currentTimeMillis();
        // get web project information
        //final String assignee = getAssignee(site, sub);
        final String pathPrefix = "/wem-projects/" + site + "/" + site + "/work-area";
        final Date now = new Date();
        if (submittedItems != null) {
            // group submitted items into packages by their scheduled date
            Map<Date, List<DmDependencyTO>> groupedPackages = groupByDate(submittedItems, now);

            for (Date scheduledDate : groupedPackages.keySet()) {
                List<DmDependencyTO> goLivePackage = groupedPackages.get(scheduledDate);
                if (goLivePackage != null) {
                    Date launchDate = scheduledDate.equals(now) ? null : scheduledDate;

                    final boolean isNotScheduled = (launchDate == null);
                    // for submit direct, package them together and submit them
                    // together as direct submit
                    final SubmitPackage submitpackage = new SubmitPackage(pathPrefix);
                    /*
                        dependencyPackage holds references of page.
                     */
                    final Set<String> rescheduledUris = new HashSet<String>();
                    final SubmitPackage dependencyPackage = new SubmitPackage("");
                    for (final DmDependencyTO dmDependencyTO : goLivePackage) {
                        goLivepackage(site, submitpackage, dmDependencyTO, isNotScheduled, dependencyPackage, approver, rescheduledUris);
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
                        for (String longPath : stringList) {
                            String uri = longPath.substring(pathPrefix.length());
                            dmPublishService.cancelScheduledItem(site, uri);
                        }
                        workflowProcessor.addToWorkflow(site, stringList, launchDate, label, operation, approver, mcpContext);

                    }
                    Set<DmDependencyTO> dependencyTOSet = submitpackage.getItems();
                    for (DmDependencyTO dmDependencyTO : dependencyTOSet) {
                        dmWorkflowListener.postGolive(site, dmDependencyTO);
                    }
                    dependencyTOSet = dependencyPackage.getItems();
                    for (DmDependencyTO dmDependencyTO : dependencyTOSet) {
                        dmWorkflowListener.postGolive(site, dmDependencyTO);
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        logger.debug("Total go live time = " + (end - start));
    }

    protected void goLivepackage(String site, SubmitPackage submitpackage, DmDependencyTO dmDependencyTO, boolean isNotScheduled, SubmitPackage dependencyPackage, String approver, Set<String> rescheduledUris) throws ServiceException {
        handleReferences(site, submitpackage, dmDependencyTO, isNotScheduled, dependencyPackage, approver, rescheduledUris);
        List<DmDependencyTO> children = dmDependencyTO.getChildren();
        if (children != null) {
            for (DmDependencyTO child : children) {
                handleReferences(site, submitpackage, child, isNotScheduled, dependencyPackage, approver, rescheduledUris);
                goLivepackage(site, submitpackage, child, isNotScheduled, dependencyPackage, approver, rescheduledUris);
            }
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
     * approve multiple packages by canceling pending workflows and submitting
     * them to one workflow instance. This should only be used for submit direct
     * case. (might change in future)
     *
     * @param site
     * @param user
     * @param scheduledDate
     * @param goLivePackage
     * @throws ServiceException
     */
    protected void approveMultiplePackages(final String site, final String user, final Date scheduledDate,
                                           final List<DmDependencyTO> goLivePackage) throws ServiceException {

        // attach submitted aspect to all items within this package. Prepare workfow
        final StringBuffer buffer = new StringBuffer();
        List<String> packagePaths = new ArrayList<>();
        for (DmDependencyTO item : goLivePackage) {
            buffer.append(item.getUri() + ", ");
            List<String> paths = prepareWorkflowSubmission(site, user, item, scheduledDate, item.isSendEmail());
            packagePaths.addAll(paths);
        }

        // submit to workflow
        String label = buffer.toString();
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }
        submitToWorkflow(site, scheduledDate, label, packagePaths);
    }

    /**
     * approve single item by approving the existing workflow or submitting
     * direct
     *
     * @param site
     * @param user
     * @param scheduledDate
     * @throws ServiceException
     */
    protected void approveSinglePackage(final String site, final String user, final Date scheduledDate,
                                        final DmDependencyTO submittedItem) throws ServiceException {
        List<String> tasks = submittedItem.getWorkflowTasks();
        if (tasks == null || tasks.size() == 0) {
            // if no workflow started, submit items in the package
            // attach submitted aspect to items being submitted

            List<String> paths = prepareWorkflowSubmission(site, user, submittedItem, scheduledDate, submittedItem.isSendEmail());
            String label = submittedItem.getUri();
            if (label.length() > 255) {
                label = label.substring(0, 252) + "..";
            }

            submitToWorkflow(site,scheduledDate, label, paths);
        }
    }

    protected List<String> prepareWorkflowSubmission(String site, String user, DmDependencyTO submittedItem,
                                                     Date launchDate, boolean sendEmail) throws ServiceException {
        return prepareWorkflowSubmission(site, user, submittedItem, launchDate, sendEmail, false);
    }

    /**
     * prepare the content for workflow submission by attaching submitted aspect
     *
     * @param site
     * @param user
     * @param submittedItem
     * @param launchDate
     * @param sendEmail
     * @return
     * @throws ServiceException
     */
    protected List<String> prepareWorkflowSubmission(String site, String user, DmDependencyTO submittedItem,
                                                     Date launchDate, boolean sendEmail, boolean submittedForDeletion) throws ServiceException {
        List<String> paths = new ArrayList<>();
        String fullPath = contentService.expandRelativeSitePath(site, submittedItem.getUri());
        ContentItemTO contentItem = contentService.getContentItem(site, submittedItem.getUri());

        if (contentItem != null) {
            if (!submittedItem.isDeleted()) {
                /* TODO: set properties
                persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTED_BY, user);
                persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SEND_EMAIL, sendEmail);
                persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_SUBMITTEDFORDELETION, submittedForDeletion);
                */
                List<String> includedItems = new ArrayList<>();
                addSubmittedAspect(site, user, null, submittedItem, launchDate, includedItems);

                for (String includedItem : includedItems) {
                    paths.add(includedItem);
                }
            } else {
                // do nothing if deleted
                String topLevelItem = contentService.getRelativeSitePath(site, fullPath);
                paths.add(topLevelItem);
            }
        } else {
            logger.error(submittedItem.getUri() + " does not exist.");
        }

        return paths;
    }

    /**
     * add the submitted aspect to each item submitted and set properties
     *
     * @param site
     * @param user
     * @param parentUri
     * @param submittedItem
     * @param scheduledDate
     * @param includedItems
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    protected void addSubmittedAspect(String site, String user, String parentUri, DmDependencyTO submittedItem,
                                      Date scheduledDate, List<String> includedItems) throws ServiceException {
        String fullPath = contentService.expandRelativeSitePath(site, submittedItem.getUri());


            ContentItemTO node = contentService.getContentItem(site, submittedItem.getUri());
            if (node != null) {
                // add submitted aspect
                /* TODO: Submitted aspectreplacement
                if (!persistenceManagerService.hasAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED)) {
                    // add submitted aspect
                    persistenceManagerService.addAspect(node, CStudioContentModel.ASPECT_WORKFLOW_SUBMITTED, null);

                }*/

                // set direct child items
                /* TODO: set properties
                List<String> childDependencies = getDependencies(site, user, submittedItem.getUri(), submittedItem,
                        scheduledDate, includedItems);

                persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_CHILDREN, (Serializable) childDependencies);
                // set a scheduled date

                persistenceManagerService.setProperty(node, WCMWorkflowModel.PROP_LAUNCH_DATE, scheduledDate);

                Map<QName, Serializable> properties = persistenceManagerService.getProperties(node);
                // add parent URI if not null
                if (!StringUtils.isEmpty(parentUri)) {
                    Serializable parentValue = properties.get(CStudioContentModel.PROP_WEB_WF_PARENT_URI);
                    List<String> parents = (parentValue == null) ? new ArrayList<String>(1) : (List<String>) parentValue;
                    if (!parents.contains(parentUri)) {
                        parents.add(parentUri);
                    }
                    persistenceManagerService.setProperty(node, CStudioContentModel.PROP_WEB_WF_PARENT_URI, (Serializable) parents);
                }
                */
                includedItems.add(submittedItem.getUri());
            } else {
                includedItems.add(submittedItem.getUri());
            }

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
    protected void submitToWorkflow(final String site, final Date launchDate, final String label, final List<String> paths) throws ServiceException {
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
    protected void submitToWorkflow(final String site, final Date launchDate, final String label, final List<String> paths, final MultiChannelPublishingContext mcpContext) throws ServiceException {
        _submit(site, launchDate, label, paths, mcpContext);
    }

    protected void _submit(String site, Date launchDate, String label, List<String> paths, MultiChannelPublishingContext mcpContext) {
        if (label.length() > 255) {
            label = label.substring(0, 252) + "..";
        }

        // submit to workflow

        logger.debug("[WORKFLOW] w1,publish for " + label + ",start," + System.currentTimeMillis());

        dmPublishService.publish(site, paths, launchDate, mcpContext);

    }

    @Override
    public boolean isRescheduleRequest(DmDependencyTO dependencyTO, String site) {
        if ((dependencyTO.isDeleted() || (!dependencyTO.isSubmitted() && !dependencyTO.isInProgress()))) {
            String path = contentService.expandRelativeSitePath(site, dependencyTO.getUri());
            ContentItemTO to = contentService.getContentItem(site, dependencyTO.getUri());
            Date newDate = dependencyTO.getScheduledDate();
            Date oldDate = to.getScheduledDate();
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
    public void preSchedule(Set<String> uris, final Date date, final GoLiveContext context, Set<String> rescheduledUris) {
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
    @ValidateParams
	public ResultTO submitToDelete(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, @ValidateStringParam(name = "requestBody") String requestBody) throws ServiceException {
		return submitForApproval(site, user, requestBody, true);
	}

    @Override
    @ValidateParams
    public ResultTO reject(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "user") String user, @ValidateStringParam(name = "request") String request) throws ServiceException {
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
                SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ);
                List<DmDependencyTO> submittedItems = new ArrayList<DmDependencyTO>();
                for (int index = 0; index < length; index++) {
                    String stringItem = items.optString(index);
                    //JSONObject item = items.getJSONObject(index);
                    DmDependencyTO submittedItem = null; //getSubmittedItem(site, item, format, scheduledDate);
                    submittedItem = getSubmittedItem(site, stringItem, format, scheduledDate);
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
                result.setMessage(notificationService.getCompleteMessage(site, NotificationService.COMPLETE_REJECT));
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
				final ObjectMetadata metaData = objectMetadataManager.getProperties(site, submittedItems.get(0).getUri
					());
				 String whoToBlame = "admin"; //worst case, we need someone to blame.
				if(metaData!=null && StringUtils.isNotBlank(metaData.getModifier())){
					whoToBlame=metaData.getModifier();
				}
				notificationService2.notifyContentRejection(site,whoToBlame
					, getDeploymentPaths(submittedItems),reason,approver, Locale.ENGLISH);
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
                String fullPath = contentService.expandRelativeSitePath(site, dependencyTO.getUri());
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
        String path = contentService.expandRelativeSitePath(site, dmDependencyTO.getUri());
        boolean contentExists = contentService.contentExists(site, dmDependencyTO.getUri());
        if (contentExists) {
            ObjectMetadata properties = null;
            if (!objectMetadataManager.metadataExist(site, dmDependencyTO.getUri())) {
                objectMetadataManager.insertNewObjectMetadata(site, dmDependencyTO.getUri());
            }
            properties = objectMetadataManager.getProperties(site, dmDependencyTO.getUri());

            String submittedBy = properties.getSubmittedBy();
            if (sendEmail && StringUtils.isNotEmpty(submittedBy) && StringUtils.isNotEmpty(approver)) {
                boolean isPreviewable = true;
                try {
                    ContentItemTO contentItem = contentService.getContentItem(site, dmDependencyTO.getUri());
                    isPreviewable = contentItem.isPreviewable();
                } catch (Exception e) {
                    logger.error("Item cannot be retrieved during rejection notification" + path);

                }
                notificationService.sendRejectionNotification(site, submittedBy, dmDependencyTO.getUri(), reason, approver, isPreviewable);

            }
            Map<String, Object> newProps = new HashMap<String, Object>();
            newProps.put(ObjectMetadata.PROP_SUBMITTED_BY, "");
            newProps.put(ObjectMetadata.PROP_SEND_EMAIL, 0);
            newProps.put(ObjectMetadata.PROP_SUBMITTED_FOR_DELETION, 0);
            newProps.put(ObjectMetadata.PROP_LAUNCH_DATE, null);
            objectMetadataManager.setObjectMetadata(site, dmDependencyTO.getUri(), newProps);
            ContentItemTO item = contentService.getContentItem(site, dmDependencyTO.getUri());
            objectStateService.transition(site, item, TransitionEvent.REJECT);
        }
        dmWorkflowListener.postReject(site, dmDependencyTO);
    }

    public void setWorkflowJobDAL(WorkflowJobDAL dal) { _workflowJobDAL = dal; }

//	public void setDmWorkflowService(DmWorkflowService service) { _dmSimpleWfService = service; }

	// // @Override
	public NotificationService getNotificationService() { return notificationService; }
	public void setNotificationService(NotificationService service) { notificationService = service; }

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

    public DmRenameService getDmRenameService() { return dmRenameService; }
    public void setDmRenameService(DmRenameService dmRenameService) { this.dmRenameService = dmRenameService; }

    public WorkflowProcessor getWorkflowProcessor() { return workflowProcessor; }
    public void setWorkflowProcessor(WorkflowProcessor workflowProcessor) { this.workflowProcessor = workflowProcessor; }

    public DmWorkflowListener getDmWorkflowListener() { return dmWorkflowListener; }
    public void setDmWorkflowListener(DmWorkflowListener dmWorkflowListener) { this.dmWorkflowListener = dmWorkflowListener; }

    public String getCustomContentTypeNotificationPattern() { return customContentTypeNotificationPattern; }
    public void setCustomContentTypeNotificationPattern(String customContentTypeNotificationPattern) { this.customContentTypeNotificationPattern = customContentTypeNotificationPattern; }

    public boolean isCustomContentTypeNotification() { return customContentTypeNotification; }
    public void setCustomContentTypeNotification(boolean customContentTypeNotification) { this.customContentTypeNotification = customContentTypeNotification; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public org.craftercms.studio.api.v2.service.notification.NotificationService getNotificationService2() { return notificationService2; }
    public void setNotificationService2(final org.craftercms.studio.api.v2.service.notification.NotificationService notificationService2) { this.notificationService2 = notificationService2; }

    public boolean isEnablePublishingWithoutDependencies() { return enablePublishingWithoutDependencies; }
    public void setEnablePublishingWithoutDependencies(boolean enablePublishingWithoutDependencies) { this.enablePublishingWithoutDependencies = enablePublishingWithoutDependencies; }

    private WorkflowJobDAL _workflowJobDAL;
	private NotificationService notificationService;
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
    protected DmRenameService dmRenameService;
    protected WorkflowProcessor workflowProcessor;
    protected DmWorkflowListener dmWorkflowListener;
    protected String customContentTypeNotificationPattern;
    protected boolean customContentTypeNotification;
    protected ObjectMetadataManager objectMetadataManager;
	protected org.craftercms.studio.api.v2.service.notification.NotificationService notificationService2;
    protected boolean enablePublishingWithoutDependencies = false;

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

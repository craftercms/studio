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
package org.craftercms.cstudio.alfresco.dm.service.api;

//import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.dm.to.DmDependencyTO;
import org.craftercms.cstudio.alfresco.dm.to.DmError;
import org.craftercms.cstudio.alfresco.dm.util.DmContentItemComparator;
import org.craftercms.cstudio.alfresco.dm.workflow.GoLiveContext;
import org.craftercms.cstudio.alfresco.dm.workflow.MultiChannelPublishingContext;
import org.craftercms.cstudio.alfresco.dm.workflow.RequestContext;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.dm.to.DmContentItemTO;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides Alfresco Workflow related services for DM
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public interface DmWorkflowService {

    public boolean isInFlight(String assetPath);

    /**
     *
     * @param submittedItems
     * @param scheduledDate
     * @param sendEmail
     * @param submitForDeletion
     * @param requestContext
     * @throws ServiceException
     */
    public List<DmError> submitToGoLive(List<DmDependencyTO> submittedItems, Date scheduledDate, boolean sendEmail, boolean submitForDeletion, RequestContext requestContext, String submissionComment) throws ServiceException;

    /**
     * get In progress items
     *
     * @param site
     * @param sub
     * @param comparator
     * @param inProgressOnly
     * 			get in progress items only? if false, it includes items currently pending in workflow
     * @return items in edit
     * @throws ServiceException
     */
    public List<DmContentItemTO> getInProgressItems(String site, String sub, DmContentItemComparator comparator,
                                                     boolean inProgressOnly) throws ServiceException;

    /**
     * get lists of items awaiting approval in WCM workflow mapped to top level
     * folders
     *
     * @param site
     *            site to get go live items from
     * @param sub
     *            sub key if any. if null it will be defaulted to the corporate
     *            location
     * @param comparator
     *            content item comparator for sorting
     * @return lists of items awaiting approval
     * @throws ServiceException
     */
    public List<DmContentItemTO> getGoLiveItems(final String site, final String sub,
                                                 DmContentItemComparator comparator) throws ServiceException;

    public void doDelete(String site,String sub,List<DmDependencyTO> submiitedItems, String approver) throws ServiceException;

    /**
     * prepare workflow submission by removing submitted aspects from submitted items
     *
     * @param site
     * @param sub
     * @param submittedItems
     * @param approver TODO
     * @throws ServiceException
     */
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver) throws ServiceException;

    /**
     * prepare workflow submission by removing submitted aspects from submitted items
     *
     * @param site
     * @param sub
     * @param submittedItems
     * @param approver TODO
     * @throws ServiceException
     */
    public void goLive(String site, String sub, List<DmDependencyTO> submittedItems, String approver, MultiChannelPublishingContext mcpContext) throws ServiceException;

    /**
     *
     * Group the dependencyTO items by date
     *
     * @param submittedItems
     * @param now
     * @return
     */
    public Map<Date, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, Date now);

    boolean isRescheduleRequest(DmDependencyTO dependencyTO, String site);

    void preGoLive(Set<String> uris, GoLiveContext context,Set<String> rescheduledUris);

    void preSchedule(Set<String> uris, Date date, GoLiveContext context,Set<String> rescheduledUris);

    /**
     * submit to given workflow name the paths provided
     *
     * @param site
     * @param sub
     * @param workflowName
     * @param assignee
     * @param priority
     * @param launchDate
     * @param label
     * @param description
     * @param autoDeploy
     * @param paths
     */
    public void submitToWorkflow(String site, String sub, String workflowName,
                                 String assignee, int priority, Date launchDate, String label, String description,
                                 boolean autoDeploy, List<String> paths) throws ServiceException;

    /**
     * submit to given workflow name the paths provided
     *
     * @param site
     * @param sub
     * @param workflowName
     * @param assignee
     * @param priority
     * @param launchDate
     * @param label
     * @param description
     * @param autoDeploy
     * @param paths
     */
    public void submitToWorkflow(String site, String sub, String workflowName,
                                 String assignee, int priority, Date launchDate, String label, String description,
                                 boolean autoDeploy, List<String> paths, MultiChannelPublishingContext mcpContext) throws ServiceException;

    /**
     * reject submitted items that are currently in workflow and send a notification to the user
     *
     * @param site
     * @param sub
     * @param submittedItems
     * @param reason
     * @param approver TODO
     */
    public void reject(String site, String sub, List<DmDependencyTO> submittedItems, String reason, String approver);

    /**
     *
     * Update the workflow sandbox with the given content
     *
     * @param site
     * @param fullPath
     * @param workflow
     */
    public void updateWorkflowSandbox(String site, String fullPath, String workflow);

    /**
     * cancel the workflow pending on the given item.
     *
     * @param site
     * @param sub
     * @param path
     * @param cancelWorkflow
     * 			cancel the pending workflow instance this content belongs to?
     * @throws ServiceException
     */
    public boolean removeFromWorkflow(String site, String sub, String path, boolean cancelWorkflow);

    /**
     * update workflow sandboxes if the content at the given path is in workflow
     *
     * @param site
     * @param path
     */
    public void updateWorkflowSandboxes(String site, String path);

    /**
     * get scheduled items that are approved and awaiting for deployment
     *
     * @param site
     * @param sub
     * @param comparator
     * 			categoery comparator
     * @param subComparator
     * 			comparator for items within each category
     * @return scheduled items in JSON
     */
    public List<DmContentItemTO> getScheduledItems(String site, String sub, DmContentItemComparator comparator,
                                                    DmContentItemComparator subComparator,String filterType);

	public void preScheduleDelete(Set<String> _uris, Date _date,
			GoLiveContext _context, Set _rescheduledUris) throws ServiceException;

	List<String> preDelete(Set<String> urisToDelete, GoLiveContext context,Set<String> rescheduledUris) throws ServiceException;
	
	boolean cleanWorkflow(final String sandBox, final String url, final String site, final Set<DmDependencyTO> dependents);

    /**
     * prepare submission for the given workflow package
     *
     * @param packageRef
     * @param taskId
     * @param desc
     * @param sendNotice
     * 			send approval notice?
     */
    //public void prepareSubmission(NodeRef packageRef, String taskId, String desc, boolean sendNotice);

    //public void postSubmission(NodeRef packageRef, String workflowId,String desc);

    /**
     * update all items' status associated with the given task id to the status provided
     *
     * @param packageRef
     * @param status
     * @param date
     */
    //public void updateItemStatus(NodeRef packageRef, String status, Date date);

    //public void scheduleDeleteSubmission(NodeRef packageRef,String workflowId,String description);

    public List<DmContentItemTO> getWorkflowAffectedPaths(String site, String path);
}

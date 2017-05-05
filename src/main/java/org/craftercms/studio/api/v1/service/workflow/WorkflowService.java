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
package org.craftercms.studio.api.v1.service.workflow;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.ResultTO;

public interface WorkflowService {

	/**
	 * submit content to go-live
	 * - convienience method for workflow that puts items in the approval queue for go-live
	 * - may result in items (and related dependencies) being put in several workflows (depending on rules)
	 * @param site the site
	 * @param paths the paths of the content to be submitted
	 * @param scheduledDate A suggested launch date if appropriate.  Null for no date
	 * @param sendApprovedNotice true triggers email to submitter on approval
	 * @param submitter the one submitted the job.
	 */
	void submitToGoLive(String site, List<String> paths, Date scheduledDate, boolean sendApprovedNotice, String submitter);

	ResultTO submitToGoLive(String site, String username, String request) throws ServiceException;

    void preGoLive(Set<String> uris, GoLiveContext context, Set<String> rescheduledUris);

	Map<String, Object> getGoLiveItems(String site, String sort, boolean ascending) throws ServiceException;

	Map<String, Object> getInProgressItems(String site, String sort, boolean ascending, boolean inProgressOnly) throws ServiceException;

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
	public boolean removeFromWorkflow(String site, String path, boolean cancelWorkflow);

	public List<ContentItemTO> getWorkflowAffectedPaths(String site, String path);

	/**
	 * update workflow sandboxes if the content at the given path is in workflow
	 *
	 * @param site
	 * @param path
	 */
	public void updateWorkflowSandboxes(String site, String path);

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceException
     */
    ResultTO goDelete(String site, String request, String user);

    Map<Date, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, Date now);

    void preScheduleDelete(Set<String> uris, Date _date,
                           GoLiveContext context, Set rescheduledUris) throws ServiceException;

    List<String> preDelete(Set<String> urisToDelete, GoLiveContext context,Set<String> rescheduledUris) throws ServiceException;

    boolean isRescheduleRequest(DmDependencyTO dependencyTO, String site);

    void preSchedule(Set<String> uris, Date date, GoLiveContext context,Set<String> rescheduledUris);

    ResultTO reject(final String site, final String user, final String request) throws ServiceException;
}

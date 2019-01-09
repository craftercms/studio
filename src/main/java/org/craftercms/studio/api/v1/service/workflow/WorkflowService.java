/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.api.v1.service.workflow;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.GoLiveQueue;
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
	void submitToGoLive(String site, List<String> paths, ZonedDateTime scheduledDate, boolean sendApprovedNotice, String submitter);

	ResultTO submitToGoLive(String site, String username, String request) throws ServiceLayerException;

    void preGoLive(Set<String> uris, GoLiveContext context, Set<String> rescheduledUris);

	Map<String, Object> getGoLiveItems(String site, String sort, boolean ascending) throws ServiceLayerException;

	Map<String, Object> getInProgressItems(String site, String sort, boolean ascending, boolean inProgressOnly)
		throws ServiceLayerException;

	/**
	 * cancel the workflow pending on the given item.
	 *
	 * @param site
	 * @param path
	 * @param cancelWorkflow
	 * 			cancel the pending workflow instance this content belongs to?
	 * @throws ServiceLayerException
	 */
	boolean removeFromWorkflow(String site, String path, boolean cancelWorkflow) throws ServiceLayerException;

	List<ContentItemTO> getWorkflowAffectedPaths(String site, String path) throws ServiceLayerException;

	/**
	 * update workflow sandboxes if the content at the given path is in workflow
	 *
	 * @param site
	 * @param path
	 */
	void updateWorkflowSandboxes(String site, String path);

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceLayerException
     */
    ResultTO goDelete(String site, String request, String user);

    Map<ZonedDateTime, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, ZonedDateTime now);

    void preScheduleDelete(Set<String> uris, ZonedDateTime _date,
                           GoLiveContext context, Set rescheduledUris) throws ServiceLayerException;

    List<String> preDelete(Set<String> urisToDelete, GoLiveContext context,Set<String> rescheduledUris) throws
		ServiceLayerException;

    boolean isRescheduleRequest(DmDependencyTO dependencyTO, String site);

    void preSchedule(Set<String> uris, ZonedDateTime date, GoLiveContext context,Set<String> rescheduledUris);

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site
     * @param request
     * @return call result
     * @throws ServiceLayerException
     */
    ResultTO goLive(final String site, final String request) throws ServiceLayerException;

    ResultTO reject(final String site, final String user, final String request) throws ServiceLayerException;

    void fillQueue(String site, GoLiveQueue goLiveQueue, GoLiveQueue inProcessQueue) throws ServiceLayerException;

    boolean cleanWorkflow(final String url, final String site, final Set<DmDependencyTO> dependents) throws
		ServiceLayerException;
}

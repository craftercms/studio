/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.workflow.context.GoLiveContext;
import org.craftercms.studio.api.v1.to.DmDependencyTO;
import org.craftercms.studio.api.v1.to.GoLiveQueue;
import org.craftercms.studio.api.v1.to.ResultTO;

public interface WorkflowService {

	ResultTO submitToGoLive(String site, String username, String request);

	Map<String, Object> getGoLiveItems(String site, String sort, boolean ascending) throws ServiceLayerException;

	Map<String, Object> getInProgressItems(String site, String sort, boolean ascending, boolean inProgressOnly)
			;

	/**
	 * cancel the workflow pending on the given item.
	 *
	 * @param site site identifier
	 * @param path path of the content
	 *
	 * @param cancelWorkflow
	 * 			cancel the pending workflow instance this content belongs to?
	 * @return true if success, otherwise false
	 * @throws ServiceLayerException general service error
	 */
	boolean removeFromWorkflow(String site, String path, boolean cancelWorkflow) throws ServiceLayerException, UserNotFoundException;

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site site identifier
     * @param request request body
	 * @param user  user
     * @return call result
     */
    ResultTO goDelete(String site, String request);

    Map<ZonedDateTime, List<DmDependencyTO>> groupByDate(List<DmDependencyTO> submittedItems, ZonedDateTime now);

    void preScheduleDelete(Set<String> uris, ZonedDateTime _date,
                           GoLiveContext context);

    List<String> preDelete(Set<String> urisToDelete, GoLiveContext context,Set<String> rescheduledUris) throws
			ServiceLayerException, UserNotFoundException;

    boolean isRescheduleRequest(DmDependencyTO dependencyTO, String site);

    /**
     * approve workflows and schedule them as specified in the request
     *
     * @param site site identifier
     * @param request request body
     * @return call result
     * @throws ServiceLayerException general service error
     */
    ResultTO goLive(final String site, final String request) throws ServiceLayerException;

    ResultTO reject(final String site, final String request) throws ServiceLayerException;

    void fillQueue(String site, GoLiveQueue goLiveQueue, GoLiveQueue inProcessQueue);

    boolean cleanWorkflow(final String url, final String site) throws
			ServiceLayerException, UserNotFoundException;
}

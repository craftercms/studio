/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2014 Crafter Software Corporation.
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
package org.craftercms.studio.api.v1.service.activity;

import java.util.List;

import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.ContentItemTO;

/**
 * Provides services for tracking user activities
 * 
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public interface DmActivityService extends ActivityService {

	/**
	 * get a list of activities by the given user
	 * @param site
	 * @param user
	 * @param num 
	 * 			a number of records to return
	 * @param sort
	 * @param ascending
	 * @param excludeLive
	 * 			exclude live items? 
	 * @return a list of activities 
	 * @throws ServiceException 
	 */
	public List<ContentItemTO> getActivities(String site, String user, int num, String sort, boolean ascending, boolean excludeLive, String filterType) throws ServiceException;
}

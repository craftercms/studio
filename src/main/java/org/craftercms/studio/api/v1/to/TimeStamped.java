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
package org.craftercms.studio.api.v1.to;

import java.time.ZonedDateTime;

/**
 * 
 * 
 * @author hyanghee
 *
 */
public interface TimeStamped {

	/**
	 * set the last updated date 
	 * 
	 * @param lastUpdated last updated date
	 */
	void setLastUpdated(ZonedDateTime lastUpdated);
	
	/**
	 * get the last updated date
	 * 
	 * @return last updated date
	 */
	ZonedDateTime getLastUpdated();
	
}

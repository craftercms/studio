/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;


public class SiteContentTypePathsTO implements TimeStamped, Serializable {

    private static final long serialVersionUID = 1034702203309615732L;
    /** content type path configuration **/
	protected List<ContentTypePathTO> _configs = null;
	/** last updated date of this configuration **/
	protected ZonedDateTime _lastUpdated = null;

	public ZonedDateTime getLastUpdated() {
		return _lastUpdated;
	}

	public void setLastUpdated(ZonedDateTime lastUpdated) {
		this._lastUpdated = lastUpdated;
	}

	/**
	 * @return the configs
	 */
	public List<ContentTypePathTO> getConfigs() {
		return _configs;
	}

	/**
	 * @param configs the configs to set
	 */
	public void setConfigs(List<ContentTypePathTO> configs) {
		this._configs = configs;
	}

}

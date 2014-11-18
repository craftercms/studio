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
package org.craftercms.cstudio.alfresco.objectstate;

import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.to.ObjectStateTO;

import java.util.List;

public interface ObjectStateDAOService {
	// code review rdanner/hlim 3-28
	
	// TODO CodeRev: add comments to methods
	// TODO CodeRev: methods seem to assume object ID is unique across all sites?
	// TODO CodeRev: Where are my exceptions?

	// TODO CodeRev: Implementation detail? Does this belong in the interface
	public void initIndexes();

    public void insertNewObject(String objectId, String site, String path);

    public ObjectStateTO getObjectState(String objectId);

    public void setObjectState(String objectId, ObjectStateService.State state);

    public void setSystemProcessing(String objectId, boolean isSystemProcessing);

	// TODO CodeRev: shouldn't this method have the same name as setSystemProcessing but with overloaded params?
    public void setSystemProcessingBulk(List<String> objectIds, boolean isSystemProcessing);

    public void deleteObjectStatesForSite(String site);

	// TODO CodeRev: What's going on with scheduledStates param name?
    public List<ObjectStateTO> getObjectStateByStates(String site, List<ObjectStateService.State> scheduledStates);

    public void updateObjectPath(String objectId, String newPath);

	// TODO CodeRev: does this belong in this interface?
    public boolean isFolderLive(String site, String path);

    public void deleteObjectState(String objectId);

    public void deleteObjectStatesForPath(String site, String path);

    public void deleteObjectStatesForPaths(String site, List<String> paths);

    public List<ObjectStateTO> getObjectStates(List<String> objectIds);

	// TODO CodeRev: similar to comment above, consider overloading
    public void setObjectStateBulk(List<String> objectIds, ObjectStateService.State state);
}

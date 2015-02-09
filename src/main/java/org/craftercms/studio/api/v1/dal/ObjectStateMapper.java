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

package org.craftercms.studio.api.v1.dal;

import java.util.List;
import java.util.Map;

/**
 * @author Dejan Brkic
 */
public interface ObjectStateMapper {

    List<ObjectState> getObjectStateByStates(Map params);

    void deleteObjectState(String objectId);

    ObjectState getObjectStateBySiteAndPath(Map params);

    void setSystemProcessingBySiteAndPath(Map params);

    void insertEntry(ObjectState objectState);

    void setObjectState(ObjectState objectState);

    List<ObjectState> getObjectStateForSiteAndPaths(Map params);

    void setObjectStateForSiteAndPaths(Map params);

    void updateObjectPath(Map params);
}

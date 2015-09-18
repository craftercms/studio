/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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

package org.craftercms.studio.api.v1.ebus;

public final class EBusConstants {
  
    /**
     * Repository reactor bean name.
     */
    public static final String REPOSITORY_REACTOR = "@repositoryReactor";

    /**
     * Repository create event name.
     */
    public static final String REPOSITORY_CREATE_EVENT = "repository.create";

    /**
     * Repository update event name.
     */
    public static final String REPOSITORY_UPDATE_EVENT = "repository.update";

    /**
     * Repository delete event name.
     */
    public static final String REPOSITORY_DELETE_EVENT = "repository.delete";

    /**
     * Repository preview sync event name.
     */
    public static final String REPOSITORY_PREVIEW_SYNC_EVENT = "repository.previewSync";

    /**
     * Repository delete event name.
     */
    public static final String REPOSITORY_MOVE_EVENT = "repository.move";

    /**
     * Cluster clear cache event name.
     */
    public static final String CLUSTER_CLEAR_CACHE_EVENT = "cluster.clearCache";

    public static final String DISTRIBUTED_REACTOR = "@distributedReactor";

    private EBusConstants() {}
}

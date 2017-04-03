/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */
package org.craftercms.studio.api.v1.service.event;

import org.craftercms.studio.api.v1.ebus.DeploymentEventContext;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;

import java.util.List;

public interface EventService {
    String CLUSTER_NAME = "StudioCluster";

    /** Preview Sync Event listener method */
    String PREVIEW_SYNC_LISTENER_METHOD = "onPreviewSyncEvent";
    String PREVIEW_CREATE_TARGET_LISTENER_METHOD = "onCreateTargetEvent";
    String PREVIEW_DELETE_TARGET_LISTENER_METHOD = "onDeleteTargetEvent";

    /** Publish to environment Event listener method  */
    String PUBLISH_TO_ENVIRONMENT_LISTENER_METHOD = "onEnvironmentDeploymentEvent";

    void firePreviewSyncEvent(String site);

    void onPreviewSyncEvent(PreviewEventContext context);

    boolean firePreviewCreateTargetEvent(String site);

    void onDeleteTargetEvent(PreviewEventContext context);

    boolean firePreviewDeleteTargetEvent(String site);

    void onCreateTargetEvent(PreviewEventContext context);

    void firePublishToEnvironmentEvent(String site, List<DeploymentItem> items, String environment, String author, String comment);

    void onEnvironmentDeploymentEvent(DeploymentEventContext context);
}

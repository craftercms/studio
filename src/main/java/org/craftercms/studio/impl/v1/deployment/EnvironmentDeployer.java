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

package org.craftercms.studio.impl.v1.deployment;

import org.craftercms.studio.api.v1.ebus.DeploymentEventContext;
import org.craftercms.studio.api.v1.ebus.DeploymentItem;
import org.craftercms.studio.api.v1.repository.ContentRepository;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentDeployer {

    public void onEnvironmentDeploymentEvent(DeploymentEventContext context) {
        List<DeploymentItem> items = context.getItems();
        List<String> commitIds = new ArrayList<String>(items.size());
        for (DeploymentItem item : items) {
            commitIds.add(item.getCommitId());
        }
        contentRepository.publish(context.getSite(), commitIds, context.getEnvironment(), context.getAuthor(), context.getComment());
    }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    ContentRepository contentRepository;
}
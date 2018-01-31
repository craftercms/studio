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
 */
package org.craftercms.studio.api.v1.service.deployment;


import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.service.workflow.context.MultiChannelPublishingContext;

import java.util.Date;
import java.util.List;

public interface DmPublishService {

    void publish(String site, List<String> paths, Date launchDate, MultiChannelPublishingContext mcpContext);

    public void unpublish(String site, List<String> paths, String approver);

    public void unpublish(String site, List<String> paths, String approver, Date scheduleDate);

    public void cancelScheduledItem(String site, String path);

    //public List<PublishingChannelTO> getAvailablePublishingChannelGroups(String site, String path);

    boolean hasChannelsConfigure(String site, MultiChannelPublishingContext mcpContext);

    void bulkGoLive(String site, String environment, String path) throws ServiceException;

    void bulkDelete(String site, String path);

}

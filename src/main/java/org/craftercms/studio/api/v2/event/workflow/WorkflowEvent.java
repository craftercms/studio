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
package org.craftercms.studio.api.v2.event.workflow;

import org.craftercms.studio.api.v2.event.BroadcastEvent;
import org.craftercms.studio.api.v2.event.SiteAwareEvent;
import org.springframework.security.core.Authentication;

/**
 * Event triggered when items go through the different workflow states
 *
 * @implNote For now this is triggered for any state change
 *
 * @author joseross
 * @since 4.0.0
 */
public class WorkflowEvent extends SiteAwareEvent implements BroadcastEvent {

    public WorkflowEvent(Authentication authentication, String siteId) {
        super(authentication, siteId);
    }

    @Override
    public String getEventType() {
        return "WORKFLOW_EVENT";
    }

    @Override
    public String toString() {
        return "WorkflowEvent{" +
                "siteId='" + siteId + '\'' +
                ", timestamp=" + timestamp +
                ", user=" + user +
                '}';
    }

}

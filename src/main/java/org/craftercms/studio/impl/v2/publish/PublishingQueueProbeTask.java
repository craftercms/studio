/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.publish;

import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.event.publish.RequestPublishEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.beans.ConstructorProperties;

/**
 * Task to probe the publishing queue and trigger events for each site.
 */
public class PublishingQueueProbeTask implements Job, ApplicationEventPublisherAware {
    private final PublishDAO publishDAO;
    private ApplicationEventPublisher eventPublisher;

    @ConstructorProperties({"publishDAO"})
    public PublishingQueueProbeTask(final PublishDAO publishDAO) {
        this.publishDAO = publishDAO;
    }

    @Override
    public void execute() {
        publishDAO.getNextPublishPackages().forEach(publishPackage->
                eventPublisher.publishEvent(new RequestPublishEvent(publishPackage.getSite().getSiteId(), publishPackage.getId())));
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}

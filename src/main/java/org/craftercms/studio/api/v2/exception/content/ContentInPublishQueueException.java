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

package org.craftercms.studio.api.v2.exception.content;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

import java.util.Collection;
import java.util.Collections;

/**
 * Exception to be thrown when attempting to perform an operation on content that is in workflow.
 */
public class ContentInPublishQueueException extends ServiceLayerException {

    private final Collection<PublishPackage> publishPackages;

    /**
     * Constructor.
     *
     * @param message         The exception message.
     * @param publishPackages The publish packages the item is in workflow for.
     */
    public ContentInPublishQueueException(final String message, final Collection<PublishPackage> publishPackages) {
        super(message);
        this.publishPackages = Collections.unmodifiableCollection(publishPackages);
    }

    public Collection<PublishPackage> getPublishPackages() {
        return publishPackages;
    }
}

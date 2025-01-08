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

package org.craftercms.studio.api.v2.repository;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.GitPublishCapableRepository.GitPublishChangeSet;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Interface for content repositories that support publishing
 */
public interface PublishCapableRepository {
    /**
     * Execute initial publish for given site
     *
     * @param publishPackage the package to publish
     * @return commit id of the initial publish.
     * After this method runs, the returned value is the last
     * commit in the published repository for the target branch
     */
    InitialPublishChangeSet initialPublish(PublishPackage publishPackage, String target) throws ServiceLayerException;

    /**
     * Publishes the given items to the given target
     *
     * @param publishPackage   the publish package
     * @param publishingTarget the publishing target
     * @param publishItems     the items to publish
     * @param <T>              the type of the {@link PublishItemTO} objects
     * @return the change set listing the affected paths and new commit id
     * @throws ServiceLayerException if there is any error while publishing or publishItems is null or empty
     */
    <T extends PublishItemTO> GitPublishChangeSet<T> publish(PublishPackage publishPackage,
                                                             String publishingTarget,
                                                             Collection<T> publishItems) throws ServiceLayerException, IOException;

    record InitialPublishChangeSet(String commitId, Map<String, Integer> failedItems) {
        /**
         * Check if there are failed items
         *
         * @return true if failed items list contains items, false otherwise
         */
        public boolean hasFailedItems() {
            return !ObjectUtils.isEmpty(failedItems);
        }
    }
}

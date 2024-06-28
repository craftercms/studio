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
package org.craftercms.studio.api.v2.repository.blob;

import org.craftercms.commons.file.blob.Blob;
import org.craftercms.commons.file.blob.BlobStore;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.PublishCapableContentRepository;
import org.craftercms.studio.api.v2.repository.PublishItemTO;

import java.util.Collection;
import java.util.List;

/**
 * Extension of {@link BlobStore} that adds support for Studio content repository operations
 *
 * @author joseross
 * @since 3.1.6
 */
public interface StudioBlobStore extends BlobStore, ContentRepository,
        org.craftercms.studio.api.v2.repository.ContentRepository, PublishCapableContentRepository {

    /**
     * Return a reference to a file in the store
     * @param path the path of the file
     * @return the blob object
     */
    Blob getReference(String path);

    /**
     * Indicate if the store is read only
     * @return true if the store is read only
     */
    boolean isReadOnly();

    /**
     * Copy the blob items from the source store
     *
     * @param sourceStore the source store
     * @param environment the environment (publishing target)
     * @param items       the items to copy
     */
    void copyBlobs(StudioBlobStore sourceStore, String environment, List<String> items);

    /**
     * Publish the given items to the given publishing target
     *
     * @param publishPackage   the package to publish
     * @param publishingTarget the target to publish to
     * @param blobStoreItems   the items to publish
     * @param <T>              the actual type of the items to publish
     * @return the result of the publish operation
     */
    <T extends PublishItemTO> PublishChangeSet<T> publish(PublishPackage publishPackage,
                                                          String publishingTarget,
                                                          Collection<T> blobStoreItems) throws ServiceLayerException;

    /**
     * Store the result of a publish operation
     *
     * @param successfulItems the paths that were updated
     * @param failedItems     the paths that failed to publish, mapped to the error message
     */
    record PublishChangeSet<T extends PublishItemTO>(Collection<T> successfulItems,
                                                     Collection<T> failedItems) {
    }

}

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

package org.craftercms.studio.impl.v2.repository.blob;

import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.PublishItemTO;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobStore;

/**
 * {@link PublishItemTO} implementation that wraps another {@link PublishItemTO}
 * to be passed to a {@link StudioBlobStore} or {@link GitContentRepository} publish operation.
 * Notice that path should be the path in the underlying repository (i.e.: git repository or blob store), not the logical path in the site.
 *
 * @param <T> the type of the wrapped item
 */
public class BlobAwarePublishItemTOWrapper<T extends PublishItemTO> implements PublishItemTO {

    private final T wrappedItem;
    private final String path;

    public BlobAwarePublishItemTOWrapper(final T wrappedItem, final String path) {
        this.wrappedItem = wrappedItem;
        this.path = path;
    }

    public T getWrappedItem() {
        return wrappedItem;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public PublishItem.Action getAction() {
        return wrappedItem.getAction();
    }

    @Override
    public int getError() {
        return wrappedItem.getError();
    }

    @Override
    public void setFailed(int error) {
        wrappedItem.setFailed(error);
    }

    @Override
    public void setCompleted() {
        wrappedItem.setCompleted();
    }
}

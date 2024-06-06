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

import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.repository.PublishItemTO;

/**
 * {@link PublishItemTO} implementation that wraps a {@link PublishItem}
 * Notice that the same {@link PublishItem} might be wrapped by more than
 * one {@link PublishItemTO} instance, each with a different path and
 * action. e.g.: A move operation will be expanded to a DELETE and an ADD
 */
public class PublishItemTOImpl implements PublishItemTO {
    private final PublishItem publishItem;
    private final String path;
    private final PublishItem.Action action;

    public PublishItemTOImpl(final PublishItem publishItem, final String path, final PublishItem.Action action) {
        this.publishItem = publishItem;
        this.path = path;
        this.action = action;
    }

    public PublishItem getPublishItem() {
        return publishItem;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public PublishItem.Action getAction() {
        return action;
    }

    @Override
    public String getError() {
        return publishItem.getError();
    }

    @Override
    public void setError(String error) {
        publishItem.setError(error);
    }
}

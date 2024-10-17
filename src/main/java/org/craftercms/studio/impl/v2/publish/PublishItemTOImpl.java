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

import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.*;

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
    private final TargetStrategy targetStrategy;

    private static final TargetStrategy LIVE_STRATEGY = new LiveStrategy();
    private static final TargetStrategy STAGING_STRATEGY = new StagingStrategy();

    public PublishItemTOImpl(final PublishItem publishItem, final String path, final PublishItem.Action action, final boolean isLiveTarget) {
        this.publishItem = publishItem;
        this.path = path;
        this.action = action;
        targetStrategy = isLiveTarget ? LIVE_STRATEGY : STAGING_STRATEGY;
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
    public int getError() {
        return targetStrategy.getError(publishItem);
    }

    @Override
    public void setFailed(final int error) {
        targetStrategy.setFailed(publishItem, error);
    }

    @Override
    public void setCompleted() {
        targetStrategy.setCompleted(publishItem);
    }

    /**
     * Stategy to access the target specific fields of the {@link PublishItem}
     */
    private interface TargetStrategy {
        void setCompleted(PublishItem publishItem);

        void setFailed(PublishItem publishItem, final int error);

        int getError(PublishItem publishItem);
    }

    /**
     * Strategy for the live target
     */
    private static class LiveStrategy implements TargetStrategy {

        @Override
        public void setCompleted(PublishItem publishItem) {
            if ((publishItem.getPublishState() & LIVE_FAILED.value) == 0) {
                publishItem.setPublishState(publishItem.getPublishState() | LIVE_SUCCESS.value);
            }
        }

        @Override
        public void setFailed(PublishItem publishItem, int error) {
            publishItem.setLiveError(error);
            publishItem.setPublishState(publishItem.getPublishState() | LIVE_FAILED.value);
        }

        @Override
        public int getError(PublishItem publishItem) {
            return publishItem.getLiveError();
        }
    }

    /**
     * Strategy for the staging target
     */
    private static class StagingStrategy implements TargetStrategy {
        @Override
        public void setCompleted(PublishItem publishItem) {
            if ((publishItem.getPublishState() & STAGING_FAILED.value) == 0) {
                publishItem.setPublishState(publishItem.getPublishState() | STAGING_SUCCESS.value);
            }
        }

        @Override
        public void setFailed(PublishItem publishItem, int error) {
            publishItem.setStagingError(error);
            publishItem.setPublishState(publishItem.getPublishState() | STAGING_FAILED.value);
        }

        @Override
        public int getError(PublishItem publishItem) {
            return publishItem.getStagingError();
        }
    }
}

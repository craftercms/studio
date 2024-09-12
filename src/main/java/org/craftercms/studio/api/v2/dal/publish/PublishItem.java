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

package org.craftercms.studio.api.v2.dal.publish;

import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Represents an item to be published.
 */
public class PublishItem {
    protected long id;
    protected long packageId;
    protected String path;
    protected String livePreviousPath;
    protected String stagingPreviousPath;
    protected List<ItemTarget> itemTargets;
    protected Action action;
    protected boolean userRequested;
    protected long publishState;
    protected int liveError;
    protected int stagingError;

    protected long itemId;

    public long getItemId() {
        return itemId;
    }

    @SuppressWarnings("unused")
    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPackageId() {
        return packageId;
    }

    public void setPackageId(long packageId) {
        this.packageId = packageId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLivePreviousPath() {
        return livePreviousPath;
    }

    public void setLivePreviousPath(String livePreviousPath) {
        this.livePreviousPath = livePreviousPath;
    }

    public String getStagingPreviousPath() {
        return stagingPreviousPath;
    }

    public void setStagingPreviousPath(String stagingPreviousPath) {
        this.stagingPreviousPath = stagingPreviousPath;
    }

    public String getPreviousPath(final String target, boolean isLiveTarget) {
        if (isEmpty(itemTargets)) {
            return isLiveTarget ? livePreviousPath : stagingPreviousPath;
        }
        return itemTargets.stream()
                .filter(itemTarget -> itemTarget.getTarget().equals(target))
                .findFirst()
                .map(ItemTarget::getPreviousPath)
                .orElse(null);
    }

    public List<ItemTarget> getItemTargets() {
        return itemTargets;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isUserRequested() {
        return userRequested;
    }

    public void setUserRequested(boolean userRequested) {
        this.userRequested = userRequested;
    }

    public long getPublishState() {
        return publishState;
    }

    public void setPublishState(long publishState) {
        this.publishState = publishState;
    }

    public int getLiveError() {
        return liveError;
    }

    @SuppressWarnings("unused")
    public void setLiveError(int liveError) {
        this.liveError = liveError;
    }

    public int getStagingError() {
        return stagingError;
    }

    @SuppressWarnings("unused")
    public void setStagingError(int stagingError) {
        this.stagingError = stagingError;
    }

    /**
     * Represents the action to be performed when publishing this item
     */
    public enum Action {
        ADD,
        UPDATE,
        DELETE
    }

    /**
     * Represents the processing state of this item
     */
    public enum PublishState {
        // Item is pending to be processed
        PENDING(0),
        PROCESSING(1),
        // Live operation was completed
        LIVE_SUCCESS(2),
        // There was an error during live operation (even if completed)
        LIVE_FAILED(3),
        // Staging operation was completed
        STAGING_SUCCESS(4),
        // There was an error during staging operation (even if completed)
        STAGING_FAILED(5);

        public final long value;

        PublishState(final long exponent) {
            this.value = Math.round(Math.pow(2, exponent));
        }
    }
}

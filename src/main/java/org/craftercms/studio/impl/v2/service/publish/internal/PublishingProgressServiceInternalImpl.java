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

package org.craftercms.studio.impl.v2.service.publish.internal;

import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressObserver;
import org.craftercms.studio.api.v2.service.publish.internal.PublishingProgressServiceInternal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PublishingProgressServiceInternalImpl implements PublishingProgressServiceInternal {

    private Map<String, PublishingProgressObserver> publishingProgress = new HashMap<>();

    @Override
    public void addObserver(PublishingProgressObserver observer) {
        this.publishingProgress.put(observer.getSiteId(), observer);
    }

    @Override
    public void removeObserver(PublishingProgressObserver observer) {
        if (Objects.nonNull(observer)) {
            this.publishingProgress.remove(observer.getSiteId());
        }
    }

    @Override
    public void removeObserver(String siteId) {
        this.publishingProgress.remove(siteId);
    }

    @Override
    public void updateObserver(String siteId) {
        PublishingProgressObserver observer = this.publishingProgress.get(siteId);
        if (Objects.nonNull(observer)) {
            observer.updateProgress();
        }
    }

    @Override
    public void updateObserver(String siteId, String packageId) {
        PublishingProgressObserver observer = this.publishingProgress.get(siteId);
        if (Objects.nonNull(observer)) {
            observer.setPackageId(packageId);
            observer.updateProgress();
        }
    }

    @Override
    public void updateObserver(String siteId, int delta) {
        PublishingProgressObserver observer = this.publishingProgress.get(siteId);
        if (Objects.nonNull(observer)) {
            observer.updateProgress(delta);
        }
    }

    @Override
    public void updateObserver(String siteId, int delta, String packageId) {
        PublishingProgressObserver observer = this.publishingProgress.get(siteId);
        if (Objects.nonNull(observer)) {
            observer.setPackageId(packageId);
            observer.updateProgress(delta);
        }
    }

    @Override
    public PublishingProgressObserver getPublishingProgress(String siteId) {
        return this.publishingProgress.get(siteId);
    }
}

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

package org.craftercms.studio.api.v2.service.publish.internal;

import java.util.concurrent.atomic.AtomicInteger;

public class PublishingProgressObserver {

    private String siteId;
    private String packageId;
    private String publishingTarget;
    private int numberOfFilesBeingPublished;
    private AtomicInteger numberOfFilesCompleted;

    public PublishingProgressObserver(String siteId, String packageId, String publishingTarget,
                                      int numberOfFilesBeingPublished) {
        this.siteId = siteId;
        this.packageId = packageId;
        this.publishingTarget = publishingTarget;
        this.numberOfFilesBeingPublished = numberOfFilesBeingPublished;
        numberOfFilesCompleted = new AtomicInteger(0);
    }

    public void updateProgress() {
        numberOfFilesCompleted.incrementAndGet();
    }

    public void updateProgress(int delta) {
        numberOfFilesCompleted.addAndGet(delta);
    }

    public String getSiteId() {
        return siteId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPublishingTarget() {
        return publishingTarget;
    }

    public int getNumberOfFilesBeingPublished() {
        return numberOfFilesBeingPublished;
    }

    public int getNumberOfFilesCompleted() {
        return numberOfFilesCompleted.get();
    }
}

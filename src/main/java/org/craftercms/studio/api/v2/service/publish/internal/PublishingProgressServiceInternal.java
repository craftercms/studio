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

import java.time.ZonedDateTime;

public interface PublishingProgressServiceInternal {

    /**
     * Add publishing progress observer
     * @param observer publishing progress observer object
     */
    void addObserver(PublishingProgressObserver observer);

    /**
     * Remove publishing progress observer
     * @param observer publishing progress observer object
     */
    void removeObserver(PublishingProgressObserver observer);

    /**
     * Remove publishing progress observer
     * @param siteId site identifier
     */
    void removeObserver(String siteId);

    /**
     * Update publishing progress for given site
     * @param siteId site identifier
     */
    void updateObserver(String siteId);

    /**
     * Update publishing progress for given site
     * @param siteId site identifier
     * @param packageId package identifier
     */
    void updateObserver(String siteId, String packageId);

    /**
     * Update publishing progres for given site by delta
     * @param siteId site identifier
     * @param delta increment progress by delta
     */
    void updateObserver(String siteId, int delta);

    /**
     * Update publishing progres for given site by delta
     * @param siteId site identifier
     * @param delta increment progress by delta
     * @param packageId package identifier
     */
    void updateObserver(String siteId, int delta, String packageId);

    /**
     * Get publishing progress for given site
     * @param siteId site identifier
     * @return publishing progress
     */
    PublishingProgressObserver getPublishingProgress(String siteId);
}

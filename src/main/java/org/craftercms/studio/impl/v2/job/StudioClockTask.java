/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.job;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

public abstract class StudioClockTask implements SiteJob {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockTask.class);

    private int executeEveryNCycles;
    protected int counter;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;

    public StudioClockTask(int executeEveryNCycles,
                           StudioConfiguration studioConfiguration,
                           SiteService siteService) {
        this.executeEveryNCycles = executeEveryNCycles;
        this.counter = executeEveryNCycles;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
    }

    protected synchronized boolean checkCycleCounter() {
        return !(--counter > 0);
    }

    protected abstract void executeInternal(String site);
    protected abstract boolean lockSiteInternal(String site);
    protected abstract void unlockSiteInternal(String site);

    @Override
    public final void execute(String site) {
        if (checkCycleCounter()) {
            if (lockSiteInternal(site)) {
                try {
                    executeInternal(site);
                    counter = executeEveryNCycles;
                } finally {
                    unlockSiteInternal(site);
                }
            }
        }
    }
}

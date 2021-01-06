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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class StudioClockTask implements SiteJob {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockTask.class);

    private int executeEveryNCycles;
    protected Map<String,Integer> counters;
    protected int offset;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;

    public StudioClockTask(int executeEveryNCycles,
                           int offset,
                           StudioConfiguration studioConfiguration,
                           SiteService siteService) {
        this.executeEveryNCycles = executeEveryNCycles;
        this.counters = new HashMap<String,Integer>();
        this.offset = offset;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
    }

    protected synchronized boolean checkCycleCounter(String site) {
        if (!counters.containsKey(site)) {
            setCycleCounter(site, executeEveryNCycles);
        }

        int counter = counters.get(site);
        setCycleCounter(site, --counter);

        return (counter <= 0); // Trigger if <= 0
    }

    protected synchronized void setCycleCounter(String site, int counter) {
        counters.put(site, counter);
    }

    protected abstract void executeInternal(String site);

    @Override
    public final void execute(String site) {
        if (checkCycleCounter(site)) {
            try {
                long sleepTime = (long) (Math.random() * offset);
                logger.debug("Sleeping for offset " + sleepTime + " milliseconds");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.debug("Woke up from random offset");
            }
            executeInternal(site);
            setCycleCounter(site, executeEveryNCycles);
        }
    }

    protected boolean validateRepository(Repository repository) throws IOException {
        for (Ref ref : repository.getRefDatabase().getRefs()) {
            if (ref.getObjectId() == null)
                continue;
            return true;
        }

        return false;
    }
}

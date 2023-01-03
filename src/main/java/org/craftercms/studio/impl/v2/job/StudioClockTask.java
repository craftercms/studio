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

package org.craftercms.studio.impl.v2.job;

import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class StudioClockTask implements SiteJob, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockTask.class);

    protected int executeEveryNCycles;
    protected Map<String, Integer> counters = new HashMap<>();
    protected int offset;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected ContentRepository contentRepository;
    protected ApplicationContext applicationContext;

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
        logger.debug("Clock Task '{}' for site '{}' with counter '{}' execute ever '{}' cycles", this.getClass().getName(), site, counters.get(site), executeEveryNCycles);
        if (checkCycleCounter(site)) {
            try {
                long sleepTime = (long) (Math.random() * offset);
                logger.debug("Sleep for an offset of '{}' milliseconds in site '{}'", sleepTime, site);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.debug("Woke up from the random offset in site '{}'", site);
            }
            executeInternal(site);
            setCycleCounter(site, executeEveryNCycles);
        }
    }

    public void setExecuteEveryNCycles(int executeEveryNCycles) {
        this.executeEveryNCycles = executeEveryNCycles;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Override
    public void setApplicationContext(final @NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}

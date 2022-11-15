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

import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.utils.spring.context.SystemStatusProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.STUDIO_CLOCK_EXECUTOR_SITE_LOCK;

public class StudioClockExecutor implements Job {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockExecutor.class);
    private static final ReentrantLock singleWorkerLock = new ReentrantLock();
    private static boolean running = false;
    private SystemStatusProvider systemStatusProvider;

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    private TaskExecutor taskExecutor;
    private SiteService siteService;
    private GeneralLockService generalLockService;
    private List<Job> globalTasks;
    private List<SiteJob> siteTasks;
    private static int threadCounter = 0;

    @Override
    public void execute() {
        threadCounter++;
        if (systemStatusProvider.isSystemReady()) {
            if (singleWorkerLock.tryLock()) {
                try {
                    setRunning(true);
                    logger.debug("Execute the Studio Clock Job in thread '{}'", threadCounter);
                    executeTasks();
                } catch (Exception e) {
                    logger.error("Studio Clock Job failed", e);
                } finally {
                    setRunning(false);
                    singleWorkerLock.unlock();
                }
            }
        } else {
            logger.debug("The system is not ready yet to execute Studio Clock Job. Skip a cycle.");
        }
    }

    private void executeTasks() {
        for (Job job : globalTasks) {
            job.execute();
        }

        List<String> sites = siteService.getAllCreatedSites();
        for (String site : sites) {
            taskExecutor.execute(() -> {
                String tasksLock = STUDIO_CLOCK_EXECUTOR_SITE_LOCK.replaceAll(PATTERN_SITE, site);
                if (generalLockService.tryLock(tasksLock)) {
                    try {
                        for (SiteJob siteTask : siteTasks) {
                            siteTask.execute(site);
                        }
                    } finally {
                        generalLockService.unlock(tasksLock);
                    }
                }
            });
        }
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setGlobalTasks(List<Job> globalTasks) {
        this.globalTasks = globalTasks;
    }

    public void setSiteTasks(List<SiteJob> siteTasks) {
        this.siteTasks = siteTasks;
    }

    public void setSystemStatusProvider(SystemStatusProvider systemStatusProvider) {
        this.systemStatusProvider = systemStatusProvider;
    }
}

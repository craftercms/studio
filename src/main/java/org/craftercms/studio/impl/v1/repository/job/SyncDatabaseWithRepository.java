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

package org.craftercms.studio.impl.v1.repository.job;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.locks.ReentrantLock;

public class SyncDatabaseWithRepository {

    private final static Logger logger = LoggerFactory.getLogger(SyncDatabaseWithRepository.class);

    private static ReentrantLock taskLock = new ReentrantLock();

    public void execute(String site, String lastDbCommitId) {
        if (taskLock.tryLock()) {
            try {
                logger.debug("Starting Sync Database With Repository Task.");
                SyncDatabaseWithRepositoryTask task = new SyncDatabaseWithRepositoryTask(site, lastDbCommitId);
                taskExecutor.execute(task);
            } finally {
                taskLock.unlock();
            }
        }
    }

    class SyncDatabaseWithRepositoryTask implements Runnable {

        private String site;
        private String lastDbCommitId;

        public SyncDatabaseWithRepositoryTask(String site, String lastDbCommitId) {
            this.site = site;
            this.lastDbCommitId = lastDbCommitId;
        }

        @Override
        public void run() {
            logger.debug("Start synchronizing database with repository  for site " + site);
            try {
                siteService.syncDatabaseWithRepo(site, lastDbCommitId);
            } catch (ServiceLayerException | UserNotFoundException e) {
                logger.error("Error while syncing database with repository", e);
            }
        }
    }

    protected SecurityService securityService;
    protected TaskExecutor taskExecutor;
    protected SiteService siteService;

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public TaskExecutor getTaskExecutor() { return taskExecutor; }
    public void setTaskExecutor(TaskExecutor taskExecutor) { this.taskExecutor = taskExecutor; }


    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

}

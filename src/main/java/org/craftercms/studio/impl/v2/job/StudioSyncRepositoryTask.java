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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.event.repository.RepositoryEvent;

import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;

public class StudioSyncRepositoryTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioSyncRepositoryTask.class);
    private static int threadCounter = 0;

    public void init() {
        threadCounter++;
    }

    @Override
    protected void executeInternal(String site) {
        try {
            logger.debug("Execute sync repository thread counter '{}' ID '{}'", threadCounter,
                    Thread.currentThread().getId());
            String siteState = siteService.getSiteState(site);
            if (StringUtils.equals(siteState, STATE_READY)) {
                syncRepository(site);
            }
        } catch (Exception e) {
            logger.error("Failed to sync the database from the repository in site '{}'", site, e);
        }
    }

    private void syncRepository(String site) throws ServiceLayerException, UserNotFoundException {
        logger.debug("Get the last verified commit ID for site '{}'", site);
        SiteFeed siteFeed = siteService.getSite(site);
        if (checkSiteUuid(site, siteFeed.getSiteUuid())) {
            String lastProcessedCommit = siteService.getLastVerifiedGitlogCommitId(site);
            if (StringUtils.isNotEmpty(lastProcessedCommit)) {
                GitLog gl = contentRepository.getGitLog(site, lastProcessedCommit);
                if (Objects.nonNull(gl)) {
                    String lastRepoCommitId = contentRepository.getRepoLastCommitId(site);
                    if (StringUtils.equals(lastRepoCommitId, lastProcessedCommit)) {
                        if (gl.getProcessed() == 0) {
                            contentRepository.markGitLogVerifiedProcessed(site, gl.getCommitId());
                        }
                        if (contentRepository.countUnprocessedCommits(site, gl.getId()) > 0) {
                            contentRepository.markGitLogProcessedBeforeMarker(site, gl.getId(), 1);
                        }
                    } else {
                        logger.debug("Sync the database with the repository in site '{}' from the " +
                                        "last processed commit '{}'", site, lastProcessedCommit);
                        List<GitLog> unprocessedCommitIds = contentRepository.getUnprocessedCommits(site, gl.getId());
                        if (unprocessedCommitIds != null && unprocessedCommitIds.size() > 0) {
                            siteService.syncDatabaseWithRepo(site, lastProcessedCommit);
                            unprocessedCommitIds.forEach(x -> {
                                contentRepository.markGitLogVerifiedProcessed(site, x.getCommitId());
                            });

                            // Sync all preview deployers
                            try {
                                logger.debug("Sync preview for site '{}'", site);
                                applicationContext.publishEvent(new RepositoryEvent(site));
                            } catch (Exception e) {
                                logger.error("Failed to sync preview for site '{}'", site, e);
                            }
                        } else {
                            GitLog gl2 = contentRepository.getGitLog(site, lastRepoCommitId);
                            if (Objects.nonNull(gl2) && !StringUtils.equals(lastRepoCommitId, lastProcessedCommit)) {
                                siteService.updateLastVerifiedGitlogCommitId(site, lastRepoCommitId);
                                contentRepository.markGitLogProcessedBeforeMarker(site, gl2.getId(), 1);
                            } else {
                                contentRepository.markGitLogProcessedBeforeMarker(site, gl.getId(), 1);
                            }
                        }
                    }
                }
            }
        }
    }

}

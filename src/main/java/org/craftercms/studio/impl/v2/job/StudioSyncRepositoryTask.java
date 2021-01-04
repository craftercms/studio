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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_UUID_FILENAME;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_CREATED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SITES_REPOS_PATH;

public class StudioSyncRepositoryTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioSyncRepositoryTask.class);
    private static int threadCounter = 0;
    private ContentRepository contentRepository;

    public StudioSyncRepositoryTask(int executeEveryNCycles,
                                    int offset,
                                    StudioConfiguration studioConfiguration,
                                    SiteService siteService,
                                    ContentRepository contentRepository) {
        super(executeEveryNCycles, offset, studioConfiguration, siteService);
        this.contentRepository = contentRepository;
        threadCounter++;
    }

    @Override
    protected void executeInternal(String site) {
        try {
            try {
                logger.debug("Executing sync repository thread ID = " + threadCounter + "; " +
                        Thread.currentThread().getId());
                String siteState = siteService.getSiteState(site);
                if (StringUtils.equals(siteState, STATE_CREATED)) {
                    syncRepository(site);
                }
            } catch (Exception e) {
                logger.error("Failed to sync database from repository for site " + site, e);
            }
        } catch (Exception e) {
            logger.error("Failed to sync database from repository for site " + site, e);
        }
    }

    private void syncRepository(String site) throws ServiceLayerException, UserNotFoundException {
        logger.debug("Getting last verified commit for site: " + site);
        SiteFeed siteFeed = siteService.getSite(site);
        if (checkSiteUuid(site, siteFeed.getSiteUuid())) {
            String lastProcessedCommit = siteService.getLastVerifiedGitlogCommitId(site);
            if (StringUtils.isNotEmpty(lastProcessedCommit)) {

                logger.debug("Syncing database with repository for site " + site + " from last processed commit "
                        + lastProcessedCommit);
                GitLog gl = contentRepository.getGitLog(site, lastProcessedCommit);
                if (Objects.nonNull(gl)) {
                    List<GitLog> unprocessedCommitIds = contentRepository.getUnprocessedCommits(site, gl.getId());
                    if (unprocessedCommitIds != null && unprocessedCommitIds.size() > 0) {
                        String firstUnprocessedCommit = unprocessedCommitIds.get(0).getCommitId();
                        siteService.syncDatabaseWithRepo(site, firstUnprocessedCommit);
                        unprocessedCommitIds.forEach(x -> {
                            contentRepository.markGitLogVerifiedProcessed(site, x.getCommitId());
                        });

                        String lastRepoCommitId = contentRepository.getRepoLastCommitId(site);
                        siteService.updateLastCommitId(site, lastRepoCommitId);
                        siteService.updateLastVerifiedGitlogCommitId(site, lastRepoCommitId);
                    } else {
                        String lastRepoCommitId = contentRepository.getRepoLastCommitId(site);
                        if (!StringUtils.equals(lastRepoCommitId, lastProcessedCommit)) {
                            siteService.updateLastVerifiedGitlogCommitId(site, lastRepoCommitId);
                        }
                    }
                }
            }
        }
    }

    private boolean checkSiteUuid(String siteId, String siteUuid) {
        boolean toRet = false;
        try {
            Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                    studioConfiguration.getProperty(SITES_REPOS_PATH), siteId, SITE_UUID_FILENAME);
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (!StringUtils.startsWith(line, "#") && StringUtils.equals(line, siteUuid)) {
                    toRet = true;
                    break;
                }
            }
        } catch (IOException e) {
            logger.info("Invalid site UUID. Local copy will not be deleted");
        }
        return toRet;
    }

}

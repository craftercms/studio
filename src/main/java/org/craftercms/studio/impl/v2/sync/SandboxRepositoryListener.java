/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.sync;

import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.event.site.SiteDeletingEvent;
import org.craftercms.studio.api.v2.event.site.SiteReadyEvent;
import org.craftercms.studio.api.v2.event.site.SyncFromRepoEvent;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.sync.RepositoryWatcher;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.impl.v2.utils.spring.event.BootstrapFinishedEvent;
import org.eclipse.jgit.lib.Repository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.Path;

import static org.craftercms.studio.api.v2.dal.Site.State.READY;

/**
 * Register and de-register sites for repository events.
 * This class will register all created sites on system bootstrap, and then listen
 * to site lifecycle events to register and de-register sites accordingly.
 */
public class SandboxRepositoryListener implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(SandboxRepositoryListener.class);
    private final SitesService siteService;
    private final GitRepositoryHelper repositoryHelper;
    private final RepositoryWatcher repositoryWatcher;
    private ApplicationEventPublisher eventPublisher;

    @ConstructorProperties({"siteService", "repositoryHelper",
            "repositoryWatcher"})
    public SandboxRepositoryListener(final SitesService siteService, final GitRepositoryHelper repositoryHelper,
                                     final RepositoryWatcher repositoryWatcher) {
        this.siteService = siteService;
        this.repositoryHelper = repositoryHelper;
        this.repositoryWatcher = repositoryWatcher;
    }

    @Async
    @EventListener(BootstrapFinishedEvent.class)
    public void onBootstrapFinished() {
        siteService.getSitesByState(READY).forEach(site -> {
            logger.debug("Registering site '{}' for repository events", site);
            Repository repo = repositoryHelper.getRepository(site.getSiteId(), GitRepositories.SANDBOX);
            if (repo == null) {
                // This can happen in clusters when a site is created while the replica is down
                logger.warn("Repository not found for site '{}'", site);
                return;
            }
            Path sandboxRepoPath = repo.getDirectory().toPath();
            try {
                repositoryWatcher.registerSite(site.getSiteId(), sandboxRepoPath);
            } catch (SiteNotFoundException | IOException e) {
                logger.error("Error registering site '{}' for repository events", site, e);
            }
            eventPublisher.publishEvent(new SyncFromRepoEvent(site.getSiteId()));
        });
    }

    private Path getSandboxRepoPath(String site) {
        Repository repo = repositoryHelper.getRepository(site, GitRepositories.SANDBOX);
        return repo.getDirectory().toPath();
    }

    @Async
    @Order(20)
    @EventListener
    public void onSiteReady(SiteReadyEvent event) throws SiteNotFoundException, IOException {
        logger.debug("Site ready event received for site '{}'", event.getSiteId());
        repositoryWatcher.registerSite(event.getSiteId(), getSandboxRepoPath(event.getSiteId()));
    }

    @Async
    @EventListener
    public void onSiteDeleting(SiteDeletingEvent event) {
        logger.debug("Site deleting event received for site '{}'", event.getSiteId());
        repositoryWatcher.deregisterSite(event.getSiteId());
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
}

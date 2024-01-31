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

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.event.site.SyncFromRepoEvent;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.sync.RepositoryWatcher;
import org.craftercms.studio.impl.v2.utils.spring.event.BootstrapFinishedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * {@link RepositoryWatcher} default implementation.
 */
public class RepositoryWatcherImpl implements RepositoryWatcher, ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryWatcherImpl.class);
    private static final String REFS_HEADS = "refs/heads";

    private final SitesService sitesService;

    private final WatchService watcher;
    private final BidiMap<WatchKey, String> siteKeys;
    private final Map<String, SiteRegistration> siteRegistrations;

    private ApplicationEventPublisher eventPublisher;

    public RepositoryWatcherImpl(final SitesService sitesService) throws IOException {
        this.sitesService = sitesService;
        watcher = FileSystems.getDefault().newWatchService();
        siteKeys = new DualHashBidiMap<>();
        siteRegistrations = new HashMap<>();
    }

    @Async
    @EventListener(BootstrapFinishedEvent.class)
    public void startWatching() {
        for (; ; ) {
            try {
                logger.debug("Getting a WatchKey");
                // Get the next key from the queue (this will block until an event is available)
                WatchKey key = watcher.take();
                String siteId = siteKeys.get(key);
                // Get the events queued for the key (notice that pollEvents() will not wait)
                for (WatchEvent<?> event : key.pollEvents()) {
                    // Check if the key is valid (in case the site has been just deleted)
                    if (key.isValid() && accept(event, siteId)) {
                        eventPublisher.publishEvent(new SyncFromRepoEvent(siteId));
                    }
                }
                if (!key.reset()) {
                    // This can happen if the watched directory is deleted
                    logger.warn("Failed to reset the WatchKey for site '{}'", siteId);
                    siteKeys.remove(key);
                    key.cancel();
                }
            } catch (InterruptedException e) {
                logger.error("Failed to get a WatchKey to monitor site repositories", e);
                return;
            }
        }
    }

    /**
     * Determines if the event should trigger a repo sync.
     *
     * @param event the event
     * @return true if the site should be synced, false otherwise
     */
    private boolean accept(WatchEvent<?> event, String siteId) {
        SiteRegistration siteRegistration = siteRegistrations.get(siteId);

        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path filename = ev.context();

        logger.debug("Event kind: {}. File affected: {}", event.kind(), filename);

        return filename.equals(siteRegistration.branchFilename());
    }

    @Override
    public void registerSite(String siteId, Path sitePath) throws SiteNotFoundException, IOException {
        logger.debug("Sandbox repo path: {}", sitePath);
        try {
            Site site = sitesService.getSite(siteId);
            String sandboxBranch = site.getSandboxBranch();

            Path sandboxBranchPath = sitePath.resolve(REFS_HEADS).resolve(sandboxBranch);

            SiteRegistration siteRegistration = new SiteRegistration(siteId, sitePath, sandboxBranchPath.getFileName());

            // Monitor the parent directory of the sandbox branch file (we cannot only monitor directories, not files)
            WatchKey key = sandboxBranchPath
                    .getParent()
                    .register(watcher,
                            ENTRY_CREATE,
                            ENTRY_MODIFY);
            siteKeys.put(key, siteId);
            siteRegistrations.put(siteId, siteRegistration);
        } catch (IOException e) {
            logger.error("Failed to register site '{}'", siteId, e);
            throw e;
        }
    }

    @Override
    public void deregisterSite(String siteId) {
        WatchKey key = siteKeys.removeValue(siteId);
        siteRegistrations.remove(siteId);
        if (key != null) {
            key.cancel();
        }
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    /**
     * Convenience record to hold site registration information.
     *
     * @param siteId         the site id
     * @param sitePath       the site path
     * @param branchFilename the filename portion of the branch file. e.g.: if sandbox branch name is 'crafter/feature/123',
     *                       the filename for the events will be '123'
     */
    private record SiteRegistration(String siteId, Path sitePath, Path branchFilename) {
    }
}

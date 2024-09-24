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
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.spring.event.BootstrapFinishedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_EVENT_DELAY_MILLIS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_SYNC_EVENT_MAX_RESET_COUNT;

/**
 * {@link RepositoryWatcher} default implementation. Based on {@link WatchService}.
 */
public class RepositoryWatcherImpl implements RepositoryWatcher, ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryWatcherImpl.class);
    private static final String REFS_HEADS = "refs/heads";

    private final SitesService sitesService;
    private final StudioConfiguration studioConfiguration;

    private final WatchService watcher;
    private final BidiMap<WatchKey, String> siteKeys;
    private final Map<String, SiteRegistration> siteRegistrations;

    private ApplicationEventPublisher eventPublisher;

    private final Map<String, QueuedEvent> queuedEvents;
    private final TaskExecutor taskExecutor;

    @ConstructorProperties({"sitesService", "studioConfiguration",
            "taskExecutor"})
    public RepositoryWatcherImpl(final SitesService sitesService, final StudioConfiguration studioConfiguration,
                                 final TaskExecutor taskExecutor) throws IOException {
        this.sitesService = sitesService;
        this.studioConfiguration = studioConfiguration;
        this.taskExecutor = taskExecutor;
        watcher = FileSystems.getDefault().newWatchService();
        siteKeys = new DualHashBidiMap<>();
        siteRegistrations = new HashMap<>();
        queuedEvents = new ConcurrentHashMap<>();
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
                logger.debug("Polling WatchKey events for site '{}'", siteId);
                for (WatchEvent<?> event : key.pollEvents()) {
                    // Check if the key is valid (in case the site has been just deleted)
                    if (key.isValid() && accept(event, siteId)) {
                        queueRepoEvent(siteId);
                    }
                }
                if (!key.reset()) {
                    // This can happen if the watched directory is deleted
                    logger.warn("Failed to reset the WatchKey for site '{}'", siteId);
                    siteKeys.remove(key);
                    key.cancel();
                }
            } catch (InterruptedException e) {
                // TODO: we may want to consider a mechanism to restart the thread
                logger.warn("Failed to monitor site repositories, thread has been interrupted");
                return;
            } catch (Exception e) {
                logger.error("Failed to process a WatchKey to monitor site repositories", e);
            }
        }
    }

    /**
     * Creates a {@link QueuedEvent} for the given site and adds it to the queue.
     *
     */
    private void queueRepoEvent(String siteId) {
        QueuedEvent event = queuedEvents.get(siteId);
        if (event != null) {
            event.additionalEvents().set(true);
        } else {
            QueuedEvent newEvent = new QueuedEvent(siteId, new AtomicBoolean(false));
            queuedEvents.put(siteId, newEvent);
            taskExecutor.execute(() -> processRepoEvent(newEvent));
        }
    }

    /**
     * Process the repo event.
     *
     * @param queuedEvent the event to process
     */
    private void processRepoEvent(final QueuedEvent queuedEvent) {
        int resetCount = 0;
        while (resetCount < getEventTimerMaxResetCount()) {
            try {
                // Give some time in case more events are coming
                Thread.sleep(getEventHandlingDelayMillis());
            } catch (InterruptedException e) {
                logger.warn("Thread has been interrupted while waiting for the event handling delay", e);
                return;
            }
            if (!queuedEvent.additionalEvents().get()) {
                break;
            }
            queuedEvent.additionalEvents().set(false);
            resetCount++;
        }
        queuedEvents.remove(queuedEvent.siteId());
        eventPublisher.publishEvent(new SyncFromRepoEvent(queuedEvent.siteId()));
    }

    private int getEventTimerMaxResetCount() {
        return studioConfiguration.getProperty(REPO_SYNC_EVENT_MAX_RESET_COUNT, Integer.class);
    }

    private long getEventHandlingDelayMillis() {
        return studioConfiguration.getProperty(REPO_SYNC_EVENT_DELAY_MILLIS, Long.class);
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

        logger.debug("Received event for site '{}', event kind: '{}'. File affected: '{}', watching file '{}'",
                siteId, event.kind(), filename, siteRegistration.branchFilename());

        return filename.equals(siteRegistration.branchFilename());
    }

    @Override
    public void registerSite(String siteId, Path sitePath) throws SiteNotFoundException, IOException {
        logger.debug("Registering site '{}', sandbox repo path: '{}'", siteId, sitePath);
        try {
            Site site = sitesService.getSite(siteId);
            String sandboxBranch = site.getSandboxBranch();
            logger.debug("Using sandbox branch: '{}' for site '{}'", sandboxBranch, siteId);

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
            logger.debug("Site '{}' registered", siteId);
        } catch (IOException e) {
            logger.error("Failed to register site '{}'", siteId, e);
            throw e;
        }
    }

    @Override
    public void deregisterSite(String siteId) {
        logger.debug("Deregistering site '{}'", siteId);
        WatchKey key = siteKeys.removeValue(siteId);
        siteRegistrations.remove(siteId);
        if (key != null) {
            key.cancel();
        }
        logger.debug("Site '{}' deregistered", siteId);
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

    /**
     * Hold a thread safe flag to indicate if additional events are queued for the site.
     *
     * @param siteId
     * @param additionalEvents
     */
    private record QueuedEvent(String siteId, AtomicBoolean additionalEvents) {
    }
}

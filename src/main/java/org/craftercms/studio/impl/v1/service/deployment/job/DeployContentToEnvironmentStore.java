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
package org.craftercms.studio.impl.v1.service.deployment.job;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.deployment.PublishingManager;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.notification.NotificationService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.job.RepositoryJob;
import org.springframework.core.task.TaskExecutor;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE;

public class DeployContentToEnvironmentStore extends RepositoryJob {

    private static final Logger logger = LoggerFactory.getLogger(DeployContentToEnvironmentStore.class);

    protected static final ReentrantLock singleWorkerLock = new ReentrantLock();

    private static boolean stopSignaled = false;
    private static boolean running = false;

    protected PublishingManager publishingManager;
    protected ContentRepository contentRepository;
    protected SiteService siteService;
    protected NotificationService notificationService;
    protected GeneralLockService generalLockService;
    protected EventService eventService;
    protected StudioConfiguration studioConfiguration;
    protected ServicesConfig servicesConfig;
    protected TaskExecutor taskExecutor;
    protected AuditServiceInternal auditServiceInternal;

    public static synchronized void signalToStop(boolean toStop) {
        stopSignaled = toStop;
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    public void execute() {
        if (isMasterPublishingNode() && !stopSignaled) {
            setRunning(true);
            if (singleWorkerLock.tryLock()) {
                try {
                    processJobs();
                } catch (Throwable err) {
                    logger.error("unable to execute job", err);
                } finally {
                    singleWorkerLock.unlock();
                }
            }
            setRunning(false);
        }
    }

    public void processJobs() {

        try {
            Set<String> siteNames = siteService.getAllAvailableSites();
            if (siteNames != null && siteNames.size() > 0) {
                for (String site : siteNames) {

                    PublisherTask publisherTask = new PublisherTask(site, studioConfiguration, siteService,
                            publishingManager, servicesConfig,contentRepository, notificationService, auditServiceInternal);
                    taskExecutor.execute(publisherTask);
                }
            }
        } catch (Exception err) {
            logger.error("Error while executing deployment to environment store", err);
            notificationService.notifyDeploymentError("UNKNOWN", err);
        }
    }

    public boolean isMasterPublishingNode() {
        boolean toReturn =
                Boolean.parseBoolean(studioConfiguration.getProperty(JOB_DEPLOYMENT_MASTER_PUBLISHING_NODE));
        return toReturn;
    }

    public PublishingManager getPublishingManager() {
        return publishingManager;
    }

    public void setPublishingManager(PublishingManager publishingManager) {
        this.publishingManager = publishingManager;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }
}

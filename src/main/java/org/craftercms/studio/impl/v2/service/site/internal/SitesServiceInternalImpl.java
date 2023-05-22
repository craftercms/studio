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

package org.craftercms.studio.impl.v2.service.site.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.plugin.PluginDescriptorReader;
import org.craftercms.commons.plugin.exception.PluginException;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.event.site.SiteDeleteEvent;
import org.craftercms.studio.api.v2.exception.CompositeException;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.beans.ConstructorProperties;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;

public class SitesServiceInternalImpl implements SitesService, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(SitesServiceInternalImpl.class);

    private final PluginDescriptorReader descriptorReader;
    private final ContentRepository contentRepositoryV1;
    private final org.craftercms.studio.api.v2.repository.ContentRepository contentRepository;
    private final StudioConfiguration studioConfiguration;
    private final SiteFeedMapper siteFeedMapper;
    private final SiteDAO siteDao;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private final Deployer deployer;
    private final ConfigurationService configurationService;
    private final SecurityService securityService;
    private final AuditServiceInternal auditServiceInternal;
    private ApplicationContext applicationContext;

    @ConstructorProperties({"descriptorReader", "contentRepositoryV1",
            "studioConfiguration", "siteFeedMapper",
            "siteDao", "retryingDatabaseOperationFacade",
            "deployer", "contentRepository",
            "configurationService", "securityService",
            "auditServiceInternal"})
    public SitesServiceInternalImpl(final PluginDescriptorReader descriptorReader, final ContentRepository contentRepositoryV1,
                                    final StudioConfiguration studioConfiguration, final SiteFeedMapper siteFeedMapper,
                                    final SiteDAO siteDao, final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade,
                                    final Deployer deployer, final org.craftercms.studio.api.v2.repository.ContentRepository contentRepository,
                                    final ConfigurationService configurationService, final SecurityService securityService,
                                    final AuditServiceInternal auditServiceInternal) {
        this.descriptorReader = descriptorReader;
        this.contentRepositoryV1 = contentRepositoryV1;
        this.studioConfiguration = studioConfiguration;
        this.siteFeedMapper = siteFeedMapper;
        this.siteDao = siteDao;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.deployer = deployer;
        this.contentRepository = contentRepository;
        this.configurationService = configurationService;
        this.securityService = securityService;
        this.auditServiceInternal = auditServiceInternal;
    }

    @Override
    public List<PluginDescriptor> getAvailableBlueprints() {
        RepositoryItem[] blueprintsFolders = getBlueprintsFolders();
        List<PluginDescriptor> toRet = new ArrayList<>();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                PluginDescriptor descriptor = loadDescriptor(folder);
                if (descriptor != null) {
                    toRet.add(descriptor);
                }
            }
        }
        return toRet;
    }

    @Override
    public PluginDescriptor getBlueprintDescriptor(final String id) {
        RepositoryItem[] blueprintsFolders = getBlueprintsFolders();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                PluginDescriptor descriptor = loadDescriptor(folder);
                if (descriptor != null && descriptor.getPlugin().getId().equals(id)) {
                    return descriptor;
                }
            }
        }
        return null;
    }

    @Override
    public String getBlueprintLocation(String blueprintId) {
        RepositoryItem[] blueprintsFolders = getBlueprintsFolders();
        for (RepositoryItem folder : blueprintsFolders) {
            if (folder.isFolder) {
                Path descriptorPath = getBlueprintPath(folder);
                PluginDescriptor descriptor = loadDescriptor(folder);
                if (descriptor != null && descriptor.getPlugin().getId().equals(blueprintId)) {
                    return descriptorPath.getParent().toAbsolutePath().toString();
                }
            }
        }

        return StringUtils.EMPTY;
    }

    @Override
    public PluginDescriptor getSiteBlueprintDescriptor(final String id) {
        String descriptorPath = studioConfiguration.getProperty(REPO_BLUEPRINTS_DESCRIPTOR_FILENAME);
        if (contentRepositoryV1.contentExists(id, descriptorPath)) {
            try (InputStream is = contentRepositoryV1.getContent(id, descriptorPath)) {
                return loadDescriptor(is);
            } catch (Exception e) {
                logger.error("Failed to get site blueprint descriptor for site '{}'", id, e);
            }
        }
        return null;
    }

    protected RepositoryItem[] getBlueprintsFolders() {
        return contentRepositoryV1.getContentChildren(
                StringUtils.EMPTY, studioConfiguration.getProperty(BLUE_PRINTS_PATH));
    }

    protected Path getBlueprintPath(RepositoryItem folder) {
        return Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.GLOBAL_REPO_PATH), folder.path, folder.name,
                studioConfiguration.getProperty(REPO_BLUEPRINTS_DESCRIPTOR_FILENAME)).toAbsolutePath();
    }

    protected PluginDescriptor loadDescriptor(InputStream is) {
        try {
            return descriptorReader.read(is);
        } catch (PluginException e) {
            logger.error("Failed to load descriptor", e);
        }
        return null;
    }

    protected PluginDescriptor loadDescriptor(RepositoryItem folder) {
        Path descriptorPath = getBlueprintPath(folder);
        if (Files.exists(descriptorPath)) {
            try (FileReader reader = new FileReader(descriptorPath.toString())) {
                return descriptorReader.read(reader);
            } catch (PluginException | IOException e) {
                logger.error("Failed to load descriptor from blueprint '{}'", folder.name, e);
            }
        }
        return null;
    }

    @Override
    public void updateSite(String siteId, String name, String description)
            throws SiteNotFoundException, SiteAlreadyExistsException {
        if (isNotEmpty(name) && siteFeedMapper.isNameUsed(siteId, name)) {
            throw new SiteAlreadyExistsException("A site with name " + name + " already exists");
        }
        int updated = retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updateSite(siteId, name, description));
        if (updated != 1) {
            throw new SiteNotFoundException();
        }
    }

    @Override
    public boolean exists(String siteId) {
        return siteDao.exists(siteId);
    }

    @Override
    public void deleteSite(String siteId) throws ServiceLayerException {
        logger.info("Delete site '{}'", siteId);
        Site site = siteDao.getSite(siteId);
        insertDeleteSiteAuditLog(site.getSiteId(), site.getName(), OPERATION_START_DELETE);
        siteDao.startSiteDelete(siteId);

        List<Exception> exceptions = new ArrayList<>();

        try {
            // Disable publishing
            logger.debug("Disable publishing for site '{}'", siteId);
            enablePublishing(siteId, false);
        } catch (Exception e) {
            logger.error("Failed to disable publishing for site '{}'", siteId, e);
        }

        try {
            // Delete deployer targets
            logger.debug("Delete deployer targets for site '{}'", siteId);
            deployer.deleteTargets(siteId);
        } catch (Exception e) {
            logger.error("Failed to delete deployer targets for site '{}'", siteId, e);
            exceptions.add(new ServiceLayerException(format("Failed to delete deployer targets for site '%s'", siteId), e));
        }

        try {
            // Destroy site preview context
            logger.debug("Destroy site preview context for site '{}'", siteId);
            destroySitePreviewContext(siteId);
        } catch (Exception e) {
            logger.error("Failed to destroy site preview context for site '{}'", siteId, e);
        }

        try {
            // Delete site content repository
            logger.debug("Delete site content repository for site '{}'", siteId);
            contentRepository.deleteSite(siteId);
        } catch (Exception e) {
            logger.error("Failed to delete site content repository for site '{}'", siteId, e);
            exceptions.add(new ServiceLayerException(format("Failed to delete site content repository for site '%s'", siteId), e));
        }

        try {
            // Clear configuration cache for site
            logger.debug("Clear configuration cache for site '{}'", siteId);
            configurationService.invalidateConfiguration(siteId);
        } catch (Exception e) {
            logger.error("Failed to clear configuration cache for site '{}'", siteId, e);
            exceptions.add(new ServiceLayerException(format("Failed to clear configuration cache for site '%s'", siteId), e));
        }

        try {
            // Delete database records
            logger.debug("Delete database records for site '{}'", siteId);
            retryingDatabaseOperationFacade.retry(() -> siteDao.deleteSiteRelatedItems(siteId));
        } catch (Exception e) {
            logger.error("Failed to delete the database records for site '{}'", siteId, e);
            exceptions.add(new ServiceLayerException(format("Failed to delete the database records for site '%s'", siteId), e));
        }

        // Don't update site record if there are any exceptions
        if (exceptions.isEmpty()) {
            try {
                // delete database records
                logger.debug("Mark the site '{}' as DELETED", siteId);
                retryingDatabaseOperationFacade.retry(() -> siteDao.completeSiteDelete(siteId));
                insertDeleteSiteAuditLog(site.getSiteId(), site.getName(), OPERATION_DELETE);
            } catch (Exception e) {
                logger.error("Failed to mark site '{}' as 'DELETED'", siteId, e);
                exceptions.add(new ServiceLayerException(format("Failed to mark site '%s' as 'DELETED'", siteId), e));
            }
        }
        if (!exceptions.isEmpty()) {
            throw new CompositeException(format("Failed to delete site '%s'", siteId), exceptions);
        }

        logger.info("Site '{}' deleted", siteId);
        applicationContext.publishEvent(new SiteDeleteEvent(siteId, site.getSiteUuid()));
    }

    /**
     * Insert delete site audit log entry
     *
     * @param siteId    the site id (String id)
     * @param siteName  the site name
     * @param operation the operation to record: OPERATION_START_DELETE or OPERATION_DELETE
     */
    private void insertDeleteSiteAuditLog(String siteId, String siteName, String operation) {
        Site globalSite = siteDao.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
        String user = securityService.getCurrentUser();
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(operation);
        auditLog.setSiteId(globalSite.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteName);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    private void destroySitePreviewContext(String siteId) throws ServiceLayerException {
        String requestUrl = studioConfiguration.getProperty(CONFIGURATION_SITE_PREVIEW_DESTROY_CONTEXT_URL)
                .replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, siteId);

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ServiceLayerException(format("Failed to destroy site preview context for site '%s'", siteId));
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceLayerException(format("Failed to destroy site preview context for site '%s'", siteId), e);
        }
    }

    @Override
    public void enablePublishing(String siteId, boolean enabled) {
        retryingDatabaseOperationFacade.retry(() -> siteDao.enablePublishing(siteId, enabled));
    }

    @Override
    public PublishStatus getPublishingStatus(String siteId) {
        int ttl = studioConfiguration.getProperty(PUBLISHING_SITE_LOCK_TTL, Integer.class);
        return siteFeedMapper.getPublishingStatus(siteId, ttl);
    }

    @Override
    public void clearPublishingLock(String siteId) {
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.clearPublishingLockForSite(siteId));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

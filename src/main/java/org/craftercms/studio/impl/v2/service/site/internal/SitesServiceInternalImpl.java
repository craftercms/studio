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

package org.craftercms.studio.impl.v2.service.site.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.plugin.PluginDescriptorReader;
import org.craftercms.commons.plugin.exception.PluginException;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.event.site.SiteReadyEvent;
import org.craftercms.studio.api.v2.exception.InvalidSiteStateException;
import org.craftercms.studio.api.v2.repository.blob.StudioBlobAwareContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;

public class SitesServiceInternalImpl implements SitesService, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(SitesServiceInternalImpl.class);

    private final PluginDescriptorReader descriptorReader;
    private final ContentRepository contentRepository;
    private final StudioBlobAwareContentRepository blobAwareRepository;
    private final StudioConfiguration studioConfiguration;
    private final SiteFeedMapper siteFeedMapper;
    private final SiteDAO siteDao;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private final SiteService siteServiceV1;
    private final Deployer deployer;
    private final ConfigurationService configurationService;
    private final SecurityService securityService;
    private final AuditServiceInternal auditServiceInternal;
    private ApplicationContext applicationContext;

    @ConstructorProperties({"descriptorReader", "contentRepository",
            "blobAwareRepository",
            "studioConfiguration", "siteFeedMapper",
            "siteDao",
            "retryingDatabaseOperationFacade", "siteServiceV1",
            "deployer", "configurationService",
            "securityService", "auditServiceInternal"})
    public SitesServiceInternalImpl(PluginDescriptorReader descriptorReader, ContentRepository contentRepository,
                                    StudioBlobAwareContentRepository blobAwareRepository,
                                    StudioConfiguration studioConfiguration, SiteFeedMapper siteFeedMapper,
                                    SiteDAO siteDao,
                                    RetryingDatabaseOperationFacade retryingDatabaseOperationFacade, SiteService siteServiceV1,
                                    Deployer deployer, ConfigurationService configurationService,
                                    SecurityService securityService, AuditServiceInternal auditServiceInternal) {
        this.descriptorReader = descriptorReader;
        this.contentRepository = contentRepository;
        this.blobAwareRepository = blobAwareRepository;
        this.studioConfiguration = studioConfiguration;
        this.siteFeedMapper = siteFeedMapper;
        this.siteDao = siteDao;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.siteServiceV1 = siteServiceV1;
        this.deployer = deployer;
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
        if (contentRepository.contentExists(id, descriptorPath)) {
            try (InputStream is = contentRepository.getContent(id, descriptorPath)) {
                return loadDescriptor(is);
            } catch (Exception e) {
                logger.error("Failed to get site blueprint descriptor for site '{}'", id, e);
            }
        }
        return null;
    }

    protected RepositoryItem[] getBlueprintsFolders() {
        return contentRepository.getContentChildren(
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
    public PublishStatus getPublishingStatus(String siteId) {
        int ttl = studioConfiguration.getProperty(PUBLISHING_SITE_LOCK_TTL, Integer.class);
        return siteFeedMapper.getPublishingStatus(siteId, ttl);
    }

    @Override
    public void clearPublishingLock(String siteId) {
        retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.clearPublishingLockForSite(siteId));
    }

    @Override
    public void checkSiteState(final String siteId, final String requiredState) throws InvalidSiteStateException, SiteNotFoundException {
        SiteFeed site = siteFeedMapper.getSite(Map.of(SITE_ID, siteId));
        if (site == null) {
            throw new SiteNotFoundException(siteId);
        }
        if (!requiredState.equals(site.getState())) {
            throw new InvalidSiteStateException(siteId, format("Site '%s' state ('%s') is not the required value: '%s'",
                    siteId, site.getState(), requiredState));
        }
    }

    @Override
    public void duplicate(String sourceSiteId, String siteId, String siteName, String description, String sandboxBranch, boolean readOnlyBlobStores)
            throws ServiceLayerException {
        if (isNotEmpty(siteName) && siteFeedMapper.isNameUsed(siteId, siteName)) {
            throw new SiteAlreadyExistsException(format("A site with name '%s' already exists", siteName));
        }

        if (isEmpty(sandboxBranch)) {
            sandboxBranch = studioConfiguration.getProperty(REPO_SANDBOX_BRANCH);
        }
        doDuplicate(sourceSiteId, siteId, siteName, description, sandboxBranch, readOnlyBlobStores);
    }

    protected void doDuplicate(String sourceSiteId, String siteId, String siteName, String description, String sandboxBranch, boolean readOnlyBlobStores)
            throws ServiceLayerException {
        logger.info("Site duplicate from '{}' to '{}' - START", sourceSiteId, siteId);

        Site sourceSite = siteDao.getSite(sourceSiteId);
        boolean publishingEnabled = sourceSite.getPublishingEnabled();
        try {
            // Lock source site
            if (publishingEnabled) {
                siteServiceV1.enablePublishing(sourceSiteId, false);
            }
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(sourceSiteId, SiteFeed.STATE_LOCKED));
            readOnlyBlobStores = readOnlyBlobStores && !studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false);

            // Copy site repos in disk
            logger.debug("Duplicate site repos in disk from '{}' to '{}'", sourceSiteId, siteId);
            blobAwareRepository.duplicateSite(sourceSiteId, siteId, sourceSite.getSandboxBranch(), sandboxBranch);

            String siteUuid = UUID.randomUUID().toString();
            addSiteUuidFile(siteId, siteUuid);
            // Create site in db (site state is INITIALIZING) and copy all db data
            logger.debug("Duplicate site DB data from '{}' to '{}'", sourceSiteId, siteId);
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.duplicate(sourceSiteId, siteId, siteName, description, sandboxBranch, siteUuid));

            // Duplicate site in deployer
            logger.debug("Duplicate site deployer targets from '{}' to '{}'", sourceSiteId, siteId);
            deployer.duplicateTargets(sourceSiteId, siteId);

            // read-only blobstores
            if (readOnlyBlobStores) {
                logger.debug("Make blobstores read-only for duplicate site '{}'", siteId);
                configurationService.makeBlobStoresReadOnly(siteId);
            } else {
                logger.debug("Duplicating blobstores content from site '{}' to '{}'", sourceSiteId, siteId);
                blobAwareRepository.duplicateBlobs(sourceSiteId, siteId);
            }

            auditSiteDuplicate(sourceSiteId, siteId, siteName);

            // Set site state to READY
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(siteId, SiteFeed.STATE_READY));
            siteServiceV1.enablePublishing(siteId, true);
            applicationContext.publishEvent(new SiteReadyEvent(siteId, siteUuid));
            logger.info("Site duplicate from '{}' to '{}' - COMPLETE", sourceSiteId, siteId);
        } catch (ServiceLayerException ex) {
            siteServiceV1.deleteSite(siteId);
            throw ex;
        } catch (Exception ex) {
            siteServiceV1.deleteSite(siteId);
            throw new ServiceLayerException(format("Failed to duplicate site '%s' into '%s'", sourceSiteId, siteId), ex);
        } finally {
            // Unlock source site
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(sourceSiteId, SiteFeed.STATE_READY));
            if (publishingEnabled) {
                siteServiceV1.enablePublishing(sourceSiteId, true);
            }
        }
    }

    @Override
    public List<Site> getSitesByState(String state) {
        return siteDao.getSitesByState(state);
    }

    /**
     * Creates an audit log entry for the site duplication operation, including the source site as audit params
     *
     * @param sourceSiteId the source site id
     * @param siteId       the new site id
     * @param siteName     the new site name
     */
    protected void auditSiteDuplicate(final String sourceSiteId, final String siteId, final String siteName) {
        SiteFeed globalSiteFeed = siteFeedMapper.getSite(Map.of(SITE_ID, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE)));
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_DUPLICATE);
        auditLog.setSiteId(globalSiteFeed.getId());
        auditLog.setActorId(securityService.getCurrentUser());
        auditLog.setPrimaryTargetId(siteId);
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(siteName);
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
        AuditLogParameter auditLogParameter = new AuditLogParameter();
        auditLogParameter.setTargetId(siteId);
        auditLogParameter.setTargetType(TARGET_TYPE_SOURCE_SITE);
        auditLogParameter.setTargetValue(sourceSiteId);
        auditLogParameters.add(auditLogParameter);

        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    /**
     * Add a file containing the site uuid in the site folder
     *
     * @param site     site id
     * @param siteUuid site uuid
     * @throws IOException if the file cannot be written
     */
    protected void addSiteUuidFile(final String site, final String siteUuid) throws IOException {
        Path path = Paths.get(studioConfiguration.getProperty(REPO_BASE_PATH),
                studioConfiguration.getProperty(SITES_REPOS_PATH), site,
                StudioConstants.SITE_UUID_FILENAME);
        String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
        Files.write(path, toWrite.getBytes());
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

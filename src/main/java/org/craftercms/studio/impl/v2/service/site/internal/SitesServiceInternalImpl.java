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
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.exception.InvalidSiteStateException;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.service.site.SiteServiceImpl;
import org.craftercms.studio.impl.v2.deployment.PreviewDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;

public class SitesServiceInternalImpl implements SitesService {

    private final static Logger logger = LoggerFactory.getLogger(SitesServiceInternalImpl.class);

    private final PluginDescriptorReader descriptorReader;
    private final ContentRepository contentRepository;
    private final org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2;
    private final StudioConfiguration studioConfiguration;
    private final SiteFeedMapper siteFeedMapper;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private final SiteServiceImpl siteServiceV1;
    private final PreviewDeployer previewDeployer;

    @ConstructorProperties({"descriptorReader", "contentRepository",
            "contentRepositoryV2",
            "studioConfiguration", "siteFeedMapper",
            "retryingDatabaseOperationFacade", "siteServiceV1",
            "previewDeployer"})
    public SitesServiceInternalImpl(PluginDescriptorReader descriptorReader, ContentRepository contentRepository,
                                    org.craftercms.studio.api.v2.repository.ContentRepository contentRepositoryV2,
                                    StudioConfiguration studioConfiguration, SiteFeedMapper siteFeedMapper,
                                    RetryingDatabaseOperationFacade retryingDatabaseOperationFacade, SiteServiceImpl siteServiceV1,
                                    PreviewDeployer previewDeployer) {
        this.descriptorReader = descriptorReader;
        this.contentRepository = contentRepository;
        this.contentRepositoryV2 = contentRepositoryV2;
        this.studioConfiguration = studioConfiguration;
        this.siteFeedMapper = siteFeedMapper;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.siteServiceV1 = siteServiceV1;
        this.previewDeployer = previewDeployer;
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
    public void duplicate(String sourceSiteId, String siteId, String siteName, String description, String sandboxBranch)
            throws ServiceLayerException {
        if (isNotEmpty(siteName) && siteFeedMapper.isNameUsed(siteId, siteName)) {
            throw new SiteAlreadyExistsException(format("A site with name '%s' already exists", siteName));
        }

        boolean publishingEnabled = siteServiceV1.isPublishingEnabled(sourceSiteId);
        try {
            // Lock source site
            if (publishingEnabled) {
                siteServiceV1.enablePublishing(sourceSiteId, false);
            }
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(sourceSiteId, SiteFeed.STATE_LOCKED));

            // Copy site repos in disk
            contentRepositoryV2.duplicateSite(sourceSiteId, siteId, sandboxBranch);

            String siteUuid = UUID.randomUUID().toString();
            addSiteUuidFile(siteId, siteUuid);
            // Create site in db (site state is INITIALIZING) and copy all db data
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.duplicate(sourceSiteId, siteId, siteName, description, sandboxBranch, siteUuid));

            // Duplicate site in deployer
            previewDeployer.duplicateTargets(sourceSiteId, siteId);

            // Set site state to READY
            retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(siteId, SiteFeed.STATE_READY));
            siteServiceV1.enablePublishing(siteId, true);
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

    /**
     * Add a file containing the site uuid in the site folder
     *
     * @param site     site id
     * @param siteUuid site uuid
     * @throws IOException if the file cannot be written
     */
    private void addSiteUuidFile(final String site, final String siteUuid) throws IOException {
        Path path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                StudioConstants.SITE_UUID_FILENAME);
        String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
        Files.write(path, toWrite.getBytes());
    }
}

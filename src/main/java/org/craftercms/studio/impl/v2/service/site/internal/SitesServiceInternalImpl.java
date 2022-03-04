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
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.SiteAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v2.dal.PublishStatus;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.site.internal.SitesServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.BLUE_PRINTS_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PUBLISHING_SITE_LOCK_TTL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.REPO_BLUEPRINTS_DESCRIPTOR_FILENAME;

public class SitesServiceInternalImpl implements SitesServiceInternal {

    private final static Logger logger = LoggerFactory.getLogger(SitesServiceInternalImpl.class);

    private PluginDescriptorReader descriptorReader;
    private ContentRepository contentRepository;
    private StudioConfiguration studioConfiguration;
    private SiteFeedMapper siteFeedMapper;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public List<PluginDescriptor> getAvailableBlueprints() {
        RepositoryItem[] blueprintsFolders = getBlueprintsFolders();
        List<PluginDescriptor> toRet = new ArrayList<PluginDescriptor>();
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
                logger.error("Error while getting blueprint descriptor for site " + id, e);
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
            logger.error("Error while getting descriptor from stream", e);
        }
        return null;
    }

    protected PluginDescriptor loadDescriptor(RepositoryItem folder) {
        Path descriptorPath = getBlueprintPath(folder);
        if (Files.exists(descriptorPath)) {
            try (FileReader reader = new FileReader(descriptorPath.toString())) {
                return descriptorReader.read(reader);
            } catch (PluginException | IOException e) {
                logger.error("Error while getting descriptor for blueprint " + folder.name, e);
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
        int updated = retryingDatabaseOperationFacade.updateSite(siteId, name, description);
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
        retryingDatabaseOperationFacade.clearPublishingLockForSite(siteId);
    }

    public PluginDescriptorReader getDescriptorReader() {
        return descriptorReader;
    }

    public void setDescriptorReader(PluginDescriptorReader descriptorReader) {
        this.descriptorReader = descriptorReader;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SiteFeedMapper getSiteFeedMapper() {
        return siteFeedMapper;
    }

    public void setSiteFeedMapper(SiteFeedMapper siteFeedMapper) {
        this.siteFeedMapper = siteFeedMapper;
    }

    public RetryingDatabaseOperationFacade getRetryingDatabaseOperationFacade() {
        return retryingDatabaseOperationFacade;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}

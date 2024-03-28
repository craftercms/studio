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

package org.craftercms.studio.impl.v1.util.config.profiles;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationMapper;
import org.craftercms.commons.config.ConfigurationProvider;
import org.craftercms.commons.config.profiles.ConfigurationProfile;
import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

/**
 * Reads a configuration profiles file for a specific site and loads a specific {@link ConfigurationProfile}.
 *
 * @author avasquez
 */
public class SiteAwareConfigProfileLoader<T extends ConfigurationProfile> {

    private String profilesModule;
    private String profilesPath;
    private ConfigurationMapper<T> profileMapper;
    private ContentService contentService;

    public SiteAwareConfigProfileLoader(String profilesModule, String profilesPath, ConfigurationMapper<T> profileMapper,
                                        ContentService contentService) {
        this.profilesModule = profilesModule;
        this.profilesPath = profilesPath;
        this.profileMapper = profileMapper;
        this.contentService = contentService;
    }

    public T loadProfile(String site, String profileId) throws ConfigurationException, ConfigurationProfileNotFoundException {
        try (InputStream is = contentService.getContent(site, profilesPath)) {
            return profileMapper.readConfig(new ConfigurationProviderImpl(site), profilesModule,
                    profilesPath, null, profileId);
        } catch (ConfigurationProfileNotFoundException e) {
            throw new ConfigurationProfileNotFoundException(format("Profile '%s' not found from configuration at '%s'",
                    profileId, profilesPath), e);
        } catch (Exception e) {
            throw new ConfigurationException(format("Error while loading profile '%s' from configuration at '%s'",
                    profileId, profilesPath), e);
        }
    }

    /**
     *  Internal class to provide access to configuration files
     */
    private class ConfigurationProviderImpl implements ConfigurationProvider {

        private String site;

        public ConfigurationProviderImpl(String site) {
            this.site = site;
        }

        @Override
        public boolean configExists(String path) {
            return SiteAwareConfigProfileLoader.this.contentService.contentExists(site, path);
        }

        @Override
        public InputStream getConfig(String path) throws IOException {
            try {
                return SiteAwareConfigProfileLoader.this.contentService.getContent(site, path);
            } catch (Exception e) {
                throw new IOException("Error reading file", e);
            }
        }

    }

}

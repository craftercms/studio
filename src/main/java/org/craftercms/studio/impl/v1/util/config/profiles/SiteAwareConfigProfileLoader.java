/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.impl.v1.util.config.profiles;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationMapper;
import org.craftercms.commons.config.profiles.ConfigurationProfile;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.beans.factory.annotation.Required;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Reads a configuration profiles file for a specific site and loads a specific {@link ConfigurationProfile}.
 *
 * @author avasquez
 */
public class SiteAwareConfigProfileLoader<T extends ConfigurationProfile> {

    private String profilesPath;
    private ConfigurationMapper<T> profileMapper;
    private ContentService contentService;

    @Required
    public void setProfilesPath(String profilesPath) {
        this.profilesPath = profilesPath;
    }

    @Required
    public void setProfileMapper(ConfigurationMapper<T> profileMapper) {
        this.profileMapper = profileMapper;
    }

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public T loadProfile(String site, String profileId) throws ConfigurationException {
        try (InputStream is = contentService.getContent(site, profilesPath)) {
            return profileMapper.readConfig(is, StandardCharsets.UTF_8.name(), profileId);
        } catch (Exception e) {
            throw new ConfigurationException("Error while loading profile " + profileId + " from configuration at " +
                                             profileId, e);
        }
    }

}

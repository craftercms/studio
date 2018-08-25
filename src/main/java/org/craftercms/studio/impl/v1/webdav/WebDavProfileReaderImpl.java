/*
 * Copyright (C) 2007-2018 Crafter Software Corporation.
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
 */

package org.craftercms.studio.impl.v1.webdav;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.io.FileHandler;
import org.craftercms.studio.api.v1.exception.WebDavException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.webdav.WebDavProfile;
import org.craftercms.studio.api.v1.webdav.WebDavProfileReader;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link WebDavProfileReader}.
 * @author joseross
 */
public class WebDavProfileReaderImpl implements WebDavProfileReader {

    private static final String CONFIG_KEY_PROFILE = "profile";
    private static final String CONFIG_KEY_ID = "id";
    private static final String CONFIG_KEY_BASE_URL = "baseUrl";
    private static final String CONFIG_KEY_USERNAME = "username";
    private static final String CONFIG_KEY_PASSWORD = "password";

    /**
     * Path of the configuration file in the site repository.
     */
    protected String configPath;

    /**
     * Current instance of {@link ContentService}.
     */
    protected ContentService contentService;

    @Required
    public void setConfigPath(final String configPath) {
        this.configPath = configPath;
    }

    @Required
    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Reads the configuration file from the site repository.
     * @param site the name of the site
     * @return the configuration object
     * @throws WebDavException if there is any error reading the file
     */
    protected HierarchicalConfiguration getConfiguration(String site) throws WebDavException {
        try {
            InputStream input = contentService.getContent(site, configPath);
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> builder = new FileBasedConfigurationBuilder<>(XMLConfiguration.class);

            XMLConfiguration config = builder.configure(params.xml()).getConfiguration();
            FileHandler fileHandler = new FileHandler(config);

            fileHandler.setEncoding("UTF-8");
            fileHandler.load(input);

            return config;
        } catch (Exception e) {
            throw new WebDavException("Unable to read configuration file", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes, unchecked")
    public WebDavProfile getProfile(final String site, final String profileId) throws WebDavException {
        HierarchicalConfiguration config = getConfiguration(site);

        List<HierarchicalConfiguration> profiles = config.configurationsAt(CONFIG_KEY_PROFILE);
        HierarchicalConfiguration profileConfig = profiles.stream()
            .filter(c -> profileId.equals(c.getString(CONFIG_KEY_ID)))
            .findFirst()
            .orElseThrow(() -> new WebDavException("Profile '" + profileId + "' not found"));

        WebDavProfile profile = new WebDavProfile();
        profile.setBaseUrl(profileConfig.getString(CONFIG_KEY_BASE_URL));
        profile.setUsername(profileConfig.getString(CONFIG_KEY_USERNAME));
        profile.setPassword(profileConfig.getString(CONFIG_KEY_PASSWORD));

        return profile;
    }

}

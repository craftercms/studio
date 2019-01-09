/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import org.craftercms.studio.api.v1.service.aws.AwsProfileManager;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.impl.v1.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link AwsProfileManager} that reads a single XML file from the repository.\
 *
 * @author joseross
 */
public class AwsProfileManagerImpl implements AwsProfileManager {

    /**
     * The path where the XML file is stored.
     */
    protected String basePath;

    /**
     * The name of the XML file.
     */
    protected String fileName;

    /**
     * Crafter Studio content service.
     */
    protected ContentService contentService;

    @Required
    public void setBasePath(final String basePath) {
        this.basePath = basePath;
    }

    @Required
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Required
    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
    }

    protected HierarchicalConfiguration getConfiguration(InputStream input) throws Exception {
        try {
            return ConfigUtils.readXmlConfiguration(input);
        } catch (ConfigurationException e) {
            throw new Exception("Unable to read the AWS configuration", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public HierarchicalConfiguration getProfile(String site, String profileId) throws AwsConfigurationException {
        try {
            InputStream content = contentService.getContent(site, basePath + "/" + fileName);
            HierarchicalConfiguration config = getConfiguration(content);
            List<HierarchicalConfiguration> profiles = config.configurationsAt("profile");
            Optional<HierarchicalConfiguration> profile =
                profiles.stream()
                            .filter(profileItem -> profileId.equals(profileItem.getString("id")))
                            .findFirst();
            return profile.orElseThrow(() -> new AwsConfigurationException("Profile not found: " + profileId));
        } catch (Exception e) {
            throw new AwsConfigurationException("Unable to retrieve profile", e);
        }
    }

}

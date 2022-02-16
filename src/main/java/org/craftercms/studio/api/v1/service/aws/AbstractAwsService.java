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

package org.craftercms.studio.api.v1.service.aws;

import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.profiles.aws.AbstractAwsProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides common profile operations used by all services.
 * @param <T> The type of {@link AbstractAwsProfile} that will be used.
 *
 * @author joseross
 */
public abstract class AbstractAwsService<T extends AbstractAwsProfile> {

    /**
     * Instance of {@link SiteAwareConfigProfileLoader} used to load the configuration file.
     */
    protected SiteAwareConfigProfileLoader<T> profileLoader;

    @Required
    public void setProfileLoader(SiteAwareConfigProfileLoader<T> profileLoader) {
        this.profileLoader = profileLoader;
    }

    protected T getProfile(String site, String profileId) throws AwsException {
        try {
            return profileLoader.loadProfile(site, profileId);
        } catch (ConfigurationException e) {
            throw new AwsException("Unable to load AWS profile", e);
        }
    }

}

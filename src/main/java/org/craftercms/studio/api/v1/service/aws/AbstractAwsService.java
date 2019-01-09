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

package org.craftercms.studio.api.v1.service.aws;

import org.craftercms.studio.api.v1.aws.AwsProfile;
import org.craftercms.studio.api.v1.aws.AwsProfileReader;
import org.craftercms.studio.api.v1.exception.AwsConfigurationException;
import org.springframework.beans.factory.annotation.Required;

/**
 * Provides common profile operations used by all services.
 * @param <T> The type of {@link AwsProfile} that will be used.
 *
 * @author joseross
 */
public abstract class AbstractAwsService<T extends AwsProfile> {

    /**
     * Used to read profiles from the configuration file.
     */
    private AwsProfileManager profileManager;

    /**
     * Used to generate {@link AwsProfile} instances.
     */
    private AwsProfileReader<T> profileReader;

    @Required
    public void setProfileManager(final AwsProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    @Required
    public void setProfileReader(AwsProfileReader<T> profileReader) {
        this.profileReader = profileReader;
    }

    protected T getProfile(String site, String profileId) throws AwsConfigurationException {
        return profileReader.readProfile(profileManager.getProfile(site, profileId));
    }

}

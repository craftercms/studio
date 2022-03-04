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

package org.craftercms.studio.impl.v1.service.box;

import com.box.sdk.*;
import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.profiles.box.BoxProfile;
import org.craftercms.studio.api.v1.exception.BoxException;
import org.craftercms.studio.api.v1.service.box.BoxService;
import org.craftercms.studio.impl.v1.util.config.profiles.SiteAwareConfigProfileLoader;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@inheritDoc}
 */
public class BoxServiceImpl implements BoxService {

    protected static final String URL_FORMAT = "/remote-assets/box/%s/%s.%s";

    protected SiteAwareConfigProfileLoader<BoxProfile> profileLoader;

    @Required
    public void setProfileLoader(SiteAwareConfigProfileLoader<BoxProfile> profileLoader) {
        this.profileLoader = profileLoader;
    }

    protected BoxProfile getProfile(String site, String profileId) throws BoxException  {
        try {
            return profileLoader.loadProfile(site, profileId);
        } catch (ConfigurationException e) {
            throw new BoxException("Unable to load Box profile", e);
        }
    }

    protected BoxAPIConnection getConnection(BoxProfile profile) {
        JWTEncryptionPreferences jwtPrefs = new JWTEncryptionPreferences();
        jwtPrefs.setPublicKeyID(profile.getPublicKeyId());
        jwtPrefs.setPrivateKey(profile.getPrivateKey());
        jwtPrefs.setPrivateKeyPassword(profile.getPrivateKeyPassword());
        jwtPrefs.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);
        BoxConfig config = new BoxConfig(
            profile.getClientId(), profile.getClientSecret(), profile.getEnterpriseId(), jwtPrefs);

        return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccessToken(final String site, final String profileId) throws BoxException {
        BoxProfile profile = getProfile(site, profileId);
        BoxAPIConnection api = getConnection(profile);
        return api.getAccessToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl(final String site, final String profileId, final String fileId,
                         final String filename) throws BoxException {
        getProfile(site, profileId); // validate that the profileId exists in the site
        return String.format(URL_FORMAT, profileId, fileId, FilenameUtils.getExtension(filename));
    }

}

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

package org.craftercms.studio.impl.v1.service.box;

import com.box.sdk.*;
import org.craftercms.studio.api.v1.box.BoxProfile;
import org.craftercms.studio.api.v1.box.BoxProfileReader;
import org.craftercms.studio.api.v1.exception.BoxException;
import org.craftercms.studio.api.v1.service.box.BoxService;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@inheritDoc}
 */
public class BoxServiceImpl implements BoxService {

    /**
     * Box API file size limit, according to the documentation larger files should be uploaded in chunks.
     */
    public static final long MIN_SIZE = 20000000;

    protected BoxProfileReader profileReader;

    @Required
    public void setProfileReader(final BoxProfileReader profileReader) {
        this.profileReader = profileReader;
    }

    protected BoxProfile getProfile(String site, String profileId) throws BoxException  {
        return profileReader.getProfile(site, profileId);
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
}

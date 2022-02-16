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
package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.craftercms.studio.api.v2.service.security.internal.AccessTokenServiceInternal;
import org.craftercms.studio.model.security.AccessToken;
import org.craftercms.studio.model.security.PersistentAccessToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.List;

/**
 * Default implementation of {@link AccessTokenService}
 *
 * @author joseross
 * @since 4.0
 */
public class AccessTokenServiceImpl implements AccessTokenService {

    protected AccessTokenServiceInternal accessTokenServiceInternal;

    @ConstructorProperties({"accessTokenServiceInternal"})
    public AccessTokenServiceImpl(AccessTokenServiceInternal accessTokenServiceInternal) {
        this.accessTokenServiceInternal = accessTokenServiceInternal;
    }

    // Temporary tokens

    @Override
    public boolean hasValidRefreshToken(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        return accessTokenServiceInternal.hasValidRefreshToken(auth, request, response);
    }

    @Override
    public void updateRefreshToken(Authentication auth, HttpServletResponse response) {
        accessTokenServiceInternal.updateRefreshToken(auth, response);
    }

    @Override
    public AccessToken createTokens(Authentication auth, HttpServletResponse response) throws ServiceLayerException {
        return accessTokenServiceInternal.createTokens(auth, response);
    }

    @Override
    public void deleteRefreshToken(Authentication auth) {
        accessTokenServiceInternal.deleteRefreshToken(auth);
    }

    @Override
    public void deleteExpiredRefreshTokens() {
        accessTokenServiceInternal.deleteExpiredRefreshTokens();
    }

    // Persisten tokens

    @Override
    @HasPermission(type = DefaultPermission.class, action = "manage_access_token")
    public PersistentAccessToken createAccessToken(String label, Instant expiresAt) throws ServiceLayerException {
        return accessTokenServiceInternal.createAccessToken(label, expiresAt);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "manage_access_token")
    public List<PersistentAccessToken> getAccessTokens() {
        return accessTokenServiceInternal.getAccessTokens();
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "manage_access_token")
    public PersistentAccessToken updateAccessToken(long id, boolean enabled) {
        return accessTokenServiceInternal.updateAccessToken(id, enabled);
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "manage_access_token")
    public void deleteAccessToken(long id) {
        accessTokenServiceInternal.deleteAccessToken(id);
    }

    // All tokens

    @Override
    public String getUsername(String token) {
        return accessTokenServiceInternal.getUsername(token);
    }

    @Override
    public void updateUserActivity(Authentication authentication) {
        accessTokenServiceInternal.updateUserActivity(authentication);
    }

}

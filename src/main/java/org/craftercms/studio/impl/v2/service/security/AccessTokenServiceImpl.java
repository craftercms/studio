/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.craftercms.studio.model.security.AccessToken;
import org.craftercms.studio.model.security.PersistentAccessToken;
import org.springframework.security.core.Authentication;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_MANAGE_ACCESS_TOKEN;

/**
 * Default implementation of {@link AccessTokenService}
 *
 * @author joseross
 * @since 4.0
 */
public class AccessTokenServiceImpl implements AccessTokenService {

	protected AccessTokenService accessTokenService;

	@ConstructorProperties({"accessTokenService"})
	public AccessTokenServiceImpl(AccessTokenService accessTokenService) {
		this.accessTokenService = accessTokenService;
	}

	// Temporary tokens

	@Override
	public boolean hasValidRefreshToken(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
		return accessTokenService.hasValidRefreshToken(auth, request, response);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public void updateRefreshToken(Authentication auth, HttpServletResponse response) {
		accessTokenService.updateRefreshToken(auth, response);
	}

	@Override
	public AccessToken createTokens(Authentication auth, HttpServletRequest request, HttpServletResponse response) throws ServiceLayerException {
		return accessTokenService.createTokens(auth, request, response);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public void deleteRefreshToken(long userId) {
		accessTokenService.deleteRefreshToken(userId);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public void deleteExpiredRefreshTokens() {
		accessTokenService.deleteExpiredRefreshTokens();
	}

	// Persistent tokens

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public PersistentAccessToken createAccessToken(String label, Instant expiresAt) throws ServiceLayerException {
		return accessTokenService.createAccessToken(label, expiresAt);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public List<PersistentAccessToken> getAccessTokens() {
		return accessTokenService.getAccessTokens();
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public PersistentAccessToken updateAccessToken(long id, boolean enabled) {
		return accessTokenService.updateAccessToken(id, enabled);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public void deleteAccessToken(long id) {
		accessTokenService.deleteAccessToken(id);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_MANAGE_ACCESS_TOKEN)
	public void deleteUsersTokens(Collection<Long> userIds) {
		accessTokenService.deleteUsersTokens(userIds);
	}

	// All tokens

	@Override
	public String getUsername(String token) {
		return accessTokenService.getUsername(token);
	}

	@Override
	public void updateUserActivity(Authentication authentication) {
		accessTokenService.updateUserActivity(authentication);
	}

	@Override
	public void refreshPreviewCookie(Authentication authentication, HttpServletRequest request, HttpServletResponse response, boolean silent) throws ServiceLayerException {
		accessTokenService.refreshPreviewCookie(authentication, request, response, silent);
	}

	@Override
	public void deletePreviewCookie(HttpServletResponse response) {
		accessTokenService.deletePreviewCookie(response);
	}

}

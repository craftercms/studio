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
package org.craftercms.studio.api.v2.service.security;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.model.security.AccessToken;
import org.craftercms.studio.model.security.PersistentAccessToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;

/**
 * Defines all operations related to access and refresh tokens
 *
 * @author joseross
 * @since 4.0
 */
public interface AccessTokenService {

    // Temporary tokens

    /**
     * Checks if the given request contains a valid refresh token
     * @param auth the current authentication
     * @param request the request to check
     * @param response the response
     * @return true if the request contains a valid refresh token
     */
    boolean hasValidRefreshToken(Authentication auth, HttpServletRequest request, HttpServletResponse response);

    /**
     * Updates the refresh token for the given response
     * @param auth the current authentication
     * @param response the response
     */
    void updateRefreshToken(Authentication auth, HttpServletResponse response);

    /**
     * Creates the access & refresh tokens for the given authentication
     * @param auth the current authentication
     * @param response the response
     * @return the access token
     * @throws ServiceLayerException if there is any error creating the access token
     */
    AccessToken createTokens(Authentication auth, HttpServletResponse response) throws ServiceLayerException;

    /**
     * Deletes the refresh token for the given authentication
     * @param auth the current authentication
     */
    void deleteRefreshToken(Authentication auth);

    /**
     * Deletes all expired refresh tokens
     */
    void deleteExpiredRefreshTokens();

    // Persistent tokens

    /**
     * Creates a new access token for the current user
     * @param label the label of the access token
     * @param expiresOn the date of expiration of the access token
     * @return the access token
     * @throws ServiceLayerException if there is any error creating the access token
     */
    PersistentAccessToken createAccessToken(String label, Instant expiresOn) throws ServiceLayerException;

    /**
     * Get all existing access tokens for the current user
     * @return the list of access tokens
     */
    List<PersistentAccessToken> getAccessTokens();

    /**
     * Updates an access token for the current user
     * @param id the id of the access token
     * @param enabled indicates if the token is enabled or not
     * @return the updated access token
     */
    PersistentAccessToken updateAccessToken(long id, boolean enabled);

    /**
     * Deletes an access token for the current user
     * @param id the id of the access token
     */
    void deleteAccessToken(long id);

    // All tokens

    /**
     * Returns the username for the given access token
     * @param token the access token
     * @return the username, null if the access token is invalid
     */
    String getUsername(String token);

    /**
     * Updates the user activity record to extend the timeout
     * @param authentication the current authentication
     */
    void updateUserActivity(Authentication authentication);

}

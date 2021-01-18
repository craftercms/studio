/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.service.security.internal;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.SecurityDAO;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.AccessTokenServiceInternal;
import org.craftercms.studio.api.v2.service.system.InstanceService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.security.AccessToken;
import org.craftercms.studio.model.security.PersistentAccessToken;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.keys.PbkdfKey;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.lang.Long.parseLong;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_LOGIN_FAILED;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_ACCESS_TOKEN;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REFRESH_TOKEN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.springframework.web.util.WebUtils.getCookie;

/**
 * Default implementation of {@link AccessTokenServiceInternal}
 *
 * @author joseross
 * @since 4.0
 */
public class AccessTokenServiceInternalImpl extends CookieGenerator
        implements AccessTokenServiceInternal, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AccessTokenServiceInternalImpl.class);

    /**
     * The issuer for generation access tokens
     */
    protected String issuer;

    /**
     * List of accepted issuers for validation of access tokens
     */
    protected String[] validIssuers;

    /**
     * The audience for generation and validation of access tokens
     */
    protected String audience;

    /**
     * The time in minutes for the expiration of the generated access tokens
     */
    protected int accessTokenExpiration;

    /**
     * The password for signing the access tokens
     */
    protected String signPassword;

    /**
     * The password for encrypting the access tokens
     */
    protected String encryptPassword;

    /**
     * The time in minutes for the expiration of the refresh tokens
     */
    protected int refreshTokenExpiration;

    protected Key jwtSignKey;
    protected Key jwtEncryptKey;

    protected SecurityDAO securityDao;
    protected InstanceService instanceService;
    protected AuditServiceInternal auditService;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;

    public AccessTokenServiceInternalImpl(String issuer, String[] validIssuers, int accessTokenExpiration,
                                          String signPassword, String encryptPassword, int refreshTokenExpiration,
                                          SecurityDAO securityDao, InstanceService instanceService,
                                          AuditServiceInternal auditService, StudioConfiguration studioConfiguration,
                                          SiteService siteService) {
        this.issuer = issuer;
        this.validIssuers = validIssuers;
        this.accessTokenExpiration = accessTokenExpiration;
        this.signPassword = signPassword;
        this.encryptPassword = encryptPassword;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.securityDao = securityDao;
        this.instanceService = instanceService;
        this.auditService = auditService;
        this.studioConfiguration = studioConfiguration;
        this.siteService = siteService;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    @Override
    public void afterPropertiesSet() {
        jwtSignKey = new HmacKey(signPassword.getBytes(StandardCharsets.UTF_8));
        jwtEncryptKey = new PbkdfKey(encryptPassword);
        setCookieHttpOnly(true); // Always HTTPOnly to protect the refresh token
    }

    @Override
    public boolean hasValidRefreshToken(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        var cookie = getCookie(request, getCookieName());
        var refreshToken = cookie != null? cookie.getValue() : null;
        var userId = getUserId(auth);

        if (isNotEmpty(refreshToken)) {
            if (securityDao.validateRefreshToken(userId, refreshToken)) {
                return true;
            } else {
                // when an expired token is detected, clear the cookie & the auth
                SecurityContextHolder.clearContext();
                request.getSession().invalidate();
                response.addCookie(createCookie(StringUtils.EMPTY));
            }
        }

        return false;
    }

    @Override
    public void updateRefreshToken(Authentication auth, HttpServletResponse response) {
        var refreshToken = UUID.randomUUID().toString();
        var userId = getUserId(auth);

        securityDao.upsertRefreshToken(userId, refreshToken);

        response.addCookie(createCookie(refreshToken));
    }

    @Override
    public AccessToken createTokens(Authentication auth, HttpServletResponse response) throws ServiceLayerException {
        var issuedAt = Instant.now();
        var expireAt = issuedAt.plus(accessTokenExpiration, MINUTES);

        String token = createToken(issuedAt, expireAt, auth.getName(), null);

        updateRefreshToken(auth, response);

        var accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setExpiresAt(expireAt);

        // The userId is not really useful, but the db requires a value
        createAuditLog(auth, getUserId(auth), TARGET_TYPE_REFRESH_TOKEN, OPERATION_CREATE);

        return accessToken;
    }

    @Override
    public void deleteRefreshToken(Authentication auth) {
        var userId = getUserId(auth);
        securityDao.deleteRefreshToken(userId);

        // The userId is not really useful, but the db requires a value
        createAuditLog(auth, userId, TARGET_TYPE_REFRESH_TOKEN, OPERATION_DELETE);
    }

    @Override
    public void deleteRefreshToken(User user) {
        logger.debug("Triggering re-authentication for user {0}", user.getUsername());
        securityDao.deleteRefreshToken(user.getId());
    }

    @Override
    public void deleteExpiredRefreshTokens() {
        securityDao.deleteExpiredTokens(refreshTokenExpiration);
    }

    @Override
    public PersistentAccessToken createAccessToken(String label, Instant expiresAt) throws ServiceLayerException {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        var token = new PersistentAccessToken();
        token.setLabel(label);
        token.setExpiresAt(expiresAt);

        securityDao.createAccessToken(getUserId(auth), token);

        token.setToken(createToken(Instant.now(), expiresAt, auth.getName(), token.getId()));

        createAuditLog(auth, token.getId(), TARGET_TYPE_ACCESS_TOKEN, OPERATION_CREATE);

        return token;
    }

    @Override
    public List<PersistentAccessToken> getAccessTokens() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        return securityDao.getAccessTokens(getUserId(auth));
    }

    @Override
    public PersistentAccessToken updateAccessToken(long tokenId, boolean enabled) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var userId = getUserId(auth);

        securityDao.updateAccessToken(userId, tokenId, enabled);

        createAuditLog(auth, tokenId, TARGET_TYPE_ACCESS_TOKEN, OPERATION_UPDATE);

        return securityDao.getAccessTokenByUserIdAndTokenId(userId, tokenId);
    }

    @Override
    public void deleteAccessToken(long tokenId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        securityDao.deleteAccessToken(getUserId(auth), tokenId);

        createAuditLog(auth, tokenId, TARGET_TYPE_ACCESS_TOKEN, OPERATION_DELETE);
    }

    protected String getActualAudience() {
        return isNotEmpty(audience)? audience : instanceService.getInstanceId();
    }

    @Override
    public String getUsername(String token) {
        try {
            var jwtConsumer = new JwtConsumerBuilder()
                    .setEnableRequireEncryption()
                    .setRequireSubject()
                    .setExpectedIssuers(true, validIssuers)
                    .setExpectedAudience(getActualAudience())
                    .setRelaxVerificationKeyValidation() // TODO: Check if this is ok
                    .setVerificationKey(jwtSignKey)
                    .setDecryptionKey(jwtEncryptKey)
                    .build();

            var claims = jwtConsumer.processToClaims(token);

            var username = claims.getSubject();
            var jwtId = claims.getJwtId();
            if (isNotEmpty(jwtId)) {
                var tokenId = parseLong(jwtId);
                var storedToken = securityDao.getAccessTokenById(tokenId);
                if (storedToken == null) {
                    // someone is trying to use a deleted token!
                    logger.debug("Detected usage of deleted JWT with id {0} for user {1}", tokenId, username);
                    createAuditLog(username, tokenId, TARGET_TYPE_ACCESS_TOKEN, OPERATION_LOGIN_FAILED);
                    return null;
                }
                if (!storedToken.isEnabled()) {
                    // someone is trying to use a disabled token!
                    logger.debug("Detected usage of disabled JWT with id {0} for user {1}", tokenId, username);
                    createAuditLog(username, tokenId, TARGET_TYPE_ACCESS_TOKEN, OPERATION_LOGIN_FAILED);
                    return null;
                }

                logger.debug("Successfully validated JWT with id {0} for user {1}", tokenId, username);
                createAuditLog(username, tokenId, TARGET_TYPE_ACCESS_TOKEN, OPERATION_LOGIN);
            } else {
                logger.debug("Successfully validated JWT with for user {0}", username);
            }

            // Return the user
            return username;
        } catch (InvalidJwtException | MalformedClaimException e) {
            // someone is trying to use an invalid token!
            logger.debug("Detected usage of invalid JWT {0}", token);
            logger.error("Detected usage of invalid JWT", e);
            createAuditLog("JWT", -1, TARGET_TYPE_ACCESS_TOKEN, token, OPERATION_LOGIN_FAILED);
        }
        return null;
    }

    protected long getUserId(Authentication auth) {
        return ((User) auth.getPrincipal()).getId();
    }

    protected String createToken(Instant issuedAt, Instant expiresAt, String username, Long id)
            throws ServiceLayerException {
        var claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setIssuedAt(NumericDate.fromMilliseconds(issuedAt.toEpochMilli()));
        claims.setSubject(username);
        claims.setAudience(getActualAudience());
        if (expiresAt != null) {
            claims.setExpirationTime(NumericDate.fromMilliseconds(expiresAt.toEpochMilli()));
        }
        if (id != null) {
            claims.setJwtId(id.toString());
        }

        try {
            // Sign the JWT
            var jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(jwtSignKey);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512); // TODO: Choose an algorithm
            jws.setDoKeyValidation(false); // TODO: Check if this is ok

            // Encrypt the JWS
            var jwe = new JsonWebEncryption();
            jwe.setPayload(jws.getCompactSerialization());
            // TODO: Choose algorithms
            jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.PBES2_HS512_A256KW);
            jwe.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_256_CBC_HMAC_SHA_512);
            jwe.setKey(jwtEncryptKey);
            jwe.setContentTypeHeaderValue("JWT");

            return jwe.getCompactSerialization();
        } catch (JoseException e) {
            throw new ServiceLayerException("Error generating JWT for user " + username, e);
        }
    }

    protected void createAuditLog(Authentication auth, long tokenId, String type, String operation) {
        createAuditLog(auth.getName(), tokenId, type, operation);
    }

    protected void createAuditLog(String actor, long tokenId, String type, String operation) {
        createAuditLog(actor, tokenId, type, Long.toString(tokenId), operation);
    }

    protected void createAuditLog(String actor, long tokenId, String type, String value, String operation) {
        try {
            var site = siteService.getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
            var entry = auditService.createAuditLogEntry();
            entry.setOperation(operation);
            entry.setActorId(actor);
            entry.setSiteId(site.getId());
            entry.setPrimaryTargetId(Long.toString(tokenId));
            entry.setPrimaryTargetType(type);
            entry.setPrimaryTargetValue(value);
            auditService.insertAuditLog(entry);
        } catch (SiteNotFoundException e) {
            // should never happen
        }
    }

}

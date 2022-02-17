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

package org.craftercms.studio.controller.rest.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.security.AccessTokenService;
import org.craftercms.studio.api.v2.service.security.EncryptionService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.security.EncryptRequest;
import org.craftercms.studio.model.security.PersistentAccessToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.beans.ConstructorProperties;
import java.time.Instant;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEM;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_TOKEN;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_TOKENS;

/**
 * Rest controller that provides access to security operations
 *
 * @author joseross
 * @since 3.1.5
 */
@RestController
@RequestMapping("/api/2/security")
public class SecurityController {

    protected EncryptionService encryptionService;

    protected AccessTokenService accessTokenService;

    @ConstructorProperties({"encryptionService", "accessTokenService"})
    public SecurityController(EncryptionService encryptionService, AccessTokenService accessTokenService) {
        this.encryptionService = encryptionService;
        this.accessTokenService = accessTokenService;
    }

    @PostMapping("/encrypt")
    public ResponseBody encryptText(@RequestBody EncryptRequest request) throws ServiceLayerException {
        String encrypted = encryptionService.encrypt(request.getSiteId(), request.getText());

        ResultOne<String> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_ITEM, encrypted);
        result.setResponse(ApiResponse.OK);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

    @GetMapping("/tokens")
    public ResponseBody getAccessTokens() {
        var result = new ResultList<PersistentAccessToken>();
        result.setEntities(RESULT_KEY_TOKENS, accessTokenService.getAccessTokens());
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);
        return response;
    }

    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseBody createAccessToken(@Valid @RequestBody CreateAccessTokenRequest request) throws ServiceLayerException {
        var result = new ResultOne<PersistentAccessToken>();
        result.setEntity(RESULT_KEY_TOKEN, accessTokenService.createAccessToken(request.getLabel(), request.getExpiresAt()));
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);
        return response;
    }

    @PostMapping("/tokens/{tokenId}")
    public ResponseBody updateAccessToken(@PathVariable long tokenId, @Valid @RequestBody UpdateAccessTokenRequest request) {
        var result = new ResultOne<PersistentAccessToken>();
        result.setEntity(RESULT_KEY_TOKEN, accessTokenService.updateAccessToken(tokenId, request.isEnabled()));
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);
        return response;
    }

    @DeleteMapping("/tokens/{tokenId}")
    public ResponseBody deleteAccessToken(@PathVariable long tokenId) {
        accessTokenService.deleteAccessToken(tokenId);

        var result = new Result();
        result.setResponse(ApiResponse.OK);

        var response = new ResponseBody();
        response.setResult(result);
        return response;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateAccessTokenRequest {

        @NotEmpty
        protected String label;

        protected Instant expiresAt;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateAccessTokenRequest {

        protected boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

    }

}

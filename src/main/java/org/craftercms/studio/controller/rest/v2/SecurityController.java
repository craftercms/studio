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

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.security.EncryptionService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultOne;
import org.craftercms.studio.model.rest.security.EncryptRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEM;

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

    public SecurityController(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PostMapping("/encrypt")
    public ResponseBody encryptText(@RequestBody EncryptRequest request) throws ServiceLayerException {
        String encrypted = encryptionService.encrypt(request.getText());

        ResultOne<String> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_ITEM, encrypted);
        result.setResponse(ApiResponse.OK);

        ResponseBody response = new ResponseBody();
        response.setResult(result);

        return response;
    }

}

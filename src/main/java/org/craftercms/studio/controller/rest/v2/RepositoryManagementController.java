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
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static org.craftercms.studio.model.rest.ApiResponse.CREATED;

public class RepositoryManagementController {

    private RepositoryManagementService repositoryManagementService;

    @PostMapping("/api/2/repository/add_remote")
    public ResponseBody addRemote(@RequestBody RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException {

        repositoryManagementService.addRemote(remoteRepository.getSiteId(), remoteRepository);

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(CREATED);
        responseBody.setResult(result);
        return responseBody;
    }

    public RepositoryManagementService getRepositoryManagementService() {
        return repositoryManagementService;
    }

    public void setRepositoryManagementService(RepositoryManagementService repositoryManagementService) {
        this.repositoryManagementService = repositoryManagementService;
    }
}

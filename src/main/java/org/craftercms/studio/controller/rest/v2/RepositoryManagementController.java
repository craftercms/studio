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

import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.model.rest.PullFromRemoteRequest;
import org.craftercms.studio.model.rest.PushToRemoteRequest;
import org.craftercms.studio.model.rest.RebuildDatabaseRequest;
import org.craftercms.studio.model.rest.RemoveRemoteRequest;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.Result;
import org.craftercms.studio.model.rest.ResultList;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_REMOTES;
import static org.craftercms.studio.model.rest.ApiResponse.CREATED;
import static org.craftercms.studio.model.rest.ApiResponse.INTERNAL_SYSTEM_FAILURE;
import static org.craftercms.studio.model.rest.ApiResponse.OK;

@RestController
public class RepositoryManagementController {

    private RepositoryManagementService repositoryManagementService;
    private SiteService siteService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/2/repository/add_remote")
    public ResponseBody addRemote(@RequestBody RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException {

        if (!siteService.exists(remoteRepository.getSiteId())) {
            throw new SiteNotFoundException(remoteRepository.getSiteId());
        }

        boolean res = repositoryManagementService.addRemote(remoteRepository.getSiteId(), remoteRepository);

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        if (res) {
            result.setResponse(CREATED);
        } else {
            result.setResponse(INTERNAL_SYSTEM_FAILURE);
        }
        responseBody.setResult(result);
        return responseBody;
    }

    @GetMapping("/api/2/repository/list_remotes")
    public ResponseBody listRemotes(@RequestParam(name = "siteId", required = true) String siteId)
            throws ServiceLayerException, CryptoException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        List<RemoteRepositoryInfo> remotes = repositoryManagementService.listRemotes(siteId);

        ResponseBody responseBody = new ResponseBody();
        ResultList<RemoteRepositoryInfo> result = new ResultList<RemoteRepositoryInfo>();
        result.setEntities(RESULT_KEY_REMOTES, remotes);
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/repository/pull_from_remote")
    public ResponseBody pullFromRemote(@RequestBody PullFromRemoteRequest pullFromRemoteRequest)
            throws InvalidRemoteUrlException, ServiceLayerException, CryptoException {
        if (!siteService.exists(pullFromRemoteRequest.getSiteId())) {
            throw new SiteNotFoundException(pullFromRemoteRequest.getSiteId());
        }
        boolean res = repositoryManagementService.pullFromRemote(pullFromRemoteRequest.getSiteId(),
                pullFromRemoteRequest.getRemoteName(), pullFromRemoteRequest.getRemoteBranch(),
                pullFromRemoteRequest.getMergeStrategy());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        if (res) {
            result.setResponse(OK);
        } else {
            result.setResponse(INTERNAL_SYSTEM_FAILURE);
        }
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/repository/push_to_remote")
    public ResponseBody pushToRemote(@RequestBody PushToRemoteRequest pushToRemoteRequest)
            throws InvalidRemoteUrlException, CryptoException, ServiceLayerException {
        if (!siteService.exists(pushToRemoteRequest.getSiteId())) {
            throw new SiteNotFoundException(pushToRemoteRequest.getSiteId());
        }
        boolean res = repositoryManagementService.pushToRemote(pushToRemoteRequest.getSiteId(),
                pushToRemoteRequest.getRemoteName(), pushToRemoteRequest.getRemoteBranch(),
                pushToRemoteRequest.isForce());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        if (res) {
            result.setResponse(OK);
        } else {
            result.setResponse(INTERNAL_SYSTEM_FAILURE);
        }
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/repository/rebuild_database")
    public ResponseBody rebuildDatabase(@RequestBody RebuildDatabaseRequest rebuildDatabaseRequest)
            throws SiteNotFoundException {
        if (!siteService.exists(rebuildDatabaseRequest.getSiteId())) {
            throw new SiteNotFoundException(rebuildDatabaseRequest.getSiteId());
        }
        repositoryManagementService.rebuildDatabase(rebuildDatabaseRequest.getSiteId());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        result.setResponse(OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping("/api/2/repository/remove_remote")
    public ResponseBody removeRemote(@RequestBody RemoveRemoteRequest removeRemoteRequest)
            throws CryptoException, SiteNotFoundException {
        if (!siteService.exists(removeRemoteRequest.getSiteId())) {
            throw new SiteNotFoundException(removeRemoteRequest.getSiteId());
        }
        boolean res = repositoryManagementService.removeRemote(removeRemoteRequest.getSiteId(),
                removeRemoteRequest.getRemoteName());

        ResponseBody responseBody = new ResponseBody();
        Result result = new Result();
        if (res) {
            result.setResponse(OK);
        } else {
            result.setResponse(INTERNAL_SYSTEM_FAILURE);
        }
        responseBody.setResult(result);
        return responseBody;
    }

    public RepositoryManagementService getRepositoryManagementService() {
        return repositoryManagementService;
    }

    public void setRepositoryManagementService(RepositoryManagementService repositoryManagementService) {
        this.repositoryManagementService = repositoryManagementService;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}

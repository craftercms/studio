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

import org.craftercms.commons.validation.annotations.param.ValidExistingContentPath;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.repository.RemoteNotRemovableException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v2.dal.DiffConflictedFile;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.dal.RemoteRepositoryInfo;
import org.craftercms.studio.api.v2.dal.RepositoryStatus;
import org.craftercms.studio.api.v2.service.repository.MergeResult;
import org.craftercms.studio.api.v2.service.repository.RepositoryManagementService;
import org.craftercms.studio.model.rest.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_PATH;
import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.RequestMappingConstants.*;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.*;
import static org.craftercms.studio.model.rest.ApiResponse.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping(API_2 + REPOSITORY)
public class RepositoryManagementController {

    private final RepositoryManagementService repositoryManagementService;

    @ConstructorProperties({"repositoryManagementService"})
    public RepositoryManagementController(final RepositoryManagementService repositoryManagementService) {
        this.repositoryManagementService = repositoryManagementService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(ADD_REMOTE)
    public Result addRemote(HttpServletResponse response, @Valid @RequestBody RemoteRepository remoteRepository)
            throws ServiceLayerException, InvalidRemoteUrlException, RemoteRepositoryNotFoundException {
        boolean res = repositoryManagementService.addRemote(remoteRepository.getSiteId(), remoteRepository);

        Result result = new Result();
        if (res) {
            result.setResponse(CREATED);
        } else {
            result.setResponse(ADD_REMOTE_INVALID);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return result;
    }

    @GetMapping(value = LIST_REMOTES, produces = APPLICATION_JSON_VALUE)
    public ResultList<RemoteRepositoryInfo> listRemotes(@ValidSiteId @RequestParam(name = "siteId") String siteId)
            throws ServiceLayerException {
        List<RemoteRepositoryInfo> remotes = repositoryManagementService.listRemotes(siteId);

        ResultList<RemoteRepositoryInfo> result = new ResultList<>();
        result.setEntities(RESULT_KEY_REMOTES, remotes);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(PULL_FROM_REMOTE)
    public ResultOne<MergeResult> pullFromRemote(@Valid @RequestBody PullFromRemoteRequest pullFromRemoteRequest)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        MergeResult mergeResult = repositoryManagementService.pullFromRemote(pullFromRemoteRequest.getSiteId(),
                pullFromRemoteRequest.getRemoteName(), pullFromRemoteRequest.getRemoteBranch(),
                pullFromRemoteRequest.getMergeStrategy());

        ResultOne<MergeResult> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_RESULT, mergeResult);
        return result;
    }

    @PostMapping(PUSH_TO_REMOTE)
    public Result pushToRemote(HttpServletResponse response, @Valid @RequestBody PushToRemoteRequest pushToRemoteRequest)
            throws InvalidRemoteUrlException, ServiceLayerException,
            InvalidRemoteRepositoryCredentialsException, RemoteRepositoryNotFoundException {
        boolean res = repositoryManagementService.pushToRemote(pushToRemoteRequest.getSiteId(),
                pushToRemoteRequest.getRemoteName(), pushToRemoteRequest.getRemoteBranch(),
                pushToRemoteRequest.isForce());

        Result result = new Result();
        if (res) {
            result.setResponse(OK);
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setResponse(PUSH_TO_REMOTE_FAILED);
        }
        return result;
    }

    @PostMapping(REMOVE_REMOTE)
    public Result removeRemote(HttpServletResponse response, @Valid @RequestBody RemoveRemoteRequest removeRemoteRequest)
            throws SiteNotFoundException, RemoteNotRemovableException {
        boolean res = repositoryManagementService.removeRemote(removeRemoteRequest.getSiteId(),
                removeRemoteRequest.getRemoteName());

        Result result = new Result();
        if (res) {
            result.setResponse(OK);
        } else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            result.setResponse(REMOVE_REMOTE_FAILED);
        }
        return result;
    }

    @GetMapping(STATUS)
    public ResultOne<RepositoryStatus> getRepositoryStatus(@ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId)
            throws ServiceLayerException {
        RepositoryStatus status = repositoryManagementService.getRepositoryStatus(siteId);
        ResultOne<RepositoryStatus> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_REPOSITORY_STATUS, status);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(RESOLVE_CONFLICT)
    public ResultOne<RepositoryStatus> resolveConflict(@Valid @RequestBody ResolveConflictRequest resolveConflictRequest)
            throws ServiceLayerException {
        String path = resolveConflictRequest.getPath();
        if (!path.startsWith(FILE_SEPARATOR)) {
            path = FILE_SEPARATOR + path;
        }
        RepositoryStatus status = repositoryManagementService.resolveConflict(resolveConflictRequest.getSiteId(),
                path, resolveConflictRequest.getResolution());
        ResultOne<RepositoryStatus> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_REPOSITORY_STATUS, status);
        return result;
    }

    @GetMapping(DIFF_CONFLICTED_FILE)
    public ResultOne<DiffConflictedFile> getDiffForConflictedFile(@ValidSiteId @RequestParam(value = REQUEST_PARAM_SITEID) String siteId,
                                                                  @ValidExistingContentPath @RequestParam(value = REQUEST_PARAM_PATH) String path)
            throws ServiceLayerException {
        String diffPath = path;
        if (!diffPath.startsWith(FILE_SEPARATOR)) {
            diffPath = FILE_SEPARATOR + diffPath;
        }
        DiffConflictedFile diff = repositoryManagementService.getDiffForConflictedFile(siteId, diffPath);
        ResultOne<DiffConflictedFile> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_DIFF, diff);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(COMMIT_RESOLUTION)
    public ResultOne<RepositoryStatus> commitConflictResolution(@Valid @RequestBody CommitResolutionRequest commitResolutionRequest)
            throws ServiceLayerException {
        RepositoryStatus status = repositoryManagementService.commitResolution(commitResolutionRequest.getSiteId(),
                commitResolutionRequest.getCommitMessage());
        ResultOne<RepositoryStatus> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_REPOSITORY_STATUS, status);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(CANCEL_FAILED_PULL)
    public ResultOne<RepositoryStatus> cancelFailedPull(@Valid @RequestBody CancelFailedPullRequest cancelFailedPullRequest)
            throws ServiceLayerException {
        RepositoryStatus status = repositoryManagementService.cancelFailedPull(cancelFailedPullRequest.getSiteId());
        ResultOne<RepositoryStatus> result = new ResultOne<>();
        result.setEntity(RESULT_KEY_REPOSITORY_STATUS, status);
        result.setResponse(OK);
        return result;
    }

    @PostMapping(UNLOCK)
    public Result unlockRepository(@Valid @RequestBody UnlockRepositoryRequest unlockRepositoryRequest) throws SiteNotFoundException {
        boolean success = repositoryManagementService.unlockRepository(unlockRepositoryRequest.getSiteId(),
                unlockRepositoryRequest.getRepositoryType());
        Result result = new Result();
        if (success) {
            result.setResponse(OK);
        } else {
            result.setResponse(INTERNAL_SYSTEM_FAILURE);
        }
        return result;
    }

    @GetMapping(CORRUPTED)
    public ResultOne<Boolean> isRepositoryCorrupted(@ValidSiteId @RequestParam(required = false) String siteId,
                                                    @RequestParam GitRepositories repositoryType)
            throws ServiceLayerException {
        ResultOne<Boolean> result = new ResultOne<>();
        result.setResponse(OK);
        result.setEntity(RESULT_KEY_CORRUPTED,
                repositoryManagementService.isCorrupted(siteId, repositoryType));
        return result;
    }

    @PostMapping(REPAIR)
    public Result repairCorruptedRepository(@Valid @RequestBody RepairRepositoryRequest request)
            throws ServiceLayerException {
        repositoryManagementService.repairCorrupted(request.getSiteId(), request.getRepositoryType());

        Result result = new Result();
        result.setResponse(OK);

        return result;
    }

}

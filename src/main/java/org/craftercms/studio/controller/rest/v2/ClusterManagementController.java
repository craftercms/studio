/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.controller.rest.v2;

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.exception.ClusterMemberAlreadyExistsException;
import org.craftercms.studio.api.v2.exception.ClusterMemberNotFoundException;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CLUSTER_MEMBER;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CLUSTER_MEMBERS;

@RestController
public class ClusterManagementController {

    private ClusterManagementService clusterManagementService;

    @GetMapping("/api/2/cluster")
    public ResponseBody getAllMembers() throws ServiceLayerException {
        List<ClusterMember> clusterMembers = clusterManagementService.getAllMemebers();

        ResponseBody responseBody = new ResponseBody();
        ResultList<ClusterMember> result = new ResultList<ClusterMember>();
        result.setEntities(RESULT_KEY_CLUSTER_MEMBERS, clusterMembers);
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);
        return responseBody;
    }

    @PostMapping(value = "/api/2/cluster", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseBody addClusterMember(@RequestBody ClusterMember member)
            throws ServiceLayerException, ClusterMemberAlreadyExistsException {
        ClusterMember clusterMember = clusterManagementService.addMember(member);

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setEntity(RESULT_KEY_CLUSTER_MEMBER, member);
        result.setResponse(ApiResponse.CREATED);
        responseBody.setResult(result);

        return responseBody;
    }

    @PatchMapping("/api/2/cluster")
    public ResponseBody updateClusterMember(@RequestBody ClusterMember member)
            throws ServiceLayerException, ClusterMemberNotFoundException {
        ClusterMember clusterMember = clusterManagementService.updateMember(member);

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.OK);
        result.setEntity(RESULT_KEY_CLUSTER_MEMBER, clusterMember);
        responseBody.setResult(result);
        return responseBody;
    }

    @DeleteMapping("/api/2/cluster")
    public ResponseBody removeClusterMembers(@RequestParam(value = "id") List<Long> memberIds)
            throws ServiceLayerException {
        boolean success = clusterManagementService.removeMembers(memberIds);

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.DELETED);
        return responseBody;
    }

    public ClusterManagementService getClusterManagementService() {
        return clusterManagementService;
    }

    public void setClusterManagementService(ClusterManagementService clusterManagementService) {
        this.clusterManagementService = clusterManagementService;
    }
}

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

import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CLUSTER_MEMBERS;

@RestController
public class ClusterManagementController {

    private ClusterManagementService clusterManagementService;

    @GetMapping("/api/2/cluster")
    public ResponseBody getAllMembers() throws ServiceLayerException {
        List<ClusterMember> clusterMembers = clusterManagementService.getAllMembers();

        ResponseBody responseBody = new ResponseBody();
        ResultList<ClusterMember> result = new ResultList<ClusterMember>();
        result.setEntities(RESULT_KEY_CLUSTER_MEMBERS, clusterMembers);
        result.setResponse(ApiResponse.OK);
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

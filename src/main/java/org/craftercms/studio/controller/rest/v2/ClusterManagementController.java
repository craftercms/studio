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

package org.craftercms.studio.controller.rest.v2;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.exceptions.InvalidManagementTokenException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.ClusterMember;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.cluster.ClusterManagementService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResponseBody;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_MANAGEMENT_AUTHORIZATION_TOKEN;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_CLUSTER_MEMBERS;

@RestController
public class ClusterManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterManagementController.class);

    private ClusterManagementService clusterManagementService;
    private StudioConfiguration studioConfiguration;
    private SecurityService securityService;

    protected void validateToken(String token) throws InvalidManagementTokenException, InvalidParametersException {
        if (Objects.isNull(token)) {
            throw new InvalidParametersException("Missing parameter: 'token'");
        } else if(!StringUtils.equals(token, getConfiguredToken())) {
            throw new InvalidManagementTokenException("Management authorization failed, invalid token.");
        }
    }

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

    @DeleteMapping("/api/2/cluster")
    public ResponseBody removeClusterMembers(@RequestParam(value = "id") List<Long> memberIds)
            throws ServiceLayerException {
        boolean success = clusterManagementService.removeMembers(memberIds);

        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.DELETED);
        return responseBody;
    }

    @GetMapping("/api/2/cluster/setPrimary")
    public ResponseBody setClusterPrimary(@RequestParam(name = "token") String token) {
        ResponseBody responseBody = new ResponseBody();
        ResultOne result = new ResultOne();
        result.setResponse(ApiResponse.OK);
        responseBody.setResult(result);

        try {
            validateToken(token);
            clusterManagementService.setClusterPrimary();
        } catch (Exception e) {
            logger.error("Error while setting primary node", e);
        } finally {
            return responseBody;
        }
    }

    protected String getConfiguredToken() {
        return studioConfiguration.getProperty(CONFIGURATION_MANAGEMENT_AUTHORIZATION_TOKEN);
    }

    public ClusterManagementService getClusterManagementService() {
        return clusterManagementService;
    }

    public void setClusterManagementService(ClusterManagementService clusterManagementService) {
        this.clusterManagementService = clusterManagementService;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}

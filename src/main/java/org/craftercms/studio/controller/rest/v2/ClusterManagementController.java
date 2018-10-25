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
import org.craftercms.studio.model.rest.ClusterMember;
import org.craftercms.studio.model.rest.ResponseBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterManagementController {

    @GetMapping("/api/2/cluster")
    public ResponseBody getAllMembers() throws ServiceLayerException {
        throw new ServiceLayerException("Not implemented");
    }

    @PostMapping("/api/2/cluster")
    public ResponseBody addClusterMember(@RequestBody ClusterMember member) throws ServiceLayerException {
        throw new ServiceLayerException("Not implemented");
    }

    @PatchMapping("/api/2/cluster")
    public ResponseBody updateClusterMember(@RequestBody ClusterMember member) throws ServiceLayerException {
        throw new ServiceLayerException("Not implemented");
    }

    @DeleteMapping("/api/2/cluster")
    public ResponseBody removeClusterMembers(@RequestParam(value = "id") List<Long> memberIds) throws ServiceLayerException {
        throw new ServiceLayerException("Not implmented");
    }
}

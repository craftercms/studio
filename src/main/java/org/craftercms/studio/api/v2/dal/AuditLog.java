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

package org.craftercms.studio.api.v2.dal;

import java.time.ZonedDateTime;

public class AuditLog {
    private long id;
    private long organizationId;
    private long siteId;
    private String operation;
    private ZonedDateTime operationTimestamp;
    private String origin;
    private String primaryTargetId;
    private String primaryTargetType;
    private String primaryTargetSubtype;
    private String primaryTargetValue;
    private String actorId;
    private String actorDetails;
    private String clusterNodeId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public long getSiteId() {
        return siteId;
    }

    public void setSiteId(long siteId) {
        this.siteId = siteId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public ZonedDateTime getOperationTimestamp() {
        return operationTimestamp;
    }

    public void setOperationTimestamp(ZonedDateTime operationTimestamp) {
        this.operationTimestamp = operationTimestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPrimaryTargetId() {
        return primaryTargetId;
    }

    public void setPrimaryTargetId(String primaryTargetId) {
        this.primaryTargetId = primaryTargetId;
    }

    public String getPrimaryTargetType() {
        return primaryTargetType;
    }

    public void setPrimaryTargetType(String primaryTargetType) {
        this.primaryTargetType = primaryTargetType;
    }

    public String getPrimaryTargetSubtype() {
        return primaryTargetSubtype;
    }

    public void setPrimaryTargetSubtype(String primaryTargetSubtype) {
        this.primaryTargetSubtype = primaryTargetSubtype;
    }

    public String getPrimaryTargetValue() {
        return primaryTargetValue;
    }

    public void setPrimaryTargetValue(String primaryTargetValue) {
        this.primaryTargetValue = primaryTargetValue;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorDetails() {
        return actorDetails;
    }

    public void setActorDetails(String actorDetails) {
        this.actorDetails = actorDetails;
    }

    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }
}

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

package org.craftercms.studio.model.rest.dashboard;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.ZonedDateTime;
import java.util.List;

public class AuditDashboardFilters {

    private String actor;
    private List<String> operations;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime operationTimestampFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime operationTimestampTo;
    private String target;

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    public ZonedDateTime getOperationTimestampFrom() {
        return operationTimestampFrom;
    }

    public void setOperationTimestampFrom(ZonedDateTime operationTimestampFrom) {
        this.operationTimestampFrom = operationTimestampFrom;
    }

    public ZonedDateTime getOperationTimestampTo() {
        return operationTimestampTo;
    }

    public void setOperationTimestampTo(ZonedDateTime operationTimestampTo) {
        this.operationTimestampTo = operationTimestampTo;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}

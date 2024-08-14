/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.model.rest.publish;

import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.model.rest.Person;

import java.time.Instant;

/**
 * Contains {@link PublishPackage} information to be returned by rest APIs
 */
public class PublishPackageResponse {
    private final long id;
    private final Instant submittedOn;
    private final String submitterComment;
    private final String target;
    private final PublishPackage.ApprovalState approvalState;
    private final long packageState;
    private final Instant schedule;
    private final Person submitter;

    public PublishPackageResponse(final PublishPackage publishPackage) {
        this.approvalState = publishPackage.getApprovalState();
        this.id = publishPackage.getId();
        this.packageState = publishPackage.getPackageState();
        this.submittedOn = publishPackage.getSubmittedOn();
        this.submitterComment = publishPackage.getSubmitterComment();
        this.target = publishPackage.getTarget();
        this.schedule = publishPackage.getSchedule();
        this.submitter = publishPackage.getSubmitter();
    }

    @SuppressWarnings("unused")
    public PublishPackage.ApprovalState getApprovalState() {
        return approvalState;
    }

    @SuppressWarnings("unused")
    public long getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public long getPackageState() {
        return packageState;
    }

    @SuppressWarnings("unused")
    public Instant getSubmittedOn() {
        return submittedOn;
    }

    @SuppressWarnings("unused")
    public String getSubmitterComment() {
        return submitterComment;
    }

    @SuppressWarnings("unused")
    public String getTarget() {
        return target;
    }

    @SuppressWarnings("unused")
    public Instant getSchedule() {
        return schedule;
    }

    @SuppressWarnings("unused")
    public Person getSubmitter() {
        return submitter;
    }
}

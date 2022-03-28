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

package org.craftercms.studio.model.rest.workflow;

import javax.validation.constraints.NotEmpty;
import java.time.ZonedDateTime;
import java.util.List;

public class WorkflowPackagesApproveRequestBody {

    @NotEmpty
    private String siteId;
    @NotEmpty
    private List<ApprovedPackage> packages;
    private ZonedDateTime schedule;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public List<ApprovedPackage> getPackages() {
        return packages;
    }

    public void setPackages(List<ApprovedPackage> packages) {
        this.packages = packages;
    }

    public ZonedDateTime getSchedule() {
        return schedule;
    }

    public void setSchedule(ZonedDateTime schedule) {
        this.schedule = schedule;
    }

    public class ApprovedPackage {

        @NotEmpty
        private String packageId;
        private String approverComment;

        public String getPackageId() {
            return packageId;
        }

        public void setPackageId(String packageId) {
            this.packageId = packageId;
        }

        public String getApproverComment() {
            return approverComment;
        }

        public void setApproverComment(String approverComment) {
            this.approverComment = approverComment;
        }
    }
}

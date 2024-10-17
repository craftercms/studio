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

import java.time.Instant;

/**
 * Request body for approving a package
 */
public class ApproveRequestBody extends ReviewPackageRequestBody {

    private Instant schedule;
    private boolean updateSchedule;

    public Instant getSchedule() {
        return schedule;
    }

    public void setSchedule(final Instant schedule) {
        this.schedule = schedule;
    }

    public boolean isUpdateSchedule() {
        return updateSchedule;
    }

    public void setUpdateSchedule(boolean updateSchedule) {
        this.updateSchedule = updateSchedule;
    }
}
